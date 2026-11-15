package net.gsantner.markor.tools;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import net.gsantner.markor.R;
import net.gsantner.markor.activity.DocumentActivity;
import net.gsantner.markor.activity.MarkorBaseActivity;

import java.io.File;

/** Shared plumbing for the tool screens: header, clipboard, share and "save as note". */
public abstract class ToolBaseActivity extends MarkorBaseActivity {

    protected abstract int layoutId();

    protected abstract @StringRes int titleId();

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId());
        ((TextView) findViewById(R.id.tool_title)).setText(titleId());
        findViewById(R.id.tool_back).setOnClickListener(v -> finish());
    }

    protected String clipboardText() {
        try {
            final ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip() != null && cm.getPrimaryClip().getItemCount() > 0) {
                final CharSequence t = cm.getPrimaryClip().getItemAt(0).coerceToText(this);
                return t == null ? "" : t.toString();
            }
        } catch (final RuntimeException ignored) {
        }
        return "";
    }

    protected void copyText(final String text) {
        if (text == null || text.isEmpty()) {
            toast(R.string.tool_nothing);
            return;
        }
        try {
            final ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("text", text));
            toast(R.string.tool_copied);
        } catch (final RuntimeException e) {
            toast(R.string.tool_nothing);
        }
    }

    protected void shareText(final String text) {
        if (text == null || text.isEmpty()) {
            toast(R.string.tool_nothing);
            return;
        }
        final Intent i = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text);
        try {
            startActivity(Intent.createChooser(i, getString(R.string.tool_share)));
        } catch (final RuntimeException e) {
            toast(R.string.tool_nothing);
        }
    }

    /** Writes the text into the notebook folder (no storage permission needed) and opens it in the editor. */
    protected void saveAsNote(final String text, final String baseName, final String extension) {
        if (text == null || text.isEmpty()) {
            toast(R.string.tool_nothing);
            return;
        }
        final File f = NoteFiles.create(_appSettings instanceof net.gsantner.markor.util.AppSettings
                ? ((net.gsantner.markor.util.AppSettings) _appSettings).getNotebookDirectory() : getFilesDir(), baseName, extension, text);
        if (f == null) {
            toast(R.string.tool_save_failed);
            return;
        }
        Toast.makeText(this, getString(R.string.tool_saved, f.getName()), Toast.LENGTH_SHORT).show();
        DocumentActivity.launch(this, f, false, null, null);
    }

    protected void toast(@StringRes final int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    /** Adds an operation chip to a flow container. */
    protected TextView addChip(final ViewGroup parent, final String label, final View.OnClickListener l) {
        final TextView t = new TextView(this);
        t.setText(label);
        t.setOnClickListener(l);
        // ToolChip style supplies size, margins, background and font; the layout params must be created here.
        final ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(40));
        lp.setMarginEnd(dp(8));
        lp.bottomMargin = dp(8);
        t.setLayoutParams(lp);
        t.setBackgroundResource(R.drawable.tool_chip_bg);
        t.setGravity(android.view.Gravity.CENTER);
        t.setPadding(dp(14), 0, dp(14), 0);
        t.setTextColor(getResources().getColor(R.color.tool_chip_text));
        t.setTextSize(13);
        t.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(this, R.font.jakarta_bold));
        parent.addView(t);
        return t;
    }

    protected int dp(final int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
