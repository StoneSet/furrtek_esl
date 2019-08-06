package android.support.v7.internal.view;

import android.content.Context;
import android.support.v7.view.ActionMode.Callback;
import android.view.ActionMode;

public class ActionModeWrapperJB extends ActionModeWrapper {

    public static class CallbackWrapper extends android.support.v7.internal.view.ActionModeWrapper.CallbackWrapper {
        public CallbackWrapper(Context context, Callback callback) {
            super(context, callback);
        }

        protected ActionModeWrapper createActionModeWrapper(Context context, ActionMode actionMode) {
            return new ActionModeWrapperJB(context, actionMode);
        }
    }

    public ActionModeWrapperJB(Context context, ActionMode actionMode) {
        super(context, actionMode);
    }

    public boolean getTitleOptionalHint() {
        return this.mWrappedObject.getTitleOptionalHint();
    }

    public boolean isTitleOptional() {
        return this.mWrappedObject.isTitleOptional();
    }

    public void setTitleOptionalHint(boolean z) {
        this.mWrappedObject.setTitleOptionalHint(z);
    }
}
