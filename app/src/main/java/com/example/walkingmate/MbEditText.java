package com.example.walkingmate;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

public class MbEditText extends androidx.appcompat.widget.AppCompatEditText {
    private static final String TAG = "실행된것?";

    public MbEditText(Context context ) {
        super( context );
    }
    public MbEditText(Context context, AttributeSet attrs ) {
        super( context, attrs );
    }
    public boolean onKeyPreIme( int keyCode, KeyEvent event ) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                this.clearFocus();
                // Focus_Helper.releaseFocus (etx_email_input);
            }
        }
        return super.onKeyPreIme( keyCode, event );
    }

}
