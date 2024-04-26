package com.eduar2tc.calculator;

import android.graphics.Paint;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;

public class TextLengthFilter implements InputFilter {

    private final static int NO_MAX_CHARACTERS = -1;

    private Paint p;
    private int maxWidth;
    private static int maxCharacters;

    /**
     * A filter based on the maxWidth of the text.
     * @param p Paint used by the View
     * @param maxWidth Max width of the text (in pixels)
     */
    public TextLengthFilter(Paint p, int maxWidth) {
        this(p, maxWidth, NO_MAX_CHARACTERS);
    }

    /**
     * A filter based on the maxWidth of the text.
     * @param p Paint used by the View
     * @param maxWidth Max width of the text (in pixels)
     * @param maxCharacters Max amount of characters for the text
     */
    public TextLengthFilter(Paint p, int maxWidth, int maxCharacters) {
        this.p = p;
        this.maxWidth = maxWidth;
        this.maxCharacters = maxCharacters;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        float originalW = p.measureText(dest, 0, dest.length());
        //Calculate the amount of space that is being reclaimed by characters that are replaced
        int toBeReplacedWidth = p.breakText(dest, dstart, dend, true, originalW, null);
        float spaceLeft = (maxWidth - originalW) + toBeReplacedWidth;
        int selectionLength = (dend - dstart); //The amount of characters that are going to be replaced
        int changeLength = (end - start);

        //Check if there are more characters after the change than before.
        if (maxCharacters != NO_MAX_CHARACTERS && changeLength > selectionLength) {
            //If the length of the original text was already too many characters don't allow more to be added.
            if (dest.length() > maxCharacters) {
                return "";
            }
            int finalLength = dest.length() + changeLength - selectionLength;
            //Check if the final length, after the replacement, doesn't exceed the maximum characters
            if (finalLength > maxCharacters) {
                //if it does limit the characters to be added to not exceed the maximum
                end =  end - (finalLength - maxCharacters);
            }
        }
        //Check if the size of the characters does not exceed the maximum view width
        if (spaceLeft > 0) {
            int w = p.breakText(source, start, end, true, spaceLeft, null) ;
            //If not all characters would fit only allow the ones that do fit
            if (w != source.length())
                return source.subSequence(0, start + w);
        }
        else {
            return "";
        }
        return null;
    }
}