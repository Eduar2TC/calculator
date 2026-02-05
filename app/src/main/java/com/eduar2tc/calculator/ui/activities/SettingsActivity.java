package com.eduar2tc.calculator.ui.activities;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.utils.SharePrefs;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;

import java.util.Locale;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {
    // Views are instance properties of the Activity
    private CardView cardViewHibernationConstraint;
    private SwitchCompat switchHibernation;

    // Keys for SharedPreferences (used by MainActivity/ForwardingHelper)
    public static final String PREF_FORWARD_SLOP = "forward_slop_factor";
    public static final String PREF_FORWARD_MIN_DP = "forward_min_dp";
    public static final String PREF_FORWARD_SMOOTHING = "forward_smoothing";

    // UI controls for the sliders (Material Slider)
    private Slider sliderSlopFactor;
    private Slider sliderMinDp;
    private Slider sliderSmoothing;
    private TextView tvSlopValue;
    private TextView tvMinDpValue;
    private TextView tvSmoothingValue;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme fallback first (setDefaultNightMode) so Day/Night resource resolution is correct
        applyTheme();
        // Then apply the Settings-specific theme that responds to DayNight
        setTheme(R.style.AppTheme_Settings);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        initToolbar();
        // Obtain views directly (the layout now contains CardView and Sliders)
        // the layout now uses section_general_card as the container for the hibernation option
        cardViewHibernationConstraint = findViewById(R.id.section_general_card);
        switchHibernation = findViewById(R.id.hibernation_switch);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        hibernation();

        // Initialize forwarding sliders
        // Use local SharedPreferences to avoid dependency on androidx.preference
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        sliderSlopFactor = findViewById(R.id.slider_slop_factor);
        sliderMinDp = findViewById(R.id.slider_min_dp);
        sliderSmoothing = findViewById(R.id.slider_smoothing);

        // Find the button once (do not apply colors in code; rely on Day/Night XML resources)
        MaterialButton btnReset = findViewById(R.id.btn_reset_defaults);

        tvSlopValue = findViewById(R.id.tv_slop_value);
        tvMinDpValue = findViewById(R.id.tv_min_dp_value);
        tvSmoothingValue = findViewById(R.id.tv_smoothing_value);

        // Defaults
        float defaultSlop = 0.5f;
        float defaultMinDp = 8f;
        float defaultSmoothing = 0.85f;

        float slop = prefs.getFloat(PREF_FORWARD_SLOP, defaultSlop);
        float minDp = prefs.getFloat(PREF_FORWARD_MIN_DP, defaultMinDp);
        float smoothing = prefs.getFloat(PREF_FORWARD_SMOOTHING, defaultSmoothing);

        // Configure Sliders and map value <-> SharedPreferences
        if (sliderSlopFactor != null) {
            // Real range: 0.1 .. 2.0, step 0.01 for fine granularity
            sliderSlopFactor.setValueFrom(0.1f);
            sliderSlopFactor.setValueTo(2.0f);
            sliderSlopFactor.setStepSize(0.01f);
            sliderSlopFactor.setValue(slop);
            if (tvSlopValue != null) {
                tvSlopValue.setText(String.format(Locale.getDefault(), "%.2f", slop));
            }
            sliderSlopFactor.addOnChangeListener((slider, value, fromUser) -> {
                if (tvSlopValue != null) tvSlopValue.setText(String.format(Locale.getDefault(), "%.2f", value));
                prefs.edit().putFloat(PREF_FORWARD_SLOP, value).apply();
            });
        }

        if (sliderMinDp != null) {
            // Real range: 0 .. 32 (step 0.5)
            sliderMinDp.setValueFrom(0f);
            sliderMinDp.setValueTo(32f);
            sliderMinDp.setStepSize(0.5f);
            sliderMinDp.setValue(minDp);
            if (tvMinDpValue != null) {
                tvMinDpValue.setText(String.format(Locale.getDefault(), "%.1f dp", minDp));
            }
            sliderMinDp.addOnChangeListener((slider, value, fromUser) -> {
                if (tvMinDpValue != null) tvMinDpValue.setText(String.format(Locale.getDefault(), "%.1f dp", value));
                prefs.edit().putFloat(PREF_FORWARD_MIN_DP, value).apply();
            });
        }

        if (sliderSmoothing != null) {
            // Real range: 0.0 .. 1.0, step 0.01
            sliderSmoothing.setValueFrom(0f);
            sliderSmoothing.setValueTo(1f);
            sliderSmoothing.setStepSize(0.01f);
            sliderSmoothing.setValue(smoothing);
            if (tvSmoothingValue != null) {
                tvSmoothingValue.setText(String.format(Locale.getDefault(), "%.2f", smoothing));
            }
            sliderSmoothing.addOnChangeListener((slider, value, fromUser) -> {
                if (tvSmoothingValue != null) tvSmoothingValue.setText(String.format(Locale.getDefault(), "%.2f", value));
                prefs.edit().putFloat(PREF_FORWARD_SMOOTHING, value).apply();
            });
        }

        // Assign listener to the button (if exists) once; reset behavior always applies
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> resetToDefaults());
            // Ensure visible in case layout/theme hides it accidentally
            btnReset.setVisibility(View.VISIBLE);
        }

        // DO NOT apply programmatic tints: rely on Day/Night XML themes and selectors for dynamic color.

    }

    private void resetToDefaults() {
        // Defaults (must match those used above)
        float defaultSlop = 0.5f;
        float defaultMinDp = 8f;
        float defaultSmoothing = 0.85f;

        prefs.edit()
                .putFloat(PREF_FORWARD_SLOP, defaultSlop)
                .putFloat(PREF_FORWARD_MIN_DP, defaultMinDp)
                .putFloat(PREF_FORWARD_SMOOTHING, defaultSmoothing)
                .apply();

        // Update UI immediately (forwarding only)
        if (sliderSlopFactor != null) {
            sliderSlopFactor.setValue(defaultSlop);
            if (tvSlopValue != null) tvSlopValue.setText(String.format(Locale.getDefault(), "%.2f", defaultSlop));
        }
        if (sliderMinDp != null) {
            sliderMinDp.setValue(defaultMinDp);
            if (tvMinDpValue != null) tvMinDpValue.setText(String.format(Locale.getDefault(), "%.1f dp", defaultMinDp));
        }
        if (sliderSmoothing != null) {
            sliderSmoothing.setValue(defaultSmoothing);
            if (tvSmoothingValue != null) tvSmoothingValue.setText(String.format(Locale.getDefault(), "%.2f", defaultSmoothing));
        }
        // Do not touch switchHibernation: reset affects forwarding only
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar) ;
        setSupportActionBar(toolbar);
        // Remove programmatic tints that interfere with dynamic theme resolution.
        // Use colors defined in XML (ThemeOverlay.Settings.Toolbar and theme attrs).
        // Only ensure navigation functionality.
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
            //add icon back button
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24);
        }
        // Ensure the back arrow responds to click
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //TODO: Implement hibernation feature
   //activate switch hibernation when tap cardView or switch
    private void hibernation() {
        // Guard against nulls if the view is missing for any reason
        if (cardViewHibernationConstraint != null && switchHibernation != null) {
            cardViewHibernationConstraint.setOnClickListener(v -> switchHibernation.toggle());
            switchHibernation.setOnClickListener(v -> switchHibernation.toggle());
        }
        // If any view is null, do nothing (prevents crash on inflate/inconsistent themes)
    }
    //TODO: Fix the theme not applying when changing it
    private void applyTheme() {
        String theme = SharePrefs.getInstance(this).getTheme();
        switch (theme) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }
}
