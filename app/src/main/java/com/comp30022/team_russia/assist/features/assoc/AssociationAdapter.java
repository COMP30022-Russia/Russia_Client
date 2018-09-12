package com.comp30022.team_russia.assist.features.assoc;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

// Adapted from:
// https://developer.android.com/training/animation/screen-slide
// https://stackoverflow.com/questions/41413150/fragment-tabs-inside-fragment

class AssociationAdapter extends FragmentPagerAdapter {
    /**
     * Number of pages
     */
    private int numPages = 0;
    /**
     * Array of fragment items
     */
    private final ArrayList<FragmentItem> fragmentItems = new ArrayList<>();

    /**
     * Constructor of AssociationAdapter
     *
     * @param fm Fragment Manager
     */
    AssociationAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Adds a fragment and its title/name to the adapter
     *
     * @param fragment Fragment
     * @param title    Title of fragment
     */
    void addItem(Fragment fragment, String title) {
        fragmentItems.add(new FragmentItem(fragment, title));
        numPages++;
    }

    /**
     * Retrieves a fragment from the adapter
     *
     * @param i Index of fragment
     * @return Fragment at the specified index
     */
    @Override
    public Fragment getItem(int i) {
        return fragmentItems.get(i).getFragment();
    }

    /**
     * Returns the title/name of the fragment at the specified index
     *
     * @param i Index of fragment
     * @return Title of fragment at index
     */
    @Override
    public CharSequence getPageTitle(int i) {
        return fragmentItems.get(i).getFragmentName();
    }

    /**
     * Get count
     *
     * @return The number of fragments
     */
    @Override
    public int getCount() {
        return numPages;
    }

    /**
     * A fragment and its title
     */
    private class FragmentItem {
        private Fragment fragment;
        private String fragmentName;

        /**
         * Constructor of FragmentItem
         *
         * @param fragment     The fragment
         * @param fragmentName The name of the fragment
         */
        FragmentItem(Fragment fragment, String fragmentName) {
            this.fragment = fragment;
            this.fragmentName = fragmentName;
        }

        /**
         * @return The fragment
         */
        Fragment getFragment() {
            return fragment;
        }

        /**
         * @return The title of the fragment
         */
        String getFragmentName() {
            return fragmentName;
        }
    }
}

