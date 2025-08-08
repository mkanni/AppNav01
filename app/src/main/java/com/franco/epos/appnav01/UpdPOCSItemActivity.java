package com.franco.epos.appnav01;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.OPListTB;
import com.franco.epos.appnav01.database.model.SettingsTB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class UpdPOCSItemActivity extends Activity {

/*    private Button btnScan;
    private Button btnSr;*/
    private Button btnSave;
    private Button btnCancel;
   // private Button btnPckQty;
    private ArrayList<String> SampleArrayList = new ArrayList<String>();
    //private AppCompatSpinner suppSpinner;
    //private TextView suppVal;

    private String actionType = "";
    private String aactionType = "";
    private String poCode = "";
    private String poStatus = "";

    private EditText itemPOCode, itemDescription, itemShipTo, itemDeliveryDate, itemShipCharge;

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

    private int mYear, mMonth, mDay;
    List<Item> items;
    List<String> suppCodeList;
    private String custCode = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upd_pocs_item);
        db = new DatabaseHelper(context);
        actionType = getIntent().getStringExtra("ActType");
        aactionType = getIntent().getStringExtra("AActType");
        poCode = getIntent().getStringExtra("POCode");
        poStatus = getIntent().getStringExtra("POStatus");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode


       // itemSearch = findViewById(R.id.input_search);
        itemPOCode = findViewById(R.id.input_po_code);
        itemDescription = findViewById(R.id.input_descr);
        itemShipTo = findViewById(R.id.input_ship_to);
        //itemReason = findViewById(R.id.input_reason);
        itemDeliveryDate = findViewById(R.id.input_delivery_date);
        itemShipCharge = findViewById(R.id.input_ship_charge);
        //suppVal = findViewById(R.id.supp_val);

        SettingsTB setTb = db.getSetTBByType("SERVER");
        ipaddress = setTb.getIpaddress();
        port = setTb.getPort();
        staffID = pref.getString("staffID",null);

        Log.d("myTag session ", "This is my message rs" + actionType + " DDDD : " + getIntent().getStringExtra("ActFrom"));

        loginLoader = (RelativeLayout) findViewById(R.id.loadingPanel);
        loginLoader.setVisibility(View.GONE);




        //suppSpinner = findViewById(R.id.supp_spinner);
//        recyclerView = findViewById(R.id.recyclerView);




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


        }else{
            showLoadingActionButton();
            String ckConn = mConnectionCheck("");
            if (ckConn.equals("WifiCon")){

                UpdTaskParams mParams = new UpdTaskParams("GetItem", "", 0);
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



        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        System.out.println("The value in doInBackground UPD OPO ITEM: " + mDay +"-"+ mMonth+"-"+ mYear);


        itemDeliveryDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {



                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                itemDeliveryDate.setText(String.format("%02d", dayOfMonth) + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + year);
                               // Calendar c = Calendar.getInstance();
                                mYear = year;
                                mMonth = monthOfYear;
                                mDay = dayOfMonth;

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

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

        items = new ArrayList<>();
        suppCodeList =  new ArrayList<String>();

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

            if (loadMStrOne.equals("GetItem")) {

                //result = setSerItemDataAdapter(actionType);
                result = mQucikConn("",0);

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

            if (loadMStrOne.equals("GetItem")){
                //finishLongTask

                if (result.equals("Success")) {


                    //finishLongTask("NoItem","No Item found");
                    getItemCode();

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
                    finishLongTask("ClearAll","Clear All");
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
                            itemPOCode.getText().clear();
                            itemDescription.getText().clear();
                            itemShipTo.getText().clear();
                            itemDeliveryDate.getText().clear();
                            itemShipCharge.getText().clear();
                           // itemSearch.requestFocus();
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

                            itemPOCode.getText().clear();
                            itemDescription.getText().clear();
                            itemShipTo.getText().clear();
                            itemDeliveryDate.getText().clear();
                            itemShipCharge.getText().clear();

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

                            itemPOCode.getText().clear();
                            itemDescription.getText().clear();
                            itemShipTo.getText().clear();
                            itemDeliveryDate.getText().clear();
                            itemShipCharge.getText().clear();

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(action.equals("UpdItem")){

            /*itemDescription.setText(dictVal.get("descr").toString());
            itemStock.setText(dictVal.get("stock").toString());
            itemQty.setText(dictVal.get("in_qty").toString());
            itemSuppCode.setText(dictVal.get("supp_code").toString());*/


        }else if(action.equals("NewItem")){

            /*itemDescription.setText(dictVal.get("descr").toString());
            itemStock.setText(dictVal.get("stock").toString());
            itemSuppCode.setText(dictVal.get("supp_code").toString());
            itemQty.setText(dictVal.get("in_qty").toString());
            itemQty.requestFocus();*/

        }else if(action.equals("EditItem")){

            System.out.println("The value date : " + dictVal.get("deliv_date").toString().substring(0,4) +"Month"+dictVal.get("deliv_date").toString().substring(5, 7)+"Date"+dictVal.get("deliv_date").toString().substring(8, 10));

            mYear = Integer.parseInt(dictVal.get("deliv_date").toString().substring(0,4));
            mMonth = Integer.parseInt(dictVal.get("deliv_date").toString().substring(5, 7))-1;
            mDay = Integer.parseInt(dictVal.get("deliv_date").toString().substring(8, 10));

            itemPOCode.setText(dictVal.get("poCode").toString());
            itemDescription.setText(dictVal.get("descr").toString());
            itemShipTo.setText(dictVal.get("ship_to").toString());
            itemDeliveryDate.setText(dictVal.get("deliv_date").toString());
            itemDeliveryDate.setText(String.format("%02d", mDay) + "-" + String.format("%02d", (mMonth + 1)) + "-" + mYear);
            itemShipCharge.setText(dictVal.get("ship_charge").toString());

        }else if(action.equals("ClearAll")) {

            itemPOCode.getText().clear();
            itemDescription.getText().clear();
            itemShipTo.getText().clear();
            itemDeliveryDate.getText().clear();
            itemShipCharge.getText().clear();

            Log.d("myTag", "This is my message rs" );
            Intent resultIntent = new Intent();
            resultIntent.putExtra("NavValue", "OPO List");
            resultIntent.putExtra("AActType", "POFRAG");
            resultIntent.putExtra("POCode", poCode);
            resultIntent.putExtra("POStatus", poStatus);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();


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

    private String mQucikConn(String type, int postion) {
        String ckExp = "";
        stmt = mConnection();
        if (stmt != null) {
            ckExp = "Success";
        }else{
            ckExp = "SqlExp";

        }


        return ckExp;

    }


    private String getItemCode(){
        //   itemLookupQD
        //String lookUpID = itemLookupQD.trim();

        Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        String ckExp = "SqlExp";

        stmt = mConnection();

        if (stmt != null){

            if (getCount("idItem","Select id from PoCode") == 0){
                itemPOCode.setText("PO0001");
            }else{

                itemPOCode.setText("PO000" + getCount("maxItem","Select MAX(id) As id from PoCode").toString());

            }

            System.out.println("The value in doInBackground itemPOCode: ");

            itemDescription.requestFocus();
            showSoftKeyBoard(itemDescription);



            ckExp = "Success";

        }else{

            ckExp = "SqlExp";
        }






        return ckExp;




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

    private Integer getCount(String type, String query){
        Dictionary dict = new Hashtable();

        Integer count = 0;
        Integer maxID = 0;

        stmt = mConnection();

        if (stmt != null) try {


            ResultSet rs = stmt.executeQuery(query);

            //SampleArrayList.clear();
            dict.isEmpty();
            while (rs.next()) {

                count = count + 1;

                if (type.equals("maxItem")){
                    maxID = Integer.valueOf(rs.getString("id"));
                    // rs.getInt("id");
                }

            }


        } catch (SQLException e) {

            Log.d("myTag", "This is my message rss hhhh" + e.getMessage());
            e.printStackTrace();

        }
        else{

            Log.d("myTag", "Stmt in empty");

        }


        if (type.equals("maxItem")){
            count = maxID + 1;
        }
        Log.d("myTag", "This count value : " + count);
        return  count;
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
        if (itemPOCode.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Please Input PO Code");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemPOCode.getText().clear();
                            itemPOCode.requestFocus();
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
        items.clear();

        suppCodeList.clear();
        List<OPListTB> itemsPO = db.getAllOPListTB();
        if (itemsPO.size() > 0){
            for (int j=0; j<itemsPO.size();j++){
                Log.d("myTag", "This is my message rss" + itemsPO.get(j).getItem_lookup());

                if (!suppCodeList.contains(itemsPO.get(j).getVendor_code())){
                    suppCodeList.add(itemsPO.get(j).getVendor_code());
                }

                Item item = new Item();
                item.setItemCode(itemsPO.get(j).getPo_code());
                item.setItemLU(itemsPO.get(j).getItem_lookup());
                item.setDescr(itemsPO.get(j).getItem_descr());
                item.setSupCode(itemsPO.get(j).getVendor_code());
                item.setFnType(itemsPO.get(j).getType());
                item.setInQty(itemsPO.get(j).getQty());
                item.setMidasCode(itemsPO.get(j).getMidas_code());
                items.add(item);

            }
        }else{
            ckExp = "NoRecord";
        }

        Log.d("myTag", "The size of the suppcode : " + suppCodeList.size());

        //Dim strStatus As String = "PROCESSING"
        String strStatus = "PROCESSING";
        String updFrom = "";
        int f = 0;
        String purdes = itemShipTo.getText().toString();
        String dattim = DateFormat.getDateTimeInstance().format(new Date());

       // String sDate = dattim;

        String sDate = mYear + "-" + String.format("%02d", (mMonth + 1)) + "-" + String.format("%02d", mDay) + " 00:00:00.000";

        String des = itemDescription.getText().toString();
        double spSrg = 0;
        if(itemShipCharge.getText().toString().trim().length() < 1) {
            spSrg = 0;
        }else{
            spSrg = Double.parseDouble(itemShipCharge.getText().toString());
        }

        if (stmt != null){
            if (poCode.equals("")){

                if (aactionType.equals("COMMIT")){
                    strStatus = "OPEN";
                    updFrom = "Android";
                }

                for (int j=0; j<suppCodeList.size();j++){


                    String Pid;
                    if (j == 0){
                        Pid = itemPOCode.getText().toString();
                    }else{
                        Pid = itemPOCode.getText().toString() + (j + 1);
                    }

                    ckExp = mCount("SELECT * from PoCode where PoCode = '" + Pid + "'");
                    if (ckExp.equals("SqlExp")) {
                        ckExp = "SqlExp";
                    }else if(ckExp.equals("No")){

                        try {
                            int result = stmt.executeUpdate("insert into PoCode(PoCode, description, datetim, staffid, status, vendorCode, cust_code, descp, shipCharge, shipTo, delvDatetim, updFrom) VALUES ('" + Pid + "', '" + purdes + "', '" + dattim + "', '" + staffID + "', '" + strStatus + "', '" + suppCodeList.get(j) + "','" + custCode + "', '" + des + "', '" + spSrg + "', '" + itemShipTo.getText().toString() + "', '" + sDate + "', '" + updFrom + "')");
                            if(result == 1){


                                for (int k = 0; k < items.size(); k++) {

                                    Item item = new Item();
                                    item = items.get(k);
                                    String itemlook = item.getItemLU();
                                    String qty = item.getInQty();
                                    String typ = item.getFnType();
                                    String ven = item.getSupCode();
                                    String midasCode = item.getMidasCode();

                                    if (suppCodeList.get(j).equals(ven)){
                                        try {
                                            int resultt = stmt.executeUpdate("Insert into purchaseOrder(PoCode, PoDesc, itemlookup, qty, type, vendorCode, datetim, delvDatetim) VALUES ('" + Pid + "','" + purdes + "', '" + itemlook + "', '" + qty + "', '" + typ + "', '" + ven + "', '" + dattim + "', '" + sDate + "')");
                                            if(resultt == 1){

                                                ckExp = "InsSuc";
                                                System.out.println("Succesfully Upadted : "+ result);
                                            }else{
                                                ckExp = "InsFail";

                                                System.out.println("Failed to update : "+ result);
                                            }
                                        }catch (SQLException e) {

                                        Log.d("Error Type", "Exception : " + e.getMessage() );
                                        e.printStackTrace();
                                        ckExp = "SqlExp";

                                    }
                                    }


                                }


                                ckExp = "InsSuc";
                                System.out.println("Succesfully Upadted : "+ result);

                            }else{
                                ckExp = "InsFail";


                                System.out.println("Failed to update : "+ result);
                            }
                        }catch (SQLException e) {

                            Log.d("Error Type", "Exception : " + e.getMessage() );
                            e.printStackTrace();
                            ckExp = "SqlExp";

                        }




                    }else if(ckExp.equals("Yes")){

                        ckExp = "InsFail";

                        /*ckExp = "ItemAlready";

                        lType = type;
                        lVal = dictVal.get("itemId").toString();*/



                    }else{
                        ckExp = "SqlExp";
                    }


                }

            }else{
                //ckExp = "POCODE EXIST!";


                try {
                    int resultDel = stmt.executeUpdate("delete from purchaseOrder where PoCode = '" + poCode + "'");
                    if (resultDel == 1){
                    }

                    if (aactionType.equals("COMMIT")){
                        strStatus = "OPEN";
                        updFrom = "Android";
                        int result = stmt.executeUpdate("update PoCode set datetim = '" + dattim + "', descp = '" + des + "', shipCharge = '" + spSrg + "', shipTo = '" + itemShipTo.getText().toString() + "', delvDatetim = '" + sDate + "', status = '" + strStatus + "', updFrom = '" + updFrom + "' where PoCode =  '" + poCode + "'");
                        if(result == 1){


                            ckExp = "InsSuc";

                        }else{


                            ckExp = "InsFail";
                        }
                    }else{
                        int result = stmt.executeUpdate("update PoCode set datetim = '" + dattim + "', descp = '" + des + "', shipCharge = '" + spSrg + "', shipTo = '" + itemShipTo.getText().toString() + "', delvDatetim = '" + sDate + "' where PoCode =  '" + poCode + "'");
                        if(result == 1){


                            ckExp = "InsSuc";

                        }else{


                            ckExp = "InsFail";
                        }
                    }

                    for (int k = 0; k < items.size(); k++) {

                        Item item = new Item();
                        item = items.get(k);
                        String itemlook = item.getItemLU();
                        String qty = item.getInQty();
                        String typ = item.getFnType();
                        String ven = item.getSupCode();
                        String midasCode = item.getMidasCode();

                        try {
                            int resultt = stmt.executeUpdate("Insert into purchaseOrder(PoCode, PoDesc, itemlookup, qty, type, vendorCode, datetim, delvDatetim) VALUES ('" + poCode + "','" + purdes + "', '" + itemlook + "', '" + qty + "', '" + typ + "', '" + ven + "', '" + dattim + "', '" + sDate + "')");
                            if(resultt == 1){

                                ckExp = "InsSuc";
                                System.out.println("Succesfully Upadted : "+ resultt);
                            }else{
                                ckExp = "InsFail";

                                System.out.println("Failed to update : "+ resultt);
                            }
                        }catch (SQLException e) {

                            Log.d("Error Type", "Exception : " + e.getMessage() );
                            e.printStackTrace();
                            ckExp = "SqlExp";

                        }

                        ckExp = "InsSuc";
                        System.out.println("Succesfully Upadted : ");
                    }



                }catch (SQLException e) {

                }



            }
        }else{
            ckExp = "SqlExp";
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
        /*OPListTB oPListTB;
        oPListTB = new OPListTB(1,"","",dictVal.get("itemLU").toString(),itemQty.getText().toString().trim(),itemSuppCode.getText().toString().trim(),dictVal.get("supp_code").toString(),dictVal.get("descr").toString(),dictVal.get("midasCode").toString());
        long trest =  db.updateOPListTB(oPListTB);
        Log.d("myTag ", "This is my message Filter" + trest);*/
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

        dictTemp = selectArray("itemTemp", "select id, PoCode, descp, shipTo, delvDatetim, shipCharge From poCode Where PoCode = '" + itmID + "'");
        Integer resCountTM = Integer.parseInt(String.valueOf(dictTemp.get("count")));

        System.out.println("The value for the int : " + resCountTM);

        ckExp = dictTemp.get("error_type").toString();

        if (!ckExp.equals("SqlExp")){


            if (resCountTM > 0) {
                ckExp = "EditItem";


                dictVal = dictTemp;

            }else{
                ckExp = "ClearAll";


            }

        }

        return ckExp;

    }




    private Dictionary selectArray(String type, String query) {

        Dictionary dict = new Hashtable();

        Integer count = 0;
        String id = "",itemid = "", poCode = "", descr = "", ship_to = "", deliv_date = "", ship_charge = "", supp_code = "", error_type = "Success";

        stmt = mConnection();

        if (stmt != null) try {


            ResultSet rs = stmt.executeQuery(query);

            //SampleArrayList.clear();
            dict.isEmpty();
            while (rs.next()) {

                count = count + 1;

                if (type.equals("itemTemp")) {

                    id = rs.getString("id");
                    itemid = id;
                    poCode = rs.getString("PoCode");
                    descr = rs.getString("descp");
                    ship_to = rs.getString("shipTo");
                    deliv_date = rs.getString("delvDatetim");
                    ship_charge = rs.getString("shipCharge");


                }else{
                    id = rs.getString("id");
                    itemid = id;
                    poCode = rs.getString("PoCode");
                    descr = rs.getString("descp");
                    ship_to = rs.getString("shipTo");
                    deliv_date = rs.getString("delvDatetim");
                    ship_charge = rs.getString("shipCharge");

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





        if (type.equals("itemTemp")) {


            dict.put("count", count);
            dict.put("fnType", "new");
            dict.put("id", id);
            dict.put("itemId", itemid);
            dict.put("poCode", poCode);
            dict.put("descr", descr);
            dict.put("ship_to", ship_to);
            dict.put("deliv_date", deliv_date);
            dict.put("ship_charge", ship_charge);
            dict.put("error_type", error_type);


        }else{


            dict.put("count", count);
            dict.put("fnType", "edit");
            dict.put("id", id);
            dict.put("itemId", itemid);
            dict.put("poCode", poCode);
            dict.put("descr", descr);
            dict.put("ship_to", ship_to);
            dict.put("deliv_date", deliv_date);
            dict.put("ship_charge", ship_charge);
            dict.put("error_type", error_type);

        }

        return  dict;


    }

    public void onActionQAdd(String type, String itemLU){

        Intent i = new Intent(UpdPOCSItemActivity.this, OtherActivity.class);
        i.putExtra("ActType", type);
        i.putExtra("ActFrom", "Update");
        i.putExtra("ActFromSub", "");
        i.putExtra("itmLU", itemLU);
        startActivityForResult(i, 10001);

        itemPOCode.getText().clear();
        itemDescription.getText().clear();
        itemShipTo.getText().clear();
        itemDeliveryDate.getText().clear();
        itemShipCharge.getText().clear();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)){





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


    private void showSoftKeyBoard(EditText textEdit) {
        InputMethodManager mgr =      (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(textEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
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
