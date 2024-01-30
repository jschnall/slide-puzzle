package com.sporksoft.slidepuzzle;

import com.sporksoft.graphics.ColorPickerDialog;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class SelectColorPreference extends Preference {
    private class ColorSelectionListener implements ColorPickerDialog.OnColorSelectedListener{
        public void colorSelected(int color) {
            SelectColorPreference.this.persistInt(color);
        }
    }

    public SelectColorPreference(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    protected void onClick() {
        ColorPickerDialog picker = new ColorPickerDialog(
                getContext(), new ColorSelectionListener(), getPersistedInt(getContext().getResources().getColor(R.drawable.default_fg_color)));
        picker.setCanceledOnTouchOutside(true);
        picker.show();
    }
}
