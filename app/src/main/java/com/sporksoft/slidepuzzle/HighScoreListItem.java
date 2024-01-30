package com.sporksoft.slidepuzzle;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableLayout;
import android.widget.TextView;

public class HighScoreListItem extends TableLayout {
	private TextView mSize;
    private TextView mTime;
	
	public HighScoreListItem(Context context) {
		super(context);
	}
	
	public HighScoreListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mSize = (TextView) findViewById(R.id.puzzle_size);
        mTime = (TextView) findViewById(R.id.solve_time);
	}
	
	public void init(ScoreItem score) {
		mSize.setText(score.getPuzzleSize());
		
		long time = score.getGameTime();
		if (time <= 0) {
		    mTime.setText("-");
		} else {
		    mTime.setText(formatTime(time));
		}
	}
	
	private String formatTime(long time) {
	    int seconds = (int) (time / 1000);
	    int hours = seconds / 3600;
	    seconds %= 3600;
	    int minutes = seconds / 60;
	    seconds %= 60;
	    
        StringBuffer t = new StringBuffer();
            
        if(hours < 10) {
            t.append('0');
        }
        t.append(hours);
        t.append(':');
        if(minutes < 10) {
            t.append('0');
        }
        t.append(minutes);
        t.append(':');
        if(seconds < 10) {
            t.append('0');
        }
        t.append(seconds);
	    
	    return t.toString();
	}
}
