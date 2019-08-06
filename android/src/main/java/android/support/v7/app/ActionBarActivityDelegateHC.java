package android.support.v7.app;

class ActionBarActivityDelegateHC extends ActionBarActivityDelegateBase {
    ActionBarActivityDelegateHC(ActionBarActivity actionBarActivity) {
        super(actionBarActivity);
    }

    public ActionBar createSupportActionBar() {
        ensureSubDecor();
        return new ActionBarImplHC(this.mActivity, this.mActivity);
    }
}
