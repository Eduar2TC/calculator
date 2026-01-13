package com.eduar2tc.calculator.activities;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.utils.SharePrefs;

public class Settings extends AppCompatActivity {
    private static ConstraintLayout cardViewHibernationConstraint;
    private static SwitchCompat switchHibernation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //apply theme
        applyTheme();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        initToolbar();
        cardViewHibernationConstraint = findViewById(R.id.card_view_hibernation_constraint);
        switchHibernation = findViewById(R.id.hibernation_switch);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        hibernation();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar) ;
        setSupportActionBar(toolbar);
        // Forzar color del título y el icono de navegación en modo día y noche usando solo el color definido
        int overflowColor = ContextCompat.getColor(this, R.color.overflow_menu_item_text_color);
        toolbar.setTitleTextColor(overflowColor);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_24);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(overflowColor);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
            //add icon back button
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_24);
        }
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
        cardViewHibernationConstraint.setOnClickListener(v -> switchHibernation.toggle());
        switchHibernation.setOnClickListener(v -> switchHibernation.toggle());
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