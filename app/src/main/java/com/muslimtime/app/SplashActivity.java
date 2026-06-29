package com.muslimtime.app;

import android.app.Activity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressWarnings("deprecation")
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySystemBars();
        buildSplash();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 900);
    }

    private void buildSplash() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(isDarkTheme() ? Color.parseColor("#0B1D1A") : Color.parseColor("#F7FAF8"));

        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.bg_muslim_time);
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        background.setAlpha(isDarkTheme() ? 0.18f : 0.5f);
        root.addView(background, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        View wash = new View(this);
        wash.setBackgroundColor(isDarkTheme() ? 0xE60B1D1A : 0xBBF7FAF8);
        root.addView(wash, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(24), dp(24), dp(24), dp(24));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher_foreground);
        content.addView(logo, new LinearLayout.LayoutParams(dp(128), dp(128)));

        TextView title = new TextView(this);
        title.setText(R.string.app_name);
        title.setTextSize(30);
        title.setTextColor(isDarkTheme() ? Color.parseColor("#35D0B6") : Color.parseColor("#0F766E"));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(R.string.splash_subtitle);
        subtitle.setTextSize(15);
        subtitle.setTextColor(isDarkTheme() ? Color.parseColor("#C4D3CD") : Color.parseColor("#405650"));
        subtitle.setGravity(Gravity.CENTER);
        content.addView(subtitle);

        root.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(root);
    }

    private void applySystemBars() {
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setStatusBarColor(isDarkTheme() ? Color.parseColor("#071C19") : Color.parseColor("#115E59"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private boolean isDarkTheme() {
        return Preferences.isDarkTheme(this);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
