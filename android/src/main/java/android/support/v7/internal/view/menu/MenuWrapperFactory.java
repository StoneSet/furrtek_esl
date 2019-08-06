package android.support.v7.internal.view.menu;

import android.os.Build.VERSION;
import android.support.v4.internal.view.SupportMenu;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.internal.view.SupportSubMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public final class MenuWrapperFactory {
    private MenuWrapperFactory() {
    }

    public static MenuItem createMenuItemWrapper(MenuItem menuItem) {
        return VERSION.SDK_INT >= 16 ? new MenuItemWrapperJB(menuItem) : VERSION.SDK_INT >= 14 ? new MenuItemWrapperICS(menuItem) : menuItem;
    }

    public static Menu createMenuWrapper(Menu menu) {
        return VERSION.SDK_INT >= 14 ? new MenuWrapperICS(menu) : menu;
    }

    public static SupportMenuItem createSupportMenuItemWrapper(MenuItem menuItem) {
        if (VERSION.SDK_INT >= 16) {
            return new MenuItemWrapperJB(menuItem);
        }
        if (VERSION.SDK_INT >= 14) {
            return new MenuItemWrapperICS(menuItem);
        }
        throw new UnsupportedOperationException();
    }

    public static SupportMenu createSupportMenuWrapper(Menu menu) {
        if (VERSION.SDK_INT >= 14) {
            return new MenuWrapperICS(menu);
        }
        throw new UnsupportedOperationException();
    }

    public static SupportSubMenu createSupportSubMenuWrapper(SubMenu subMenu) {
        if (VERSION.SDK_INT >= 14) {
            return new SubMenuWrapperICS(subMenu);
        }
        throw new UnsupportedOperationException();
    }
}
