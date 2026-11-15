package net.gsantner.markor.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

/** TextView whose start drawable and text are centered together as one group (RTL aware). */
public class IconTextButton extends AppCompatTextView {
    public IconTextButton(final Context c) {
        super(c);
    }

    public IconTextButton(final Context c, final AttributeSet a) {
        super(c, a);
    }

    public IconTextButton(final Context c, final AttributeSet a, final int s) {
        super(c, a, s);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final Drawable d = getCompoundDrawablesRelative()[0];
        if (d != null && getLayout() != null) {
            float text = 0;
            for (int i = 0; i < getLayout().getLineCount(); i++) {
                text = Math.max(text, getLayout().getLineWidth(i));
            }
            final float group = d.getIntrinsicWidth() + getCompoundDrawablePadding() + text;
            final float free = getWidth() - getPaddingLeft() - getPaddingRight() - group;
            if (free > 0) {
                final boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
                canvas.translate(rtl ? -free / 2f : free / 2f, 0);
            }
        }
        super.onDraw(canvas);
    }
}
