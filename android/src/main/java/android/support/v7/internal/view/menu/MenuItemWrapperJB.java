package android.support.v7.internal.view.menu;

import android.support.v4.view.ActionProvider.VisibilityListener;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.View;

class MenuItemWrapperJB extends MenuItemWrapperICS {

    class ActionProviderWrapperJB extends ActionProviderWrapper implements VisibilityListener {
        ActionProvider.VisibilityListener mListener;

        public ActionProviderWrapperJB(android.support.v4.view.ActionProvider actionProvider) {
            super(actionProvider);
        }

        public boolean isVisible() {
            return this.mInner.isVisible();
        }

        public void onActionProviderVisibilityChanged(boolean z) {
            if (this.mListener != null) {
                this.mListener.onActionProviderVisibilityChanged(z);
            }
        }

        public View onCreateActionView(MenuItem menuItem) {
            return this.mInner.onCreateActionView(menuItem);
        }

        public boolean overridesItemVisibility() {
            return this.mInner.overridesItemVisibility();
        }

        public void refreshVisibility() {
            this.mInner.refreshVisibility();
        }

        public void setVisibilityListener(ActionProvider.VisibilityListener visibilityListener) {
            VisibilityListener visibilityListener2;
            this.mListener = visibilityListener;
            android.support.v4.view.ActionProvider actionProvider = this.mInner;
            if (visibilityListener == null) {
                visibilityListener2 = null;
            }
            actionProvider.setVisibilityListener(visibilityListener2);
        }
    }

    MenuItemWrapperJB(MenuItem menuItem) {
        super(menuItem, false);
    }

    ActionProviderWrapper createActionProviderWrapper(android.support.v4.view.ActionProvider actionProvider) {
        return new ActionProviderWrapperJB(actionProvider);
    }
}
