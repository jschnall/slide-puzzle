package com.sporksoft.slidepuzzle;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class HighScoreListAdapter extends ArrayAdapter {
    public HighScoreListAdapter(Context context, List<ScoreItem> scores) {
    	super(context, R.layout.high_score_list_item, R.id.puzzle_size, scores);
    }
    
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
    	HighScoreListItem listItem;
    	ScoreItem item = (ScoreItem) this.getItem(position);
    	
    	if (view == null) {
    	    LayoutInflater factory = LayoutInflater.from(getContext());
    		listItem = (HighScoreListItem) factory.inflate(
    				R.layout.high_score_list_item, viewGroup, false);
    	} else {
    	    listItem = (HighScoreListItem) view;
    	}
    	
    	listItem.init(item);
    	
    	return listItem;
    }
}
