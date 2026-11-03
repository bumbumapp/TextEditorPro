package net.gsantner.markor.activity;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.gsantner.markor.R;
import net.gsantner.markor.util.AppSettings;
import net.gsantner.markor.util.SampleDocs;
import net.gsantner.opoc.activity.GsFragmentBase;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

/** Landing screen: hero "New note", feature tiles and recent files. Works with no permissions. */
public class HomeFragment extends GsFragmentBase {
    public static final String FRAGMENT_TAG = "HomeFragment";

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.home__fragment;
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final MainActivity main = (MainActivity) requireActivity();

        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        ((TextView) view.findViewById(R.id.home__greeting)).setText(hour < 12 ? R.string.home_greeting_morning
                : (hour < 18 ? R.string.home_greeting_afternoon : R.string.home_greeting_evening));

        view.findViewById(R.id.home__new_note).setOnClickListener(v -> main.homeNewNote());
        view.findViewById(R.id.home__tile_todo).setOnClickListener(v -> main.homeSelectTab(R.id.nav_todo));
        view.findViewById(R.id.home__tile_quicknote).setOnClickListener(v -> main.homeSelectTab(R.id.nav_quicknote));
        view.findViewById(R.id.home__tile_search).setOnClickListener(v -> main.homeSearch());
        view.findViewById(R.id.home__tile_markdown).setOnClickListener(v -> {
            final File welcome = new File(AppSettings.get().getNotebookDirectory(), "Welcome.md");
            if (!welcome.exists()) {
                SampleDocs.restoreWelcome(welcome);
            }
            DocumentActivity.launch(main, welcome, false, null, null);
        });
        refreshRecent();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshRecent();
    }

    public void refreshRecent() {
        final View root = getView();
        if (root == null) {
            return;
        }
        final LinearLayout list = root.findViewById(R.id.home__recent_list);
        list.removeAllViews();
        final AppSettings as = AppSettings.get();
        final ArrayList<File> files = new ArrayList<>();
        for (final String p : as.getRecentDocuments()) {
            final File f = new File(p);
            if (f.isFile() && files.size() < 5) {
                files.add(f);
            }
        }
        // Always show something useful: fall back to the seeded notes.
        for (final File f : new File[]{new File(as.getNotebookDirectory(), "Welcome.md"), as.getTodoFile(), as.getQuickNoteFile()}) {
            if (f.isFile() && files.size() < 3 && !files.contains(f)) {
                files.add(f);
            }
        }
        final LayoutInflater inflater = LayoutInflater.from(getContext());
        for (final File f : files) {
            final View row = inflater.inflate(R.layout.home__recent_item, list, false);
            ((TextView) row.findViewById(R.id.home__item_name)).setText(f.getName());
            ((TextView) row.findViewById(R.id.home__item_sub)).setText(getString(R.string.home_edited,
                    DateUtils.getRelativeTimeSpanString(f.lastModified(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)));
            row.setOnClickListener(v -> DocumentActivity.launch(requireActivity(), f, false, null, null));
            list.addView(row);
        }
    }
}
