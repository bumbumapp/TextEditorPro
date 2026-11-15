package net.gsantner.markor.tools;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.gsantner.markor.R;

public class JsonStudioActivity extends ToolBaseActivity {
    private static final String SAMPLE = "{\"name\":\"Text Editor Pro\",\"version\":\"1.3\",\"features\":[\"Text Tools\",\"JSON Studio\",\"Templates\",\"Compare\"],\"settings\":{\"autosave\":true,\"fontSize\":14,\"theme\":null}}";
    private static final String KEY_MODE = "mode", KEY_INDENT = "indent";

    private EditText _input;
    private TextView _status, _output;
    private final TextView[] _modeChips = new TextView[2];
    private final TextView[] _indentChips = new TextView[3];
    private int _mode; // 0 format, 1 minify
    private int _indentIdx; // 0=2, 1=4, 2=tab
    private final Handler _handler = new Handler(Looper.getMainLooper());
    private final Runnable _refresh = this::refresh;
    private String _lastOutput = "";

    @Override
    protected int layoutId() {
        return R.layout.tool__json;
    }

    @Override
    protected int titleId() {
        return R.string.tool_json_name;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            _mode = savedInstanceState.getInt(KEY_MODE);
            _indentIdx = savedInstanceState.getInt(KEY_INDENT);
        }
        _input = findViewById(R.id.js_input);
        _status = findViewById(R.id.js_status);
        _output = findViewById(R.id.js_output);

        final ViewGroup opts = findViewById(R.id.js_options);
        _modeChips[0] = addChip(opts, getString(R.string.js_format), v -> setMode(0));
        _modeChips[1] = addChip(opts, getString(R.string.js_minify), v -> setMode(1));
        final ViewGroup indents = findViewById(R.id.js_indents);
        _indentChips[0] = addChip(indents, getString(R.string.js_indent_2), v -> setIndent(0));
        _indentChips[1] = addChip(indents, getString(R.string.js_indent_4), v -> setIndent(1));
        _indentChips[2] = addChip(indents, getString(R.string.js_indent_tab), v -> setIndent(2));

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
                _handler.postDelayed(_refresh, 250);
            }
        });
        findViewById(R.id.js_sample).setOnClickListener(v -> _input.setText(SAMPLE));
        findViewById(R.id.js_paste).setOnClickListener(v -> {
            final String t = clipboardText();
            if (t.isEmpty()) {
                toast(R.string.tool_nothing);
            } else {
                _input.setText(t);
            }
        });
        findViewById(R.id.js_clear).setOnClickListener(v -> _input.setText(""));
        findViewById(R.id.tool_copy).setOnClickListener(v -> copyText(_lastOutput));
        findViewById(R.id.tool_share).setOnClickListener(v -> shareText(_lastOutput));
        findViewById(R.id.tool_save).setOnClickListener(v ->
                saveAsNote(_lastOutput, getString(R.string.tool_json_name) + " " + NoteFiles.today(), "json"));
        refresh();
    }

    @Override
    protected void onSaveInstanceState(final Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt(KEY_MODE, _mode);
        out.putInt(KEY_INDENT, _indentIdx);
    }

    private void setMode(final int m) {
        _mode = m;
        refresh();
    }

    private void setIndent(final int i) {
        _indentIdx = i;
        if (_mode == 1) {
            _mode = 0;
        }
        refresh();
    }

    private void refresh() {
        _modeChips[0].setSelected(_mode == 0);
        _modeChips[1].setSelected(_mode == 1);
        for (int i = 0; i < 3; i++) {
            _indentChips[i].setSelected(_mode == 0 && _indentIdx == i);
        }
        final String text = _input.getText().toString();
        if (text.trim().isEmpty()) {
            _lastOutput = "";
            _output.setText(R.string.js_output_hint);
            _output.setTextColor(getResources().getColor(R.color.tool_mono_dim));
            setStatus(true, getString(R.string.js_status_idle));
            return;
        }
        final int indent = _mode == 1 ? 0 : (_indentIdx == 0 ? 2 : (_indentIdx == 1 ? 4 : -1));
        final JsonStudio.Result r = JsonStudio.process(text, indent);
        if (r.ok) {
            _lastOutput = r.output;
            _output.setText(r.output.length() > 200_000 ? r.output.substring(0, 200_000) + "\n…" : r.output);
            _output.setTextColor(getResources().getColor(R.color.home_text));
            setStatus(true, getString(R.string.js_valid, r.keys, r.depth));
        } else {
            _lastOutput = "";
            _output.setText(R.string.js_output_error);
            _output.setTextColor(getResources().getColor(R.color.tool_mono_dim));
            setStatus(false, getString(R.string.js_invalid, r.line, r.column, errorText(r.error)));
        }
    }

    private void setStatus(final boolean ok, final String text) {
        _status.setText(text);
        _status.setBackgroundResource(ok ? R.drawable.tool_pill_ok : R.drawable.tool_pill_err);
        _status.setTextColor(getResources().getColor(ok ? R.color.tool_ok_fg : R.color.tool_err_fg));
        _status.setCompoundDrawablesRelativeWithIntrinsicBounds(ok ? R.drawable.ic_tool_check : R.drawable.ic_tool_alert, 0, 0, 0);
        _status.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(ok ? R.color.tool_ok_fg : R.color.tool_err_fg)));
    }

    private String errorText(final String code) {
        switch (code) {
            case "empty":
                return getString(R.string.js_err_empty);
            case "trailing":
                return getString(R.string.js_err_trailing);
            case "unexpected_end":
                return getString(R.string.js_err_unexpected_end);
            case "expected_key":
                return getString(R.string.js_err_expected_key);
            case "expected_colon":
                return getString(R.string.js_err_expected_colon);
            case "expected_comma":
                return getString(R.string.js_err_expected_comma);
            case "unterminated_string":
                return getString(R.string.js_err_unterminated_string);
            case "control_char":
                return getString(R.string.js_err_control_char);
            case "bad_escape":
                return getString(R.string.js_err_bad_escape);
            case "bad_number":
                return getString(R.string.js_err_bad_number);
            case "depth":
                return getString(R.string.js_err_depth);
            default:
                return getString(R.string.js_err_unexpected);
        }
    }

    @Override
    protected void onDestroy() {
        _handler.removeCallbacks(_refresh);
        super.onDestroy();
    }
}
