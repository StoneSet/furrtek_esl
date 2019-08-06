package android.support.v7.app;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.internal.view.ActionModeWrapper;
import android.support.v7.internal.view.ActionModeWrapper.CallbackWrapper;
import android.support.v7.internal.view.menu.MenuWrapperFactory;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Window.Callback;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;

class ActionBarActivityDelegateICS extends ActionBarActivityDelegate {
    Menu mMenu;

    class WindowCallbackWrapper implements Callback {
        final Callback mWrapped;

        public WindowCallbackWrapper(Callback callback) {
            this.mWrapped = callback;
        }

        public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
            return this.mWrapped.dispatchGenericMotionEvent(motionEvent);
        }

        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            return this.mWrapped.dispatchKeyEvent(keyEvent);
        }

        public boolean dispatchKeyShortcutEvent(KeyEvent keyEvent) {
            return this.mWrapped.dispatchKeyShortcutEvent(keyEvent);
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
            return this.mWrapped.dispatchPopulateAccessibilityEvent(accessibilityEvent);
        }

        public boolean dispatchTouchEvent(MotionEvent motionEvent) {
            return this.mWrapped.dispatchTouchEvent(motionEvent);
        }

        public boolean dispatchTrackballEvent(MotionEvent motionEvent) {
            return this.mWrapped.dispatchTrackballEvent(motionEvent);
        }

        public void onActionModeFinished(ActionMode actionMode) {
            this.mWrapped.onActionModeFinished(actionMode);
            ActionBarActivityDelegateICS.this.onActionModeFinished(actionMode);
        }

        public void onActionModeStarted(ActionMode actionMode) {
            this.mWrapped.onActionModeStarted(actionMode);
            ActionBarActivityDelegateICS.this.onActionModeStarted(actionMode);
        }

        public void onAttachedToWindow() {
            this.mWrapped.onAttachedToWindow();
        }

        public void onContentChanged() {
            this.mWrapped.onContentChanged();
        }

        public boolean onCreatePanelMenu(int i, Menu menu) {
            return this.mWrapped.onCreatePanelMenu(i, menu);
        }

        public View onCreatePanelView(int i) {
            return this.mWrapped.onCreatePanelView(i);
        }

        public void onDetachedFromWindow() {
            this.mWrapped.onDetachedFromWindow();
        }

        public boolean onMenuItemSelected(int i, MenuItem menuItem) {
            return this.mWrapped.onMenuItemSelected(i, menuItem);
        }

        public boolean onMenuOpened(int i, Menu menu) {
            return this.mWrapped.onMenuOpened(i, menu);
        }

        public void onPanelClosed(int i, Menu menu) {
            this.mWrapped.onPanelClosed(i, menu);
        }

        public boolean onPreparePanel(int i, View view, Menu menu) {
            return this.mWrapped.onPreparePanel(i, view, menu);
        }

        public boolean onSearchRequested() {
            return this.mWrapped.onSearchRequested();
        }

        public void onWindowAttributesChanged(LayoutParams layoutParams) {
            this.mWrapped.onWindowAttributesChanged(layoutParams);
        }

        public void onWindowFocusChanged(boolean z) {
            this.mWrapped.onWindowFocusChanged(z);
        }

        public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
            return this.mWrapped.onWindowStartingActionMode(callback);
        }
    }

    ActionBarActivityDelegateICS(ActionBarActivity actionBarActivity) {
        super(actionBarActivity);
    }

    public void addContentView(View view, ViewGroup.LayoutParams layoutParams) {
        this.mActivity.superAddContentView(view, layoutParams);
    }

    CallbackWrapper createActionModeCallbackWrapper(Context context, android.support.v7.view.ActionMode.Callback callback) {
        return new CallbackWrapper(context, callback);
    }

    ActionModeWrapper createActionModeWrapper(Context context, ActionMode actionMode) {
        return new ActionModeWrapper(context, actionMode);
    }

    public ActionBar createSupportActionBar() {
        return new ActionBarImplICS(this.mActivity, this.mActivity);
    }

    Callback createWindowCallbackWrapper(Callback callback) {
        return new WindowCallbackWrapper(callback);
    }

    int getHomeAsUpIndicatorAttrId() {
        return 16843531;
    }

    public void onActionModeFinished(ActionMode actionMode) {
        this.mActivity.onSupportActionModeFinished(createActionModeWrapper(getActionBarThemedContext(), actionMode));
    }

    public void onActionModeStarted(ActionMode actionMode) {
        this.mActivity.onSupportActionModeStarted(createActionModeWrapper(getActionBarThemedContext(), actionMode));
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onConfigurationChanged(Configuration configuration) {
    }

    public void onContentChanged() {
        this.mActivity.onSupportContentChanged();
    }

    public void onCreate(Bundle bundle) {
        if ("splitActionBarWhenNarrow".equals(getUiOptionsFromMetadata())) {
            this.mActivity.getWindow().setUiOptions(1, 1);
        }
        super.onCreate(bundle);
        if (this.mHasActionBar) {
            this.mActivity.requestWindowFeature(8);
        }
        if (this.mOverlayActionBar) {
            this.mActivity.requestWindowFeature(9);
        }
        Window window = this.mActivity.getWindow();
        window.setCallback(createWindowCallbackWrapper(window.getCallback()));
    }

    public boolean onCreatePanelMenu(int i, Menu menu) {
        if (i != 0 && i != 8) {
            return this.mActivity.superOnCreatePanelMenu(i, menu);
        }
        if (this.mMenu == null) {
            this.mMenu = MenuWrapperFactory.createMenuWrapper(menu);
        }
        return this.mActivity.superOnCreatePanelMenu(i, this.mMenu);
    }

    public View onCreatePanelView(int i) {
        return null;
    }

    public boolean onMenuItemSelected(int i, MenuItem menuItem) {
        if (i == 0) {
            menuItem = MenuWrapperFactory.createMenuItemWrapper(menuItem);
        }
        return this.mActivity.superOnMenuItemSelected(i, menuItem);
    }

    public void onPostResume() {
    }

    public boolean onPreparePanel(int i, View view, Menu menu) {
        return (i == 0 || i == 8) ? this.mActivity.superOnPreparePanel(i, view, this.mMenu) : this.mActivity.superOnPreparePanel(i, view, menu);
    }

    public void onStop() {
    }

    public void onTitleChanged(CharSequence charSequence) {
    }

    public void setContentView(int i) {
        this.mActivity.superSetContentView(i);
    }

    public void setContentView(View view) {
        this.mActivity.superSetContentView(view);
    }

    public void setContentView(View view, ViewGroup.LayoutParams layoutParams) {
        this.mActivity.superSetContentView(view, layoutParams);
    }

    void setSupportProgress(int i) {
        this.mActivity.setProgress(i);
    }

    void setSupportProgressBarIndeterminate(boolean z) {
        this.mActivity.setProgressBarIndeterminate(z);
    }

    void setSupportProgressBarIndeterminateVisibility(boolean z) {
        this.mActivity.setProgressBarIndeterminateVisibility(z);
    }

    void setSupportProgressBarVisibility(boolean z) {
        this.mActivity.setProgressBarVisibility(z);
    }

    public android.support.v7.view.ActionMode startSupportActionMode(android.support.v7.view.ActionMode.Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("ActionMode callback can not be null.");
        }
        Context actionBarThemedContext = getActionBarThemedContext();
        Object createActionModeCallbackWrapper = createActionModeCallbackWrapper(actionBarThemedContext, callback);
        ActionMode startActionMode = this.mActivity.startActionMode(createActionModeCallbackWrapper);
        if (startActionMode == null) {
            return null;
        }
        android.support.v7.view.ActionMode createActionModeWrapper = createActionModeWrapper(actionBarThemedContext, startActionMode);
        createActionModeCallbackWrapper.setLastStartedActionMode(createActionModeWrapper);
        return createActionModeWrapper;
    }

    public void supportInvalidateOptionsMenu() {
        this.mMenu = null;
    }

    public boolean supportRequestWindowFeature(int i) {
        return this.mActivity.requestWindowFeature(i);
    }
}
