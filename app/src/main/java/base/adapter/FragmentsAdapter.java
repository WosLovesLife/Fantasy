package base.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

public class FragmentsAdapter extends FragmentPagerAdapter {
    ArrayList<Fragment> mFragments = new ArrayList<>();
    ArrayList<String> mTitles = new ArrayList<>();

    private boolean mDestroyEnable = true;

    public FragmentsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    public ArrayList<String> getPageTitles() {
        return mTitles;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        if (mDestroyEnable) {
            super.destroyItem(container, position, object);
        }
    }

    public void addFragment(Fragment fragment, String title) {
        if (fragment != null) {
            mFragments.add(fragment);
            mTitles.add(title == null ? "" : title);
            notifyDataSetChanged();
        }
    }

    public boolean removeFragment(Fragment fragment) {
        return removeFragment(mFragments.indexOf(fragment));
    }

    public boolean removeFragment(int position) {
        boolean success = false;
        if (position >= 0 && position < mFragments.size()) {
            mFragments.remove(position);
            mTitles.remove(position);
            success = true;
            notifyDataSetChanged();
        }
        return success;
    }

    public void setDestroyEnable(boolean enable) {
        mDestroyEnable = enable;
    }

    public ArrayList<Fragment> getFragments() {
        return mFragments;
    }
}