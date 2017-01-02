package base;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wosloveslife.fantasy.R;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import butterknife.ButterKnife;

/**
 * Created by YesingBeijing on 2016/9/14.
 */
public abstract class BaseFragment extends Fragment {
    private static final int QUEST_CODE_LOGIN = 0;

    /** 没有menu */
    protected static final int MENU_TYPE_NONE = 0;
    /** menu为帮助按钮 */
    protected static final int MENU_TYPE_HELP = 1;

    //==========核心控制器
    private AlertDialog mLoadingProgressDialog;

    //==========其它
    private int mMenuRes;

    //==========Views
    private TextView mTvLoadingMsg;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = setContentView(inflater, container, savedInstanceState);
        butterKnife(view);

        mMenuRes = initMenu();
        setHasOptionsMenu(mMenuRes != MENU_TYPE_NONE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        getData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case QUEST_CODE_LOGIN:
                updateData();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(mMenuRes, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //==============================================================================================
    protected abstract View setContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    //==============================================================================================
    protected void butterKnife(View view) {
        ButterKnife.bind(this, view);
    }

    /**
     * 根据Type生成不同的menu
     *
     * @return type
     * {@link BaseFragment#MENU_TYPE_NONE} 没有menu
     * {@link BaseFragment#MENU_TYPE_HELP} 帮助menu
     * 除了以上两种之外,表示自定义menu布局, 则子类自己处理
     */
    protected int initMenu() {
        return MENU_TYPE_NONE;
    }

    protected void initView() {
    }

    /**
     * 该方法在{@link Fragment#onActivityCreated(Bundle)}时执行
     * 调用{@link BaseFragment#updateData()}方法
     * 该方法只执行一次
     */
    protected void getData() {
        updateData();
    }

    /**
     * 子类的数据加载工作应该在本方法中执行
     */
    protected void updateData() {
    }

    protected boolean onBackPressed() {
        return false;
    }

    //========================方便子类操作的通用方法=====================

    protected void showLoadingProgressDialog() {
        showLoadingProgressDialog("");
    }

    protected void showLoadingProgressDialog(String msg) {
        if (mLoadingProgressDialog == null) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_loading_dialog, null);
            mTvLoadingMsg = (TextView) view.findViewById(R.id.tv_msg);
            mLoadingProgressDialog = new AlertDialog.Builder(getActivity())
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

    public void fitActionBar(View view) {
        if (view == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop() + Dp2Px.toPX(getActivity(), 25),
                    view.getPaddingRight(),
                    view.getPaddingBottom());
        }
    }

    public boolean fitActionBarWidthMargin(View view) {
        try {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            params.topMargin = params.topMargin + Dp2Px.toPX(getContext(), 25);
            view.setLayoutParams(params);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
