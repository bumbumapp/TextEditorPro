package net.gsantner.markor.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.gsantner.markor.R;

import java.util.ArrayDeque;
import java.util.List;

public class TextToolsActivity extends ToolBaseActivity {
    private interface Op {
        String apply(String s);
    }

    private EditText _input;
    private final ArrayDeque<String> _undo = new ArrayDeque<>();
    private final Handler _handler = new Handler(Looper.getMainLooper());
    private final Runnable _refresh = this::refresh;
    private boolean _programmatic;

    @Override
    protected int layoutId() {
        return R.layout.tool__texttools;
    }

    @Override
    protected int titleId() {
        return R.string.tool_text_name;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _input = findViewById(R.id.tt_input);

        final ViewGroup caseChips = findViewById(R.id.tt_chips_case);
        final ViewGroup lineChips = findViewById(R.id.tt_chips_lines);
        op(caseChips, R.string.tt_upper, TextToolsEngine::upper);
        op(caseChips, R.string.tt_lower, TextToolsEngine::lower);
        op(caseChips, R.string.tt_title, TextToolsEngine::title);
        op(caseChips, R.string.tt_sentence, TextToolsEngine::sentence);
        op(lineChips, R.string.tt_sort_az, TextToolsEngine::sortAsc);
        op(lineChips, R.string.tt_sort_za, TextToolsEngine::sortDesc);
        op(lineChips, R.string.tt_sort_len, TextToolsEngine::sortByLength);
        op(lineChips, R.string.tt_reverse, TextToolsEngine::reverseLines);
        op(lineChips, R.string.tt_dedupe, TextToolsEngine::removeDuplicateLines);
        op(lineChips, R.string.tt_remove_empty, TextToolsEngine::removeEmptyLines);
        op(lineChips, R.string.tt_trim, TextToolsEngine::trimSpaces);
        op(lineChips, R.string.tt_number, TextToolsEngine::numberLines);
        op(lineChips, R.string.tt_join, TextToolsEngine::joinLines);

        _input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int a, final int b, final int c) {
            }

            @Override
            public void onTextChanged(final CharSequence s, final int a, final int b, final int c) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                _handler.removeCallbacks(_refresh);
                _handler.postDelayed(_refresh, _programmatic ? 0 : 200);
            }
        });
        findViewById(R.id.tt_paste).setOnClickListener(v -> {
            final String t = clipboardText();
            if (t.isEmpty()) {
                toast(R.string.tool_nothing);
            } else {
                setText(t, true);
            }
        });
        findViewById(R.id.tt_clear).setOnClickListener(v -> {
            if (_input.length() > 0) {
                setText("", true);
            }
        });
        findViewById(R.id.tt_undo).setOnClickListener(v -> {
            if (!_undo.isEmpty()) {
                setText(_undo.pop(), false);
            }
        });
        findViewById(R.id.tool_copy).setOnClickListener(v -> copyText(_input.getText().toString()));
        findViewById(R.id.tool_share).setOnClickListener(v -> shareText(_input.getText().toString()));
        findViewById(R.id.tool_save).setOnClickListener(v -> {
            final String t = _input.getText().toString();
            String first = t.trim().split("\n", 2)[0];
            if (first.length() > 40) {
                first = first.substring(0, 40);
            }
            saveAsNote(t, first.isEmpty() ? getString(R.string.tool_text_name) + " " + NoteFiles.today() : first, "txt");
        });
        refresh();
    }

    private void op(final ViewGroup parent, final int label, final Op f) {
        addChip(parent, getString(label), v -> {
            final String in = _input.getText().toString();
            if (in.isEmpty()) {
                toast(R.string.tool_nothing);
                return;
            }
            final String out = f.apply(in);
            if (!out.equals(in)) {
                setText(out, true);
            }
        });
    }

    private void setText(final String text, final boolean pushUndo) {
        if (pushUndo) {
            _undo.push(_input.getText().toString());
            while (_undo.size() > 20) {
                _undo.removeLast();
            }
        }
        _programmatic = true;
        _input.setText(text);
        _input.setSelection(text.length());
        _programmatic = false;
    }

    private void refresh() {
        final String text = _input.getText().toString();
        final TextToolsEngine.Stats s = TextToolsEngine.stats(text);
        ((TextView) findViewById(R.id.tt_stat_words)).setText(String.valueOf(s.words));
        ((TextView) findViewById(R.id.tt_stat_chars)).setText(String.valueOf(s.characters));
        ((TextView) findViewById(R.id.tt_stat_lines)).setText(String.valueOf(s.lines));
        ((TextView) findViewById(R.id.tt_stat_read)).setText(s.readingSeconds < 60
                ? getString(R.string.tt_read_sec, s.readingSeconds)
                : getString(R.string.tt_read_min, Math.round(s.readingSeconds / 60f)));
        findViewById(R.id.tt_undo).setAlpha(_undo.isEmpty() ? 0.4f : 1f);

        final LinearLayout list = findViewById(R.id.tt_words_list);
        list.removeAllViews();
        final List<TextToolsEngine.WordCount> top = TextToolsEngine.topWords(text, 6);
        findViewById(R.id.tt_words_empty).setVisibility(top.isEmpty() ? View.VISIBLE : View.GONE);
        final int max = top.isEmpty() ? 1 : top.get(0).count;
        for (final TextToolsEngine.WordCount wc : top) {
            list.addView(wordRow(wc, max));
        }
    }

    private View wordRow(final TextToolsEngine.WordCount wc, final int max) {
        final LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(32)));
        final TextView w = new TextView(this);
        w.setText(wc.word);
        w.setSingleLine(true);
        w.setEllipsize(android.text.TextUtils.TruncateAt.END);
        w.setTextColor(getResources().getColor(R.color.home_text));
        w.setTextSize(14);
        row.addView(w, new LinearLayout.LayoutParams(dp(96), ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout track = new LinearLayout(this);
        track.setOrientation(LinearLayout.HORIZONTAL);
        final View fill = new View(this);
        fill.setBackgroundResource(R.drawable.tool_bar_fill);
        fill.getBackground().mutate().setTint(getResources().getColor(R.color.home_hero_end));
        track.addView(fill, new LinearLayout.LayoutParams(0, dp(8), Math.max(wc.count, 0)));
        final View rest = new View(this);
        track.addView(rest, new LinearLayout.LayoutParams(0, dp(8), Math.max(max - wc.count, 0)));
        track.setBackgroundResource(R.drawable.tool_bar_fill);
        row.addView(track, new LinearLayout.LayoutParams(0, dp(8), 1f));

        final TextView c = new TextView(this);
        c.setText(String.valueOf(wc.count));
        c.setGravity(Gravity.END);
        c.setTextColor(getResources().getColor(R.color.home_text_dim));
        c.setTextSize(13);
        final LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(36), ViewGroup.LayoutParams.WRAP_CONTENT);
        cp.setMarginStart(dp(8));
        row.addView(c, cp);
        return row;
    }

    @Override
    protected void onDestroy() {
        _handler.removeCallbacks(_refresh);
        super.onDestroy();
    }
}
