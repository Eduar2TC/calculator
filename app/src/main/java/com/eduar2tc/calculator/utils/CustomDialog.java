package com.eduar2tc.calculator.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.AppCompatDialog;
import androidx.core.text.HtmlCompat;

import com.eduar2tc.calculator.R;
import com.eduar2tc.calculator.ui.activities.SettingsActivity;

import java.util.List;
import java.util.Objects;

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
        applyRadioButtonTint(context, lightRadioButton, darkRadioButton, defaultRadioButton);  //apply radio button tint
    }
    private void applyRadioButtonTint(Context context, RadioButton... radioButtons) {
        int uiMode = context.getResources().getConfiguration().uiMode;
        boolean isNightMode = (uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        int color = context.getResources().getColor(isNightMode ? android.R.color.white : android.R.color.black, context.getTheme()); //TODO: cambiar los colores hardcodeados
        for (RadioButton rb : radioButtons) {
            rb.setButtonTintList(android.content.res.ColorStateList.valueOf(color));
        }
    }

    @SuppressLint("NonConstantResourceId")
    public void onOptionsItemSelected(MenuItem item, Context context) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.theme_item:
                showThemeDialog(context);
                break;
            case R.id.about_item:
                showPrivacyPolicyDialog(context);
                break;
            case R.id.settings:
                Intent intent = new Intent(context, SettingsActivity.class);
                context.startActivity(intent);
                break;
        }

    }
    //change themes and save in shared preferences
    @SuppressLint("UseCompatLoadingForDrawables")
    private void showThemeDialog(Context context) {
        final Dialog dialog = new AppCompatDialog(context, R.style.DialogTheme);
        dialog.setTitle(R.string.theme_dialog_title);
        // sharePreferences
        SharePrefs sharePrefs = SharePrefs.getInstance(context);
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
            int selectedId = radioGroup.getCheckedRadioButtonId();
            String selectedTheme = "system";
            if (selectedId == lightRadioButton.getId()) {
                selectedTheme = "light";
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else if (selectedId == darkRadioButton.getId()) {
                selectedTheme = "dark";
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else if (selectedId == defaultRadioButton.getId()) {
                selectedTheme = "system";
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            SharePrefs.getInstance(context).setTheme(selectedTheme);
            if (context instanceof Activity) {
                ((Activity) context).recreate();
            }
            dialog.dismiss();
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
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(context.getDrawable(R.drawable.bg_dialog));

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

    private void showPrivacyPolicyDialog(Context context) {
        final Dialog dialog = new AppCompatDialog(context, R.style.DialogTheme);
        dialog.setTitle(R.string.privacy_policy_dialog_title);

        // Create ScrollView
        ScrollView scrollView = new ScrollView(context);

        // Create LinearLayout for ScrollView content
        LinearLayout scrollContentLayout = new LinearLayout(context);
        scrollContentLayout.setOrientation(LinearLayout.VERTICAL);

        // Content
        TextView content = new TextView(context);
        content.setText(HtmlCompat.fromHtml(context.getString(R.string.privacy_policy_html), HtmlCompat.FROM_HTML_MODE_COMPACT));
        content.setTextSize(16);
        content.setPadding(0, 20, 0, 20);

        // Add content to ScrollView layout
        scrollContentLayout.addView(content);

        // Add ScrollView layout to ScrollView
        scrollView.addView(scrollContentLayout);

        // Create main container layout
        LinearLayout containerLinearLayout = new LinearLayout(context);
        containerLinearLayout.setOrientation(LinearLayout.VERTICAL);

        // Add ScrollView to container layout with weight 1
        containerLinearLayout.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        );

        // Create bottom section
        LinearLayout bottomSection = new LinearLayout(context);
        bottomSection.setOrientation(LinearLayout.HORIZONTAL);
        bottomSection.setGravity(Gravity.END);
        bottomSection.setPadding(0, 10, 0, 10);

        Button acceptButton = new Button(context);
        acceptButton.setBackgroundColor(Color.TRANSPARENT);
        acceptButton.setText(R.string.privacy_policy_dialog_accept);
        acceptButton.setOnClickListener(view -> {
            dialog.dismiss();
        });
        Button contactButton = new Button(context);
        contactButton.setBackgroundColor(Color.TRANSPARENT);
        contactButton.setText(R.string.privacy_policy_dialog_contact);
        contactButton.setOnClickListener(view -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:eduar2tc@gmail.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.privacy_policy_dialog_email_subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, R.string.privacy_policy_dialog_email_body);

            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(emailIntent, 0);
            boolean isIntentSafe = !activities.isEmpty();

            if (isIntentSafe) {
                try {
                    context.startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, R.string.privacy_policy_app_not_found , Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, R.string.privacy_policy_app_not_found, Toast.LENGTH_SHORT).show();
            }
        });
        bottomSection.addView(contactButton);
        bottomSection.addView(acceptButton);


        // Add bottom section to container layout
        containerLinearLayout.addView(bottomSection, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        );

        // Set container layout as the content view of the dialog
        dialog.setContentView(containerLinearLayout);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.bg_dialog));

        dialog.show();
    }
}
