package android.support.v7.internal.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.menu.ActionMenuItem;
import android.support.v7.internal.view.menu.ActionMenuPresenter;
import android.support.v7.internal.view.menu.ActionMenuView;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.internal.view.menu.MenuPresenter;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.internal.view.menu.SubMenuBuilder;
import android.support.v7.internal.widget.AdapterViewICS.OnItemSelectedListener;
import android.support.v7.view.CollapsibleActionView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window.Callback;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class ActionBarView extends AbsActionBarView {
    private static final int DEFAULT_CUSTOM_GRAVITY = 19;
    public static final int DISPLAY_DEFAULT = 0;
    private static final int DISPLAY_RELAYOUT_MASK = 31;
    private static final String TAG = "ActionBarView";
    private OnNavigationListener mCallback;
    private Context mContext;
    private ActionBarContextView mContextView;
    private View mCustomNavView;
    private int mDisplayOptions = -1;
    View mExpandedActionView;
    private final OnClickListener mExpandedActionViewUpListener = new OnClickListener() {
        public void onClick(View view) {
            MenuItemImpl menuItemImpl = ActionBarView.this.mExpandedMenuPresenter.mCurrentExpandedItem;
            if (menuItemImpl != null) {
                menuItemImpl.collapseActionView();
            }
        }
    };
    private HomeView mExpandedHomeLayout;
    private ExpandedActionViewMenuPresenter mExpandedMenuPresenter;
    private HomeView mHomeLayout;
    private Drawable mIcon;
    private boolean mIncludeTabs;
    private int mIndeterminateProgressStyle;
    private ProgressBarICS mIndeterminateProgressView;
    private boolean mIsCollapsable;
    private boolean mIsCollapsed;
    private int mItemPadding;
    private LinearLayout mListNavLayout;
    private Drawable mLogo;
    private ActionMenuItem mLogoNavItem;
    private final OnItemSelectedListener mNavItemSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterViewICS<?> adapterViewICS, View view, int i, long j) {
            if (ActionBarView.this.mCallback != null) {
                ActionBarView.this.mCallback.onNavigationItemSelected(i, j);
            }
        }

        public void onNothingSelected(AdapterViewICS<?> adapterViewICS) {
        }
    };
    private int mNavigationMode;
    private MenuBuilder mOptionsMenu;
    private int mProgressBarPadding;
    private int mProgressStyle;
    private ProgressBarICS mProgressView;
    private SpinnerICS mSpinner;
    private SpinnerAdapter mSpinnerAdapter;
    private CharSequence mSubtitle;
    private int mSubtitleStyleRes;
    private TextView mSubtitleView;
    private ScrollingTabContainerView mTabScrollView;
    private Runnable mTabSelector;
    private CharSequence mTitle;
    private LinearLayout mTitleLayout;
    private int mTitleStyleRes;
    private View mTitleUpView;
    private TextView mTitleView;
    private final OnClickListener mUpClickListener = new OnClickListener() {
        public void onClick(View view) {
            ActionBarView.this.mWindowCallback.onMenuItemSelected(0, ActionBarView.this.mLogoNavItem);
        }
    };
    private boolean mUserTitle;
    Callback mWindowCallback;

    private class ExpandedActionViewMenuPresenter implements MenuPresenter {
        MenuItemImpl mCurrentExpandedItem;
        MenuBuilder mMenu;

        private ExpandedActionViewMenuPresenter() {
        }

        public boolean collapseItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            if (ActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) ActionBarView.this.mExpandedActionView).onActionViewCollapsed();
            }
            ActionBarView.this.removeView(ActionBarView.this.mExpandedActionView);
            ActionBarView.this.removeView(ActionBarView.this.mExpandedHomeLayout);
            ActionBarView.this.mExpandedActionView = null;
            if ((ActionBarView.this.mDisplayOptions & 2) != 0) {
                ActionBarView.this.mHomeLayout.setVisibility(0);
            }
            if ((ActionBarView.this.mDisplayOptions & 8) != 0) {
                if (ActionBarView.this.mTitleLayout == null) {
                    ActionBarView.this.initTitle();
                } else {
                    ActionBarView.this.mTitleLayout.setVisibility(0);
                }
            }
            if (ActionBarView.this.mTabScrollView != null && ActionBarView.this.mNavigationMode == 2) {
                ActionBarView.this.mTabScrollView.setVisibility(0);
            }
            if (ActionBarView.this.mSpinner != null && ActionBarView.this.mNavigationMode == 1) {
                ActionBarView.this.mSpinner.setVisibility(0);
            }
            if (!(ActionBarView.this.mCustomNavView == null || (ActionBarView.this.mDisplayOptions & 16) == 0)) {
                ActionBarView.this.mCustomNavView.setVisibility(0);
            }
            ActionBarView.this.mExpandedHomeLayout.setIcon(null);
            this.mCurrentExpandedItem = null;
            ActionBarView.this.requestLayout();
            menuItemImpl.setActionViewExpanded(false);
            return true;
        }

        public boolean expandItemActionView(MenuBuilder menuBuilder, MenuItemImpl menuItemImpl) {
            ActionBarView.this.mExpandedActionView = menuItemImpl.getActionView();
            ActionBarView.this.mExpandedHomeLayout.setIcon(ActionBarView.this.mIcon.getConstantState().newDrawable(ActionBarView.this.getResources()));
            this.mCurrentExpandedItem = menuItemImpl;
            if (ActionBarView.this.mExpandedActionView.getParent() != ActionBarView.this) {
                ActionBarView.this.addView(ActionBarView.this.mExpandedActionView);
            }
            if (ActionBarView.this.mExpandedHomeLayout.getParent() != ActionBarView.this) {
                ActionBarView.this.addView(ActionBarView.this.mExpandedHomeLayout);
            }
            ActionBarView.this.mHomeLayout.setVisibility(8);
            if (ActionBarView.this.mTitleLayout != null) {
                ActionBarView.this.mTitleLayout.setVisibility(8);
            }
            if (ActionBarView.this.mTabScrollView != null) {
                ActionBarView.this.mTabScrollView.setVisibility(8);
            }
            if (ActionBarView.this.mSpinner != null) {
                ActionBarView.this.mSpinner.setVisibility(8);
            }
            if (ActionBarView.this.mCustomNavView != null) {
                ActionBarView.this.mCustomNavView.setVisibility(8);
            }
            ActionBarView.this.requestLayout();
            menuItemImpl.setActionViewExpanded(true);
            if (ActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) ActionBarView.this.mExpandedActionView).onActionViewExpanded();
            }
            return true;
        }

        public boolean flagActionItems() {
            return false;
        }

        public int getId() {
            return 0;
        }

        public MenuView getMenuView(ViewGroup viewGroup) {
            return null;
        }

        public void initForMenu(Context context, MenuBuilder menuBuilder) {
            if (!(this.mMenu == null || this.mCurrentExpandedItem == null)) {
                this.mMenu.collapseItemActionView(this.mCurrentExpandedItem);
            }
            this.mMenu = menuBuilder;
        }

        public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
        }

        public void onRestoreInstanceState(Parcelable parcelable) {
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenuBuilder) {
            return false;
        }

        public void setCallback(MenuPresenter.Callback callback) {
        }

        public void updateMenuView(boolean z) {
            if (this.mCurrentExpandedItem != null) {
                Object obj;
                if (this.mMenu != null) {
                    int size = this.mMenu.size();
                    for (int i = 0; i < size; i++) {
                        if (((SupportMenuItem) this.mMenu.getItem(i)) == this.mCurrentExpandedItem) {
                            obj = 1;
                            break;
                        }
                    }
                }
                obj = null;
                if (obj == null) {
                    collapseItemActionView(this.mMenu, this.mCurrentExpandedItem);
                }
            }
        }
    }

    private static class HomeView extends FrameLayout {
        private Drawable mDefaultUpIndicator;
        private ImageView mIconView;
        private int mUpIndicatorRes;
        private ImageView mUpView;
        private int mUpWidth;

        public HomeView(Context context) {
            this(context, null);
        }

        public HomeView(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            CharSequence contentDescription = getContentDescription();
            if (!TextUtils.isEmpty(contentDescription)) {
                accessibilityEvent.getText().add(contentDescription);
            }
            return true;
        }

        public int getLeftOffset() {
            return this.mUpView.getVisibility() == 8 ? this.mUpWidth : 0;
        }

        protected void onConfigurationChanged(Configuration configuration) {
            super.onConfigurationChanged(configuration);
            if (this.mUpIndicatorRes != 0) {
                setUpIndicator(this.mUpIndicatorRes);
            }
        }

        protected void onFinishInflate() {
            this.mUpView = (ImageView) findViewById(R.id.up);
            this.mIconView = (ImageView) findViewById(R.id.home);
            this.mDefaultUpIndicator = this.mUpView.getDrawable();
        }

        protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
            LayoutParams layoutParams;
            int measuredHeight;
            int i5;
            int i6 = 0;
            int i7 = (i4 - i2) / 2;
            int i8 = i3 - i;
            if (this.mUpView.getVisibility() != 8) {
                layoutParams = (LayoutParams) this.mUpView.getLayoutParams();
                measuredHeight = this.mUpView.getMeasuredHeight();
                int measuredWidth = this.mUpView.getMeasuredWidth();
                int i9 = i7 - (measuredHeight / 2);
                this.mUpView.layout(0, i9, measuredWidth, measuredHeight + i9);
                i5 = layoutParams.rightMargin + (layoutParams.leftMargin + measuredWidth);
                i6 = i8 - i5;
                i += i5;
                i6 = i5;
            }
            layoutParams = (LayoutParams) this.mIconView.getLayoutParams();
            i8 = this.mIconView.getMeasuredHeight();
            measuredHeight = this.mIconView.getMeasuredWidth();
            i6 += Math.max(layoutParams.leftMargin, ((i3 - i) / 2) - (measuredHeight / 2));
            i5 = Math.max(layoutParams.topMargin, i7 - (i8 / 2));
            this.mIconView.layout(i6, i5, measuredHeight + i6, i8 + i5);
        }

        protected void onMeasure(int i, int i2) {
            measureChildWithMargins(this.mUpView, i, 0, i2, 0);
            LayoutParams layoutParams = (LayoutParams) this.mUpView.getLayoutParams();
            this.mUpWidth = (layoutParams.leftMargin + this.mUpView.getMeasuredWidth()) + layoutParams.rightMargin;
            int i3 = this.mUpView.getVisibility() == 8 ? 0 : this.mUpWidth;
            int measuredHeight = (layoutParams.topMargin + this.mUpView.getMeasuredHeight()) + layoutParams.bottomMargin;
            measureChildWithMargins(this.mIconView, i, i3, i2, 0);
            layoutParams = (LayoutParams) this.mIconView.getLayoutParams();
            int measuredWidth = i3 + ((layoutParams.leftMargin + this.mIconView.getMeasuredWidth()) + layoutParams.rightMargin);
            measuredHeight = Math.max(measuredHeight, layoutParams.bottomMargin + (layoutParams.topMargin + this.mIconView.getMeasuredHeight()));
            int mode = MeasureSpec.getMode(i);
            int mode2 = MeasureSpec.getMode(i2);
            int size = MeasureSpec.getSize(i);
            int size2 = MeasureSpec.getSize(i2);
            switch (mode) {
                case ExploreByTouchHelper.INVALID_ID /*-2147483648*/:
                    size = Math.min(measuredWidth, size);
                    break;
                case 1073741824:
                    break;
                default:
                    size = measuredWidth;
                    break;
            }
            switch (mode2) {
                case ExploreByTouchHelper.INVALID_ID /*-2147483648*/:
                    size2 = Math.min(measuredHeight, size2);
                    break;
                case 1073741824:
                    break;
                default:
                    size2 = measuredHeight;
                    break;
            }
            setMeasuredDimension(size, size2);
        }

        public void setIcon(Drawable drawable) {
            this.mIconView.setImageDrawable(drawable);
        }

        public void setUp(boolean z) {
            this.mUpView.setVisibility(z ? 0 : 8);
        }

        public void setUpIndicator(int i) {
            this.mUpIndicatorRes = i;
            this.mUpView.setImageDrawable(i != 0 ? getResources().getDrawable(i) : this.mDefaultUpIndicator);
        }

        public void setUpIndicator(Drawable drawable) {
            ImageView imageView = this.mUpView;
            if (drawable == null) {
                drawable = this.mDefaultUpIndicator;
            }
            imageView.setImageDrawable(drawable);
            this.mUpIndicatorRes = 0;
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
        int expandedMenuItemId;
        boolean isOverflowOpen;

        private SavedState(Parcel parcel) {
            super(parcel);
            this.expandedMenuItemId = parcel.readInt();
            this.isOverflowOpen = parcel.readInt() != 0;
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.expandedMenuItemId);
            parcel.writeInt(this.isOverflowOpen ? 1 : 0);
        }
    }

    public ActionBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        setBackgroundResource(0);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ActionBar, R.attr.actionBarStyle, 0);
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        PackageManager packageManager = context.getPackageManager();
        this.mNavigationMode = obtainStyledAttributes.getInt(2, 0);
        this.mTitle = obtainStyledAttributes.getText(1);
        this.mSubtitle = obtainStyledAttributes.getText(4);
        this.mLogo = obtainStyledAttributes.getDrawable(8);
        if (this.mLogo == null && VERSION.SDK_INT >= 9) {
            if (context instanceof Activity) {
                try {
                    this.mLogo = packageManager.getActivityLogo(((Activity) context).getComponentName());
                } catch (Throwable e) {
                    Log.e(TAG, "Activity component name not found!", e);
                }
            }
            if (this.mLogo == null) {
                this.mLogo = applicationInfo.loadLogo(packageManager);
            }
        }
        this.mIcon = obtainStyledAttributes.getDrawable(7);
        if (this.mIcon == null) {
            if (context instanceof Activity) {
                try {
                    this.mIcon = packageManager.getActivityIcon(((Activity) context).getComponentName());
                } catch (Throwable e2) {
                    Log.e(TAG, "Activity component name not found!", e2);
                }
            }
            if (this.mIcon == null) {
                this.mIcon = applicationInfo.loadIcon(packageManager);
            }
        }
        LayoutInflater from = LayoutInflater.from(context);
        int resourceId = obtainStyledAttributes.getResourceId(14, R.layout.abc_action_bar_home);
        this.mHomeLayout = (HomeView) from.inflate(resourceId, this, false);
        this.mExpandedHomeLayout = (HomeView) from.inflate(resourceId, this, false);
        this.mExpandedHomeLayout.setUp(true);
        this.mExpandedHomeLayout.setOnClickListener(this.mExpandedActionViewUpListener);
        this.mExpandedHomeLayout.setContentDescription(getResources().getText(R.string.abc_action_bar_up_description));
        this.mTitleStyleRes = obtainStyledAttributes.getResourceId(5, 0);
        this.mSubtitleStyleRes = obtainStyledAttributes.getResourceId(6, 0);
        this.mProgressStyle = obtainStyledAttributes.getResourceId(15, 0);
        this.mIndeterminateProgressStyle = obtainStyledAttributes.getResourceId(16, 0);
        this.mProgressBarPadding = obtainStyledAttributes.getDimensionPixelOffset(17, 0);
        this.mItemPadding = obtainStyledAttributes.getDimensionPixelOffset(18, 0);
        setDisplayOptions(obtainStyledAttributes.getInt(3, 0));
        int resourceId2 = obtainStyledAttributes.getResourceId(13, 0);
        if (resourceId2 != 0) {
            this.mCustomNavView = from.inflate(resourceId2, this, false);
            this.mNavigationMode = 0;
            setDisplayOptions(this.mDisplayOptions | 16);
        }
        this.mContentHeight = obtainStyledAttributes.getLayoutDimension(0, 0);
        obtainStyledAttributes.recycle();
        this.mLogoNavItem = new ActionMenuItem(context, 0, 16908332, 0, 0, this.mTitle);
        this.mHomeLayout.setOnClickListener(this.mUpClickListener);
        this.mHomeLayout.setClickable(true);
        this.mHomeLayout.setFocusable(true);
    }

    private void configPresenters(MenuBuilder menuBuilder) {
        if (menuBuilder != null) {
            menuBuilder.addMenuPresenter(this.mActionMenuPresenter);
            menuBuilder.addMenuPresenter(this.mExpandedMenuPresenter);
        } else {
            this.mActionMenuPresenter.initForMenu(this.mContext, null);
            this.mExpandedMenuPresenter.initForMenu(this.mContext, null);
        }
        this.mActionMenuPresenter.updateMenuView(true);
        this.mExpandedMenuPresenter.updateMenuView(true);
    }

    private void initTitle() {
        boolean z = true;
        if (this.mTitleLayout == null) {
            this.mTitleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.abc_action_bar_title_item, this, false);
            this.mTitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_title);
            this.mSubtitleView = (TextView) this.mTitleLayout.findViewById(R.id.action_bar_subtitle);
            this.mTitleUpView = this.mTitleLayout.findViewById(R.id.up);
            this.mTitleLayout.setOnClickListener(this.mUpClickListener);
            if (this.mTitleStyleRes != 0) {
                this.mTitleView.setTextAppearance(this.mContext, this.mTitleStyleRes);
            }
            if (this.mTitle != null) {
                this.mTitleView.setText(this.mTitle);
            }
            if (this.mSubtitleStyleRes != 0) {
                this.mSubtitleView.setTextAppearance(this.mContext, this.mSubtitleStyleRes);
            }
            if (this.mSubtitle != null) {
                this.mSubtitleView.setText(this.mSubtitle);
                this.mSubtitleView.setVisibility(0);
            }
            boolean z2 = (this.mDisplayOptions & 4) != 0;
            boolean z3 = (this.mDisplayOptions & 2) != 0;
            View view = this.mTitleUpView;
            int i = !z3 ? z2 ? 0 : 4 : 8;
            view.setVisibility(i);
            LinearLayout linearLayout = this.mTitleLayout;
            if (!z2 || z3) {
                z = false;
            }
            linearLayout.setEnabled(z);
        }
        addView(this.mTitleLayout);
        if (this.mExpandedActionView != null || (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle))) {
            this.mTitleLayout.setVisibility(8);
        }
    }

    private void setTitleImpl(CharSequence charSequence) {
        int i = 0;
        this.mTitle = charSequence;
        if (this.mTitleView != null) {
            this.mTitleView.setText(charSequence);
            int i2 = (this.mExpandedActionView != null || (this.mDisplayOptions & 8) == 0 || (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle))) ? 0 : 1;
            LinearLayout linearLayout = this.mTitleLayout;
            if (i2 == 0) {
                i = 8;
            }
            linearLayout.setVisibility(i);
        }
        if (this.mLogoNavItem != null) {
            this.mLogoNavItem.setTitle(charSequence);
        }
    }

    public /* bridge */ /* synthetic */ void animateToVisibility(int i) {
        super.animateToVisibility(i);
    }

    public void collapseActionView() {
        MenuItemImpl menuItemImpl = this.mExpandedMenuPresenter == null ? null : this.mExpandedMenuPresenter.mCurrentExpandedItem;
        if (menuItemImpl != null) {
            menuItemImpl.collapseActionView();
        }
    }

    public /* bridge */ /* synthetic */ void dismissPopupMenus() {
        super.dismissPopupMenus();
    }

    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ActionBar.LayoutParams(19);
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new ActionBar.LayoutParams(getContext(), attributeSet);
    }

    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return layoutParams == null ? generateDefaultLayoutParams() : layoutParams;
    }

    public /* bridge */ /* synthetic */ int getAnimatedVisibility() {
        return super.getAnimatedVisibility();
    }

    public /* bridge */ /* synthetic */ int getContentHeight() {
        return super.getContentHeight();
    }

    public View getCustomNavigationView() {
        return this.mCustomNavView;
    }

    public int getDisplayOptions() {
        return this.mDisplayOptions;
    }

    public SpinnerAdapter getDropdownAdapter() {
        return this.mSpinnerAdapter;
    }

    public int getDropdownSelectedPosition() {
        return this.mSpinner.getSelectedItemPosition();
    }

    public int getNavigationMode() {
        return this.mNavigationMode;
    }

    public CharSequence getSubtitle() {
        return this.mSubtitle;
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public boolean hasEmbeddedTabs() {
        return this.mIncludeTabs;
    }

    public boolean hasExpandedActionView() {
        return (this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null) ? false : true;
    }

    public /* bridge */ /* synthetic */ boolean hideOverflowMenu() {
        return super.hideOverflowMenu();
    }

    public void initIndeterminateProgress() {
        this.mIndeterminateProgressView = new ProgressBarICS(this.mContext, null, 0, this.mIndeterminateProgressStyle);
        this.mIndeterminateProgressView.setId(R.id.progress_circular);
        this.mIndeterminateProgressView.setVisibility(8);
        addView(this.mIndeterminateProgressView);
    }

    public void initProgress() {
        this.mProgressView = new ProgressBarICS(this.mContext, null, 0, this.mProgressStyle);
        this.mProgressView.setId(R.id.progress_horizontal);
        this.mProgressView.setMax(10000);
        this.mProgressView.setVisibility(8);
        addView(this.mProgressView);
    }

    public boolean isCollapsed() {
        return this.mIsCollapsed;
    }

    public /* bridge */ /* synthetic */ boolean isOverflowMenuShowing() {
        return super.isOverflowMenuShowing();
    }

    public /* bridge */ /* synthetic */ boolean isOverflowReserved() {
        return super.isOverflowReserved();
    }

    public boolean isSplitActionBar() {
        return this.mSplitActionBar;
    }

    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mTitleView = null;
        this.mSubtitleView = null;
        this.mTitleUpView = null;
        if (this.mTitleLayout != null && this.mTitleLayout.getParent() == this) {
            removeView(this.mTitleLayout);
        }
        this.mTitleLayout = null;
        if ((this.mDisplayOptions & 8) != 0) {
            initTitle();
        }
        if (this.mTabScrollView != null && this.mIncludeTabs) {
            ViewGroup.LayoutParams layoutParams = this.mTabScrollView.getLayoutParams();
            if (layoutParams != null) {
                layoutParams.width = -2;
                layoutParams.height = -1;
            }
            this.mTabScrollView.setAllowCollapse(true);
        }
        if (this.mProgressView != null) {
            removeView(this.mProgressView);
            initProgress();
        }
        if (this.mIndeterminateProgressView != null) {
            removeView(this.mIndeterminateProgressView);
            initIndeterminateProgress();
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(this.mTabSelector);
        if (this.mActionMenuPresenter != null) {
            this.mActionMenuPresenter.hideOverflowMenu();
            this.mActionMenuPresenter.hideSubMenus();
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(this.mHomeLayout);
        if (this.mCustomNavView != null && (this.mDisplayOptions & 16) != 0) {
            ActionBarView parent = this.mCustomNavView.getParent();
            if (parent != this) {
                if (parent instanceof ViewGroup) {
                    parent.removeView(this.mCustomNavView);
                }
                addView(this.mCustomNavView);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onLayout(boolean r10, int r11, int r12, int r13, int r14) {
        /*
        r9 = this;
        r1 = r9.getPaddingLeft();
        r2 = r9.getPaddingTop();
        r0 = r14 - r12;
        r3 = r9.getPaddingTop();
        r0 = r0 - r3;
        r3 = r9.getPaddingBottom();
        r3 = r0 - r3;
        if (r3 > 0) goto L_0x0018;
    L_0x0017:
        return;
    L_0x0018:
        r0 = r9.mExpandedActionView;
        if (r0 == 0) goto L_0x0119;
    L_0x001c:
        r0 = r9.mExpandedHomeLayout;
    L_0x001e:
        r4 = r0.getVisibility();
        r5 = 8;
        if (r4 == r5) goto L_0x01c4;
    L_0x0026:
        r4 = r0.getLeftOffset();
        r5 = r1 + r4;
        r0 = r9.positionChild(r0, r5, r2, r3);
        r0 = r0 + r4;
        r0 = r0 + r1;
    L_0x0032:
        r1 = r9.mExpandedActionView;
        if (r1 != 0) goto L_0x0059;
    L_0x0036:
        r1 = r9.mTitleLayout;
        if (r1 == 0) goto L_0x011d;
    L_0x003a:
        r1 = r9.mTitleLayout;
        r1 = r1.getVisibility();
        r4 = 8;
        if (r1 == r4) goto L_0x011d;
    L_0x0044:
        r1 = r9.mDisplayOptions;
        r1 = r1 & 8;
        if (r1 == 0) goto L_0x011d;
    L_0x004a:
        r1 = 1;
    L_0x004b:
        if (r1 == 0) goto L_0x0054;
    L_0x004d:
        r4 = r9.mTitleLayout;
        r4 = r9.positionChild(r4, r0, r2, r3);
        r0 = r0 + r4;
    L_0x0054:
        r4 = r9.mNavigationMode;
        switch(r4) {
            case 1: goto L_0x0120;
            case 2: goto L_0x0136;
            default: goto L_0x0059;
        };
    L_0x0059:
        r1 = r0;
    L_0x005a:
        r0 = r13 - r11;
        r4 = r9.getPaddingRight();
        r0 = r0 - r4;
        r4 = r9.mMenuView;
        if (r4 == 0) goto L_0x0079;
    L_0x0065:
        r4 = r9.mMenuView;
        r4 = r4.getParent();
        if (r4 != r9) goto L_0x0079;
    L_0x006d:
        r4 = r9.mMenuView;
        r9.positionChildInverse(r4, r0, r2, r3);
        r4 = r9.mMenuView;
        r4 = r4.getMeasuredWidth();
        r0 = r0 - r4;
    L_0x0079:
        r4 = r9.mIndeterminateProgressView;
        if (r4 == 0) goto L_0x01c1;
    L_0x007d:
        r4 = r9.mIndeterminateProgressView;
        r4 = r4.getVisibility();
        r5 = 8;
        if (r4 == r5) goto L_0x01c1;
    L_0x0087:
        r4 = r9.mIndeterminateProgressView;
        r9.positionChildInverse(r4, r0, r2, r3);
        r2 = r9.mIndeterminateProgressView;
        r2 = r2.getMeasuredWidth();
        r0 = r0 - r2;
        r2 = r0;
    L_0x0094:
        r0 = r9.mExpandedActionView;
        if (r0 == 0) goto L_0x014c;
    L_0x0098:
        r0 = r9.mExpandedActionView;
        r7 = r0;
    L_0x009b:
        if (r7 == 0) goto L_0x00f5;
    L_0x009d:
        r0 = r7.getLayoutParams();
        r3 = r0 instanceof android.support.v7.app.ActionBar.LayoutParams;
        if (r3 == 0) goto L_0x015d;
    L_0x00a5:
        r0 = (android.support.v7.app.ActionBar.LayoutParams) r0;
        r5 = r0;
    L_0x00a8:
        if (r5 == 0) goto L_0x0161;
    L_0x00aa:
        r0 = r5.gravity;
    L_0x00ac:
        r8 = r7.getMeasuredWidth();
        r4 = 0;
        r3 = 0;
        if (r5 == 0) goto L_0x01bb;
    L_0x00b4:
        r3 = r5.leftMargin;
        r4 = r1 + r3;
        r1 = r5.rightMargin;
        r3 = r2 - r1;
        r1 = r5.topMargin;
        r2 = r5.bottomMargin;
        r5 = r2;
        r6 = r3;
        r3 = r4;
        r4 = r1;
    L_0x00c4:
        r1 = r0 & 7;
        r2 = 1;
        if (r1 != r2) goto L_0x016c;
    L_0x00c9:
        r2 = r9.getWidth();
        r2 = r2 - r8;
        r2 = r2 / 2;
        if (r2 >= r3) goto L_0x0165;
    L_0x00d2:
        r1 = 3;
        r2 = r1;
    L_0x00d4:
        r1 = 0;
        switch(r2) {
            case 1: goto L_0x0173;
            case 2: goto L_0x00d8;
            case 3: goto L_0x017d;
            case 4: goto L_0x00d8;
            case 5: goto L_0x0180;
            default: goto L_0x00d8;
        };
    L_0x00d8:
        r2 = r1;
    L_0x00d9:
        r1 = r0 & 112;
        r6 = -1;
        if (r0 != r6) goto L_0x00e1;
    L_0x00de:
        r0 = 16;
        r1 = r0;
    L_0x00e1:
        r0 = 0;
        switch(r1) {
            case 16: goto L_0x0185;
            case 48: goto L_0x019d;
            case 80: goto L_0x01a4;
            default: goto L_0x00e5;
        };
    L_0x00e5:
        r1 = r7.getMeasuredWidth();
        r4 = r2 + r1;
        r5 = r7.getMeasuredHeight();
        r5 = r5 + r0;
        r7.layout(r2, r0, r4, r5);
        r0 = r3 + r1;
    L_0x00f5:
        r0 = r9.mProgressView;
        if (r0 == 0) goto L_0x0017;
    L_0x00f9:
        r0 = r9.mProgressView;
        r0.bringToFront();
        r0 = r9.mProgressView;
        r0 = r0.getMeasuredHeight();
        r0 = r0 / 2;
        r1 = r9.mProgressView;
        r2 = r9.mProgressBarPadding;
        r3 = -r0;
        r4 = r9.mProgressBarPadding;
        r5 = r9.mProgressView;
        r5 = r5.getMeasuredWidth();
        r4 = r4 + r5;
        r1.layout(r2, r3, r4, r0);
        goto L_0x0017;
    L_0x0119:
        r0 = r9.mHomeLayout;
        goto L_0x001e;
    L_0x011d:
        r1 = 0;
        goto L_0x004b;
    L_0x0120:
        r4 = r9.mListNavLayout;
        if (r4 == 0) goto L_0x0059;
    L_0x0124:
        if (r1 == 0) goto L_0x0129;
    L_0x0126:
        r1 = r9.mItemPadding;
        r0 = r0 + r1;
    L_0x0129:
        r1 = r9.mListNavLayout;
        r1 = r9.positionChild(r1, r0, r2, r3);
        r4 = r9.mItemPadding;
        r1 = r1 + r4;
        r0 = r0 + r1;
        r1 = r0;
        goto L_0x005a;
    L_0x0136:
        r4 = r9.mTabScrollView;
        if (r4 == 0) goto L_0x0059;
    L_0x013a:
        if (r1 == 0) goto L_0x013f;
    L_0x013c:
        r1 = r9.mItemPadding;
        r0 = r0 + r1;
    L_0x013f:
        r1 = r9.mTabScrollView;
        r1 = r9.positionChild(r1, r0, r2, r3);
        r4 = r9.mItemPadding;
        r1 = r1 + r4;
        r0 = r0 + r1;
        r1 = r0;
        goto L_0x005a;
    L_0x014c:
        r0 = r9.mDisplayOptions;
        r3 = r0 & 16;
        r0 = 0;
        if (r3 == 0) goto L_0x01b8;
    L_0x0153:
        r3 = r9.mCustomNavView;
        r0 = 0;
        if (r3 == 0) goto L_0x01b8;
    L_0x0158:
        r0 = r9.mCustomNavView;
        r7 = r0;
        goto L_0x009b;
    L_0x015d:
        r0 = 0;
        r5 = r0;
        goto L_0x00a8;
    L_0x0161:
        r0 = 19;
        goto L_0x00ac;
    L_0x0165:
        r2 = r2 + r8;
        if (r2 <= r6) goto L_0x01b5;
    L_0x0168:
        r1 = 5;
        r2 = r1;
        goto L_0x00d4;
    L_0x016c:
        r2 = -1;
        if (r0 != r2) goto L_0x01b5;
    L_0x016f:
        r1 = 3;
        r2 = r1;
        goto L_0x00d4;
    L_0x0173:
        r1 = r9.getWidth();
        r1 = r1 - r8;
        r1 = r1 / 2;
        r2 = r1;
        goto L_0x00d9;
    L_0x017d:
        r2 = r3;
        goto L_0x00d9;
    L_0x0180:
        r1 = r6 - r8;
        r2 = r1;
        goto L_0x00d9;
    L_0x0185:
        r0 = r9.getPaddingTop();
        r1 = r9.getHeight();
        r4 = r9.getPaddingBottom();
        r1 = r1 - r4;
        r0 = r1 - r0;
        r1 = r7.getMeasuredHeight();
        r0 = r0 - r1;
        r0 = r0 / 2;
        goto L_0x00e5;
    L_0x019d:
        r0 = r9.getPaddingTop();
        r0 = r0 + r4;
        goto L_0x00e5;
    L_0x01a4:
        r0 = r9.getHeight();
        r1 = r9.getPaddingBottom();
        r0 = r0 - r1;
        r1 = r7.getMeasuredHeight();
        r0 = r0 - r1;
        r0 = r0 - r5;
        goto L_0x00e5;
    L_0x01b5:
        r2 = r1;
        goto L_0x00d4;
    L_0x01b8:
        r7 = r0;
        goto L_0x009b;
    L_0x01bb:
        r5 = r4;
        r6 = r2;
        r4 = r3;
        r3 = r1;
        goto L_0x00c4;
    L_0x01c1:
        r2 = r0;
        goto L_0x0094;
    L_0x01c4:
        r0 = r1;
        goto L_0x0032;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.internal.widget.ActionBarView.onLayout(boolean, int, int, int, int):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onMeasure(int r20, int r21) {
        /*
        r19 = this;
        r13 = r19.getChildCount();
        r0 = r19;
        r1 = r0.mIsCollapsable;
        if (r1 == 0) goto L_0x0045;
    L_0x000a:
        r2 = 0;
        r1 = 0;
        r18 = r1;
        r1 = r2;
        r2 = r18;
    L_0x0011:
        if (r2 >= r13) goto L_0x0036;
    L_0x0013:
        r0 = r19;
        r3 = r0.getChildAt(r2);
        r4 = r3.getVisibility();
        r5 = 8;
        if (r4 == r5) goto L_0x0033;
    L_0x0021:
        r0 = r19;
        r4 = r0.mMenuView;
        if (r3 != r4) goto L_0x0031;
    L_0x0027:
        r0 = r19;
        r3 = r0.mMenuView;
        r3 = r3.getChildCount();
        if (r3 == 0) goto L_0x0033;
    L_0x0031:
        r1 = r1 + 1;
    L_0x0033:
        r2 = r2 + 1;
        goto L_0x0011;
    L_0x0036:
        if (r1 != 0) goto L_0x0045;
    L_0x0038:
        r1 = 0;
        r2 = 0;
        r0 = r19;
        r0.setMeasuredDimension(r1, r2);
        r1 = 1;
        r0 = r19;
        r0.mIsCollapsed = r1;
    L_0x0044:
        return;
    L_0x0045:
        r1 = 0;
        r0 = r19;
        r0.mIsCollapsed = r1;
        r1 = android.view.View.MeasureSpec.getMode(r20);
        r2 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        if (r1 == r2) goto L_0x0079;
    L_0x0052:
        r1 = new java.lang.IllegalStateException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = r19.getClass();
        r3 = r3.getSimpleName();
        r2 = r2.append(r3);
        r3 = " can only be used ";
        r2 = r2.append(r3);
        r3 = "with android:layout_width=\"MATCH_PARENT\" (or fill_parent)";
        r2 = r2.append(r3);
        r2 = r2.toString();
        r1.<init>(r2);
        throw r1;
    L_0x0079:
        r1 = android.view.View.MeasureSpec.getMode(r21);
        r2 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        if (r1 == r2) goto L_0x00a8;
    L_0x0081:
        r1 = new java.lang.IllegalStateException;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = r19.getClass();
        r3 = r3.getSimpleName();
        r2 = r2.append(r3);
        r3 = " can only be used ";
        r2 = r2.append(r3);
        r3 = "with android:layout_height=\"wrap_content\"";
        r2 = r2.append(r3);
        r2 = r2.toString();
        r1.<init>(r2);
        throw r1;
    L_0x00a8:
        r14 = android.view.View.MeasureSpec.getSize(r20);
        r0 = r19;
        r1 = r0.mContentHeight;
        if (r1 <= 0) goto L_0x0270;
    L_0x00b2:
        r0 = r19;
        r1 = r0.mContentHeight;
        r3 = r1;
    L_0x00b7:
        r1 = r19.getPaddingTop();
        r2 = r19.getPaddingBottom();
        r15 = r1 + r2;
        r1 = r19.getPaddingLeft();
        r2 = r19.getPaddingRight();
        r10 = r3 - r15;
        r4 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r6 = android.view.View.MeasureSpec.makeMeasureSpec(r10, r4);
        r1 = r14 - r1;
        r5 = r1 - r2;
        r4 = r5 / 2;
        r0 = r19;
        r1 = r0.mExpandedActionView;
        if (r1 == 0) goto L_0x0277;
    L_0x00dd:
        r0 = r19;
        r1 = r0.mExpandedHomeLayout;
    L_0x00e1:
        r2 = r1.getVisibility();
        r7 = 8;
        if (r2 == r7) goto L_0x03ac;
    L_0x00e9:
        r2 = r1.getLayoutParams();
        r7 = r2.width;
        if (r7 >= 0) goto L_0x027d;
    L_0x00f1:
        r2 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r2);
    L_0x00f7:
        r7 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r10, r7);
        r1.measure(r2, r7);
        r2 = r1.getMeasuredWidth();
        r1 = r1.getLeftOffset();
        r1 = r1 + r2;
        r2 = 0;
        r5 = r5 - r1;
        r2 = java.lang.Math.max(r2, r5);
        r5 = 0;
        r1 = r2 - r1;
        r1 = java.lang.Math.max(r5, r1);
    L_0x0116:
        r0 = r19;
        r5 = r0.mMenuView;
        if (r5 == 0) goto L_0x0141;
    L_0x011c:
        r0 = r19;
        r5 = r0.mMenuView;
        r5 = r5.getParent();
        r0 = r19;
        if (r5 != r0) goto L_0x0141;
    L_0x0128:
        r0 = r19;
        r5 = r0.mMenuView;
        r7 = 0;
        r0 = r19;
        r2 = r0.measureChildView(r5, r2, r6, r7);
        r5 = 0;
        r0 = r19;
        r7 = r0.mMenuView;
        r7 = r7.getMeasuredWidth();
        r4 = r4 - r7;
        r4 = java.lang.Math.max(r5, r4);
    L_0x0141:
        r0 = r19;
        r5 = r0.mIndeterminateProgressView;
        if (r5 == 0) goto L_0x016c;
    L_0x0147:
        r0 = r19;
        r5 = r0.mIndeterminateProgressView;
        r5 = r5.getVisibility();
        r7 = 8;
        if (r5 == r7) goto L_0x016c;
    L_0x0153:
        r0 = r19;
        r5 = r0.mIndeterminateProgressView;
        r7 = 0;
        r0 = r19;
        r2 = r0.measureChildView(r5, r2, r6, r7);
        r5 = 0;
        r0 = r19;
        r6 = r0.mIndeterminateProgressView;
        r6 = r6.getMeasuredWidth();
        r4 = r4 - r6;
        r4 = java.lang.Math.max(r5, r4);
    L_0x016c:
        r0 = r19;
        r5 = r0.mTitleLayout;
        if (r5 == 0) goto L_0x0287;
    L_0x0172:
        r0 = r19;
        r5 = r0.mTitleLayout;
        r5 = r5.getVisibility();
        r6 = 8;
        if (r5 == r6) goto L_0x0287;
    L_0x017e:
        r0 = r19;
        r5 = r0.mDisplayOptions;
        r5 = r5 & 8;
        if (r5 == 0) goto L_0x0287;
    L_0x0186:
        r5 = 1;
    L_0x0187:
        r0 = r19;
        r6 = r0.mExpandedActionView;
        if (r6 != 0) goto L_0x0194;
    L_0x018d:
        r0 = r19;
        r6 = r0.mNavigationMode;
        switch(r6) {
            case 1: goto L_0x028a;
            case 2: goto L_0x02d4;
            default: goto L_0x0194;
        };
    L_0x0194:
        r6 = r1;
        r7 = r2;
    L_0x0196:
        r0 = r19;
        r1 = r0.mExpandedActionView;
        if (r1 == 0) goto L_0x031e;
    L_0x019c:
        r0 = r19;
        r1 = r0.mExpandedActionView;
        r12 = r1;
    L_0x01a1:
        if (r12 == 0) goto L_0x0229;
    L_0x01a3:
        r1 = r12.getLayoutParams();
        r0 = r19;
        r2 = r0.generateLayoutParams(r1);
        r1 = r2 instanceof android.support.v7.app.ActionBar.LayoutParams;
        if (r1 == 0) goto L_0x0335;
    L_0x01b1:
        r1 = r2;
        r1 = (android.support.v7.app.ActionBar.LayoutParams) r1;
        r11 = r1;
    L_0x01b5:
        r8 = 0;
        r1 = 0;
        if (r11 == 0) goto L_0x01c3;
    L_0x01b9:
        r1 = r11.leftMargin;
        r8 = r11.rightMargin;
        r8 = r8 + r1;
        r1 = r11.topMargin;
        r9 = r11.bottomMargin;
        r1 = r1 + r9;
    L_0x01c3:
        r0 = r19;
        r9 = r0.mContentHeight;
        if (r9 > 0) goto L_0x0339;
    L_0x01c9:
        r9 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
    L_0x01cb:
        r0 = r2.height;
        r16 = r0;
        if (r16 < 0) goto L_0x01db;
    L_0x01d1:
        r0 = r2.height;
        r16 = r0;
        r0 = r16;
        r10 = java.lang.Math.min(r0, r10);
    L_0x01db:
        r16 = 0;
        r1 = r10 - r1;
        r0 = r16;
        r16 = java.lang.Math.max(r0, r1);
        r1 = r2.width;
        r10 = -2;
        if (r1 == r10) goto L_0x0349;
    L_0x01ea:
        r1 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
    L_0x01ec:
        r10 = r2.width;
        if (r10 < 0) goto L_0x034d;
    L_0x01f0:
        r10 = r2.width;
        r10 = java.lang.Math.min(r10, r7);
    L_0x01f6:
        r17 = 0;
        r10 = r10 - r8;
        r0 = r17;
        r10 = java.lang.Math.max(r0, r10);
        if (r11 == 0) goto L_0x0350;
    L_0x0201:
        r11 = r11.gravity;
    L_0x0203:
        r11 = r11 & 7;
        r17 = 1;
        r0 = r17;
        if (r11 != r0) goto L_0x03a9;
    L_0x020b:
        r2 = r2.width;
        r11 = -1;
        if (r2 != r11) goto L_0x03a9;
    L_0x0210:
        r2 = java.lang.Math.min(r6, r4);
        r2 = r2 * 2;
    L_0x0216:
        r1 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r1);
        r0 = r16;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r0, r9);
        r12.measure(r1, r2);
        r1 = r12.getMeasuredWidth();
        r1 = r1 + r8;
        r7 = r7 - r1;
    L_0x0229:
        r0 = r19;
        r1 = r0.mExpandedActionView;
        if (r1 != 0) goto L_0x0253;
    L_0x022f:
        if (r5 == 0) goto L_0x0253;
    L_0x0231:
        r0 = r19;
        r1 = r0.mTitleLayout;
        r0 = r19;
        r2 = r0.mContentHeight;
        r4 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r4);
        r4 = 0;
        r0 = r19;
        r0.measureChildView(r1, r7, r2, r4);
        r1 = 0;
        r0 = r19;
        r2 = r0.mTitleLayout;
        r2 = r2.getMeasuredWidth();
        r2 = r6 - r2;
        java.lang.Math.max(r1, r2);
    L_0x0253:
        r0 = r19;
        r1 = r0.mContentHeight;
        if (r1 > 0) goto L_0x039d;
    L_0x0259:
        r2 = 0;
        r1 = 0;
        r3 = r1;
    L_0x025c:
        if (r3 >= r13) goto L_0x0354;
    L_0x025e:
        r0 = r19;
        r1 = r0.getChildAt(r3);
        r1 = r1.getMeasuredHeight();
        r1 = r1 + r15;
        if (r1 <= r2) goto L_0x03a6;
    L_0x026b:
        r2 = r3 + 1;
        r3 = r2;
        r2 = r1;
        goto L_0x025c;
    L_0x0270:
        r1 = android.view.View.MeasureSpec.getSize(r21);
        r3 = r1;
        goto L_0x00b7;
    L_0x0277:
        r0 = r19;
        r1 = r0.mHomeLayout;
        goto L_0x00e1;
    L_0x027d:
        r2 = r2.width;
        r7 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r7);
        goto L_0x00f7;
    L_0x0287:
        r5 = 0;
        goto L_0x0187;
    L_0x028a:
        r0 = r19;
        r6 = r0.mListNavLayout;
        if (r6 == 0) goto L_0x0194;
    L_0x0290:
        if (r5 == 0) goto L_0x02cf;
    L_0x0292:
        r0 = r19;
        r6 = r0.mItemPadding;
        r6 = r6 * 2;
    L_0x0298:
        r7 = 0;
        r2 = r2 - r6;
        r2 = java.lang.Math.max(r7, r2);
        r7 = 0;
        r1 = r1 - r6;
        r1 = java.lang.Math.max(r7, r1);
        r0 = r19;
        r6 = r0.mListNavLayout;
        r7 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r7);
        r8 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r8 = android.view.View.MeasureSpec.makeMeasureSpec(r10, r8);
        r6.measure(r7, r8);
        r0 = r19;
        r6 = r0.mListNavLayout;
        r6 = r6.getMeasuredWidth();
        r7 = 0;
        r2 = r2 - r6;
        r2 = java.lang.Math.max(r7, r2);
        r7 = 0;
        r1 = r1 - r6;
        r1 = java.lang.Math.max(r7, r1);
        r6 = r1;
        r7 = r2;
        goto L_0x0196;
    L_0x02cf:
        r0 = r19;
        r6 = r0.mItemPadding;
        goto L_0x0298;
    L_0x02d4:
        r0 = r19;
        r6 = r0.mTabScrollView;
        if (r6 == 0) goto L_0x0194;
    L_0x02da:
        if (r5 == 0) goto L_0x0319;
    L_0x02dc:
        r0 = r19;
        r6 = r0.mItemPadding;
        r6 = r6 * 2;
    L_0x02e2:
        r7 = 0;
        r2 = r2 - r6;
        r2 = java.lang.Math.max(r7, r2);
        r7 = 0;
        r1 = r1 - r6;
        r1 = java.lang.Math.max(r7, r1);
        r0 = r19;
        r6 = r0.mTabScrollView;
        r7 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r7 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r7);
        r8 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r8 = android.view.View.MeasureSpec.makeMeasureSpec(r10, r8);
        r6.measure(r7, r8);
        r0 = r19;
        r6 = r0.mTabScrollView;
        r6 = r6.getMeasuredWidth();
        r7 = 0;
        r2 = r2 - r6;
        r2 = java.lang.Math.max(r7, r2);
        r7 = 0;
        r1 = r1 - r6;
        r1 = java.lang.Math.max(r7, r1);
        r6 = r1;
        r7 = r2;
        goto L_0x0196;
    L_0x0319:
        r0 = r19;
        r6 = r0.mItemPadding;
        goto L_0x02e2;
    L_0x031e:
        r0 = r19;
        r1 = r0.mDisplayOptions;
        r2 = r1 & 16;
        r1 = 0;
        if (r2 == 0) goto L_0x03a3;
    L_0x0327:
        r0 = r19;
        r2 = r0.mCustomNavView;
        r1 = 0;
        if (r2 == 0) goto L_0x03a3;
    L_0x032e:
        r0 = r19;
        r1 = r0.mCustomNavView;
        r12 = r1;
        goto L_0x01a1;
    L_0x0335:
        r1 = 0;
        r11 = r1;
        goto L_0x01b5;
    L_0x0339:
        r9 = r2.height;
        r16 = -2;
        r0 = r16;
        if (r9 == r0) goto L_0x0345;
    L_0x0341:
        r9 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        goto L_0x01cb;
    L_0x0345:
        r9 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        goto L_0x01cb;
    L_0x0349:
        r1 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        goto L_0x01ec;
    L_0x034d:
        r10 = r7;
        goto L_0x01f6;
    L_0x0350:
        r11 = 19;
        goto L_0x0203;
    L_0x0354:
        r0 = r19;
        r0.setMeasuredDimension(r14, r2);
    L_0x0359:
        r0 = r19;
        r1 = r0.mContextView;
        if (r1 == 0) goto L_0x036a;
    L_0x035f:
        r0 = r19;
        r1 = r0.mContextView;
        r2 = r19.getMeasuredHeight();
        r1.setContentHeight(r2);
    L_0x036a:
        r0 = r19;
        r1 = r0.mProgressView;
        if (r1 == 0) goto L_0x0044;
    L_0x0370:
        r0 = r19;
        r1 = r0.mProgressView;
        r1 = r1.getVisibility();
        r2 = 8;
        if (r1 == r2) goto L_0x0044;
    L_0x037c:
        r0 = r19;
        r1 = r0.mProgressView;
        r0 = r19;
        r2 = r0.mProgressBarPadding;
        r2 = r2 * 2;
        r2 = r14 - r2;
        r3 = 1073741824; // 0x40000000 float:2.0 double:5.304989477E-315;
        r2 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r3);
        r3 = r19.getMeasuredHeight();
        r4 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r3 = android.view.View.MeasureSpec.makeMeasureSpec(r3, r4);
        r1.measure(r2, r3);
        goto L_0x0044;
    L_0x039d:
        r0 = r19;
        r0.setMeasuredDimension(r14, r3);
        goto L_0x0359;
    L_0x03a3:
        r12 = r1;
        goto L_0x01a1;
    L_0x03a6:
        r1 = r2;
        goto L_0x026b;
    L_0x03a9:
        r2 = r10;
        goto L_0x0216;
    L_0x03ac:
        r1 = r4;
        r2 = r5;
        goto L_0x0116;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.internal.widget.ActionBarView.onMeasure(int, int):void");
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (!(savedState.expandedMenuItemId == 0 || this.mExpandedMenuPresenter == null || this.mOptionsMenu == null)) {
            SupportMenuItem supportMenuItem = (SupportMenuItem) this.mOptionsMenu.findItem(savedState.expandedMenuItemId);
            if (supportMenuItem != null) {
                supportMenuItem.expandActionView();
            }
        }
        if (savedState.isOverflowOpen) {
            postShowOverflowMenu();
        }
    }

    public Parcelable onSaveInstanceState() {
        Parcelable savedState = new SavedState(super.onSaveInstanceState());
        if (!(this.mExpandedMenuPresenter == null || this.mExpandedMenuPresenter.mCurrentExpandedItem == null)) {
            savedState.expandedMenuItemId = this.mExpandedMenuPresenter.mCurrentExpandedItem.getItemId();
        }
        savedState.isOverflowOpen = isOverflowMenuShowing();
        return savedState;
    }

    public /* bridge */ /* synthetic */ void postShowOverflowMenu() {
        super.postShowOverflowMenu();
    }

    public void setCallback(OnNavigationListener onNavigationListener) {
        this.mCallback = onNavigationListener;
    }

    public void setCollapsable(boolean z) {
        this.mIsCollapsable = z;
    }

    public /* bridge */ /* synthetic */ void setContentHeight(int i) {
        super.setContentHeight(i);
    }

    public void setContextView(ActionBarContextView actionBarContextView) {
        this.mContextView = actionBarContextView;
    }

    public void setCustomNavigationView(View view) {
        Object obj = (this.mDisplayOptions & 16) != 0 ? 1 : null;
        if (!(this.mCustomNavView == null || obj == null)) {
            removeView(this.mCustomNavView);
        }
        this.mCustomNavView = view;
        if (this.mCustomNavView != null && obj != null) {
            addView(this.mCustomNavView);
        }
    }

    public void setDisplayOptions(int i) {
        int i2 = 8;
        int i3 = -1;
        boolean z = true;
        if (this.mDisplayOptions != -1) {
            i3 = this.mDisplayOptions ^ i;
        }
        this.mDisplayOptions = i;
        if ((i3 & DISPLAY_RELAYOUT_MASK) != 0) {
            boolean z2;
            boolean z3 = (i & 2) != 0;
            int i4 = (z3 && this.mExpandedActionView == null) ? 0 : 8;
            this.mHomeLayout.setVisibility(i4);
            if ((i3 & 4) != 0) {
                z2 = (i & 4) != 0;
                this.mHomeLayout.setUp(z2);
                if (z2) {
                    setHomeButtonEnabled(true);
                }
            }
            if ((i3 & 1) != 0) {
                z2 = (this.mLogo == null || (i & 1) == 0) ? false : true;
                this.mHomeLayout.setIcon(z2 ? this.mLogo : this.mIcon);
            }
            if ((i3 & 8) != 0) {
                if ((i & 8) != 0) {
                    initTitle();
                } else {
                    removeView(this.mTitleLayout);
                }
            }
            if (!(this.mTitleLayout == null || (i3 & 6) == 0)) {
                z2 = (this.mDisplayOptions & 4) != 0;
                View view = this.mTitleUpView;
                if (!z3) {
                    i2 = z2 ? 0 : 4;
                }
                view.setVisibility(i2);
                LinearLayout linearLayout = this.mTitleLayout;
                if (z3 || !z2) {
                    z = false;
                }
                linearLayout.setEnabled(z);
            }
            if (!((i3 & 16) == 0 || this.mCustomNavView == null)) {
                if ((i & 16) != 0) {
                    addView(this.mCustomNavView);
                } else {
                    removeView(this.mCustomNavView);
                }
            }
            requestLayout();
        } else {
            invalidate();
        }
        if (!this.mHomeLayout.isEnabled()) {
            this.mHomeLayout.setContentDescription(null);
        } else if ((i & 4) != 0) {
            this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(R.string.abc_action_bar_up_description));
        } else {
            this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(R.string.abc_action_bar_home_description));
        }
    }

    public void setDropdownAdapter(SpinnerAdapter spinnerAdapter) {
        this.mSpinnerAdapter = spinnerAdapter;
        if (this.mSpinner != null) {
            this.mSpinner.setAdapter(spinnerAdapter);
        }
    }

    public void setDropdownSelectedPosition(int i) {
        this.mSpinner.setSelection(i);
    }

    public void setEmbeddedTabView(ScrollingTabContainerView scrollingTabContainerView) {
        if (this.mTabScrollView != null) {
            removeView(this.mTabScrollView);
        }
        this.mTabScrollView = scrollingTabContainerView;
        this.mIncludeTabs = scrollingTabContainerView != null;
        if (this.mIncludeTabs && this.mNavigationMode == 2) {
            addView(this.mTabScrollView);
            ViewGroup.LayoutParams layoutParams = this.mTabScrollView.getLayoutParams();
            layoutParams.width = -2;
            layoutParams.height = -1;
            scrollingTabContainerView.setAllowCollapse(true);
        }
    }

    public void setHomeAsUpIndicator(int i) {
        this.mHomeLayout.setUpIndicator(i);
    }

    public void setHomeAsUpIndicator(Drawable drawable) {
        this.mHomeLayout.setUpIndicator(drawable);
    }

    public void setHomeButtonEnabled(boolean z) {
        this.mHomeLayout.setEnabled(z);
        this.mHomeLayout.setFocusable(z);
        if (!z) {
            this.mHomeLayout.setContentDescription(null);
        } else if ((this.mDisplayOptions & 4) != 0) {
            this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(R.string.abc_action_bar_up_description));
        } else {
            this.mHomeLayout.setContentDescription(this.mContext.getResources().getText(R.string.abc_action_bar_home_description));
        }
    }

    public void setIcon(int i) {
        setIcon(this.mContext.getResources().getDrawable(i));
    }

    public void setIcon(Drawable drawable) {
        this.mIcon = drawable;
        if (drawable != null && ((this.mDisplayOptions & 1) == 0 || this.mLogo == null)) {
            this.mHomeLayout.setIcon(drawable);
        }
        if (this.mExpandedActionView != null) {
            this.mExpandedHomeLayout.setIcon(this.mIcon.getConstantState().newDrawable(getResources()));
        }
    }

    public void setLogo(int i) {
        setLogo(this.mContext.getResources().getDrawable(i));
    }

    public void setLogo(Drawable drawable) {
        this.mLogo = drawable;
        if (drawable != null && (this.mDisplayOptions & 1) != 0) {
            this.mHomeLayout.setIcon(drawable);
        }
    }

    public void setMenu(SupportMenu supportMenu, MenuPresenter.Callback callback) {
        if (supportMenu != this.mOptionsMenu) {
            ActionMenuView actionMenuView;
            if (this.mOptionsMenu != null) {
                this.mOptionsMenu.removeMenuPresenter(this.mActionMenuPresenter);
                this.mOptionsMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
            }
            MenuBuilder menuBuilder = (MenuBuilder) supportMenu;
            this.mOptionsMenu = menuBuilder;
            if (this.mMenuView != null) {
                ViewGroup viewGroup = (ViewGroup) this.mMenuView.getParent();
                if (viewGroup != null) {
                    viewGroup.removeView(this.mMenuView);
                }
            }
            if (this.mActionMenuPresenter == null) {
                this.mActionMenuPresenter = new ActionMenuPresenter(this.mContext);
                this.mActionMenuPresenter.setCallback(callback);
                this.mActionMenuPresenter.setId(R.id.action_menu_presenter);
                this.mExpandedMenuPresenter = new ExpandedActionViewMenuPresenter();
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
            ViewGroup viewGroup2;
            if (this.mSplitActionBar) {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                this.mActionMenuPresenter.setItemLimit(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                layoutParams.width = -1;
                configPresenters(menuBuilder);
                actionMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                if (this.mSplitView != null) {
                    viewGroup2 = (ViewGroup) actionMenuView.getParent();
                    if (!(viewGroup2 == null || viewGroup2 == this.mSplitView)) {
                        viewGroup2.removeView(actionMenuView);
                    }
                    actionMenuView.setVisibility(getAnimatedVisibility());
                    this.mSplitView.addView(actionMenuView, layoutParams);
                } else {
                    actionMenuView.setLayoutParams(layoutParams);
                }
            } else {
                this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(R.bool.abc_action_bar_expanded_action_views_exclusive));
                configPresenters(menuBuilder);
                actionMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                actionMenuView.initialize(menuBuilder);
                viewGroup2 = (ViewGroup) actionMenuView.getParent();
                if (!(viewGroup2 == null || viewGroup2 == this)) {
                    viewGroup2.removeView(actionMenuView);
                }
                addView(actionMenuView, layoutParams);
            }
            this.mMenuView = actionMenuView;
        }
    }

    public void setNavigationMode(int i) {
        int i2 = this.mNavigationMode;
        if (i != i2) {
            switch (i2) {
                case 1:
                    if (this.mListNavLayout != null) {
                        removeView(this.mListNavLayout);
                        break;
                    }
                    break;
                case 2:
                    if (this.mTabScrollView != null && this.mIncludeTabs) {
                        removeView(this.mTabScrollView);
                        break;
                    }
            }
            switch (i) {
                case 1:
                    if (this.mSpinner == null) {
                        this.mSpinner = new SpinnerICS(this.mContext, null, R.attr.actionDropDownStyle);
                        this.mListNavLayout = (LinearLayout) LayoutInflater.from(this.mContext).inflate(R.layout.abc_action_bar_view_list_nav_layout, null);
                        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -1);
                        layoutParams.gravity = 17;
                        this.mListNavLayout.addView(this.mSpinner, layoutParams);
                    }
                    if (this.mSpinner.getAdapter() != this.mSpinnerAdapter) {
                        this.mSpinner.setAdapter(this.mSpinnerAdapter);
                    }
                    this.mSpinner.setOnItemSelectedListener(this.mNavItemSelectedListener);
                    addView(this.mListNavLayout);
                    break;
                case 2:
                    if (this.mTabScrollView != null && this.mIncludeTabs) {
                        addView(this.mTabScrollView);
                        break;
                    }
            }
            this.mNavigationMode = i;
            requestLayout();
        }
    }

    public void setSplitActionBar(boolean z) {
        if (this.mSplitActionBar != z) {
            if (this.mMenuView != null) {
                ViewGroup viewGroup = (ViewGroup) this.mMenuView.getParent();
                if (viewGroup != null) {
                    viewGroup.removeView(this.mMenuView);
                }
                if (z) {
                    if (this.mSplitView != null) {
                        this.mSplitView.addView(this.mMenuView);
                    }
                    this.mMenuView.getLayoutParams().width = -1;
                } else {
                    addView(this.mMenuView);
                    this.mMenuView.getLayoutParams().width = -2;
                }
                this.mMenuView.requestLayout();
            }
            if (this.mSplitView != null) {
                this.mSplitView.setVisibility(z ? 0 : 8);
            }
            if (this.mActionMenuPresenter != null) {
                if (z) {
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(false);
                    this.mActionMenuPresenter.setWidthLimit(getContext().getResources().getDisplayMetrics().widthPixels, true);
                    this.mActionMenuPresenter.setItemLimit(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                } else {
                    this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(R.bool.abc_action_bar_expanded_action_views_exclusive));
                }
            }
            super.setSplitActionBar(z);
        }
    }

    public /* bridge */ /* synthetic */ void setSplitView(ActionBarContainer actionBarContainer) {
        super.setSplitView(actionBarContainer);
    }

    public /* bridge */ /* synthetic */ void setSplitWhenNarrow(boolean z) {
        super.setSplitWhenNarrow(z);
    }

    public void setSubtitle(CharSequence charSequence) {
        int i = 8;
        this.mSubtitle = charSequence;
        if (this.mSubtitleView != null) {
            this.mSubtitleView.setText(charSequence);
            this.mSubtitleView.setVisibility(charSequence != null ? 0 : 8);
            Object obj = (this.mExpandedActionView != null || (this.mDisplayOptions & 8) == 0 || (TextUtils.isEmpty(this.mTitle) && TextUtils.isEmpty(this.mSubtitle))) ? null : 1;
            LinearLayout linearLayout = this.mTitleLayout;
            if (obj != null) {
                i = 0;
            }
            linearLayout.setVisibility(i);
        }
    }

    public void setTitle(CharSequence charSequence) {
        this.mUserTitle = true;
        setTitleImpl(charSequence);
    }

    public /* bridge */ /* synthetic */ void setVisibility(int i) {
        super.setVisibility(i);
    }

    public void setWindowCallback(Callback callback) {
        this.mWindowCallback = callback;
    }

    public void setWindowTitle(CharSequence charSequence) {
        if (!this.mUserTitle) {
            setTitleImpl(charSequence);
        }
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public /* bridge */ /* synthetic */ boolean showOverflowMenu() {
        return super.showOverflowMenu();
    }
}
