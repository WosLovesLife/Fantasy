package base;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by YesingBeijing on 2016/9/14.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static int sDefActionbarColor;
    private static int sDefActionbarIcon;

    //==========Views
    protected RelativeLayout mRootView;
    protected AppBarLayout mAppBarLayout;
    protected Toolbar mToolbar;

    private TextView mTvLoadingMsg;

    //==========核心控制器
    private AlertDialog mLoadingProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_toolbar);

        mRootView = (RelativeLayout) findViewById(R.id.rl_root_view);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);

        bindToolbar();
        setActivityTitle();

        setContentView();

        initView();
    }

    private void bindToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        setSupportActionBar(mToolbar);
        setHomeAsUpEnabled(true);

        if (sDefActionbarColor != 0) {
            mAppBarLayout.setBackgroundColor(sDefActionbarColor);
        }

        if (sDefActionbarIcon != 0) {
            mToolbar.setNavigationIcon(sDefActionbarIcon);
        }
    }

    private void setActivityTitle() {
        mToolbar.setTitle(setLabel());
    }

    /**
     * 将FragmentContainer添加到RootView
     */
    private void setContentView() {
        View view = setFragmentContainer(LayoutInflater.from(this), mRootView);
        mRootView.addView(view, 0);
        setActionBarMode(ACTION_BAR_MODE_TOP);
    }

    //====================================默认实现-可更改=================================
    protected void initView() {
    }

    //====================================必须实现=================================
    protected abstract View setFragmentContainer(LayoutInflater from, ViewGroup parent);

    protected abstract String setLabel();


    //=======================================不用动=================================

    @Override
    public Intent getSupportParentActivityIntent() {
        // 默认调用父类的方法,返回一个Intent对象, 而这里finish()掉此Activity,并返回null
        onBackPressed();
        return null;
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() > 0) {
            Fragment fragment = fragments.get(0);
            if (fragment instanceof BaseFragment) {
                if (((BaseFragment) fragment).onBackPressed()) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    //=====================================方便调用的方法===========================================

    public void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && actionBar.isShowing()) {
            actionBar.hide();
        }
    }

    public void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && !actionBar.isShowing()) {
            actionBar.show();
        }
    }

    public void fitActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mToolbar.setPadding(
                    mToolbar.getPaddingLeft(),
                    Dp2Px.toPX(this, 25),
                    mToolbar.getPaddingRight(),
                    mToolbar.getPaddingBottom());
        }
    }

    public final static int ACTION_BAR_MODE_TOP = 0;    //默认 处于顶部
    public final static int ACTION_BAR_MODE_OVERLAY = 1;    //覆盖在主界面上

    @IntDef({ACTION_BAR_MODE_TOP, ACTION_BAR_MODE_OVERLAY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ReqType {
    }

    public void setActionBarMode(@ReqType final int type) {
        switch (type) {
            case ACTION_BAR_MODE_TOP:
                if (mRootView.getChildCount() > 1) {
                    View mainView = mRootView.getChildAt(0);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mainView.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW, R.id.app_bar_layout);
                    mainView.setLayoutParams(params);
                }
                break;
            case ACTION_BAR_MODE_OVERLAY:
                if (mRootView.getChildCount() > 1) {
                    View mainView = mRootView.getChildAt(0);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mainView.getLayoutParams();
                    params.addRule(RelativeLayout.BELOW, 0);
                    mainView.setLayoutParams(params);
                }
                break;
        }
    }

    public void setHomeAsUpEnabled(boolean enabled) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
        }
    }


    //========================Alert相关

    protected void showLoadingProgressDialog() {
        showLoadingProgressDialog("");
    }

    protected void showLoadingProgressDialog(String msg) {
        if (mLoadingProgressDialog == null) {
            View view = LayoutInflater.from(this).inflate(com.yesing.blibrary_wos.R.layout.view_loading_dialog, null);
            mTvLoadingMsg = (TextView) view.findViewById(com.yesing.blibrary_wos.R.id.tv_msg);
            mLoadingProgressDialog = new AlertDialog.Builder(this)
                    .setTitle(null)
                    .setView(view)
                    .setCancelable(false)
                    .create();
        }

        mTvLoadingMsg.setText(msg);

        if (!mLoadingProgressDialog.isShowing()) {
            mLoadingProgressDialog.show();
        }
    }

    protected void dismissLoadingProgressDialog() {
        if (mLoadingProgressDialog != null && mLoadingProgressDialog.isShowing()) {
            mLoadingProgressDialog.dismiss();
        }
    }
}
