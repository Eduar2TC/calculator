package com.eduar2tc.calculator.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatDialog;

import com.eduar2tc.calculator.R;

public class CustomDialog {
    RadioGroup radioGroup;
    RadioButton lightRadioButton;
    RadioButton darkRadioButton;
    RadioButton defaultRadioButton;
    public CustomDialog(Context context) {
        initializeComponents(context);
    }

    private void initializeComponents(Context context) {
        radioGroup = new RadioGroup(context);
        lightRadioButton = new RadioButton(context);
        lightRadioButton.setText(R.string.theme_dialog_light);
        darkRadioButton = new RadioButton(context);
        darkRadioButton.setText(R.string.theme_dialog_dark);
        defaultRadioButton = new RadioButton(context);
        defaultRadioButton.setText(R.string.theme_dialog_default);
    }

    public void onOptionsItemSelected(MenuItem item, Context context) {
        if (item.getItemId() == R.id.theme_item) {
            showThemeDialog(context);
        }
    }
    private void showThemeDialog(Context context) {
        final Dialog dialog = new AppCompatDialog(context, R.style.DialogTheme);
        dialog.setTitle(R.string.theme_dialog_title);
        // Layout
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout horizontalLayout = new LinearLayout(context);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        //add margin around radio button (pixels)
        int marginTop = 35;
        int paddingLeft = 30;
        LinearLayout.LayoutParams darkRadioButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams lightRadioButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams defaultButtonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout.LayoutParams horizontalLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        darkRadioButtonParams.setMargins(0, marginTop, 0, 0);
        lightRadioButtonParams.setMargins(0, marginTop, 0, 0);
        defaultButtonParams.setMargins(0, marginTop, 0, 0);

        darkRadioButton.setLayoutParams(darkRadioButtonParams);
        lightRadioButton.setLayoutParams(lightRadioButtonParams);
        defaultRadioButton.setLayoutParams(defaultButtonParams);
        horizontalLayout.setLayoutParams(horizontalLayoutParams);

        darkRadioButton.setPadding(paddingLeft,0,0,0);
        lightRadioButton.setPadding(paddingLeft,0,0,0);
        defaultRadioButton.setPadding(paddingLeft,0,0,0);
        //add radio buttons to group
        radioGroup.addView(lightRadioButton);
        radioGroup.addView(darkRadioButton);
        radioGroup.addView(defaultRadioButton);

        //add group to layout
        linearLayout.addView(radioGroup);
        //add horizontal layout to vertical layout
        linearLayout.addView(horizontalLayout);
        //buttons accept and cancel
        Button acceptButton = new Button(context);
        acceptButton.setBackgroundColor(Color.TRANSPARENT);
        acceptButton.setText(R.string.about_dialog_accept);
        acceptButton.setOnClickListener(view -> {
            //handle theme selection
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == lightRadioButton.getId()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                dialog.dismiss();
            } else if (selectedId == darkRadioButton.getId()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                dialog.dismiss();
            } else if (selectedId == defaultRadioButton.getId()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                dialog.dismiss();
            }
        });

        Button cancelButton = new Button(context);
        cancelButton.setBackgroundColor(Color.TRANSPARENT);
        cancelButton.setText(R.string.theme_dialog_cancel);
        cancelButton.setOnClickListener(view -> {
            dialog.dismiss();
        });
        horizontalLayout.setGravity(Gravity.END);
        // add buttons to horizontal layout
        horizontalLayout.addView(cancelButton);
        horizontalLayout.addView(acceptButton);

        // add layout to dialog
        dialog.setContentView(linearLayout);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_bg));

        //set selected radio button based on current system theme
        int uiMode = context.getResources().getConfiguration().uiMode;
        boolean isNightMode = (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if(isNightMode){
            darkRadioButton.setChecked(true);
        }else {
            lightRadioButton.setChecked(true);
        }
        dialog.show();
    }
}
