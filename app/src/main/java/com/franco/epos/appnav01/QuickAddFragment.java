package com.franco.epos.appnav01;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;

import com.franco.epos.appnav01.database.model.ItemUploadTB;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.SettingsTB;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.content.Context.INPUT_METHOD_SERVICE;
/*import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.StatusData;*/
public class QuickAddFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //FloatingActionButton fab;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private QuickAddFragment.OnFragmentInteractionListener mListener;


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

    private Button btnScan;
    private Button btnSaveLBL;
    private Button btnSave;
    private Button btnClr;


    private EditText itemID, itemLookup, itemDescription, itemPrice, itemBarcode;
    private AppCompatSpinner departSpinner, vatSpinner;
    ImageView imageViewQrCode;

    private String ipaddress, port;
    private String staffID = "";
    private String staffName = "";
    private String LogType = "FO";
    private String tillType = "";
    private String serIPAdd = "";
    private String myPCName = "";
    Set<String> fetch;

    List<String> spinnerDepartArray, spinnerVatArray;
    ArrayAdapter<String> adapterDepart, adapterVat;
    private List<Dictionary> departsDict;
    int selDepartItem = -1, selVatItem = -1;
    Boolean checkDUN = false;
    private String defaultDep, defaultVat;


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

    public QuickAddFragment() {
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
    public static QuickAddFragment newInstance(String param1, String param2) {
        QuickAddFragment fragment = new QuickAddFragment();
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


        View view = inflater.inflate(R.layout.fragment_quick_add,   container, false);


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


        itemID = view.findViewById(R.id.input_id);
        itemLookup = view.findViewById(R.id.input_itemLook);
        itemDescription = view.findViewById(R.id.input_descr);
        itemPrice = view.findViewById(R.id.input_price);
        itemBarcode = view.findViewById(R.id.input_barcode);
        departSpinner = view.findViewById(R.id.depart_spinner);
        vatSpinner = view.findViewById(R.id.vat_spinner);
        imageViewQrCode = (ImageView) view.findViewById(R.id.imageView2);

        imageViewQrCode.setImageResource(android.R.color.transparent);

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


        spinnerDepartArray =  new ArrayList<String>();
        spinnerVatArray =  new ArrayList<String>();
        departsDict = new ArrayList<>();

        spinnerVatArray = setAdapterList("vat","Select description from vat");
        spinnerDepartArray =  setAdapterList("depart","Select depart_id, description, ageCheck, vat from department order by description asc");

        vatSpinner.setEnabled(false);
        vatSpinner.setClickable(false);
       // vatSpinner.setAdapter(typeAdapter);

        showLoadingActionButton();
        String ckConn = mConnectionCheck("");
        if (ckConn.equals("WifiCon")){

            QAddTaskParams mParams = new QAddTaskParams("GetItem", "", 0);
            new LongOperation(false).execute(mParams);

        }else{

            hideLoadingActionButton();
            finishLongTask("ErrorWifi","Connection error");
        }

       // getItemCode();


        itemBarcode.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // your action here
                    System.out.println("The value");
                    generateBarcode(itemBarcode.getText().toString());
                    hideSoftKeyBoard();
                    //showSoftKeyBoard(itemBarcode);
                    return true;
                }
                return false;
            }
        });


        adapterDepart = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, spinnerDepartArray){

            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    if (checkDUN) {
                        return true;
                    }else {
                        return false;
                    }

                }
                else
                {
                    return true;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent)
            {
                View v = null;
                v = super.getDropDownView(position, null, parent);
                TextView tv = (TextView) v;
                // If this is the selected item position
               /* if (position == selDepartItem) {
                    v.setBackgroundColor(Color.parseColor("#CCCCCC"));
                }
                else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }*/

                if (checkDUN){
                    if (position == selDepartItem) {
                        v.setBackgroundColor(Color.parseColor("#CCCCCC"));
                    }
                    else {
                        // for other views
                        v.setBackgroundColor(Color.WHITE);

                    }
                }else{
                    if (position == selDepartItem || position == 0) {
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
                }


                return v;
            }
        };
        adapterDepart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departSpinner.setAdapter(adapterDepart);

        if (checkDUN) {
            selDepartItem = adapterDepart.getPosition("UNKNOWN");
            departSpinner.setSelection(selDepartItem);

        }else {
            selDepartItem = 0;
            departSpinner.setSelection(selDepartItem);

        }

       // departSpinner.sett


        departSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int myPosition, long myID) {

                ((TextView) selectedItemView).setTextColor(Color.RED);

                Log.i("renderSpinner -> ", "onItemSelected: " + myPosition + "/" + myID + "/" + departSpinner.getSelectedItem().toString());

                Dictionary dictArr = departsDict.get(myPosition);

               // Toast.makeText(getActivity(), "Depart ID : " + dictArr.get("departID").toString() + "Depart Name : " + dictArr.get("departName").toString() + "Depart Vat : " + dictArr.get("departVat").toString(), Toast.LENGTH_LONG).show();

                selVatItem = adapterVat.getPosition(dictArr.get("departVat").toString());
                vatSpinner.setSelection(selVatItem);

                if (dictArr.get("departName").toString().equals("Select")) {
                    itemDescription.setText("");
                } else {
                    itemDescription.setText(dictArr.get("departName").toString());
                }


                if (checkDUN) {

                } else {

                }

                selDepartItem = myPosition;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        adapterVat = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout, spinnerVatArray) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = null;
                v = super.getDropDownView(position, null, parent);
                // If this is the selected item position
                if (position == selVatItem) {
                    v.setBackgroundColor(Color.parseColor("#CCCCCC"));
                } else {
                    // for other views
                    v.setBackgroundColor(Color.WHITE);

                }
                return v;
            }
        };




        adapterVat.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vatSpinner.setAdapter(adapterVat);


        /*vatSpinner.setSelection(adapterDepart.getPosition("Mech"));
        selVatItem = adapterDepart.getPosition("Mech");*/

        if (checkDUN) {

            Dictionary dictArr = departsDict.get(adapterDepart.getPosition("UNKNOWN"));
            selVatItem = adapterVat.getPosition(dictArr.get("departVat").toString());
            vatSpinner.setSelection(selVatItem);

        }else {
            selVatItem = 0;
            vatSpinner.setSelection(selVatItem);

        }







        vatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int myPosition, long myID) {

                Log.i("renderSpinner -> ", "onItemSelected: " + myPosition + "/" + myID + "/" + vatSpinner.getSelectedItem().toString());

                selVatItem = myPosition;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
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


        btnSave =(Button)view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    QAddTaskParams mParams = new QAddTaskParams("Save", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
                }

               // mSaveItem("Save");

            }

        });

        btnSaveLBL = (Button)view.findViewById(R.id.btn_saveLBL);
        btnSaveLBL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    QAddTaskParams mParams = new QAddTaskParams("SaveLBL", "", 0);
                    new LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLongTask("ErrorWifi","Connection error");
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

    private class LongOperation extends AsyncTask<QAddTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "";
        int mPos = 0;

        public LongOperation(boolean showCommit) {
            super();
            // do stuff

        }

        @Override
        protected String doInBackground(QAddTaskParams... params) {




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

            }else if(loadMStrOne.equals("Save")){
                // result = mEditItem(ActType, mPos);
               // result = mSaveItem(loadMStrTwo);
                result = mQucikConn("",0);

            }else if(loadMStrOne.equals("SaveLBL")){
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
                    getItemCode();

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");
                }

            }else if(loadMStrOne.equals("Save")){
                if (result.equals("Success")){
                   // finishLongTask("ClearAll","Clear All");

                    mSaveItem("Save");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLongTask("Error","Connection error");

                }
            }else if(loadMStrOne.equals("SaveLBL")){

                if (result.equals("Success")){
                    // finishLongTask("ClearAll","Clear All");
                    mSaveItem("SaveLBL");

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


    private String getItemCode(){
     //   itemLookupQD
        String lookUpID = itemLookupQD.trim();

        String ckExp = "SqlExp";

        stmt = mConnection();

        if (stmt != null){

            if (getCount("idItem","Select id from itemProduct") == 0){
                itemID.setText("1");
            }else{

                itemID.setText(getCount("maxItem","Select MAX(id) As id from itemProduct").toString());

            }



            if (!lookUpID.equals("")){

                if (getCount("barcode","select ItemLookup, barCode from itemproduct where (barCode = '" + lookUpID + "' or ItemLookup = '" + lookUpID + "')") == 0){
                   // ckExp = "SucGenBar";
                    itemLookup.setText(lookUpID);
                    itemBarcode.setText(lookUpID);
                    generateBarcode(lookUpID);
                    itemPrice.requestFocus();
                    showSoftKeyBoard(itemPrice);
                }

            }else{
                //ckExp = "SucITF";
                itemLookup.requestFocus();
                showSoftKeyBoard(itemLookup);
            }

            ckExp = "Success";

        }else{

            ckExp = "SqlExp";
        }






        return ckExp;




    }

    private void generateBarcode(String content){

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int widthS = size.x;
        int heightS = size.y / 3;

        // Toast.makeText(getActivity(),"You clicked yes button Width : " + widthS + " Height : " + heightS ,Toast.LENGTH_LONG).show();
        try {
            /*
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap("content", BarcodeFormat.QR_CODE, 400, 400);
            ImageView imageViewQrCode = (ImageView) findViewById(R.id.qrCode);
            imageViewQrCode.setImageBitmap(bitmap);
            */

            if (content.trim().length() < 1){
                imageViewQrCode.setImageResource(android.R.color.transparent);
            }else{
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.CODE_128, widthS, widthS / 4);
                imageViewQrCode.setImageBitmap(bitmap);
            }






        } catch(Exception e) {

        }
    }

    private void setSerItemDataAdapter(String type) {
        Dictionary dict = new Hashtable();

        dict = selectArray("itemProduct","select id, ItemLookup, description, barCode, price, avaiable from itemproduct where barCode = '"+itemLookup.getText().toString().trim()+"' OR ItemLookup = '"+itemLookup.getText().toString().trim()+"'");
        Integer resCount = Integer.parseInt(String.valueOf(dict.get("count")));

        if (resCount > 0){
            itemDescription.setText(dict.get("descr").toString());
          //  itemStock.setText(dict.get("stock").toString());
            itemPrice.setText(dict.get("price").toString());
           // itemInPrice.getText().clear();
           // itemInPrice.requestFocus();
            //showSoftKeyBoard(itemInPrice);
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
                            mClear();

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
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
            if (type.equals("depart")) {
                spinnerArray.add("Select");
                departsDict.clear();
                Dictionary dictTempSel = new Hashtable();
                dictTempSel.put("departID", "");
                dictTempSel.put("departName", "Select");
                dictTempSel.put("departVat", "0%");
                departsDict.add(dictTempSel);
            }


            while (rs.next()) {

                count = count + 1;
                Dictionary dictTemp = new Hashtable();

                if (type.equals("depart")) {
                    spinnerArray.add(rs.getString("description"));


                    dictTemp.put("departID", rs.getString("depart_id"));
                    dictTemp.put("departName", rs.getString("description"));
                    if (rs.getString("vat").toString().equals("")){
                        dictTemp.put("departVat", "0%");
                    }else{
                        dictTemp.put("departVat", rs.getString("vat"));
                    }

                    departsDict.add(dictTemp);



                }else if (type.equals("vat")) {
                    spinnerArray.add(rs.getString("description"));
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
            if (spinnerArray.contains("UNKNOWN")){
                departsDict.remove(0);
                spinnerArray.remove(0);
                checkDUN = true;
            }else{
                checkDUN = false;
            }
        }else{

        }


        return spinnerArray;

    }

    private Dictionary selectArray(String type, String query) {

        Dictionary dict = new Hashtable();

        Integer count = 0;
        String id = "",itemid = "", itemLU = "", descr = "", barCode = "", price = "", stock = "", in_qty = "", adj_type = "";

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



                Log.d("myTag", "This is my message rss dfdf " + rs.getString("ItemLookup") + itemLookup.getText());


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

    private Boolean getCheck(String type, String query){
        Dictionary dict = new Hashtable();

        Integer count = 0;
        Boolean ageCheck = false;


        stmt = mConnection();

        if (stmt != null) try {


            ResultSet rs = stmt.executeQuery(query);

            //SampleArrayList.clear();
            dict.isEmpty();
            while (rs.next()) {

                count = count + 1;
                ageCheck = rs.getBoolean("ageCheck");
                /*if (type.equals("maxItem")){
                    //ageCheck = Integer.valueOf(rs.getString("id"));
                    ageCheck = rs.getBoolean("ageCheck");
                    Toast.makeText(getActivity(), "ageCheck " + ageCheck, Toast.LENGTH_SHORT).show();
                    // rs.getInt("id");
                }*/

            }


        } catch (SQLException e) {

            Log.d("myTag", "This is my message rss hhhh" + e.getMessage());
            e.printStackTrace();

        }
        else{

            Log.d("myTag", "Stmt in empty");

        }



        return  ageCheck;
    }

    private void mSaveItem(final String type) {

        /*If Trim(txtPrice.Text) <> "" Then
                Itmprice = Trim(txtPrice.Text)
        Else
                Itmprice = 0
        End If*/
        if(itemPrice.getText().toString().trim().length() < 1) {
            Itmprice = 0;
        }else{
            Itmprice = Double.parseDouble(itemPrice.getText().toString());
        }

        itemCost = 0;
        itmMrgProf = "0";
        Rprice = 0;
        OnHand = 0;
        Rcost = 0;
        RMargin = "0";
        itmPriceA = 0;
        itmPriceB = 0;
        itmPriceC = 0;
        itmMSRP = 0;
        itmLowBound = 0;
        itmUpBound = 0;
        itmrepcost = 0;
        itmBuydown = 0;

        if (itemLookup.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter Item lookup");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemLookup.getText().clear();
                            itemLookup.requestFocus();
                            showSoftKeyBoard(itemLookup);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(departSpinner.getSelectedItem().toString().equals("Select")){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please select department");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            //itemLookup.getText().clear();
                            departSpinner.requestFocus();
                           // showSoftKeyBoard(itemLookup);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else if(itemDescription.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter description");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemDescription.getText().clear();
                            itemDescription.requestFocus();
                            showSoftKeyBoard(itemDescription);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else if(itemPrice.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter price");
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
        }else if(itemBarcode.getText().toString().trim().length() < 1) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please enter barcode");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            itemBarcode.getText().clear();
                            itemBarcode.requestFocus();
                            showSoftKeyBoard(itemBarcode);
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else{
            generateBarcode(itemBarcode.getText().toString());

            if (type.equals("SaveLBL")){
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage("Do you want to Save And Add Label?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                insertQuickItem(type);
                            }
                        });
                alertDialogBuilder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });


                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }else{

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage("Do you want to Save?");
                alertDialogBuilder.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                insertQuickItem(type);
                            }
                        });
                alertDialogBuilder.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });


                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }


        }



    }

    private void insertQuickItem(String type)







    {

        showLoadingActionButton();

        System.out.println("I am in!");

        //Public dtendtim As DateTime = TimeValue("00:00")
        String ageCkVal, logDescription, logdat, dtendtim, dttim, expsdate, txtdepart, txtcate;
        dtendtim = "00:00:00";
        dttim = "00:00:00";

        logdat = DateFormat.getDateTimeInstance().format(new Date());

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        expsdate = formatter.format(date);

        ageCkVal = "0";
        txtdepart = "";
        txtcate = "";


        Integer mPos = adapterDepart.getPosition(departSpinner.getSelectedItem().toString());


        Dictionary dictArr = departsDict.get(mPos);
        txtdepart = dictArr.get("departID").toString();

        if (getCheck("ageCK","select ageCheck from department where depart_id = '" + txtdepart + "'")){
            //Toast.makeText(getActivity(), "I am true " + txtdepart, Toast.LENGTH_SHORT).show();
            ageCkVal = "1";
        }else{
            //Toast.makeText(getActivity(), "I am false" + txtdepart, Toast.LENGTH_SHORT).show();
            ageCkVal = "0";
        }

        if(itemPrice.getText().toString().trim().length() < 1) {
            Itmprice = 0;
        }else{
            Itmprice = Double.parseDouble(itemPrice.getText().toString());
        }

        Rprice = Itmprice;

        salesstDate = "1900-01-01";
        salesendDate = "1900-01-01";
        salesShedules = "1900-01-01";

        if (getCount("ItemProduct","select * from itemProduct where ((ItemLookup = '" + itemLookup.getText().toString().trim() + "' or ItemLookup = '" + itemBarcode.getText().toString().trim() + "') and ItemLookup <> '') or ((barCode = '" + itemLookup.getText().toString().trim() + "' or barCode = '" + itemBarcode.getText().toString().trim() + "') and barCode <> '')") == 0){
           // itemID.setText("1");

            // Old String strquery = "Insert into itemProduct(ItemLookup,description,descriptionExtend,productBrand,modelNo,sizeGroup,barCode,itemType,department,category,vat,price,cost,profitMargin,oneHand,commited,avaiable,offline,dateCreate,lastRecevied,lastorder,lastSold,lastCost,replaceCost,restockLevel,reoderPoint,Rprice,Rcost,Rmargin,price_A,price_B,price_C,MaxSellRetailPrice,lowerBound,upperBound,Itemsales,salesPrice,startDate,endDate,shedule,buydown,buydownQuantity,discount,discountscheme,dimage,refund,ageCheck,ageCheckdt,ageCheckdtend,handlCharge,notes,uploadfile,wtScale,inPrice,damageSales,damagePrice,expireCk,expireDate)values('" + itemLookup.getText().toString().trim() + "','" + itemDescription.getText().toString().trim() + "','" + itemDescription.getText().toString().trim() + "','','','','" + itemBarcode.getText().toString().trim() + "','','" + txtdepart + "','" + txtcate + "','" + vatSpinner.getSelectedItem().toString() + "','" + Itmprice + "','" + itemCost + "','" + itmMrgProf + "','0','0','0','0','" + logdat + "','','','','0','" + itmrepcost + "','0','0','" + Rprice + "','" + Rcost + "','" + RMargin + "','" + itmPriceA + "','" + itmPriceB + "','" + itmPriceC + "','" + itmMSRP + "','" + itmLowBound + "','" + itmUpBound + "','0','" + salesprice + "','" + salesstDate + "','" + salesendDate + "','" + salesShedules + "','" + itmBuydown + "','0','','0','', '0', '" + ageCkVal + "', '" + dttim + "', '" + dtendtim + "', '', '',  '', '0', '0','0','0', '0','" + expsdate + "')";
            String strquery = "Insert into itemProduct(ItemLookup,description,descriptionExtend,productBrand,modelNo,sizeGroup,barCode,itemType,department,category,vat,price,cost,profitMargin,oneHand,commited,avaiable,offline,dateCreate,lastRecevied,lastorder,lastSold,lastCost,replaceCost,restockLevel,reoderPoint,Rprice,Rcost,Rmargin,price_A,price_B,price_C,MaxSellRetailPrice,lowerBound,upperBound,Itemsales,salesPrice,startDate,endDate,shedule,buydown,buydownQuantity,discount,discountscheme,dimage,refund,ageCheck,ageCheckdt,ageCheckdtend,handlCharge,notes,uploadfile,wtScale,inPrice,damageSales,damagePrice,expireCk,expireDate,stockCk,pckQty,pckPrice,pckSize)values('" + itemLookup.getText().toString().trim() + "','" + itemDescription.getText().toString().trim() + "','" + itemDescription.getText().toString().trim() + "','','','','" + itemBarcode.getText().toString().trim() + "','','" + txtdepart + "','" + txtcate + "','" + vatSpinner.getSelectedItem().toString() + "','" + Itmprice + "','" + itemCost + "','" + itmMrgProf + "','0','0','0','0','" + logdat + "','1900-01-01 00:00:00.000','1900-01-01 00:00:00.000','1900-01-01 00:00:00.000','0','" + itmrepcost + "','0','0','" + Rprice + "','" + Rcost + "','" + RMargin + "','" + itmPriceA + "','" + itmPriceB + "','" + itmPriceC + "','" + itmMSRP + "','" + itmLowBound + "','" + itmUpBound + "','0','" + salesprice + "','" + salesstDate + "','" + salesendDate + "','" + salesShedules + "','" + itmBuydown + "','0','','0','', '0', '" + ageCkVal + "', '" + dttim + "', '" + dtendtim + "', '', '',  '', '0', '0','0','0', '0','" + expsdate + "', '0', '0', '0', '')";

            // String strquery = "Insert into itemProduct(ItemLookup,description,descriptionExtend,productBrand,modelNo,sizeGroup,barCode,itemType,department,category,vat,price,cost,profitMargin,oneHand,commited,avaiable,offline,dateCreate,lastRecevied,lastorder,lastSold,lastCost,replaceCost,restockLevel,reoderPoint,Rprice,Rcost,Rmargin,price_A,price_B,price_C,MaxSellRetailPrice,lowerBound,upperBound,Itemsales,salesPrice,startDate,endDate,shedule,buydown,buydownQuantity,discount,discountscheme,dimage,refund,ageCheck,ageCheckdt,ageCheckdtend,handlCharge,notes,uploadfile,wtScale,inPrice,damageSales,damagePrice,expireCk,expireDate)values('" + itemLookup.getText().toString().trim() + "','" & Trim(txtDescript.Text) & "','" & txtDescript.Text & "','','','','" & txtBarCode.Text & "','','" & txtdepart & "','" & txtcate & "','" & vatList.SelectedItem & "','" & Itmprice & "','" & itemCost & "','" & itmMrgProf & "','0','0','0','0','" & logdat & "','','','','0','" & itmrepcost & "','0','0','" & Rprice & "','" & Rcost & "','" & RMargin & "','" & itmPriceA & "','" & itmPriceB & "','" & itmPriceC & "','" & itmMSRP & "','" & itmLowBound & "','" & itmUpBound & "','0','" & salesprice & "','" & salesstDate & "','" & salesendDate & "','" & salesShedules & "','" & itmBuydown & "','0','','0','', '0', '" & ageCkVal & "', '" & dttim & "', '" & dtendtim & "', '', '',  '', '0', '0','0','0', '0','" & expsdate & "')"
            stmt = mConnection();
            if (stmt != null) {
                try {

                    int result = stmt.executeUpdate(strquery);

                    if(result == 1) {

                        if (tillType.equals("Server")){
                            Iterator<String> newStringsIter = fetch.iterator();
                            String ipt;
                            while (newStringsIter.hasNext()) {
                               // System.out.println("The sting value : " + newStringsIter.next());

                                //Order order = (Order)newStringsIter.next();

                                ipt = newStringsIter.next();
                                int resultClient = stmt.executeUpdate("insert into clientUpdate(tableName, itemlookup, upType, ipaddress) VALUES ('itemProductBO', '" + itemLookup.getText().toString().trim() + "', 'insert', '" + ipt + "')");

                                if(resultClient == 1) {

                                }

                            }
                        }else if(tillType.equals("Client")){


                            int resultServer = stmt.executeUpdate("insert into update_server(tableName, itemlookup, upType, ipaddress) VALUES ('itemProductBO', '" + itemLookup.getText().toString().trim() + "', 'insert', '" + serIPAdd + "')");

                            if(resultServer == 1) {

                            }


                        }

                        if (type.equals("SaveLBL")){
                            int resultSaveLBL = stmt.executeUpdate("INSERT INTO LabelPrint(itemlookupCode, decription, barcode, department, price) VALUES ('" + itemLookup.getText().toString().trim() + "','" + itemDescription.getText().toString().trim() + "','" + itemBarcode.getText().toString().trim() + "','" + vatSpinner.getSelectedItem().toString() + "','" + Double.parseDouble(itemPrice.getText().toString()) + "')");

                            if(resultSaveLBL == 1) {

                                hideLoadingActionButton();

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setMessage("Item And Print Label added successfully");
                                alertDialogBuilder.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                mfinishAdd("Success");
                                            }
                                        });


                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }else{

                                hideLoadingActionButton();

                                //Error in adding Label
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                alertDialogBuilder.setMessage("Error in adding Label");
                                alertDialogBuilder.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface arg0, int arg1) {
                                                mfinishAdd("Success");
                                            }
                                        });


                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }else{

                            hideLoadingActionButton();

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                            alertDialogBuilder.setMessage("Saved item sucessfully");
                            alertDialogBuilder.setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            mfinishAdd("Success");
                                        }
                                    });


                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }
                        String depDescr = "", depGroups = "", depAgeCheck= "", depCommission = "", depVat = "", catDescr = "", catVat = "";

                        ResultSet rsd = stmt.executeQuery("select description, groups, ageCheck, commission, vat from department where depart_id = '"+txtdepart+"'");

                        while (rsd.next()) {

                            depDescr = rsd.getString("description");
                            depGroups = rsd.getString("groups");
                            depAgeCheck = rsd.getString("ageCheck");
                            depCommission = rsd.getString("commission");
                            depVat = rsd.getString("vat");

                        }

                        System.out.println("Department details : "  + depDescr + depGroups + depAgeCheck + "comi : "+depCommission+" : " + depVat);

                        ItemUploadTB itemUploadTB;
                        itemUploadTB = new ItemUploadTB(1,"insert",itemLookup.getText().toString().trim(),itemDescription.getText().toString().trim(),itemBarcode.getText().toString().trim(),vatSpinner.getSelectedItem().toString(),Double.toString(Itmprice),Double.toString(itemCost),depDescr.trim(),depGroups.trim(),depAgeCheck.trim(),depCommission.trim(),depVat.trim(),catDescr.trim(),catVat.trim());
                        long trest =  db.insertItemUploadTB(itemUploadTB);



                    }else{

                        hideLoadingActionButton();

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage("Error in saving item");
                        alertDialogBuilder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {

                                    }
                                });


                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }



                } catch (SQLException e) {

                    Log.d("Error Type", "Exception : " + e.getMessage());
                    e.printStackTrace();

                    hideLoadingActionButton();

                }


            }else{
                hideLoadingActionButton();
            }



        }else{

            hideLoadingActionButton();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Item Lookup code or barcode is already exist");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mfinishAdd("Exist");
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }


    }

    private void mfinishAdd(String type){
        if (type.equals("Exist")){
            itemLookup.getText().clear();
            /*itemDescription.getText().clear();
            itemPrice.getText().clear();*/
            itemBarcode.getText().clear();
            imageViewQrCode.setImageResource(android.R.color.transparent);
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
                itemBarcode.getText().clear();
                imageViewQrCode.setImageResource(android.R.color.transparent);
                itemLookup.requestFocus();
                showSoftKeyBoard(itemLookup);
            }



        }


    }
    private void exitQA(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("NavValue", "QuickAddExit");
        resultIntent.putExtra("itemLUValue", itemLookup.getText().toString());
        getActivity().setResult(Activity.RESULT_OK, resultIntent);
        getActivity().finish();
    }

    private void mClear(){
        //itemSearch.getText().clear();
        itemLookup.getText().clear();
        itemDescription.getText().clear();
        itemPrice.getText().clear();
        itemBarcode.getText().clear();
        imageViewQrCode.setImageResource(android.R.color.transparent);
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

                QAddTaskParams mParams = new QAddTaskParams("GetItem", "", 0);
                new LongOperation(false).execute(mParams);

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
