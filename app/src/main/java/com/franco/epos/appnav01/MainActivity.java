package com.franco.epos.appnav01;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.PListTB;
import com.franco.epos.appnav01.database.model.SettingsTB;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;


import com.franco.epos.appnav01.MyResultReceiver.Receiver;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,Receiver {


    public MyResultReceiver mReceiver;
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
    MenuItem navSettings, navLogin, navServer, navType, navDelivery, navWaste, navStocktake, navOther, navPriceChange, navQuickAdd, navAddLabel, navPLAdd, navPO, navOpenPO, navClosePO;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private String ipaddress, port;

    private RelativeLayout loginLoader;
    private String lType = "" , lVal = "";
    private Handler mHandler;

    private Connection connection = null;
    private String ConnectionURL = null;
    Statement stmt = null;

    public static final int PERMISSION_BLUETOOTH = 1;

    private final Locale locale = new Locale("id", "ID");
    private final DateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", locale);
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
    BluetoothConnection BTconnection;

    // declaring width and height
    // for our PDF file.
    int pageHeight = 1120;
    int pagewidth = 792;

    int pageHeightNet = 3508;
    int pagewidthNet = 2480;

    // creating a bitmap variable
    // for storing our images
    Bitmap bmp, scaledbmp;

    // constant code for runtime permissions
    private static final int PERMISSION_REQUEST_CODE = 200;



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

        //Toast.makeText(MainActivity.this,"Guruvae Potri Potri!",Toast.LENGTH_LONG).show();

        // checking our permissions.
        if (checkPermission()) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
        } else {
            requestPermission();
        }

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mReceiver = new MyResultReceiver(new Handler());

        mReceiver.setReceiver(this);

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
        navAddLabel = menuNav.findItem(R.id.nav_add_label);
        navPLAdd = menuNav.findItem(R.id.nav_pl_add);
        navPO = menuNav.findItem(R.id.nav_po);
        navOpenPO = menuNav.findItem(R.id.nav_open_op);
        navClosePO = menuNav.findItem(R.id.nav_close_op);

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

                Toast.makeText(MainActivity.this,CURRENT_AVAL,Toast.LENGTH_LONG).show();

                if (CURRENT_AVAL == "Open PO"){
                    /*Intent i = new Intent(MainActivity.this, UpdPOItemActivity.class);
                    i.putExtra("ActType", CURRENT_AVAL);
                    i.putExtra("ActFrom", "new");
                    i.putExtra("itmID", "");
                    startActivityForResult(i, 10001);*/
                }else{
                    Intent i = new Intent(MainActivity.this, UpdItemActivity.class);
                    i.putExtra("ActType", CURRENT_AVAL);
                    i.putExtra("ActFrom", "new");
                    i.putExtra("itmID", "");
                    startActivityForResult(i, 10001);
                }



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

                System.out.println("On start 02ssss service database Access1 ");
                System.out.println("On start database Access1" + pref.getString("EXPDATE",null));

                if(pref.getString("EXPDATE",null) == null || pref.getString("EXPDATE",null) == "null" || pref.getString("EXPDATE",null).equalsIgnoreCase("null")){



                }else{
                    System.out.println("On start database Access1 closed" + pref.getString("EXPDATE",null));
                    String exprDate = pref.getString("EXPDATE",null);



                    String[] separatedDT = exprDate.split("-");

                    String serDateTime = separatedDT[0]+"-"+separatedDT[1]+"-"+separatedDT[1]+"T00:00:00";
                    LocalDateTime serLdatetime = LocalDateTime.parse(serDateTime);



                    String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    System.out.println("generated LocalDaddteTime: " + currentDate+"T"+currentTime);

                    String locDateTime = currentDate+"T"+currentTime;
                    LocalDateTime locLdatetime = LocalDateTime.parse(locDateTime);

                    System.out.println("generated LocalDateTime: " + serLdatetime +" ::: "+locLdatetime);
                    if(locLdatetime.isAfter(serLdatetime)){
                        //System.out.println("After: " + serLdatetime +" ::: "+locLdatetime +" Expired");
                        mLogout("LogoutService");

                    }else{

                       // mLogout("LogoutService");

                    }

                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Called when a drawer has settled in a completely closed state.
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
                /*InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);*/

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                //Find the currently focused view, so we can grab the correct window token from it.
                View view = getCurrentFocus();
                //If no view currently has focus, create a new one, just so we can grab a window token from it
                /*if (view == null) {
                    view = new View(activity);
                }*/

                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

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
                navPO.setVisible(true);
                navLogin.setTitle("Logout");

                fragment = new HomeFragment();
                bundle.putString("PageName", "Main");
                bundle.putString("ActType", "Delivery");
                CURRENT_AVAL = "Delivery";
                navDelivery.setChecked(true);

            }else{

                navType.setVisible(false);
                navOther.setVisible(false);
                navPO.setVisible(false);
                fragment = new LoginFragment();
                bundle.putString("PageName", "Login");
                bundle.putString("ActType", "");
                CURRENT_AVAL = "Login";
                navLogin.setChecked(true);

            }

        }else {

            navType.setVisible(false);
            navOther.setVisible(false);
            navPO.setVisible(false);
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
                mFragment.onActivityCallBack("Price change",""); OPO List*/
            }else if(CURRENT_AVAL.equals("Add Label")){

                fragment = new ALFragment();

                bundle.putString("PageName", "Main");
                bundle.putString("ActType", "Add Label");
                CURRENT_AVAL = "Add Label";

            }else if(CURRENT_AVAL.equals("Puchase List")){

                fragment = new PLFragment();

                bundle.putString("PageName", "Main");
                bundle.putString("ActType", "Puchase List");
                CURRENT_AVAL = "Puchase List";

            }else if(CURRENT_AVAL.equals("OPO List")){


                if (data.getStringExtra("AActType").equals("POFRAG")){
                    FragmentManager fm = getSupportFragmentManager();
                    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                        fm.popBackStack();
                    }
                    fragment = new POFragment();
                    bundle.putString("PageName", "Main");
                    bundle.putString("ActType", "Open PO");
                    CURRENT_AVAL = "Open PO";
                }else {
                    fragment = new OPOFragment();
                    bundle.putString("PageName", "Main");
                    bundle.putString("ActType", "OPO List");
                    bundle.putString("AActType", data.getStringExtra("AActType"));
                    bundle.putString("POCode", data.getStringExtra("POCode"));
                    bundle.putString("POStatus", data.getStringExtra("POStatus"));
                }


                CURRENT_AVAL = "OPO List";
            }else if(CURRENT_AVAL.equals("CPO List")){


                if (data.getStringExtra("AActType").equals("POFRAG")){
                    FragmentManager fm = getSupportFragmentManager();
                    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                        fm.popBackStack();
                    }
                    fragment = new POFragment();
                    bundle.putString("PageName", "Main");
                    bundle.putString("ActType", "Close PO");
                    CURRENT_AVAL = "Close PO";
                }else {
                    fragment = new CPOFragment();
                    bundle.putString("PageName", "Main");
                    bundle.putString("ActType", "CPO List");
                    bundle.putString("AActType", data.getStringExtra("AActType"));
                    bundle.putString("POCode", data.getStringExtra("POCode"));
                    bundle.putString("POStatus", data.getStringExtra("POStatus"));
                }


                CURRENT_AVAL = "CPO List";

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
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }

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
        } else if (id == R.id.nav_add_label) {

            fragment = new ALFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Add Label");
            CURRENT_AVAL = "Add Label";

        } else if (id == R.id.nav_pl_add) {

            fragment = new PLFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Puchase List");
            CURRENT_AVAL = "Puchase List";

        } else if (id == R.id.nav_open_op) {
            fragment = new POFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Open PO");
            CURRENT_AVAL = "Open PO";
        } else if (id == R.id.nav_close_op) {
            fragment = new POFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Close PO");
            CURRENT_AVAL = "Close PO";
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
        }else if(CURRENT_AVAL.equals("Add Label")){
            navAddLabel.setChecked(true);
        }else if(CURRENT_AVAL.equals("Puchase List")){
            navPLAdd.setChecked(true);
        }else if(CURRENT_AVAL.equals("Open PO")){
            navOpenPO.setChecked(true);
        }else if(CURRENT_AVAL.equals("Close PO")){
            navClosePO.setChecked(true);
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

                result = mQucikConn("", 0);

            }else{
                result = "0";
            }



            return result;
        }

        @Override
        protected void onPostExecute(String result) {


            hideLoadingActionButton();
            System.out.println("The value in onPostExecute : " + loadMStrOne);

            if (loadMStrOne.equals("QuickAdd")) {
                //finishLongTask
                if (result.equals("Success")) {

                    // finishLongTask("NoItem","No Item found");
                    mQuickAdd("Yes");

                } else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")) {

                    mQuickAdd("No");
                    finishLongTask("Error", "Connection error");
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


        } else if (action.equals("NoPrExp")) {
            //finishLongTask("NoPrExp", "No Printer connected");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
            alertDialogBuilder.setMessage("Connection error, No Printer connected");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else if (action.equals("ckPrExp")) {
            //finishLongTask("ckPrExp", "Check Printer connection");
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
            alertDialogBuilder.setMessage("Connection error, Please Check Printer connection");
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
            }else if(CURRENT_AVAL.equals("Add Label")){
                navAddLabel.setChecked(true);
            }else if(CURRENT_AVAL.equals("Puchase List")){
                navPLAdd.setChecked(true);
            }else if(CURRENT_AVAL.equals("Open PO")){
                navOpenPO.setChecked(true);
            }else if(CURRENT_AVAL.equals("Close PO")){
                navClosePO.setChecked(true);
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
            }else if(CURRENT_AVAL.equals("Add Label")){
                navAddLabel.setChecked(true);
            }else if(CURRENT_AVAL.equals("Puchase List")){
                navPLAdd.setChecked(true);
            }else if(CURRENT_AVAL.equals("Open PO")){
                navOpenPO.setChecked(true);
            }else if(CURRENT_AVAL.equals("Close PO")){
                navClosePO.setChecked(true);
            }else{
            }
        }



    }

    public void mLogout(String typeCK){

        if (typeCK.equals("Logout")) {


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
                            editor.putString("staffType", null);
                            editor.putString("PCID", "null");
                            editor.putString("EXPDATE", "null");// Storing string
                            editor.commit();

                            // MenuItem navType = menuNav.findItem(R.id.nav_type);
                            navType.setVisible(false);
                            navOther.setVisible(false);
                            navPO.setVisible(false);

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

            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // finish();
                    clearMenu();
                    System.out.println("The current tag : " + CURRENT_AVAL);

                    if (CURRENT_AVAL.equals("Server")) {
                        navServer.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Delivery")) {
                        navDelivery.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Waste")) {
                        navWaste.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Stock take")) {
                        navStocktake.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Price change")) {
                        navPriceChange.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Quick add")) {
                        navQuickAdd.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Add Label")) {
                        navAddLabel.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Puchase List")) {
                        navPLAdd.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Open PO")) {
                        navOpenPO.setChecked(true);
                    } else if (CURRENT_AVAL.equals("Close PO")) {
                        navClosePO.setChecked(true);
                    } else {
                    }


                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(typeCK.equals("LogoutService")){
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("ckLogin", false); // Storing boolean - true/false
            editor.putString("staffID", null); // Storing string
            editor.putString("staffName", null); // Storing string
            editor.putString("password", null); // Storing string
            editor.putString("permission", null); // Storing string
            editor.putString("staffType", null);
            editor.putString("PCID", "null");
            editor.putString("EXPDATE", "null");// Storing string
            editor.commit();

            // MenuItem navType = menuNav.findItem(R.id.nav_type);
            navType.setVisible(false);
            navOther.setVisible(false);
            navPO.setVisible(false);

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
                navPO.setVisible(false);
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

    public void loadPOEditFragment(String type,String itmID, String poCode, String poStatus){

        if (type.equals("ScanAdd")){
            Intent i = new Intent(MainActivity.this, UpdPOItemActivity.class);
            i.putExtra("ActType", CURRENT_AVAL);
            if (itmID.equals("")){
                i.putExtra("ActFrom", "new");
                i.putExtra("itmID", "");
            }else{
                i.putExtra("ActFrom", "edit");
                i.putExtra("itmID", itmID);
            }

            i.putExtra("AActType",type);
            i.putExtra("POCode", poCode);
            i.putExtra("POStatus", poStatus);
            startActivityForResult(i, 10001);
        }else{
            Intent i = new Intent(MainActivity.this, POFilterActivity.class);
            i.putExtra("ActType", CURRENT_AVAL);
            i.putExtra("ActFrom", "new");
            i.putExtra("AActType",type);
            i.putExtra("itmID", "");
            i.putExtra("POCode", poCode);
            i.putExtra("POStatus", poStatus);
            startActivityForResult(i, 10001);
        }



        Log.d("myTag ", "This is my message Current val " + CURRENT_AVAL);

    }

    public void loadPOCSEditFragment(String type,String itmID,String poCode, String poStatus){

        if (type.equals("COMMIT")) {
            Intent i = new Intent(MainActivity.this, UpdPOCSItemActivity.class);
            i.putExtra("ActType", CURRENT_AVAL);
            if (itmID.equals("")) {
                i.putExtra("ActFrom", "new");
                i.putExtra("itmID", "");
            } else {
                i.putExtra("ActFrom", "edit");
                i.putExtra("itmID", itmID);
            }

            i.putExtra("AActType", type);
            i.putExtra("POCode", poCode);
            i.putExtra("POStatus", poStatus);

            startActivityForResult(i, 10001);
        }else if (type.equals("SAVE")){
            Intent i = new Intent(MainActivity.this, UpdPOCSItemActivity.class);
            i.putExtra("ActType", CURRENT_AVAL);
            if (itmID.equals("")) {
                i.putExtra("ActFrom", "new");
                i.putExtra("itmID", "");
            } else {
                i.putExtra("ActFrom", "edit");
                i.putExtra("itmID", itmID);
            }

            i.putExtra("AActType", type);
            i.putExtra("POCode", poCode);
            i.putExtra("POStatus", poStatus);

            startActivityForResult(i, 10001);
        }else{
            /*Intent i = new Intent(MainActivity.this, POFilterActivity.class);
            i.putExtra("ActType", CURRENT_AVAL);
            i.putExtra("ActFrom", "new");
            i.putExtra("AActType",type);
            i.putExtra("itmID", "");
            startActivityForResult(i, 10001);*/
        }



        Log.d("myTag ", "This is my message Current val " + CURRENT_AVAL);

    }

    public void loadCPOEditFragment(String type,String itmID, String poCode, String poStatus){

        if (type.equals("ScanAdd")){
            Intent i = new Intent(MainActivity.this, UpdCPOItemActivity.class);
            i.putExtra("ActType", CURRENT_AVAL);
            if (itmID.equals("")){
                i.putExtra("ActFrom", "new");
                i.putExtra("itmID", "");
            }else{
                i.putExtra("ActFrom", "edit");
                i.putExtra("itmID", itmID);
            }

            i.putExtra("AActType",type);
            i.putExtra("POCode", poCode);
            i.putExtra("POStatus", poStatus);
            startActivityForResult(i, 10001);
        }else{
            /*Intent i = new Intent(MainActivity.this, POFilterActivity.class);
            i.putExtra("ActType", CURRENT_AVAL);
            i.putExtra("ActFrom", "new");
            i.putExtra("AActType",type);
            i.putExtra("itmID", "");
            i.putExtra("POCode", poCode);
            i.putExtra("POStatus", poStatus);
            startActivityForResult(i, 10001);*/
        }



        Log.d("myTag ", "This is my message Current val " + CURRENT_AVAL);

    }

    public void loadCPOCSEditFragment(String type,String itmID, String poCode, String poStatus){
        Fragment fragment = null;
        Bundle bundle = new Bundle();

        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
        fragment = new POFragment();
        bundle.putString("PageName", "Main");
        bundle.putString("ActType", "Close PO");

        CURRENT_AVAL = "Close PO";

        if (fragment != null) {
            changeActionBarTitle(CURRENT_AVAL);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
            fragment.setArguments(bundle);
            fragmentTransaction.commitAllowingStateLoss();
        }



        Log.d("myTag ", "This is my message Current val " + CURRENT_AVAL);

    }
    public void loadALFragment(String type){

        Intent i = new Intent(MainActivity.this, UpdALItemActivity.class);
        i.putExtra("ActType", CURRENT_AVAL);
        i.putExtra("ActFrom", "new");
        i.putExtra("itmID", "");
        startActivityForResult(i, 10001);


    }
    /*public void loadALEditFragment(String itmID){
        Intent i = new Intent(MainActivity.this, UpdItemActivity.class);
        i.putExtra("ActType", CURRENT_AVAL);
        i.putExtra("ActFrom", "edit");
        i.putExtra("itmID", itmID);
        startActivityForResult(i, 10001);
    }*/

    public void loadPLFragment(String value){
        Intent i = new Intent(MainActivity.this, UpdPLItemActivity.class);
        i.putExtra("ActType", CURRENT_AVAL);
        if(value != ""){
            i.putExtra("ActFrom", "edit");
            i.putExtra("itmID", value);
        }else{
            i.putExtra("ActFrom", "new");
            i.putExtra("itmID", "");
        }


        startActivityForResult(i, 10001);



    }
    /*public void mBTThermalPrint(String value){
        //showLoadingActionButton();
        *//*MainTaskParams mParams = new MainTaskParams("BTPrint", "", 0);
        new LongOperation(true).execute(mParams);*//*
        *//*final Handler handler = new Handler(Looper.getMainLooper());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Do something after 100ms
                mBTThermalPrinter();
            }
        }, 100);*//*
        //handler.removeMessages(0);



        *//*Handler().postDelayed({
                //Do something after 100ms
                mBTThermalPrinter();
        }, 100);
*//*
        *//*try {
            Thread.sleep(100);
            mBTThermalPrinter();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*//*
        mBTThermalPrinter();


    }*/

    public String mBTThermalPrinter(){


        String Stars = "------------------------------------------------";
        String stars1 = "-----------------------------------------------";
        String asterick = "************************************************";
        String Blanks = "                                         ";
        String Zeros = "000000000000000000000000000000000000000000000000";



        Integer count = 0;
        String ckExp = "Success";
        /*List<Item> items = new ArrayList<>();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        connection = null;
        ConnectionURL = null;
        try
        {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://"+ ipaddress +":"+ port +"/ePos_Master;user=eposLourdes;password=KrishnaAndJesus;";
            connection = DriverManager.getConnection(ConnectionURL);
            stmt = null;
            try {
                stmt = connection.createStatement();

                try {

                    ResultSet rs = stmt.executeQuery("select * from POSConfig");
                    //select * from PoCode
                    //SampleArrayList.clear();
                    while (rs.next()){
                        count = count + 1;

                        //mobileArray.
                        Item item = new Item();
                        item.setItemID(rs.getString("id"));
                        item.setItemCode(rs.getString("PoCode"));
                        item.setDescr(rs.getString("descp"));
                        item.setSDate(rs.getString("datetim"));
                        item.setStatus(rs.getString("status"));
                        items.add(item);
                        //Log.d("myTag ", "This is my message rss" + rs.getString("descp") );





                    }

                    if (count > 0){

                        ckExp = "Success";

                    }else{

                        ckExp = "NoRecord";

                    }

                } catch (SQLException e) {

                    Log.d("myTag", "This is my message rss" + e.getMessage() );
                    e.printStackTrace();
                    ckExp = "SqlExp";

                }

            } catch (SQLException e) {
                Log.d("myTag", "This is my message stmt");
                e.printStackTrace();
                ckExp = "SqlExp";
            }

        }
        catch (SQLException se)
        {
            Log.e("error here 1 : ", se.getMessage());
            ckExp = "SqlExp";
        }
        catch (ClassNotFoundException e)
        {
            Log.e("error here 2 : ", e.getMessage());
            ckExp = "ClassExp";
        }
        catch (Exception e)
        {
            Log.e("error here 3 : ", e.getMessage());

            ckExp = "Exp";
        }
        Log.d("myTag", "This is my message rss" + count );*/


        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
            } else {

                try {
                    //BluetoothConnection BTconnection = BluetoothPrintersConnections.selectFirstPaired();

                    for(int l=0; l<=5; l++){
                        BTconnection = BluetoothPrintersConnections.selectFirstPaired();
                        if (BTconnection != null){
                            break;
                        }
                    }

                    BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
                    BluetoothConnection[] bluetoothPrinters = printers.getList();
                    if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
                        for (BluetoothConnection printer : bluetoothPrinters) {
                            try {
                                BTconnection = printer.connect();
                            } catch (EscPosConnectionException e) {
                                e.printStackTrace();
                            }
                        }
                    }



                    if (BTconnection != null) {
                        EscPosPrinter printer = new EscPosPrinter(printers.selectFirstPaired(), 203, 68f, 47);

                        String printItem = "";
                        List<PListTB> itemsP = db.getAllPListTB();
                        if (itemsP.size() > 0){
                            for (int j=0; j<itemsP.size();j++){

                                String itemDescrPr = itemsP.get(j).getItem_descr();
                                if(itemDescrPr.length() > 25){
                                    itemDescrPr = itemDescrPr.substring(0,25);
                                }

                                printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;



                            }
                            //ckExp = "Success";
                        }else{
                            // ckExp = "NoRecord";
                        }
                        printer.printFormattedText(
                                "[L]\n" +
                                        "[C]"+asterick+"\n" +
                                        "[C]Purchase List\n" +
                                        "[C]"+asterick+"\n" +
                                        "[L]\n" +
                                        "[L]"+"Qty " + "Description" + "            " + "  " + "  " + "[R]Each Price"+"\n" +
                                        "[C]"+Stars+"\n" +
                                        printItem +
                                        "[C]"+asterick+"\n"
                                );


                        printer.printFormattedTextAndCut("\n\n");
                        printer.disconnectPrinter();
                        ckExp = "Success";
                    } else {
                        //Toast.makeText(this, "No printer was connected!", Toast.LENGTH_SHORT).show();
                        ckExp = "NoPrExp";
                    }
                }catch (Exception e) {
                    Log.e("APP", "Can't print", e);
                    ckExp = "ckPrExp";
                }


            }
        } catch (Exception e) {
            Log.e("APP", "Can't print", e);
            ckExp = "ckPrExp";
        }
        //hideLoadingActionButton();
        //loadPLFragment("");
        return ckExp;
    }


    public String mSendPLMailed(){






        String Stars = "------------------------------------------------";
        String stars1 = "-----------------------------------------------";
        String asterick = "************************************************";
        String Blanks = "                                         ";
        String Zeros = "000000000000000000000000000000000000000000000000";

       // bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_login_image);
        //scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

        Integer count = 0;
        String ckExp = "Success";

        // creating an object variable
        // for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        Paint paint = new Paint();
        Paint title = new Paint();
        PdfDocument.PageInfo mypageInfo;
        PdfDocument.Page myPage = null;
        Canvas canvas = null;



        // below line is used for setting
        // our text to center of PDF.
        int lineY = 100;
        String printItem = "";
        String pageFlag = "Start";

        List<PListTB> itemsP = db.getAllPListTB();

        if (itemsP.size() > 0){

            int pageVal = 0;
            int pageEVal = 0;
           // float k = (float) 10.00;
            for (int j=0; j<itemsP.size();j++){

                //int mVal = (j%20);
                pageEVal = pageEVal + 1;

                if (pageFlag == "Start"){
                    pageFlag = "Finish";
                    lineY = 100;
                    pageVal = pageVal + 1;
                    pageEVal = 1;
                    mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, pageVal).create();
                    myPage = pdfDocument.startPage(mypageInfo);
                    canvas = myPage.getCanvas();


                    paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(20, 85, 780, 65, paint);


                    title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    title.setTextSize(20);
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("Purchase List", 400, 40, title);


                    title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(15);

                    title.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText("Qty " + "Description" + "      ", 30, 80, title);
                    title.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText("Each Price", 750, 80, title);
                }

                String itemDescrPr = itemsP.get(j).getItem_descr();
                if(itemDescrPr.length() > 25){
                    itemDescrPr = itemDescrPr.substring(0,25);
                }

                //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;

                title.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(itemsP.get(j).getQty()+" " + itemDescrPr + "      ", 30, lineY, title);
                title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(itemsP.get(j).getPrice(), 750, lineY, title);
                lineY = lineY + 20;



                if(itemsP.size() == (j+1) && pageFlag == "Finish"){
                    pdfDocument.finishPage(myPage);
                    pageFlag = "Start";
                }
                if (pageEVal == 50 && pageFlag == "Finish"){
                    pdfDocument.finishPage(myPage);
                    pageFlag = "Start";
                }


            }


            //ckExp = "Success";
        }else{
            // ckExp = "NoRecord";
        }
        //pdfDocument.finishPage(myPage);





        // below line is used to set the name of
        // our PDF file and its path.
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Franco/";
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }
        File pdfFile = new File(rootPath, "FrancoePosPurchaseList.pdf");

        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(new FileOutputStream(pdfFile));

            // below line is to print toast message
            // on completion of PDF generation.
            Toast.makeText(MainActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // below line is used
            // to handle error
            e.printStackTrace();
        }
        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close();



       // File file = new File(stringFile);
        if (!pdfFile.exists()){
            Toast.makeText(this, "File doesn't exists", Toast.LENGTH_LONG).show();
            //return;
        }
        Intent intentShare = new Intent(Intent.ACTION_SEND);
        /*intentShare.setData(Uri.parse("info@francoepos.co.uk"));
        intentShare.setPackage("com.google.android.gm");
        intentShare.putExtra(Intent.EXTRA_EMAIL, new String[] { "info@francoepos.co.uk" });*/
        intentShare.putExtra(Intent.EXTRA_SUBJECT,"Franco Purchase List");
        intentShare.putExtra(Intent.EXTRA_TEXT,"Please check the attachement");
        intentShare.setType("application/pdf");
        intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+pdfFile));
        startActivity(Intent.createChooser(intentShare, "Share the file ..."));



        return ckExp;
    }


    public String mBTThermalALPrinter(List<Dictionary> arrayList1){


        String Stars = "------------------------------------------------";
        String stars1 = "-----------------------------------------------";
        String asterick = "************************************************";
        String Blanks = "                                         ";
        String Zeros = "000000000000000000000000000000000000000000000000";



        Integer count = 0;
        String ckExp = "Success";
        /*List<Item> items = new ArrayList<>();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        connection = null;
        ConnectionURL = null;
        try
        {

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://"+ ipaddress +":"+ port +"/ePos_Master;user=eposLourdes;password=KrishnaAndJesus;";
            connection = DriverManager.getConnection(ConnectionURL);
            stmt = null;
            try {
                stmt = connection.createStatement();

                try {

                    ResultSet rs = stmt.executeQuery("select * from POSConfig");
                    //select * from PoCode
                    //SampleArrayList.clear();
                    while (rs.next()){
                        count = count + 1;

                        //mobileArray.
                        Item item = new Item();
                        item.setItemID(rs.getString("id"));
                        item.setItemCode(rs.getString("PoCode"));
                        item.setDescr(rs.getString("descp"));
                        item.setSDate(rs.getString("datetim"));
                        item.setStatus(rs.getString("status"));
                        items.add(item);
                        //Log.d("myTag ", "This is my message rss" + rs.getString("descp") );





                    }

                    if (count > 0){

                        ckExp = "Success";

                    }else{

                        ckExp = "NoRecord";

                    }

                } catch (SQLException e) {

                    Log.d("myTag", "This is my message rss" + e.getMessage() );
                    e.printStackTrace();
                    ckExp = "SqlExp";

                }

            } catch (SQLException e) {
                Log.d("myTag", "This is my message stmt");
                e.printStackTrace();
                ckExp = "SqlExp";
            }

        }
        catch (SQLException se)
        {
            Log.e("error here 1 : ", se.getMessage());
            ckExp = "SqlExp";
        }
        catch (ClassNotFoundException e)
        {
            Log.e("error here 2 : ", e.getMessage());
            ckExp = "ClassExp";
        }
        catch (Exception e)
        {
            Log.e("error here 3 : ", e.getMessage());

            ckExp = "Exp";
        }
        Log.d("myTag", "This is my message rss" + count );*/

        System.out.println("The count fo tdicto : " + arrayList1.size());
        /*Dictionary dictTemp = new Hashtable();
        dictTemp = arrayList1.get(1);
        System.out.println("The count fo tdicto : " + dictTemp.get("itemLU"));*/

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, MainActivity.PERMISSION_BLUETOOTH);
            } else {

                try {
                    //BluetoothConnection BTconnection = BluetoothPrintersConnections.selectFirstPaired();

                    for(int l=0; l<=5; l++){
                        BTconnection = BluetoothPrintersConnections.selectFirstPaired();
                        if (BTconnection != null){
                            break;
                        }
                    }

                    BluetoothPrintersConnections printers = new BluetoothPrintersConnections();
                    BluetoothConnection[] bluetoothPrinters = printers.getList();
                    if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
                        for (BluetoothConnection printer : bluetoothPrinters) {
                            try {
                                BTconnection = printer.connect();
                            } catch (EscPosConnectionException e) {
                                e.printStackTrace();
                            }
                        }
                    }



                    if (BTconnection != null) {
                        EscPosPrinter printer = new EscPosPrinter(printers.selectFirstPaired(), 203, 68f, 47);

                        String printItem = "";
                        /*for (int i = 0; i < myList.size(); i++) {

                            // Print all elements of List
                            System.out.println(myList.get(i));
                        }*/
                        //List<PListTB> itemsP = db.getAllPListTB();
                        if (arrayList1.size() > 0){
                            for (int j=0; j<arrayList1.size();j++){

                                Dictionary dictTemp = new Hashtable();
                                dictTemp = arrayList1.get(j);
                                System.out.println("The count fo tdicto : " + dictTemp.get("descr"));

                                String itemDescrPr = dictTemp.get("descr").toString();
                                if(itemDescrPr.length() > 25){
                                    itemDescrPr = itemDescrPr.substring(0,25);
                                }

                               // printItem = "[L]"+dictTemp.get("barCode").toString()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+dictTemp.get("price").toString()+"\n" + printItem;




                                printer.printFormattedTextAndCut("[L]\n" +
                                        "[L]\n" +
                                        "[L]"+ itemDescrPr + "      " + "  " + "\n" + "[R]"+dictTemp.get("price").toString()+"\n\n"  +
                                        "[R]<barcode type='ean13' height='20'>"+dictTemp.get("barCode")+"</barcode>\n"+
                                        "[L]\n\n\n");



                            }
                            //ckExp = "Success";
                        }else{
                            // ckExp = "NoRecord";
                        }
                        /*printer.printFormattedText(
                                "[L]\n" +
                                        "[C]"+asterick+"\n" +
                                        "[C]Purchase List\n" +
                                        "[C]"+asterick+"\n" +
                                        "[L]\n" +
                                        "[L]"+"Qty " + "Description" + "            " + "  " + "  " + "[R]Each Price"+"\n" +
                                        "[C]"+Stars+"\n" +
                                        printItem +
                                        "[C]"+asterick+"\n"
                        );


                        printer.printFormattedTextAndCut("\n\n");*/


                        //printer.disconnectPrinter();
                        ckExp = "Success";
                    } else {
                        //Toast.makeText(this, "No printer was connected!", Toast.LENGTH_SHORT).show();
                        ckExp = "NoPrExp";
                    }
                }catch (Exception e) {
                    Log.e("APP", "Can't print", e);
                    ckExp = "ckPrExp";
                }


            }
        } catch (Exception e) {
            Log.e("APP", "Can't print", e);
            ckExp = "ckPrExp";
        }
        //hideLoadingActionButton();
        //loadPLFragment("");
        return ckExp;
    }
    public String mNetworkPrinter(List<Dictionary> arrayList1, String template){

       pageHeight = 1120;
       pagewidth = 792;

        pageHeightNet = 1150;//1135
        pagewidthNet = 850;

        String Stars = "------------------------------------------------";
        String stars1 = "-----------------------------------------------";
        String asterick = "************************************************";
        String Blanks = "                                         ";
        String Zeros = "000000000000000000000000000000000000000000000000";

        // bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_login_image);
        //scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

        Integer count = 0;
        String ckExp = "Success";

        // creating an object variable
        // for our PDF document.
        PdfDocument pdfDocument = new PdfDocument();

        Paint paint = new Paint();
        Paint title = new Paint();
        PdfDocument.PageInfo mypageInfo;
        PdfDocument.Page myPage = null;
        Canvas canvas = null;
        BitMatrix bitMatrix = null;



        // below line is used for setting
        // our text to center of PDF.
       // int lineY = 85;
        String printItem = "";
        String pageFlag = "Start";
        //String template = "FRA PREMIER";
        Rect rectMeasure = new Rect();

      //  List<PListTB> itemsP = db.getAllPListTB();

        if (arrayList1.size() > 0){
            int pageVal = 0;
            int pageEVal = 0;

            int celWidth = 0;
            int celTop = 85;
            int celck = 1;
            int billtD = 0;

            // float k = (float) 10.00;
            for (int j=0; j<arrayList1.size();j++){
                Dictionary dictTemp = new Hashtable();
                dictTemp = arrayList1.get(j);
                pageEVal = pageEVal + 1;


                if (template.equals("Template 1")) {

                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 85;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();


                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(20, 85, 780, 65, paint);


                    title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    title.setTextSize(20);
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("Purchase List", 400, 40, title);


                    title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(15);

                    title.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText("Qty " + "Description" + "      ", 30, 80, title);
                    title.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText("Each Price", 750, 80, title);*/
                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 30;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 27;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 27;



                    int cellwidthtemp = celWidth + 20;
                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    cellwidthtemp = cellwidthtemp + 135;

                    paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, billtD + 25, title);

                    celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celTop = celTop + 150;
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }

                }else if (template.equals("Template 1 (WITH OFFER)")){
                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 85;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();



                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();

                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 30;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 27;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 27;



                    int cellwidthtemp = celWidth + 20;
                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    cellwidthtemp = cellwidthtemp + 135;

                    paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, billtD + 25, title);

                    String discountDescr = dictTemp.get("discountDescr").toString();
                    String discountDescr01 = "";
                    String discountDescr02 = "";
                    if(discountDescr.length() > 17){
                        int remDisLen = discountDescr.length() - 17;
                        discountDescr01 = discountDescr.substring(0,17);
                        if(remDisLen > 0){
                            if(remDisLen > 17){
                                discountDescr02 = discountDescr.substring(17,35);
                            }else{
                                discountDescr02 = discountDescr.substring(17,discountDescr.length());
                            }

                        }else{

                        }

                    }else{
                        discountDescr01 = discountDescr;
                    }

                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText(discountDescr01, cellwidthtemp, billtD+50, title);

                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText(discountDescr02, cellwidthtemp, billtD+60, title);

                    celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celTop = celTop + 150;
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }

                }else if (template.equals("Template 2")){

                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 85;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();


                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(20, 85, 780, 65, paint);


                    title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    title.setTextSize(20);
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("Purchase List", 400, 40, title);


                    title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(15);

                    title.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText("Qty " + "Description" + "      ", 30, 80, title);
                    title.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText("Each Price", 750, 80, title);*/
                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 30;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 22;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 22;



                    int cellwidthtemp = celWidth + 20;
                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    cellwidthtemp = cellwidthtemp + 135;

                    paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("vatPrice").toString(), cellwidthtemp + (100 - widthPrice) / 2, billtD + 15, title);


                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText("Exc", cellwidthtemp + 80, billtD + 30, title);

                    /*Paint paint2Price = new Paint();
                    paint2Price.setTextSize(9);
                    paint2Price.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paint2Price.setColor(Color.BLACK);
                    paint2Price.setStyle(Paint.Style.FILL);
                    paint2Price.getTextBounds("£" + dictTemp.get("price").toString() + " inc", 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float width2Price = rectMeasure.width();
                    float height2Price = rectMeasure.height();*/


                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText("£" + dictTemp.get("price").toString() + " inc", cellwidthtemp + (100 - widthPrice) / 2, billtD + 50, title);

                    celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celTop = celTop + 150;
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }

                }else if (template.equals("Template 3")){

                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 53;
                        celck = 1;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();

                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 41;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 22;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 17;



                    int cellwidthtemp = 0;

                    if(celck == 1){
                        cellwidthtemp = celWidth + 5;
                    }else if(celck == 2){
                        cellwidthtemp = celWidth + 15;
                    }else if(celck == 3){
                        cellwidthtemp = celWidth + 15;
                    }

                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    //cellwidthtemp = cellwidthtemp + 135;
                    if(celck == 1){
                        cellwidthtemp = cellwidthtemp + 140;
                    }else if(celck == 2){
                        cellwidthtemp = cellwidthtemp + 135;
                    }else if(celck == 3){
                        cellwidthtemp = cellwidthtemp + 135;
                    }

                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);*/


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, billtD + 25, title);

                    if(celck == 1){
                        celck = 2;
                        celWidth = celWidth + 264;
                    }else if(celck == 2){
                        celck = 3;
                        celWidth = celWidth + 272;
                    }

                   // celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celck = 1;
                        if(((j+1) % 2) == 0) {
                            celTop = celTop + 152;
                        }else{
                            celTop = celTop + 147;
                        }

                    }else if(((j+1) % 2) == 0) {
                    }else{
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }

                }else if (template.equals("Template 3 (WITH OFFER)")){

                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 53;
                        celck = 1;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();

                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 41;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 22;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 17;



                    int cellwidthtemp = 0;

                    if(celck == 1){
                        cellwidthtemp = celWidth + 5;
                    }else if(celck == 2){
                        cellwidthtemp = celWidth + 15;
                    }else if(celck == 3){
                        cellwidthtemp = celWidth + 15;
                    }

                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    //cellwidthtemp = cellwidthtemp + 135;
                    if(celck == 1){
                        cellwidthtemp = cellwidthtemp + 140;
                    }else if(celck == 2){
                        cellwidthtemp = cellwidthtemp + 135;
                    }else if(celck == 3){
                        cellwidthtemp = cellwidthtemp + 135;
                    }

                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);*/


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, billtD + 25, title);

                    String discountDescr = dictTemp.get("discountDescr").toString();
                    String discountDescr01 = "";
                    String discountDescr02 = "";
                    if(discountDescr.length() > 17){
                        int remDisLen = discountDescr.length() - 17;
                        discountDescr01 = discountDescr.substring(0,17);
                        if(remDisLen > 0){
                            if(remDisLen > 17){
                                discountDescr02 = discountDescr.substring(17,35);
                            }else{
                                discountDescr02 = discountDescr.substring(17,discountDescr.length());
                            }

                        }else{

                        }

                    }else{
                        discountDescr01 = discountDescr;
                    }

                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText(discountDescr01, cellwidthtemp, billtD+50, title);

                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText(discountDescr02, cellwidthtemp, billtD+60, title);

                    if(celck == 1){
                        celck = 2;
                        celWidth = celWidth + 264;
                    }else if(celck == 2){
                        celck = 3;
                        celWidth = celWidth + 272;
                    }

                    // celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celck = 1;
                        if(((j+1) % 2) == 0) {
                            celTop = celTop + 152;
                        }else{
                            celTop = celTop + 147;
                        }

                    }else if(((j+1) % 2) == 0) {
                    }else{
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }

                }else if (template.equals("FRA PREMIER")){

                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 53;
                        celck = 1;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();

                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 41;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 22;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 17;



                    int cellwidthtemp = 0;

                    if(celck == 1){
                        cellwidthtemp = celWidth + 5;
                    }else if(celck == 2){
                        cellwidthtemp = celWidth + 15;
                    }else if(celck == 3){
                        cellwidthtemp = celWidth + 15;
                    }

                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    //cellwidthtemp = cellwidthtemp + 135;
                    if(celck == 1){
                        cellwidthtemp = cellwidthtemp + 140;
                    }else if(celck == 2){
                        cellwidthtemp = cellwidthtemp + 135;
                    }else if(celck == 3){
                        cellwidthtemp = cellwidthtemp + 135;
                    }

                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);*/


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, (billtD - 30) + 25, title);

                    if(celck == 1){
                        celck = 2;
                        celWidth = celWidth + 264;
                    }else if(celck == 2){
                        celck = 3;
                        celWidth = celWidth + 272;
                    }

                    // celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celck = 1;
                        if(((j+1) % 2) == 0) {
                            celTop = celTop + 152;
                        }else{
                            celTop = celTop + 147;
                        }

                    }else if(((j+1) % 2) == 0) {
                    }else{
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }

                }else if (template.equals("FRA PREMIER (WITH OFFER)")){
                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 53;
                        celck = 1;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();

                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 41;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 22;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 17;



                    int cellwidthtemp = 0;

                    if(celck == 1){
                        cellwidthtemp = celWidth + 5;
                    }else if(celck == 2){
                        cellwidthtemp = celWidth + 15;
                    }else if(celck == 3){
                        cellwidthtemp = celWidth + 15;
                    }

                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    //cellwidthtemp = cellwidthtemp + 135;
                    if(celck == 1){
                        cellwidthtemp = cellwidthtemp + 140;
                    }else if(celck == 2){
                        cellwidthtemp = cellwidthtemp + 135;
                    }else if(celck == 3){
                        cellwidthtemp = cellwidthtemp + 135;
                    }

                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);*/


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, (billtD - 30) + 25, title);

                    String discountDescr = dictTemp.get("discountDescr").toString();
                    String discountDescr01 = "";
                    String discountDescr02 = "";
                    if(discountDescr.length() > 17){
                        int remDisLen = discountDescr.length() - 17;
                        discountDescr01 = discountDescr.substring(0,17);
                        if(remDisLen > 0){
                            if(remDisLen > 17){
                                discountDescr02 = discountDescr.substring(17,35);
                            }else{
                                discountDescr02 = discountDescr.substring(17,discountDescr.length());
                            }

                        }else{

                        }

                    }else{
                        discountDescr01 = discountDescr;
                    }

                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText(discountDescr01, cellwidthtemp, billtD+50, title);

                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(9);
                    canvas.drawText(discountDescr02, cellwidthtemp, billtD+60, title);

                    if(celck == 1){
                        celck = 2;
                        celWidth = celWidth + 264;
                    }else if(celck == 2){
                        celck = 3;
                        celWidth = celWidth + 272;
                    }

                    // celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celck = 1;
                        if(((j+1) % 2) == 0) {
                            celTop = celTop + 152;
                        }else{
                            celTop = celTop + 147;
                        }

                    }else if(((j+1) % 2) == 0) {
                    }else{
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                }else if (template.equals("Parfetts GO Local Shelf edge Labels")){
                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 56;
                        celck = 1;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();

                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 41;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 22;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 17;



                    int cellwidthtemp = 0;

                    if(celck == 1){
                        cellwidthtemp = celWidth + 5;
                    }else if(celck == 2){
                        cellwidthtemp = celWidth + 15;
                    }else if(celck == 3){
                        cellwidthtemp = celWidth + 15;
                    }

                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    //cellwidthtemp = cellwidthtemp + 135;
                    if(celck == 1){
                        cellwidthtemp = cellwidthtemp + 140;
                    }else if(celck == 2){
                        cellwidthtemp = cellwidthtemp + 135;
                    }else if(celck == 3){
                        cellwidthtemp = cellwidthtemp + 135;
                    }

                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);*/


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, billtD + 25, title);

                    if(celck == 1){
                        celck = 2;
                        celWidth = celWidth + 264;
                    }else if(celck == 2){
                        celck = 3;
                        celWidth = celWidth + 272;
                    }

                    // celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celck = 1;
                        if(((j+1) % 2) == 0) {
                            celTop = celTop + 152;
                        }else{
                            celTop = celTop + 147;
                        }

                    }else if(((j+1) % 2) == 0) {
                    }else{
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                }else{
                    if (pageFlag == "Start"){
                        billtD = 0;
                        pageFlag = "Finish";
                        //lineY = 85;
                        celTop = 85;

                        pageVal = pageVal + 1;
                        pageEVal = 1;
                        mypageInfo = new PdfDocument.PageInfo.Builder(pagewidthNet, pageHeightNet, pageVal).create();
                        myPage = pdfDocument.startPage(mypageInfo);
                        canvas = myPage.getCanvas();


                    /*paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(20, 85, 780, 65, paint);


                    title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                    title.setTextSize(20);
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("Purchase List", 400, 40, title);


                    title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(15);

                    title.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText("Qty " + "Description" + "      ", 30, 80, title);
                    title.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText("Each Price", 750, 80, title);*/
                    }
                    // billtD = celTop + 30;

                    String itemDescrPr = dictTemp.get("descr").toString();
                    if(itemDescrPr.length() > 40){
                        itemDescrPr = itemDescrPr.substring(0,40);
                    }

                    // String str ="Hiren Patel";


                    Paint paintDescr = new Paint();
                    paintDescr.setTextSize(10);
                    Typeface typeface = Typeface.create("font/kreonbold.ttf",Typeface.BOLD);
                    paintDescr.setTypeface(typeface);
                    paintDescr.setColor(Color.BLACK);
                    paintDescr.setStyle(Paint.Style.FILL);
                    paintDescr.getTextBounds(itemDescrPr, 0, itemDescrPr.length(), rectMeasure);
                    float widthDescr = rectMeasure.width();

                    //printItem = "[L]"+itemsP.get(j).getQty()+" " + itemDescrPr + "      " + "  " + "  " + "[R]"+itemsP.get(j).getPrice()+"\n" + printItem;
                    billtD = celTop + 30;
                    title.setTextAlign(Paint.Align.LEFT);
                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(10);



                    canvas.drawText(itemDescrPr, celWidth + Math.round(260 - widthDescr) / 2, billtD, title);
                    billtD = billtD + 27;
                /*title.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(dictTemp.get("price").toString(), 750, lineY, title);*/

                    billtD = billtD + 27;



                    int cellwidthtemp = celWidth + 20;
                    //EAN13Writer barcodeWriter = new EAN13Writer();

                    if(dictTemp.get("barCode").toString() != ""){

                        MultiFormatWriter barcodeWriter =new MultiFormatWriter();

                        //BarcodeFormat.CODE_128;

                        try {
                            bitMatrix = barcodeWriter.encode(dictTemp.get("barCode").toString(), BarcodeFormat.CODE_128, 120, 60);
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                        int height = bitMatrix.getHeight();
                        int width = bitMatrix.getWidth();
                        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                        for (int x = 0; x < width; x++){
                            for (int y = 0; y < height; y++){
                                bmp.setPixel(x, y, bitMatrix.get(x,y) ? Color.BLACK : Color.WHITE);
                            }
                        }

                        canvas.drawBitmap(bmp, cellwidthtemp, billtD, null);


                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText(dictTemp.get("barCode").toString(), cellwidthtemp + 30, billtD + 70, title);

                    }else{

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonregular.ttf",Typeface.NORMAL));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(9);
                        canvas.drawText("Item lookup code", cellwidthtemp + 2, billtD, title);

                        title.setTextAlign(Paint.Align.LEFT);
                        title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                        title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                        title.setTextSize(16);
                        canvas.drawText("45466546646", cellwidthtemp + 2, billtD + 30, title);

                    }

                    cellwidthtemp = cellwidthtemp + 135;

                    paint.setColor(ContextCompat.getColor(this, R.color.yellowColor));
                    paint.setStrokeWidth(0);
                    canvas.drawRect(cellwidthtemp, billtD, cellwidthtemp + 100, billtD+40, paint);


                    Paint paintPrice = new Paint();
                    paintPrice.setTextSize(16);
                    paintPrice.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    paintPrice.setColor(Color.BLACK);
                    paintPrice.setStyle(Paint.Style.FILL);
                    paintPrice.getTextBounds("£" + dictTemp.get("price").toString(), 0, dictTemp.get("price").toString().length(), rectMeasure);
                    float widthPrice = rectMeasure.width();
                    float heightPrice = rectMeasure.height();


                    title.setTextAlign(Paint.Align.LEFT);

                    title.setTypeface(Typeface.create("font/kreonbold.ttf",Typeface.BOLD));
                    title.setColor(ContextCompat.getColor(this, R.color.itemUnSelectColor));
                    title.setTextSize(16);
                    canvas.drawText("£" + dictTemp.get("price").toString(), cellwidthtemp + (100 - widthPrice) / 2, billtD + 25, title);

                    celWidth = celWidth + 275;

                    if (((j+1) % 3) == 0){
                        celWidth = 0;
                        celTop = celTop + 150;
                    }


                    if(arrayList1.size() == (j+1) && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                    if (pageEVal == 21 && pageFlag == "Finish"){
                        pdfDocument.finishPage(myPage);
                        pageFlag = "Start";
                    }
                }





            }


        }else{

        }

        // below line is used to set the name of
        // our PDF file and its path.
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Franco/";
        File root = new File(rootPath);
        if (!root.exists()) {
            root.mkdirs();
        }
        File pdfFile = new File(rootPath, "FrancoePosLabels.pdf");

        try {
            // after creating a file name we will
            // write our PDF file to that location.
            pdfDocument.writeTo(new FileOutputStream(pdfFile));

            // below line is to print toast message
            // on completion of PDF generation.

        } catch (IOException e) {
            // below line is used
            // to handle error
            e.printStackTrace();
        }
        // after storing our pdf to that
        // location we are closing our PDF file.
        pdfDocument.close();
        // File file = new File(stringFile);
        if (!pdfFile.exists()){

            //return;
        }
        // Get a PrintManager instance


        // Set job name, which will be displayed in the print queue
        //String jobName = MainActivity.this.getString(R.string.app_name) + " Document";




        PrintManager printManager = (PrintManager) MainActivity.this.getSystemService(Context.PRINT_SERVICE);
        try {

            PrintDocumentAdapter printDocumentAdapter = new PdfDocumentAdapter(MainActivity.this,pdfFile);
            printManager.print("Document", printDocumentAdapter, new PrintAttributes.Builder().build());

        } catch (Exception e) {
            // below line is used
            // to handle error
            e.printStackTrace();
        }


        // Start a print job, passing in a PrintDocumentAdapter implementation
        // to handle the generation of a print document
        //printManager.print(jobName, new MyPrintDocumentAdapter(MainActivity.this),null); //
        
        return ckExp;
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

    public void loadBackFragment(String type){

        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }


        Fragment fragment = null;
        Bundle bundle = new Bundle();


        if (type.equals("Server")){
            // navServer.setChecked(true);
        }else if(type.equals("Login")){
            //  navLogin.setChecked(true);
        }else if(type.equals("Open PO")){
            fragment = new POFragment();
            bundle.putString("PageName", "Main");
            bundle.putString("ActType", "Open PO");
            CURRENT_AVAL = "Open PO";


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
        navAddLabel.setChecked(false);
        navPLAdd.setChecked(false);
        navOpenPO.setChecked(false);
        navClosePO.setChecked(false);
    }

    public void loadFragment(){

        clearMenu();

        Fragment fragment = null;
        Bundle bundle = new Bundle();



        if (pref.getBoolean("ckLogin", true)) {
           // MenuItem navType = menuNav.findItem(R.id.nav_type);



            navType.setVisible(true);
            navOther.setVisible(true);
            navPO.setVisible(true);

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


    private boolean checkPermission() {
        // checking of permissions.
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // requesting permissions if not provided.
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {

                // after requesting permissions we are showing
                // users a toast message of permission granted.
                boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if (writeStorage && readStorage) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission Denined", Toast.LENGTH_SHORT).show();
                    //finish();
                }
            }
        }
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

        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

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
        }else if(val.equals("Add Label")){
            titleVal = "Add Label";
        }else if(val.equals("Puchase List")){
            titleVal = "Puchase List";
        }else if(val.equals("Open PO") || val.equals("OPO List")){
            titleVal = "Open PO";
        }else if(val.equals("Close PO") || val.equals("CPO List")){
            titleVal = "Close PO";
        }else {

        }
        //OPO List

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

    /*
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resume database Access1");
    }

     */

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("On start database Access1");

        if (!isMyServiceRunning(MySimpleService.class)) {
            if(pref.getString("PCID",null) == null || pref.getString("PCID",null) == "null" || pref.getString("PCID",null).equalsIgnoreCase("null")){

            }else{
                Intent i = new Intent(this, MySimpleService.class);
                i.putExtra("PCID", pref.getString("PCID",null));
                i.putExtra("EXPDATE", pref.getString("EXPDATE",null));
                i.putExtra("receiverTag", mReceiver);
                this.startService(i);
            }

        }



    }
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        // TODO Auto-generated method stub


        System.out.println("On sxxxtart database Access1 " + resultData.getString("ServiceTag"));
        if(resultData.getString("ServiceTag") == "INACTIVE"){
            mLogout("LogoutService");
        }




    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
