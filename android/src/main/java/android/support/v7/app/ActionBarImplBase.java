package android.support.v7.app;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.ActionBar.OnMenuVisibilityListener;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.ActionBarPolicy;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.SubMenuBuilder;
import android.support.v7.internal.widget.ActionBarContainer;
import android.support.v7.internal.widget.ActionBarContextView;
import android.support.v7.internal.widget.ActionBarOverlayLayout;
import android.support.v7.internal.widget.ActionBarView;
import android.support.v7.internal.widget.ScrollingTabContainerView;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.SpinnerAdapter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

class ActionBarImplBase extends ActionBar {
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final int INVALID_POSITION = -1;
    ActionModeImpl mActionMode;
    private ActionBarView mActionView;
    private ActionBarActivity mActivity;
    private Callback mCallback;
    private ActionBarContainer mContainerView;
    private View mContentView;
    private Context mContext;
    private int mContextDisplayMode;
    private ActionBarContextView mContextView;
    private int mCurWindowVisibility = 0;
    ActionMode mDeferredDestroyActionMode;
    Callback mDeferredModeDestroyCallback;
    private Dialog mDialog;
    private boolean mDisplayHomeAsUpSet;
    final Handler mHandler = new Handler();
    private boolean mHasEmbeddedTabs;
    private boolean mHiddenByApp;
    private boolean mHiddenBySystem;
    private boolean mLastMenuVisibility;
    private ArrayList<OnMenuVisibilityListener> mMenuVisibilityListeners = new ArrayList();
    private boolean mNowShowing = true;
    private ActionBarOverlayLayout mOverlayLayout;
    private int mSavedTabPosition = -1;
    private TabImpl mSelectedTab;
    private boolean mShowHideAnimationEnabled;
    private boolean mShowingForMode;
    private ActionBarContainer mSplitView;
    private ScrollingTabContainerView mTabScrollView;
    Runnable mTabSelector;
    private ArrayList<TabImpl> mTabs = new ArrayList();
    private Context mThemedContext;
    private ViewGroup mTopVisibilityView;

    class ActionModeImpl extends ActionMode implements MenuBuilder.Callback {
        private Callback mCallback;
        private WeakReference<View> mCustomView;
        private MenuBuilder mMenu;

        public ActionModeImpl(Callback callback) {
            this.mCallback = callback;
            this.mMenu = new MenuBuilder(ActionBarImplBase.this.getThemedContext()).setDefaultShowAsAction(1);
            this.mMenu.setCallback(this);
        }

        public boolean dispatchOnCreate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                boolean onCreateActionMode = this.mCallback.onCreateActionMode(this, this.mMenu);
                return onCreateActionMode;
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public void finish() {
            if (ActionBarImplBase.this.mActionMode == this) {
                if (ActionBarImplBase.checkShowingFlags(ActionBarImplBase.this.mHiddenByApp, ActionBarImplBase.this.mHiddenBySystem, false)) {
                    this.mCallback.onDestroyActionMode(this);
                } else {
                    ActionBarImplBase.this.mDeferredDestroyActionMode = this;
                    ActionBarImplBase.this.mDeferredModeDestroyCallback = this.mCallback;
                }
                this.mCallback = null;
                ActionBarImplBase.this.animateToMode(false);
                ActionBarImplBase.this.mContextView.closeMode();
                ActionBarImplBase.this.mActionView.sendAccessibilityEvent(32);
                ActionBarImplBase.this.mActionMode = null;
            }
        }

        public View getCustomView() {
            return this.mCustomView != null ? (View) this.mCustomView.get() : null;
        }

        public Menu getMenu() {
            return this.mMenu;
        }

        public MenuInflater getMenuInflater() {
            return new SupportMenuInflater(ActionBarImplBase.this.getThemedContext());
        }

        public CharSequence getSubtitle() {
            return ActionBarImplBase.this.mContextView.getSubtitle();
        }

        public CharSequence getTitle() {
            return ActionBarImplBase.this.mContextView.getTitle();
        }

        public void invalidate() {
            this.mMenu.stopDispatchingItemsChanged();
            try {
                this.mCallback.onPrepareActionMode(this, this.mMenu);
            } finally {
                this.mMenu.startDispatchingItemsChanged();
            }
        }

        public boolean isTitleOptional() {
            return ActionBarImplBase.this.mContextView.isTitleOptional();
        }

        public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
        }

        public void onCloseSubMenu(SubMenuBuilder subMenuBuilder) {
        }

        public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
            return this.mCallback != null ? this.mCallback.onActionItemClicked(this, menuItem) : false;
        }

        public void onMenuModeChange(MenuBuilder menuBuilder) {
            if (this.mCallback != null) {
                invalidate();
                ActionBarImplBase.this.mContextView.showOverflowMenu();
            }
        }

        public void onMenuModeChange(Menu menu) {
            if (this.mCallback != null) {
                invalidate();
                ActionBarImplBase.this.mContextView.showOverflowMenu();
            }
        }

        public boolean onSubMenuSelected(SubMenuBuilder subMenuBuilder) {
            return this.mCallback == null ? false : !subMenuBuilder.hasVisibleItems() ? true : true;
        }

        public void setCustomView(View view) {
            ActionBarImplBase.this.mContextView.setCustomView(view);
            this.mCustomView = new WeakReference(view);
        }

        public void setSubtitle(int i) {
            setSubtitle(ActionBarImplBase.this.mContext.getResources().getString(i));
        }

        public void setSubtitle(CharSequence charSequence) {
            ActionBarImplBase.this.mContextView.setSubtitle(charSequence);
        }

        public void setTitle(int i) {
            setTitle(ActionBarImplBase.this.mContext.getResources().getString(i));
        }

        public void setTitle(CharSequence charSequence) {
            ActionBarImplBase.this.mContextView.setTitle(charSequence);
        }

        public void setTitleOptionalHint(boolean z) {
            super.setTitleOptionalHint(z);
            ActionBarImplBase.this.mContextView.setTitleOptional(z);
        }
    }

    public class TabImpl extends Tab {
        private TabListener mCallback;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private int mPosition = -1;
        private Object mTag;
        private CharSequence mText;

        public TabListener getCallback() {
            return this.mCallback;
        }

        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }

        public View getCustomView() {
            return this.mCustomView;
        }

        public Drawable getIcon() {
            return this.mIcon;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public Object getTag() {
            return this.mTag;
        }

        public CharSequence getText() {
            return this.mText;
        }

        public void select() {
            ActionBarImplBase.this.selectTab(this);
        }

        public Tab setContentDescription(int i) {
            return setContentDescription(ActionBarImplBase.this.mContext.getResources().getText(i));
        }

        public Tab setContentDescription(CharSequence charSequence) {
            this.mContentDesc = charSequence;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setCustomView(int i) {
            return setCustomView(LayoutInflater.from(ActionBarImplBase.this.getThemedContext()).inflate(i, null));
        }

        public Tab setCustomView(View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public Tab setIcon(int i) {
            return setIcon(ActionBarImplBase.this.mContext.getResources().getDrawable(i));
        }

        public Tab setIcon(Drawable drawable) {
            this.mIcon = drawable;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }

        public void setPosition(int i) {
            this.mPosition = i;
        }

        public Tab setTabListener(TabListener tabListener) {
            this.mCallback = tabListener;
            return this;
        }

        public Tab setTag(Object obj) {
            this.mTag = obj;
            return this;
        }

        public Tab setText(int i) {
            return setText(ActionBarImplBase.this.mContext.getResources().getText(i));
        }

        public Tab setText(CharSequence charSequence) {
            this.mText = charSequence;
            if (this.mPosition >= 0) {
                ActionBarImplBase.this.mTabScrollView.updateTab(this.mPosition);
            }
            return this;
        }
    }

    public ActionBarImplBase(ActionBarActivity actionBarActivity, Callback callback) {
        this.mActivity = actionBarActivity;
        this.mContext = actionBarActivity;
        this.mCallback = callback;
        init(this.mActivity);
    }

    private static boolean checkShowingFlags(boolean z, boolean z2, boolean z3) {
        return z3 || !(z || z2);
    }

    private void cleanupTabs() {
        if (this.mSelectedTab != null) {
            selectTab(null);
        }
        this.mTabs.clear();
        if (this.mTabScrollView != null) {
            this.mTabScrollView.removeAllTabs();
        }
        this.mSavedTabPosition = -1;
    }

    private void configureTab(Tab tab, int i) {
        TabImpl tabImpl = (TabImpl) tab;
        if (tabImpl.getCallback() == null) {
            throw new IllegalStateException("Action Bar Tab must have a Callback");
        }
        tabImpl.setPosition(i);
        this.mTabs.add(i, tabImpl);
        int size = this.mTabs.size();
        for (int i2 = i + 1; i2 < size; i2++) {
            ((TabImpl) this.mTabs.get(i2)).setPosition(i2);
        }
    }

    private void ensureTabsExist() {
        if (this.mTabScrollView == null) {
            ScrollingTabContainerView scrollingTabContainerView = new ScrollingTabContainerView(this.mContext);
            if (this.mHasEmbeddedTabs) {
                scrollingTabContainerView.setVisibility(0);
                this.mActionView.setEmbeddedTabView(scrollingTabContainerView);
            } else {
                if (getNavigationMode() == 2) {
                    scrollingTabContainerView.setVisibility(0);
                } else {
                    scrollingTabContainerView.setVisibility(8);
                }
                this.mContainerView.setTabContainer(scrollingTabContainerView);
            }
            this.mTabScrollView = scrollingTabContainerView;
        }
    }

    private void init(ActionBarActivity actionBarActivity) {
        boolean z = false;
        this.mOverlayLayout = (ActionBarOverlayLayout) actionBarActivity.findViewById(R.id.action_bar_overlay_layout);
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setActionBar(this);
        }
        this.mActionView = (ActionBarView) actionBarActivity.findViewById(R.id.action_bar);
        this.mContextView = (ActionBarContextView) actionBarActivity.findViewById(R.id.action_context_bar);
        this.mContainerView = (ActionBarContainer) actionBarActivity.findViewById(R.id.action_bar_container);
        this.mTopVisibilityView = (ViewGroup) actionBarActivity.findViewById(R.id.top_action_bar);
        if (this.mTopVisibilityView == null) {
            this.mTopVisibilityView = this.mContainerView;
        }
        this.mSplitView = (ActionBarContainer) actionBarActivity.findViewById(R.id.split_action_bar);
        if (this.mActionView == null || this.mContextView == null || this.mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with a compatible window decor layout");
        }
        this.mActionView.setContextView(this.mContextView);
        this.mContextDisplayMode = this.mActionView.isSplitActionBar() ? 1 : 0;
        boolean z2 = (this.mActionView.getDisplayOptions() & 4) != 0;
        if (z2) {
            this.mDisplayHomeAsUpSet = true;
        }
        ActionBarPolicy actionBarPolicy = ActionBarPolicy.get(this.mContext);
        if (actionBarPolicy.enableHomeButtonByDefault() || z2) {
            z = true;
        }
        setHomeButtonEnabled(z);
        setHasEmbeddedTabs(actionBarPolicy.hasEmbeddedTabs());
        setTitle(this.mActivity.getTitle());
    }

    private void setHasEmbeddedTabs(boolean z) {
        boolean z2 = true;
        this.mHasEmbeddedTabs = z;
        if (this.mHasEmbeddedTabs) {
            this.mContainerView.setTabContainer(null);
            this.mActionView.setEmbeddedTabView(this.mTabScrollView);
        } else {
            this.mActionView.setEmbeddedTabView(null);
            this.mContainerView.setTabContainer(this.mTabScrollView);
        }
        boolean z3 = getNavigationMode() == 2;
        if (this.mTabScrollView != null) {
            if (z3) {
                this.mTabScrollView.setVisibility(0);
            } else {
                this.mTabScrollView.setVisibility(8);
            }
        }
        ActionBarView actionBarView = this.mActionView;
        if (this.mHasEmbeddedTabs || !z3) {
            z2 = false;
        }
        actionBarView.setCollapsable(z2);
    }

    private void updateVisibility(boolean z) {
        if (checkShowingFlags(this.mHiddenByApp, this.mHiddenBySystem, this.mShowingForMode)) {
            if (!this.mNowShowing) {
                this.mNowShowing = true;
                doShow(z);
            }
        } else if (this.mNowShowing) {
            this.mNowShowing = false;
            doHide(z);
        }
    }

    public void addOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener) {
        this.mMenuVisibilityListeners.add(onMenuVisibilityListener);
    }

    public void addTab(Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, int i) {
        addTab(tab, i, this.mTabs.isEmpty());
    }

    public void addTab(Tab tab, int i, boolean z) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, i, z);
        configureTab(tab, i);
        if (z) {
            selectTab(tab);
        }
    }

    public void addTab(Tab tab, boolean z) {
        ensureTabsExist();
        this.mTabScrollView.addTab(tab, z);
        configureTab(tab, this.mTabs.size());
        if (z) {
            selectTab(tab);
        }
    }

    void animateToMode(boolean z) {
        int i = 0;
        if (z) {
            showForActionMode();
        } else {
            hideForActionMode();
        }
        this.mActionView.animateToVisibility(z ? 4 : 0);
        this.mContextView.animateToVisibility(z ? 0 : 8);
        if (this.mTabScrollView != null && !this.mActionView.hasEmbeddedTabs() && this.mActionView.isCollapsed()) {
            ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
            if (z) {
                i = 8;
            }
            scrollingTabContainerView.setVisibility(i);
        }
    }

    public void doHide(boolean z) {
        this.mTopVisibilityView.clearAnimation();
        if (this.mTopVisibilityView.getVisibility() != 8) {
            Object obj = (isShowHideAnimationEnabled() || z) ? 1 : null;
            if (obj != null) {
                this.mTopVisibilityView.startAnimation(AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_out_top));
            }
            this.mTopVisibilityView.setVisibility(8);
            if (this.mSplitView != null && this.mSplitView.getVisibility() != 8) {
                if (obj != null) {
                    this.mSplitView.startAnimation(AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_out_bottom));
                }
                this.mSplitView.setVisibility(8);
            }
        }
    }

    public void doShow(boolean z) {
        this.mTopVisibilityView.clearAnimation();
        if (this.mTopVisibilityView.getVisibility() != 0) {
            int i = (isShowHideAnimationEnabled() || z) ? 1 : 0;
            if (i != 0) {
                this.mTopVisibilityView.startAnimation(AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_in_top));
            }
            this.mTopVisibilityView.setVisibility(0);
            if (this.mSplitView != null && this.mSplitView.getVisibility() != 0) {
                if (i != 0) {
                    this.mSplitView.startAnimation(AnimationUtils.loadAnimation(this.mContext, R.anim.abc_slide_in_bottom));
                }
                this.mSplitView.setVisibility(0);
            }
        }
    }

    public View getCustomView() {
        return this.mActionView.getCustomNavigationView();
    }

    public int getDisplayOptions() {
        return this.mActionView.getDisplayOptions();
    }

    public int getHeight() {
        return this.mContainerView.getHeight();
    }

    public int getNavigationItemCount() {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                SpinnerAdapter dropdownAdapter = this.mActionView.getDropdownAdapter();
                if (dropdownAdapter != null) {
                    return dropdownAdapter.getCount();
                }
                break;
            case 2:
                return this.mTabs.size();
        }
        return 0;
    }

    public int getNavigationMode() {
        return this.mActionView.getNavigationMode();
    }

    public int getSelectedNavigationIndex() {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                return this.mActionView.getDropdownSelectedPosition();
            case 2:
                if (this.mSelectedTab != null) {
                    return this.mSelectedTab.getPosition();
                }
                break;
        }
        return -1;
    }

    public Tab getSelectedTab() {
        return this.mSelectedTab;
    }

    public CharSequence getSubtitle() {
        return this.mActionView.getSubtitle();
    }

    public Tab getTabAt(int i) {
        return (Tab) this.mTabs.get(i);
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    public Context getThemedContext() {
        if (this.mThemedContext == null) {
            TypedValue typedValue = new TypedValue();
            this.mContext.getTheme().resolveAttribute(R.attr.actionBarWidgetTheme, typedValue, true);
            int i = typedValue.resourceId;
            if (i != 0) {
                this.mThemedContext = new ContextThemeWrapper(this.mContext, i);
            } else {
                this.mThemedContext = this.mContext;
            }
        }
        return this.mThemedContext;
    }

    public CharSequence getTitle() {
        return this.mActionView.getTitle();
    }

    public boolean hasNonEmbeddedTabs() {
        return !this.mHasEmbeddedTabs && getNavigationMode() == 2;
    }

    public void hide() {
        if (!this.mHiddenByApp) {
            this.mHiddenByApp = true;
            updateVisibility(false);
        }
    }

    void hideForActionMode() {
        if (this.mShowingForMode) {
            this.mShowingForMode = false;
            updateVisibility(false);
        }
    }

    boolean isShowHideAnimationEnabled() {
        return this.mShowHideAnimationEnabled;
    }

    public boolean isShowing() {
        return this.mNowShowing;
    }

    public Tab newTab() {
        return new TabImpl();
    }

    public void onConfigurationChanged(Configuration configuration) {
        setHasEmbeddedTabs(ActionBarPolicy.get(this.mContext).hasEmbeddedTabs());
    }

    public void removeAllTabs() {
        cleanupTabs();
    }

    public void removeOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener) {
        this.mMenuVisibilityListeners.remove(onMenuVisibilityListener);
    }

    public void removeTab(Tab tab) {
        removeTabAt(tab.getPosition());
    }

    public void removeTabAt(int i) {
        if (this.mTabScrollView != null) {
            int position = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : this.mSavedTabPosition;
            this.mTabScrollView.removeTabAt(i);
            TabImpl tabImpl = (TabImpl) this.mTabs.remove(i);
            if (tabImpl != null) {
                tabImpl.setPosition(-1);
            }
            int size = this.mTabs.size();
            for (int i2 = i; i2 < size; i2++) {
                ((TabImpl) this.mTabs.get(i2)).setPosition(i2);
            }
            if (position == i) {
                Tab tab;
                if (this.mTabs.isEmpty()) {
                    tab = null;
                } else {
                    tabImpl = (TabImpl) this.mTabs.get(Math.max(0, i - 1));
                }
                selectTab(tab);
            }
        }
    }

    public void selectTab(Tab tab) {
        int i = -1;
        if (getNavigationMode() != 2) {
            if (tab != null) {
                i = tab.getPosition();
            }
            this.mSavedTabPosition = i;
            return;
        }
        FragmentTransaction disallowAddToBackStack = this.mActivity.getSupportFragmentManager().beginTransaction().disallowAddToBackStack();
        if (this.mSelectedTab != tab) {
            ScrollingTabContainerView scrollingTabContainerView = this.mTabScrollView;
            if (tab != null) {
                i = tab.getPosition();
            }
            scrollingTabContainerView.setTabSelected(i);
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabUnselected(this.mSelectedTab, disallowAddToBackStack);
            }
            this.mSelectedTab = (TabImpl) tab;
            if (this.mSelectedTab != null) {
                this.mSelectedTab.getCallback().onTabSelected(this.mSelectedTab, disallowAddToBackStack);
            }
        } else if (this.mSelectedTab != null) {
            this.mSelectedTab.getCallback().onTabReselected(this.mSelectedTab, disallowAddToBackStack);
            this.mTabScrollView.animateToTab(tab.getPosition());
        }
        if (!disallowAddToBackStack.isEmpty()) {
            disallowAddToBackStack.commit();
        }
    }

    public void setBackgroundDrawable(Drawable drawable) {
        this.mContainerView.setPrimaryBackground(drawable);
    }

    public void setCustomView(int i) {
        setCustomView(LayoutInflater.from(getThemedContext()).inflate(i, this.mActionView, false));
    }

    public void setCustomView(View view) {
        this.mActionView.setCustomNavigationView(view);
    }

    public void setCustomView(View view, LayoutParams layoutParams) {
        view.setLayoutParams(layoutParams);
        this.mActionView.setCustomNavigationView(view);
    }

    public void setDisplayHomeAsUpEnabled(boolean z) {
        setDisplayOptions(z ? 4 : 0, 4);
    }

    public void setDisplayOptions(int i) {
        if ((i & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mActionView.setDisplayOptions(i);
    }

    public void setDisplayOptions(int i, int i2) {
        int displayOptions = this.mActionView.getDisplayOptions();
        if ((i2 & 4) != 0) {
            this.mDisplayHomeAsUpSet = true;
        }
        this.mActionView.setDisplayOptions((displayOptions & (i2 ^ -1)) | (i & i2));
    }

    public void setDisplayShowCustomEnabled(boolean z) {
        setDisplayOptions(z ? 16 : 0, 16);
    }

    public void setDisplayShowHomeEnabled(boolean z) {
        setDisplayOptions(z ? 2 : 0, 2);
    }

    public void setDisplayShowTitleEnabled(boolean z) {
        setDisplayOptions(z ? 8 : 0, 8);
    }

    public void setDisplayUseLogoEnabled(boolean z) {
        setDisplayOptions(z ? 1 : 0, 1);
    }

    public void setHomeAsUpIndicator(int i) {
        this.mActionView.setHomeAsUpIndicator(i);
    }

    public void setHomeAsUpIndicator(Drawable drawable) {
        this.mActionView.setHomeAsUpIndicator(drawable);
    }

    public void setHomeButtonEnabled(boolean z) {
        this.mActionView.setHomeButtonEnabled(z);
    }

    public void setIcon(int i) {
        this.mActionView.setIcon(i);
    }

    public void setIcon(Drawable drawable) {
        this.mActionView.setIcon(drawable);
    }

    public void setListNavigationCallbacks(SpinnerAdapter spinnerAdapter, OnNavigationListener onNavigationListener) {
        this.mActionView.setDropdownAdapter(spinnerAdapter);
        this.mActionView.setCallback(onNavigationListener);
    }

    public void setLogo(int i) {
        this.mActionView.setLogo(i);
    }

    public void setLogo(Drawable drawable) {
        this.mActionView.setLogo(drawable);
    }

    public void setNavigationMode(int i) {
        boolean z = false;
        switch (this.mActionView.getNavigationMode()) {
            case 2:
                this.mSavedTabPosition = getSelectedNavigationIndex();
                selectTab(null);
                this.mTabScrollView.setVisibility(8);
                break;
        }
        this.mActionView.setNavigationMode(i);
        switch (i) {
            case 2:
                ensureTabsExist();
                this.mTabScrollView.setVisibility(0);
                if (this.mSavedTabPosition != -1) {
                    setSelectedNavigationItem(this.mSavedTabPosition);
                    this.mSavedTabPosition = -1;
                    break;
                }
                break;
        }
        ActionBarView actionBarView = this.mActionView;
        if (i == 2 && !this.mHasEmbeddedTabs) {
            z = true;
        }
        actionBarView.setCollapsable(z);
    }

    public void setSelectedNavigationItem(int i) {
        switch (this.mActionView.getNavigationMode()) {
            case 1:
                this.mActionView.setDropdownSelectedPosition(i);
                return;
            case 2:
                selectTab((Tab) this.mTabs.get(i));
                return;
            default:
                throw new IllegalStateException("setSelectedNavigationIndex not valid for current navigation mode");
        }
    }

    public void setShowHideAnimationEnabled(boolean z) {
        this.mShowHideAnimationEnabled = z;
        if (!z) {
            this.mTopVisibilityView.clearAnimation();
            if (this.mSplitView != null) {
                this.mSplitView.clearAnimation();
            }
        }
    }

    public void setSplitBackgroundDrawable(Drawable drawable) {
        this.mContainerView.setSplitBackground(drawable);
    }

    public void setStackedBackgroundDrawable(Drawable drawable) {
        this.mContainerView.setStackedBackground(drawable);
    }

    public void setSubtitle(int i) {
        setSubtitle(this.mContext.getString(i));
    }

    public void setSubtitle(CharSequence charSequence) {
        this.mActionView.setSubtitle(charSequence);
    }

    public void setTitle(int i) {
        setTitle(this.mContext.getString(i));
    }

    public void setTitle(CharSequence charSequence) {
        this.mActionView.setTitle(charSequence);
    }

    public void show() {
        if (this.mHiddenByApp) {
            this.mHiddenByApp = false;
            updateVisibility(false);
        }
    }

    void showForActionMode() {
        if (!this.mShowingForMode) {
            this.mShowingForMode = true;
            updateVisibility(false);
        }
    }

    public ActionMode startActionMode(Callback callback) {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        this.mContextView.killMode();
        ActionMode actionModeImpl = new ActionModeImpl(callback);
        if (!actionModeImpl.dispatchOnCreate()) {
            return null;
        }
        actionModeImpl.invalidate();
        this.mContextView.initForMode(actionModeImpl);
        animateToMode(true);
        if (!(this.mSplitView == null || this.mContextDisplayMode != 1 || this.mSplitView.getVisibility() == 0)) {
            this.mSplitView.setVisibility(0);
        }
        this.mContextView.sendAccessibilityEvent(32);
        this.mActionMode = actionModeImpl;
        return actionModeImpl;
    }
}
