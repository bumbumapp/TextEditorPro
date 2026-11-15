package net.gsantner.markor.tools;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/** Minimal wrapping layout (RTL aware) used for operation chips. Children may use margins. */
public class FlowLayout extends ViewGroup {
    public FlowLayout(final Context c) {
        super(c);
    }

    public FlowLayout(final Context c, final AttributeSet a) {
        super(c, a);
    }

    public FlowLayout(final Context c, final AttributeSet a, final int s) {
        super(c, a, s);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(final AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(final LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(final LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected void onMeasure(final int wSpec, final int hSpec) {
        final int maxW = MeasureSpec.getSize(wSpec) - getPaddingLeft() - getPaddingRight();
        int x = 0, y = 0, rowH = 0, usedW = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View c = getChildAt(i);
            if (c.getVisibility() == GONE) continue;
            measureChildWithMargins(c, wSpec, 0, hSpec, 0);
            final MarginLayoutParams lp = (MarginLayoutParams) c.getLayoutParams();
            final int w = c.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            final int h = c.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (x > 0 && x + w > maxW) {
                y += rowH;
                x = 0;
                rowH = 0;
            }
            x += w;
            usedW = Math.max(usedW, x);
            rowH = Math.max(rowH, h);
        }
        y += rowH;
        setMeasuredDimension(resolveSize(usedW + getPaddingLeft() + getPaddingRight(), wSpec),
                resolveSize(y + getPaddingTop() + getPaddingBottom(), hSpec));
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        final int width = r - l;
        final int maxW = width - getPaddingLeft() - getPaddingRight();
        final boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
        int x = 0, y = getPaddingTop(), rowH = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View c = getChildAt(i);
            if (c.getVisibility() == GONE) continue;
            final MarginLayoutParams lp = (MarginLayoutParams) c.getLayoutParams();
            final int w = c.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            final int h = c.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            if (x > 0 && x + w > maxW) {
                y += rowH;
                x = 0;
                rowH = 0;
            }
            final int left = rtl ? width - getPaddingRight() - x - w + lp.leftMargin : getPaddingLeft() + x + lp.leftMargin;
            c.layout(left, y + lp.topMargin, left + c.getMeasuredWidth(), y + lp.topMargin + c.getMeasuredHeight());
            x += w;
            rowH = Math.max(rowH, h);
        }
    }
}
