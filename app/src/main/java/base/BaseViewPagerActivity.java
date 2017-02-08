package base;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wosloveslife.fantasy.R;

import java.util.ArrayList;

import base.adapter.FragmentsAdapter;

/**
 * 我的收藏列表 集合了 商城的收藏列表和 车贴广告的收藏列表
 * Created by YesingBeijing on 2016/9/21.
 */
public abstract class BaseViewPagerActivity extends BaseActivity {

    //========Views
    ViewPager mVpFragmentContainer;
    TabLayout mTabLayout;

    private boolean mLoaded;
    private FragmentsAdapter mAdapter;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, BaseViewPagerActivity.class));
    }

    @Override
    protected View setFragmentContainer(LayoutInflater from, ViewGroup parent) {
        View view = LayoutInflater.from(this).inflate(R.layout.base_activity_view_pager, parent,false);
        mVpFragmentContainer = (ViewPager) view.findViewById(R.id.vp_fragment_container);
        mTabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        return view;
    }

    protected void initView() {
        initAdapter();

        initViewPager();

        bindTabLayout();
    }

    private void initAdapter() {
        mAdapter = new FragmentsAdapter(getSupportFragmentManager());
        ArrayList<FragmentInfo> fragmentInfos = setFragments();
        for (int i = 0; i < fragmentInfos.size(); i++) {
            FragmentInfo fragmentInfo = fragmentInfos.get(i);
            mAdapter.addFragment(fragmentInfo.mFragment, fragmentInfo.mTitle);
        }
    }

    private void initViewPager() {
        mVpFragmentContainer.setAdapter(mAdapter);
    }

    private void bindTabLayout() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            mTabLayout.addTab(mTabLayout.newTab().setText(mAdapter.getPageTitle(i)));
        }
        if (mAdapter.getCount() > 0) {
            mTabLayout.setupWithViewPager(mVpFragmentContainer);
        }
    }

    protected abstract ArrayList<FragmentInfo> setFragments();

    public class FragmentInfo {
        String mTitle;
        Fragment mFragment;

        public FragmentInfo(String title, Fragment fragment) {
            mTitle = title;
            mFragment = fragment;
        }
    }
}
