package android.support.v4.view;

import android.os.Bundle;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;

class AccessibilityDelegateCompatJellyBean {

    public interface AccessibilityDelegateBridgeJellyBean {
        boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        Object getAccessibilityNodeProvider(View view);

        void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        void onInitializeAccessibilityNodeInfo(View view, Object obj);

        void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent);

        boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent);

        boolean performAccessibilityAction(View view, int i, Bundle bundle);

        void sendAccessibilityEvent(View view, int i);

        void sendAccessibilityEventUnchecked(View view, AccessibilityEvent accessibilityEvent);
    }

    AccessibilityDelegateCompatJellyBean() {
    }

    public static Object getAccessibilityNodeProvider(Object obj, View view) {
        return ((AccessibilityDelegate) obj).getAccessibilityNodeProvider(view);
    }

    public static Object newAccessibilityDelegateBridge(final AccessibilityDelegateBridgeJellyBean accessibilityDelegateBridgeJellyBean) {
        return new AccessibilityDelegate() {
            public boolean dispatchPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
                return accessibilityDelegateBridgeJellyBean.dispatchPopulateAccessibilityEvent(view, accessibilityEvent);
            }

            public AccessibilityNodeProvider getAccessibilityNodeProvider(View view) {
                return (AccessibilityNodeProvider) accessibilityDelegateBridgeJellyBean.getAccessibilityNodeProvider(view);
            }

            public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
                accessibilityDelegateBridgeJellyBean.onInitializeAccessibilityEvent(view, accessibilityEvent);
            }

            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                accessibilityDelegateBridgeJellyBean.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
            }

            public void onPopulateAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
                accessibilityDelegateBridgeJellyBean.onPopulateAccessibilityEvent(view, accessibilityEvent);
            }

            public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
                return accessibilityDelegateBridgeJellyBean.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
            }

            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                return accessibilityDelegateBridgeJellyBean.performAccessibilityAction(view, i, bundle);
            }

            public void sendAccessibilityEvent(View view, int i) {
                accessibilityDelegateBridgeJellyBean.sendAccessibilityEvent(view, i);
            }

            public void sendAccessibilityEventUnchecked(View view, AccessibilityEvent accessibilityEvent) {
                accessibilityDelegateBridgeJellyBean.sendAccessibilityEventUnchecked(view, accessibilityEvent);
            }
        };
    }

    public static boolean performAccessibilityAction(Object obj, View view, int i, Bundle bundle) {
        return ((AccessibilityDelegate) obj).performAccessibilityAction(view, i, bundle);
    }
}
