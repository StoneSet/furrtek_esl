package android.support.v7.app;

import android.app.Activity;
import android.graphics.drawable.Drawable;

public class ActionBarImplJBMR2 extends ActionBarImplJB {
    public ActionBarImplJBMR2(Activity activity, Callback callback) {
        super(activity, callback);
    }

    public void setHomeActionContentDescription(int i) {
        this.mActionBar.setHomeActionContentDescription(i);
    }

    public void setHomeActionContentDescription(CharSequence charSequence) {
        this.mActionBar.setHomeActionContentDescription(charSequence);
    }

    public void setHomeAsUpIndicator(int i) {
        this.mActionBar.setHomeAsUpIndicator(i);
    }

    public void setHomeAsUpIndicator(Drawable drawable) {
        this.mActionBar.setHomeAsUpIndicator(drawable);
    }
}
