package base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.wosloveslife.fantasy.R;

/**
 * Created by zhangh on 2017/2/8.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
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
}
