package net.danmercer.ponderizer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import net.danmercer.ponderizer.memorize.MemorizeTestActivity;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static class InternalLinkSpan extends ClickableSpan {
        View.OnClickListener mListener;

        public InternalLinkSpan(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View widget) {
            mListener.onClick(widget);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView body = (TextView) findViewById(R.id.textView4);
        body.setMovementMethod(LinkMovementMethod.getInstance());

        // Set up the text
        Spanned text = Html.fromHtml(getString(R.string.about_text_body));
        final String shareString = getString(R.string.about_text_share_snippet);
        int shareStart = text.toString().indexOf(shareString);
        SpannableString spannableText = new SpannableString(text);

        InternalLinkSpan shareLinkSpan = new InternalLinkSpan(this);
        spannableText.setSpan(shareLinkSpan, shareStart, shareStart + shareString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        // Put text in TextView
        body.setText(spannableText);
    }

    @Override
    public void onClick(View v) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this Ponderizer app for Android! \n\n"
                + MemorizeTestActivity.PLAYSTORE_URL);
        shareIntent.setType("text/plain");
        Intent chooser = Intent.createChooser(shareIntent, "Share with...");
        startActivity(chooser);
    }
}
