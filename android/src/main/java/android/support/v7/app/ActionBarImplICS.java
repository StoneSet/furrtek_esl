package android.support.v7.app;

import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.app.ActionBar.OnNavigationListener;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.ActionBar.Tab;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

class ActionBarImplICS extends ActionBar {
    final ActionBar mActionBar;
    FragmentTransaction mActiveTransaction;
    final Activity mActivity;
    private ArrayList<WeakReference<OnMenuVisibilityListenerWrapper>> mAddedMenuVisWrappers;
    final Callback mCallback;
    private ImageView mHomeActionView;

    static class OnMenuVisibilityListenerWrapper implements OnMenuVisibilityListener {
        final ActionBar.OnMenuVisibilityListener mWrappedListener;

        public OnMenuVisibilityListenerWrapper(ActionBar.OnMenuVisibilityListener onMenuVisibilityListener) {
            this.mWrappedListener = onMenuVisibilityListener;
        }

        public void onMenuVisibilityChanged(boolean z) {
            this.mWrappedListener.onMenuVisibilityChanged(z);
        }
    }

    static class OnNavigationListenerWrapper implements OnNavigationListener {
        private final ActionBar.OnNavigationListener mWrappedListener;

        public OnNavigationListenerWrapper(ActionBar.OnNavigationListener onNavigationListener) {
            this.mWrappedListener = onNavigationListener;
        }

        public boolean onNavigationItemSelected(int i, long j) {
            return this.mWrappedListener.onNavigationItemSelected(i, j);
        }
    }

    class TabWrapper extends Tab implements TabListener {
        private CharSequence mContentDescription;
        private ActionBar.TabListener mTabListener;
        private Object mTag;
        final ActionBar.Tab mWrappedTab;

        public TabWrapper(ActionBar.Tab tab) {
            this.mWrappedTab = tab;
        }

        public CharSequence getContentDescription() {
            return this.mContentDescription;
        }

        public View getCustomView() {
            return this.mWrappedTab.getCustomView();
        }

        public Drawable getIcon() {
            return this.mWrappedTab.getIcon();
        }

        public int getPosition() {
            return this.mWrappedTab.getPosition();
        }

        public Object getTag() {
            return this.mTag;
        }

        public CharSequence getText() {
            return this.mWrappedTab.getText();
        }

        public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
            this.mTabListener.onTabReselected(this, fragmentTransaction != null ? ActionBarImplICS.this.getActiveTransaction() : null);
            ActionBarImplICS.this.commitActiveTransaction();
        }

        public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
            this.mTabListener.onTabSelected(this, fragmentTransaction != null ? ActionBarImplICS.this.getActiveTransaction() : null);
            ActionBarImplICS.this.commitActiveTransaction();
        }

        public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
            this.mTabListener.onTabUnselected(this, fragmentTransaction != null ? ActionBarImplICS.this.getActiveTransaction() : null);
        }

        public void select() {
            this.mWrappedTab.select();
        }

        public Tab setContentDescription(int i) {
            this.mContentDescription = ActionBarImplICS.this.mActivity.getText(i);
            return this;
        }

        public Tab setContentDescription(CharSequence charSequence) {
            this.mContentDescription = charSequence;
            return this;
        }

        public Tab setCustomView(int i) {
            this.mWrappedTab.setCustomView(i);
            return this;
        }

        public Tab setCustomView(View view) {
            this.mWrappedTab.setCustomView(view);
            return this;
        }

        public Tab setIcon(int i) {
            this.mWrappedTab.setIcon(i);
            return this;
        }

        public Tab setIcon(Drawable drawable) {
            this.mWrappedTab.setIcon(drawable);
            return this;
        }

        public Tab setTabListener(ActionBar.TabListener tabListener) {
            this.mTabListener = tabListener;
            this.mWrappedTab.setTabListener(tabListener != null ? this : null);
            return this;
        }

        public Tab setTag(Object obj) {
            this.mTag = obj;
            return this;
        }

        public Tab setText(int i) {
            this.mWrappedTab.setText(i);
            return this;
        }

        public Tab setText(CharSequence charSequence) {
            this.mWrappedTab.setText(charSequence);
            return this;
        }
    }

    public ActionBarImplICS(Activity activity, Callback callback) {
        this(activity, callback, true);
    }

    ActionBarImplICS(Activity activity, Callback callback, boolean z) {
        this.mAddedMenuVisWrappers = new ArrayList();
        this.mActivity = activity;
        this.mCallback = callback;
        this.mActionBar = activity.getActionBar();
        if (z && (getDisplayOptions() & 4) != 0) {
            setHomeButtonEnabled(true);
        }
    }

    private OnMenuVisibilityListenerWrapper findAndRemoveMenuVisWrapper(ActionBar.OnMenuVisibilityListener onMenuVisibilityListener) {
        int i = 0;
        while (i < this.mAddedMenuVisWrappers.size()) {
            OnMenuVisibilityListenerWrapper onMenuVisibilityListenerWrapper = (OnMenuVisibilityListenerWrapper) ((WeakReference) this.mAddedMenuVisWrappers.get(i)).get();
            if (onMenuVisibilityListenerWrapper == null) {
                int i2 = i - 1;
                this.mAddedMenuVisWrappers.remove(i);
                i = i2;
            } else if (onMenuVisibilityListenerWrapper.mWrappedListener == onMenuVisibilityListener) {
                this.mAddedMenuVisWrappers.remove(i);
                return onMenuVisibilityListenerWrapper;
            }
            i++;
        }
        return null;
    }

    public void addOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener onMenuVisibilityListener) {
        if (onMenuVisibilityListener != null) {
            OnMenuVisibilityListener onMenuVisibilityListenerWrapper = new OnMenuVisibilityListenerWrapper(onMenuVisibilityListener);
            this.mAddedMenuVisWrappers.add(new WeakReference(onMenuVisibilityListenerWrapper));
            this.mActionBar.addOnMenuVisibilityListener(onMenuVisibilityListenerWrapper);
        }
    }

    public void addTab(Tab tab) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab);
    }

    public void addTab(Tab tab, int i) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab, i);
    }

    public void addTab(Tab tab, int i, boolean z) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab, i, z);
    }

    public void addTab(Tab tab, boolean z) {
        this.mActionBar.addTab(((TabWrapper) tab).mWrappedTab, z);
    }

    void commitActiveTransaction() {
        if (!(this.mActiveTransaction == null || this.mActiveTransaction.isEmpty())) {
            this.mActiveTransaction.commit();
        }
        this.mActiveTransaction = null;
    }

    FragmentTransaction getActiveTransaction() {
        if (this.mActiveTransaction == null) {
            this.mActiveTransaction = this.mCallback.getSupportFragmentManager().beginTransaction().disallowAddToBackStack();
        }
        return this.mActiveTransaction;
    }

    public View getCustomView() {
        return this.mActionBar.getCustomView();
    }

    public int getDisplayOptions() {
        return this.mActionBar.getDisplayOptions();
    }

    public int getHeight() {
        return this.mActionBar.getHeight();
    }

    ImageView getHomeActionView() {
        if (this.mHomeActionView == null) {
            View findViewById = this.mActivity.findViewById(16908332);
            if (findViewById != null) {
                ViewGroup viewGroup = (ViewGroup) findViewById.getParent();
                if (viewGroup.getChildCount() == 2) {
                    View childAt = viewGroup.getChildAt(0);
                    findViewById = viewGroup.getChildAt(1);
                    if (childAt.getId() != 16908332) {
                        findViewById = childAt;
                    }
                    if (findViewById instanceof ImageView) {
                        this.mHomeActionView = (ImageView) findViewById;
                    }
                }
            }
            return null;
        }
        return this.mHomeActionView;
    }

    public int getNavigationItemCount() {
        return this.mActionBar.getNavigationItemCount();
    }

    public int getNavigationMode() {
        return this.mActionBar.getNavigationMode();
    }

    public int getSelectedNavigationIndex() {
        return this.mActionBar.getSelectedNavigationIndex();
    }

    public Tab getSelectedTab() {
        return (Tab) this.mActionBar.getSelectedTab().getTag();
    }

    public CharSequence getSubtitle() {
        return this.mActionBar.getSubtitle();
    }

    public Tab getTabAt(int i) {
        return (Tab) this.mActionBar.getTabAt(i).getTag();
    }

    public int getTabCount() {
        return this.mActionBar.getTabCount();
    }

    Drawable getThemeDefaultUpIndicator() {
        TypedArray obtainStyledAttributes = this.mActivity.obtainStyledAttributes(new int[]{16843531});
        Drawable drawable = obtainStyledAttributes.getDrawable(0);
        obtainStyledAttributes.recycle();
        return drawable;
    }

    public Context getThemedContext() {
        return this.mActionBar.getThemedContext();
    }

    public CharSequence getTitle() {
        return this.mActionBar.getTitle();
    }

    public void hide() {
        this.mActionBar.hide();
    }

    public boolean isShowing() {
        return this.mActionBar.isShowing();
    }

    public Tab newTab() {
        ActionBar.Tab newTab = this.mActionBar.newTab();
        Tab tabWrapper = new TabWrapper(newTab);
        newTab.setTag(tabWrapper);
        return tabWrapper;
    }

    public void removeAllTabs() {
        this.mActionBar.removeAllTabs();
    }

    public void removeOnMenuVisibilityListener(ActionBar.OnMenuVisibilityListener onMenuVisibilityListener) {
        this.mActionBar.removeOnMenuVisibilityListener(findAndRemoveMenuVisWrapper(onMenuVisibilityListener));
    }

    public void removeTab(Tab tab) {
        this.mActionBar.removeTab(((TabWrapper) tab).mWrappedTab);
    }

    public void removeTabAt(int i) {
        this.mActionBar.removeTabAt(i);
    }

    public void selectTab(Tab tab) {
        this.mActionBar.selectTab(((TabWrapper) tab).mWrappedTab);
    }

    public void setBackgroundDrawable(Drawable drawable) {
        this.mActionBar.setBackgroundDrawable(drawable);
    }

    public void setCustomView(int i) {
        this.mActionBar.setCustomView(i);
    }

    public void setCustomView(View view) {
        this.mActionBar.setCustomView(view);
    }

    public void setCustomView(View view, LayoutParams layoutParams) {
        ActionBar.LayoutParams layoutParams2 = new ActionBar.LayoutParams(layoutParams);
        layoutParams2.gravity = layoutParams.gravity;
        this.mActionBar.setCustomView(view, layoutParams2);
    }

    public void setDisplayHomeAsUpEnabled(boolean z) {
        this.mActionBar.setDisplayHomeAsUpEnabled(z);
    }

    public void setDisplayOptions(int i) {
        this.mActionBar.setDisplayOptions(i);
    }

    public void setDisplayOptions(int i, int i2) {
        this.mActionBar.setDisplayOptions(i, i2);
    }

    public void setDisplayShowCustomEnabled(boolean z) {
        this.mActionBar.setDisplayShowCustomEnabled(z);
    }

    public void setDisplayShowHomeEnabled(boolean z) {
        this.mActionBar.setDisplayShowHomeEnabled(z);
    }

    public void setDisplayShowTitleEnabled(boolean z) {
        this.mActionBar.setDisplayShowTitleEnabled(z);
    }

    public void setDisplayUseLogoEnabled(boolean z) {
        this.mActionBar.setDisplayUseLogoEnabled(z);
    }

    public void setHomeAsUpIndicator(int i) {
        ImageView homeActionView = getHomeActionView();
        if (homeActionView == null) {
            return;
        }
        if (i != 0) {
            homeActionView.setImageResource(i);
        } else {
            homeActionView.setImageDrawable(getThemeDefaultUpIndicator());
        }
    }

    public void setHomeAsUpIndicator(Drawable drawable) {
        ImageView homeActionView = getHomeActionView();
        if (homeActionView != null) {
            if (drawable == null) {
                drawable = getThemeDefaultUpIndicator();
            }
            homeActionView.setImageDrawable(drawable);
        }
    }

    public void setHomeButtonEnabled(boolean z) {
        this.mActionBar.setHomeButtonEnabled(z);
    }

    public void setIcon(int i) {
        this.mActionBar.setIcon(i);
    }

    public void setIcon(Drawable drawable) {
        this.mActionBar.setIcon(drawable);
    }

    public void setListNavigationCallbacks(SpinnerAdapter spinnerAdapter, ActionBar.OnNavigationListener onNavigationListener) {
        this.mActionBar.setListNavigationCallbacks(spinnerAdapter, onNavigationListener != null ? new OnNavigationListenerWrapper(onNavigationListener) : null);
    }

    public void setLogo(int i) {
        this.mActionBar.setLogo(i);
    }

    public void setLogo(Drawable drawable) {
        this.mActionBar.setLogo(drawable);
    }

    public void setNavigationMode(int i) {
        this.mActionBar.setNavigationMode(i);
    }

    public void setSelectedNavigationItem(int i) {
        this.mActionBar.setSelectedNavigationItem(i);
    }

    public void setSplitBackgroundDrawable(Drawable drawable) {
        this.mActionBar.setSplitBackgroundDrawable(drawable);
    }

    public void setStackedBackgroundDrawable(Drawable drawable) {
        this.mActionBar.setStackedBackgroundDrawable(drawable);
    }

    public void setSubtitle(int i) {
        this.mActionBar.setSubtitle(i);
    }

    public void setSubtitle(CharSequence charSequence) {
        this.mActionBar.setSubtitle(charSequence);
    }

    public void setTitle(int i) {
        this.mActionBar.setTitle(i);
    }

    public void setTitle(CharSequence charSequence) {
        this.mActionBar.setTitle(charSequence);
    }

    public void show() {
        this.mActionBar.show();
    }
}
