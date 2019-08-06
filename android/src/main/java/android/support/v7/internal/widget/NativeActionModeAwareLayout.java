package android.support.v7.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.widget.LinearLayout;

public class NativeActionModeAwareLayout extends LinearLayout {
    private OnActionModeForChildListener mActionModeForChildListener;

    public interface OnActionModeForChildListener {
        Callback onActionModeForChild(Callback callback);
    }

    public NativeActionModeAwareLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setActionModeForChildListener(OnActionModeForChildListener onActionModeForChildListener) {
        this.mActionModeForChildListener = onActionModeForChildListener;
    }

    public ActionMode startActionModeForChild(View view, Callback callback) {
        if (this.mActionModeForChildListener != null) {
            callback = this.mActionModeForChildListener.onActionModeForChild(callback);
        }
        return super.startActionModeForChild(view, callback);
    }
}
