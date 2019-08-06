package android.support.v7.internal.widget;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.appcompat.R;
import android.support.v7.internal.widget.AdapterViewICS.OnItemClickListener;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;

class SpinnerICS extends AbsSpinnerICS implements OnClickListener {
    private static final int MAX_ITEMS_MEASURED = 15;
    static final int MODE_DIALOG = 0;
    static final int MODE_DROPDOWN = 1;
    private static final int MODE_THEME = -1;
    private static final String TAG = "Spinner";
    int mDropDownWidth;
    private int mGravity;
    private SpinnerPopup mPopup;
    private DropDownAdapter mTempAdapter;
    private Rect mTempRect;

    private interface SpinnerPopup {
        void dismiss();

        CharSequence getHintText();

        boolean isShowing();

        void setAdapter(ListAdapter listAdapter);

        void setPromptText(CharSequence charSequence);

        void show();
    }

    private class DialogPopup implements SpinnerPopup, OnClickListener {
        private ListAdapter mListAdapter;
        private AlertDialog mPopup;
        private CharSequence mPrompt;

        private DialogPopup() {
        }

        public void dismiss() {
            this.mPopup.dismiss();
            this.mPopup = null;
        }

        public CharSequence getHintText() {
            return this.mPrompt;
        }

        public boolean isShowing() {
            return this.mPopup != null ? this.mPopup.isShowing() : false;
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            SpinnerICS.this.setSelection(i);
            if (SpinnerICS.this.mOnItemClickListener != null) {
                SpinnerICS.this.performItemClick(null, i, this.mListAdapter.getItemId(i));
            }
            dismiss();
        }

        public void setAdapter(ListAdapter listAdapter) {
            this.mListAdapter = listAdapter;
        }

        public void setPromptText(CharSequence charSequence) {
            this.mPrompt = charSequence;
        }

        public void show() {
            Builder builder = new Builder(SpinnerICS.this.getContext());
            if (this.mPrompt != null) {
                builder.setTitle(this.mPrompt);
            }
            this.mPopup = builder.setSingleChoiceItems(this.mListAdapter, SpinnerICS.this.getSelectedItemPosition(), this).show();
        }
    }

    private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public DropDownAdapter(SpinnerAdapter spinnerAdapter) {
            this.mAdapter = spinnerAdapter;
            if (spinnerAdapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) spinnerAdapter;
            }
        }

        public boolean areAllItemsEnabled() {
            ListAdapter listAdapter = this.mListAdapter;
            return listAdapter != null ? listAdapter.areAllItemsEnabled() : true;
        }

        public int getCount() {
            return this.mAdapter == null ? 0 : this.mAdapter.getCount();
        }

        public View getDropDownView(int i, View view, ViewGroup viewGroup) {
            return this.mAdapter == null ? null : this.mAdapter.getDropDownView(i, view, viewGroup);
        }

        public Object getItem(int i) {
            return this.mAdapter == null ? null : this.mAdapter.getItem(i);
        }

        public long getItemId(int i) {
            return this.mAdapter == null ? -1 : this.mAdapter.getItemId(i);
        }

        public int getItemViewType(int i) {
            return 0;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            return getDropDownView(i, view, viewGroup);
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean hasStableIds() {
            return this.mAdapter != null && this.mAdapter.hasStableIds();
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }

        public boolean isEnabled(int i) {
            ListAdapter listAdapter = this.mListAdapter;
            return listAdapter != null ? listAdapter.isEnabled(i) : true;
        }

        public void registerDataSetObserver(DataSetObserver dataSetObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(dataSetObserver);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(dataSetObserver);
            }
        }
    }

    private class DropdownPopup extends ListPopupWindow implements SpinnerPopup {
        private ListAdapter mAdapter;
        private CharSequence mHintText;

        public DropdownPopup(Context context, AttributeSet attributeSet, int i) {
            super(context, attributeSet, i);
            setAnchorView(SpinnerICS.this);
            setModal(true);
            setPromptPosition(0);
            setOnItemClickListener(new OnItemClickListenerWrapper(new OnItemClickListener(SpinnerICS.this) {
                public void onItemClick(AdapterViewICS adapterViewICS, View view, int i, long j) {
                    SpinnerICS.this.setSelection(i);
                    if (SpinnerICS.this.mOnItemClickListener != null) {
                        SpinnerICS.this.performItemClick(view, i, DropdownPopup.this.mAdapter.getItemId(i));
                    }
                    DropdownPopup.this.dismiss();
                }
            }));
        }

        public CharSequence getHintText() {
            return this.mHintText;
        }

        public void setAdapter(ListAdapter listAdapter) {
            super.setAdapter(listAdapter);
            this.mAdapter = listAdapter;
        }

        public void setPromptText(CharSequence charSequence) {
            this.mHintText = charSequence;
        }

        public void show() {
            int paddingLeft = SpinnerICS.this.getPaddingLeft();
            if (SpinnerICS.this.mDropDownWidth == -2) {
                int width = SpinnerICS.this.getWidth();
                setContentWidth(Math.max(SpinnerICS.this.measureContentWidth((SpinnerAdapter) this.mAdapter, getBackground()), (width - paddingLeft) - SpinnerICS.this.getPaddingRight()));
            } else if (SpinnerICS.this.mDropDownWidth == -1) {
                setContentWidth((SpinnerICS.this.getWidth() - paddingLeft) - SpinnerICS.this.getPaddingRight());
            } else {
                setContentWidth(SpinnerICS.this.mDropDownWidth);
            }
            Drawable background = getBackground();
            int i = 0;
            if (background != null) {
                background.getPadding(SpinnerICS.this.mTempRect);
                i = -SpinnerICS.this.mTempRect.left;
            }
            setHorizontalOffset(i + paddingLeft);
            setInputMethodMode(2);
            super.show();
            getListView().setChoiceMode(1);
            setSelection(SpinnerICS.this.getSelectedItemPosition());
        }
    }

    SpinnerICS(Context context) {
        this(context, null);
    }

    SpinnerICS(Context context, int i) {
        this(context, null, R.attr.spinnerStyle, i);
    }

    SpinnerICS(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, R.attr.spinnerStyle);
    }

    SpinnerICS(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, -1);
    }

    SpinnerICS(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i);
        this.mTempRect = new Rect();
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.Spinner, i, 0);
        if (i2 == -1) {
            i2 = obtainStyledAttributes.getInt(7, 0);
        }
        switch (i2) {
            case 0:
                this.mPopup = new DialogPopup();
                break;
            case 1:
                SpinnerPopup dropdownPopup = new DropdownPopup(context, attributeSet, i);
                this.mDropDownWidth = obtainStyledAttributes.getLayoutDimension(3, -2);
                dropdownPopup.setBackgroundDrawable(obtainStyledAttributes.getDrawable(2));
                int dimensionPixelOffset = obtainStyledAttributes.getDimensionPixelOffset(5, 0);
                if (dimensionPixelOffset != 0) {
                    dropdownPopup.setVerticalOffset(dimensionPixelOffset);
                }
                dimensionPixelOffset = obtainStyledAttributes.getDimensionPixelOffset(4, 0);
                if (dimensionPixelOffset != 0) {
                    dropdownPopup.setHorizontalOffset(dimensionPixelOffset);
                }
                this.mPopup = dropdownPopup;
                break;
        }
        this.mGravity = obtainStyledAttributes.getInt(0, 17);
        this.mPopup.setPromptText(obtainStyledAttributes.getString(6));
        obtainStyledAttributes.recycle();
        if (this.mTempAdapter != null) {
            this.mPopup.setAdapter(this.mTempAdapter);
            this.mTempAdapter = null;
        }
    }

    private View makeAndAddView(int i) {
        View view;
        if (!this.mDataChanged) {
            view = this.mRecycler.get(i);
            if (view != null) {
                setUpChild(view);
                return view;
            }
        }
        view = this.mAdapter.getView(i, null, this);
        setUpChild(view);
        return view;
    }

    private void setUpChild(View view) {
        LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = generateDefaultLayoutParams();
        }
        addViewInLayout(view, 0, layoutParams);
        view.setSelected(hasFocus());
        view.measure(ViewGroup.getChildMeasureSpec(this.mWidthMeasureSpec, this.mSpinnerPadding.left + this.mSpinnerPadding.right, layoutParams.width), ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mSpinnerPadding.top + this.mSpinnerPadding.bottom, layoutParams.height));
        int measuredHeight = this.mSpinnerPadding.top + ((((getMeasuredHeight() - this.mSpinnerPadding.bottom) - this.mSpinnerPadding.top) - view.getMeasuredHeight()) / 2);
        view.layout(0, measuredHeight, view.getMeasuredWidth() + 0, view.getMeasuredHeight() + measuredHeight);
    }

    public int getBaseline() {
        View view = null;
        if (getChildCount() > 0) {
            view = getChildAt(0);
        } else if (this.mAdapter != null && this.mAdapter.getCount() > 0) {
            view = makeAndAddView(0);
            this.mRecycler.put(0, view);
            removeAllViewsInLayout();
        }
        if (view == null) {
            return -1;
        }
        int baseline = view.getBaseline();
        return baseline >= 0 ? view.getTop() + baseline : -1;
    }

    public CharSequence getPrompt() {
        return this.mPopup.getHintText();
    }

    void layout(int i, boolean z) {
        int i2 = this.mSpinnerPadding.left;
        int right = ((getRight() - getLeft()) - this.mSpinnerPadding.left) - this.mSpinnerPadding.right;
        if (this.mDataChanged) {
            handleDataChanged();
        }
        if (this.mItemCount == 0) {
            resetList();
            return;
        }
        if (this.mNextSelectedPosition >= 0) {
            setSelectedPositionInt(this.mNextSelectedPosition);
        }
        recycleAllViews();
        removeAllViewsInLayout();
        this.mFirstPosition = this.mSelectedPosition;
        View makeAndAddView = makeAndAddView(this.mSelectedPosition);
        int measuredWidth = makeAndAddView.getMeasuredWidth();
        switch (this.mGravity & 7) {
            case 1:
                i2 = (i2 + (right / 2)) - (measuredWidth / 2);
                break;
            case 5:
                i2 = (i2 + right) - measuredWidth;
                break;
        }
        makeAndAddView.offsetLeftAndRight(i2);
        this.mRecycler.clear();
        invalidate();
        checkSelectionChanged();
        this.mDataChanged = false;
        this.mNeedSync = false;
        setNextSelectedPositionInt(this.mSelectedPosition);
    }

    int measureContentWidth(SpinnerAdapter spinnerAdapter, Drawable drawable) {
        int i = 0;
        if (spinnerAdapter == null) {
            return 0;
        }
        int makeMeasureSpec = MeasureSpec.makeMeasureSpec(0, 0);
        int makeMeasureSpec2 = MeasureSpec.makeMeasureSpec(0, 0);
        int max = Math.max(0, getSelectedItemPosition());
        int min = Math.min(spinnerAdapter.getCount(), max + 15);
        int max2 = Math.max(0, max - (15 - (min - max)));
        int i2 = 0;
        View view = null;
        while (max2 < min) {
            View view2;
            max = spinnerAdapter.getItemViewType(max2);
            if (max != i2) {
                view2 = null;
            } else {
                max = i2;
                view2 = view;
            }
            view = spinnerAdapter.getView(max2, view2, this);
            if (view.getLayoutParams() == null) {
                view.setLayoutParams(new LayoutParams(-2, -2));
            }
            view.measure(makeMeasureSpec, makeMeasureSpec2);
            max2++;
            i = Math.max(i, view.getMeasuredWidth());
            i2 = max;
        }
        if (drawable == null) {
            return i;
        }
        drawable.getPadding(this.mTempRect);
        return i + (this.mTempRect.left + this.mTempRect.right);
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        setSelection(i);
        dialogInterface.dismiss();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mInLayout = true;
        layout(0, false);
        this.mInLayout = false;
    }

    protected void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        if (this.mPopup != null && MeasureSpec.getMode(i) == ExploreByTouchHelper.INVALID_ID) {
            setMeasuredDimension(Math.min(Math.max(getMeasuredWidth(), measureContentWidth(getAdapter(), getBackground())), MeasureSpec.getSize(i)), getMeasuredHeight());
        }
    }

    public boolean performClick() {
        boolean performClick = super.performClick();
        if (!performClick) {
            performClick = true;
            if (!this.mPopup.isShowing()) {
                this.mPopup.show();
            }
        }
        return performClick;
    }

    public void setAdapter(SpinnerAdapter spinnerAdapter) {
        super.setAdapter(spinnerAdapter);
        if (this.mPopup != null) {
            this.mPopup.setAdapter(new DropDownAdapter(spinnerAdapter));
        } else {
            this.mTempAdapter = new DropDownAdapter(spinnerAdapter);
        }
    }

    public void setGravity(int i) {
        if (this.mGravity != i) {
            if ((i & 7) == 0) {
                i |= 3;
            }
            this.mGravity = i;
            requestLayout();
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        throw new RuntimeException("setOnItemClickListener cannot be used with a spinner.");
    }

    void setOnItemClickListenerInt(OnItemClickListener onItemClickListener) {
        super.setOnItemClickListener(onItemClickListener);
    }

    public void setPrompt(CharSequence charSequence) {
        this.mPopup.setPromptText(charSequence);
    }

    public void setPromptId(int i) {
        setPrompt(getContext().getText(i));
    }
}
