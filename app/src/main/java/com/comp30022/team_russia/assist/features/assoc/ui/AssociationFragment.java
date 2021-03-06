package com.comp30022.team_russia.assist.features.assoc.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.TabAdapter;
import com.comp30022.team_russia.assist.base.di.Injectable;

// Adapted from:
// https://developer.android.com/training/animation/screen-slide
// https://stackoverflow.com/questions/41413150/fragment-tabs-inside-fragment

/**
 * Fragment for adding association.
 * Consists of two tabs.
 */
public class AssociationFragment extends BaseFragment implements Injectable {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_association, container, false);

        // Initiate a PagerAdapter with fragments
        PagerAdapter adapter = new TabAdapter(getChildFragmentManager());
        ((TabAdapter) adapter).addItem(new ScanQrFragment(), "Scan");
        ((TabAdapter) adapter).addItem(new GenerateQrFragment(), "My QR");

        // Set view pager
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        // Set tabs
        TabLayout tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        return view;
    }
}
