package base;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import base.adapter.FragmentsAdapter;


/**
 * 实验性质的, 可以动态添加/删除Fragment,不需要切换Activity
 * Created by YesingBeijing on 2016/10/8.
 */
public class BasePagerActivity extends BaseActivity {

    protected ViewPager mVpFragmentContainer;
    protected boolean mAutoBack;

    //===========适配器
    protected FragmentsAdapter mAdapter;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, BasePagerActivity.class));
    }

    @Override
    protected View setFragmentContainer(LayoutInflater from, ViewGroup parent) {
        View view = from.inflate(com.yesing.blibrary_wos.R.layout.view_common_unscrollable_view_pager, parent, false);
        mVpFragmentContainer = (ViewPager) view.findViewById(com.yesing.blibrary_wos.R.id.uvp_fragment_container);
        return view;
    }

    @Override
    protected String setLabel() {
        return "";
    }

    protected void initView() {
        initAdapter();
        mAutoBack = true;

        mVpFragmentContainer.setAdapter(mAdapter);
        mVpFragmentContainer.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mToolbarTitle.setText(mAdapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void initAdapter() {
        mAdapter = new FragmentsAdapter(getSupportFragmentManager());
    }

    @Override
    public Intent getSupportParentActivityIntent() {
        if (mAdapter.getCount() > 1) {
            int currentItem = mVpFragmentContainer.getCurrentItem();
            mVpFragmentContainer.setCurrentItem(currentItem - 1);
            mAdapter.removeFragment(currentItem);
        } else {
            return super.getSupportParentActivityIntent();
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (mAutoBack) {
            if (mAdapter.getCount() > 1 && mVpFragmentContainer.getCurrentItem() > 0) {
                mVpFragmentContainer.setCurrentItem(mVpFragmentContainer.getCurrentItem() - 1);
            } else {
                super.onBackPressed();
            }
        }
    }

    //==============================================================================================
    public void addFragment(Fragment fragment, String title) {
        addFragment(fragment, title, true);
    }

    public void addFragment(Fragment fragment, String title, boolean turn) {
        int i = mAdapter.getPageTitles().indexOf(title);
        if (i >= 0) {
            mVpFragmentContainer.setCurrentItem(i);
        } else {
            mAdapter.addFragment(fragment, title);
            if (turn) {
                mVpFragmentContainer.setCurrentItem(mAdapter.getCount() - 1);
            }
        }
    }

    public boolean isAutoBack() {
        return mAutoBack;
    }

    public void setAutoBack(boolean autoBack) {
        mAutoBack = autoBack;
    }
}
