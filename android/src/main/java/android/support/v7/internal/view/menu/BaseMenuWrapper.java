package android.support.v7.internal.view.menu;

import android.support.v4.internal.view.SupportMenuItem;
import android.view.MenuItem;
import android.view.SubMenu;
import java.util.HashMap;
import java.util.Iterator;

abstract class BaseMenuWrapper<T> extends BaseWrapper<T> {
    private HashMap<MenuItem, SupportMenuItem> mMenuItems;
    private HashMap<SubMenu, SubMenu> mSubMenus;

    BaseMenuWrapper(T t) {
        super(t);
    }

    final SupportMenuItem getMenuItemWrapper(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }
        if (this.mMenuItems == null) {
            this.mMenuItems = new HashMap();
        }
        SupportMenuItem supportMenuItem = (SupportMenuItem) this.mMenuItems.get(menuItem);
        if (supportMenuItem != null) {
            return supportMenuItem;
        }
        supportMenuItem = MenuWrapperFactory.createSupportMenuItemWrapper(menuItem);
        this.mMenuItems.put(menuItem, supportMenuItem);
        return supportMenuItem;
    }

    final SubMenu getSubMenuWrapper(SubMenu subMenu) {
        if (subMenu == null) {
            return null;
        }
        if (this.mSubMenus == null) {
            this.mSubMenus = new HashMap();
        }
        SubMenu subMenu2 = (SubMenu) this.mSubMenus.get(subMenu);
        if (subMenu2 != null) {
            return subMenu2;
        }
        subMenu2 = MenuWrapperFactory.createSupportSubMenuWrapper(subMenu);
        this.mSubMenus.put(subMenu, subMenu2);
        return subMenu2;
    }

    final void internalClear() {
        if (this.mMenuItems != null) {
            this.mMenuItems.clear();
        }
        if (this.mSubMenus != null) {
            this.mSubMenus.clear();
        }
    }

    final void internalRemoveGroup(int i) {
        if (this.mMenuItems != null) {
            Iterator it = this.mMenuItems.keySet().iterator();
            while (it.hasNext()) {
                if (i == ((MenuItem) it.next()).getGroupId()) {
                    it.remove();
                }
            }
        }
    }

    final void internalRemoveItem(int i) {
        if (this.mMenuItems != null) {
            Iterator it = this.mMenuItems.keySet().iterator();
            while (it.hasNext()) {
                if (i == ((MenuItem) it.next()).getItemId()) {
                    it.remove();
                    return;
                }
            }
        }
    }
}
