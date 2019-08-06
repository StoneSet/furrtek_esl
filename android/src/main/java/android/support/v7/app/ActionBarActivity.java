package android.support.v7.app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle.Delegate;
import android.support.v4.app.ActionBarDrawerToggle.DelegateProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.app.TaskStackBuilder.SupportParentable;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class ActionBarActivity extends FragmentActivity implements Callback, SupportParentable, DelegateProvider {
    ActionBarActivityDelegate mImpl;

    public void addContentView(View view, LayoutParams layoutParams) {
        this.mImpl.addContentView(view, layoutParams);
    }

    public final Delegate getDrawerToggleDelegate() {
        return this.mImpl.getDrawerToggleDelegate();
    }

    public MenuInflater getMenuInflater() {
        return this.mImpl.getMenuInflater();
    }

    public ActionBar getSupportActionBar() {
        return this.mImpl.getSupportActionBar();
    }

    public Intent getSupportParentActivityIntent() {
        return NavUtils.getParentActivityIntent(this);
    }

    public void onBackPressed() {
        if (!this.mImpl.onBackPressed()) {
            super.onBackPressed();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mImpl.onConfigurationChanged(configuration);
    }

    public final void onContentChanged() {
        this.mImpl.onContentChanged();
    }

    protected void onCreate(Bundle bundle) {
        this.mImpl = ActionBarActivityDelegate.createDelegate(this);
        super.onCreate(bundle);
        this.mImpl.onCreate(bundle);
    }

    public boolean onCreatePanelMenu(int i, Menu menu) {
        return this.mImpl.onCreatePanelMenu(i, menu);
    }

    public View onCreatePanelView(int i) {
        return i == 0 ? this.mImpl.onCreatePanelView(i) : super.onCreatePanelView(i);
    }

    public void onCreateSupportNavigateUpTaskStack(TaskStackBuilder taskStackBuilder) {
        taskStackBuilder.addParentStack((Activity) this);
    }

    public final boolean onMenuItemSelected(int i, MenuItem menuItem) {
        if (this.mImpl.onMenuItemSelected(i, menuItem)) {
            return true;
        }
        ActionBar supportActionBar = getSupportActionBar();
        return (menuItem.getItemId() != 16908332 || supportActionBar == null || (supportActionBar.getDisplayOptions() & 4) == 0) ? false : onSupportNavigateUp();
    }

    protected void onPostResume() {
        super.onPostResume();
        this.mImpl.onPostResume();
    }

    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        return this.mImpl.onPrepareOptionsPanel(view, menu);
    }

    public boolean onPreparePanel(int i, View view, Menu menu) {
        return this.mImpl.onPreparePanel(i, view, menu);
    }

    public void onPrepareSupportNavigateUpTaskStack(TaskStackBuilder taskStackBuilder) {
    }

    protected void onStop() {
        super.onStop();
        this.mImpl.onStop();
    }

    public void onSupportActionModeFinished(ActionMode actionMode) {
    }

    public void onSupportActionModeStarted(ActionMode actionMode) {
    }

    public void onSupportContentChanged() {
    }

    public boolean onSupportNavigateUp() {
        Intent supportParentActivityIntent = getSupportParentActivityIntent();
        if (supportParentActivityIntent == null) {
            return false;
        }
        if (supportShouldUpRecreateTask(supportParentActivityIntent)) {
            TaskStackBuilder create = TaskStackBuilder.create(this);
            onCreateSupportNavigateUpTaskStack(create);
            onPrepareSupportNavigateUpTaskStack(create);
            create.startActivities();
            try {
                ActivityCompat.finishAffinity(this);
            } catch (IllegalStateException e) {
                finish();
            }
        } else {
            supportNavigateUpTo(supportParentActivityIntent);
        }
        return true;
    }

    protected void onTitleChanged(CharSequence charSequence, int i) {
        super.onTitleChanged(charSequence, i);
        this.mImpl.onTitleChanged(charSequence);
    }

    public void setContentView(int i) {
        this.mImpl.setContentView(i);
    }

    public void setContentView(View view) {
        this.mImpl.setContentView(view);
    }

    public void setContentView(View view, LayoutParams layoutParams) {
        this.mImpl.setContentView(view, layoutParams);
    }

    public void setSupportProgress(int i) {
        this.mImpl.setSupportProgress(i);
    }

    public void setSupportProgressBarIndeterminate(boolean z) {
        this.mImpl.setSupportProgressBarIndeterminate(z);
    }

    public void setSupportProgressBarIndeterminateVisibility(boolean z) {
        this.mImpl.setSupportProgressBarIndeterminateVisibility(z);
    }

    public void setSupportProgressBarVisibility(boolean z) {
        this.mImpl.setSupportProgressBarVisibility(z);
    }

    public ActionMode startSupportActionMode(Callback callback) {
        return this.mImpl.startSupportActionMode(callback);
    }

    void superAddContentView(View view, LayoutParams layoutParams) {
        super.addContentView(view, layoutParams);
    }

    boolean superOnCreatePanelMenu(int i, Menu menu) {
        return super.onCreatePanelMenu(i, menu);
    }

    boolean superOnMenuItemSelected(int i, MenuItem menuItem) {
        return super.onMenuItemSelected(i, menuItem);
    }

    boolean superOnPrepareOptionsPanel(View view, Menu menu) {
        return super.onPrepareOptionsPanel(view, menu);
    }

    boolean superOnPreparePanel(int i, View view, Menu menu) {
        return super.onPreparePanel(i, view, menu);
    }

    void superSetContentView(int i) {
        super.setContentView(i);
    }

    void superSetContentView(View view) {
        super.setContentView(view);
    }

    void superSetContentView(View view, LayoutParams layoutParams) {
        super.setContentView(view, layoutParams);
    }

    public void supportInvalidateOptionsMenu() {
        if (VERSION.SDK_INT >= 14) {
            super.supportInvalidateOptionsMenu();
        }
        this.mImpl.supportInvalidateOptionsMenu();
    }

    public void supportNavigateUpTo(Intent intent) {
        NavUtils.navigateUpTo(this, intent);
    }

    public boolean supportRequestWindowFeature(int i) {
        return this.mImpl.supportRequestWindowFeature(i);
    }

    public boolean supportShouldUpRecreateTask(Intent intent) {
        return NavUtils.shouldUpRecreateTask(this, intent);
    }
}
