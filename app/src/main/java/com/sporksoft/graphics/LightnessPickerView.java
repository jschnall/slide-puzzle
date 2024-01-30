package com.sporksoft.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class LightnessPickerView extends View {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 200;

    private Paint mRectPaint;
    private OnLightnessChangedListener mListener;
    private int[] mColors;

    public interface OnLightnessChangedListener {
        void lightnessChanged(int value);
    }

    public LightnessPickerView(Context context, OnLightnessChangedListener l, int color) {
        super(context);
        mListener = l;

        init();
        setColor(color);
    }

    public LightnessPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setOnLightnessChangedListener(OnLightnessChangedListener l) {
        mListener = l;
    }

    private void init() {
        mRectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mRectPaint.setStrokeWidth(32);        
    }
    
    public void setColor(int color) {
        mColors = new int[] { 0xffffffff, color, 0xff000000 };
        Shader s = new LinearGradient(0, 0, 0, 200, 
                mColors, null,  Shader.TileMode.REPEAT);
        
        mRectPaint.setShader(s);
    }
    
    @Override 
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(getWidth()-32, getTop(), getWidth(), getBottom(), mRectPaint);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(WIDTH, HEIGHT);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                int color = interpColor(mColors, y/getHeight());
                mListener.lightnessChanged(color);
                break;
            }
            case MotionEvent.ACTION_UP: {
                break;                
            }
        }
        return true;
    }
    
    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }
        
        float p = unit * (colors.length - 1);
        int i = (int)p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i+1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);
        
        return Color.argb(a, r, g, b);
    }
    
    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }



}

