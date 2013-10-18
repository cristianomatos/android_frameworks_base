package com.android.internal.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.InputChannel;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.InputEventReceiver;
import android.view.MotionEvent;
import android.view.View;

public class QuickPeekInputReceiver extends InputEventReceiver {
	private final Context mContext;
		
	public QuickPeekInputReceiver(InputChannel inputChannel, Looper looper,
			Context context){
		super(inputChannel, looper);
		mContext = context;
	}
		
	@Override
    public void onInputEvent(InputEvent event) {
        boolean handled = false;
        try {
            if (event instanceof MotionEvent
                    && (event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
                final MotionEvent motionEvent = (MotionEvent)event;
                onTouchEvent(motionEvent);
                handled = true;
            }
        } finally {
            finishInputEvent(event, handled);
        }
    }
	
    private boolean mQuickPeekAction = false;
    private boolean mNtQsShadeActive = false;
    private float mQuickPeekInitialY;
    private float mQuickPeekInitialX;
    
    public int getStatusBarHeight() {
        return mContext.getResources().getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
    }
    
    public boolean onTouchEvent(MotionEvent ev) {
    	final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(mNtQsShadeActive && ev.getY() > getStatusBarHeight()) {
                   	if(Settings.System.getInt(mContext.getContentResolver(), Settings.System.NOTIFICATION_SHADE_ACTIVE, 0) == 0){
                   		Settings.System.putInt(mContext.getContentResolver(),
                       		Settings.System.FORCE_SHOW_STATUS_BAR, 0);
                 		mNtQsShadeActive = false;
                	}
                } else if (!mQuickPeekAction && ev.getY() < getStatusBarHeight()) {
                	mQuickPeekInitialY = ev.getY();
                	mQuickPeekInitialX = ev.getX();
                	mQuickPeekAction = true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (!mQuickPeekAction) {
                    break;
                }
                float deltaY = Math.abs(ev.getY() - mQuickPeekInitialY);
                float deltaX = Math.abs(ev.getX() - mQuickPeekInitialX);
                if (deltaY < getStatusBarHeight() ||
                        deltaY < deltaX * 2) {
                        mQuickPeekAction = false;
                }
                if (mQuickPeekAction) {
                    Settings.System.putInt(mContext.getContentResolver(),
                    		Settings.System.FORCE_SHOW_STATUS_BAR, 1);
                    mNtQsShadeActive = true;
                    mQuickPeekAction = false;    
                }

                break;
        }
        return true;
    }
}
