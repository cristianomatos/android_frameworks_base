/*
 * Copyright (C) 2011 The Android Open Source Project
 * This code has been modified. Portions copyright (C) 2012, ParanoidAndroid Project.
 * Copyright (c) 2012-2013 The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
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

package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.ColorUtils;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.statusbar.policy.NetworkController;

import com.android.systemui.R;

// Intimately tied to the design of res/layout/signal_cluster_view.xml
public class SignalClusterView
        extends LinearLayout
        implements NetworkController.SignalCluster {

    static final boolean DEBUG = false;
    static final String TAG = "SignalClusterView";

    NetworkController mNC;

    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0, mWifiActivityId = 0;
    private boolean mMobileVisible = false;
    private int mMobileStrengthId = 0, mMobileActivityId = 0;
    private int mMobileTypeId = 0, mNoSimIconId = 0;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;
    private String mWifiDescription, mMobileDescription, mMobileTypeDescription;

    ViewGroup mWifiGroup, mMobileGroup;
    ImageView mWifi, mMobile, mWifiActivity, mMobileActivity, mMobileType, mAirplane, mNoSimSlot;
    TextView mMobileText,mWiFiText;
    View mSpacer;

    private ColorUtils.ColorSettingInfo mColorInfo;

    public SignalClusterView(Context context) {
        this(context, null);
    }

    public SignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mColorInfo = ColorUtils.getColorSettingInfo(context, Settings.System.STATUS_ICON_COLOR);
    }

    public void setNetworkController(NetworkController nc) {
        if (DEBUG) Slog.d(TAG, "NetworkController=" + nc);
        mNC = nc;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiActivity   = (ImageView) findViewById(R.id.wifi_inout);
        mMobileGroup    = (ViewGroup) findViewById(R.id.mobile_combo);
        mMobile         = (ImageView) findViewById(R.id.mobile_signal);
        mMobileActivity = (ImageView) findViewById(R.id.mobile_inout);
        mMobileType     = (ImageView) findViewById(R.id.mobile_type);
        mMobileText     = (TextView)  findViewById(R.id.signal_text);
        mNoSimSlot      = (ImageView) findViewById(R.id.no_sim);
        mWiFiText       = (TextView)  findViewById(R.id.wifi_signal_text);
        mSpacer         =             findViewById(R.id.spacer);
        mAirplane       = (ImageView) findViewById(R.id.airplane);

        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mWifiGroup      = null;
        mWifi           = null;
        mWifiActivity   = null;
        mMobileGroup    = null;
        mMobile         = null;
        mMobileActivity = null;
        mMobileType     = null;
        mNoSimSlot      = null;
        mMobileText     = null;
        mWiFiText       = null;
        mSpacer         = null;
        mAirplane       = null;

        super.onDetachedFromWindow();
    }

    @Override
    public void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon,
            String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiActivityId = activityIcon;
        mWifiDescription = contentDescription;

        apply();
    }

    @Override
    public void setMobileDataIndicators(boolean visible, int strengthIcon, int activityIcon,
            int typeIcon, String contentDescription, String typeContentDescription,
            int noSimIcon) {
        mMobileVisible = visible;
        mMobileStrengthId = strengthIcon;
        mMobileActivityId = activityIcon;
        mMobileTypeId = typeIcon;
        mMobileDescription = contentDescription;
        mMobileTypeDescription = typeContentDescription;
        mNoSimIconId = noSimIcon;

        apply();
    }

    @Override
    public void setIsAirplaneMode(boolean is, int airplaneIconId) {
        mIsAirplaneMode = is;
        mAirplaneIconId = airplaneIconId;

        apply();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mWifiVisible && mWifiGroup.getContentDescription() != null) {
            event.getText().add(mWifiGroup.getContentDescription());
        }
        if (mMobileVisible && mMobileGroup.getContentDescription() != null) {
            event.getText().add(mMobileGroup.getContentDescription());
        }
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    public void setColor(ColorUtils.ColorSettingInfo colorInfo) {
        mColorInfo = colorInfo;
        apply();
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        super.onRtlPropertiesChanged(layoutDirection);

        if (mWifi != null) {
            mWifi.setImageDrawable(null);
        }
        if (mWifiActivity != null) {
            mWifiActivity.setImageDrawable(null);
        }

        if (mMobile != null) {
            mMobile.setImageDrawable(null);
        }
        if (mMobileActivity != null) {
            mMobileActivity.setImageDrawable(null);
        }
        if (mMobileType != null) {
            mMobileType.setImageDrawable(null);
        }

        if(mAirplane != null) {
            mAirplane.setImageDrawable(null);
        }
        apply();
    }

    // Run after each indicator change.
    public void apply() {
        if (mWifiGroup == null) return;

        if (mWifiVisible) {
            mWifiGroup.setVisibility(View.VISIBLE);
            Drawable wifiBitmap = mContext.getResources().getDrawable(mWifiStrengthId);
            if (mColorInfo.isLastColorNull) {
                wifiBitmap.clearColorFilter();
            } else {
                wifiBitmap.setColorFilter(mColorInfo.lastColor, PorterDuff.Mode.SRC_IN);
            }
            mWifi.setImageDrawable(wifiBitmap);
            mWifiActivity.setImageResource(mWifiActivityId);

            mWifiGroup.setContentDescription(mWifiDescription);
            mWifiGroup.setVisibility(View.VISIBLE);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Slog.d(TAG,
                String.format("wifi: %s sig=%d act=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId, mWifiActivityId));

        if (mMobileVisible && !mIsAirplaneMode) {
            mMobileGroup.setVisibility(View.VISIBLE);
            if(mMobileStrengthId != 0) {
                Drawable mobileBitmap = mContext.getResources().getDrawable(mMobileStrengthId);
                if (mColorInfo.isLastColorNull) {
                    mobileBitmap.clearColorFilter();
                } else {
                    mobileBitmap.setColorFilter(mColorInfo.lastColor, PorterDuff.Mode.SRC_IN);
                }
                mMobile.setImageDrawable(mobileBitmap);
            }

            mMobile.setImageResource(mMobileStrengthId);
            mMobileActivity.setImageResource(mMobileActivityId);
            mMobileType.setImageResource(mMobileTypeId);

            mMobileGroup.setContentDescription(mMobileTypeDescription + " " + mMobileDescription);

            mMobileGroup.setVisibility(View.VISIBLE);
            mNoSimSlot.setImageResource(mNoSimIconId);
            if (showingSignalText && !mIsAirplaneMode) {
                mMobile.setVisibility(View.GONE);
                mMobileText.setVisibility(View.VISIBLE);
            } else{
                mMobile.setVisibility(View.VISIBLE);
                mMobileText.setVisibility(View.GONE);
            }
        } else {
            mMobileGroup.setVisibility(View.GONE);
        }

        if (mIsAirplaneMode) {
            mAirplane.setVisibility(View.VISIBLE);
            if(mAirplaneIconId != 0) {
                Drawable AirplaneBitmap = mContext.getResources().getDrawable(mAirplaneIconId);
                if (mColorInfo.isLastColorNull) {
                    mAirplane.clearColorFilter();
                } else {
                    mAirplane.setColorFilter(mColorInfo.lastColor, PorterDuff.Mode.SRC_IN);
                }
                mAirplane.setImageDrawable(AirplaneBitmap);
            }

            mAirplane.setImageResource(mAirplaneIconId);
            mAirplane.setVisibility(View.VISIBLE);
        } else {
            mAirplane.setVisibility(View.GONE);
        }

        if (mMobileVisible && mWifiVisible &&
                ((mIsAirplaneMode) || (mNoSimIconId != 0))) {
            mSpacer.setVisibility(View.INVISIBLE);
        } else {
            mSpacer.setVisibility(View.GONE);
        }

        if (DEBUG) Slog.d(TAG,
                String.format("mobile: %s sig=%d act=%d typ=%d",
                    (mMobileVisible ? "VISIBLE" : "GONE"),
                    mMobileStrengthId, mMobileActivityId, mMobileTypeId));

        mMobileType.setVisibility(
                !mWifiVisible ? View.VISIBLE : View.GONE);
    }
}