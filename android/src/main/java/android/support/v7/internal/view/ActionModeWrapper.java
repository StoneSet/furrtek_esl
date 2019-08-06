package android.support.v7.internal.view;

import android.content.Context;
import android.support.v7.internal.view.menu.MenuWrapperFactory;
import android.support.v7.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class ActionModeWrapper extends ActionMode {
    final MenuInflater mInflater;
    final android.view.ActionMode mWrappedObject;

    public static class CallbackWrapper implements Callback {
        final Context mContext;
        private ActionModeWrapper mLastStartedActionMode;
        final ActionMode.Callback mWrappedCallback;

        public CallbackWrapper(Context context, ActionMode.Callback callback) {
            this.mContext = context;
            this.mWrappedCallback = callback;
        }

        private ActionMode getActionModeWrapper(android.view.ActionMode actionMode) {
            return (this.mLastStartedActionMode == null || this.mLastStartedActionMode.mWrappedObject != actionMode) ? createActionModeWrapper(this.mContext, actionMode) : this.mLastStartedActionMode;
        }

        protected ActionModeWrapper createActionModeWrapper(Context context, android.view.ActionMode actionMode) {
            return new ActionModeWrapper(context, actionMode);
        }

        public boolean onActionItemClicked(android.view.ActionMode actionMode, MenuItem menuItem) {
            return this.mWrappedCallback.onActionItemClicked(getActionModeWrapper(actionMode), MenuWrapperFactory.createMenuItemWrapper(menuItem));
        }

        public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu) {
            return this.mWrappedCallback.onCreateActionMode(getActionModeWrapper(actionMode), MenuWrapperFactory.createMenuWrapper(menu));
        }

        public void onDestroyActionMode(android.view.ActionMode actionMode) {
            this.mWrappedCallback.onDestroyActionMode(getActionModeWrapper(actionMode));
        }

        public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu) {
            return this.mWrappedCallback.onPrepareActionMode(getActionModeWrapper(actionMode), MenuWrapperFactory.createMenuWrapper(menu));
        }

        public void setLastStartedActionMode(ActionModeWrapper actionModeWrapper) {
            this.mLastStartedActionMode = actionModeWrapper;
        }
    }

    public ActionModeWrapper(Context context, android.view.ActionMode actionMode) {
        this.mWrappedObject = actionMode;
        this.mInflater = new SupportMenuInflater(context);
    }

    public void finish() {
        this.mWrappedObject.finish();
    }

    public View getCustomView() {
        return this.mWrappedObject.getCustomView();
    }

    public Menu getMenu() {
        return MenuWrapperFactory.createMenuWrapper(this.mWrappedObject.getMenu());
    }

    public MenuInflater getMenuInflater() {
        return this.mInflater;
    }

    public CharSequence getSubtitle() {
        return this.mWrappedObject.getSubtitle();
    }

    public Object getTag() {
        return this.mWrappedObject.getTag();
    }

    public CharSequence getTitle() {
        return this.mWrappedObject.getTitle();
    }

    public void invalidate() {
        this.mWrappedObject.invalidate();
    }

    public void setCustomView(View view) {
        this.mWrappedObject.setCustomView(view);
    }

    public void setSubtitle(int i) {
        this.mWrappedObject.setSubtitle(i);
    }

    public void setSubtitle(CharSequence charSequence) {
        this.mWrappedObject.setSubtitle(charSequence);
    }

    public void setTag(Object obj) {
        this.mWrappedObject.setTag(obj);
    }

    public void setTitle(int i) {
        this.mWrappedObject.setTitle(i);
    }

    public void setTitle(CharSequence charSequence) {
        this.mWrappedObject.setTitle(charSequence);
    }
}
