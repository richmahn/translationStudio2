package com.door43.translationstudio.spannables;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import com.door43.translationstudio.R;
import com.door43.translationstudio.util.MainContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by joel on 1/27/2015.
 */
public class VerseSpan extends Span {
    public static final String PATTERN = "<verse\\s+number=\"(\\d+)\"\\s+style=\"v\"\\s*/>";
    private int mVerseNumber;
    private SpannableStringBuilder mSpannable;

    /**
     * Creates a new verse span
     * @param verse
     */
    public VerseSpan(String verse) {
        super(Integer.parseInt(verse)+"", "<verse number=\""+Integer.parseInt(verse)+"\" style=\"v\" />");
        mVerseNumber = Integer.parseInt(verse);
    }

    /**
     * Creates a new verse span
     * @param verse
     */
    public VerseSpan(int verse) {
        super(verse+"", "<verse number=\""+verse+"\" style=\"v\" />");
        mVerseNumber = verse;
    }

    /**
     * Returns the verse number
     * @return
     */
    public int getVerseNumber() {
        return mVerseNumber;
    }

    /**
     * Generates the spannable.
     * This provides caching so we can look up the span in the text later
     * @return
     */
    @Override
    public SpannableStringBuilder render() {
        if(mSpannable == null) {
            mSpannable = super.render();
            // apply custom styles
            mSpannable.setSpan(new RelativeSizeSpan(0.8f), 0, mSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mSpannable.setSpan(new ForegroundColorSpan(MainContext.getContext().getResources().getColor(R.color.gray)), 0, mSpannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return mSpannable;
    }

    /**
     * Parses a usx string into a verse span
     * @param usx
     * @return
     */
    public static VerseSpan parseVerse(String usx) {
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(usx);
        while(matcher.find()) {
            return new VerseSpan(matcher.group(1));
        }
        return null;
    }
}
