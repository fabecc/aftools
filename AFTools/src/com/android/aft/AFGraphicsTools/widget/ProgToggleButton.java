package com.android.aft.AFGraphicsTools.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class ProgToggleButton extends ToggleButton {

    public ProgToggleButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ProgToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgToggleButton(Context context) {
        super(context);
    }

    @Override
    // This method is override to avoid toggle by graphical click
    public void toggle() {
        // Do not toggle
    }

    // Use this method to toggle the button status
    public void ptoggle() {
        super.toggle();
    }

}
