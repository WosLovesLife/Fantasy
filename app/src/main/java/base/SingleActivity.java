package base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by YesingBeijing on 2016/10/9.
 */
public abstract class SingleActivity extends BaseActivity {
    @Override
    protected View setFragmentContainer(LayoutInflater from, ViewGroup parent) {
        return from.inflate(com.yesing.blibrary_wos.R.layout.view_common_framelayout, parent, false);
    }

    @Override
    protected void initView() {
        super.initView();
        initFragment();
    }

    private void initFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(com.yesing.blibrary_wos.R.id.fl_fragment_container);
        if (fragment == null) {
            fragment = setFragment();
            if (fragment != null) {
                manager.beginTransaction().add(com.yesing.blibrary_wos.R.id.fl_fragment_container, fragment).commit();
            }
        }
    }

    protected abstract Fragment setFragment();

    protected abstract String setLabel();
}
