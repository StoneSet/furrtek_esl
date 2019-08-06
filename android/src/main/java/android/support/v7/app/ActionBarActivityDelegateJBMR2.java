package android.support.v7.app;

class ActionBarActivityDelegateJBMR2 extends ActionBarActivityDelegateJB {
    ActionBarActivityDelegateJBMR2(ActionBarActivity actionBarActivity) {
        super(actionBarActivity);
    }

    public ActionBar createSupportActionBar() {
        return new ActionBarImplJBMR2(this.mActivity, this.mActivity);
    }
}
