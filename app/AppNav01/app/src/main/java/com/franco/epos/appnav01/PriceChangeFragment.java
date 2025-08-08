package com.franco.epos.appnav01;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.SettingsTB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class PriceChangeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //FloatingActionButton fab;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PriceChangeFragment.OnFragmentInteractionListener mListener;


    private String PageName;
    private String ActType;
    private String actionType = "";

    private Button btnScan;
    private Button btnSr;
    private Button btnSave;
    private Button btnClr;


    private EditText itemSearch, itemDescription, itemInPrice, itemStock, itemPrice;

    private String ipaddress, port;
    private String staffID = "";
    private String staffName = "";
    private String LogType = "FO";
    private String tillType = "";
    private String serIPAdd = "";
    Set<String> fetch;

    private Dictionary dictVal = new Hashtable();

    private Connection connection = null;
    private String ConnectionURL = null;
    Statement stmt = null;



    DatabaseHelper db;
    private SharedPreferences pref;
  //  private DatabaseHelper db;

    private RelativeLayout loginLoader;

    private String lType = "" , lVal = "";

    public PriceChangeFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PriceChangeFragment newInstance(String param1, String param2) {
        PriceChangeFragment fragment = new PriceChangeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        System.out.println("---------key_value."+inflater);

        readBundle(getArguments());

        System.out.println("Page: Main " + PageName);
        System.out.println("Types: " + ActType);


        View view = inflater.inflate(R.layout.fragment_price_change,   container, false);


        if(PageName.equals("Main")){
            final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();

            if (fab != null) {
                ((MainActivity) getActivity()).hideFloatingActionButton();
            }
        }else{

        }



        itemSearch = view.findViewById(R.id.input_search);
        itemDescription = view.findViewById(R.id.input_descr);
        itemInPrice = view.findViewById(R.id.input_price);
        itemStock = view.findViewById(R.id.current_stock);
        itemPrice = view.findViewById(R.id.current_price);

        db = new DatabaseHelper(getContext());
       // actionType = getIntent().getStringExtra("ActType");
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        staffID = pref.getString("staffID",null);
        staffName = pref.getString("staffName",null);
        tillType = pref.getString("tillType",null);
        fetch = pref.getStringSet("CIPAddArray", null);
        serIPAdd = pref.getString("SIPAdd",null);


        SettingsTB setTb = db.getSetTBByType("SERVER");
        ipaddress = setTb.getIpaddress();
        port = setTb.getPort();
        staffID = pref.getString("staffID",null);

        loginLoader = (RelativeLayout) view.findViewById(R.id.loadingPanel);
        loginLoader.setVisibility(View.GONE);


        itemSearch.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                System.out.println("The keycode : " + event.getAction() + keyCode);

                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    // Toast.makeText(context, itemSearch.getText(), Toast.LENGTH_SHORT).show();

                    showLoadingActionButton();
                    String ckConn = mConnectionCheck("");
                    if (ckConn.equals("WifiCon")){

                        PriceTaskParams mParams = new PriceTaskParams("Search", "", 0);
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
        btnScan = view.findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent i = new Intent(getActivity(), BarcodeActivity.class);
                // i.putExtra("ActType", CURRENT_AVAL);
                i.putExtra("ActFrom", "edit");
                //  i.putExtra("itmID", itmID);
                startActivityForResult(i, 10001);

            }

        });

        btnSr = view.findViewById(R.id.btn_search);
        btnSr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("myTag", "This is my message rs" );

                //setSerItemDataAdapter(actionType);

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    PriceTaskParams mParams = new PriceTaskParams("Search", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }

            }

        });

        btnSave =(Button)view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

           // mSaveItem("");

            mSaveCheck("");

            }

        });

        btnClr =(Button)view.findViewById(R.id.btn_cancel);
        btnClr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d("myTag", "This is my message rs" );
                /*itemSearch.getText().clear();
                itemDescription.getText().clear();
                itemInPrice.getText().clear();
                itemStock.getText().clear();
                itemPrice.getText().clear();
                itemSearch.requestFocus();
                showSoftKeyBoard(itemSearch);*/
                mClear();


            }

        });

        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }


    private static class PriceTaskParams {
        String hParaOne;
        String hParaTwo;
        int recPos;


        PriceTaskParams(String hParaOne, String hParaTwo, int recPos) {
            this.hParaOne = hParaOne;
            this.hParaTwo = hParaTwo;
            this.recPos = recPos;

        }
    }

    private class LongOperation extends AsyncTask<PriceTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "";
        int mPos = 0;

        public LongOperation(boolean showCommit) {
            super();
            // do stuff

        }

        @Override
        protected String doInBackground(PriceTaskParams... params) {




            loadMStrOne  = params[0].hParaOne;
            loadMStrTwo  = params[0].hParaTwo;
            mPos = params[0].recPos;



            //String mVal = params[0];

            System.out.println("The value in doInBackground : " + loadMStrOne);

            String result = "0";

            if (loadMStrOne.equals("Search")) {
                result = setSerItemDataAdapter(ActType);


            }else if(loadMStrOne.equals("Save")){
               // result = mEditItem(ActType, mPos);
                result = mSaveItem(loadMStrTwo);

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

                if (result.equals("NoItem")) {

                    finishLongTask("NoItem","No Item found");


                }else if(result.equals("UpdItem")){

                    finishLongTask("UpdItem","Update Item");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");
                }

               // setupRecyclerView();




            }else if(loadMStrOne.equals("Save")){
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

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("No Item found");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            onActionQAdd(lType,lVal);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();



        }else if(action.equals("UpdItem")){

            itemDescription.setText(dictVal.get("descr").toString());
            itemStock.setText(dictVal.get("stock").toString());
            itemPrice.setText(dictVal.get("price").toString());
            itemInPrice.getText().clear();
            itemInPrice.requestFocus();
            showSoftKeyBoard(itemInPrice);



        }else if(action.equals("ClearAll")) {

            /*itemDescription.getText().clear();
            itemStock.getText().clear();
            itemPrice.getText().clear();
            itemQty.getText().clear();
            itemReason.getText().clear();
            itemSearch.getText().clear();
            itemSearch.requestFocus();*/

            mClear();

        }else if(action.equals("InsFail")){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Error in Saving");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();



        }else if(action.equals("Error") || action.equals("SqlExp")) {
            // btnCommit.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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
    private String setSerItemDataAdapter(final String type) {
        Dictionary dict = new Hashtable();
        String ckExp = "";


        dict = selectArray("itemProduct","select id, ItemLookup, description, barCode, price, avaiable from itemproduct where barCode = '"+itemSearch.getText().toString().trim()+"' OR ItemLookup = '"+itemSearch.getText().toString().trim()+"'");
        Integer resCount = Integer.parseInt(String.valueOf(dict.get("count")));
        ckExp = dict.get("error_type").toString();

        if (!ckExp.equals("SqlExp")){


            if (resCount > 0){

                ckExp = "UpdItem";

                /*itemDescription.setText(dict.get("descr").toString());
                itemStock.setText(dict.get("stock").toString());
                itemPrice.setText(dict.get("price").toString());
                itemInPrice.getText().clear();
                itemInPrice.requestFocus();
                showSoftKeyBoard(itemInPrice);*/

                dictVal = dict;

            }else{

                ckExp = "NoItem";
                lType = type;
                lVal = itemSearch.getText().toString();

                /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage("No Item found");
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                onActionQAdd(type,itemSearch.getText().toString());

                            }
                        });


                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();*/

            }

        }




        return ckExp;
    }

    public void onActionQAdd(String type, String itemLU){

        Intent i = new Intent(getActivity(), OtherActivity.class);
        i.putExtra("ActType", type);
        i.putExtra("ActFrom", "PriceChange");
        i.putExtra("itmLU", itemLU);
        startActivityForResult(i, 10001);

        //itemSearch.getText().clear();
        itemDescription.getText().clear();
        itemInPrice.getText().clear();
        itemStock.getText().clear();
        itemPrice.getText().clear();
        itemSearch.requestFocus();
        showSoftKeyBoard(itemSearch);

    }

    private Dictionary selectArray(String type, String query) {

        Dictionary dict = new Hashtable();

        Integer count = 0;
        String id = "",itemid = "", itemLU = "", descr = "", barCode = "", price = "", stock = "", in_qty = "", adj_type = "", error_type = "Success";

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
                price = rs.getString("price");
                stock = rs.getString("avaiable");



               // Log.d("myTag", "This is my message rss dfdf " + rs.getString("ItemLookup") + itemSearch.getText());
                error_type = "Success";

            }


        } catch (SQLException e) {

            Log.d("myTag", "This is my message rss hhhh" + e.getMessage());
            e.printStackTrace();
            error_type = "SqlExp";

        }
        else{

            Log.d("myTag", "Stmt in empty");
            error_type = "SqlExp";

        }

        dict.put("count", count);
        dict.put("fnType", "edit");
        dict.put("id", id);
        dict.put("itemId", itemid);
        dict.put("itemLU", itemLU);
        dict.put("descr", descr);
        dict.put("barCode", barCode);
        dict.put("price", price);
        dict.put("stock", stock);
        dict.put("error_type", error_type);


        return  dict;


    }

    private void mSaveCheck(String type){
        if(itemInPrice.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter input price");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemInPrice.getText().clear();
                            itemInPrice.requestFocus();
                            showSoftKeyBoard(itemInPrice);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else{

            showLoadingActionButton();
            String ckConn = mConnectionCheck("");

            if (ckConn.equals("WifiCon")){

                PriceTaskParams mParams = new PriceTaskParams("Save", type, 0);
                new LongOperation(false).execute(mParams);


            }else{

                hideLoadingActionButton();
                finishLongTask("ErrorWifi","Connection error");
            }


        }
    }

    private String mSaveItem(String type) {
        String ckExp = "SqlExp";

        /*if(itemInPrice.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter input price");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemInPrice.getText().clear();
                            itemInPrice.requestFocus();
                            showSoftKeyBoard(itemInPrice);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else{


        }*/


        String logdat = DateFormat.getDateTimeInstance().format(new Date());
        stmt = mConnection();
        if (stmt != null) {
            try {

                double trackPrice, Rprice;

                String logDescription;

                trackPrice = Double.parseDouble(dictVal.get("price").toString());
                Rprice = Double.parseDouble(itemInPrice.getText().toString());

                int resultProd = stmt.executeUpdate("UPDATE itemProduct set Price =	'" + Rprice + "',  Rprice =	'" + Rprice + "' where id = '" + dictVal.get("itemId") + "'");

                if(resultProd == 1) {


                    if (tillType.equals("Server")){
                        Iterator<String> newStringsIter = fetch.iterator();
                        String ipt;
                        while (newStringsIter.hasNext()) {
                            System.out.println("The sting value : " + newStringsIter.next());
                            ipt = newStringsIter.next();
                            int resultClient = stmt.executeUpdate("insert into clientUpdate(tableName, itemlookup, upType, ipaddress) VALUES ('itemProductBO', '" + dictVal.get("itemLU") + "', 'update', '" + ipt + "')");

                            if(resultClient == 1) {

                            }

                        }
                    }else if(tillType.equals("Client")){


                        int resultServer = stmt.executeUpdate("insert into update_server(tableName, itemlookup, upType, ipaddress) VALUES ('itemProductBO', '" + dictVal.get("itemLU") + "', 'update', '" + serIPAdd + "')");

                        if(resultServer == 1) {

                        }



                    }

                    System.out.println("Updated successfully" + dictVal.get("itemLU"));

                    if (Rprice != trackPrice){
                        logDescription = staffName + " has changed the price for " + dictVal.get("descr") + " (" + dictVal.get("itemLU") + ") " + " from " + trackPrice + " to " + Rprice;
                        int resultPriceN = stmt.executeUpdate("Insert into log(description, staffid, BFoffice, logType, dattim,itemlookup,price_old,price) VALUES ('" + logDescription + "','" + staffID + "', '" + LogType + "', 'Price','" + logdat + "','" + dictVal.get("itemLU") + "','" + trackPrice + "', '" + Rprice + "')");

                        if(resultPriceN == 1) {

                        }
                    }

                   // mClear();

                    ckExp = "InsSuc";

                }else{

                    System.out.println("Error in saving item " + dictVal.get("itemLU"));
                    ckExp = "InsFail";

                }





            } catch (SQLException e) {

                Log.d("Error Type", "Exception : " + e.getMessage());
                e.printStackTrace();
                ckExp = "SqlExp";
            }


        }else{
            ckExp = "SqlExp";
        }




        return ckExp;

    }

    private void mClear(){
        itemSearch.getText().clear();
        itemDescription.getText().clear();
        itemInPrice.getText().clear();
        itemStock.getText().clear();
        itemPrice.getText().clear();
        itemSearch.requestFocus();
        showSoftKeyBoard(itemSearch);
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

        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)){

            if (data.getStringExtra("NavValue").equals("QuickAdd")) {

                itemSearch.setText(data.getStringExtra("itemLUValue"));

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    PriceTaskParams mParams = new PriceTaskParams("Search", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }

               // setSerItemDataAdapter(actionType);

            }else if (data.getStringExtra("NavValue").equals("QuickAddExit")) {

                mClear();

            }else{

                itemSearch.setText(data.getStringExtra("BarCodeValue"));

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    PriceTaskParams mParams = new PriceTaskParams("Search", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }

               // setSerItemDataAdapter(actionType);

            }



            // Toast.makeText(getActivity(),"You clicked yes button",Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityCallBack(String actTyp, String val){

    }
    private void showSoftKeyBoard(EditText textEdit) {
        InputMethodManager mgr =      (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(textEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void showLoadingActionButton() {
        loginLoader.setVisibility(View.VISIBLE);

    }

    public void hideLoadingActionButton() {
        loginLoader.setVisibility(View.GONE);
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            PageName = bundle.getString("PageName");
            ActType = bundle.getString("ActType");
            // ActType = bundle.getInt("age");
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
      //  InputMethodManager imm = (InputMethodManager) MainActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
