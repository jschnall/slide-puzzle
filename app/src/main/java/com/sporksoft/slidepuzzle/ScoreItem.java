package com.sporksoft.slidepuzzle;

public class ScoreItem {
    private long mGameTime;
    private String mPuzzleSize;
    
    public ScoreItem(String size, long time) {
        mPuzzleSize = size;
        mGameTime = time;
    }
    
    public long getGameTime() {
        return mGameTime;
    }
    public String getPuzzleSize() {
        return mPuzzleSize;
    }
}
