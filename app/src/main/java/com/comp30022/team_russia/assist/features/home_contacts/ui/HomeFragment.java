package com.comp30022.team_russia.assist.features.home_contacts.ui;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.TabAdapter;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.features.profile.ProfileFragment;


// Adapted from:
// https://developer.android.com/training/animation/screen-slide
// https://stackoverflow.com/questions/41413150/fragment-tabs-inside-fragment

/**
 * Fragment for adding association.
 * Consists of two tabs.
 */
public class HomeFragment extends BaseFragment implements Injectable {

    TabLayout tabs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_base, container, false);

        // Initiate a PagerAdapter with fragments
        PagerAdapter adapter = new TabAdapter(getChildFragmentManager());
        ((TabAdapter) adapter).addItem(new HomeContactFragment(), "Contacts");
        ((TabAdapter) adapter).addItem(new ProfileFragment(), "Profile");

        // Set view pager
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        // Set tabs
        tabs = view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //Add icon to tab
        tabs.getTabAt(0).setIcon(R.drawable.ic_home).getIcon().setColorFilter(
            ContextCompat.getColor(getActivity(), R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        tabs.getTabAt(1).setIcon(R.drawable.ic_edit);

        //Makes icons color change when tab is selected
        tabs.addOnTabSelectedListener(
            new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    super.onTabSelected(tab);
                    int tabIconColor = ContextCompat.getColor(getActivity(), R.color.colorWhite);
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    super.onTabUnselected(tab);
                    int tabIconColor = ContextCompat.getColor(getActivity(), R.color.colorFaded);
                    tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                }
            }
        );
        return view;
    }
}


