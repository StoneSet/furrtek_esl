package android.support.v7.internal.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Adapter;
import android.widget.AdapterView;

abstract class AdapterViewICS<T extends Adapter> extends ViewGroup {
    public static final int INVALID_POSITION = -1;
    public static final long INVALID_ROW_ID = Long.MIN_VALUE;
    static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;
    static final int ITEM_VIEW_TYPE_IGNORE = -1;
    static final int SYNC_FIRST_POSITION = 1;
    static final int SYNC_MAX_DURATION_MILLIS = 100;
    static final int SYNC_SELECTED_POSITION = 0;
    boolean mBlockLayoutRequests = false;
    boolean mDataChanged;
    private boolean mDesiredFocusableInTouchModeState;
    private boolean mDesiredFocusableState;
    private View mEmptyView;
    @ExportedProperty(category = "scrolling")
    int mFirstPosition = 0;
    boolean mInLayout = false;
    @ExportedProperty(category = "list")
    int mItemCount;
    private int mLayoutHeight;
    boolean mNeedSync = false;
    @ExportedProperty(category = "list")
    int mNextSelectedPosition = -1;
    long mNextSelectedRowId = Long.MIN_VALUE;
    int mOldItemCount;
    int mOldSelectedPosition = -1;
    long mOldSelectedRowId = Long.MIN_VALUE;
    OnItemClickListener mOnItemClickListener;
    OnItemLongClickListener mOnItemLongClickListener;
    OnItemSelectedListener mOnItemSelectedListener;
    @ExportedProperty(category = "list")
    int mSelectedPosition = -1;
    long mSelectedRowId = Long.MIN_VALUE;
    private SelectionNotifier mSelectionNotifier;
    int mSpecificTop;
    long mSyncHeight;
    int mSyncMode;
    int mSyncPosition;
    long mSyncRowId = Long.MIN_VALUE;

    public interface OnItemSelectedListener {
        void onItemSelected(AdapterViewICS<?> adapterViewICS, View view, int i, long j);

        void onNothingSelected(AdapterViewICS<?> adapterViewICS);
    }

    public static class AdapterContextMenuInfo implements ContextMenuInfo {
        public long id;
        public int position;
        public View targetView;

        public AdapterContextMenuInfo(View view, int i, long j) {
            this.targetView = view;
            this.position = i;
            this.id = j;
        }
    }

    class AdapterDataSetObserver extends DataSetObserver {
        private Parcelable mInstanceState = null;

        AdapterDataSetObserver() {
        }

        public void clearSavedState() {
            this.mInstanceState = null;
        }

        public void onChanged() {
            AdapterViewICS.this.mDataChanged = true;
            AdapterViewICS.this.mOldItemCount = AdapterViewICS.this.mItemCount;
            AdapterViewICS.this.mItemCount = AdapterViewICS.this.getAdapter().getCount();
            if (!AdapterViewICS.this.getAdapter().hasStableIds() || this.mInstanceState == null || AdapterViewICS.this.mOldItemCount != 0 || AdapterViewICS.this.mItemCount <= 0) {
                AdapterViewICS.this.rememberSyncState();
            } else {
                AdapterViewICS.this.onRestoreInstanceState(this.mInstanceState);
                this.mInstanceState = null;
            }
            AdapterViewICS.this.checkFocus();
            AdapterViewICS.this.requestLayout();
        }

        public void onInvalidated() {
            AdapterViewICS.this.mDataChanged = true;
            if (AdapterViewICS.this.getAdapter().hasStableIds()) {
                this.mInstanceState = AdapterViewICS.this.onSaveInstanceState();
            }
            AdapterViewICS.this.mOldItemCount = AdapterViewICS.this.mItemCount;
            AdapterViewICS.this.mItemCount = 0;
            AdapterViewICS.this.mSelectedPosition = -1;
            AdapterViewICS.this.mSelectedRowId = Long.MIN_VALUE;
            AdapterViewICS.this.mNextSelectedPosition = -1;
            AdapterViewICS.this.mNextSelectedRowId = Long.MIN_VALUE;
            AdapterViewICS.this.mNeedSync = false;
            AdapterViewICS.this.checkFocus();
            AdapterViewICS.this.requestLayout();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(AdapterViewICS<?> adapterViewICS, View view, int i, long j);
    }

    class OnItemClickListenerWrapper implements android.widget.AdapterView.OnItemClickListener {
        private final OnItemClickListener mWrappedListener;

        public OnItemClickListenerWrapper(OnItemClickListener onItemClickListener) {
            this.mWrappedListener = onItemClickListener;
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            this.mWrappedListener.onItemClick(AdapterViewICS.this, view, i, j);
        }
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(AdapterViewICS<?> adapterViewICS, View view, int i, long j);
    }

    private class SelectionNotifier implements Runnable {
        private SelectionNotifier() {
        }

        public void run() {
            if (!AdapterViewICS.this.mDataChanged) {
                AdapterViewICS.this.fireOnSelected();
            } else if (AdapterViewICS.this.getAdapter() != null) {
                AdapterViewICS.this.post(this);
            }
        }
    }

    AdapterViewICS(Context context) {
        super(context);
    }

    AdapterViewICS(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    AdapterViewICS(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    private void fireOnSelected() {
        if (this.mOnItemSelectedListener != null) {
            int selectedItemPosition = getSelectedItemPosition();
            if (selectedItemPosition >= 0) {
                View selectedView = getSelectedView();
                this.mOnItemSelectedListener.onItemSelected(this, selectedView, selectedItemPosition, getAdapter().getItemId(selectedItemPosition));
                return;
            }
            this.mOnItemSelectedListener.onNothingSelected(this);
        }
    }

    private void updateEmptyStatus(boolean z) {
        if (isInFilterMode()) {
            z = false;
        }
        if (z) {
            if (this.mEmptyView != null) {
                this.mEmptyView.setVisibility(0);
                setVisibility(8);
            } else {
                setVisibility(0);
            }
            if (this.mDataChanged) {
                onLayout(false, getLeft(), getTop(), getRight(), getBottom());
                return;
            }
            return;
        }
        if (this.mEmptyView != null) {
            this.mEmptyView.setVisibility(8);
        }
        setVisibility(0);
    }

    public void addView(View view) {
        throw new UnsupportedOperationException("addView(View) is not supported in AdapterView");
    }

    public void addView(View view, int i) {
        throw new UnsupportedOperationException("addView(View, int) is not supported in AdapterView");
    }

    public void addView(View view, int i, LayoutParams layoutParams) {
        throw new UnsupportedOperationException("addView(View, int, LayoutParams) is not supported in AdapterView");
    }

    public void addView(View view, LayoutParams layoutParams) {
        throw new UnsupportedOperationException("addView(View, LayoutParams) is not supported in AdapterView");
    }

    protected boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    void checkFocus() {
        boolean z = false;
        Adapter adapter = getAdapter();
        boolean z2 = adapter == null || adapter.getCount() == 0;
        boolean z3 = !z2 || isInFilterMode();
        z2 = z3 && this.mDesiredFocusableInTouchModeState;
        super.setFocusableInTouchMode(z2);
        z2 = z3 && this.mDesiredFocusableState;
        super.setFocusable(z2);
        if (this.mEmptyView != null) {
            if (adapter == null || adapter.isEmpty()) {
                z = true;
            }
            updateEmptyStatus(z);
        }
    }

    void checkSelectionChanged() {
        if (this.mSelectedPosition != this.mOldSelectedPosition || this.mSelectedRowId != this.mOldSelectedRowId) {
            selectionChanged();
            this.mOldSelectedPosition = this.mSelectedPosition;
            this.mOldSelectedRowId = this.mSelectedRowId;
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        View selectedView = getSelectedView();
        return selectedView != null && selectedView.getVisibility() == 0 && selectedView.dispatchPopulateAccessibilityEvent(accessibilityEvent);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> sparseArray) {
        dispatchThawSelfOnly(sparseArray);
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> sparseArray) {
        dispatchFreezeSelfOnly(sparseArray);
    }

    int findSyncPosition() {
        int i = this.mItemCount;
        if (i == 0) {
            return -1;
        }
        long j = this.mSyncRowId;
        int i2 = this.mSyncPosition;
        if (j == Long.MIN_VALUE) {
            return -1;
        }
        int min = Math.min(i - 1, Math.max(0, i2));
        long uptimeMillis = 100 + SystemClock.uptimeMillis();
        Adapter adapter = getAdapter();
        if (adapter == null) {
            return -1;
        }
        int i3 = min;
        int i4 = min;
        i2 = min;
        Object obj = null;
        while (SystemClock.uptimeMillis() <= uptimeMillis) {
            if (adapter.getItemId(i2) != j) {
                Object obj2 = i3 == i + -1 ? 1 : null;
                Object obj3 = i4 == 0 ? 1 : null;
                if (obj2 != null && obj3 != null) {
                    break;
                } else if (obj3 != null || (r1 != null && obj2 == null)) {
                    min = i3 + 1;
                    i3 = min;
                    i2 = min;
                    obj = null;
                } else if (obj2 != null || (r1 == null && obj3 == null)) {
                    min = i4 - 1;
                    i4 = min;
                    i2 = min;
                    obj = 1;
                }
            } else {
                return i2;
            }
        }
        return -1;
    }

    public abstract T getAdapter();

    @CapturedViewProperty
    public int getCount() {
        return this.mItemCount;
    }

    public View getEmptyView() {
        return this.mEmptyView;
    }

    public int getFirstVisiblePosition() {
        return this.mFirstPosition;
    }

    public Object getItemAtPosition(int i) {
        Adapter adapter = getAdapter();
        return (adapter == null || i < 0) ? null : adapter.getItem(i);
    }

    public long getItemIdAtPosition(int i) {
        Adapter adapter = getAdapter();
        return (adapter == null || i < 0) ? Long.MIN_VALUE : adapter.getItemId(i);
    }

    public int getLastVisiblePosition() {
        return (this.mFirstPosition + getChildCount()) - 1;
    }

    public final OnItemClickListener getOnItemClickListener() {
        return this.mOnItemClickListener;
    }

    public final OnItemLongClickListener getOnItemLongClickListener() {
        return this.mOnItemLongClickListener;
    }

    public final OnItemSelectedListener getOnItemSelectedListener() {
        return this.mOnItemSelectedListener;
    }

    public int getPositionForView(View view) {
        while (true) {
            try {
                View view2 = (View) view.getParent();
                if (view2.equals(this)) {
                    break;
                }
                view = view2;
            } catch (ClassCastException e) {
            }
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).equals(view)) {
                return i + this.mFirstPosition;
            }
        }
        return -1;
    }

    public Object getSelectedItem() {
        Adapter adapter = getAdapter();
        int selectedItemPosition = getSelectedItemPosition();
        return (adapter == null || adapter.getCount() <= 0 || selectedItemPosition < 0) ? null : adapter.getItem(selectedItemPosition);
    }

    @CapturedViewProperty
    public long getSelectedItemId() {
        return this.mNextSelectedRowId;
    }

    @CapturedViewProperty
    public int getSelectedItemPosition() {
        return this.mNextSelectedPosition;
    }

    public abstract View getSelectedView();

    void handleDataChanged() {
        boolean z;
        int i = this.mItemCount;
        if (i > 0) {
            int findSyncPosition;
            boolean z2;
            if (this.mNeedSync) {
                this.mNeedSync = false;
                findSyncPosition = findSyncPosition();
                if (findSyncPosition >= 0 && lookForSelectablePosition(findSyncPosition, true) == findSyncPosition) {
                    setNextSelectedPositionInt(findSyncPosition);
                    z2 = true;
                    if (!z2) {
                        findSyncPosition = getSelectedItemPosition();
                        if (findSyncPosition >= i) {
                            findSyncPosition = i - 1;
                        }
                        if (findSyncPosition < 0) {
                            findSyncPosition = 0;
                        }
                        i = lookForSelectablePosition(findSyncPosition, true);
                        findSyncPosition = i >= 0 ? lookForSelectablePosition(findSyncPosition, false) : i;
                        if (findSyncPosition >= 0) {
                            setNextSelectedPositionInt(findSyncPosition);
                            checkSelectionChanged();
                            z = true;
                        }
                    }
                    z = z2;
                }
            }
            z2 = false;
            if (z2) {
                findSyncPosition = getSelectedItemPosition();
                if (findSyncPosition >= i) {
                    findSyncPosition = i - 1;
                }
                if (findSyncPosition < 0) {
                    findSyncPosition = 0;
                }
                i = lookForSelectablePosition(findSyncPosition, true);
                if (i >= 0) {
                }
                if (findSyncPosition >= 0) {
                    setNextSelectedPositionInt(findSyncPosition);
                    checkSelectionChanged();
                    z = true;
                }
            }
            z = z2;
        } else {
            z = false;
        }
        if (!z) {
            this.mSelectedPosition = -1;
            this.mSelectedRowId = Long.MIN_VALUE;
            this.mNextSelectedPosition = -1;
            this.mNextSelectedRowId = Long.MIN_VALUE;
            this.mNeedSync = false;
            checkSelectionChanged();
        }
    }

    boolean isInFilterMode() {
        return false;
    }

    int lookForSelectablePosition(int i, boolean z) {
        return i;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mSelectionNotifier);
    }

    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        this.mLayoutHeight = getHeight();
    }

    public boolean performItemClick(View view, int i, long j) {
        if (this.mOnItemClickListener == null) {
            return false;
        }
        playSoundEffect(0);
        if (view != null) {
            view.sendAccessibilityEvent(1);
        }
        this.mOnItemClickListener.onItemClick(this, view, i, j);
        return true;
    }

    void rememberSyncState() {
        if (getChildCount() > 0) {
            this.mNeedSync = true;
            this.mSyncHeight = (long) this.mLayoutHeight;
            View childAt;
            if (this.mSelectedPosition >= 0) {
                childAt = getChildAt(this.mSelectedPosition - this.mFirstPosition);
                this.mSyncRowId = this.mNextSelectedRowId;
                this.mSyncPosition = this.mNextSelectedPosition;
                if (childAt != null) {
                    this.mSpecificTop = childAt.getTop();
                }
                this.mSyncMode = 0;
                return;
            }
            childAt = getChildAt(0);
            Adapter adapter = getAdapter();
            if (this.mFirstPosition < 0 || this.mFirstPosition >= adapter.getCount()) {
                this.mSyncRowId = -1;
            } else {
                this.mSyncRowId = adapter.getItemId(this.mFirstPosition);
            }
            this.mSyncPosition = this.mFirstPosition;
            if (childAt != null) {
                this.mSpecificTop = childAt.getTop();
            }
            this.mSyncMode = 1;
        }
    }

    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in AdapterView");
    }

    public void removeView(View view) {
        throw new UnsupportedOperationException("removeView(View) is not supported in AdapterView");
    }

    public void removeViewAt(int i) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in AdapterView");
    }

    void selectionChanged() {
        if (this.mOnItemSelectedListener != null) {
            if (this.mInLayout || this.mBlockLayoutRequests) {
                if (this.mSelectionNotifier == null) {
                    this.mSelectionNotifier = new SelectionNotifier();
                }
                post(this.mSelectionNotifier);
            } else {
                fireOnSelected();
            }
        }
        if (this.mSelectedPosition != -1 && isShown() && !isInTouchMode()) {
            sendAccessibilityEvent(4);
        }
    }

    public abstract void setAdapter(T t);

    public void setEmptyView(View view) {
        this.mEmptyView = view;
        Adapter adapter = getAdapter();
        boolean z = adapter == null || adapter.isEmpty();
        updateEmptyStatus(z);
    }

    public void setFocusable(boolean z) {
        boolean z2 = true;
        Adapter adapter = getAdapter();
        boolean z3 = adapter == null || adapter.getCount() == 0;
        this.mDesiredFocusableState = z;
        if (!z) {
            this.mDesiredFocusableInTouchModeState = false;
        }
        if (!z || (z3 && !isInFilterMode())) {
            z2 = false;
        }
        super.setFocusable(z2);
    }

    public void setFocusableInTouchMode(boolean z) {
        boolean z2 = true;
        Adapter adapter = getAdapter();
        boolean z3 = adapter == null || adapter.getCount() == 0;
        this.mDesiredFocusableInTouchModeState = z;
        if (z) {
            this.mDesiredFocusableState = true;
        }
        if (!z || (z3 && !isInFilterMode())) {
            z2 = false;
        }
        super.setFocusableInTouchMode(z2);
    }

    void setNextSelectedPositionInt(int i) {
        this.mNextSelectedPosition = i;
        this.mNextSelectedRowId = getItemIdAtPosition(i);
        if (this.mNeedSync && this.mSyncMode == 0 && i >= 0) {
            this.mSyncPosition = i;
            this.mSyncRowId = this.mNextSelectedRowId;
        }
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        throw new RuntimeException("Don't call setOnClickListener for an AdapterView. You probably want setOnItemClickListener instead");
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        this.mOnItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.mOnItemSelectedListener = onItemSelectedListener;
    }

    void setSelectedPositionInt(int i) {
        this.mSelectedPosition = i;
        this.mSelectedRowId = getItemIdAtPosition(i);
    }

    public abstract void setSelection(int i);
}
