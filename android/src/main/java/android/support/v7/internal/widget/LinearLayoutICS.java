package android.support.v7.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.appcompat.R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class LinearLayoutICS extends LinearLayout {
    private static final int SHOW_DIVIDER_BEGINNING = 1;
    private static final int SHOW_DIVIDER_END = 4;
    private static final int SHOW_DIVIDER_MIDDLE = 2;
    private static final int SHOW_DIVIDER_NONE = 0;
    private final Drawable mDivider;
    private final int mDividerHeight;
    private final int mDividerPadding;
    private final int mDividerWidth;
    private final int mShowDividers;

    public LinearLayoutICS(Context context, AttributeSet attributeSet) {
        boolean z = false;
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.LinearLayoutICS);
        this.mDivider = obtainStyledAttributes.getDrawable(0);
        if (this.mDivider != null) {
            this.mDividerWidth = this.mDivider.getIntrinsicWidth();
            this.mDividerHeight = this.mDivider.getIntrinsicHeight();
        } else {
            this.mDividerWidth = 0;
            this.mDividerHeight = 0;
        }
        this.mShowDividers = obtainStyledAttributes.getInt(1, 0);
        this.mDividerPadding = obtainStyledAttributes.getDimensionPixelSize(2, 0);
        obtainStyledAttributes.recycle();
        if (this.mDivider == null) {
            z = true;
        }
        setWillNotDraw(z);
    }

    void drawSupportDividersHorizontal(Canvas canvas) {
        int childCount = getChildCount();
        int i = 0;
        while (i < childCount) {
            View childAt = getChildAt(i);
            if (!(childAt == null || childAt.getVisibility() == 8 || !hasSupportDividerBeforeChildAt(i))) {
                drawSupportVerticalDivider(canvas, childAt.getLeft() - ((LayoutParams) childAt.getLayoutParams()).leftMargin);
            }
            i++;
        }
        if (hasSupportDividerBeforeChildAt(childCount)) {
            View childAt2 = getChildAt(childCount - 1);
            drawSupportVerticalDivider(canvas, childAt2 == null ? (getWidth() - getPaddingRight()) - this.mDividerWidth : childAt2.getRight());
        }
    }

    void drawSupportDividersVertical(Canvas canvas) {
        int childCount = getChildCount();
        int i = 0;
        while (i < childCount) {
            View childAt = getChildAt(i);
            if (!(childAt == null || childAt.getVisibility() == 8 || !hasSupportDividerBeforeChildAt(i))) {
                drawSupportHorizontalDivider(canvas, childAt.getTop() - ((LayoutParams) childAt.getLayoutParams()).topMargin);
            }
            i++;
        }
        if (hasSupportDividerBeforeChildAt(childCount)) {
            View childAt2 = getChildAt(childCount - 1);
            drawSupportHorizontalDivider(canvas, childAt2 == null ? (getHeight() - getPaddingBottom()) - this.mDividerHeight : childAt2.getBottom());
        }
    }

    void drawSupportHorizontalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(getPaddingLeft() + this.mDividerPadding, i, (getWidth() - getPaddingRight()) - this.mDividerPadding, this.mDividerHeight + i);
        this.mDivider.draw(canvas);
    }

    void drawSupportVerticalDivider(Canvas canvas, int i) {
        this.mDivider.setBounds(i, getPaddingTop() + this.mDividerPadding, this.mDividerWidth + i, (getHeight() - getPaddingBottom()) - this.mDividerPadding);
        this.mDivider.draw(canvas);
    }

    public int getSupportDividerWidth() {
        return this.mDividerWidth;
    }

    protected boolean hasSupportDividerBeforeChildAt(int i) {
        boolean z = true;
        if (i == 0) {
            if ((this.mShowDividers & 1) == 0) {
                return false;
            }
        } else if (i == getChildCount()) {
            if ((this.mShowDividers & 4) == 0) {
                return false;
            }
        } else if ((this.mShowDividers & 2) == 0) {
            return false;
        } else {
            for (int i2 = i - 1; i2 >= 0; i2--) {
                if (getChildAt(i2).getVisibility() != 8) {
                    break;
                }
            }
            z = false;
            return z;
        }
        return true;
    }

    protected void measureChildWithMargins(View view, int i, int i2, int i3, int i4) {
        if (this.mDivider != null) {
            int indexOfChild = indexOfChild(view);
            int childCount = getChildCount();
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (getOrientation() == 1) {
                if (hasSupportDividerBeforeChildAt(indexOfChild)) {
                    layoutParams.topMargin = this.mDividerHeight;
                } else if (indexOfChild == childCount - 1 && hasSupportDividerBeforeChildAt(childCount)) {
                    layoutParams.bottomMargin = this.mDividerHeight;
                }
            } else if (hasSupportDividerBeforeChildAt(indexOfChild)) {
                layoutParams.leftMargin = this.mDividerWidth;
            } else if (indexOfChild == childCount - 1 && hasSupportDividerBeforeChildAt(childCount)) {
                layoutParams.rightMargin = this.mDividerWidth;
            }
        }
        super.measureChildWithMargins(view, i, i2, i3, i4);
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDivider != null) {
            if (getOrientation() == 1) {
                drawSupportDividersVertical(canvas);
            } else {
                drawSupportDividersHorizontal(canvas);
            }
        }
    }
}
