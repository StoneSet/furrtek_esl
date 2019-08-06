package android.support.v7.internal.view.menu;

import android.graphics.drawable.Drawable;
import android.support.v4.internal.view.SupportSubMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

class SubMenuWrapperICS extends MenuWrapperICS implements SupportSubMenu {
    SubMenuWrapperICS(SubMenu subMenu) {
        super(subMenu);
    }

    public void clearHeader() {
        ((SubMenu) this.mWrappedObject).clearHeader();
    }

    public MenuItem getItem() {
        return getMenuItemWrapper(((SubMenu) this.mWrappedObject).getItem());
    }

    public SubMenu getWrappedObject() {
        return (SubMenu) this.mWrappedObject;
    }

    public SubMenu setHeaderIcon(int i) {
        ((SubMenu) this.mWrappedObject).setHeaderIcon(i);
        return this;
    }

    public SubMenu setHeaderIcon(Drawable drawable) {
        ((SubMenu) this.mWrappedObject).setHeaderIcon(drawable);
        return this;
    }

    public SubMenu setHeaderTitle(int i) {
        ((SubMenu) this.mWrappedObject).setHeaderTitle(i);
        return this;
    }

    public SubMenu setHeaderTitle(CharSequence charSequence) {
        ((SubMenu) this.mWrappedObject).setHeaderTitle(charSequence);
        return this;
    }

    public SubMenu setHeaderView(View view) {
        ((SubMenu) this.mWrappedObject).setHeaderView(view);
        return this;
    }

    public SubMenu setIcon(int i) {
        ((SubMenu) this.mWrappedObject).setIcon(i);
        return this;
    }

    public SubMenu setIcon(Drawable drawable) {
        ((SubMenu) this.mWrappedObject).setIcon(drawable);
        return this;
    }
}
