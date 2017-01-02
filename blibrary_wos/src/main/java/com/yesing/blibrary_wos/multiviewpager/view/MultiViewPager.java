package com.yesing.blibrary_wos.multiviewpager.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.yesing.blibrary_wos.R;
import com.yesing.blibrary_wos.multiviewpager.transformer.SimpleZoomOutPageTransformer;
import com.yesing.blibrary_wos.utils.screenAdaptation.Dp2Px;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个可以显示左右两边的类似ViewPager控件
 * 本控件的缓存和复用思想就是将缓存容器(一个固定数量的集合)当作一个轮子而元素当作一条路, 不管路有多远(元素有多少),
 * 只要让轮子一直左右滚动即可(即滚动到不同的位置就装载那个位置所对应的页面)
 * <p/>
 * Created by WosLovesLife on 2016/8/15.
 */
public class MultiViewPager extends ViewGroup {
    private static final String TAG = "BannerLayout";

    /** 滑动状态为静止 */
    private static final int STATE_IDLE = 0;
    /** 滑动状态为滑动 */
    private static final int STATE_SCROLL = 1;
    /** 当速度值大于绝对值200时,即表示用户想要滑动页面. 正数为从左向右滑动, 负数为从右向左滑动 */
    private static final int VELOCITY_LIMIT = 1200;
    private static final int CHANGE_PAGE_LIMIT = 75;

    /** 滑动距离是否满足滑动事件的参照值 */
    private static int sTouchSlop;

    /** 默认的页面间间距, 10dp, 需要在构造中初始化 */
    private int mPageDistance;

    /** 状态, 静止或滑动 */
    private int mState = STATE_IDLE;
    /** 记录按下时的位置, 用于判断滑动等触摸操作 */
    private float mLastTouchX;
    /** 如果Y轴移动距离大于X,则不拦截触摸事件 */
    private float mLastTouchY;
    /** 记录在一次滑动事件结束后一共滑动了多远的距离, 根据速度值和此值决定是否翻页 */
    private int mMovedX;

    /** 本控件的实际宽度 */
    private int mViewWidth;
    /** 规范可滑动范围 在{@link MultiViewPager#onMeasure(int, int)}中进行初始化 */
    private int mScrollRange;
    /** 侧页的View的宽度 */
    private int mSideWidth;
    /** 每一个页面的界限, 每滑动这个值的距离就相当于翻了一页. = 一个子页面 + 一个间距的宽度 */
    private int mViewLimit;
    /** 布局的起始偏移量 见{@link MultiViewPager#onLayout(boolean, int, int, int, int)} */
    private int mOffset;

    /**
     * 当前处于中间页容器的索引
     * 注意: 这个值只是一个'预计'的值, 它不代表着本控件中有这么多页的内容
     * 而是说在不考虑容器数量的情况下当前的中心页是所有页面中的第几个页面
     * 在{@link MultiViewPager#setAdapter(PagerAdapter)}中会对本值进行初始化
     */
    private int mCurrentPosition = -1;
    /** 当前处于中间位置的View对象 */
    private ViewGroup mCenterContainer;

    /** 滑动控制器,模拟滑动值使滑动过程变的平滑 */
    private Scroller mScroller;
    /** 速度监听器, 对用户的滑动速度进行追踪,从而判断用户是否有意翻页 */
    private VelocityTracker mVelocityTracker;
    /** Adapter控制器 */
    private PagerAdapter mAdapter;

    /** Fragment的缓存容器集合,用于复用,默认三个缓存容器 */
    private ArrayList<ViewGroup> mContainers;
    /** 元素集合,每一个元素代表一页内容 */
    private List<ItemInfo> mItems;
    /** Adapter托管的Item总量 */
    private int mItemCount;
    /** 当前的实际个数, 是{@link MultiViewPager#mItemCount} 和 {@link MultiViewPager#mContainers}的getSize()的最小值 */
    private int mRealCount;
    /** 将滑动值传递出去,用于进行变形操作 */
    ViewPager.PageTransformer mPageTransformer;

    /** 为true时,限制用户滑动的距离, 使一次滑动最多翻一页 */
    private boolean mRestrict;
    /** 记录当前的滑动事件是否向左, 作为缓存处理判断依据 */
    private boolean mIsToLeft;

    static class ItemInfo {
        Object object;
        int position;
        boolean scrolling;
        float widthFactor;
        float offset;

        @Override
        public String toString() {
            return "ItemInfo{" +
                    "object=" + object +
                    ", position=" + position +
                    ", scrolling=" + scrolling +
                    ", widthFactor=" + widthFactor +
                    ", offset=" + offset +
                    '}';
        }
    }

    public MultiViewPager(Context context) {
        this(context, null);
    }

    public MultiViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MultiViewPager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        removeAllViews();

        setRestrictScrollRange(true);

        setTransformer(new SimpleZoomOutPageTransformer());

        initContainers();

        mItems = new ArrayList<ItemInfo>();

        sTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        setPageDistance(10);

        mScroller = new Scroller(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Log.w(TAG, "onMeasure: ");
        mViewWidth = measureSize(widthMeasureSpec, 400);
        int height = measureSize(heightMeasureSpec, 200);
        setMeasuredDimension(mViewWidth, height);

        int childWidth = (int) (mViewWidth * 0.6f);
        int sideViewWidth = (mViewWidth - childWidth) / 2;
        mSideWidth = sideViewWidth - mPageDistance;
        mViewLimit = childWidth + mPageDistance;

        int childWidthSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.EXACTLY);
        int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        for (int i = 0; i < mRealCount; i++) {
            View view = mContainers.get(i);
            if (view == null || view.getVisibility() == GONE) continue;
            view.measure(childWidthSpec, childHeightSpec);
        }

        mScrollRange = (mItemCount - 1) * childWidth + (mItemCount - 1) * mPageDistance;
    }

    /**
     * 根据模式计算尺寸
     *
     * @param spec         尺寸描述
     * @param defaultValue 当测量不出当前控件尺寸时使用该默认值
     * @return 计算后的尺寸
     */
    private int measureSize(int spec, int defaultValue) {
        int result;
        int mode = MeasureSpec.getMode(spec);
        if (mode == MeasureSpec.EXACTLY) {
            result = MeasureSpec.getSize(spec);
        } else {
            result = defaultValue;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, MeasureSpec.getSize(spec));
            }
        }
        return result;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.w(TAG, "onLayout: ");
        int usedLeft = getPaddingLeft() + mSideWidth + mOffset;
        for (int i = 0; i < mRealCount; i++) {
            View v = mContainers.get(i);
            if (v == null || v.getVisibility() == GONE) continue;
            int left = usedLeft + mPageDistance;
            int right = v.getMeasuredWidth() + left;
            int top = getPaddingTop();
            int bottom = v.getMeasuredHeight() + top;
            usedLeft = right;
            v.layout(left, top, right, bottom);
        }
    }

    float mDownX;

    /**
     * 非常重要
     * 横向滑动的距离大于滑动事件阀值{@link MultiViewPager#sTouchSlop},  触摸时的容差
     * 并且大于纵向滑动距离,  识别用户意图是想滑动别的控件还是要滑动本控件
     * <p/>
     * ----------防止遮挡DrawerLayout等抽屉控件-----------
     * 事件为{@link MotionEvent#ACTION_DOWN}时触摸的x点{@link MultiViewPager#mDownX}大于{@link MultiViewPager#sTouchSlop}
     * 并且{@link MultiViewPager#mViewWidth} - {@link MultiViewPager#mDownX} 大于 {@link MultiViewPager#sTouchSlop}
     * 也就是触摸在空间的两边时, 不请求父控件不拦截触摸事件, 保证抽屉控件能够正常展开
     * <p/>
     * 只有满足这四个条件才会拦截
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        /* 如果没有数据, 则不处理任何触摸事件 */
        if (mItemCount <= 0) return false;

        int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE && mState != STATE_IDLE) return true;

        float x = ev.getX();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = x;
                mLastTouchX = x;
                mLastTouchY = y;
                mState = STATE_IDLE;
                break;
            case MotionEvent.ACTION_MOVE:
                float movedX = Math.abs(mLastTouchX - x);
                float movedY = Math.abs(mLastTouchY - y);
                /* 如果有右边抽屉, 则使用该判断 */
//                if (mDownX > sTouchSlop && mViewWidth - mDownX > sTouchSlop && movedX > sTouchSlop && movedX > movedY) {
                if (mDownX > sTouchSlop && movedX > sTouchSlop && movedX > movedY) {
                    mState = STATE_SCROLL;
                }
                break;
            case MotionEvent.ACTION_UP:
            default:
                mState = STATE_IDLE;
                break;
        }
        boolean b = mState != STATE_IDLE;
//        Log.w(TAG, "onInterceptTouchEvent: 事件 = " + ev.getAction() + "; 是否拦截 = " + b);
        return b;
    }

    /** 非常重要, 请求父层控件不要拦截触摸事件, 在本项目中是ListView */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mState != STATE_IDLE) {
            ViewParent parent = getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
        boolean b = super.dispatchTouchEvent(ev);
//        Log.w(TAG, "dispatchTouchEvent: " + b + "; action = " + ev.getAction() + "; 是否分发 = " + b);
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.w(TAG, "onTouchEvent: 事件 = " + event.getAction());
        if (!mScroller.isFinished()) {
//            Log.w(TAG, "onTouchEvent: mScroller.isNotFinished");
            mScroller.abortAnimation();
        }
        float x = event.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.w(TAG, "onTouchEvent: ACTION_DOWN");
                mLastTouchX = x;
                mMovedX = 0;
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.w(TAG, "onTouchEvent: ACTION_MOVE");
                /** 默认向右滑动页面向左,因此取反 */
                float movedX = -(x - mLastTouchX);
                mLastTouchX = x;
                mMovedX += movedX;

                if (movedX > 0) {
                    mIsToLeft = false;
                } else if (movedX < 0) {
                    mIsToLeft = true;
                }

                int scrollX = getScrollX();
                /* 当滑动到边缘时, 忽略滑动操作, 节省开销 */
                if (scrollX >= mScrollRange && movedX > 0 || scrollX <= 0 && movedX < 0) break;
                /* 下面两个判断是为了防止滑动距离过大超出边界 */
                if (scrollX + movedX > mScrollRange) movedX = mScrollRange - scrollX;
                if (scrollX + movedX < 0) movedX = -scrollX;
                movedX = restrictScrollRange(movedX);

                /* 移动 movedX 距离 */
                scrollBy((int) movedX, 0);
                break;
            case MotionEvent.ACTION_UP:
//                Log.w(TAG, "onTouchEvent: ACTION_UP");
            case MotionEvent.ACTION_CANCEL:
//                Log.w(TAG, "onTouchEvent: ACTION_CANCEL");
                mVelocityTracker.computeCurrentVelocity(1000);
                float xVelocity = mVelocityTracker.getXVelocity();
//                Log.w(TAG, "onTouchEvent: xVelocity = " + xVelocity);

                /* 释放速度监听器 */
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                whereScroll(xVelocity);
                break;
        }
        return true;
    }

    /**
     * 如果设置了限制滑动一页的跨距(这样用户最多滑动一页)
     *
     * @param movedX 原始滑动的距离
     * @return 计算后获得的滑动距离
     */
    private float restrictScrollRange(float movedX) {
        if (mRestrict) {
            /* 减小滑动的比例 */
            float v = movedX / mViewWidth;
            movedX = v * mViewLimit;
            return movedX;
        }
        return movedX;
    }

    /** 参见{@link MultiViewPager#VELOCITY_LIMIT} 当大于临界值时,表示从左向右滑动, 当小于临界值相反数时,表示从右向左滑动 */
    private void whereScroll(float xVelocity) {
        int scrollX = getScrollX();

        /* 当滑动到边缘时, 忽略滑动操作, 节省开销 */
        if (scrollX >= mScrollRange && xVelocity < 0 || scrollX <= 0 && xVelocity > 0) return;

        if (xVelocity > VELOCITY_LIMIT || mMovedX < -CHANGE_PAGE_LIMIT) {
            toPrevious(scrollX);
        } else if (xVelocity < -VELOCITY_LIMIT || mMovedX > CHANGE_PAGE_LIMIT) {
            toNext(scrollX);
        } else {
            toRecover(scrollX, xVelocity > 0);
        }
    }

    /** 滚动到上一页 */
    private void toPrevious(int scrollX) {
        int position = scrollX / mViewLimit;
//        Log.w(TAG, "toPrevious: position = " + position);
        onPageChanged(position);

        int offset = -(scrollX % mViewLimit);
        scrollToSomePlace(offset);
    }

    /** 滚动到下一页 */
    private void toNext(int scrollX) {
        int position = scrollX / mViewLimit + 1;
//        Log.w(TAG, "toNext: position = " + position);
        onPageChanged(position);

        int offset = mViewLimit - (scrollX % mViewLimit);
        scrollToSomePlace(offset);
    }

    /** 复位当前页 */
    private void toRecover(int scrollX, boolean fromLeft2Right) {
        int offset = -(scrollX % mViewLimit);
        if (fromLeft2Right) offset = mViewLimit - (scrollX % mViewLimit);
        scrollToSomePlace(offset);
    }

    private void scrollToSomePlace(int target) {
        int scrollX = getScrollX();
        /* 避免越界 */
        if (scrollX >= mScrollRange && target > 0 || scrollX <= 0 && target < 0) return;

        int time = (int) (Math.abs(target + 0f) / mViewLimit * 300) + 100;
//        Log.w(TAG, "scrollToSomePlace: getScrollX() = " + scrollX + "; target = " + target + "; time = " + time);

        mScroller.startScroll(scrollX, 0, target, 0, time);
        invalidate();
    }

    private void onPageChanged(float position) {
        /* 参数错误, 忽略 */
        if (position < 0) {
            scrollTo(0, 0);
            return;
        }
        if (position >= mItemCount) {
            scrollTo(mScrollRange, 0);
            return;
        }

        int targetPagePosition = (int) position;
        /** 没有切换页面,不需进行任何操作 */
        if (targetPagePosition == mCurrentPosition) return;

        /* 只有真实页面数量大于3的时候才开启缓存策略, 因为最低显示三页, 因此低于三页的缓存无意义 */
        if (mRealCount > 3) {
            computePage(targetPagePosition);
        }
    }

    /**
     * @param position 目标页数
     */
    private void computePage(int position) {
//        Log.w(TAG, "computePage: ");
        int transitionOffset = position - mCurrentPosition; // 跳转的页面数, 如果为负说明向左滑动, 为正向右滑动
        for (int i = 0; i < mRealCount; i++) {
            /* 找到当前处于中心页的容器对应的Item, 然后根据Item中保存的position值
             * 获知当前的中心页的内容在Adapter中的元素的下标.
             * 根据这个下标值, 就能找到左/右页的内容下标等.*/
            ItemInfo info = mItems.get(i);
            boolean viewFromObject = mAdapter.isViewFromObject(mCenterContainer.getChildAt(0), info.object);  // 找到和老中心页对应的Item
            if (!viewFromObject) continue;

            /* 根据已知的中心页的Item下标获取左/右一页的Item下标以及左/右缓存页的下标
             * 其中左/右缓存页的下标就是要处理的数据,需要提前加载出来 */
            int oldCenterItemPosition = info.position;  // 老中心页对于的Item在集合中的下标
            int newCenterItemPosition = oldCenterItemPosition + transitionOffset;
            int newNextItemPosition = newCenterItemPosition + 1;
            int newNextCacheItemPosition = newCenterItemPosition + 2;
            int newPreItemPosition = newCenterItemPosition - 1;
            int newPreCacheItemPosition = newCenterItemPosition - 2;

            /* 根据已知的中心页的容器在容器集合中的下标, 获取到'预计'的左/右页所需要占用的容器以及
             * 以及左/右缓存页所需要占用的容器
             * 注意: 下面的值只是估值, 即没有考虑集合的存储状态和下标是否越界等问题
             * 下标是否越界需要根据'预计'的值来判断并处理 */
            int newContainerPosition = mContainers.indexOf(mCenterContainer) + transitionOffset;
//            Log.w(TAG, "computePage: newContainerPosition = " + newContainerPosition + "; transitionOffset = " + transitionOffset);
            int newNextCacheContainerPosition = newContainerPosition + 2;
            int newPreCacheContainerPosition = newContainerPosition - 2;

            /** {@link BannerLayout#mCurrentPosition} 是一个'预计的只',只用来记录用户下一次滑动到的页面和当前页面的对比. */
            mCurrentPosition = position;
            mCenterContainer = mContainers.get(newContainerPosition % mContainers.size());

            if (transitionOffset > 0 && newNextCacheItemPosition < mItemCount) { // 处理右边的缓存
                ViewGroup group = mContainers.get(newNextCacheContainerPosition % mContainers.size());

                /* 如果缓存容器的下标超过了实际容器集合大小, 则将缓存页的容器裁剪掉换到容器集合的尾部
                 * 因为在 onLayout() 方法中是按照集合的顺序依次排列本控件的子View的, 所以剪裁到尾部就会排列在右侧
                 * 同样的, 也需要将容器从本控件中移出并添加到本控件的尾部
                 * 最后请求重新布局 */
                if (newNextCacheContainerPosition >= mContainers.size()) {
                    mContainers.remove(group);
                    mContainers.add(group);
                    mOffset += mViewLimit;
                    removeView(group);
                    addView(group);
                    requestLayout();
                }

                updateItem(group, newNextCacheItemPosition);
            } else if (transitionOffset < 0 && newPreCacheItemPosition >= 0) {// 处理左边的缓存
                /* 因为newPreCacheContainerPosition可能为负数, 所以为负时变换为最大值+偏移量
                 * 这样就相当于找到最右边的容器的下标 */
                int pre = newPreCacheContainerPosition < 0 ? mContainers.size() + newPreCacheContainerPosition : newPreCacheContainerPosition;
                ViewGroup group = mContainers.get(pre);

                /* 参见上面的解释 */
                if (newPreCacheContainerPosition < 0) {
                    mContainers.remove(group);
                    mContainers.add(0, group);
                    mOffset -= mViewLimit;
                    removeView(group);
                    addView(group, 0);
                    requestLayout();
                }

                updateItem(group, newPreCacheItemPosition);
            }
            break;
        }
    }

    private void updateItem(ViewGroup group, int cacheItemPosition) {
        /* 找到这个缓存容器原本装载的Item, 将其卸载掉 */
        for (int j = 0; j < mRealCount; j++) {
            ItemInfo nextInfo = mItems.get(j);
            boolean fromObject = mAdapter.isViewFromObject(group.getChildAt(0), nextInfo.object);
            if (fromObject) {
                if (nextInfo.position == cacheItemPosition) return; // 不需要重新加载

                mAdapter.destroyItem(group, nextInfo.position, nextInfo.object);
                mAdapter.finishUpdate(group);
                break;
            }
        }

        /* 加载新的Item进缓存容器 */
        Object item = mAdapter.instantiateItem(group, cacheItemPosition);   // 参1 容器, 参2 要去的View位于集合的位置
        ItemInfo cacheItemInfo = mItems.get(cacheItemPosition % mContainers.size());
        cacheItemInfo.object = item;
        cacheItemInfo.position = cacheItemPosition;
        mAdapter.finishUpdate(group);   // 结束Fragment的添加事件
    }

    @Override
    public void computeScroll() {
        int scrollX = getScrollX();
        if (scrollX < 0) scrollTo(0, 0);
        if (scrollX > mScrollRange) scrollTo(mScrollRange, 0);

        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }

        toTransformer(scrollX);
    }

    /**
     * 将滑动状态传递给变形器, 做子控件的效果
     *
     * @param scrollX 当前滑动的坐标点
     */
    private void toTransformer(int scrollX) {
        if (mPageTransformer != null) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = getChildAt(i);
                final float transformPos = (child.getLeft() - mSideWidth - mPageDistance - scrollX + 0.0f) / (mViewWidth - getPaddingLeft() - getPaddingRight());
                mPageTransformer.transformPage(child, transformPos);
            }
        }
    }

    public void setAdapter(PagerAdapter adapter) {
//        Log.d(TAG, "setAdapter: new mItemCount = " + adapter.getCount());
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
            mAdapter.startUpdate(this);
            for (int i = 0; i < mItems.size(); i++) {
                final ItemInfo ii = mItems.get(i);
                mAdapter.destroyItem(this, ii.position, ii.object);
            }
            mAdapter.finishUpdate(this);
            mItems.clear();
            mCurrentPosition = -1;
            removeAllViews();
        }

        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mObserver);

        mItemCount = mAdapter.getCount();
        mRealCount = Math.min(mItemCount, mContainers.size());

        mAdapter.startUpdate(this);
        for (int i = 0; i < mRealCount; i++) {
            ViewGroup group = mContainers.get(i);
            group.removeAllViews();
            Object item = mAdapter.instantiateItem(group, i);   // 参1 容器, 参2 要去的View位于集合的位置
            ItemInfo info = new ItemInfo();
            info.object = item;
            info.position = i;
            mItems.add(info);
            addView(group);
            if (i == 0) {
                mCurrentPosition = 0;
                mCenterContainer = group;
            }
        }
        mAdapter.finishUpdate(this);   // 结束Fragment的添加事件

        mOffset = 0;
        scrollTo(0,0);
        computePage(mCurrentPosition);
        requestLayout();

        toTransformer(getScrollX());
    }

    DataSetObserver mObserver = new DataSetObserver(){
        @Override
        public void onChanged() {
            super.onChanged();
//            Log.w(TAG, "DataSetObserver onChanged: ");
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
//            Log.w(TAG, "DataSetObserver onInvalidated: ");
        }
    };

    /**
     * 对Fragment的缓存容器进行初始化,如果内容是Fragment,就先创建这些容器,然后将Fragment添加到容器中
     * 缓存默认有三个. 条目循环复用这三个缓存.
     */
    private void initContainers() {
        if (mContainers != null) return;

        mContainers = new ArrayList<ViewGroup>();
        FrameLayout layout = new FrameLayout(getContext());
        layout.setId(R.id.wosloveslife_container1);
        FrameLayout layout2 = new FrameLayout(getContext());
        layout2.setId(R.id.wosloveslife_container2);
        FrameLayout layout3 = new FrameLayout(getContext());
        layout3.setId(R.id.wosloveslife_container3);
        FrameLayout layout4 = new FrameLayout(getContext());
        layout4.setId(R.id.wosloveslife_container4);
        FrameLayout layout5 = new FrameLayout(getContext());
        layout5.setId(R.id.wosloveslife_container5);
        mContainers.add(layout);
        mContainers.add(layout2);
        mContainers.add(layout3);
        mContainers.add(layout4);
        mContainers.add(layout5);
    }

    ////////////控制方法

    /**
     * 决定是否限制用户滑动翻页的速度
     *
     * @param restrict 为true时, 将限制用户每次滑动最多滑动一页
     */
    public void setRestrictScrollRange(boolean restrict) {
        mRestrict = restrict;
    }

    /** 获取当前是否限制了翻页的距离, 要设置限制请调用{@link MultiViewPager#setRestrictScrollRange(boolean)} */
    public boolean isRestrictScrollRange() {
        return mRestrict;
    }

    /**
     * 设置页面切换的转换器, 可以在滑动中对View进行操作
     *
     * @param transformer 转换器对象, 滑动中会不停回调该对象的方法
     */
    public void setTransformer(ViewPager.PageTransformer transformer) {
        mPageTransformer = transformer;
    }

    /**
     * 设置两页之间的间距
     *
     * @param pageDistance 两页之间的间距,单位dp
     */
    public void setPageDistance(int pageDistance) {
        mPageDistance = Dp2Px.toPX(getContext(), pageDistance);
        requestLayout();
    }

    /** 获取当前的页面间距值{@link MultiViewPager#mPageDistance} */
    public int getPageDistance() {
        return mPageDistance;
    }
}