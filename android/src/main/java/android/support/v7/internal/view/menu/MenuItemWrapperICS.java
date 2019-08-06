package android.support.v7.internal.view.menu;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.ActionProvider.VisibilityListener;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.util.Log;
import android.view.ActionProvider;
import android.view.CollapsibleActionView;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.widget.FrameLayout;
import java.lang.reflect.Method;

public class MenuItemWrapperICS extends BaseMenuWrapper<MenuItem> implements SupportMenuItem {
    static final String LOG_TAG = "MenuItemWrapper";
    private final boolean mEmulateProviderVisibilityOverride;
    private boolean mLastRequestVisible;
    private Method mSetExclusiveCheckableMethod;

    class ActionProviderWrapper extends ActionProvider {
        final android.support.v4.view.ActionProvider mInner;

        public ActionProviderWrapper(android.support.v4.view.ActionProvider actionProvider) {
            super(actionProvider.getContext());
            this.mInner = actionProvider;
            if (MenuItemWrapperICS.this.mEmulateProviderVisibilityOverride) {
                this.mInner.setVisibilityListener(new VisibilityListener(MenuItemWrapperICS.this) {
                    public void onActionProviderVisibilityChanged(boolean z) {
                        if (ActionProviderWrapper.this.mInner.overridesItemVisibility() && MenuItemWrapperICS.this.mLastRequestVisible) {
                            MenuItemWrapperICS.this.wrappedSetVisible(z);
                        }
                    }
                });
            }
        }

        public boolean hasSubMenu() {
            return this.mInner.hasSubMenu();
        }

        public View onCreateActionView() {
            if (MenuItemWrapperICS.this.mEmulateProviderVisibilityOverride) {
                MenuItemWrapperICS.this.checkActionProviderOverrideVisibility();
            }
            return this.mInner.onCreateActionView();
        }

        public boolean onPerformDefaultAction() {
            return this.mInner.onPerformDefaultAction();
        }

        public void onPrepareSubMenu(SubMenu subMenu) {
            this.mInner.onPrepareSubMenu(MenuItemWrapperICS.this.getSubMenuWrapper(subMenu));
        }
    }

    static class CollapsibleActionViewWrapper extends FrameLayout implements CollapsibleActionView {
        final android.support.v7.view.CollapsibleActionView mWrappedView;

        CollapsibleActionViewWrapper(View view) {
            super(view.getContext());
            this.mWrappedView = (android.support.v7.view.CollapsibleActionView) view;
            addView(view);
        }

        View getWrappedView() {
            return (View) this.mWrappedView;
        }

        public void onActionViewCollapsed() {
            this.mWrappedView.onActionViewCollapsed();
        }

        public void onActionViewExpanded() {
            this.mWrappedView.onActionViewExpanded();
        }
    }

    private class OnActionExpandListenerWrapper extends BaseWrapper<OnActionExpandListener> implements MenuItem.OnActionExpandListener {
        OnActionExpandListenerWrapper(OnActionExpandListener onActionExpandListener) {
            super(onActionExpandListener);
        }

        public boolean onMenuItemActionCollapse(MenuItem menuItem) {
            return ((OnActionExpandListener) this.mWrappedObject).onMenuItemActionCollapse(MenuItemWrapperICS.this.getMenuItemWrapper(menuItem));
        }

        public boolean onMenuItemActionExpand(MenuItem menuItem) {
            return ((OnActionExpandListener) this.mWrappedObject).onMenuItemActionExpand(MenuItemWrapperICS.this.getMenuItemWrapper(menuItem));
        }
    }

    private class OnMenuItemClickListenerWrapper extends BaseWrapper<OnMenuItemClickListener> implements OnMenuItemClickListener {
        OnMenuItemClickListenerWrapper(OnMenuItemClickListener onMenuItemClickListener) {
            super(onMenuItemClickListener);
        }

        public boolean onMenuItemClick(MenuItem menuItem) {
            return ((OnMenuItemClickListener) this.mWrappedObject).onMenuItemClick(MenuItemWrapperICS.this.getMenuItemWrapper(menuItem));
        }
    }

    MenuItemWrapperICS(MenuItem menuItem) {
        this(menuItem, true);
    }

    MenuItemWrapperICS(MenuItem menuItem, boolean z) {
        super(menuItem);
        this.mLastRequestVisible = menuItem.isVisible();
        this.mEmulateProviderVisibilityOverride = z;
    }

    final boolean checkActionProviderOverrideVisibility() {
        if (!this.mLastRequestVisible) {
            return false;
        }
        android.support.v4.view.ActionProvider supportActionProvider = getSupportActionProvider();
        if (supportActionProvider == null || !supportActionProvider.overridesItemVisibility() || supportActionProvider.isVisible()) {
            return false;
        }
        wrappedSetVisible(false);
        return true;
    }

    public boolean collapseActionView() {
        return ((MenuItem) this.mWrappedObject).collapseActionView();
    }

    ActionProviderWrapper createActionProviderWrapper(android.support.v4.view.ActionProvider actionProvider) {
        return new ActionProviderWrapper(actionProvider);
    }

    public boolean expandActionView() {
        return ((MenuItem) this.mWrappedObject).expandActionView();
    }

    public ActionProvider getActionProvider() {
        return ((MenuItem) this.mWrappedObject).getActionProvider();
    }

    public View getActionView() {
        View actionView = ((MenuItem) this.mWrappedObject).getActionView();
        return actionView instanceof CollapsibleActionViewWrapper ? ((CollapsibleActionViewWrapper) actionView).getWrappedView() : actionView;
    }

    public char getAlphabeticShortcut() {
        return ((MenuItem) this.mWrappedObject).getAlphabeticShortcut();
    }

    public int getGroupId() {
        return ((MenuItem) this.mWrappedObject).getGroupId();
    }

    public Drawable getIcon() {
        return ((MenuItem) this.mWrappedObject).getIcon();
    }

    public Intent getIntent() {
        return ((MenuItem) this.mWrappedObject).getIntent();
    }

    public int getItemId() {
        return ((MenuItem) this.mWrappedObject).getItemId();
    }

    public ContextMenuInfo getMenuInfo() {
        return ((MenuItem) this.mWrappedObject).getMenuInfo();
    }

    public char getNumericShortcut() {
        return ((MenuItem) this.mWrappedObject).getNumericShortcut();
    }

    public int getOrder() {
        return ((MenuItem) this.mWrappedObject).getOrder();
    }

    public SubMenu getSubMenu() {
        return getSubMenuWrapper(((MenuItem) this.mWrappedObject).getSubMenu());
    }

    public android.support.v4.view.ActionProvider getSupportActionProvider() {
        ActionProviderWrapper actionProviderWrapper = (ActionProviderWrapper) ((MenuItem) this.mWrappedObject).getActionProvider();
        return actionProviderWrapper != null ? actionProviderWrapper.mInner : null;
    }

    public CharSequence getTitle() {
        return ((MenuItem) this.mWrappedObject).getTitle();
    }

    public CharSequence getTitleCondensed() {
        return ((MenuItem) this.mWrappedObject).getTitleCondensed();
    }

    public boolean hasSubMenu() {
        return ((MenuItem) this.mWrappedObject).hasSubMenu();
    }

    public boolean isActionViewExpanded() {
        return ((MenuItem) this.mWrappedObject).isActionViewExpanded();
    }

    public boolean isCheckable() {
        return ((MenuItem) this.mWrappedObject).isCheckable();
    }

    public boolean isChecked() {
        return ((MenuItem) this.mWrappedObject).isChecked();
    }

    public boolean isEnabled() {
        return ((MenuItem) this.mWrappedObject).isEnabled();
    }

    public boolean isVisible() {
        return ((MenuItem) this.mWrappedObject).isVisible();
    }

    public MenuItem setActionProvider(ActionProvider actionProvider) {
        ((MenuItem) this.mWrappedObject).setActionProvider(actionProvider);
        if (actionProvider != null && this.mEmulateProviderVisibilityOverride) {
            checkActionProviderOverrideVisibility();
        }
        return this;
    }

    public MenuItem setActionView(int i) {
        ((MenuItem) this.mWrappedObject).setActionView(i);
        View actionView = ((MenuItem) this.mWrappedObject).getActionView();
        if (actionView instanceof android.support.v7.view.CollapsibleActionView) {
            ((MenuItem) this.mWrappedObject).setActionView(new CollapsibleActionViewWrapper(actionView));
        }
        return this;
    }

    public MenuItem setActionView(View view) {
        if (view instanceof android.support.v7.view.CollapsibleActionView) {
            view = new CollapsibleActionViewWrapper(view);
        }
        ((MenuItem) this.mWrappedObject).setActionView(view);
        return this;
    }

    public MenuItem setAlphabeticShortcut(char c) {
        ((MenuItem) this.mWrappedObject).setAlphabeticShortcut(c);
        return this;
    }

    public MenuItem setCheckable(boolean z) {
        ((MenuItem) this.mWrappedObject).setCheckable(z);
        return this;
    }

    public MenuItem setChecked(boolean z) {
        ((MenuItem) this.mWrappedObject).setChecked(z);
        return this;
    }

    public MenuItem setEnabled(boolean z) {
        ((MenuItem) this.mWrappedObject).setEnabled(z);
        return this;
    }

    public void setExclusiveCheckable(boolean z) {
        try {
            if (this.mSetExclusiveCheckableMethod == null) {
                this.mSetExclusiveCheckableMethod = ((MenuItem) this.mWrappedObject).getClass().getDeclaredMethod("setExclusiveCheckable", new Class[]{Boolean.TYPE});
            }
            this.mSetExclusiveCheckableMethod.invoke(this.mWrappedObject, new Object[]{Boolean.valueOf(z)});
        } catch (Throwable e) {
            Log.w(LOG_TAG, "Error while calling setExclusiveCheckable", e);
        }
    }

    public MenuItem setIcon(int i) {
        ((MenuItem) this.mWrappedObject).setIcon(i);
        return this;
    }

    public MenuItem setIcon(Drawable drawable) {
        ((MenuItem) this.mWrappedObject).setIcon(drawable);
        return this;
    }

    public MenuItem setIntent(Intent intent) {
        ((MenuItem) this.mWrappedObject).setIntent(intent);
        return this;
    }

    public MenuItem setNumericShortcut(char c) {
        ((MenuItem) this.mWrappedObject).setNumericShortcut(c);
        return this;
    }

    public MenuItem setOnActionExpandListener(MenuItem.OnActionExpandListener onActionExpandListener) {
        ((MenuItem) this.mWrappedObject).setOnActionExpandListener(onActionExpandListener);
        return this;
    }

    public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener onMenuItemClickListener) {
        ((MenuItem) this.mWrappedObject).setOnMenuItemClickListener(onMenuItemClickListener != null ? new OnMenuItemClickListenerWrapper(onMenuItemClickListener) : null);
        return this;
    }

    public MenuItem setShortcut(char c, char c2) {
        ((MenuItem) this.mWrappedObject).setShortcut(c, c2);
        return this;
    }

    public void setShowAsAction(int i) {
        ((MenuItem) this.mWrappedObject).setShowAsAction(i);
    }

    public MenuItem setShowAsActionFlags(int i) {
        ((MenuItem) this.mWrappedObject).setShowAsActionFlags(i);
        return this;
    }

    public SupportMenuItem setSupportActionProvider(android.support.v4.view.ActionProvider actionProvider) {
        ((MenuItem) this.mWrappedObject).setActionProvider(actionProvider != null ? createActionProviderWrapper(actionProvider) : null);
        return this;
    }

    public SupportMenuItem setSupportOnActionExpandListener(OnActionExpandListener onActionExpandListener) {
        ((MenuItem) this.mWrappedObject).setOnActionExpandListener(onActionExpandListener != null ? new OnActionExpandListenerWrapper(onActionExpandListener) : null);
        return null;
    }

    public MenuItem setTitle(int i) {
        ((MenuItem) this.mWrappedObject).setTitle(i);
        return this;
    }

    public MenuItem setTitle(CharSequence charSequence) {
        ((MenuItem) this.mWrappedObject).setTitle(charSequence);
        return this;
    }

    public MenuItem setTitleCondensed(CharSequence charSequence) {
        ((MenuItem) this.mWrappedObject).setTitleCondensed(charSequence);
        return this;
    }

    public MenuItem setVisible(boolean z) {
        if (this.mEmulateProviderVisibilityOverride) {
            this.mLastRequestVisible = z;
            if (checkActionProviderOverrideVisibility()) {
                return this;
            }
        }
        return wrappedSetVisible(z);
    }

    final MenuItem wrappedSetVisible(boolean z) {
        return ((MenuItem) this.mWrappedObject).setVisible(z);
    }
}
