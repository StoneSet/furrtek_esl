package android.support.v7.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.ActionBar.OnMenuVisibilityListener;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBar.Tab;
import android.view.View;
import android.widget.SpinnerAdapter;

public class ActionBarImplJB extends ActionBarImplICS {
    public ActionBarImplJB(Activity activity, Callback callback) {
        super(activity, callback, false);
    }

    public /* bridge */ /* synthetic */ void addOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener) {
        super.addOnMenuVisibilityListener(onMenuVisibilityListener);
    }

    public /* bridge */ /* synthetic */ void addTab(Tab tab) {
        super.addTab(tab);
    }

    public /* bridge */ /* synthetic */ void addTab(Tab tab, int i, boolean z) {
        super.addTab(tab, i, z);
    }

    public /* bridge */ /* synthetic */ View getCustomView() {
        return super.getCustomView();
    }

    public /* bridge */ /* synthetic */ int getDisplayOptions() {
        return super.getDisplayOptions();
    }

    public /* bridge */ /* synthetic */ int getHeight() {
        return super.getHeight();
    }

    public /* bridge */ /* synthetic */ int getNavigationItemCount() {
        return super.getNavigationItemCount();
    }

    public /* bridge */ /* synthetic */ int getNavigationMode() {
        return super.getNavigationMode();
    }

    public /* bridge */ /* synthetic */ int getSelectedNavigationIndex() {
        return super.getSelectedNavigationIndex();
    }

    public /* bridge */ /* synthetic */ Tab getSelectedTab() {
        return super.getSelectedTab();
    }

    public /* bridge */ /* synthetic */ CharSequence getSubtitle() {
        return super.getSubtitle();
    }

    public /* bridge */ /* synthetic */ Tab getTabAt(int i) {
        return super.getTabAt(i);
    }

    public /* bridge */ /* synthetic */ int getTabCount() {
        return super.getTabCount();
    }

    public /* bridge */ /* synthetic */ Context getThemedContext() {
        return super.getThemedContext();
    }

    public /* bridge */ /* synthetic */ CharSequence getTitle() {
        return super.getTitle();
    }

    public /* bridge */ /* synthetic */ void hide() {
        super.hide();
    }

    public /* bridge */ /* synthetic */ boolean isShowing() {
        return super.isShowing();
    }

    public /* bridge */ /* synthetic */ Tab newTab() {
        return super.newTab();
    }

    public /* bridge */ /* synthetic */ void removeAllTabs() {
        super.removeAllTabs();
    }

    public /* bridge */ /* synthetic */ void removeOnMenuVisibilityListener(OnMenuVisibilityListener onMenuVisibilityListener) {
        super.removeOnMenuVisibilityListener(onMenuVisibilityListener);
    }

    public /* bridge */ /* synthetic */ void removeTab(Tab tab) {
        super.removeTab(tab);
    }

    public /* bridge */ /* synthetic */ void removeTabAt(int i) {
        super.removeTabAt(i);
    }

    public /* bridge */ /* synthetic */ void selectTab(Tab tab) {
        super.selectTab(tab);
    }

    public /* bridge */ /* synthetic */ void setBackgroundDrawable(Drawable drawable) {
        super.setBackgroundDrawable(drawable);
    }

    public /* bridge */ /* synthetic */ void setCustomView(View view, LayoutParams layoutParams) {
        super.setCustomView(view, layoutParams);
    }

    public /* bridge */ /* synthetic */ void setDisplayHomeAsUpEnabled(boolean z) {
        super.setDisplayHomeAsUpEnabled(z);
    }

    public /* bridge */ /* synthetic */ void setDisplayOptions(int i) {
        super.setDisplayOptions(i);
    }

    public /* bridge */ /* synthetic */ void setDisplayOptions(int i, int i2) {
        super.setDisplayOptions(i, i2);
    }

    public /* bridge */ /* synthetic */ void setDisplayShowCustomEnabled(boolean z) {
        super.setDisplayShowCustomEnabled(z);
    }

    public /* bridge */ /* synthetic */ void setDisplayShowHomeEnabled(boolean z) {
        super.setDisplayShowHomeEnabled(z);
    }

    public /* bridge */ /* synthetic */ void setDisplayShowTitleEnabled(boolean z) {
        super.setDisplayShowTitleEnabled(z);
    }

    public /* bridge */ /* synthetic */ void setDisplayUseLogoEnabled(boolean z) {
        super.setDisplayUseLogoEnabled(z);
    }

    public /* bridge */ /* synthetic */ void setHomeButtonEnabled(boolean z) {
        super.setHomeButtonEnabled(z);
    }

    public /* bridge */ /* synthetic */ void setListNavigationCallbacks(SpinnerAdapter spinnerAdapter, OnNavigationListener onNavigationListener) {
        super.setListNavigationCallbacks(spinnerAdapter, onNavigationListener);
    }

    public /* bridge */ /* synthetic */ void setNavigationMode(int i) {
        super.setNavigationMode(i);
    }

    public /* bridge */ /* synthetic */ void setSelectedNavigationItem(int i) {
        super.setSelectedNavigationItem(i);
    }

    public /* bridge */ /* synthetic */ void setSplitBackgroundDrawable(Drawable drawable) {
        super.setSplitBackgroundDrawable(drawable);
    }

    public /* bridge */ /* synthetic */ void setStackedBackgroundDrawable(Drawable drawable) {
        super.setStackedBackgroundDrawable(drawable);
    }

    public /* bridge */ /* synthetic */ void show() {
        super.show();
    }
}
