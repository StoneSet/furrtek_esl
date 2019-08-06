package android.support.v7.app;

import android.support.v7.appcompat.R;
import android.support.v7.internal.widget.NativeActionModeAwareLayout;
import android.support.v7.internal.widget.NativeActionModeAwareLayout.OnActionModeForChildListener;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;

class ActionBarImplHC extends ActionBarImplBase implements OnActionModeForChildListener {
    private ActionMode mCurActionMode;
    final NativeActionModeAwareLayout mNativeActionModeAwareLayout;

    private class CallbackWrapper implements Callback {
        private final Callback mWrappedCallback;

        CallbackWrapper(Callback callback) {
            this.mWrappedCallback = callback;
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return this.mWrappedCallback.onActionItemClicked(actionMode, menuItem);
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            boolean onCreateActionMode = this.mWrappedCallback.onCreateActionMode(actionMode, menu);
            if (onCreateActionMode) {
                ActionBarImplHC.this.mCurActionMode = actionMode;
                ActionBarImplHC.this.showForActionMode();
            }
            return onCreateActionMode;
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            this.mWrappedCallback.onDestroyActionMode(actionMode);
            ActionBarImplHC.this.hideForActionMode();
            ActionBarImplHC.this.mCurActionMode = null;
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrappedCallback.onPrepareActionMode(actionMode, menu);
        }
    }

    public ActionBarImplHC(ActionBarActivity actionBarActivity, Callback callback) {
        super(actionBarActivity, callback);
        this.mNativeActionModeAwareLayout = (NativeActionModeAwareLayout) actionBarActivity.findViewById(R.id.action_bar_root);
        if (this.mNativeActionModeAwareLayout != null) {
            this.mNativeActionModeAwareLayout.setActionModeForChildListener(this);
        }
    }

    public void hide() {
        super.hide();
        if (this.mCurActionMode != null) {
            this.mCurActionMode.finish();
        }
    }

    boolean isShowHideAnimationEnabled() {
        return this.mCurActionMode == null && super.isShowHideAnimationEnabled();
    }

    public Callback onActionModeForChild(Callback callback) {
        return new CallbackWrapper(callback);
    }

    public void show() {
        super.show();
        if (this.mCurActionMode != null) {
            this.mCurActionMode.finish();
        }
    }
}
