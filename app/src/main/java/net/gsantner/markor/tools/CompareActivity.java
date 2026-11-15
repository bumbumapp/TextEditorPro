package net.gsantner.markor.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineBackgroundSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.gsantner.markor.R;

public class CompareActivity extends ToolBaseActivity {
    private static final int MAX_ROWS = 1500;
    private EditText _a, _b;
    private String _lastSummary = "";
    private String _lastDiff = "";

    @Override
    protected int layoutId() {
        return R.layout.tool__compare;
    }

    @Override
    protected int titleId() {
        return R.string.tool_cmp_name;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _a = findViewById(R.id.cp_a);
        _b = findViewById(R.id.cp_b);
        findViewById(R.id.cp_paste_a).setOnClickListener(v -> paste(_a));
        findViewById(R.id.cp_paste_b).setOnClickListener(v -> paste(_b));
        findViewById(R.id.cp_swap).setOnClickListener(v -> {
            final CharSequence a = _a.getText();
            _a.setText(_b.getText());
            _b.setText(a);
            if (findViewById(R.id.cp_result_card).getVisibility() == View.VISIBLE) {
                compare();
            }
        });
        findViewById(R.id.cp_compare).setOnClickListener(v -> compare());
        findViewById(R.id.cp_copy).setOnClickListener(v -> copyText(_lastDiff.isEmpty() ? "" : _lastSummary + "\n\n" + _lastDiff));
        if (savedInstanceState != null && (_a.length() > 0 || _b.length() > 0)
                && savedInstanceState.getBoolean("shown")) {
            compare();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle out) {
        super.onSaveInstanceState(out);
        out.putBoolean("shown", findViewById(R.id.cp_result_card).getVisibility() == View.VISIBLE);
    }

    private void paste(final EditText target) {
        final String t = clipboardText();
        if (t.isEmpty()) {
            toast(R.string.tool_nothing);
        } else {
            target.setText(t);
        }
    }

    private static final class RowBg implements LineBackgroundSpan {
        private final int color, pad;

        RowBg(final int color, final int pad) {
            this.color = color;
            this.pad = pad;
        }

        @Override
        public void drawBackground(final Canvas c, final Paint p, final int left, final int right, final int top, final int baseline,
                                   final int bottom, final CharSequence text, final int start, final int end, final int lnum) {
            final int old = p.getColor();
            p.setColor(color);
            c.drawRect(left - pad, top, right + pad, bottom, p);
            p.setColor(old);
        }
    }

    private void compare() {
        final String a = _a.getText().toString();
        final String b = _b.getText().toString();
        final View card = findViewById(R.id.cp_result_card);
        final TextView summary = findViewById(R.id.cp_summary);
        final TextView diff = findViewById(R.id.cp_diff);
        if (a.isEmpty() && b.isEmpty()) {
            toast(R.string.tool_nothing);
            return;
        }
        final TextDiff.Result r = TextDiff.diff(a, b);
        card.setVisibility(View.VISIBLE);
        if (r.tooLarge) {
            summary.setText(getString(R.string.cp_too_large, TextDiff.MAX_LINES));
            diff.setText("");
            _lastSummary = "";
            _lastDiff = "";
            return;
        }
        if (r.identical()) {
            summary.setText(R.string.cp_identical);
        } else {
            summary.setText(getString(R.string.cp_summary, r.added, r.removed, r.changed));
        }
        final SpannableStringBuilder sb = new SpannableStringBuilder();
        final StringBuilder plain = new StringBuilder();
        int shown = 0;
        for (final TextDiff.Line l : r.lines) {
            if (shown++ >= MAX_ROWS) {
                sb.append("…\n");
                break;
            }
            final String prefix = l.type == TextDiff.Type.ADDED ? "+ " : (l.type == TextDiff.Type.REMOVED ? "- " : "  ");
            final int start = sb.length();
            sb.append(prefix).append(l.text).append('\n');
            plain.append(prefix).append(l.text).append('\n');
            if (l.type != TextDiff.Type.SAME) {
                final boolean add = l.type == TextDiff.Type.ADDED;
                sb.setSpan(new RowBg(getResources().getColor(add ? R.color.tool_add_bg : R.color.tool_del_bg), dp(16)),
                        start, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.setSpan(new ForegroundColorSpan(getResources().getColor(add ? R.color.tool_ok_fg : R.color.tool_err_fg)),
                        start, sb.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        diff.setText(sb);
        _lastSummary = summary.getText().toString();
        _lastDiff = plain.toString();
    }
}
