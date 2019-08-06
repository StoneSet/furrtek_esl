package android.support.v7.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

public class ProgressBarICS extends View {
    private static final int ANIMATION_RESOLUTION = 200;
    private static final int MAX_LEVEL = 10000;
    private static final int[] android_R_styleable_ProgressBar = new int[]{16843062, 16843063, 16843064, 16843065, 16843066, 16843067, 16843068, 16843069, 16843070, 16843071, 16843039, 16843072, 16843040, 16843073};
    private AlphaAnimation mAnimation;
    private int mBehavior;
    private Drawable mCurrentDrawable;
    private int mDuration;
    private boolean mInDrawing;
    private boolean mIndeterminate;
    private Drawable mIndeterminateDrawable;
    private Interpolator mInterpolator;
    private long mLastDrawTime;
    private int mMax;
    int mMaxHeight;
    int mMaxWidth;
    int mMinHeight;
    int mMinWidth;
    private boolean mNoInvalidate;
    private boolean mOnlyIndeterminate;
    private int mProgress;
    private Drawable mProgressDrawable;
    private RefreshProgressRunnable mRefreshProgressRunnable;
    Bitmap mSampleTile;
    private int mSecondaryProgress;
    private boolean mShouldStartAnimationDrawable;
    private Transformation mTransformation;
    private long mUiThreadId = Thread.currentThread().getId();

    private class RefreshProgressRunnable implements Runnable {
        private boolean mFromUser;
        private int mId;
        private int mProgress;

        RefreshProgressRunnable(int i, int i2, boolean z) {
            this.mId = i;
            this.mProgress = i2;
            this.mFromUser = z;
        }

        public void run() {
            ProgressBarICS.this.doRefreshProgress(this.mId, this.mProgress, this.mFromUser, true);
            ProgressBarICS.this.mRefreshProgressRunnable = this;
        }

        public void setup(int i, int i2, boolean z) {
            this.mId = i;
            this.mProgress = i2;
            this.mFromUser = z;
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        int progress;
        int secondaryProgress;

        private SavedState(Parcel parcel) {
            super(parcel);
            this.progress = parcel.readInt();
            this.secondaryProgress = parcel.readInt();
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.progress);
            parcel.writeInt(this.secondaryProgress);
        }
    }

    public ProgressBarICS(Context context, AttributeSet attributeSet, int i, int i2) {
        boolean z = false;
        super(context, attributeSet, i);
        initProgressBar();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, android_R_styleable_ProgressBar, i, i2);
        this.mNoInvalidate = true;
        setMax(obtainStyledAttributes.getInt(0, this.mMax));
        setProgress(obtainStyledAttributes.getInt(1, this.mProgress));
        setSecondaryProgress(obtainStyledAttributes.getInt(2, this.mSecondaryProgress));
        boolean z2 = obtainStyledAttributes.getBoolean(3, this.mIndeterminate);
        this.mOnlyIndeterminate = obtainStyledAttributes.getBoolean(4, this.mOnlyIndeterminate);
        Drawable drawable = obtainStyledAttributes.getDrawable(5);
        if (drawable != null) {
            setIndeterminateDrawable(tileifyIndeterminate(drawable));
        }
        drawable = obtainStyledAttributes.getDrawable(6);
        if (drawable != null) {
            setProgressDrawable(tileify(drawable, false));
        }
        this.mDuration = obtainStyledAttributes.getInt(7, this.mDuration);
        this.mBehavior = obtainStyledAttributes.getInt(8, this.mBehavior);
        this.mMinWidth = obtainStyledAttributes.getDimensionPixelSize(9, this.mMinWidth);
        this.mMaxWidth = obtainStyledAttributes.getDimensionPixelSize(10, this.mMaxWidth);
        this.mMinHeight = obtainStyledAttributes.getDimensionPixelSize(11, this.mMinHeight);
        this.mMaxHeight = obtainStyledAttributes.getDimensionPixelSize(12, this.mMaxHeight);
        int resourceId = obtainStyledAttributes.getResourceId(13, 17432587);
        if (resourceId > 0) {
            setInterpolator(context, resourceId);
        }
        obtainStyledAttributes.recycle();
        this.mNoInvalidate = false;
        if (this.mOnlyIndeterminate || z2) {
            z = true;
        }
        setIndeterminate(z);
    }

    private void doRefreshProgress(int i, int i2, boolean z, boolean z2) {
        synchronized (this) {
            float f = this.mMax > 0 ? ((float) i2) / ((float) this.mMax) : 0.0f;
            Drawable drawable = this.mCurrentDrawable;
            if (drawable != null) {
                Drawable drawable2 = null;
                if (drawable instanceof LayerDrawable) {
                    drawable2 = ((LayerDrawable) drawable).findDrawableByLayerId(i);
                }
                int i3 = (int) (f * 10000.0f);
                if (drawable2 != null) {
                    drawable = drawable2;
                }
                drawable.setLevel(i3);
            } else {
                invalidate();
            }
        }
    }

    private void initProgressBar() {
        this.mMax = 100;
        this.mProgress = 0;
        this.mSecondaryProgress = 0;
        this.mIndeterminate = false;
        this.mOnlyIndeterminate = false;
        this.mDuration = 4000;
        this.mBehavior = 1;
        this.mMinWidth = 24;
        this.mMaxWidth = 48;
        this.mMinHeight = 24;
        this.mMaxHeight = 48;
    }

    private void refreshProgress(int i, int i2, boolean z) {
        synchronized (this) {
            if (this.mUiThreadId == Thread.currentThread().getId()) {
                doRefreshProgress(i, i2, z, true);
            } else {
                Runnable runnable;
                if (this.mRefreshProgressRunnable != null) {
                    runnable = this.mRefreshProgressRunnable;
                    this.mRefreshProgressRunnable = null;
                    runnable.setup(i, i2, z);
                } else {
                    runnable = new RefreshProgressRunnable(i, i2, z);
                }
                post(runnable);
            }
        }
    }

    private Drawable tileify(Drawable drawable, boolean z) {
        Drawable layerDrawable;
        int i = 0;
        if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable2 = (LayerDrawable) drawable;
            int numberOfLayers = layerDrawable2.getNumberOfLayers();
            Drawable[] drawableArr = new Drawable[numberOfLayers];
            for (int i2 = 0; i2 < numberOfLayers; i2++) {
                int id = layerDrawable2.getId(i2);
                Drawable drawable2 = layerDrawable2.getDrawable(i2);
                boolean z2 = id == 16908301 || id == 16908303;
                drawableArr[i2] = tileify(drawable2, z2);
            }
            layerDrawable = new LayerDrawable(drawableArr);
            while (i < numberOfLayers) {
                layerDrawable.setId(i, layerDrawable2.getId(i));
                i++;
            }
        } else if (!(drawable instanceof BitmapDrawable)) {
            return drawable;
        } else {
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            if (this.mSampleTile == null) {
                this.mSampleTile = bitmap;
            }
            Drawable shapeDrawable = new ShapeDrawable(getDrawableShape());
            shapeDrawable.getPaint().setShader(new BitmapShader(bitmap, TileMode.REPEAT, TileMode.CLAMP));
            layerDrawable = z ? new ClipDrawable(shapeDrawable, 3, 1) : shapeDrawable;
        }
        return layerDrawable;
    }

    private Drawable tileifyIndeterminate(Drawable drawable) {
        if (!(drawable instanceof AnimationDrawable)) {
            return drawable;
        }
        AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
        int numberOfFrames = animationDrawable.getNumberOfFrames();
        Drawable animationDrawable2 = new AnimationDrawable();
        animationDrawable2.setOneShot(animationDrawable.isOneShot());
        for (int i = 0; i < numberOfFrames; i++) {
            Drawable tileify = tileify(animationDrawable.getFrame(i), true);
            tileify.setLevel(MAX_LEVEL);
            animationDrawable2.addFrame(tileify, animationDrawable.getDuration(i));
        }
        animationDrawable2.setLevel(MAX_LEVEL);
        return animationDrawable2;
    }

    private void updateDrawableBounds(int i, int i2) {
        int i3;
        int paddingRight = (i - getPaddingRight()) - getPaddingLeft();
        int paddingBottom = (i2 - getPaddingBottom()) - getPaddingTop();
        if (this.mIndeterminateDrawable != null) {
            int i4;
            if (this.mOnlyIndeterminate && !(this.mIndeterminateDrawable instanceof AnimationDrawable)) {
                float intrinsicWidth = ((float) this.mIndeterminateDrawable.getIntrinsicWidth()) / ((float) this.mIndeterminateDrawable.getIntrinsicHeight());
                float f = ((float) i) / ((float) i2);
                if (intrinsicWidth != f) {
                    if (f > intrinsicWidth) {
                        paddingRight = (int) (intrinsicWidth * ((float) i2));
                        i4 = (i - paddingRight) / 2;
                        i3 = paddingRight + i4;
                        paddingRight = paddingBottom;
                        paddingBottom = i4;
                        i4 = 0;
                    } else {
                        paddingBottom = (int) ((1.0f / intrinsicWidth) * ((float) i));
                        i4 = (i2 - paddingBottom) / 2;
                        i3 = paddingRight;
                        paddingRight = paddingBottom + i4;
                        paddingBottom = 0;
                    }
                    this.mIndeterminateDrawable.setBounds(paddingBottom, i4, i3, paddingRight);
                }
            }
            i4 = 0;
            i3 = paddingRight;
            paddingRight = paddingBottom;
            paddingBottom = 0;
            this.mIndeterminateDrawable.setBounds(paddingBottom, i4, i3, paddingRight);
        } else {
            i3 = paddingRight;
            paddingRight = paddingBottom;
        }
        if (this.mProgressDrawable != null) {
            this.mProgressDrawable.setBounds(0, 0, i3, paddingRight);
        }
    }

    private void updateDrawableState() {
        int[] drawableState = getDrawableState();
        if (this.mProgressDrawable != null && this.mProgressDrawable.isStateful()) {
            this.mProgressDrawable.setState(drawableState);
        }
        if (this.mIndeterminateDrawable != null && this.mIndeterminateDrawable.isStateful()) {
            this.mIndeterminateDrawable.setState(drawableState);
        }
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        updateDrawableState();
    }

    Shape getDrawableShape() {
        return new RoundRectShape(new float[]{5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f}, null, null);
    }

    public Drawable getIndeterminateDrawable() {
        return this.mIndeterminateDrawable;
    }

    public Interpolator getInterpolator() {
        return this.mInterpolator;
    }

    public int getMax() {
        int i;
        synchronized (this) {
            i = this.mMax;
        }
        return i;
    }

    public int getProgress() {
        int i;
        synchronized (this) {
            i = this.mIndeterminate ? 0 : this.mProgress;
        }
        return i;
    }

    public Drawable getProgressDrawable() {
        return this.mProgressDrawable;
    }

    public int getSecondaryProgress() {
        int i;
        synchronized (this) {
            i = this.mIndeterminate ? 0 : this.mSecondaryProgress;
        }
        return i;
    }

    public final void incrementProgressBy(int i) {
        synchronized (this) {
            setProgress(this.mProgress + i);
        }
    }

    public final void incrementSecondaryProgressBy(int i) {
        synchronized (this) {
            setSecondaryProgress(this.mSecondaryProgress + i);
        }
    }

    public void invalidateDrawable(Drawable drawable) {
        if (!this.mInDrawing) {
            if (verifyDrawable(drawable)) {
                Rect bounds = drawable.getBounds();
                int scrollX = getScrollX() + getPaddingLeft();
                int scrollY = getScrollY() + getPaddingTop();
                invalidate(bounds.left + scrollX, bounds.top + scrollY, scrollX + bounds.right, bounds.bottom + scrollY);
                return;
            }
            super.invalidateDrawable(drawable);
        }
    }

    public boolean isIndeterminate() {
        boolean z;
        synchronized (this) {
            z = this.mIndeterminate;
        }
        return z;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mIndeterminate) {
            startAnimation();
        }
    }

    protected void onDetachedFromWindow() {
        if (this.mIndeterminate) {
            stopAnimation();
        }
        if (this.mRefreshProgressRunnable != null) {
            removeCallbacks(this.mRefreshProgressRunnable);
        }
        super.onDetachedFromWindow();
    }

    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            super.onDraw(canvas);
            Drawable drawable = this.mCurrentDrawable;
            if (drawable != null) {
                canvas.save();
                canvas.translate((float) getPaddingLeft(), (float) getPaddingTop());
                long drawingTime = getDrawingTime();
                if (this.mAnimation != null) {
                    this.mAnimation.getTransformation(drawingTime, this.mTransformation);
                    float alpha = this.mTransformation.getAlpha();
                    try {
                        this.mInDrawing = true;
                        drawable.setLevel((int) (alpha * 10000.0f));
                        this.mInDrawing = false;
                        if (SystemClock.uptimeMillis() - this.mLastDrawTime >= 200) {
                            this.mLastDrawTime = SystemClock.uptimeMillis();
                            postInvalidateDelayed(200);
                        }
                    } catch (Throwable th) {
                        this.mInDrawing = false;
                    }
                }
                drawable.draw(canvas);
                canvas.restore();
                if (this.mShouldStartAnimationDrawable && (drawable instanceof Animatable)) {
                    ((Animatable) drawable).start();
                    this.mShouldStartAnimationDrawable = false;
                }
            }
        }
    }

    protected void onMeasure(int i, int i2) {
        int i3 = 0;
        synchronized (this) {
            int max;
            Drawable drawable = this.mCurrentDrawable;
            if (drawable != null) {
                i3 = Math.max(this.mMinWidth, Math.min(this.mMaxWidth, drawable.getIntrinsicWidth()));
                max = Math.max(this.mMinHeight, Math.min(this.mMaxHeight, drawable.getIntrinsicHeight()));
            } else {
                max = 0;
            }
            updateDrawableState();
            setMeasuredDimension(resolveSize(i3 + (getPaddingLeft() + getPaddingRight()), i), resolveSize(max + (getPaddingTop() + getPaddingBottom()), i2));
        }
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        setProgress(savedState.progress);
        setSecondaryProgress(savedState.secondaryProgress);
    }

    public Parcelable onSaveInstanceState() {
        Parcelable savedState = new SavedState(super.onSaveInstanceState());
        savedState.progress = this.mProgress;
        savedState.secondaryProgress = this.mSecondaryProgress;
        return savedState;
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        updateDrawableBounds(i, i2);
    }

    protected void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (!this.mIndeterminate) {
            return;
        }
        if (i == 8 || i == 4) {
            stopAnimation();
        } else {
            startAnimation();
        }
    }

    public void postInvalidate() {
        if (!this.mNoInvalidate) {
            super.postInvalidate();
        }
    }

    public void setIndeterminate(boolean z) {
        synchronized (this) {
            if (!((this.mOnlyIndeterminate && this.mIndeterminate) || z == this.mIndeterminate)) {
                this.mIndeterminate = z;
                if (z) {
                    this.mCurrentDrawable = this.mIndeterminateDrawable;
                    startAnimation();
                } else {
                    this.mCurrentDrawable = this.mProgressDrawable;
                    stopAnimation();
                }
            }
        }
    }

    public void setIndeterminateDrawable(Drawable drawable) {
        if (drawable != null) {
            drawable.setCallback(this);
        }
        this.mIndeterminateDrawable = drawable;
        if (this.mIndeterminate) {
            this.mCurrentDrawable = drawable;
            postInvalidate();
        }
    }

    public void setInterpolator(Context context, int i) {
        setInterpolator(AnimationUtils.loadInterpolator(context, i));
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public void setMax(int i) {
        synchronized (this) {
            if (i < 0) {
                i = 0;
            }
            if (i != this.mMax) {
                this.mMax = i;
                postInvalidate();
                if (this.mProgress > i) {
                    this.mProgress = i;
                }
                refreshProgress(16908301, this.mProgress, false);
            }
        }
    }

    public void setProgress(int i) {
        synchronized (this) {
            setProgress(i, false);
        }
    }

    void setProgress(int i, boolean z) {
        synchronized (this) {
            if (!this.mIndeterminate) {
                int i2 = i < 0 ? 0 : i;
                if (i2 > this.mMax) {
                    i2 = this.mMax;
                }
                if (i2 != this.mProgress) {
                    this.mProgress = i2;
                    refreshProgress(16908301, this.mProgress, z);
                }
            }
        }
    }

    public void setProgressDrawable(Drawable drawable) {
        boolean z;
        if (this.mProgressDrawable == null || drawable == this.mProgressDrawable) {
            z = false;
        } else {
            this.mProgressDrawable.setCallback(null);
            z = true;
        }
        if (drawable != null) {
            drawable.setCallback(this);
            int minimumHeight = drawable.getMinimumHeight();
            if (this.mMaxHeight < minimumHeight) {
                this.mMaxHeight = minimumHeight;
                requestLayout();
            }
        }
        this.mProgressDrawable = drawable;
        if (!this.mIndeterminate) {
            this.mCurrentDrawable = drawable;
            postInvalidate();
        }
        if (z) {
            updateDrawableBounds(getWidth(), getHeight());
            updateDrawableState();
            doRefreshProgress(16908301, this.mProgress, false, false);
            doRefreshProgress(16908303, this.mSecondaryProgress, false, false);
        }
    }

    public void setSecondaryProgress(int i) {
        int i2 = 0;
        synchronized (this) {
            if (!this.mIndeterminate) {
                if (i >= 0) {
                    i2 = i;
                }
                if (i2 > this.mMax) {
                    i2 = this.mMax;
                }
                if (i2 != this.mSecondaryProgress) {
                    this.mSecondaryProgress = i2;
                    refreshProgress(16908303, this.mSecondaryProgress, false);
                }
            }
        }
    }

    public void setVisibility(int i) {
        if (getVisibility() != i) {
            super.setVisibility(i);
            if (!this.mIndeterminate) {
                return;
            }
            if (i == 8 || i == 4) {
                stopAnimation();
            } else {
                startAnimation();
            }
        }
    }

    void startAnimation() {
        if (getVisibility() == 0) {
            if (this.mIndeterminateDrawable instanceof Animatable) {
                this.mShouldStartAnimationDrawable = true;
                this.mAnimation = null;
            } else {
                if (this.mInterpolator == null) {
                    this.mInterpolator = new LinearInterpolator();
                }
                this.mTransformation = new Transformation();
                this.mAnimation = new AlphaAnimation(0.0f, 1.0f);
                this.mAnimation.setRepeatMode(this.mBehavior);
                this.mAnimation.setRepeatCount(-1);
                this.mAnimation.setDuration((long) this.mDuration);
                this.mAnimation.setInterpolator(this.mInterpolator);
                this.mAnimation.setStartTime(-1);
            }
            postInvalidate();
        }
    }

    void stopAnimation() {
        this.mAnimation = null;
        this.mTransformation = null;
        if (this.mIndeterminateDrawable instanceof Animatable) {
            ((Animatable) this.mIndeterminateDrawable).stop();
            this.mShouldStartAnimationDrawable = false;
        }
        postInvalidate();
    }

    protected boolean verifyDrawable(Drawable drawable) {
        return drawable == this.mProgressDrawable || drawable == this.mIndeterminateDrawable || super.verifyDrawable(drawable);
    }
}
