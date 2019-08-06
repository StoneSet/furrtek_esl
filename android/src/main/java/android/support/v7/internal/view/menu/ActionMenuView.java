package android.support.v7.internal.view.menu;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.menu.MenuBuilder.ItemInvoker;
import android.support.v7.internal.widget.LinearLayoutICS;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.accessibility.AccessibilityEvent;

public class ActionMenuView extends LinearLayoutICS implements ItemInvoker, MenuView {
    static final int GENERATED_ITEM_PADDING = 4;
    static final int MIN_CELL_SIZE = 56;
    private static final String TAG = "ActionMenuView";
    private boolean mFormatItems;
    private int mFormatItemsWidth;
    private int mGeneratedItemPadding;
    private int mMaxItemHeight;
    private int mMeasuredExtraWidth;
    private MenuBuilder mMenu;
    private int mMinCellSize;
    private ActionMenuPresenter mPresenter;
    private boolean mReserveOverflow;

    public interface ActionMenuChildView {
        boolean needsDividerAfter();

        boolean needsDividerBefore();
    }

    public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
        @ExportedProperty
        public int cellsUsed;
        @ExportedProperty
        public boolean expandable;
        public boolean expanded;
        @ExportedProperty
        public int extraPixels;
        @ExportedProperty
        public boolean isOverflowButton;
        @ExportedProperty
        public boolean preventEdgeOffset;

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.isOverflowButton = false;
        }

        public LayoutParams(int i, int i2, boolean z) {
            super(i, i2);
            this.isOverflowButton = z;
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
            this.isOverflowButton = layoutParams.isOverflowButton;
        }
    }

    public ActionMenuView(Context context) {
        this(context, null);
    }

    public ActionMenuView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setBaselineAligned(false);
        float f = context.getResources().getDisplayMetrics().density;
        this.mMinCellSize = (int) (56.0f * f);
        this.mGeneratedItemPadding = (int) (f * 4.0f);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
        this.mMaxItemHeight = obtainStyledAttributes.getDimensionPixelSize(0, 0);
        obtainStyledAttributes.recycle();
    }

    static int measureChildForCells(View view, int i, int i2, int i3, int i4) {
        int i5;
        boolean z = false;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int makeMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(i3) - i4, MeasureSpec.getMode(i3));
        ActionMenuItemView actionMenuItemView = view instanceof ActionMenuItemView ? (ActionMenuItemView) view : null;
        boolean z2 = actionMenuItemView != null && actionMenuItemView.hasText();
        if (i2 <= 0 || (z2 && i2 < 2)) {
            i5 = 0;
        } else {
            view.measure(MeasureSpec.makeMeasureSpec(i * i2, ExploreByTouchHelper.INVALID_ID), makeMeasureSpec);
            int measuredWidth = view.getMeasuredWidth();
            i5 = measuredWidth / i;
            if (measuredWidth % i != 0) {
                i5++;
            }
            if (z2 && r1 < 2) {
                i5 = 2;
            }
        }
        if (!layoutParams.isOverflowButton && z2) {
            z = true;
        }
        layoutParams.expandable = z;
        layoutParams.cellsUsed = i5;
        view.measure(MeasureSpec.makeMeasureSpec(i5 * i, 1073741824), makeMeasureSpec);
        return i5;
    }

    private void onMeasureExactFormat(int i, int i2) {
        int mode = MeasureSpec.getMode(i2);
        int size = MeasureSpec.getSize(i);
        int size2 = MeasureSpec.getSize(i2);
        int paddingRight = getPaddingRight() + getPaddingLeft();
        int paddingTop = getPaddingTop() + getPaddingBottom();
        int makeMeasureSpec = mode == 1073741824 ? MeasureSpec.makeMeasureSpec(size2 - paddingTop, 1073741824) : MeasureSpec.makeMeasureSpec(Math.min(this.mMaxItemHeight, size2 - paddingTop), ExploreByTouchHelper.INVALID_ID);
        int i3 = size - paddingRight;
        int i4 = i3 / this.mMinCellSize;
        int i5 = i3 % this.mMinCellSize;
        if (i4 == 0) {
            setMeasuredDimension(i3, 0);
            return;
        }
        Object obj;
        int i6;
        LayoutParams layoutParams;
        int i7 = this.mMinCellSize + (i5 / i4);
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        size = 0;
        Object obj2 = null;
        long j = 0;
        int childCount = getChildCount();
        int i11 = 0;
        while (i11 < childCount) {
            int i12;
            int i13;
            long j2;
            int i14;
            View childAt = getChildAt(i11);
            if (childAt.getVisibility() == 8) {
                obj = obj2;
                i12 = i8;
                i13 = i9;
                int i15 = i10;
                i10 = i4;
                j2 = j;
                i6 = size;
                i14 = i15;
            } else {
                boolean z = childAt instanceof ActionMenuItemView;
                i12 = size + 1;
                if (z) {
                    childAt.setPadding(this.mGeneratedItemPadding, 0, this.mGeneratedItemPadding, 0);
                }
                layoutParams = (LayoutParams) childAt.getLayoutParams();
                layoutParams.expanded = false;
                layoutParams.extraPixels = 0;
                layoutParams.cellsUsed = 0;
                layoutParams.expandable = false;
                layoutParams.leftMargin = 0;
                layoutParams.rightMargin = 0;
                boolean z2 = z && ((ActionMenuItemView) childAt).hasText();
                layoutParams.preventEdgeOffset = z2;
                size = measureChildForCells(childAt, i7, layoutParams.isOverflowButton ? 1 : i4, makeMeasureSpec, paddingTop);
                i9 = Math.max(i9, size);
                if (layoutParams.expandable) {
                    i10++;
                }
                if (layoutParams.isOverflowButton) {
                    obj2 = 1;
                }
                i4 -= size;
                paddingRight = Math.max(i8, childAt.getMeasuredHeight());
                if (size == 1) {
                    j2 = ((long) (1 << i11)) | j;
                    i6 = i12;
                    i14 = i10;
                    i12 = paddingRight;
                    i10 = i4;
                    obj = obj2;
                    i13 = i9;
                } else {
                    j2 = j;
                    i6 = i12;
                    i14 = i10;
                    i12 = paddingRight;
                    i10 = i4;
                    obj = obj2;
                    i13 = i9;
                }
            }
            i11++;
            i8 = i12;
            i4 = i10;
            i10 = i14;
            i9 = i13;
            obj2 = obj;
            size = i6;
            j = j2;
        }
        obj = (obj2 == null || size != 2) ? null : 1;
        long j3 = j;
        int i16 = i4;
        Object obj3 = null;
        while (i10 > 0 && i16 > 0) {
            i14 = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
            long j4 = 0;
            i6 = 0;
            int i17 = 0;
            while (i17 < childCount) {
                layoutParams = (LayoutParams) getChildAt(i17).getLayoutParams();
                if (layoutParams.expandable) {
                    if (layoutParams.cellsUsed < i14) {
                        i12 = layoutParams.cellsUsed;
                        j = (long) (1 << i17);
                        i5 = 1;
                    } else if (layoutParams.cellsUsed == i14) {
                        i5 = i6 + 1;
                        i12 = i14;
                        j = j4 | ((long) (1 << i17));
                    }
                    i17++;
                    j4 = j;
                    i6 = i5;
                    i14 = i12;
                }
                i5 = i6;
                i12 = i14;
                j = j4;
                i17++;
                j4 = j;
                i6 = i5;
                i14 = i12;
            }
            j3 |= j4;
            if (i6 > i16) {
                break;
            }
            i17 = i14 + 1;
            i4 = 0;
            j = j3;
            i12 = i16;
            while (i4 < childCount) {
                View childAt2 = getChildAt(i4);
                layoutParams = (LayoutParams) childAt2.getLayoutParams();
                if ((((long) (1 << i4)) & j4) != 0) {
                    if (obj != null && layoutParams.preventEdgeOffset && i12 == 1) {
                        childAt2.setPadding(this.mGeneratedItemPadding + i7, 0, this.mGeneratedItemPadding, 0);
                    }
                    layoutParams.cellsUsed++;
                    layoutParams.expanded = true;
                    i5 = i12 - 1;
                } else if (layoutParams.cellsUsed == i17) {
                    j |= (long) (1 << i4);
                    i5 = i12;
                } else {
                    i5 = i12;
                }
                i4++;
                i12 = i5;
            }
            obj3 = 1;
            j3 = j;
            i16 = i12;
        }
        Object obj4 = (obj2 == null && size == 1) ? 1 : null;
        if (i16 <= 0 || j3 == 0 || (i16 >= size - 1 && obj4 == null && i9 <= 1)) {
            obj = obj3;
            i6 = i16;
        } else {
            float f;
            View childAt3;
            float bitCount = (float) Long.bitCount(j3);
            if (obj4 == null) {
                if (!((1 & j3) == 0 || ((LayoutParams) getChildAt(0).getLayoutParams()).preventEdgeOffset)) {
                    bitCount -= 0.5f;
                }
                if (!((((long) (1 << (childCount - 1))) & j3) == 0 || ((LayoutParams) getChildAt(childCount - 1).getLayoutParams()).preventEdgeOffset)) {
                    f = bitCount - 0.5f;
                    size = f <= 0.0f ? (int) (((float) (i16 * i7)) / f) : 0;
                    i6 = 0;
                    obj = obj3;
                    while (i6 < childCount) {
                        if ((((long) (1 << i6)) & j3) != 0) {
                            childAt3 = getChildAt(i6);
                            layoutParams = (LayoutParams) childAt3.getLayoutParams();
                            if (childAt3 instanceof ActionMenuItemView) {
                                layoutParams.extraPixels = size;
                                layoutParams.expanded = true;
                                if (i6 == 0 && !layoutParams.preventEdgeOffset) {
                                    layoutParams.leftMargin = (-size) / 2;
                                }
                                obj4 = 1;
                            } else if (layoutParams.isOverflowButton) {
                                if (i6 != 0) {
                                    layoutParams.leftMargin = size / 2;
                                }
                                if (i6 != childCount - 1) {
                                    layoutParams.rightMargin = size / 2;
                                    obj4 = obj;
                                }
                            } else {
                                layoutParams.extraPixels = size;
                                layoutParams.expanded = true;
                                layoutParams.rightMargin = (-size) / 2;
                                obj4 = 1;
                            }
                            i6++;
                            obj = obj4;
                        }
                        obj4 = obj;
                        i6++;
                        obj = obj4;
                    }
                    i6 = 0;
                }
            }
            f = bitCount;
            if (f <= 0.0f) {
            }
            i6 = 0;
            obj = obj3;
            while (i6 < childCount) {
                if ((((long) (1 << i6)) & j3) != 0) {
                    childAt3 = getChildAt(i6);
                    layoutParams = (LayoutParams) childAt3.getLayoutParams();
                    if (childAt3 instanceof ActionMenuItemView) {
                        layoutParams.extraPixels = size;
                        layoutParams.expanded = true;
                        layoutParams.leftMargin = (-size) / 2;
                        obj4 = 1;
                    } else if (layoutParams.isOverflowButton) {
                        if (i6 != 0) {
                            layoutParams.leftMargin = size / 2;
                        }
                        if (i6 != childCount - 1) {
                            layoutParams.rightMargin = size / 2;
                            obj4 = obj;
                        }
                    } else {
                        layoutParams.extraPixels = size;
                        layoutParams.expanded = true;
                        layoutParams.rightMargin = (-size) / 2;
                        obj4 = 1;
                    }
                    i6++;
                    obj = obj4;
                }
                obj4 = obj;
                i6++;
                obj = obj4;
            }
            i6 = 0;
        }
        if (obj != null) {
            for (size = 0; size < childCount; size++) {
                childAt = getChildAt(size);
                layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.expanded) {
                    childAt.measure(MeasureSpec.makeMeasureSpec(layoutParams.extraPixels + (layoutParams.cellsUsed * i7), 1073741824), makeMeasureSpec);
                }
            }
        }
        if (mode == 1073741824) {
            i8 = size2;
        }
        setMeasuredDimension(i3, i8);
        this.mMeasuredExtraWidth = i6 * i7;
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        return layoutParams != null && (layoutParams instanceof LayoutParams);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        return false;
    }

    protected LayoutParams generateDefaultLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.gravity = 16;
        return layoutParams;
    }

    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
        if (!(layoutParams instanceof LayoutParams)) {
            return generateDefaultLayoutParams();
        }
        LayoutParams layoutParams2 = new LayoutParams((LayoutParams) layoutParams);
        if (layoutParams2.gravity > 0) {
            return layoutParams2;
        }
        layoutParams2.gravity = 16;
        return layoutParams2;
    }

    public LayoutParams generateOverflowButtonLayoutParams() {
        LayoutParams generateDefaultLayoutParams = generateDefaultLayoutParams();
        generateDefaultLayoutParams.isOverflowButton = true;
        return generateDefaultLayoutParams;
    }

    public int getWindowAnimations() {
        return 0;
    }

    protected boolean hasSupportDividerBeforeChildAt(int i) {
        boolean z = false;
        View childAt = getChildAt(i - 1);
        View childAt2 = getChildAt(i);
        if (i < getChildCount() && (childAt instanceof ActionMenuChildView)) {
            z = ((ActionMenuChildView) childAt).needsDividerAfter() | 0;
        }
        return (i <= 0 || !(childAt2 instanceof ActionMenuChildView)) ? z : ((ActionMenuChildView) childAt2).needsDividerBefore() | z;
    }

    public void initialize(MenuBuilder menuBuilder) {
        this.mMenu = menuBuilder;
    }

    public boolean invokeItem(MenuItemImpl menuItemImpl) {
        return this.mMenu.performItemAction(menuItemImpl, 0);
    }

    public boolean isExpandedFormat() {
        return this.mFormatItems;
    }

    public boolean isOverflowReserved() {
        return this.mReserveOverflow;
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (VERSION.SDK_INT >= 8) {
            super.onConfigurationChanged(configuration);
        }
        this.mPresenter.updateMenuView(false);
        if (this.mPresenter != null && this.mPresenter.isOverflowMenuShowing()) {
            this.mPresenter.hideOverflowMenu();
            this.mPresenter.showOverflowMenu();
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPresenter.dismissPopupMenus();
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        if (this.mFormatItems) {
            int i5;
            LayoutParams layoutParams;
            int width;
            int childCount = getChildCount();
            int i6 = (i2 + i4) / 2;
            int supportDividerWidth = getSupportDividerWidth();
            int i7 = 0;
            int i8 = 0;
            int paddingRight = ((i3 - i) - getPaddingRight()) - getPaddingLeft();
            Object obj = null;
            int i9 = 0;
            while (i9 < childCount) {
                Object obj2;
                View childAt = getChildAt(i9);
                if (childAt.getVisibility() == 8) {
                    obj2 = obj;
                    i5 = paddingRight;
                    paddingRight = i8;
                    i8 = i7;
                } else {
                    layoutParams = (LayoutParams) childAt.getLayoutParams();
                    if (layoutParams.isOverflowButton) {
                        i5 = childAt.getMeasuredWidth();
                        if (hasSupportDividerBeforeChildAt(i9)) {
                            i5 += supportDividerWidth;
                        }
                        int measuredHeight = childAt.getMeasuredHeight();
                        width = (getWidth() - getPaddingRight()) - layoutParams.rightMargin;
                        int i10 = i6 - (measuredHeight / 2);
                        childAt.layout(width - i5, i10, width, measuredHeight + i10);
                        i5 = paddingRight - i5;
                        obj2 = 1;
                        paddingRight = i8;
                        i8 = i7;
                    } else {
                        width = layoutParams.rightMargin + (childAt.getMeasuredWidth() + layoutParams.leftMargin);
                        i7 += width;
                        width = paddingRight - width;
                        if (hasSupportDividerBeforeChildAt(i9)) {
                            i7 += supportDividerWidth;
                        }
                        paddingRight = i8 + 1;
                        i8 = i7;
                        int i11 = width;
                        obj2 = obj;
                        i5 = i11;
                    }
                }
                i9++;
                i7 = i8;
                i8 = paddingRight;
                paddingRight = i5;
                obj = obj2;
            }
            if (childCount == 1 && obj == null) {
                View childAt2 = getChildAt(0);
                i5 = childAt2.getMeasuredWidth();
                paddingRight = childAt2.getMeasuredHeight();
                i8 = ((i3 - i) / 2) - (i5 / 2);
                i7 = i6 - (paddingRight / 2);
                childAt2.layout(i8, i7, i5 + i8, paddingRight + i7);
                return;
            }
            width = i8 - (obj != null ? 0 : 1);
            i8 = Math.max(0, width > 0 ? paddingRight / width : 0);
            i5 = getPaddingLeft();
            paddingRight = 0;
            while (paddingRight < childCount) {
                View childAt3 = getChildAt(paddingRight);
                layoutParams = (LayoutParams) childAt3.getLayoutParams();
                if (childAt3.getVisibility() == 8 || layoutParams.isOverflowButton) {
                    width = i5;
                } else {
                    i5 += layoutParams.leftMargin;
                    i9 = childAt3.getMeasuredWidth();
                    supportDividerWidth = childAt3.getMeasuredHeight();
                    int i12 = i6 - (supportDividerWidth / 2);
                    childAt3.layout(i5, i12, i5 + i9, supportDividerWidth + i12);
                    width = ((layoutParams.rightMargin + i9) + i8) + i5;
                }
                paddingRight++;
                i5 = width;
            }
            return;
        }
        super.onLayout(z, i, i2, i3, i4);
    }

    protected void onMeasure(int i, int i2) {
        boolean z = this.mFormatItems;
        this.mFormatItems = MeasureSpec.getMode(i) == 1073741824;
        if (z != this.mFormatItems) {
            this.mFormatItemsWidth = 0;
        }
        int mode = MeasureSpec.getMode(i);
        if (!(!this.mFormatItems || this.mMenu == null || mode == this.mFormatItemsWidth)) {
            this.mFormatItemsWidth = mode;
            this.mMenu.onItemsChanged(true);
        }
        if (this.mFormatItems) {
            onMeasureExactFormat(i, i2);
            return;
        }
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i3).getLayoutParams();
            layoutParams.rightMargin = 0;
            layoutParams.leftMargin = 0;
        }
        super.onMeasure(i, i2);
    }

    public void setOverflowReserved(boolean z) {
        this.mReserveOverflow = z;
    }

    public void setPresenter(ActionMenuPresenter actionMenuPresenter) {
        this.mPresenter = actionMenuPresenter;
    }
}
