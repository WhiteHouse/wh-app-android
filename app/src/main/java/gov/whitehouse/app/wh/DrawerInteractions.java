package gov.whitehouse.app.wh;

import android.view.MenuItem;

public
class DrawerInteractions
{

    public static
    interface ActivityCallbacks
    {

        public
        void closeDrawer();

        public
        void onDrawerItemSelected(MenuItem item);

        public
        void onRestoreSelectedItem(MenuItem item);
    }

    public static
    interface FragmentCallbacks
    {
        public
        void clickLive();

        public
        void onDrawerOpened();

        public
        void onDrawerClosed();
    }
}
