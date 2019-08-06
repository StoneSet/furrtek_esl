package android.support.v7.internal.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.app.ActionBar;
import android.support.v7.appcompat.R;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class ActionBarOverlayLayout extends FrameLayout {
    static final int[] mActionBarSizeAttr = new int[]{R.attr.actionBarSize};
    private ActionBar mActionBar;
    private View mActionBarBottom;
    private int mActionBarHeight;
    private View mActionBarTop;
    private ActionBarView mActionView;
    private ActionBarContainer mContainerView;
    private View mContent;
    private final Rect mZeroRect = new Rect(0, 0, 0, 0);

    public ActionBarOverlayLayout(Context context) {
        super(context);
        init(context);
    }

    public ActionBarOverlayLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    private boolean applyInsets(View view, Rect rect, boolean z, boolean z2, boolean z3, boolean z4) {
        boolean z5 = false;
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (z && layoutParams.leftMargin != rect.left) {
            layoutParams.leftMargin = rect.left;
            z5 = true;
        }
        if (z2 && layoutParams.topMargin != rect.top) {
            layoutParams.topMargin = rect.top;
            z5 = true;
        }
        if (z4 && layoutParams.rightMargin != rect.right) {
            layoutParams.rightMargin = rect.right;
            z5 = true;
        }
        if (!z3 || layoutParams.bottomMargin == rect.bottom) {
            return z5;
        }
        layoutParams.bottomMargin = rect.bottom;
        return true;
    }

    private void init(Context context) {
        TypedArray obtainStyledAttributes = getContext().getTheme().obtainStyledAttributes(mActionBarSizeAttr);
        this.mActionBarHeight = obtainStyledAttributes.getDimensionPixelSize(0, 0);
        obtainStyledAttributes.recycle();
    }

    void pullChildren() {
        if (this.mContent == null) {
            this.mContent = findViewById(R.id.action_bar_activity_content);
            if (this.mContent == null) {
                this.mContent = findViewById(16908290);
            }
            this.mActionBarTop = findViewById(R.id.top_action_bar);
            this.mContainerView = (ActionBarContainer) findViewById(R.id.action_bar_container);
            this.mActionView = (ActionBarView) findViewById(R.id.action_bar);
            this.mActionBarBottom = findViewById(R.id.split_action_bar);
        }
    }

    public void setActionBar(ActionBar actionBar) {
        this.mActionBar = actionBar;
    }
}
