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
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

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

public class PackQtyFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //FloatingActionButton fab;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private PackQtyFragment.OnFragmentInteractionListener mListener;


    private String PageName;
    private String ActType;
    private String itemLU;
    private String actionType = "";
    private String itemLookupQD = "";

    private double salesprice;
    private double Itmprice;
    private double itemCost;
    private double Rprice;
    private double Rcost;
    private double OnHand;
    private double itmPriceA;
    private double itmPriceB;
    private double itmPriceC;
    private double itmMSRP;
    private double itmLowBound;
    private double itmUpBound;
    private double itmrepcost;
    private double itmBuydown;
    private String itmMrgProf, RMargin, primSupplier;
    private String salescheck, salesstDate, salesendDate, salesShedules;


    private Button btnQty;
    private Button btnPrice;
    private Button btnBoth;
    private Button btnClr;


    private EditText itemLookup, itemDescription, itemPckQty, itemPckNos, itemPrice;
//    private AppCompatSpinner departSpinner, vatSpinner;
//    ImageView imageViewQrCode;

    private String ipaddress, port;
    private String staffID = "";
    private String staffName = "";
    private String LogType = "FO";
    private String tillType = "";
    private String serIPAdd = "";
    Set<String> fetch;

/*    List<String> spinnerDepartArray, spinnerVatArray;
    ArrayAdapter<String> adapterDepart, adapterVat;
    private List<Dictionary> departsDict;
    int selDepartItem = -1, selVatItem = -1;*/
   /* Boolean checkDUN = false;
    private String defaultDep, defaultVat;*/


    private Dictionary dictVal = new Hashtable();

    private Connection connection = null;
    private String ConnectionURL = null;
    Statement stmt = null;




    DatabaseHelper db;
    private SharedPreferences pref;
    //  private DatabaseHelper db;

    private RelativeLayout loginLoader;
    private String lType = "" , lVal = "";

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    // private EMDKWrapper emdkWrapper = null;

    public PackQtyFragment() {
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
    public static PackQtyFragment newInstance(String param1, String param2) {
        PackQtyFragment fragment = new PackQtyFragment();
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


        View view = inflater.inflate(R.layout.fragment_pack_qty,   container, false);


        if(PageName.equals("Main")){
            final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();

            if (fab != null) {
                ((MainActivity) getActivity()).hideFloatingActionButton();
            }
        }else{

        }

        btnClr =(Button)view.findViewById(R.id.btn_cancel);
        itemLookupQD = "";
        if (PageName.equals("Update") || PageName.equals("PriceChange")){
            itemLookupQD = itemLU;
            btnClr.setText("Exit");
        }


//        itemID = view.findViewById(R.id.input_id);
        itemLookup = view.findViewById(R.id.input_itemLook);
        itemDescription = view.findViewById(R.id.input_descr);
        itemPckQty = view.findViewById(R.id.input_pckQty);
        itemPckNos = view.findViewById(R.id.input_pcknos);
        itemPrice = view.findViewById(R.id.input_price);
        /*itemBarcode = view.findViewById(R.id.input_barcode);
        departSpinner = view.findViewById(R.id.depart_spinner);
        vatSpinner = view.findViewById(R.id.vat_spinner);
        imageViewQrCode = (ImageView) view.findViewById(R.id.imageView2);

        imageViewQrCode.setImageResource(android.R.color.transparent);*/

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


        showLoadingActionButton();
        String ckConn = mConnectionCheck("");
        if (ckConn.equals("WifiCon")){

            PackQtyFragment.QAddTaskParams mParams = new PackQtyFragment.QAddTaskParams("GetItem", "", 0);
            new PackQtyFragment.LongOperation(false).execute(mParams);

        }else{

            hideLoadingActionButton();
            finishLongTask("ErrorWifi","Connection error");
        }

        // getItemCode();





        btnQty =(Button)view.findViewById(R.id.btn_qty);
        btnQty.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (PageName.equals("Update")){
                    showLoadingActionButton();
                    String ckConn = mConnectionCheck("");
                    if (ckConn.equals("WifiCon")){

                        PackQtyFragment.QAddTaskParams mParams = new PackQtyFragment.QAddTaskParams("bPckQty", "", 0);
                        new PackQtyFragment.LongOperation(false).execute(mParams);

                    }else{

                        hideLoadingActionButton();
                        finishLongTask("ErrorWifi","Connection error");
                    }
                }else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("This feature is not applicable");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // onActionQAdd(lType,lVal);
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }



                // mSaveItem("Save");

            }

        });

        btnPrice = (Button)view.findViewById(R.id.btn_price);
        btnPrice.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    PackQtyFragment.QAddTaskParams mParams = new PackQtyFragment.QAddTaskParams("bPckPrice", "", 0);
                    new PackQtyFragment.LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }

                // mSaveItem("SaveLBL");

            }

        });
        btnBoth = (Button)view.findViewById(R.id.btn_both);
        btnBoth.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (PageName.equals("Update")){
                    showLoadingActionButton();
                    String ckConn = mConnectionCheck("");
                    if (ckConn.equals("WifiCon")){

                        PackQtyFragment.QAddTaskParams mParams = new PackQtyFragment.QAddTaskParams("bPckBoth", "", 0);
                        new PackQtyFragment.LongOperation(false).execute(mParams);

                    }else{

                        hideLoadingActionButton();
                        finishLongTask("ErrorWifi","Connection error");
                    }
                }else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("This feature is not applicable");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // onActionQAdd(lType,lVal);
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }



                // mSaveItem("SaveLBL");

            }

        });


        btnClr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d("myTag", "This is my message rs" );
                if (PageName.equals("Update") || PageName.equals("PriceChange")){
                    /*itemLookupQD = itemLU;
                    btnClr.setText("Exit");*/
                    exitQA();
                }else{

                    mClear();
                }



            }

        });

        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }



    private static class QAddTaskParams {
        String hParaOne;
        String hParaTwo;
        int recPos;


        QAddTaskParams(String hParaOne, String hParaTwo, int recPos) {
            this.hParaOne = hParaOne;
            this.hParaTwo = hParaTwo;
            this.recPos = recPos;

        }
    }

    private class LongOperation extends AsyncTask<PackQtyFragment.QAddTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "";
        int mPos = 0;

        public LongOperation(boolean showCommit) {
            super();
            // do stuff

        }

        @Override
        protected String doInBackground(PackQtyFragment.QAddTaskParams... params) {




            loadMStrOne  = params[0].hParaOne;
            loadMStrTwo  = params[0].hParaTwo;
            mPos = params[0].recPos;



            //String mVal = params[0];

            System.out.println("The value in doInBackground : " + loadMStrOne);

            String result = "0";

            if (loadMStrOne.equals("GetItem")) {
                //result = setSerItemDataAdapter(ActType);
                // result = getItemCode();

                result = mQucikConn("",0);

            }else if(loadMStrOne.equals("bPckQty")){
                // result = mEditItem(ActType, mPos);
                // result = mSaveItem(loadMStrTwo);
                result = mQucikConn("",0);

            }else if(loadMStrOne.equals("bPckPrice")){
                result = mQucikConn("",0);
            }else if(loadMStrOne.equals("bPckBoth")){
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

            if (loadMStrOne.equals("GetItem")){

                if (result.equals("Success")) {

                    //finishLongTask("NoItem","No Item found");
                    setSerItemDataAdapter("");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");
                }

            }else if(loadMStrOne.equals("bPckQty")){
                if (result.equals("Success")){
                    // finishLongTask("ClearAll","Clear All");

                    mSaveItem("bPckQty");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");

                }
            }else if(loadMStrOne.equals("bPckPrice")){

                if (result.equals("Success")){
                    // finishLongTask("ClearAll","Clear All");
                    mSaveItem("bPckPrice");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){
                    finishLongTask("Error","Connection error");
                }

            }else if(loadMStrOne.equals("bPckBoth")){

                if (result.equals("Success")){
                    // finishLongTask("ClearAll","Clear All");
                    mSaveItem("bPckBoth");

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
                            // onActionQAdd(lType,lVal);

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();



        }else if(action.equals("UpdItem")){

            /*itemDescription.setText(dictVal.get("descr").toString());
            itemStock.setText(dictVal.get("stock").toString());
            itemPrice.setText(dictVal.get("price").toString());
            itemInPrice.getText().clear();
            itemInPrice.requestFocus();
            showSoftKeyBoard(itemInPrice);*/



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






    private void setSerItemDataAdapter(String type) {
        String lookUpID = itemLookupQD.trim();
        Dictionary dict = new Hashtable();

        dict = selectArray("itemProduct","select id, ItemLookup, description, barCode, price, avaiable, pckQty, pckPrice from itemproduct where barCode = '"+lookUpID+"' OR ItemLookup = '"+lookUpID+"'");
        Integer resCount = Integer.parseInt(String.valueOf(dict.get("count")));

        if (resCount > 0){








            itemLookup.setText(dict.get("itemLU").toString());
            itemDescription.setText(dict.get("descr").toString());
            itemPckQty.setText(dict.get("pck_qty").toString());
            itemPckNos.setText("");

            itemPrice.setText(dict.get("pck_price").toString());

            dictVal = dict;
        }else{

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("No Item found");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            /*itemSearch.getText().clear();
                            itemDescription.getText().clear();
                            itemInPrice.getText().clear();
                            itemStock.getText().clear();
                            itemPrice.getText().clear();
                            itemSearch.requestFocus();
                            showSoftKeyBoard(itemSearch);*/
                            if (PageName.equals("Update") || PageName.equals("PriceChange")){

                                exitQA();
                            }else{

                                mClear();
                            }

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
    }



    private Dictionary selectArray(String type, String query) {

        Dictionary dict = new Hashtable();

        Integer count = 0;
        String id = "",itemid = "", itemLU = "", descr = "", barCode = "", price = "", stock = "", in_qty = "", adj_type = "", pck_qty = "", pck_price = "";

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
                pck_qty = rs.getString("pckQty");
                pck_price = rs.getString("pckPrice");



                Log.d("myTag", "This is my message rss dfdf " + pck_qty + " Price : "+ pck_qty);


            }


        } catch (SQLException e) {

            Log.d("myTag", "This is my message rss hhhh" + e.getMessage());
            e.printStackTrace();

        }
        else{

            Log.d("myTag", "Stmt in empty");

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
        /*dict.put("pck_qty", pck_qty);
        dict.put("pck_price", pck_price);*/
        if (pck_qty == null) {
            Log.d("Pack qty", "Stmt in empty");
            dict.put("pck_qty", "");
        }else {
            dict.put("pck_qty", pck_qty);
        }
        if (pck_price == null) {
            Log.d("Pack price", "Stmt in empty");
            dict.put("pck_price", "");
        }else{
            dict.put("pck_price", pck_price);
        }
        /*if (pck_qty.equals("") || pck_qty.equals("null") ){
            Log.d("Pack qty", "Stmt in empty");
            dict.put("pck_qty", "");
        }else {

        }
        if (pck_price.equals("") || pck_price.equals("null")){
            Log.d("Pack price", "Stmt in empty");
            dict.put("pck_price", "");
        }else{

        }*/




        return  dict;


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

        return  count;
    }



    private void mSaveItem(final String type) {

        if (itemLookup.getText().toString().trim().length() < 1) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter Item lookup");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            /*itemLookup.getText().clear();
                            itemLookup.requestFocus();
                            showSoftKeyBoard(itemLookup);*/
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else if(itemPckQty.getText().toString().trim().length() < 1 || Integer.parseInt(itemPckQty.getText().toString()) <= 0) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter Pack Quantity");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemPckQty.getText().clear();
                            itemPckQty.requestFocus();
                            showSoftKeyBoard(itemPckQty);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else{
            if (type.equals("bPckQty")) {

                if(itemPckNos.getText().toString().trim().length() < 1 || Integer.parseInt(itemPckNos.getText().toString()) <= 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Please enter Number of Packs");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    itemPckNos.getText().clear();
                                    itemPckNos.requestFocus();
                                    showSoftKeyBoard(itemPckNos);
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }else{
                    insertQuickItem(type);
                }
            }else if(type.equals("bPckPrice")){
                if(itemPrice.getText().toString().trim().length() < 1 || Double.parseDouble(itemPrice.getText().toString()) <= 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Please enter pack price");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    itemPrice.getText().clear();
                                    itemPrice.requestFocus();
                                    showSoftKeyBoard(itemPrice);
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{
                    insertQuickItem(type);
                }

            }else if(type.equals("bPckBoth")){
                if(itemPckNos.getText().toString().trim().length() < 1 || Integer.parseInt(itemPckNos.getText().toString()) <= 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Please enter Number of Packs");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    itemPckNos.getText().clear();
                                    itemPckNos.requestFocus();
                                    showSoftKeyBoard(itemPckNos);
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else if(itemPrice.getText().toString().trim().length() < 1 || Double.parseDouble(itemPrice.getText().toString()) <= 0) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage("Please enter pack price");
                        alertDialogBuilder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        itemPrice.getText().clear();
                                        itemPrice.requestFocus();
                                        showSoftKeyBoard(itemPrice);
                                    }
                                });


                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                }else{
                    insertQuickItem(type);
                }
            }
        }






    }
    private void insertQuickItem(String type) {
        /*showLoadingActionButton();
        Toast.makeText(getActivity(), "I am true" + type, Toast.LENGTH_SHORT).show();
        hideLoadingActionButton();*/

        /*itemDescription.setText(dictVal.get("descr").toString());
        itemStock.setText(dictVal.get("stock").toString());
        itemPrice.setText(dictVal.get("price").toString())*/
        String ageCkVal, logDescription, logdat, dtendtim, dttim;
        dtendtim = "00:00:00";
        dttim = "00:00:00";

        logdat = DateFormat.getDateTimeInstance().format(new Date());
        showLoadingActionButton();
        if (!dictVal.get("pck_qty").toString().equals(itemPckQty.getText().toString().trim()) || !dictVal.get("pck_price").toString().equals(itemPrice.getText().toString().trim())){
//            Toast.makeText(getActivity(), "I am true" + type + dictVal.get("pck_qty").toString() + dictVal.get("pck_price").toString(), Toast.LENGTH_SHORT).show();

//            String strquery = "Insert into itemProduct(ItemLookup,description,descriptionExtend,productBrand,modelNo,sizeGroup,barCode,itemType,department,category,vat,price,cost,profitMargin,oneHand,commited,avaiable,offline,dateCreate,lastRecevied,lastorder,lastSold,lastCost,replaceCost,restockLevel,reoderPoint,Rprice,Rcost,Rmargin,price_A,price_B,price_C,MaxSellRetailPrice,lowerBound,upperBound,Itemsales,salesPrice,startDate,endDate,shedule,buydown,buydownQuantity,discount,discountscheme,dimage,refund,ageCheck,ageCheckdt,ageCheckdtend,handlCharge,notes,uploadfile,wtScale,inPrice,damageSales,damagePrice,expireCk,expireDate)values('" + itemLookup.getText().toString().trim() + "','" + itemDescription.getText().toString().trim() + "','" + itemDescription.getText().toString().trim() + "','','','','" + itemBarcode.getText().toString().trim() + "','','" + txtdepart + "','" + txtcate + "','" + vatSpinner.getSelectedItem().toString() + "','" + Itmprice + "','" + itemCost + "','" + itmMrgProf + "','0','0','0','0','" + logdat + "','1900-01-01 00:00:00.000','1900-01-01 00:00:00.000','1900-01-01 00:00:00.000','0','" + itmrepcost + "','0','0','" + Rprice + "','" + Rcost + "','" + RMargin + "','" + itmPriceA + "','" + itmPriceB + "','" + itmPriceC + "','" + itmMSRP + "','" + itmLowBound + "','" + itmUpBound + "','0','" + salesprice + "','" + salesstDate + "','" + salesendDate + "','" + salesShedules + "','" + itmBuydown + "','0','','0','', '0', '" + ageCkVal + "', '" + dttim + "', '" + dtendtim + "', '', '',  '', '0', '0','0','0', '0','" + expsdate + "')";

            // String strquery = "Insert into itemProduct(ItemLookup,description,descriptionExtend,productBrand,modelNo,sizeGroup,barCode,itemType,department,category,vat,price,cost,profitMargin,oneHand,commited,avaiable,offline,dateCreate,lastRecevied,lastorder,lastSold,lastCost,replaceCost,restockLevel,reoderPoint,Rprice,Rcost,Rmargin,price_A,price_B,price_C,MaxSellRetailPrice,lowerBound,upperBound,Itemsales,salesPrice,startDate,endDate,shedule,buydown,buydownQuantity,discount,discountscheme,dimage,refund,ageCheck,ageCheckdt,ageCheckdtend,handlCharge,notes,uploadfile,wtScale,inPrice,damageSales,damagePrice,expireCk,expireDate)values('" + itemLookup.getText().toString().trim() + "','" & Trim(txtDescript.Text) & "','" & txtDescript.Text & "','','','','" & txtBarCode.Text & "','','" & txtdepart & "','" & txtcate & "','" & vatList.SelectedItem & "','" & Itmprice & "','" & itemCost & "','" & itmMrgProf & "','0','0','0','0','" & logdat & "','','','','0','" & itmrepcost & "','0','0','" & Rprice & "','" & Rcost & "','" & RMargin & "','" & itmPriceA & "','" & itmPriceB & "','" & itmPriceC & "','" & itmMSRP & "','" & itmLowBound & "','" & itmUpBound & "','0','" & salesprice & "','" & salesstDate & "','" & salesendDate & "','" & salesShedules & "','" & itmBuydown & "','0','','0','', '0', '" & ageCkVal & "', '" & dttim & "', '" & dtendtim & "', '', '',  '', '0', '0','0','0', '0','" & expsdate & "')"
//            String strquery = "UPDATE tempProAdjustment SET staff_id='" + staffID + "', dateTime='" + logdat + "',cur_stock = '" + dictVal.get("stock").toString().trim() + "',price_changed = '" + itemPrice.getText().toString().trim() + "' WHERE itemID='" + dictVal.get("itemId") + "'";
            stmt = mConnection();
            if (stmt != null) {
                try {

                    int resultProd = stmt.executeUpdate("UPDATE itemProduct set pckQty =	'" + itemPckQty.getText().toString().trim() + "', pckPrice = '" + itemPrice.getText().toString().trim() + "' where id = '" + dictVal.get("itemId") + "'");

                    if(resultProd == 1) {


                        if (tillType.equals("Server")){
                            Iterator<String> newStringsIter = fetch.iterator();
                            String ipt;
                            while (newStringsIter.hasNext()) {
                               // System.out.println("The sting value : " + newStringsIter.next());
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

//                        ckExp = "Success";

                    }else{

                        System.out.println("Error in saving item " + dictVal.get("itemLU"));



                    }



                } catch (SQLException e) {

                    Log.d("Error Type", "Exception : " + e.getMessage());
                    e.printStackTrace();

//                    hideLoadingActionButton();

                }


            }else{
//                hideLoadingActionButton();
            }

        }



        Integer pckQntVal = 0;
        Integer pckNoVal = 0;
        Double pckPriceVal = 0.00;
        Double pckItemPriceVal = 0.00;
        Integer pcktotalVal = 0;



        if (type.equals("bPckQty")) {
            pckQntVal = Integer.parseInt(itemPckQty.getText().toString());
            pckNoVal = Integer.parseInt(itemPckNos.getText().toString());
            pcktotalVal = (pckQntVal * pckNoVal);


            Intent resultIntent = new Intent();
            resultIntent.putExtra("NavValue", "PckUpdQty");
            resultIntent.putExtra("itemLUValue", itemLookup.getText().toString());
            resultIntent.putExtra("itemPckQUpdVal", pcktotalVal.toString());
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        }else if(type.equals("bPckPrice")){

            pckQntVal = Integer.parseInt(itemPckQty.getText().toString());
            pckPriceVal = Double.parseDouble(itemPrice.getText().toString());
            pckItemPriceVal = (pckPriceVal / pckQntVal);
            pckItemPriceVal = round(pckItemPriceVal,2);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("NavValue", "PckUpdPrice");
            resultIntent.putExtra("itemLUValue", itemLookup.getText().toString());
            resultIntent.putExtra("itemPckPUpdVal", pckItemPriceVal.toString());
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        }else if(type.equals("bPckBoth")){

            pckQntVal = Integer.parseInt(itemPckQty.getText().toString());
            pckNoVal = Integer.parseInt(itemPckNos.getText().toString());
            pckPriceVal = Double.parseDouble(itemPrice.getText().toString());
            pcktotalVal = (pckQntVal * pckNoVal);
            pckItemPriceVal = (pckPriceVal / pckQntVal);
            pckItemPriceVal = round(pckItemPriceVal,2);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("NavValue", "PckUpdBoth");
            resultIntent.putExtra("itemLUValue", itemLookup.getText().toString());
            resultIntent.putExtra("itemPckQUpdVal", pcktotalVal.toString());
            resultIntent.putExtra("itemPckPUpdVal", pckItemPriceVal.toString());
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        }

        hideLoadingActionButton();
    }


    private void mfinishAdd(String type){
        if (type.equals("Exist")){
            itemLookup.getText().clear();
            /*itemDescription.getText().clear();
            itemPrice.getText().clear();*/
            /*itemBarcode.getText().clear();
            imageViewQrCode.setImageResource(android.R.color.transparent);*/
            itemLookup.requestFocus();
            showSoftKeyBoard(itemLookup);
        }else{

            if (PageName.equals("Update") || PageName.equals("PriceChange")){

                Intent resultIntent = new Intent();
                resultIntent.putExtra("NavValue", "QuickAdd");
                resultIntent.putExtra("itemLUValue", itemLookup.getText().toString());
                getActivity().setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();

            }else {
                itemLookup.getText().clear();
                itemDescription.getText().clear();
                itemPrice.getText().clear();
                /*itemBarcode.getText().clear();
                imageViewQrCode.setImageResource(android.R.color.transparent);*/
                itemLookup.requestFocus();
                showSoftKeyBoard(itemLookup);
            }



        }


    }


    private void exitQA(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("NavValue", "PckQtyExit");
        resultIntent.putExtra("itemLUValue", itemLookup.getText().toString());
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    private void mClear(){
        //itemSearch.getText().clear();
        itemLookup.getText().clear();
        itemDescription.getText().clear();
        itemPrice.getText().clear();
        /*itemBarcode.getText().clear();
        imageViewQrCode.setImageResource(android.R.color.transparent);*/
        itemLookup.requestFocus();
        showSoftKeyBoard(itemLookup);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 10001) && (resultCode == Activity.RESULT_OK)){

            itemLookup.setText(data.getStringExtra("BarCodeValue"));
            // setSerItemDataAdapter(actionType);
            itemLookupQD = itemLookup.getText().toString();

            showLoadingActionButton();
            String ckConn = mConnectionCheck("");
            if (ckConn.equals("WifiCon")){

                PackQtyFragment.QAddTaskParams mParams = new PackQtyFragment.QAddTaskParams("GetItem", "", 0);
                new PackQtyFragment.LongOperation(false).execute(mParams);

            }else{

                hideLoadingActionButton();
                finishLongTask("ErrorWifi","Connection error");
            }

            //getItemCode();

            // Toast.makeText(getActivity(),"You clicked yes button",Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityCallBack(String actTyp, String val){

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
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
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
            itemLU = bundle.getString("itemLU");
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
