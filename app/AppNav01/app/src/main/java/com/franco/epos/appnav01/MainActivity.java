package com.franco.epos.appnav01;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.SettingsTB;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView navigationView;
    private static final String TAG_HOME = "home";
    public static String CURRENT_TAG = TAG_HOME;
    public static String CURRENT_AVAL = "";

    private DatabaseHelper db;
    public FloatingActionButton fab;
    AlertDialog.Builder builder;
    private SharedPreferences pref;
    private Menu menuNav;
    final Context context = this;
    MenuItem navSettings, navLogin, navServer, navType, navDelivery, navWaste, navStocktake, navOther, navPriceChange, navQuickAdd;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private String ipaddress, port;

    private RelativeLayout loginLoader;
    private String lType = "" , lVal = "";
    private Handler mHandler;

    private Connection connection = null;
    private String ConnectionURL = null;
    Statement stmt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);*/

        /*if (DEVELOPER_MODE) {
          StrictMode.setThreadPolicy(new {@link ThreadPolicy.Builder StrictMode.ThreadPolicy.Builder}()
                                   .detectDiskReads()
                                   .detectDiskWrites()
                                   .detectNetwork()   // or .detectAll() for all detectable problems
                                   .penaltyLog()
                                   .build());
          StrictMode.setVmPolicy(new {@link VmPolicy.Builder StrictMode.VmPolicy.Builder}()
                                   .detectLeakedSqlLiteObjects()
                                   .detectLeakedClosableObjects()
                                    .penaltyLog()
                                     .penaltyDeath()
                                   .build());
        }*/
        //turnOnStrictMode();
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DatabaseHelper(this);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        fab = (FloatingActionButton) findViewById(R.id.fab);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        menuNav = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);


       // navSettings, navLogin, navServer, navType, navDelivery, navWaste, navStocktake

        navSettings = menuNav.findItem(R.id.nav_settings);
        navLogin = menuNav.findItem(R.id.nav_login);
        navServer = menuNav.findItem(R.id.nav_server);
        navType = menuNav.findItem(R.id.nav_type);
        navDelivery = menuNav.findItem(R.id.nav_delivery);
        navWaste = menuNav.findItem(R.id.nav_waste);
        navStocktake = menuNav.findItem(R.id.nav_stock_take);
        navOther = menuNav.findItem(R.id.nav_other);
        navPriceChange = menuNav.findItem(R.id.nav_price_change);
        navQuickAdd = menuNav.findItem(R.id.nav_quick_add);

        loginLoader = (RelativeLayout) findViewById(R.id.loadingPanel);
        loginLoader.setVisibility(View.GONE);


        if (db.getSettingsTBCount() > 0){

            SettingsTB setTb = db.getSetTBByType("SERVER");
            if (setTb != null){
                ipaddress = setTb.getIpaddress();
                port = setTb.getPort();
            }

        }else {
            //db.insertSettingsTB(settingsTB);
            ipaddress = "";
            port = "";
        }





            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
              //  cameraSource.start(surfaceView.getHolder());
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new
                        String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }






        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this, UpdItemActivity.class);
                i.putExtra("ActType", CURRENT_AVAL);
                i.putExtra("ActFrom", "new");
                i.putExtra("itmID", "");
                startActivityForResult(i, 10001);

                    /*
                    Intent i = new Intent(MainActivity.this, OtherActivity.class);
                    i.putExtra("ActType", CURRENT_AVAL);
                    i.putExtra("ActFrom", "new");
                    i.putExtra("itmID", "");
                    startActivityForResult(i, 10001);
                    */

                //startActivity(i);


            }
        });

        //fab.hide();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                //Called when a drawer's position changes.

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //Called when a drawer has settled in a completely open state.
                //The drawer is interactive at this point.
                // If you have 2 drawers (left and right) you can distinguish
                // them by using id of the drawerView. int id = drawerView.getId();
                // id will be your layout's id: for example R.id.left_drawer
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Called when a drawer has settled in a completely closed state.
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            }

        });





        Log.d("myTag", "This is my message rs" + db.getSettingsTBCount() );


        Bundle bundle = new Bundle();
        Fragment fragment = null;



        clearMenu();
        if (db.getSettingsTBCount() > 0){

            if (pref.getBoolean("ckLogin", false)){

                navType.setVisible(true);
                navOther.setVisible(true);
                navLogin.setTitle("Logout");

                fragment = new HomeFragment();
                bundle.putString("PageName", "Main");
                bundle.putString("ActType", "Delivery");
                CURRENT_AVAL = "Delivery";
                navDelivery.setChecked(true);

            }else{

                navType.setVisible(false);
                navOther.setVisible(false);
                fragment = new LoginFragment();
                bundle.putString("PageName", "Login");
                bundle.putString("ActType", "");
                CURRENT_AVAL = "Login";
                navLogin.setChecked(true);

            }

        }else {

            navType.setVisible(false);
            navOther.setVisible(false);
            fragment = new SettingsFragment();
            bundle.putString("PageName", "Server");
            bundle.putString("ActType", "");
            CURRENT_AVAL = "Server";
            navServer.setChecked(true);
        }

        changeActionBarTitle(CURRENT_AVAL);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
        fragment.setArguments(bundle);
        fragmentTransaction.commitAllowingStateLoss();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)){

            Fragment fragment = null;
            Bundle bundle = new Bundle();


            if (CURRENT_AVAL.equals("Server")){
               // navServer.setChecked(true);
            }else if(CURRENT_AVAL.equals("Login")){
              //  navLogin.setChecked(true);
            }else if(CURRENT_AVAL.equals("Delivery")){
                fragment = new HomeFragment();
                bundle.putString("PageName", "Main");
                bundle.putString("ActType", "Delivery");
                CURRENT_AVAL = "Delivery";
              //  navDelivery.setChecked(true);
            }else if(CURRENT_AVAL.equals("Waste")){
              //  navWaste.setChecked(true);
                fragment = new HomeFragment();

                bundle.putString("PageName", "Main");
                bundle.putString("ActType", "Waste");
                CURRENT_AVAL = "Waste";
            }else if(CURRENT_AVAL.equals("Stock take")){
              //  navStocktake.setChecked(true);
                fragment = new HomeFragment();
                bundle.putString("PageName", "Main");
                bundle.putString("ActType", "Stock take");
                CURRENT_AVAL = "Stock take";

            }else if(CURRENT_AVAL.equals("Price change")){

                /*Fragment page = getSupportFragmentManager().findFragmentById(R.id.frame);
                PriceChangeFragment mFragment = (PriceChangeFragment) page;
                mFragment.onActivityCallBack("Price change","");*/

            }else if(CURRENT_AVAL.equals("Quick add")){

                /*Fragment page = getSupportFragmentManager().findFragmentById(R.id.frame);
                PriceChangeFragment mFragment = (PriceChangeFragment) page;
                mFragment.onActivityCallBack("Price change","");*/

            }else{
            }

            if (fragment != null) {
                changeActionBarTitle(CURRENT_AVAL);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragment.setArguments(bundle);
                fragmentTransaction.commitAllowingStateLoss();
            }


        }
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {



        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }
        */

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment = null;

        Bundle bundle = new Bundle();

        clearMenu();

        if(id == R.id.nav_server) {

            fragment = new SettingsFragment();
            bundle.putString("PageName", "Server");
            bundle.putString("ActType", "");
            CURRENT_AVAL = "Server";
           // navServer.setChecked(true);

        } else if (id == R.id.nav_login) {

            if (db.getSettingsTBCount() > 0) {

                if (pref.getBoolean("ckLogin", false)) {

                    mLogout("Logout");

                }else {

                   // mConnectionCheck("Login");

                    fragment = new LoginFragment();
                    bundle.putString("PageName", "Login");
                    bundle.putString("ActType", "");
                    CURRENT_AVAL = "Login";
                   // navLogin.setChecked(true);
                }


            }else{
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage("Please enter server details");
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                //editText.getText().clear();
                                //txtPort.requestFocus();
                                clearMenu();
                                navServer.setChecked(true);
                            }
                        });


                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }


        } else if (id == R.id.nav_delivery) {

            fragment = new HomeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Delivery");
            CURRENT_AVAL = "Delivery";
           // navDelivery.setChecked(true);

        } else if (id == R.id.nav_waste) {

            fragment = new HomeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Waste");
            CURRENT_AVAL = "Waste";
            //navWaste.setChecked(true);

        } else if (id == R.id.nav_stock_take) {

            fragment = new HomeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Stock take");
            CURRENT_AVAL = "Stock take";
            //navStocktake.setChecked(true);
        } else if (id == R.id.nav_price_change) {

            fragment = new PriceChangeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Price change");
            CURRENT_AVAL = "Price change";
            //navStocktake.setChecked(true);
        } else if (id == R.id.nav_quick_add) {

            /*fragment = new QuickAddFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Quick add");
            CURRENT_AVAL = "Quick add";*/

            //navStocktake.setChecked(true);

            showLoadingActionButton();
            String ckConn = mConnectionCheck("");
            if (ckConn.equals("WifiCon")){

                /*MainTaskParams mParams = new MainTaskParams("QuickAdd", "", 0);
                new LongOperation(false).execute(mParams);*/

                mQuickAddDelay("Yes");
               // mQuickAdd("Yes");
                hideLoadingActionButton();

                //mQuickAdd("Yes");



            }else{
                mQuickAdd("No");
                hideLoadingActionButton();
                finishLongTask("ErrorWifi","Connection error");
            }

           // return true;

        }else{

        }



        if (fragment != null) {
            changeActionBarTitle(CURRENT_AVAL);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
            fragment.setArguments(bundle);
            fragmentTransaction.commitAllowingStateLoss();
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);


        clearMenu();
        if (CURRENT_AVAL.equals("Server")){
            navServer.setChecked(true);
        }else if(CURRENT_AVAL.equals("Login")){
            navLogin.setChecked(true);
        }else if(CURRENT_AVAL.equals("Delivery")){
            navDelivery.setChecked(true);
        }else if(CURRENT_AVAL.equals("Waste")){
            navWaste.setChecked(true);
        }else if(CURRENT_AVAL.equals("Stock take")){
            navStocktake.setChecked(true);
        }else if(CURRENT_AVAL.equals("Price change")){
            navPriceChange.setChecked(true);
        }else if(CURRENT_AVAL.equals("Quick add")){
            navQuickAdd.setChecked(true);
        }else{
        }

        return true;
    }

    private void selectNavMenu() {
        //nav_settings
       // navigationView.getMenu().getItem(0).setChecked(true);
    }

    private static class MainTaskParams {
        String hParaOne;
        String hParaTwo;
        int recPos;


        MainTaskParams(String hParaOne, String hParaTwo, int recPos) {
            this.hParaOne = hParaOne;
            this.hParaTwo = hParaTwo;
            this.recPos = recPos;

        }
    }

    private class LongOperation extends AsyncTask<MainTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "";
        int mPos = 0;

        public LongOperation(boolean showCommit) {
            super();
            // do stuff
            if (showCommit){
               // btnCommit.setVisibility(View.VISIBLE);
              //  showLoadingActionButton();
            }else{
               // btnCommit.setVisibility(View.GONE);
            }

        }

        @Override
        protected String doInBackground(MainTaskParams... params) {




            loadMStrOne  = params[0].hParaOne;
            loadMStrTwo  = params[0].hParaTwo;
            mPos = params[0].recPos;



            //String mVal = params[0];

            System.out.println("The value in doInBackground : " + loadMStrOne);

            String result = "0";

            if (loadMStrOne.equals("QuickAdd")) {

                result = mQucikConn("",0);

            }else{
                result = "0";
            }



            return result;
        }

        @Override
        protected void onPostExecute(String result) {


            hideLoadingActionButton();
            System.out.println("The value in onPostExecute : " + loadMStrOne);

            if (loadMStrOne.equals("QuickAdd")){
                //finishLongTask
                if (result.equals("Success")) {

                   // finishLongTask("NoItem","No Item found");
                    mQuickAdd("Yes");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    mQuickAdd("No");
                    finishLongTask("Error","Connection error");
                }

                // setupRecyclerView();


            }else{


            }


        }

        @Override
        protected void onPreExecute() {

            showLoadingActionButton();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private void finishLongTask (String action,String val){

        if (action.equals("Success")) {





        }else if(action.equals("ClearAll")) {



        }else if(action.equals("Error") || action.equals("SqlExp")) {
            // btnCommit.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
            alertDialogBuilder.setMessage("Connection error, Please check the connection");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();



        }else if(action.equals("ErrorWifi")){

            // hideLoadingActionButton();
            // btnCommit.setVisibility(View.VISIBLE);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
            alertDialogBuilder.setMessage("Connection error, Please connect to WIFI");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        }else{
            hideLoadingActionButton();
        }


    }

    private String mQucikConn(String type, int postion) {
        String ckExp = "";
        stmt = mConnection();
        if (stmt != null) {
            ckExp = "Success";
        }else{
            ckExp = "SqlExp";
            /*Item item = new Item();
            item = mAdapter.items.get(postion);
            ((MainActivity) getActivity()).loadEditFragment(item.getItemID()); */
        }


        return ckExp;

    }

    public void mQuickAddDelay(String typeCK){
        showLoadingActionButton();
        MainTaskParams mParams = new MainTaskParams("QuickAdd", "", 0);
        new LongOperation(true).execute(mParams);
    }
    public void mQuickAdd(String typeCK){

        System.out.println("The value in onPostExecute : " + typeCK);
        //hideLoadingActionButton();

        navigationView.isEnabled();
        if (typeCK.equals("Yes")){

            Fragment fragment = null;
            Bundle bundle = new Bundle();

            fragment = new QuickAddFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Quick add");
            CURRENT_AVAL = "Quick add";

            changeActionBarTitle(CURRENT_AVAL);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
            fragment.setArguments(bundle);
            fragmentTransaction.commitAllowingStateLoss();

            clearMenu();
            if (CURRENT_AVAL.equals("Server")){
                navServer.setChecked(true);
            }else if(CURRENT_AVAL.equals("Login")){
                fragmentTransaction.commitAllowingStateLoss();
                navLogin.setChecked(true);
            }else if(CURRENT_AVAL.equals("Delivery")){
                navDelivery.setChecked(true);
            }else if(CURRENT_AVAL.equals("Waste")){
                navWaste.setChecked(true);
            }else if(CURRENT_AVAL.equals("Stock take")){
                navStocktake.setChecked(true);
            }else if(CURRENT_AVAL.equals("Price change")){
                navPriceChange.setChecked(true);
            }else if(CURRENT_AVAL.equals("Quick add")){
                navQuickAdd.setChecked(true);
            }else{
            }

        }else{
            clearMenu();
            System.out.println("The current tag : " + CURRENT_AVAL);

            if (CURRENT_AVAL.equals("Server")){
                navServer.setChecked(true);
            }else if(CURRENT_AVAL.equals("Delivery")){
                navDelivery.setChecked(true);
            }else if(CURRENT_AVAL.equals("Waste")){
                navWaste.setChecked(true);
            }else if(CURRENT_AVAL.equals("Stock take")){
                navStocktake.setChecked(true);
            }else if(CURRENT_AVAL.equals("Price change")){
                navPriceChange.setChecked(true);
            }else if(CURRENT_AVAL.equals("Quick add")){
                navQuickAdd.setChecked(true);
            }else{
            }
        }



    }

    public void mLogout(String typeCK){

        if (typeCK.equals("Logout")){



            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Are you sure want to logout");
            alertDialogBuilder.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                            //editText.getText().clear();
                            //txtPort.requestFocus();

                            SharedPreferences.Editor editor = pref.edit();
                            editor.putBoolean("ckLogin", false); // Storing boolean - true/false
                            editor.putString("staffID", null); // Storing string
                            editor.putString("staffName", null); // Storing string
                            editor.putString("password", null); // Storing string
                            editor.putString("permission", null); // Storing string
                            editor.putString("staffType", null); // Storing string
                            editor.commit();

                            // MenuItem navType = menuNav.findItem(R.id.nav_type);
                            navType.setVisible(false);
                            navOther.setVisible(false);

                            // MenuItem navLogin = menuNav.findItem(R.id.nav_login);
                            navLogin.setTitle("Login");
                            navLogin.setChecked(true);
                            changeLoginIcon("Login");

                            Fragment fragment = null;
                            Bundle bundle = new Bundle();

                            fragment = new LoginFragment();
                            bundle.putString("PageName", "Login");
                            bundle.putString("ActType", "");
                            CURRENT_AVAL = "Login";


                            changeActionBarTitle(CURRENT_AVAL);
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                            fragment.setArguments(bundle);
                            fragmentTransaction.commitAllowingStateLoss();


                        }
                    });

            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // finish();
                    clearMenu();
                    System.out.println("The current tag : " + CURRENT_AVAL);

                    if (CURRENT_AVAL.equals("Server")){
                        navServer.setChecked(true);
                    }else if(CURRENT_AVAL.equals("Delivery")){
                        navDelivery.setChecked(true);
                    }else if(CURRENT_AVAL.equals("Waste")){
                        navWaste.setChecked(true);
                    }else if(CURRENT_AVAL.equals("Stock take")){
                        navStocktake.setChecked(true);
                    }else if(CURRENT_AVAL.equals("Price change")){
                        navPriceChange.setChecked(true);
                    }else if(CURRENT_AVAL.equals("Quick add")){
                        navQuickAdd.setChecked(true);
                    }else{
                    }


                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else{

            if (pref.getBoolean("ckLogin", false)) {

                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("ckLogin", false); // Storing boolean - true/false
                editor.putString("staffID", null); // Storing string
                editor.putString("staffName", null); // Storing string
                editor.putString("password", null); // Storing string
                editor.putString("permission", null); // Storing string
                editor.putString("staffType", null); // Storing string
                editor.commit();

                navType.setVisible(false);
                navOther.setVisible(false);
                navLogin.setTitle("Login");
                changeLoginIcon("Login");
                Toast.makeText(MainActivity.this,"You have been Logout",Toast.LENGTH_LONG).show();

            }else{

            }


        }



    }

    public void loadEditFragment(String itmID){
        Intent i = new Intent(MainActivity.this, UpdItemActivity.class);
        i.putExtra("ActType", CURRENT_AVAL);
        i.putExtra("ActFrom", "edit");
        i.putExtra("itmID", itmID);
        startActivityForResult(i, 10001);
    }

    public void loadCommitFragment(String type){

        Fragment fragment = null;
        Bundle bundle = new Bundle();


        if (CURRENT_AVAL.equals("Server")){
            // navServer.setChecked(true);
        }else if(CURRENT_AVAL.equals("Login")){
            //  navLogin.setChecked(true);
        }else if(CURRENT_AVAL.equals("Delivery")){
            fragment = new HomeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Delivery");
            CURRENT_AVAL = "Delivery";
            //  navDelivery.setChecked(true);
        }else if(CURRENT_AVAL.equals("Waste")){
            //  navWaste.setChecked(true);
            fragment = new HomeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Waste");
            CURRENT_AVAL = "Waste";
        }else if(CURRENT_AVAL.equals("Stock take")){
            //  navStocktake.setChecked(true);
            fragment = new HomeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Stock take");
            CURRENT_AVAL = "Stock take";
        }else{
        }

        if (fragment != null) {

            changeActionBarTitle(CURRENT_AVAL);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
            fragment.setArguments(bundle);
            fragmentTransaction.commitAllowingStateLoss();
        }

    }
    public void clearMenu(){
       // MenuItem navType = menuNav.findItem(R.id.nav_type);

        navLogin.setChecked(false);
        navServer.setChecked(false);
        navDelivery.setChecked(false);
        navWaste.setChecked(false);
        navStocktake.setChecked(false);
        navPriceChange.setChecked(false);
        navQuickAdd.setChecked(false);
    }

    public void loadFragment(){

        clearMenu();

        Fragment fragment = null;
        Bundle bundle = new Bundle();



        if (pref.getBoolean("ckLogin", true)) {
           // MenuItem navType = menuNav.findItem(R.id.nav_type);



            navType.setVisible(true);
            navOther.setVisible(true);

            //MenuItem navLogin = menuNav.findItem(R.id.nav_login);
            navLogin.setTitle("Logout");
            changeLoginIcon("Logout");
            navDelivery.setChecked(true);

            fragment = new HomeFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Delivery");
            CURRENT_AVAL = "Delivery";



        }else{

            fragment = new LoginFragment();
            bundle.putString("PageName", "Login");
            bundle.putString("ActType", "");
            CURRENT_AVAL = "Login";

        }

        changeActionBarTitle(CURRENT_AVAL);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
        fragment.setArguments(bundle);
        fragmentTransaction.commitAllowingStateLoss();

        hideLoadingActionButton();



    }

    public FloatingActionButton getFloatingActionButton() {
        return fab;
    }

    public void showFloatingActionButton() {
        fab.show();
    }

    public void hideFloatingActionButton() {
        fab.hide();
    }



    public void showLoadingActionButton() {
        loginLoader.setVisibility(View.VISIBLE);
    }

    public void hideLoadingActionButton() {
        loginLoader.setVisibility(View.GONE);
    }



    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void changeActionBarTitle(String val){

        String titleVal = "Franco ePos";

        if (val.equals("Server")) {
            titleVal = "Server Details";
        }else if(val.equals("Login")){
            titleVal = "Login";
        }else if(val.equals("Delivery")){
            titleVal = "Delivery";
        }else if(val.equals("Waste")){
            titleVal = "Waste";
        }else if(val.equals("Stock take")){
            titleVal = "Stock take";
        }else if(val.equals("Price change")){
            titleVal = "Price Change";
        }else if(val.equals("Quick add")){
            titleVal = "Quick Add";
        }else {

        }

        getSupportActionBar().setTitle(titleVal);

    }

    public void changeLoginIcon(String check){

        if (check.equals("Login")){
            navLogin.setIcon(R.drawable.ic_login_image);
        }else{
            navLogin.setIcon(R.drawable.ic_logout_image);
        }

    }

    private Statement mConnection(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        connection = null;
        ConnectionURL = null;
        Statement stmtt = null;


        try
        {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            // ConnectionURL = "jdbc:jtds:sqlserver://192.168.1.13:1433/ePos_Master;user=eposLourdes;password=KrishnaAndJesus;";
            ConnectionURL = "jdbc:jtds:sqlserver://"+ ipaddress +":"+ port +"/ePos_Master;user=eposLourdes;password=KrishnaAndJesus;";
            connection = DriverManager.getConnection(ConnectionURL);

            try {
                stmtt = connection.createStatement();

            } catch (SQLException e) {
                Log.d("myTag", "This is my message stmt");
                e.printStackTrace();
            }

        }
        catch (SQLException se)
        {
            Log.e("error here 1 : ", se.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("error here 2 : ", e.getMessage());
        }
        catch (Exception e)
        {
            Log.e("error here 3 : ", e.getMessage());
        }


        return  stmtt;

    }

    public String mConnectionCheck(String check){


        SettingsTB setTb = db.getSetTBByType("SERVER");
        if (setTb != null){
            ipaddress = setTb.getIpaddress();
            port = setTb.getPort();
        }

        String msg = "";

        ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            // connected to the internet
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    // connected to wifi
                    msg = "WifiCon";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    // connected to mobile data
                    msg = "MobileIntCon";
                    break;
                default:
                    break;
            }
        } else {
            // not connected to the internet
            msg = "NoCon";
        }

        System.out.println(msg);


        return msg;

    }



    private void turnOnStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .detectActivityLeaks()
                    .build());
        }
    }
}
