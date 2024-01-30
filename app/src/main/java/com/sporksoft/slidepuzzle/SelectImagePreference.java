package com.sporksoft.slidepuzzle;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.MediaColumns;
import android.util.AttributeSet;

import java.util.Random;

public class SelectImagePreference extends Preference {
    public static final String IMAGE_TYPE_UNSPECIFIED = "image/*";

    public final static int IMAGE_DEFAULT = 0;
    public final static int IMAGE_RANDOM = 1;
    public final static int IMAGE_CUSTOM = 2;

    private final Context mContext;
    private int mSelection;
    private Uri mCustomLocation;

    private class SelectionListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            mSelection = which;
            if (which == IMAGE_CUSTOM && mContext instanceof Activity) {
                Intent innerIntent = new Intent();
                innerIntent.setType(IMAGE_TYPE_UNSPECIFIED);

                if (Build.VERSION.SDK_INT < 19){
                    innerIntent.setAction(Intent.ACTION_GET_CONTENT);
                } else {
                    innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    innerIntent.addCategory(Intent.CATEGORY_OPENABLE);
                }

                Intent wrapperIntent = Intent.createChooser(innerIntent, mContext.getString(R.string.choose_image));
                ((Activity) mContext).startActivityForResult(wrapperIntent, PuzzlePreferenceActivity.REQUEST_CODE_LOAD_IMAGE);
            }
        }
    }
    
    public void setCustomLocation(Uri location) {
        mCustomLocation = location;
    }
    
    private class ConfirmationListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            SelectImagePreference.this.persistInt(mSelection);
            if (mSelection == IMAGE_CUSTOM && mCustomLocation != null) {
                saveCustomImagePreference(mContext, mCustomLocation);
            } else if (mSelection == IMAGE_RANDOM) {
                saveRandomImagePreference(mContext, getRandomImage(mContext.getContentResolver()));
            }

        }
        
    }
    
    public SelectImagePreference(Context context, AttributeSet attributes) {
        super(context, attributes);
        mContext = context;
    }

    @Override
    protected void onClick() {        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //builder.setIcon();
        builder.setTitle(R.string.pref_image_source_dialog_title);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(R.array.pref_entries_image_source, getPersistedInt(1), new SelectionListener());
        builder.setPositiveButton(R.string.dialog_yes, new ConfirmationListener());
        builder.setNegativeButton(R.string.dialog_no, null);
        builder.show();        
    }    
    
    //TODO merge the following 2 methods into 1
    public static void saveRandomImagePreference(Context context, Uri uri) {
    	if (uri == null)
    		return;
    	
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        
        editor.putString(PuzzlePreferenceActivity.RANDOM_PUZZLE_IMAGE, uri.toString());
        editor.commit();
    }
    
    private static void saveCustomImagePreference(Context context, Uri uri) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).edit();
        
        editor.putString(PuzzlePreferenceActivity.CUSTOM_PUZZLE_IMAGE, uri.toString());
        editor.commit();
    }

    
    public static Uri getRandomImage(ContentResolver resolver) {
        //TODO might also want to try doing a union query and construct the uri from the file 
        //returned by querying DATA

        // An array specifying which columns to return. 
        String[] projection = new String[] {
            BaseColumns._ID
            //Media.DATA
        };
        Uri uri = new Random().nextInt(1) == 0 ? Media.EXTERNAL_CONTENT_URI : Media.INTERNAL_CONTENT_URI;
 
        Cursor cursor =  Media.query(resolver, uri, projection, null, MediaColumns._ID);
        if (cursor == null || cursor.getCount() <= 0) {
        	return null;
        }
        
        cursor.moveToPosition(new Random().nextInt(cursor.getCount()));
        
        return Uri.withAppendedPath(uri, cursor.getString(0));
    }

}
