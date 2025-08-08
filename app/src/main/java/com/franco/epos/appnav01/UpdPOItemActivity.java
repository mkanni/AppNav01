package com.franco.epos.appnav01;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.OPListTB;
import com.franco.epos.appnav01.database.model.SettingsTB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class UpdPOItemActivity extends Activity {

    private Button btnScan;
    private Button btnSr;
    private Button btnSave;
    private Button btnCancel;
    private Button btnPckQty;
    private ArrayList<String> SampleArrayList = new ArrayList<String>();
    private AppCompatSpinner suppSpinner;
    private TextView suppVal;

    private String actionType = "";
    private String aactionType = "";
    private String poCode = "";
    private String poStatus = "";

    private EditText itemSearch, itemDescription, itemQty, itemStock, itemSuppCode;

    private String ipaddress, port;
    private String staffID = "";
    DatabaseHelper db;
    private SharedPreferences pref;

    private Connection connection = null;
    private String ConnectionURL = null;
    Statement stmt = null;

    final Context context = this;

    private Dictionary dictVal = new Hashtable();

    private String lType = "" , lVal = "";
    private RelativeLayout loginLoader;

    List<String> spinnerSuppArray;
    List<String> spinnerSSuppArray;
    ArrayAdapter<String> adapterSupp;
    private List<Dictionary> suppsDict;
    int selSuppItem = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upd_po_item);
        db = new DatabaseHelper(context);
        actionType = getIntent().getStringExtra("ActType");
        aactionType = getIntent().getStringExtra("AActType");
        poCode = getIntent().getStringExtra("POCode");
        poStatus = getIntent().getStringExtra("POStatus");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode


        itemSearch = findViewById(R.id.input_search);
        itemDescription = findViewById(R.id.input_descr);
        itemQty = findViewById(R.id.input_qty);
        //itemReason = findViewById(R.id.input_reason);
        itemStock = findViewById(R.id.input_stock);
        itemSuppCode = findViewById(R.id.input_supp_code);
        suppVal = findViewById(R.id.supp_val);

        SettingsTB setTb = db.getSetTBByType("SERVER");
        ipaddress = setTb.getIpaddress();
        port = setTb.getPort();
        staffID = pref.getString("staffID",null);

        Log.d("myTag session ", "This is my message rs" + actionType + " DDDD : " + getIntent().getStringExtra("ActFrom"));

        loginLoader = (RelativeLayout) findViewById(R.id.loadingPanel);
        loginLoader.setVisibility(View.GONE);


        suppVal.setText("Supplier List");

        suppSpinner = findViewById(R.id.supp_spinner);
//        recyclerView = findViewById(R.id.recyclerView);

        suppsDict = new ArrayList<>();
        spinnerSuppArray =  new ArrayList<String>();
        spinnerSSuppArray =  new ArrayList<String>();
        spinnerSuppArray =  setAdapterList("supp","select id, code, vedName from vendor order by code asc");
        /*if(aactionType.equals("Department")){
            spinnerSuppArray =  setAdapterList("depart","Select depart_id, description, ageCheck, vat from department order by description asc");
        }else if(aactionType.equals("Category")){
            spinnerSuppArray =  setAdapterList("cate","Select id, departID, category, vat from category order by category asc");
        }else if(aactionType.equals("Supplier")){
            spinnerSuppArray =  setAdapterList("supp","select id, code from vendor order by code asc");
        }*/

        adapterSupp = new ArrayAdapter<String>(context, R.layout.spinner_layout, spinnerSuppArray){

            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    /*if (checkDUN) {
                        return true;
                    }else {
                        return false;
                    }*/
                    return false;

                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;
                v = super.getDropDownView(position, null, parent);
                TextView tv = (TextView) v;


                if (position == selSuppItem || position == 0) {
                    if (position == 0){
                        //v.setBackgroundColor(Color.parseColor("#E5E6E7"));
                        v.setBackgroundColor(Color.parseColor("#7990B0"));
                        v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                        tv.setTextColor(Color.WHITE);
                        tv.setTextSize(20);

                        // v.setPadding(0,0,0,0);
                    }else{
                        v.setBackgroundColor(Color.parseColor("#CCCCCC"));
                    }

                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);


                }



                return v;
            }
        };
        adapterSupp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        suppSpinner.setAdapter(adapterSupp);

        selSuppItem = 0;
        suppSpinner.setSelection(selSuppItem);

        suppSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int myPosition, long myID) {

                Log.i("renderSpinner -> ", "onItemSelected: " + myPosition + "/" + myID + "/" + suppSpinner.getSelectedItem().toString());

                Dictionary dictArr = suppsDict.get(myPosition);

                // Toast.makeText(getActivity(), "Depart ID : " + dictArr.get("departID").toString() + "Depart Name : " + dictArr.get("departName").toString() + "Depart Vat : " + dictArr.get("departVat").toString(), Toast.LENGTH_LONG).show();



                if (dictArr.get("suppName").toString().equals("Select")) {
                    itemSuppCode.setText("");
                } else {
                    itemSuppCode.setText(dictArr.get("suppCode").toString());
                }

                selSuppItem = myPosition;


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        System.out.println("The value in doInBackground UPD OPO ITEM: " + getIntent().getStringExtra("ActFrom"));

        if (getIntent().getStringExtra("ActFrom").equals("edit")){

           // setEditItemAdapter(actionType, getIntent().getStringExtra("itmID"));

            showLoadingActionButton();
            String ckConn = mConnectionCheck("");
            if (ckConn.equals("WifiCon")){

                UpdTaskParams mParams = new UpdTaskParams("EDIT", getIntent().getStringExtra("itmID"), 0);
                new LongOperation(false).execute(mParams);

            }else{

                hideLoadingActionButton();
                finishLongTask("ErrorWifi","Connection error");
            }


        }


        /*if (actionType.equals("Waste")){

            itemPrice.setEnabled(false);
            itemPrice.setFocusable(false);

        }else{

            itemPrice.setEnabled(true);
            itemPrice.setFocusable(true);

        }*/


        /*Intent i = new Intent(MainActivity.this, UpdItemActivity.class);
        i.putExtra("ActType", CURRENT_AVAL);
        i.putExtra("ActFrom", "edit");
        i.putExtra("itmID", itmID);
        startActivityForResult(i, 10001);*/




        itemSearch.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                   // Toast.makeText(context, itemSearch.getText(), Toast.LENGTH_SHORT).show();

                    showLoadingActionButton();
                    String ckConn = mConnectionCheck("");
                    if (ckConn.equals("WifiCon")){

                        UpdTaskParams mParams = new UpdTaskParams("Search", "", 0);
                        new LongOperation(false).execute(mParams);

                    }else{

                        hideLoadingActionButton();
                        finishLongTask("ErrorWifi","Connection error");
                    }


                    return true;
                }
                return false;
            }
        });



        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Log.d("myTag", "This is my message rs" );

                Intent i = new Intent(UpdPOItemActivity.this, BarcodeActivity.class);
               // i.putExtra("ActType", CURRENT_AVAL);
                i.putExtra("ActFrom", "edit");
              //  i.putExtra("itmID", itmID);
                startActivityForResult(i, 10001);



            }

        });

        btnSr = findViewById(R.id.btn_search);
        btnSr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("myTag", "This is my message rs" );

               // setSerItemDataAdapter(actionType);

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    UpdTaskParams mParams = new UpdTaskParams("Search", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }




            }

        });
        //btnSave


        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //mSaveItem(actionType);
                mSaveCheck(actionType);

            }

        });

        btnPckQty = findViewById(R.id.btn_pckQty);
        btnPckQty.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("myTag", "This is my message pack quantity!" );
//                System.out.println("pack quantity action type : "+ actionType + dictVal.get("itemLU").toString());
                System.out.println("pack quantity action type : "+ actionType);
                if (itemSearch.getText().toString().trim().length() < 1) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setMessage("Please enter Item lookup / Barcode");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    itemSearch.getText().clear();
                                    itemSearch.requestFocus();
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }else{
                    // setSerItemDataAdapter(actionType);

                    Intent i = new Intent(UpdPOItemActivity.this, OtherActivity.class);
                    i.putExtra("ActType", actionType);
                    i.putExtra("ActFrom", "Update");
                    i.putExtra("ActFromSub", "PackQty");
                    i.putExtra("itmLU", itemSearch.getText().toString().trim());
                    startActivityForResult(i, 10001);
                }


            }

        });

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("myTag", "This is my message rs" );
                Intent resultIntent = new Intent();
                resultIntent.putExtra("NavValue", "OPO List");
                resultIntent.putExtra("AActType", aactionType);
                resultIntent.putExtra("POCode", poCode);
                resultIntent.putExtra("POStatus", poStatus);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                //UpdItemActivity.this.overridePendingTransition(R.anim.nothing,R.anim.nothing);
            }

        });

        if(poStatus.equals("PROCESSING") || poStatus.equals("")){
            /*btnScan.setVisibility(View.VISIBLE);
            btnSr.setVisibility(View.VISIBLE);*/
            btnScan.setEnabled(true);
            btnSr.setEnabled(true);
            itemSearch.setEnabled(true);
        }else{
            /*btnScan.setVisibility(View.GONE);
            btnSr.setVisibility(View.GONE);*/
            btnScan.setEnabled(false);
            btnSr.setEnabled(false);
            itemSearch.setEnabled(false);

        }

    }


    private static class UpdTaskParams {
        String hParaOne;
        String hParaTwo;
        int recPos;


        UpdTaskParams(String hParaOne, String hParaTwo, int recPos) {
            this.hParaOne = hParaOne;
            this.hParaTwo = hParaTwo;
            this.recPos = recPos;

        }
    }

    private class LongOperation extends AsyncTask<UpdTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "";
        int mPos = 0;

        public LongOperation(boolean showCommit) {
            super();
            // do stuff
            /*if (showCommit){
                btnCommit.setVisibility(View.VISIBLE);
            }else{
                btnCommit.setVisibility(View.GONE);
            }*/

        }

        @Override
        protected String doInBackground(UpdTaskParams... params) {




            loadMStrOne  = params[0].hParaOne;
            loadMStrTwo  = params[0].hParaTwo;
            mPos = params[0].recPos;



            //String mVal = params[0];

            System.out.println("The value in doInBackground UPD OPO ITEM: " + loadMStrOne);

            String result = "0";

            if (loadMStrOne.equals("Search")) {

                result = setSerItemDataAdapter(actionType);

            }else if(loadMStrOne.equals("EDIT")){
                //result = mEditItem(ActType, mPos);
                System.out.println("The value in EDIT item : " + loadMStrTwo);
                result = setEditItemAdapter(actionType, loadMStrTwo);

            }else if(loadMStrOne.equals("Save")) {

               // result = mCommitItems(ActType);
                result = mSaveItem(loadMStrTwo);

            }else if(loadMStrOne.equals("SaveUpd")){
                //result = mEditItem(ActType, mPos);
                result = mSaveAlreary(actionType, loadMStrTwo);
            }else{
                result = "0";
            }



            return result;
        }

        @Override
        protected void onPostExecute(String result) {


            hideLoadingActionButton();
            System.out.println("The value in onPostExecute : " + loadMStrOne);

            if (loadMStrOne.equals("Search")){
                //finishLongTask
                if (result.equals("NoItem")) {

                    finishLongTask("NoItem","No Item found");
                }else if(result.equals("NoItemStock")){

                    finishLongTask("NoItemStock","Stock Disabled");

                }else if(result.equals("AlredyItem")){

                    finishLongTask("AlredyItem","Already Exist");

                }else if(result.equals("UpdItem")){

                    finishLongTask("UpdItem","Update Item");

                }else if(result.equals("NewItem")){

                    finishLongTask("NewItem","New Item");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");
                }

               // setupRecyclerView();


            }else if(loadMStrOne.equals("EDIT")){
                if (result.equals("EditItem")){

                    finishLongTask("EditItem","Edit Item");

                }else if(result.equals("ClearAll")){

                    finishLongTask("ClearAll","Clear All");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");
                }else{
                   // finishDelete("SqlExp", "Connection error");
                }


            }else if(loadMStrOne.equals("Save")) {

                System.out.println("The commitement value : " + result);

                if (result.equals("InsSuc")){
                    finishLongTask("ClearAll","Clear All");
                }else if(result.equals("InsFail")){
                    finishLongTask("InsFail","Insertion Failed");
                }else if(result.equals("ItemAlready")){
                    finishLongTask("ItemAlready","Item Already exist");
                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");

                }
            }else if(loadMStrOne.equals("SaveUpd")){
                if (result.equals("InsSuc")){
                    //finishLongTask("ClearAll","Clear All");
                    finishLongTask("ClearAllSaveUpd","Clear All");

                }else if(result.equals("InsFail")){
                    finishLongTask("InsFail","Insertion Failed");
                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){
                    finishLongTask("Error","Connection error");
                }
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

        }else if(action.equals("NoItem")){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("No Item found");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //onActionQAdd(lType,lVal);
                            itemSearch.getText().clear();
                            itemDescription.getText().clear();
                            itemQty.getText().clear();
                            // itemReason.getText().clear();
                            itemStock.getText().clear();
                            itemSuppCode.getText().clear();
                            itemSearch.requestFocus();
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else if(action.equals("NoItemStock")){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Stock Disabled");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            itemSearch.getText().clear();
                            itemDescription.getText().clear();
                            itemQty.getText().clear();
                            // itemReason.getText().clear();
                            itemStock.getText().clear();
                            itemSuppCode.getText().clear();
                            itemSearch.requestFocus();

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(action.equals("AlredyItem")){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Item already in " + lVal);
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            itemSearch.getText().clear();
                            itemDescription.getText().clear();
                            itemQty.getText().clear();
                           // itemReason.getText().clear();
                            itemStock.getText().clear();
                            itemSuppCode.getText().clear();
                            itemSearch.requestFocus();

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(action.equals("UpdItem")){

            itemDescription.setText(dictVal.get("descr").toString());
            itemStock.setText(dictVal.get("stock").toString());
            itemQty.setText(dictVal.get("in_qty").toString());
            itemSuppCode.setText(dictVal.get("supp_code").toString());
            int spinnerPosition = spinnerSSuppArray.indexOf(dictVal.get("supp_code").toString());
            suppSpinner.setSelection(spinnerPosition);

        }else if(action.equals("NewItem")){

            itemDescription.setText(dictVal.get("descr").toString());
            itemStock.setText(dictVal.get("stock").toString());
            itemSuppCode.setText(dictVal.get("supp_code").toString());


            int spinnerPosition = spinnerSSuppArray.indexOf(dictVal.get("supp_code").toString());
            suppSpinner.setSelection(spinnerPosition);

            itemQty.setText(dictVal.get("in_qty").toString());
            itemQty.requestFocus();

        }else if(action.equals("EditItem")){

            itemDescription.setText(dictVal.get("descr").toString());
            itemStock.setText(dictVal.get("stock").toString());
            itemSuppCode.setText(dictVal.get("supp_code").toString());
            itemQty.setText(dictVal.get("in_qty").toString());
            int spinnerPosition = spinnerSSuppArray.indexOf(dictVal.get("supp_code").toString());
            suppSpinner.setSelection(spinnerPosition);

            itemSearch.setText(dictVal.get("itemLU").toString());
            itemSearch.requestFocus();

        }else if(action.equals("ClearAll")) {

            itemDescription.getText().clear();
            itemStock.getText().clear();
            itemSuppCode.getText().clear();
            itemQty.getText().clear();
            //itemReason.getText().clear();
            int spinnerPosition = spinnerSSuppArray.indexOf("");
            suppSpinner.setSelection(spinnerPosition);
            itemSearch.getText().clear();
            itemSearch.requestFocus();

        }else if(action.equals("ClearAllSaveUpd")) {
            if(poStatus.equals("PROCESSING") || poStatus.equals("")){
                itemDescription.getText().clear();
                itemStock.getText().clear();
                itemSuppCode.getText().clear();
                itemQty.getText().clear();
                //itemReason.getText().clear();
                int spinnerPosition = spinnerSSuppArray.indexOf("");
                suppSpinner.setSelection(spinnerPosition);
                itemSearch.getText().clear();
                itemSearch.requestFocus();
            }else{


            }


        }else if(action.equals("InsFail")){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Error in Saving");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(action.equals("ItemAlready")){

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Item id already exists, do you want to update?");
            alertDialogBuilder.setPositiveButton("Yes",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            UpdTaskParams mParams = new UpdTaskParams("SaveUpd", lVal, 0);
                            new LongOperation(false).execute(mParams);

                        }
                    });

            alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // finish();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

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

        }else if(action.equals("NoRecordDel")) {

           // btnCommit.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
            alertDialogBuilder.setMessage("There is no recored found to delete!");
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
        }else if(action.equals("ErrorCommit")){

           // btnCommit.setVisibility(View.VISIBLE);
            // hideLoadingActionButton();ErrorCommit

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this.context);
            alertDialogBuilder.setMessage("Connection error, While commiting!");
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

    private String mCount(String sqlStmt){

        Statement stmt = mConnection();

        String ckExp = "SqlExp";

        Integer count = 0;

        if (stmt != null){
            try {

                //EditText itemSearch = findViewById(R.id.input_search);
                ResultSet rs = stmt.executeQuery(sqlStmt);
                while (rs.next()){
                    count = count + 1;
                }

                if (count == 0){
                    ckExp = "No";
                }else{
                    ckExp = "Yes";
                }

            } catch (SQLException e) {

                Log.d("Error Type", "Exception : " + e.getMessage() );
                e.printStackTrace();
                ckExp = "SqlExp";

            }
        }else{
            Log.d("Error Type", "Connection error!");
            ckExp = "SqlExp";
        }

        return ckExp;

    }

    private void mSaveCheck(String type){
        if (itemSearch.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Please enter Item lookup / Barcode");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemSearch.getText().clear();
                            itemSearch.requestFocus();
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(!dictVal.get("itemLU").toString().equals(itemSearch.getText().toString().trim()) &&  !dictVal.get("barCode").toString().equals(itemSearch.getText().toString())) {

            //System.out.println("The search value " + dictVal.get("itemLU").toString() + " The text box value : " + itemSearch.getText());

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Item mismatch, Please search the item");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            itemDescription.getText().clear();
                            itemQty.getText().clear();
                            //itemReason.getText().clear();
                            itemStock.getText().clear();
                            itemSuppCode.getText().clear();
                            itemSearch.requestFocus();
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if (itemQty.getText().toString().trim().length() < 1 || itemQty.getText().toString().trim().equals("0")) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Please Input Quantity");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemQty.getText().clear();
                            itemQty.requestFocus();
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else if (itemSuppCode.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Please enter Supplier Code");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemSuppCode.getText().clear();
                            itemSuppCode .requestFocus();
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else{

            showLoadingActionButton();
            String ckConn = mConnectionCheck("");

            if (ckConn.equals("WifiCon")){

                UpdTaskParams mParams = new UpdTaskParams("Save", type, 0);
                new LongOperation(false).execute(mParams);


            }else{

                hideLoadingActionButton();
                finishLongTask("ErrorWifi","Connection error");
            }



        }
    }

    private String mSaveItem(String type) {


        String ckExp = "SqlExp";

        if (db.getOPListTBCountByITL(dictVal.get("itemLU").toString()) > 0) {
            //Log.d("myTag ", "There is a record in the table" + rs.getString("ItemLookup"));
            ckExp = "ItemAlready";
            lType = type;
            lVal = dictVal.get("itemLU").toString();
        }else{
            OPListTB oPListTB;
            oPListTB = new OPListTB(1,"","",dictVal.get("itemLU").toString(),itemQty.getText().toString().trim(),"Primary",itemSuppCode.getText().toString().trim(),dictVal.get("descr").toString(),dictVal.get("midasCode").toString(),"0");
            long trest =  db.insertOPListTB(oPListTB);
            Log.d("myTag ", "This is my message Filter" + trest);
            ckExp = "InsSuc";
        }




        /*stmt = mConnection();
        if (stmt != null){

            ckExp = mCount("select * from tempProAdjustment where itemID = '"+dictVal.get("itemId")+"'");

            if (ckExp.equals("SqlExp")) {
                ckExp = "SqlExp";
            }else if(ckExp.equals("No")){
                try {


                    String logdat = DateFormat.getDateTimeInstance().format(new Date());

                    int result = stmt.executeUpdate("insert into tempProAdjustment(adj_type,itemID,staff_id,dateTime,in_qty,cur_stock,cur_price,price_changed,reason)values('" + actionType + "','" + dictVal.get("itemId") + "','" + staffID + "','" + logdat + "', '" + itemQty.getText().toString().trim() + "', '" + dictVal.get("stock") + "', '" + dictVal.get("price") + "', '" + itemSuppCode.getText().toString().trim() + "', '')");
                    if(result == 1){

                        ckExp = "InsSuc";



                        System.out.println("Succesfully Upadted : "+ result);

                    }else{
                        ckExp = "InsFail";


                        System.out.println("Failed to update : "+ result);
                    }

                } catch (SQLException e) {

                    Log.d("Error Type", "Exception : " + e.getMessage() );
                    e.printStackTrace();
                    ckExp = "SqlExp";

                }

            }else if(ckExp.equals("Yes")){
                ckExp = "ItemAlready";

                lType = type;
                lVal = dictVal.get("itemId").toString();



            }else{
                ckExp = "SqlExp";

            }



        }else{
            ckExp = "SqlExp";
        }*/






        return  ckExp;
    }

    private String mSaveAlreary(String type, String val){

        String ckExp = "SqlExp";
        OPListTB oPListTB;
        oPListTB = new OPListTB(1,"","",dictVal.get("itemLU").toString(),itemQty.getText().toString().trim(),dictVal.get("supp_type").toString(),itemSuppCode.getText().toString().trim(),dictVal.get("descr").toString(),dictVal.get("midasCode").toString(),"0");
        long trest =  db.updateOPListTB(oPListTB);
        Log.d("myTag ", "This is my message Filter" + trest);
        ckExp = "InsSuc";


        /*stmt = mConnection();
        if (stmt != null){
            try {


                String logdat = DateFormat.getDateTimeInstance().format(new Date());

                int result = stmt.executeUpdate("UPDATE tempProAdjustment SET staff_id='" + staffID + "', dateTime='" + logdat + "',in_qty = '" + itemQty.getText().toString().trim() + "',cur_stock = '" + dictVal.get("stock").toString().trim() + "',cur_price = '" + dictVal.get("price") + "',price_changed = '" + itemSuppCode.getText().toString().trim() + "',reason = '' WHERE itemID='" + dictVal.get("itemId") + "'");
                if(result == 1){


                    ckExp = "InsSuc";

                }else{


                    ckExp = "InsFail";
                }

            } catch (SQLException e) {

                Log.d("Error Type", "Exception : " + e.getMessage() );
                e.printStackTrace();

                ckExp = "SqlExp";
            }
        }else{
            ckExp = "SqlExp";
        }*/

        return  ckExp;


    }


    private String setEditItemAdapter(String type, String itmID){

        Dictionary dictTemp = new Hashtable();
        String ckExp = "";

        dictTemp = selectArray("itemTemp", itmID);
        Integer resCountTM = Integer.parseInt(String.valueOf(dictTemp.get("count")));

        System.out.println("The value for the int : " + resCountTM);

        ckExp = dictTemp.get("error_type").toString();

        if (!ckExp.equals("SqlExp")){


            if (resCountTM > 0) {
                ckExp = "EditItem";



                dictVal = dictTemp;

            }else{
                ckExp = "ClearAll";

                /*itemDescription.getText().clear();
                itemStock.getText().clear();
                itemPrice.getText().clear();
                itemQty.getText().clear();
                itemReason.getText().clear();
                itemSearch.getText().clear();
                itemSearch.requestFocus();*/

            }

        }

        return ckExp;

    }
    private String setSerItemDataAAdapter(final String type) {
        String ckExp = "SqlExp";
        Dictionary dict = new Hashtable();

        dict = selectArray("itemProduct","select id, ItemLookup, description, barCode, avaiable, restockLevel, midasCode, stockCk from itemproduct where barCode = '"+itemSearch.getText().toString().trim()+"' OR ItemLookup = '"+itemSearch.getText().toString().trim()+"'");
        Integer resCount = Integer.parseInt(String.valueOf(dict.get("count")));
        ckExp = dict.get("error_type").toString();

        if (!ckExp.equals("SqlExp")){

        }

        return ckExp;
    }

    private String setSerItemDataAdapter(final String type) {

        Dictionary dict = new Hashtable();
        String ckExp = "";

        dict = selectArray("itemProduct","select id, ItemLookup, description, barCode, avaiable, restockLevel, midasCode, stockCk from itemproduct where barCode = '"+itemSearch.getText().toString().trim()+"' OR ItemLookup = '"+itemSearch.getText().toString().trim()+"'");
        Integer resCount = Integer.parseInt(String.valueOf(dict.get("count")));
        ckExp = dict.get("error_type").toString();

        if (!ckExp.equals("SqlExp")){
            if (dict.get("stock_ck").toString().equals("NO")) {
                ckExp = "NoItemStock";
                lType = type;
                lVal = itemSearch.getText().toString();
            }else if (resCount > 0){

                Dictionary dictTemp = new Hashtable();
                dictTemp = selectArray("itemTemp", dict.get("itemLU").toString());
                Integer resCountTM = Integer.parseInt(String.valueOf(dictTemp.get("count")));
                ckExp = dictTemp.get("error_type").toString();

                if (!ckExp.equals("SqlExp")){

                    if (resCountTM > 0) {

                        ckExp = "UpdItem";


                        dictVal = dictTemp;

                    }else{

                        ckExp = "NewItem";



                        dictVal = dict;

                    }


                }




            }else{
                // System.out.println("The value for the dicetio : ");

                ckExp = "NoItem";
                lType = type;
                lVal = itemSearch.getText().toString();

            }

        }

        return ckExp;

    }

    private Dictionary selectArray(String type, String query) {

        Dictionary dict = new Hashtable();

        Integer count = 0;
        String id = "",itemid = "", itemLU = "", descr = "", barCode = "", stock = "", in_qty = "", supp_type = "", midasCode = "", supp_code = "", stock_ck = "NO", error_type = "Success";

        if (type.equals("itemTemp")) {
           // List<OPListTB> itemsPO = (List<OPListTB>) db.getOPListTBByIL(query);
            OPListTB itemsPO = db.getOPListTBByIL(query);
            if (db.getOPListTBCountByITL(query) > 0) {
                count = count + 1;

                itemLU = itemsPO.getItem_lookup();
                descr = itemsPO.getItem_descr();
                supp_code = itemsPO.getVendor_code();
                in_qty = itemsPO.getQty();
                supp_type = itemsPO.getType();
                if (itemsPO.getMidas_code() != null){
                    midasCode = itemsPO.getMidas_code();
                }

                error_type = "Success";
            }else{
               // ckExp = "NoRecord";
                count = 0;
            }
        }else{
            stmt = mConnection();

            if (stmt != null) try {


                ResultSet rs = stmt.executeQuery(query);

                //SampleArrayList.clear();
                dict.isEmpty();
                while (rs.next()) {

                    count = count + 1;

                    id = rs.getString("id");
                    itemid = id;
                    itemLU = rs.getString("ItemLookup");
                    descr = rs.getString("description");
                    barCode = rs.getString("barCode");
                    stock = rs.getString("avaiable");
                    in_qty = rs.getString("restockLevel");
                    supp_type = "Primary";
                    if (rs.getString("midasCode") != null){
                        midasCode = rs.getString("midasCode");
                    }
                    //midasCode = rs.getString("midasCode");

                    if (rs.getBoolean("stockCk") == true){
                        stock_ck = "YES";
                    }
                    error_type = "Success";

                    // Log.d("myTag", "This is my message rss dfdf " + rs.getString("ItemLookup") + itemSearch.getText());


                }


            } catch (SQLException e) {

                Log.d("myTag", "This is my message rss hhhh" + e.getMessage());
                e.printStackTrace();
                error_type = "SqlExp";

            }else{

                Log.d("myTag", "Stmt in empty");
                error_type = "SqlExp";

            }
        }





        if (type.equals("itemTemp")) {

            String avCk = "";
            avCk = getDesc("Available","Select avaiable FROM itemProduct where itemLookup = '" + itemLU + "'");

            dict.put("count", count);
            dict.put("fnType", "new");
            dict.put("id", id);
            dict.put("itemId", itemid);
            dict.put("itemLU", itemLU);
            dict.put("descr", descr);
            dict.put("barCode", barCode);
            dict.put("stock", avCk);
            dict.put("in_qty", in_qty);
            dict.put("supp_code", supp_code);
            dict.put("supp_type", supp_type);
            dict.put("midasCode", midasCode);
            dict.put("error_type", error_type);


        }else{
            String supCode = "";
            supCode = getDesc("VendorCode","Select i.vendorCode FROM itemProduct as p JOIN itemVendor as i ON p.ItemLookup = i.itemlookup where i.itemLookup = '" + itemLU + "' and i.venprimary = 'True'");

            dict.put("count", count);
            dict.put("fnType", "edit");
            dict.put("id", id);
            dict.put("itemId", itemid);
            dict.put("itemLU", itemLU);
            dict.put("descr", descr);
            dict.put("barCode", barCode);
            dict.put("stock", stock);
            dict.put("in_qty", in_qty);
            dict.put("supp_code", supCode);
            dict.put("supp_type", supp_type);
            dict.put("midasCode", midasCode);
            dict.put("stock_ck", stock_ck);
            dict.put("error_type", error_type);

        }

        return  dict;


    }

    public void onActionQAdd(String type, String itemLU){

        Intent i = new Intent(UpdPOItemActivity.this, OtherActivity.class);
        i.putExtra("ActType", type);
        i.putExtra("ActFrom", "Update");
        i.putExtra("ActFromSub", "");
        i.putExtra("itmLU", itemLU);
        startActivityForResult(i, 10001);

        // itemSearch.getText().clear();
        itemDescription.getText().clear();
        itemQty.getText().clear();
       // itemReason.getText().clear();
        itemStock.getText().clear();
        itemSuppCode.getText().clear();
        itemSearch.requestFocus();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)){

            if (data.getStringExtra("NavValue").equals("QuickAdd")) {

                itemSearch.setText(data.getStringExtra("itemLUValue"));

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    UpdTaskParams mParams = new UpdTaskParams("Search", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }

               // setSerItemDataAdapter(actionType);

            }else if (data.getStringExtra("NavValue").equals("QuickAddExit")) {

                itemSearch.getText().clear();
                itemDescription.getText().clear();
                itemQty.getText().clear();
               // itemReason.getText().clear();
                itemStock.getText().clear();
                itemSuppCode.getText().clear();
                itemSearch.requestFocus();

            }else if (data.getStringExtra("NavValue").equals("PckQtyExit")) {

            }else if (data.getStringExtra("NavValue").equals("PckUpdQty")) {
                itemQty.setText(data.getStringExtra("itemPckQUpdVal"));
            }else if (data.getStringExtra("NavValue").equals("PckUpdPrice")) {
                //itemPrice.setText(data.getStringExtra("itemPckPUpdVal"));
            }else if (data.getStringExtra("NavValue").equals("PckUpdBoth")) {
                itemQty.setText(data.getStringExtra("itemPckQUpdVal"));
                //itemPrice.setText(data.getStringExtra("itemPckPUpdVal"));

            }else{

                itemSearch.setText(data.getStringExtra("BarCodeValue"));

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    UpdTaskParams mParams = new UpdTaskParams("Search", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }

               // setSerItemDataAdapter(actionType);

            }



           // Toast.makeText(UpdItemActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
        }
    }
    private String getDesc(String type, String query){

        String rVal = "";


        stmt = mConnection();

        if (stmt != null) try {


            ResultSet rs = stmt.executeQuery(query);

            //SampleArrayList.clear();

            while (rs.next()) {
                if (type.equals("VendorCode")){
                    rVal = rs.getString("vendorCode");
                }else if (type.equals("Available")){
                    rVal = rs.getString("avaiable");
                }




            }


        } catch (SQLException e) {

            Log.d("myTag", "This is my message rss hhhh" + e.getMessage());
            e.printStackTrace();

        }
        else{

            Log.d("myTag", "Stmt in empty");

        }




        return  rVal;
    }

    private List<String> setAdapterList(String type, String query){

        stmt = mConnection();
        Integer count = 0;
        List<String> spinnerArray =  new ArrayList<String>();
        if (stmt != null) try {


            ResultSet rs = stmt.executeQuery(query);

            //SampleArrayList.clear();
            // dict.isEmpty();


            spinnerArray.clear();
            spinnerSSuppArray.clear();
            if(type.equals("supp")){
                spinnerArray.add("Select");
                spinnerSSuppArray.add("");
                suppsDict.clear();
                Dictionary dictTempSel = new Hashtable();
                dictTempSel.put("suppID","");
                dictTempSel.put("suppName", "Select");
                dictTempSel.put("suppCode", "");
                suppsDict.add(dictTempSel);
            }


            while (rs.next()) {

                count = count + 1;
                Dictionary dictTemp = new Hashtable();

                if (type.equals("supp")) {
                    spinnerArray.add(rs.getString("vedName") + " ("+ rs.getString("code") +")");
                    spinnerSSuppArray.add(rs.getString("code"));
                    dictTemp.put("suppID", rs.getString("id"));
                    dictTemp.put("suppCode", rs.getString("code"));
                    dictTemp.put("suppName", rs.getString("vedName"));


                    suppsDict.add(dictTemp);
                }




            }


        } catch (SQLException e) {

            Log.d("myTag", "This is my message rss hhhh" + e.getMessage());
            e.printStackTrace();

        }
        else{

            Log.d("myTag", "Stmt in empty");

        }
        if (type.equals("depart")) {
            /*if (spinnerArray.contains("UNKNOWN")){
                departsDict.remove(0);
                spinnerArray.remove(0);
                checkDUN = true;
            }else{
                checkDUN = false;
            }*/
        }else{

        }


        return spinnerArray;

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

    public void showLoadingActionButton() {
        loginLoader.setVisibility(View.VISIBLE);
        //norecordPanel.setVisibility(View.GONE);
    }

    public void hideLoadingActionButton() {
        loginLoader.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed()
    {
        Log.d("myTag", "Back Pressed!");
        Log.d("myTag", "This is my message rs" );
        Intent resultIntent = new Intent();
        resultIntent.putExtra("NavValue", "OPO List");
        resultIntent.putExtra("AActType", aactionType);
        resultIntent.putExtra("POCode", poCode);
        resultIntent.putExtra("POStatus", poStatus);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        super.onBackPressed();
    }



}
