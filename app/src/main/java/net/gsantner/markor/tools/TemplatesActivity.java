package net.gsantner.markor.tools;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.gsantner.markor.R;

import java.util.ArrayList;
import java.util.List;

public class TemplatesActivity extends ToolBaseActivity {
    private static final String KEY_SEL = "selected";

    private static final class Tpl {
        final int name, body, icon, bg, fg;

        Tpl(final int name, final int body, final int icon, final int bg, final int fg) {
            this.name = name;
            this.body = body;
            this.icon = icon;
            this.bg = bg;
            this.fg = fg;
        }
    }

    private final Tpl[] _tpls = {
            new Tpl(R.string.tpl_todo_name, R.string.tpl_todo_body, R.drawable.ic_tpl_todo, R.drawable.home_sq_green, R.color.home_fg_green),
            new Tpl(R.string.tpl_meeting_name, R.string.tpl_meeting_body, R.drawable.ic_tpl_meeting, R.drawable.home_sq_violet, R.color.home_fg_violet),
            new Tpl(R.string.tpl_journal_name, R.string.tpl_journal_body, R.drawable.ic_tpl_journal, R.drawable.home_sq_blue, R.color.home_fg_blue),
            new Tpl(R.string.tpl_shopping_name, R.string.tpl_shopping_body, R.drawable.ic_tpl_shopping, R.drawable.home_sq_orange, R.color.home_fg_orange),
            new Tpl(R.string.tpl_weekly_name, R.string.tpl_weekly_body, R.drawable.ic_tpl_weekly, R.drawable.home_sq_teal, R.color.home_fg_teal),
            new Tpl(R.string.tpl_project_name, R.string.tpl_project_body, R.drawable.ic_tpl_project, R.drawable.home_sq_violet, R.color.home_fg_violet),
            new Tpl(R.string.tpl_book_name, R.string.tpl_book_body, R.drawable.ic_tpl_book, R.drawable.home_sq_orange, R.color.home_fg_orange),
            new Tpl(R.string.tpl_recipe_name, R.string.tpl_recipe_body, R.drawable.ic_tpl_recipe, R.drawable.home_sq_green, R.color.home_fg_green),
    };
    private final List<View> _cards = new ArrayList<>();
    private int _selected;

    @Override
    protected int layoutId() {
        return R.layout.tool__templates;
    }

    @Override
    protected int titleId() {
        return R.string.tool_tpl_name;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            _selected = Math.max(0, Math.min(_tpls.length - 1, savedInstanceState.getInt(KEY_SEL)));
        }
        final LinearLayout grid = findViewById(R.id.tp_grid);
        final LayoutInflater inf = LayoutInflater.from(this);
        for (int i = 0; i < _tpls.length; i += 2) {
            final LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setWeightSum(2);
            final LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.bottomMargin = dp(12);
            row.setLayoutParams(rp);
            for (int k = 0; k < 2; k++) {
                final int idx = i + k;
                final View card = inf.inflate(R.layout.tool__template_card, row, false);
                final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
                if (k == 0) lp.setMarginEnd(dp(6));
                else lp.setMarginStart(dp(6));
                final Tpl t = _tpls[idx];
                final ImageView icon = card.findViewById(R.id.tp_icon);
                icon.setBackgroundResource(t.bg);
                icon.setImageResource(t.icon);
                icon.setImageTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(t.fg)));
                ((TextView) card.findViewById(R.id.tp_name)).setText(t.name);
                ((TextView) card.findViewById(R.id.tp_preview)).setText(NoteFiles.preview(getString(t.body), NoteFiles.today()));
                card.setOnClickListener(v -> select(idx));
                _cards.add(card);
                row.addView(card);
            }
            grid.addView(row);
        }
        findViewById(R.id.tp_create).setOnClickListener(v -> create());
        select(_selected);
    }

    @Override
    protected void onSaveInstanceState(final Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt(KEY_SEL, _selected);
    }

    private void select(final int idx) {
        _selected = idx;
        for (int i = 0; i < _cards.size(); i++) {
            _cards.get(i).setSelected(i == idx);
        }
        ((TextView) findViewById(R.id.tp_create)).setText(getString(R.string.tp_create_from, getString(_tpls[idx].name)));
    }

    private void create() {
        final Tpl t = _tpls[_selected];
        final String body = NoteFiles.fill(getString(t.body), NoteFiles.today());
        saveAsNote(body, getString(t.name) + " " + NoteFiles.today(), "md");
    }
}
