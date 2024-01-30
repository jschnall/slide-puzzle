/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sporksoft.graphics;

import com.sporksoft.graphics.HuePickerView.OnHueChangedListener;
import com.sporksoft.graphics.HuePickerView.OnHueSelectedListener;
import com.sporksoft.graphics.LightnessPickerView.OnLightnessChangedListener;
import com.sporksoft.slidepuzzle.R;

import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerDialog extends Dialog implements OnHueChangedListener, OnHueSelectedListener, OnLightnessChangedListener {
    private OnColorSelectedListener mColorListener;
    private int mInitialColor;
    private HuePickerView mHuePicker;
    private LightnessPickerView mLightnessPicker;
    
    public interface OnColorSelectedListener {
        void colorSelected(int color);
    }

    public ColorPickerDialog(Context context,
                             OnColorSelectedListener listener,
                             int initialColor) {
        super(context);
        
        mColorListener = listener;
        mInitialColor = initialColor;
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.color_picker);
        setTitle("Pick a Color");

        mLightnessPicker = (LightnessPickerView) findViewById(R.id.lightness_picker);
        mLightnessPicker.setColor(mInitialColor);        
        mLightnessPicker.setOnLightnessChangedListener(this);

        mHuePicker = (HuePickerView) findViewById(R.id.hue_picker);
        mHuePicker.setColor(mInitialColor);
        mHuePicker.setOnHueChangedListener(this);        
        mHuePicker.setOnHueSelectedListener(this);        
    }
    
    public void hueChanged(int color) {
        mLightnessPicker.setColor(color);
        mLightnessPicker.invalidate();
    }

    public void hueSelected(int color) {
        mColorListener.colorSelected(color);
        dismiss();
    }

    public void lightnessChanged(int color) {
        mHuePicker.setColor(color);
        mHuePicker.invalidate();
    }

}
