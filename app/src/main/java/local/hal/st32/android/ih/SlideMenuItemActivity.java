package local.hal.st32.android.ih;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Slide Menu
 */

public class SlideMenuItemActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{




    int drawerLayout;

    public int getDrawerLayout() {
        return drawerLayout;
    }

    public void setDrawerLayout(int drawerLayout) {
        this.drawerLayout = drawerLayout;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(drawerLayout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int itemId = item.getItemId();

        //noinspection SimplifiableIfStatement



        return super.onOptionsItemSelected(item);
    }

    /**
     * Slide Menu Item
     * 倉庫ログイン・ログアウト
     * 従業員ログイン・ログアウト
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent intent = new Intent(this,EmployeeLoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this,WaitingListActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this,ProductListActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(this,ProductDetailActivity.class);
            startActivity(intent);



        } else if (id == R.id.nav_share) {
            Intent intent = new Intent(this,PackingListActivity.class);
            startActivity(intent);



        } else if (id == R.id.nav_send) {
            Intent intent = new Intent(this,PackingDetailActivity.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
