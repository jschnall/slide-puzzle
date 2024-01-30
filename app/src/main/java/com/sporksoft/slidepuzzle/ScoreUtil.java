package com.sporksoft.slidepuzzle;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.HashMap;

public class ScoreUtil {
    private static final String SCORE_FILE = "scores.txt";

    private static ScoreUtil mInstance;
    private static HashMap<String, Long> mScores;
    private static Context mContext;
    
    private ScoreUtil() {
        loadScores();
    }
    
    public static ScoreUtil getInstance(Context context) {
        mContext = context;
        if (mInstance == null) {
            mInstance = new ScoreUtil();
        }
        
        return mInstance;
    }
    
    public void clearScores() {
        createScoreFile();
        mScores.clear(); 
        loadScores();
    }
    
    private void loadScores() {
        String[] sizes = mContext.getResources().getStringArray(R.array.pref_entryvalues_size);
        int len = sizes.length;
        mScores = new HashMap<String, Long>();
        
        FileInputStream fin = null;
        try {
            fin = mContext.openFileInput(SCORE_FILE);
            DataInputStream in = new DataInputStream(fin);
            for (int i = 0; i < len; i++) {
                mScores.put(sizes[i], new Long(in.readLong()));
            }
        } catch(FileNotFoundException fnfe) {
            createScoreFile();
        } catch(IOException ioe) {
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch(IOException ioe) {       
                }
            }
        }
    }
    
    private void createScoreFile() {
        FileOutputStream fout = null;

        String[] sizes = mContext.getResources().getStringArray(R.array.pref_entryvalues_size);
        int len = sizes.length;
        try {
            fout = mContext.openFileOutput(SCORE_FILE, Context.MODE_PRIVATE);
            DataOutputStream out = new DataOutputStream(fout);
            for (int i = 0; i < len; i++) {
                out.writeLong(0);
                mScores.put(sizes[i], 0L);
            }
        } catch(IOException ioe) {
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch(IOException ioe) {
                }
            }
        }
    }
    
    public long[] getAllScores() {
        String[] sizes = mContext.getResources().getStringArray(R.array.pref_entryvalues_size);

        int len = mScores.size();
        long[] allScores = new long[len];
        for (int i = 0; i < len; i++) {
            Long value = mScores.get(sizes[i]);
            allScores[i] = value;
        }
        return allScores;
    }
        
    public boolean updateScores(long time, int size) {
        String[] sizes = mContext.getResources().getStringArray(R.array.pref_entryvalues_size);

        String key = String.valueOf(size);
        long score = mScores.get(key).longValue();
        if (time < score || score <= 0) {
            // new high score
            mScores.remove(key);
            mScores.put(key, new Long(time));
            
            FileOutputStream fout = null;
            try {
                fout = mContext.openFileOutput(SCORE_FILE, Context.MODE_PRIVATE);
                DataOutputStream out = new DataOutputStream(fout);
                
                int len = mScores.size();
                for (int i = 0; i < len; i++) {
                    Long value = mScores.get(sizes[i]);
                    out.writeLong(value.longValue());
                }

            } catch(IOException ioe) {
            } finally {
                if(fout != null) {
                    try {
                        fout.close();
                    } catch(IOException ioe) {
                    }
                }
            }
            return true;
        }
        return false;
    }    
}
