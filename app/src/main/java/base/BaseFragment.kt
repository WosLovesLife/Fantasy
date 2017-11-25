package base

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.ButterKnife

import com.wosloveslife.fantasy.R
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px

/**
 * Created by WosLovesLife on 2016/9/14.
 */
abstract class BaseFragment : Fragment() {
    private var mTvLoadingMsg: TextView? = null
    private var mLoadingProgressDialog: AlertDialog? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view!!)
    }

    open protected fun onBackPressed(): Boolean {
        return false
    }

    @JvmOverloads protected fun showLoadingProgressDialog(msg: String = "") {
        if (mLoadingProgressDialog == null) {
            val view = LayoutInflater.from(activity).inflate(R.layout.view_loading_dialog, null)
            mTvLoadingMsg = view.findViewById(R.id.tv_msg) as TextView
            mLoadingProgressDialog = AlertDialog.Builder(activity)
                    .setTitle(null)
                    .setView(view)
                    .setCancelable(false)
                    .create()
        }

        mTvLoadingMsg!!.text = msg

        if (!mLoadingProgressDialog!!.isShowing) {
            mLoadingProgressDialog!!.show()
        }
    }

    protected fun dismissLoadingProgressDialog() {
        if (mLoadingProgressDialog != null && mLoadingProgressDialog!!.isShowing) {
            mLoadingProgressDialog!!.dismiss()
        }
    }

    companion object {

        fun fitActionBar(view: View?) {
            if (view == null) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                view.setPadding(
                        view.paddingLeft,
                        view.paddingTop + Dp2Px.toPX(view.context, 25),
                        view.paddingRight,
                        view.paddingBottom)
            }
        }

        fun fitActionBarWidthMargin(view: View): Boolean {
            try {
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                params.topMargin = params.topMargin + Dp2Px.toPX(view.context, 25)
                view.layoutParams = params
                return true
            } catch (e: Throwable) {
                return false
            }

        }
    }
}
