package android.support.v4.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

public class SwipeRefreshLayout extends ViewGroup {
    private static final float ACCELERATE_INTERPOLATION_FACTOR = 1.5f;
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2.0f;
    private static final int[] LAYOUT_ATTRS = new int[]{16842766};
    private static final float MAX_SWIPE_DISTANCE_FACTOR = 0.6f;
    private static final float PROGRESS_BAR_HEIGHT = 4.0f;
    private static final int REFRESH_TRIGGER_DISTANCE = 120;
    private static final long RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 300;
    private final AccelerateInterpolator mAccelerateInterpolator;
    private final Animation mAnimateToStartPosition;
    private final Runnable mCancel;
    private float mCurrPercentage;
    private int mCurrentTargetOffsetTop;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private float mDistanceToTriggerSync;
    private MotionEvent mDownEvent;
    private int mFrom;
    private float mFromPercentage;
    private OnRefreshListener mListener;
    private int mMediumAnimationDuration;
    private int mOriginalOffsetTop;
    private float mPrevY;
    private SwipeProgressBar mProgressBar;
    private int mProgressBarHeight;
    private boolean mRefreshing;
    private final Runnable mReturnToStartPosition;
    private final AnimationListener mReturnToStartPositionListener;
    private boolean mReturningToStart;
    private final AnimationListener mShrinkAnimationListener;
    private Animation mShrinkTrigger;
    private View mTarget;
    private int mTouchSlop;

    private class BaseAnimationListener implements AnimationListener {
        private BaseAnimationListener() {
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationStart(Animation animation) {
        }
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public SwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mRefreshing = false;
        this.mDistanceToTriggerSync = -1.0f;
        this.mFromPercentage = 0.0f;
        this.mCurrPercentage = 0.0f;
        this.mAnimateToStartPosition = new Animation() {
            public void applyTransformation(float f, Transformation transformation) {
                int i = 0;
                if (SwipeRefreshLayout.this.mFrom != SwipeRefreshLayout.this.mOriginalOffsetTop) {
                    i = SwipeRefreshLayout.this.mFrom + ((int) (((float) (SwipeRefreshLayout.this.mOriginalOffsetTop - SwipeRefreshLayout.this.mFrom)) * f));
                }
                i -= SwipeRefreshLayout.this.mTarget.getTop();
                int top = SwipeRefreshLayout.this.mTarget.getTop();
                if (i + top < 0) {
                    i = 0 - top;
                }
                SwipeRefreshLayout.this.setTargetOffsetTopAndBottom(i);
            }
        };
        this.mShrinkTrigger = new Animation() {
            public void applyTransformation(float f, Transformation transformation) {
                SwipeRefreshLayout.this.mProgressBar.setTriggerPercentage(SwipeRefreshLayout.this.mFromPercentage + ((0.0f - SwipeRefreshLayout.this.mFromPercentage) * f));
            }
        };
        this.mReturnToStartPositionListener = new BaseAnimationListener() {
            public void onAnimationEnd(Animation animation) {
                SwipeRefreshLayout.this.mCurrentTargetOffsetTop = 0;
            }
        };
        this.mShrinkAnimationListener = new BaseAnimationListener() {
            public void onAnimationEnd(Animation animation) {
                SwipeRefreshLayout.this.mCurrPercentage = 0.0f;
            }
        };
        this.mReturnToStartPosition = new Runnable() {
            public void run() {
                SwipeRefreshLayout.this.mReturningToStart = true;
                SwipeRefreshLayout.this.animateOffsetToStartPosition(SwipeRefreshLayout.this.mCurrentTargetOffsetTop + SwipeRefreshLayout.this.getPaddingTop(), SwipeRefreshLayout.this.mReturnToStartPositionListener);
            }
        };
        this.mCancel = new Runnable() {
            public void run() {
                SwipeRefreshLayout.this.mReturningToStart = true;
                if (SwipeRefreshLayout.this.mProgressBar != null) {
                    SwipeRefreshLayout.this.mFromPercentage = SwipeRefreshLayout.this.mCurrPercentage;
                    SwipeRefreshLayout.this.mShrinkTrigger.setDuration((long) SwipeRefreshLayout.this.mMediumAnimationDuration);
                    SwipeRefreshLayout.this.mShrinkTrigger.setAnimationListener(SwipeRefreshLayout.this.mShrinkAnimationListener);
                    SwipeRefreshLayout.this.mShrinkTrigger.reset();
                    SwipeRefreshLayout.this.mShrinkTrigger.setInterpolator(SwipeRefreshLayout.this.mDecelerateInterpolator);
                    SwipeRefreshLayout.this.startAnimation(SwipeRefreshLayout.this.mShrinkTrigger);
                }
                SwipeRefreshLayout.this.animateOffsetToStartPosition(SwipeRefreshLayout.this.mCurrentTargetOffsetTop + SwipeRefreshLayout.this.getPaddingTop(), SwipeRefreshLayout.this.mReturnToStartPositionListener);
            }
        };
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mMediumAnimationDuration = getResources().getInteger(17694721);
        setWillNotDraw(false);
        this.mProgressBar = new SwipeProgressBar(this);
        this.mProgressBarHeight = (int) (PROGRESS_BAR_HEIGHT * getResources().getDisplayMetrics().density);
        this.mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
        this.mAccelerateInterpolator = new AccelerateInterpolator(ACCELERATE_INTERPOLATION_FACTOR);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, LAYOUT_ATTRS);
        setEnabled(obtainStyledAttributes.getBoolean(0, true));
        obtainStyledAttributes.recycle();
    }

    private void animateOffsetToStartPosition(int i, AnimationListener animationListener) {
        this.mFrom = i;
        this.mAnimateToStartPosition.reset();
        this.mAnimateToStartPosition.setDuration((long) this.mMediumAnimationDuration);
        this.mAnimateToStartPosition.setAnimationListener(animationListener);
        this.mAnimateToStartPosition.setInterpolator(this.mDecelerateInterpolator);
        this.mTarget.startAnimation(this.mAnimateToStartPosition);
    }

    private void ensureTarget() {
        if (this.mTarget == null) {
            if (getChildCount() <= 1 || isInEditMode()) {
                this.mTarget = getChildAt(0);
                this.mOriginalOffsetTop = this.mTarget.getTop() + getPaddingTop();
            } else {
                throw new IllegalStateException("SwipeRefreshLayout can host only one direct child");
            }
        }
        if (this.mDistanceToTriggerSync == -1.0f && getParent() != null && ((View) getParent()).getHeight() > 0) {
            this.mDistanceToTriggerSync = (float) ((int) Math.min(((float) ((View) getParent()).getHeight()) * MAX_SWIPE_DISTANCE_FACTOR, getResources().getDisplayMetrics().density * 120.0f));
        }
    }

    private void setTargetOffsetTopAndBottom(int i) {
        this.mTarget.offsetTopAndBottom(i);
        this.mCurrentTargetOffsetTop = this.mTarget.getTop();
    }

    private void setTriggerPercentage(float f) {
        if (f == 0.0f) {
            this.mCurrPercentage = 0.0f;
            return;
        }
        this.mCurrPercentage = f;
        this.mProgressBar.setTriggerPercentage(f);
    }

    private void startRefresh() {
        removeCallbacks(this.mCancel);
        this.mReturnToStartPosition.run();
        setRefreshing(true);
        this.mListener.onRefresh();
    }

    private void updateContentOffsetTop(int i) {
        int top = this.mTarget.getTop();
        if (((float) i) > this.mDistanceToTriggerSync) {
            i = (int) this.mDistanceToTriggerSync;
        } else if (i < 0) {
            i = 0;
        }
        setTargetOffsetTopAndBottom(i - top);
    }

    private void updatePositionTimeout() {
        removeCallbacks(this.mCancel);
        postDelayed(this.mCancel, RETURN_TO_ORIGINAL_POSITION_TIMEOUT);
    }

    public boolean canChildScrollUp() {
        if (VERSION.SDK_INT >= 14) {
            return ViewCompat.canScrollVertically(this.mTarget, -1);
        }
        if (this.mTarget instanceof AbsListView) {
            AbsListView absListView = (AbsListView) this.mTarget;
            if (absListView.getChildCount() <= 0 || (absListView.getFirstVisiblePosition() <= 0 && absListView.getChildAt(0).getTop() >= absListView.getPaddingTop())) {
                return false;
            }
        } else if (this.mTarget.getScrollY() <= 0) {
            return false;
        }
        return true;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        this.mProgressBar.draw(canvas);
    }

    public boolean isRefreshing() {
        return this.mRefreshing;
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(this.mCancel);
        removeCallbacks(this.mReturnToStartPosition);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mReturnToStartPosition);
        removeCallbacks(this.mCancel);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        ensureTarget();
        if (this.mReturningToStart && motionEvent.getAction() == 0) {
            this.mReturningToStart = false;
        }
        if (!(!isEnabled() || this.mReturningToStart || canChildScrollUp())) {
            z = onTouchEvent(motionEvent);
        }
        return !z ? super.onInterceptTouchEvent(motionEvent) : z;
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        this.mProgressBar.setBounds(0, 0, measuredWidth, this.mProgressBarHeight);
        if (getChildCount() != 0) {
            View childAt = getChildAt(0);
            int paddingLeft = getPaddingLeft();
            int paddingTop = this.mCurrentTargetOffsetTop + getPaddingTop();
            childAt.layout(paddingLeft, paddingTop, ((measuredWidth - getPaddingLeft()) - getPaddingRight()) + paddingLeft, ((measuredHeight - getPaddingTop()) - getPaddingBottom()) + paddingTop);
        }
    }

    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (getChildCount() > 1 && !isInEditMode()) {
            throw new IllegalStateException("SwipeRefreshLayout can host only one direct child");
        } else if (getChildCount() > 0) {
            getChildAt(0).measure(MeasureSpec.makeMeasureSpec((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), 1073741824), MeasureSpec.makeMeasureSpec((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), 1073741824));
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case 0:
                this.mCurrPercentage = 0.0f;
                this.mDownEvent = MotionEvent.obtain(motionEvent);
                this.mPrevY = this.mDownEvent.getY();
                return false;
            case 1:
            case 3:
                if (this.mDownEvent != null) {
                    this.mDownEvent.recycle();
                    this.mDownEvent = null;
                    return false;
                }
                break;
            case 2:
                if (!(this.mDownEvent == null || this.mReturningToStart)) {
                    float y = motionEvent.getY();
                    float y2 = y - this.mDownEvent.getY();
                    if (y2 > ((float) this.mTouchSlop)) {
                        if (y2 > this.mDistanceToTriggerSync) {
                            startRefresh();
                            return true;
                        }
                        setTriggerPercentage(this.mAccelerateInterpolator.getInterpolation(y2 / this.mDistanceToTriggerSync));
                        if (this.mPrevY > y) {
                            y2 -= (float) this.mTouchSlop;
                        }
                        updateContentOffsetTop((int) y2);
                        if (this.mPrevY <= y || this.mTarget.getTop() >= this.mTouchSlop) {
                            updatePositionTimeout();
                        } else {
                            removeCallbacks(this.mCancel);
                        }
                        this.mPrevY = motionEvent.getY();
                        return true;
                    }
                }
                break;
        }
        return false;
    }

    public void requestDisallowInterceptTouchEvent(boolean z) {
    }

    public void setColorScheme(int i, int i2, int i3, int i4) {
        ensureTarget();
        Resources resources = getResources();
        this.mProgressBar.setColorScheme(resources.getColor(i), resources.getColor(i2), resources.getColor(i3), resources.getColor(i4));
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mListener = onRefreshListener;
    }

    public void setRefreshing(boolean z) {
        if (this.mRefreshing != z) {
            ensureTarget();
            this.mCurrPercentage = 0.0f;
            this.mRefreshing = z;
            if (this.mRefreshing) {
                this.mProgressBar.start();
            } else {
                this.mProgressBar.stop();
            }
        }
    }
}
