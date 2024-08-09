package com.eduar2tc.calculator.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
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

import java.util.List;

public class CustomThemeDialog {
    RadioGroup radioGroup;
    RadioButton lightRadioButton;
    RadioButton darkRadioButton;
    RadioButton defaultRadioButton;
    public CustomThemeDialog(Context context) {

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
        if (item.getItemId() == R.id.about_item) {
            showPrivacyPolicyDialog(context);
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
    @SuppressLint("QueryPermissionsNeeded")
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
        content.setText(HtmlCompat.fromHtml("<html>\n" +
                "<body>\n" +
                "<h2>Privacy Policy</h2>\n" +
                "<p>[Individual or Company Name] built the [App Name] app as a [open source | free | freemium | ad-supported | commercial] app. This SERVICE is provided by [Individual or company name] [at no cost] and is intended\n" +
                "    for use as is.</p>\n" +
                "<p>This page is used to inform website visitors regarding [my|our] policies with the collection, use, and\n" +
                "    disclosure of Personal Information if anyone decided to use [my|our] Service.</p>\n" +
                "<p>If you choose to use [my|our] Service, then you agree to the collection and use of information in\n" +
                "    relation with this policy. The Personal Information that [I|we] collect are used for providing and\n" +
                "    improving the Service. [I|We] will not use or share your information with anyone except as described\n" +
                "    in this Privacy Policy.</p>\n" +
                "<p>The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions,\n" +
                "    which is accessible at [App Name], unless otherwise defined in this Privacy Policy.</p>\n" +
                "\n" +
                "<p><strong>Information Collection and Use</strong></p>\n" +
                "<p>For a better experience while using our Service, [I|we] may require you to provide us with certain\n" +
                "    personally identifiable information, including but not limited to [add whatever else you collect here, e.g. users name | address | location | pictures]. \n" +
                "\tThe information that [I|we] request is [retained on your device and is not\n" +
                "    collected by [me|us] in any way]|[will be retained by us and used as described in this privacy policy.</p>\n" +
                "<p>The app does use third party services that may collect information used to identify you. [You can mention Google services here and link to Google's privacy policy if you want].\n" +
                "\n" +
                "<p><strong>Log Data</strong></p>\n" +
                "<p>[I|We] want to inform you that whenever you use [my|our] Service, in case of an error in the app [I|we] collect\n" +
                "    data and information (through third party products) on your phone called Log Data. This Log Data\n" +
                "    may include information such as your devices’s Internet Protocol (“IP”) address, device name,\n" +
                "    operating system version, configuration of the app when utilising [my|our] Service, the time and date\n" +
                "    of your use of the Service, and other statistics.</p>\n" +
                "\n" +
                "<p><strong>Cookies</strong></p>\n" +
                "<p>Cookies are files with small amount of data that is commonly used an anonymous unique identifier.\n" +
                "    These are sent to your browser from the website that you visit and are stored on your devices’s\n" +
                "    internal memory.</p>\n" +
                "<p>>!-- Check if this is true for your app, if unsure, just assume that you do use cookies and modify this next line -->This Services does not uses these “cookies” explicitly. However, the app may use third party code\n" +
                "    and libraries that use “cookies” to collection information and to improve their services. You\n" +
                "    have the option to either accept or refuse these cookies, and know when a cookie is being sent\n" +
                "    to your device. If you choose to refuse our cookies, you may not be able to use some portions of\n" +
                "    this Service.</p>\n" +
                "\n" +
                "<p><strong>Service Providers</strong></p> <!-- This part need seem like it's not needed, but if you use any Google services, or any other third party libraries, chances are, you need this. -->\n" +
                "<p>[I|We] may employ third-party companies and individuals due to the following reasons:</p>\n" +
                "<ul>\n" +
                "    <li>To facilitate our Service;</li>\n" +
                "    <li>To provide the Service on our behalf;</li>\n" +
                "    <li>To perform Service-related services; or</li>\n" +
                "    <li>To assist us in analyzing how our Service is used.</li>\n" +
                "</ul>\n" +
                "<p>[I|We] want to inform users of this Service that these third parties have access to your Personal\n" +
                "    Information. The reason is to perform the tasks assigned to them on our behalf. However, they\n" +
                "    are obligated not to disclose or use the information for any other purpose.</p>\n" +
                "\n" +
                "<p><strong>Security</strong></p>\n" +
                "<p>[I|We] value your trust in providing us your Personal Information, thus we are striving to use\n" +
                "    commercially acceptable means of protecting it. But remember that no method of transmission over\n" +
                "    the internet, or method of electronic storage is 100% secure and reliable, and [I|we] cannot\n" +
                "    guarantee its absolute security.</p>\n" +
                "\n" +
                "<p><strong>Links to Other Sites</strong></p>\n" +
                "<p>This Service may contain links to other sites. If you click on a third-party link, you will be\n" +
                "    directed to that site. Note that these external sites are not operated by [me|us]. Therefore, I\n" +
                "    strongly advise you to review the Privacy Policy of these websites. I have no control over, and\n" +
                "    assume no responsibility for the content, privacy policies, or practices of any third-party\n" +
                "    sites or services.</p>\n" +
                "\n" +
                "<p><strong>Children’s Privacy</strong></p>\n" +
                "<p>This Services do not address anyone under the age of 13. [I|We] do not knowingly collect personal\n" +
                "    identifiable information from children under 13. In the case [I|we] discover that a child under 13\n" +
                "    has provided [me|us] with personal information, [I|we] immediately delete this from our servers. If you\n" +
                "    are a parent or guardian and you are aware that your child has provided us with personal\n" +
                "    information, please contact [me|us] so that [I|we] will be able to do necessary actions.</p>\n" +
                "\n" +
                "<p><strong>Changes to This Privacy Policy</strong></p>\n" +
                "<p>[I|We] may update our Privacy Policy from time to time. Thus, you are advised to review this page\n" +
                "    periodically for any changes. [I|We] will notify you of any changes by posting the new Privacy Policy\n" +
                "    on this page. These changes are effective immediately, after they are posted on this page.</p>\n" +
                "\n" +
                "<p><strong>Contact Us</strong></p>\n" +
                "<p>If you have any questions or suggestions about [my|our] Privacy Policy, do not hesitate to contact\n" +
                "    [me|us].</p>\n" +
                "<p>This Privacy Policy page was created at <a href=\"https://privacypolicytemplate.net\"\n" +
                "                                              target=\"_blank\">privacypolicytemplate.net</a>.</p>\n" +
                "</body>\n" +
                "</html>", HtmlCompat.FROM_HTML_MODE_LEGACY));
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
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Create bottom section
        LinearLayout bottomSection = new LinearLayout(context);
        bottomSection.setOrientation(LinearLayout.HORIZONTAL);
        bottomSection.setGravity(Gravity.END);
        bottomSection.setPadding(0, 20, 0, 20);

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
            boolean isIntentSafe = activities.size() > 0;

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
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Set container layout as the content view of the dialog
        dialog.setContentView(containerLinearLayout);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.dialog_bg));

        dialog.show();
    }
}
