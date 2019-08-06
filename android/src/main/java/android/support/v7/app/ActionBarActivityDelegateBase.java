package android.support.v7.app;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.menu.ListMenuPresenter;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.MenuPresenter.Callback;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.internal.view.menu.MenuWrapperFactory;
import android.support.v7.internal.widget.ActionBarContainer;
import android.support.v7.internal.widget.ActionBarContextView;
import android.support.v7.internal.widget.ActionBarView;
import android.support.v7.internal.widget.ProgressBarICS;
import android.support.v7.view.ActionMode;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

class ActionBarActivityDelegateBase extends ActionBarActivityDelegate implements Callback, MenuBuilder.Callback {
    private static final int[] ACTION_BAR_DRAWABLE_TOGGLE_ATTRS = new int[]{R.attr.homeAsUpIndicator};
    private static final String TAG = "ActionBarActivityDelegateBase";
    private ActionBarView mActionBarView;
    private ActionMode mActionMode;
    private boolean mClosingActionMenu;
    private boolean mFeatureIndeterminateProgress;
    private boolean mFeatureProgress;
    private ListMenuPresenter mListMenuPresenter;
    private MenuBuilder mMenu;
    private Bundle mPanelFrozenActionViewState;
    private boolean mPanelIsPrepared;
    private boolean mPanelRefreshContent;
    private boolean mSubDecorInstalled;
    private CharSequence mTitleToSet;

    private class ActionModeCallbackWrapper implements ActionMode.Callback {
        private ActionMode.Callback mWrapped;

        public ActionModeCallbackWrapper(ActionMode.Callback callback) {
            this.mWrapped = callback;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return this.mWrapped.onActionItemClicked(actionMode, menuItem);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrapped.onCreateActionMode(actionMode, menu);
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            this.mWrapped.onDestroyActionMode(actionMode);
            ActionBarActivityDelegateBase.this.mActivity.onSupportActionModeFinished(actionMode);
            ActionBarActivityDelegateBase.this.mActionMode = null;
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrapped.onPrepareActionMode(actionMode, menu);
        }
    }

    ActionBarActivityDelegateBase(ActionBarActivity actionBarActivity) {
        super(actionBarActivity);
    }

    private void applyFixedSizeWindow() {
        TypedValue typedValue;
        TypedValue typedValue2;
        TypedValue typedValue3;
        int dimension;
        int dimension2;
        TypedValue typedValue4 = null;
        TypedArray obtainStyledAttributes = this.mActivity.obtainStyledAttributes(R.styleable.ActionBarWindow);
        if (obtainStyledAttributes.hasValue(3)) {
            typedValue = new TypedValue();
            obtainStyledAttributes.getValue(3, typedValue);
        } else {
            typedValue = null;
        }
        if (obtainStyledAttributes.hasValue(5)) {
            typedValue2 = new TypedValue();
            obtainStyledAttributes.getValue(5, typedValue2);
        } else {
            typedValue2 = null;
        }
        if (obtainStyledAttributes.hasValue(6)) {
            typedValue3 = new TypedValue();
            obtainStyledAttributes.getValue(6, typedValue3);
        } else {
            typedValue3 = null;
        }
        if (obtainStyledAttributes.hasValue(4)) {
            typedValue4 = new TypedValue();
            obtainStyledAttributes.getValue(4, typedValue4);
        }
        DisplayMetrics displayMetrics = this.mActivity.getResources().getDisplayMetrics();
        Object obj = displayMetrics.widthPixels < displayMetrics.heightPixels ? 1 : null;
        if (obj == null) {
            typedValue2 = typedValue;
        }
        if (!(typedValue2 == null || typedValue2.type == 0)) {
            if (typedValue2.type == 5) {
                dimension = (int) typedValue2.getDimension(displayMetrics);
            } else if (typedValue2.type == 6) {
                dimension = (int) typedValue2.getFraction((float) displayMetrics.widthPixels, (float) displayMetrics.widthPixels);
            }
            if (obj == null) {
                typedValue3 = typedValue4;
            }
            if (!(typedValue3 == null || typedValue3.type == 0)) {
                if (typedValue3.type != 5) {
                    dimension2 = (int) typedValue3.getDimension(displayMetrics);
                } else if (typedValue3.type == 6) {
                    dimension2 = (int) typedValue3.getFraction((float) displayMetrics.heightPixels, (float) displayMetrics.heightPixels);
                }
                if (!(dimension == -1 && dimension2 == -1)) {
                    this.mActivity.getWindow().setLayout(dimension, dimension2);
                }
                obtainStyledAttributes.recycle();
            }
            dimension2 = -1;
            this.mActivity.getWindow().setLayout(dimension, dimension2);
            obtainStyledAttributes.recycle();
        }
        dimension = -1;
        if (obj == null) {
            typedValue3 = typedValue4;
        }
        if (typedValue3.type != 5) {
            if (typedValue3.type == 6) {
                dimension2 = (int) typedValue3.getFraction((float) displayMetrics.heightPixels, (float) displayMetrics.heightPixels);
            }
            dimension2 = -1;
        } else {
            dimension2 = (int) typedValue3.getDimension(displayMetrics);
        }
        this.mActivity.getWindow().setLayout(dimension, dimension2);
        obtainStyledAttributes.recycle();
    }

    private ProgressBarICS getCircularProgressBar() {
        ProgressBarICS progressBarICS = (ProgressBarICS) this.mActionBarView.findViewById(R.id.progress_circular);
        if (progressBarICS != null) {
            progressBarICS.setVisibility(4);
        }
        return progressBarICS;
    }

    private ProgressBarICS getHorizontalProgressBar() {
        ProgressBarICS progressBarICS = (ProgressBarICS) this.mActionBarView.findViewById(R.id.progress_horizontal);
        if (progressBarICS != null) {
            progressBarICS.setVisibility(4);
        }
        return progressBarICS;
    }

    private MenuView getListMenuView(Context context, Callback callback) {
        if (this.mMenu == null) {
            return null;
        }
        if (this.mListMenuPresenter == null) {
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(R.styleable.Theme);
            int resourceId = obtainStyledAttributes.getResourceId(4, R.style.Theme_AppCompat_CompactMenu);
            obtainStyledAttributes.recycle();
            this.mListMenuPresenter = new ListMenuPresenter(R.layout.abc_list_menu_item_layout, resourceId);
            this.mListMenuPresenter.setCallback(callback);
            this.mMenu.addMenuPresenter(this.mListMenuPresenter);
        } else {
            this.mListMenuPresenter.updateMenuView(false);
        }
        return this.mListMenuPresenter.getMenuView(new FrameLayout(context));
    }

    private void hideProgressBars(ProgressBarICS progressBarICS, ProgressBarICS progressBarICS2) {
        if (this.mFeatureIndeterminateProgress && progressBarICS2.getVisibility() == 0) {
            progressBarICS2.setVisibility(4);
        }
        if (this.mFeatureProgress && progressBarICS.getVisibility() == 0) {
            progressBarICS.setVisibility(4);
        }
    }

    private boolean initializePanelMenu() {
        this.mMenu = new MenuBuilder(getActionBarThemedContext());
        this.mMenu.setCallback(this);
        return true;
    }

    private boolean preparePanel() {
        if (this.mPanelIsPrepared) {
            return true;
        }
        if (this.mMenu == null || this.mPanelRefreshContent) {
            if (this.mMenu == null && (!initializePanelMenu() || this.mMenu == null)) {
                return false;
            }
            if (this.mActionBarView != null) {
                this.mActionBarView.setMenu(this.mMenu, this);
            }
            this.mMenu.stopDispatchingItemsChanged();
            if (this.mActivity.superOnCreatePanelMenu(0, this.mMenu)) {
                this.mPanelRefreshContent = false;
            } else {
                this.mMenu = null;
                if (this.mActionBarView != null) {
                    this.mActionBarView.setMenu(null, this);
                }
                return false;
            }
        }
        this.mMenu.stopDispatchingItemsChanged();
        if (this.mPanelFrozenActionViewState != null) {
            this.mMenu.restoreActionViewStates(this.mPanelFrozenActionViewState);
            this.mPanelFrozenActionViewState = null;
        }
        if (this.mActivity.superOnPreparePanel(0, null, this.mMenu)) {
            this.mMenu.startDispatchingItemsChanged();
            this.mPanelIsPrepared = true;
            return true;
        }
        if (this.mActionBarView != null) {
            this.mActionBarView.setMenu(null, this);
        }
        this.mMenu.startDispatchingItemsChanged();
        return false;
    }

    private void reopenMenu(MenuBuilder menuBuilder, boolean z) {
        if (this.mActionBarView == null || !this.mActionBarView.isOverflowReserved()) {
            menuBuilder.close();
        } else if (this.mActionBarView.isOverflowMenuShowing() && z) {
            this.mActionBarView.hideOverflowMenu();
        } else if (this.mActionBarView.getVisibility() == 0) {
            this.mActionBarView.showOverflowMenu();
        }
    }

    private void showProgressBars(ProgressBarICS progressBarICS, ProgressBarICS progressBarICS2) {
        if (this.mFeatureIndeterminateProgress && progressBarICS2.getVisibility() == 4) {
            progressBarICS2.setVisibility(0);
        }
        if (this.mFeatureProgress && progressBarICS.getProgress() < 10000) {
            progressBarICS.setVisibility(0);
        }
    }

    private void updateProgressBars(int i) {
        ProgressBarICS circularProgressBar = getCircularProgressBar();
        ProgressBarICS horizontalProgressBar = getHorizontalProgressBar();
        if (i == -1) {
            if (this.mFeatureProgress) {
                int i2 = (horizontalProgressBar.isIndeterminate() || horizontalProgressBar.getProgress() < 10000) ? 0 : 4;
                horizontalProgressBar.setVisibility(i2);
            }
            if (this.mFeatureIndeterminateProgress) {
                circularProgressBar.setVisibility(0);
            }
        } else if (i == -2) {
            if (this.mFeatureProgress) {
                horizontalProgressBar.setVisibility(8);
            }
            if (this.mFeatureIndeterminateProgress) {
                circularProgressBar.setVisibility(8);
            }
        } else if (i == -3) {
            horizontalProgressBar.setIndeterminate(true);
        } else if (i == -4) {
            horizontalProgressBar.setIndeterminate(false);
        } else if (i >= 0 && i <= 10000) {
            horizontalProgressBar.setProgress(i + 0);
            if (i < 10000) {
                showProgressBars(horizontalProgressBar, circularProgressBar);
            } else {
                hideProgressBars(horizontalProgressBar, circularProgressBar);
            }
        }
    }

    public void addContentView(View view, LayoutParams layoutParams) {
        ensureSubDecor();
        ((ViewGroup) this.mActivity.findViewById(16908290)).addView(view, layoutParams);
        this.mActivity.onSupportContentChanged();
    }

    public ActionBar createSupportActionBar() {
        ensureSubDecor();
        return new ActionBarImplBase(this.mActivity, this.mActivity);
    }

    final void ensureSubDecor() {
        if (!this.mSubDecorInstalled) {
            if (this.mHasActionBar) {
                boolean z;
                if (this.mOverlayActionBar) {
                    this.mActivity.superSetContentView(R.layout.abc_action_bar_decor_overlay);
                } else {
                    this.mActivity.superSetContentView(R.layout.abc_action_bar_decor);
                }
                this.mActionBarView = (ActionBarView) this.mActivity.findViewById(R.id.action_bar);
                this.mActionBarView.setWindowCallback(this.mActivity);
                if (this.mFeatureProgress) {
                    this.mActionBarView.initProgress();
                }
                if (this.mFeatureIndeterminateProgress) {
                    this.mActionBarView.initIndeterminateProgress();
                }
                boolean equals = "splitActionBarWhenNarrow".equals(getUiOptionsFromMetadata());
                if (equals) {
                    z = this.mActivity.getResources().getBoolean(R.bool.abc_split_action_bar_is_narrow);
                } else {
                    TypedArray obtainStyledAttributes = this.mActivity.obtainStyledAttributes(R.styleable.ActionBarWindow);
                    boolean z2 = obtainStyledAttributes.getBoolean(2, false);
                    obtainStyledAttributes.recycle();
                    z = z2;
                }
                ActionBarContainer actionBarContainer = (ActionBarContainer) this.mActivity.findViewById(R.id.split_action_bar);
                if (actionBarContainer != null) {
                    this.mActionBarView.setSplitView(actionBarContainer);
                    this.mActionBarView.setSplitActionBar(z);
                    this.mActionBarView.setSplitWhenNarrow(equals);
                    ActionBarContextView actionBarContextView = (ActionBarContextView) this.mActivity.findViewById(R.id.action_context_bar);
                    actionBarContextView.setSplitView(actionBarContainer);
                    actionBarContextView.setSplitActionBar(z);
                    actionBarContextView.setSplitWhenNarrow(equals);
                }
            } else {
                this.mActivity.superSetContentView(R.layout.abc_simple_decor);
            }
            this.mActivity.findViewById(16908290).setId(-1);
            this.mActivity.findViewById(R.id.action_bar_activity_content).setId(16908290);
            if (this.mTitleToSet != null) {
                this.mActionBarView.setWindowTitle(this.mTitleToSet);
                this.mTitleToSet = null;
            }
            applyFixedSizeWindow();
            this.mSubDecorInstalled = true;
            this.mActivity.getWindow().getDecorView().post(new Runnable() {
                public void run() {
                    ActionBarActivityDelegateBase.this.supportInvalidateOptionsMenu();
                }
            });
        }
    }

    int getHomeAsUpIndicatorAttrId() {
        return R.attr.homeAsUpIndicator;
    }

    public boolean onBackPressed() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            return true;
        } else if (this.mActionBarView == null || !this.mActionBarView.hasExpandedActionView()) {
            return false;
        } else {
            this.mActionBarView.collapseActionView();
            return true;
        }
    }

    public void onCloseMenu(MenuBuilder menuBuilder, boolean z) {
        if (!this.mClosingActionMenu) {
            this.mClosingActionMenu = true;
            this.mActivity.closeOptionsMenu();
            this.mActionBarView.dismissPopupMenus();
            this.mClosingActionMenu = false;
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mHasActionBar && this.mSubDecorInstalled) {
            ((ActionBarImplBase) getSupportActionBar()).onConfigurationChanged(configuration);
        }
    }

    public void onContentChanged() {
    }

    public boolean onCreatePanelMenu(int i, Menu menu) {
        return i != 0 ? this.mActivity.superOnCreatePanelMenu(i, menu) : false;
    }

    public View onCreatePanelView(int i) {
        return (i == 0 && preparePanel()) ? (View) getListMenuView(this.mActivity, this) : null;
    }

    public boolean onMenuItemSelected(int i, MenuItem menuItem) {
        if (i == 0) {
            menuItem = MenuWrapperFactory.createMenuItemWrapper(menuItem);
        }
        return this.mActivity.superOnMenuItemSelected(i, menuItem);
    }

    public boolean onMenuItemSelected(MenuBuilder menuBuilder, MenuItem menuItem) {
        return this.mActivity.onMenuItemSelected(0, menuItem);
    }

    public void onMenuModeChange(MenuBuilder menuBuilder) {
        reopenMenu(menuBuilder, true);
    }

    public boolean onOpenSubMenu(MenuBuilder menuBuilder) {
        return false;
    }

    public void onPostResume() {
        ActionBarImplBase actionBarImplBase = (ActionBarImplBase) getSupportActionBar();
        if (actionBarImplBase != null) {
            actionBarImplBase.setShowHideAnimationEnabled(true);
        }
    }

    public boolean onPreparePanel(int i, View view, Menu menu) {
        return i != 0 ? this.mActivity.superOnPreparePanel(i, view, menu) : false;
    }

    public void onStop() {
        ActionBarImplBase actionBarImplBase = (ActionBarImplBase) getSupportActionBar();
        if (actionBarImplBase != null) {
            actionBarImplBase.setShowHideAnimationEnabled(false);
        }
    }

    public void onTitleChanged(CharSequence charSequence) {
        if (this.mActionBarView != null) {
            this.mActionBarView.setWindowTitle(charSequence);
        } else {
            this.mTitleToSet = charSequence;
        }
    }

    public void setContentView(int i) {
        ensureSubDecor();
        ViewGroup viewGroup = (ViewGroup) this.mActivity.findViewById(16908290);
        viewGroup.removeAllViews();
        this.mActivity.getLayoutInflater().inflate(i, viewGroup);
        this.mActivity.onSupportContentChanged();
    }

    public void setContentView(View view) {
        ensureSubDecor();
        ViewGroup viewGroup = (ViewGroup) this.mActivity.findViewById(16908290);
        viewGroup.removeAllViews();
        viewGroup.addView(view);
        this.mActivity.onSupportContentChanged();
    }

    public void setContentView(View view, LayoutParams layoutParams) {
        ensureSubDecor();
        ViewGroup viewGroup = (ViewGroup) this.mActivity.findViewById(16908290);
        viewGroup.removeAllViews();
        viewGroup.addView(view, layoutParams);
        this.mActivity.onSupportContentChanged();
    }

    void setSupportProgress(int i) {
        updateProgressBars(i + 0);
    }

    void setSupportProgressBarIndeterminate(boolean z) {
        updateProgressBars(z ? -3 : -4);
    }

    void setSupportProgressBarIndeterminateVisibility(boolean z) {
        updateProgressBars(z ? -1 : -2);
    }

    void setSupportProgressBarVisibility(boolean z) {
        updateProgressBars(z ? -1 : -2);
    }

    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("ActionMode callback can not be null.");
        }
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        ActionMode.Callback actionModeCallbackWrapper = new ActionModeCallbackWrapper(callback);
        ActionBarImplBase actionBarImplBase = (ActionBarImplBase) getSupportActionBar();
        if (actionBarImplBase != null) {
            this.mActionMode = actionBarImplBase.startActionMode(actionModeCallbackWrapper);
        }
        if (this.mActionMode != null) {
            this.mActivity.onSupportActionModeStarted(this.mActionMode);
        }
        return this.mActionMode;
    }

    public void supportInvalidateOptionsMenu() {
        if (this.mMenu != null) {
            Bundle bundle = new Bundle();
            this.mMenu.saveActionViewStates(bundle);
            if (bundle.size() > 0) {
                this.mPanelFrozenActionViewState = bundle;
            }
            this.mMenu.stopDispatchingItemsChanged();
            this.mMenu.clear();
        }
        this.mPanelRefreshContent = true;
        if (this.mActionBarView != null) {
            this.mPanelIsPrepared = false;
            preparePanel();
        }
    }

    public boolean supportRequestWindowFeature(int i) {
        switch (i) {
            case 2:
                this.mFeatureProgress = true;
                return true;
            case 5:
                this.mFeatureIndeterminateProgress = true;
                return true;
            case 8:
                this.mHasActionBar = true;
                return true;
            case 9:
                this.mOverlayActionBar = true;
                return true;
            default:
                return this.mActivity.requestWindowFeature(i);
        }
    }
}
