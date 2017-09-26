package cn.shawn.swipemenu;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by daopeng on 2017/9/26.
 */

public class ItemSwipeLayout extends LinearLayout {

    public static final String TAG = "ItemSwipeLayout";

    private int mMaxSwipeWidth;

    private Scroller mScroller;

    private static ItemSwipeLayout mStoreInstance;

    private float mFactor = 0.15f;

    public ItemSwipeLayout(Context context) {
        this(context, null);
    }

    public ItemSwipeLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemSwipeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        setOrientation(HORIZONTAL);
        setClickable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        //这里需要设置子View的clickable属性
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.setClickable(true);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            int left = 0;
            int right = 0;
            int bottom = getMeasuredHeight();
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (i == 0) {
                    //这里默认只能为向左侧滑
                    right = getMeasuredWidth();
                } else {
                    right += child.getMeasuredWidth();
                }
                child.layout(left, 0, right, bottom);
                left = right;
            }
            mMaxSwipeWidth = right - getMeasuredWidth();
        }
    }

    private int mLastX, mLastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) ev.getRawX();
                mLastY = (int) ev.getRawY();
                if (mStoreInstance != null && mStoreInstance != this) {
                    mStoreInstance.smoothScroll(0);
                }
                mStoreInstance = this;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (ev.getRawX() - mLastX);
                int dy = (int) (ev.getRawY() - mLastY);
                if (Math.abs(dx) > Math.abs(dy)) {
                    requestDisallowInterceptTouchEvent(true);
                    return true;
                }
                requestDisallowInterceptTouchEvent(false);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (event.getRawX() - mLastX);
                scrollTo((int) (getScrollX() - dx * mFactor), 0);
                modifyBoundary();
                return true;
            case MotionEvent.ACTION_UP:
                smoothScroll();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 边界修正
     */
    private void modifyBoundary() {
        if (getScrollX() < 0) scrollTo(0, 0);
        else if (getScrollX() > mMaxSwipeWidth) scrollTo(mMaxSwipeWidth, 0);
    }

    private void smoothScroll() {
        int destX = getScrollX() > mMaxSwipeWidth / 2 ? mMaxSwipeWidth : 0;
        mScroller.startScroll(getScrollX(), 0, destX - getScrollX(), 0);
        modifyBoundary();
        invalidate();
    }

    private void smoothScroll(int destX){
        int delta = destX - getScrollX();
        mScroller.startScroll(getScrollX(),0,delta,0);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        }
    }

}
