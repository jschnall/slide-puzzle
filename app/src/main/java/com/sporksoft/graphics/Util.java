package com.sporksoft.graphics;

import android.content.Context;

public class Util {
	public static int dipToPixels(Context context, int dip) {
		final float scale = context.getResources().getDisplayMetrics().density;
		
		return (int) (dip * scale + 0.5f);
	}
	
	public static int spToPixels(Context context, int sp) {
		final float scale = context.getResources().getDisplayMetrics().scaledDensity;
		
		return (int) (sp * scale + 0.5f);
	}

}
