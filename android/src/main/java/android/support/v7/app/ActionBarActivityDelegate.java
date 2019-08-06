package android.support.v7.app;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle.Delegate;
import android.support.v4.app.NavUtils;
import android.support.v7.appcompat.R;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

abstract class ActionBarActivityDelegate {
    static final String METADATA_UI_OPTIONS = "android.support.UI_OPTIONS";
    private static final String TAG = "ActionBarActivityDelegate";
    static final String UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW = "splitActionBarWhenNarrow";
    private ActionBar mActionBar;
    final ActionBarActivity mActivity;
    private boolean mEnableDefaultActionBarUp;
    boolean mHasActionBar;
    private MenuInflater mMenuInflater;
    boolean mOverlayActionBar;

    private class ActionBarDrawableToggleImpl implements Delegate {
        private ActionBarDrawableToggleImpl() {
        }

        public Drawable getThemeUpIndicator() {
            TypedArray obtainStyledAttributes = ActionBarActivityDelegate.this.mActivity.obtainStyledAttributes(new int[]{ActionBarActivityDelegate.this.getHomeAsUpIndicatorAttrId()});
            Drawable drawable = obtainStyledAttributes.getDrawable(0);
            obtainStyledAttributes.recycle();
            return drawable;
        }

        public void setActionBarDescription(int i) {
            ActionBar supportActionBar = ActionBarActivityDelegate.this.getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setHomeActionContentDescription(i);
            }
        }

        public void setActionBarUpIndicator(Drawable drawable, int i) {
            ActionBar supportActionBar = ActionBarActivityDelegate.this.getSupportActionBar();
            if (supportActionBar != null) {
                supportActionBar.setHomeAsUpIndicator(drawable);
                supportActionBar.setHomeActionContentDescription(i);
            }
        }
    }

    ActionBarActivityDelegate(ActionBarActivity actionBarActivity) {
        this.mActivity = actionBarActivity;
    }

    static ActionBarActivityDelegate createDelegate(ActionBarActivity actionBarActivity) {
        return VERSION.SDK_INT >= 18 ? new ActionBarActivityDelegateJBMR2(actionBarActivity) : VERSION.SDK_INT >= 16 ? new ActionBarActivityDelegateJB(actionBarActivity) : VERSION.SDK_INT >= 14 ? new ActionBarActivityDelegateICS(actionBarActivity) : VERSION.SDK_INT >= 11 ? new ActionBarActivityDelegateHC(actionBarActivity) : new ActionBarActivityDelegateBase(actionBarActivity);
    }

    abstract void addContentView(View view, LayoutParams layoutParams);

    abstract ActionBar createSupportActionBar();

    protected final Context getActionBarThemedContext() {
        Context context = this.mActivity;
        ActionBar supportActionBar = getSupportActionBar();
        return supportActionBar != null ? supportActionBar.getThemedContext() : context;
    }

    final Delegate getDrawerToggleDelegate() {
        return new ActionBarDrawableToggleImpl();
    }

    abstract int getHomeAsUpIndicatorAttrId();

    MenuInflater getMenuInflater() {
        if (this.mMenuInflater == null) {
            this.mMenuInflater = new SupportMenuInflater(getActionBarThemedContext());
        }
        return this.mMenuInflater;
    }

    final ActionBar getSupportActionBar() {
        if (!this.mHasActionBar && !this.mOverlayActionBar) {
            this.mActionBar = null;
        } else if (this.mActionBar == null) {
            this.mActionBar = createSupportActionBar();
            if (this.mEnableDefaultActionBarUp) {
                this.mActionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        return this.mActionBar;
    }

    protected final String getUiOptionsFromMetadata() {
        String str = null;
        try {
            ActivityInfo activityInfo = this.mActivity.getPackageManager().getActivityInfo(this.mActivity.getComponentName(), 128);
            if (activityInfo.metaData != null) {
                str = activityInfo.metaData.getString(METADATA_UI_OPTIONS);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getUiOptionsFromMetadata: Activity '" + this.mActivity.getClass().getSimpleName() + "' not in manifest");
        }
        return str;
    }

    abstract boolean onBackPressed();

    abstract void onConfigurationChanged(Configuration configuration);

    abstract void onContentChanged();

    void onCreate(Bundle bundle) {
        TypedArray obtainStyledAttributes = this.mActivity.obtainStyledAttributes(R.styleable.ActionBarWindow);
        if (obtainStyledAttributes.hasValue(0)) {
            this.mHasActionBar = obtainStyledAttributes.getBoolean(0, false);
            this.mOverlayActionBar = obtainStyledAttributes.getBoolean(1, false);
            obtainStyledAttributes.recycle();
            if (NavUtils.getParentActivityName(this.mActivity) == null) {
                return;
            }
            if (this.mActionBar == null) {
                this.mEnableDefaultActionBarUp = true;
                return;
            } else {
                this.mActionBar.setDisplayHomeAsUpEnabled(true);
                return;
            }
        }
        obtainStyledAttributes.recycle();
        throw new IllegalStateException("You need to use a Theme.AppCompat theme (or descendant) with this activity.");
    }

    abstract boolean onCreatePanelMenu(int i, Menu menu);

    abstract View onCreatePanelView(int i);

    abstract boolean onMenuItemSelected(int i, MenuItem menuItem);

    abstract void onPostResume();

    boolean onPrepareOptionsPanel(View view, Menu menu) {
        return VERSION.SDK_INT < 16 ? this.mActivity.onPrepareOptionsMenu(menu) : this.mActivity.superOnPrepareOptionsPanel(view, menu);
    }

    abstract boolean onPreparePanel(int i, View view, Menu menu);

    abstract void onStop();

    abstract void onTitleChanged(CharSequence charSequence);

    abstract void setContentView(int i);

    abstract void setContentView(View view);

    abstract void setContentView(View view, LayoutParams layoutParams);

    abstract void setSupportProgress(int i);

    abstract void setSupportProgressBarIndeterminate(boolean z);

    abstract void setSupportProgressBarIndeterminateVisibility(boolean z);

    abstract void setSupportProgressBarVisibility(boolean z);

    abstract ActionMode startSupportActionMode(Callback callback);

    abstract void supportInvalidateOptionsMenu();

    abstract boolean supportRequestWindowFeature(int i);
}
