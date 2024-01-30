package com.sporksoft.slidepuzzle;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.sporksoft.actionbar.ActionBarActivity;

public class SlidePuzzleActivity extends ActionBarActivity implements OnKeyListener {
	private static final String LOG_TAG = SlidePuzzleActivity.class.getName();
	//private static final int ERROR_DIALOG_REQUEST_CODE = 1;
	
	private ImageView mCompleteView;
	private TileView mTileView;
	private Chronometer mTimerView;
    private long mTime;
    private Toast mToast;
    
    private SoundPool mSoundPool;
    private int mClickSound;
    private int mApplauseSound;
    private boolean mSoundOn;
    
    private AdView mAdView;
    private InterstitialAd mInterstitial;

    private AnimationListener mCompleteAnimListener = new AnimationListener() {
		@Override
		public void onAnimationEnd(Animation animation) {
			mTileView.setVisibility(View.GONE);
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {}

		@Override
		public void onAnimationStart(Animation animation) {}
	};

    private class ScoresListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton ) {
            switch (whichButton) {
                case AlertDialog.BUTTON_POSITIVE: {
                    showConfirmDeleteDialog();
                    return;
                }
                case AlertDialog.BUTTON_NEGATIVE: {
                    mTimerView.setBase(SystemClock.elapsedRealtime() - mTime);
                    if (!mTileView.isSolved()) {
                        mTimerView.start();
                    }
                    return;
                }
            }
        }
    }

    private class ScoresCancelListener implements OnCancelListener {
        public void onCancel(DialogInterface dialog) {
            mTimerView.setBase(SystemClock.elapsedRealtime() - mTime);
            if (!mTileView.isSolved()) {
                mTimerView.start();
            }        
        }
    }
    
    private class ConfirmDeleteListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int whichButton ) {
            if (whichButton == AlertDialog.BUTTON_POSITIVE) {
                ScoreUtil.getInstance(SlidePuzzleActivity.this).clearScores();                
            }
            showHighScoreListDialog();
        }
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.slide_puzzle);
        mTileView = (TileView) findViewById(R.id.tile_view);
        mTileView.requestFocus();
        mTileView.setOnKeyListener(this);
        
        mCompleteView = (ImageView) findViewById(R.id.complete_view);
        mCompleteView.setImageBitmap(mTileView.getCurrentImage());
        
        mTimerView = (Chronometer) findViewById(R.id.timer_view);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mTimerView.setTextColor(prefs.getInt(PuzzlePreferenceActivity.TIMER_COLOR, getResources().getColor(R.color.default_fg)));
        
        if (icicle == null) {
            int blankLoc = Integer.parseInt(prefs.getString(PuzzlePreferenceActivity.BLANK_LOCATION, String.valueOf(1)));
            mTileView.newGame(null, blankLoc, mTimerView);
            mTime = 0;
        } else {
        	Parcelable[] parcelables = icicle.getParcelableArray("tiles");
        	Tile[] tiles = null;
        	if (parcelables != null) {
        		int len = parcelables.length;
        		
        		tiles = new Tile[len];
        		for (int i = 0; i < len; i++) {
        			tiles[i] = (Tile) parcelables[i];
        		}
        	}
        	
            mTileView.newGame(tiles, icicle.getInt("blank_first"), mTimerView);
            mTime = icicle.getLong("time", 0);
        }
        
        LinearLayout adLayout = (LinearLayout) findViewById(R.id.ad_layout);
        mAdView = new AdView(this);
        mAdView.setAdUnitId(getResources().getString(R.string.ad_unit_id));
        mAdView.setAdSize(AdSize.SMART_BANNER);
        //mAdView.setAdListener(new ToastAdListener(this));
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        adLayout.addView(mAdView, params);
        //AdRequest adRequest = new AdRequest.Builder().build();
        AdRequest.Builder builder = new AdRequest.Builder();
        builder.addKeyword("puzzle");
        builder.addKeyword("photo");
        builder.addKeyword("game");

        // Create the interstitial.
        mInterstitial = new InterstitialAd(this);
        mInterstitial.setAdUnitId(getResources().getString(R.string.interstitial_ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitial.loadAd(adRequest);
        mInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                resetTimer();
            }
        });

        //int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        //if (ConnectionResult.SUCCESS == errorCode) {
            mAdView.loadAd(builder.build());
        //} else {
        //    GooglePlayServicesUtil.getErrorDialog(errorCode, this, ERROR_DIALOG_REQUEST_CODE).show();
        //}

        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        mClickSound = mSoundPool.load(this, R.raw.click, 1);
        mApplauseSound = mSoundPool.load(this, R.raw.applause, 1);        
    }

    @Override
    public void onResume() {
    	super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundOn = prefs.getBoolean(PuzzlePreferenceActivity.SOUND_ON, true);

    	//if (prefs.getBoolean(PuzzlePreferenceActivity.SHOW_STATUS, true)) {
        //    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);       	    
    	//} else {
        //    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);   
    	//}    	

	    int bgColor = prefs.getInt(PuzzlePreferenceActivity.BACKGROUND_COLOR, getResources().getColor(R.color.default_bg));
	    findViewById(R.id.layout).setBackgroundColor(bgColor);

    	mTileView.updateInstantPrefs();
        mTimerView.setBase(SystemClock.elapsedRealtime() - mTime);
    	if (!mTileView.isSolved()) {
    	    mTimerView.start();
    	}    	
    }
    
    @Override
    public void onPause() {
        if (!mTileView.isSolved()) {
            mTime = (SystemClock.elapsedRealtime() - mTimerView.getBase());
        }
        mTimerView.stop();
        
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
          }
        super.onDestroy();
    }
    
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // Prevent user from moving tiles if the puzzle has been solved 
        if (mTileView.isSolved()) {
            return false;
        }
        
        boolean moved;
    	if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_DOWN: {
                	moved = mTileView.move(TileView.DIR_DOWN);
                    break;
                }
                case KeyEvent.KEYCODE_DPAD_UP: {
                	moved = mTileView.move(TileView.DIR_UP);
                    break;
                }
                case KeyEvent.KEYCODE_DPAD_LEFT: {
                	moved = mTileView.move(TileView.DIR_LEFT);
                    break;
                }
                case KeyEvent.KEYCODE_DPAD_RIGHT: {
                	moved = mTileView.move(TileView.DIR_RIGHT);
                    break;
                }
                default:
                    return false;
            }

            if (moved) {
            	playSound(mClickSound);
            }

            if (mTileView.checkSolved()) {
            	mCompleteView.setImageBitmap(mTileView.getCurrentImage());
            	mCompleteView.setVisibility(View.VISIBLE);
            	
            	Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            	animation.setAnimationListener(mCompleteAnimListener);
            	mCompleteView.startAnimation(animation);
            	
                postScore();
            }
            return true;
        }
    	
        return false;
    }

    private void postScore() {
        mTime = SystemClock.elapsedRealtime() - mTimerView.getBase();
        mTimerView.stop();
        mTimerView.setBase(SystemClock.elapsedRealtime() - mTime);
        mTimerView.invalidate(); // make sure the actual final time is shown
        
        boolean isHighScore = ScoreUtil.getInstance(this).updateScores(mTime, mTileView.mSize);
        if (isHighScore) {
        	playSound(mApplauseSound);
            
            mToast = Toast.makeText(this, R.string.new_high_score, Toast.LENGTH_LONG);
            mToast.setGravity(Gravity.CENTER, 0, 0);
            mToast.show();
        }    
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(50);
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        // Prevent user from moving tiles if the puzzle has been solved 
        if (mTileView.isSolved()) {
            return false;
        }

        int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
            	mTileView.grabTile(event.getX(), event.getY());
            	return true;
            }
            case MotionEvent.ACTION_MOVE: {
            	mTileView.dragTile(event.getX(), event.getY());
            	return true;
            }
            case MotionEvent.ACTION_UP: {
            	boolean moved = mTileView.dropTile(event.getX(), event.getY());
            	
            	if (moved) {
            		playSound(mClickSound);
            	}
            	
                if (mTileView.checkSolved()) {
                	mCompleteView.setImageBitmap(mTileView.getCurrentImage());
                	mCompleteView.setVisibility(View.VISIBLE);

                	Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                	animation.setAnimationListener(mCompleteAnimListener);
                	mCompleteView.startAnimation(animation);

                	postScore();
                }
            	return true;
            }
        }
        
        return false;
    }  
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
    	
    	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_new: {
            	mTileView.setVisibility(View.VISIBLE);
            	mCompleteView.setVisibility(View.GONE);

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                int blankLoc = Integer.parseInt(prefs.getString(PuzzlePreferenceActivity.BLANK_LOCATION, String.valueOf(1)));
                long interstitialShown = prefs.getLong(PuzzlePreferenceActivity.INTERSTITIAL_SHOWN, 0);

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);

                mTileView.newGame(null, blankLoc, mTimerView);
                if (interstitialShown < today.getTimeInMillis()) {
                    // Only show interstitial ad once per day
                    displayInterstitial();
                } else {
                    resetTimer();
                }
                break;
            }
            case R.id.menu_scores: {
                if (!mTileView.isSolved()) {
                    mTime = (SystemClock.elapsedRealtime() - mTimerView.getBase());
                }
                mTimerView.stop();

                showHighScoreListDialog();
                break;
            }
            case R.id.menu_settings: {
            	Intent intent = new Intent(this, PuzzlePreferenceActivity.class);
                this.startActivity(intent);
            	break;
            }
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArray("tiles", mTileView.getTiles());
        outState.putInt("blank_first", mTileView.mBlankLocation);
        outState.putLong("time", mTime);
    }
        
    private void showHighScoreListDialog() {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.high_score_list, null);
        ListView listView = (ListView) layout.findViewById(R.id.score_list);
        ScoresListener listener = new ScoresListener();
        long[] times = ScoreUtil.getInstance(this).getAllScores();
        String[] sizes = getResources().getStringArray(R.array.pref_entries_size);
        int len = sizes.length;
        
        ArrayList<ScoreItem> scores = new ArrayList<ScoreItem>();
        for (int i = 0; i < len; i++) {
            scores.add(new ScoreItem(sizes[i], times[i]));
        }
        listView.setAdapter(new HighScoreListAdapter(this, scores));
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setIcon();
        builder.setTitle(R.string.scores_title);
        builder.setCancelable(true);
        builder.setView(layout);
        //builder.setAdapter(new HighScoreListAdapter(this, scores), null);
        builder.setPositiveButton(R.string.menu_clear, listener);
        builder.setNegativeButton(R.string.dialog_close, listener);
        builder.setOnCancelListener(new ScoresCancelListener());
        builder.show();
    }
    
    private void showConfirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_dialog_title);
        //builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_yes, new ConfirmDeleteListener());
        builder.setNegativeButton(R.string.dialog_no, new ConfirmDeleteListener());
        builder.setMessage(R.string.delete_dialog_msg);

        builder.show();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
    
    private void playSound(int sound) {
    	if (!mSoundOn) {
    		return;
    	}
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);  
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); 
        
        mSoundPool.play(sound, streamVolume, streamVolume, 1, 0, 1f);
    }

    public void displayInterstitial() {
        if (mInterstitial.isLoaded()) {
            mInterstitial.show();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putLong(PuzzlePreferenceActivity.INTERSTITIAL_SHOWN, System.currentTimeMillis()).commit();
        }
    }

    public void resetTimer() {
        mTime = 0;
        mTimerView.stop();
        mTimerView.setBase(SystemClock.elapsedRealtime());
        mTimerView.start();
    }
}