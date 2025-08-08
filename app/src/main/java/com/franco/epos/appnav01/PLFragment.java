package com.franco.epos.appnav01;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.PListTB;
import com.franco.epos.appnav01.database.model.SettingsTB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PLFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String PageName;
    private String ActType;
    private String FromActTypeVal;

    private HomeFragment.OnFragmentInteractionListener mListener;

    private ItemPLDataAdapter mAdapterPO;
    SwipeController swipeController = null;

    RecyclerView recyclerView;


    private Button btnCommit;
    private Button btnAdd;
    private Button btnClearAll;


    private String ipaddress, port;
    DatabaseHelper db;

    private Connection connection = null;
    private String ConnectionURL = null;
    Statement stmt = null;
    private SharedPreferences pref;
    private String staffID = "";
    private String staffName = "";
    private String LogType = "FO";
    private String tillType = "";
    private String serIPAdd = "";
    Set<String> fetch;


    private List<Dictionary> itemsDict;

    final Context context = getContext();

    Handler mHandler;
    Runnable mRunnable;

    private RelativeLayout loginLoader, norecordPanel;
    private TextView msgAltText;
    private Button retryBtn;

    String typeVal = "";

    /*public static final int PERMISSION_BLUETOOTH = 1;

    private final Locale locale = new Locale("id", "ID");
    private final DateFormat df = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", locale);
    private final NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
    BluetoothConnection BTconnection;*/

    public PLFragment() {
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
    public static PLFragment newInstance(String param1, String param2) {
        PLFragment fragment = new PLFragment();
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

        //Log.d("myTag", "I am view method" + savedInstanceState );

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;



        if (width <= 480){
            swipeController.buttonWidth = 100;
        }


        System.out.println("---------key_value."+inflater +"  The width : "+ width);
        readBundle(getArguments());
        System.out.println("Page: " + PageName);
        System.out.println("Types: " + ActType);

        if (ActType.equals("Open PO")){
            FromActTypeVal = "OPO List";
        }else{
            FromActTypeVal = "CPO List";

        }
        //   mNameTextView.setText(String.format("Name: %s", name));
        //   mAgeTextView.setText(String.format("Age: %d", age));



        View view = inflater.inflate(R.layout.fragment_pl,   container, false);


        btnCommit  = (Button) view.findViewById(R.id.btn_commit);
        btnCommit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());
                //builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle("Select");

                final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
                arrayAdapter.add("Bluetooth Printer");
               // arrayAdapter.add("Network Printer");
                arrayAdapter.add("Share");



                builderSingle.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);
                        typeVal = "";
                        if(strName.equals("Bluetooth Printer")) {
                            typeVal = "BTPrinter";

                            showLoadingActionButton();
                            PLFragment.HomeTaskParams mParams = new PLFragment.HomeTaskParams("BTPrint", "", 0);
                            new PLFragment.LongOperation(false).execute(mParams);
                        /*}else if (strName.equals("Network Printer")){
                            typeVal = "NWPrinter";*/
                        }else if (strName.equals("Share")){
                            typeVal = "Share";
                            ((MainActivity) getActivity()).mSendPLMailed();
                        }else{

                        }

                        // ((MainActivity) getActivity()).loadPOEditFragment(typeVal,"", POCode, POStatus);


                    }
                });
                builderSingle.show();


            }

        });

        btnAdd  = (Button) view.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                ((MainActivity) getActivity()).loadPLFragment("");







            }

        });

        btnClearAll  = (Button) view.findViewById(R.id.btn_clearAll);
        btnClearAll.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("Confirm");
                builder.setMessage("Are you sure want to clear all?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing but close the dialog
                        /*List<PListTB> itemsP = db.getAllPListTB();
                        if (itemsP.size() > 0){

                        }else{

                        }*/
                        db.emptyPListTB();
                        showLoadingActionButton();
                        String ckConn = mConnectionCheck("");
                        if (ckConn.equals("WifiCon")){

                            PLFragment.HomeTaskParams mParams = new PLFragment.HomeTaskParams("Load", "", 0);
                            new PLFragment.LongOperation(false).execute(mParams);

                        }else{

                            hideLoadingActionButton();
                            finishLoad("ErrorWifi","Connection error");
                        }
                        dialog.dismiss();

                    }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();


            }

        });


        final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();

        if (fab != null) {
            //((MainActivity) getActivity()).showFloatingActionButton();
            ((MainActivity) getActivity()).hideFloatingActionButton();
        }

        db = new DatabaseHelper(getContext());
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        itemsDict = new ArrayList<>();
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        staffID = pref.getString("staffID",null);
        staffName = pref.getString("staffName",null);
        tillType = pref.getString("tillType",null);
        fetch = pref.getStringSet("CIPAddArray", null);
        serIPAdd = pref.getString("SIPAdd",null);

        SettingsTB setTb = db.getSetTBByType("SERVER");
        ipaddress = setTb.getIpaddress();
        port = setTb.getPort();






        loginLoader = (RelativeLayout) view.findViewById(R.id.loadingPanel);
        norecordPanel = (RelativeLayout) view.findViewById(R.id.noRecordPanel);
        msgAltText = (TextView) view.findViewById(R.id.txt_msg_alt);
        retryBtn = (Button) view.findViewById(R.id.btn_retry);

        loginLoader.setVisibility(View.GONE);
        norecordPanel.setVisibility(View.GONE);

        retryBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    PLFragment.HomeTaskParams mParams = new PLFragment.HomeTaskParams("Load", "", 0);
                    new PLFragment.LongOperation(false).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLoad("ErrorWifi","Connection error");
                }

            }

        });



        List<Item> items = new ArrayList<>();
        mAdapterPO = new ItemPLDataAdapter(items);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapterPO);
        // setupRecyclerView();

        btnCommit.setVisibility(View.VISIBLE);
        btnClearAll.setVisibility(View.VISIBLE);


        return view;
    }

    public void onStart() {
        super.onStart();


        showLoadingActionButton();
        String ckConn = mConnectionCheck("");
        if (ckConn.equals("WifiCon")){

            PLFragment.HomeTaskParams mParams = new PLFragment.HomeTaskParams("Load", "", 0);
            new PLFragment.LongOperation(false).execute(mParams);

        }else{

            hideLoadingActionButton();
            finishLoad("ErrorWifi","Connection error");
        }


    }
    private static class HomeTaskParams {
        String hParaOne;
        String hParaTwo;
        int recPos;


        HomeTaskParams(String hParaOne, String hParaTwo, int recPos) {
            this.hParaOne = hParaOne;
            this.hParaTwo = hParaTwo;
            this.recPos = recPos;

        }
    }

    private class LongOperation extends AsyncTask<PLFragment.HomeTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "";
        int mPos = 0;

        public LongOperation(boolean showCommit) {
            super();
            // do stuff
            if (showCommit){
                btnCommit.setVisibility(View.VISIBLE);
                btnClearAll.setVisibility(View.VISIBLE);
            }else{
                btnCommit.setVisibility(View.GONE);
                btnClearAll.setVisibility(View.GONE);
            }

        }

        @Override
        protected String doInBackground(PLFragment.HomeTaskParams... params) {




            loadMStrOne  = params[0].hParaOne;
            mPos = params[0].recPos;



            //String mVal = params[0];

            System.out.println("The value in doInBackground : " + loadMStrOne);

            String result = "0";

            if (loadMStrOne.equals("Load")) {
                result = setSerItemDataAdapter(ActType);
            }else if(loadMStrOne.equals("Delete")) {
                result = mDeleteItem(ActType, mPos);

            }else if(loadMStrOne.equals("EDIT")){
                result = mEditItem(ActType, mPos);
            }else if(loadMStrOne.equals("Commit")) {

                result = mCommitItems(ActType);
                /*if (mCommitItems(ActType)){
                    ((MainActivity) getActivity()).loadCommitFragment(ActType);
                }else{

                }*/
            }else if(loadMStrOne.equals("BTPrint")){
                result = ((MainActivity) getActivity()).mBTThermalPrinter();
            }else{
                result = "0";
            }



            return result;
        }

        @Override
        protected void onPostExecute(String result) {


            hideLoadingActionButton();
            System.out.println("The value in onPostExecute : " + loadMStrOne);

            if (loadMStrOne.equals("Load")){

                if (result.equals("Success")) {
                    finishLoad("Success","Success");

                }else if(result.equals("NoRecord")) {
                    finishLoad("NoRecord","NoRecord");

                }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){

                    finishLoad("Error","Connection error");
                }

                setupRecyclerView();


            }else if(loadMStrOne.equals("Delete")) {

                if (result.equals("Success")) {

                    Toast.makeText(getActivity(), "Deleted sucessfully", Toast.LENGTH_LONG).show();
                    mAdapterPO.items.remove(mPos);
                    mAdapterPO.notifyItemRemoved(mPos);
                    mAdapterPO.notifyItemRangeChanged(mPos, mAdapterPO.getItemCount());


                    if (mAdapterPO.getItemCount() > 0) {
                        btnCommit.setVisibility(View.VISIBLE);
                        btnClearAll.setVisibility(View.VISIBLE);
                    } else {
                        //finishDelete("NoRecord","NoRecord");

                        finishLoad("NoRecord", "NoRecord");
                    }

                } else if (result.equals("NoRecord")) {

                    finishDelete("NoRecordDel", "NoRecordDel");

                } else if (result.equals("SqlExp") || result.equals("Error")) {

                    finishDelete("Error", "Connection error");

                }

            }else if(loadMStrOne.equals("EDIT")){
                if (result.equals("Success")){

                    Item item = new Item();
                    item = mAdapterPO.items.get(mPos);
                    ((MainActivity) getActivity()).loadPLFragment(item.getItemLU());

                    /*Item item = new Item();
                    item = mAdapterPO.items.get(mPos);
                    ((MainActivity) getActivity()).loadEditFragment(item.getItemID());*//*
                    Item item = new Item();
                    item = mAdapterPO.items.get(mPos);

                    db.emptyOPListTB();
                    Bundle bundle = new Bundle();
                    Fragment fragment = null;
                    btnCommit.setVisibility(View.VISIBLE);
                    bundle.putString("PageName", "Main");
                    bundle.putString("ActType", FromActTypeVal);
                    bundle.putString("AActType", "Edit");
                    bundle.putString("POCode", item.getItemCode());
                    bundle.putString("POStatus", item.getStatus());

                    ((MainActivity) getActivity()).CURRENT_AVAL = FromActTypeVal;

                    FragmentTransaction fragmentTransaction = ((MainActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    fragmentTransaction.replace(R.id.frame, fragment, FromActTypeVal);
                    fragment.setArguments(bundle);
                   // fragmentTransaction.addToBackStack(null);
                   // fragmentTransaction.commitAllowingStateLoss();
                    fragmentTransaction.commit();*/


                }else{
                    finishDelete("SqlExp", "Connection error");
                }


            }else if(loadMStrOne.equals("Commit")) {

                System.out.println("The commitement value : " + result);

                if (result.equals("Success")){

                    ((MainActivity) getActivity()).loadCommitFragment(ActType);

                }else if(result.equals("SqlExp") || result.equals("Error")) {

                    finishDelete("SqlExp", "Connection error");

                }else if(result.equals("ErrorAdj") || result.equals("ErrorSav")) {
                    finishDelete("ErrorCommit", "Connection error commit");
                }
            }else if (loadMStrOne.equals("BTPrint")){
                if (result.equals("Success")) {

                    finishLongTask("Success","Success");
                    // mQuickAdd("Yes");

                } else if (result.equals("NoPrExp")) {
                    finishLongTask("NoPrExp", "No Printer connected");
                } else if (result.equals("ckPrExp")) {
                    finishLongTask("ckPrExp", "Check Printer connection");
                } else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")) {

                    // mQuickAdd("No");
                    finishLongTask("Error", "Connection error");
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

    private void finishDelete (String action,String val){

        if (action.equals("Success")){
        }else if(action.equals("Error") || action.equals("SqlExp")) {
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
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
        }else if(action.equals("NoRecordDel")) {

            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);

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

            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
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

    private void finishLoad (String action,String val){

        if (action.equals("Success")) {

            norecordPanel.setVisibility(View.GONE);
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);

        }else if(action.equals("NoRecord")){

            btnCommit.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("No record found");
            retryBtn.setText("Refresh");

        }else if(action.equals("ErrorWifi")){


            btnCommit.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("Wifi Connection error");
            retryBtn.setText("Retry");

        }else if(action.equals("Error")){

            btnCommit.setVisibility(View.GONE);
            btnClearAll.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("Connection error");
            retryBtn.setText("Retry");


        }else{

            hideLoadingActionButton();

        }


    }

    private void finishLongTask (String action,String val){

        if (action.equals("Success")) {
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);




        }else if(action.equals("ClearAll")) {
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);



        }else if(action.equals("Error") || action.equals("SqlExp")) {
            // btnCommit.setVisibility(View.VISIBLE);
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
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
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);

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


        } else if (action.equals("NoPrExp")) {
            //finishLongTask("NoPrExp", "No Printer connected");
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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
            btnCommit.setVisibility(View.VISIBLE);
            btnClearAll.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
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



    private String setSerItemDataAdapter(String type) {

        Integer count = 0;
        String ckExp = "";
        List<Item> items = new ArrayList<>();

        List<PListTB> itemsP = db.getAllPListTB();
        if (itemsP.size() > 0){
            for (int j=0; j<itemsP.size();j++){
                Log.d("myTag", "This is my message rss" + itemsP.get(j).getItem_lookup());

                Item item = new Item();
                //item.setItemID(rs.getString("id"));
                item.setItemLU(itemsP.get(j).getItem_lookup());
                item.setDescr(itemsP.get(j).getItem_descr());
                item.setItemCode(itemsP.get(j).getBar_code());
                item.setInQty(itemsP.get(j).getQty());
                item.setPrice(itemsP.get(j).getPrice());
                items.add(item);

            }
            ckExp = "Success";
        }else{
            ckExp = "NoRecord";
        }


        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
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

                    ResultSet rs = stmt.executeQuery("select id, PoCode, descp, datetim, status From PoCode ORDER BY datetim DESC");
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

                        Log.d("myTag ", "This is my message rss" + rs.getString("descp") );

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
        }*/
        Log.d("myTag", "This is my message rss" + count );

        mAdapterPO = new ItemPLDataAdapter(items);
        return ckExp;

    }

    private void setupRecyclerView() {


        // RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapterPO);



        swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    // new LongOperation().execute("Delete");

                    PLFragment.HomeTaskParams mParams = new PLFragment.HomeTaskParams("Delete", "", position);
                    new PLFragment.LongOperation(true).execute(mParams);



                    /*if (mDeleteItem(ActType,position)){

                        Toast.makeText(getActivity(),"Deleted sucessfully",Toast.LENGTH_LONG).show();
                        mAdapterPO.items.remove(position);
                        mAdapterPO.notifyItemRemoved(position);
                        mAdapterPO.notifyItemRangeChanged(position, mAdapterPO.getItemCount());

                    }else{


                    }*/


                }else{

                    hideLoadingActionButton();

                    finishDelete("ErrorWifi","Connection error");

                    //finishLoad("ErrorWifi","Connection error");
                }





            }

            @Override
            public void onLeftClicked(int position) {
                super.onLeftClicked(position);
                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    //mEditItem(ActType,position);
                    PLFragment.HomeTaskParams mParams = new PLFragment.HomeTaskParams("EDIT", "", position);
                    new PLFragment.LongOperation(true).execute(mParams);
                }else{

                    hideLoadingActionButton();
                    finishDelete("ErrorWifi","Connection error");

                }

            }

        });




        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Handle item click

                        showLoadingActionButton();
                        String ckConn = mConnectionCheck("");
                        if (ckConn.equals("WifiCon")){

                            // mEditItem(ActType,position);
                            PLFragment.HomeTaskParams mParams = new PLFragment.HomeTaskParams("EDIT", "", position);
                            new PLFragment.LongOperation(true).execute(mParams);

                        }else{

                            hideLoadingActionButton();
                            finishDelete("ErrorWifi","Connection error");

                        }
                        // Toast.makeText(getActivity(),"You clicked yes button" + position,Toast.LENGTH_LONG).show();
                    }
                })
        );




    }




    private String mCommitItems(String type){
        String ckExp = "";

        String logdat = DateFormat.getDateTimeInstance().format(new Date());

        ckExp = selectDictArray(type, "select I.id, O.id As idd, O.ItemLookup, O.description, O.barCode, O.price, O.avaiable, I.in_qty, I.price_changed, I.reason, I.adj_type, I.staff_id, I.dateTime From itemproduct As O INNER Join tempProAdjustment As I On O.id = I.itemID Where I.adj_type = '" + type + "' ORDER BY I.dateTime DESC");

        System.out.println("The count fo tdicto : " + itemsDict);

        for(Dictionary dictArr: itemsDict){




            double aDouble = Double.parseDouble(dictArr.get("price").toString());
            double aDouble1 = Double.parseDouble(dictArr.get("price").toString());
            double aDouble2 = (aDouble + aDouble1+ aDouble1);


            String tStr = dictArr.get("adj_dateTime").toString();
            double trackPrice, Rprice;
            Integer TrackQty, OnHand, InQty;
            String logDescription;



            //System.out.println("The count fo tdicto : " + TrackQty);





            stmt = mConnection();
            if (stmt != null) {
                try {
                    int result = stmt.executeUpdate("insert into ProductAdjustment(adj_type,itemID,staff_id,dateTime,adj_staff_id,adj_dateTime,in_qty,cur_stock,cur_price,price_changed,reason)values('" + dictArr.get("adj_type") + "','" + dictArr.get("itemId") + "','" + staffID + "','" + logdat + "','" + dictArr.get("adj_staff_id") + "','" + dictArr.get("adj_dateTime") + "', '" + dictArr.get("in_qty") + "', '" + dictArr.get("stock") + "', '" + dictArr.get("price") + "', '" + dictArr.get("price_changed") + "', '" + dictArr.get("reason") + "')");

                    if(result == 1) {

                        trackPrice = Double.parseDouble(dictArr.get("price").toString());
                        Rprice = Double.parseDouble(dictArr.get("price_changed").toString());

                        TrackQty = Integer.valueOf(dictArr.get("stock").toString());
                        InQty = Integer.valueOf(dictArr.get("in_qty").toString());

                        if (type.equals("Delivery")) {
                            OnHand = (TrackQty + InQty);
                        }else if(type.equals("Waste")){
                            OnHand = (TrackQty - InQty);
                        }else{
                            OnHand = InQty;
                        }

                        int resultProd = stmt.executeUpdate("UPDATE itemProduct set Price =	'" + Rprice + "', OneHand = '" + OnHand + "', Avaiable = '" + OnHand + "', Rprice =	'" + Rprice + "' where id = '" + dictArr.get("itemId") + "'");

                        if(resultProd == 1) {


                            if (tillType.equals("Server")){
                                Iterator<String> newStringsIter = fetch.iterator();
                                String ipt;
                                while (newStringsIter.hasNext()) {
                                    System.out.println("The sting value : " + newStringsIter.next());
                                    ipt = newStringsIter.next();
                                    int resultClient = stmt.executeUpdate("insert into clientUpdate(tableName, itemlookup, upType, ipaddress) VALUES ('itemProductBO', '" + dictArr.get("itemLU") + "', 'update', '" + ipt + "')");

                                    if(resultClient == 1) {

                                    }

                                }
                            }else if(tillType.equals("Client")){


                                int resultServer = stmt.executeUpdate("insert into update_server(tableName, itemlookup, upType, ipaddress) VALUES ('itemProductBO', '" + dictArr.get("itemLU") + "', 'update', '" + serIPAdd + "')");

                                if(resultServer == 1) {

                                }



                            }

                            System.out.println("Updated successfully" + dictArr.get("itemLU"));

                            ckExp = "Success";

                        }else{

                            System.out.println("Error in saving item " + dictArr.get("itemLU"));

                            ckExp = "ErrorSav";

                        }



                        if (Rprice != trackPrice){
                            logDescription = staffName + " has changed the price for " + dictArr.get("descr") + " (" + dictArr.get("itemLU") + ") " + " from " + trackPrice + " to " + Rprice;
                            int resultPriceN = stmt.executeUpdate("Insert into log(description, staffid, BFoffice, logType, dattim,itemlookup,price_old,price) VALUES ('" + logDescription + "','" + staffID + "', '" + LogType + "', 'Price','" + logdat + "','" + dictArr.get("itemLU") + "','" + trackPrice + "', '" + Rprice + "')");

                            if(resultPriceN == 1) {

                            }
                        }

                        if (OnHand != TrackQty){

                            logDescription = staffName + " has changed the Qty for " + dictArr.get("descr") + " (" + dictArr.get("itemLU") + ") " + " from " + TrackQty + " to " + OnHand;

                            int resultQtyN = stmt.executeUpdate("Insert into log(description, staffid, BFoffice, logType, dattim,itemlookup,qty_old,qty) VALUES ('" + logDescription + "','" + staffID + "', '" + LogType + "', 'Quantity','" + logdat + "','" + dictArr.get("itemLU") + "','" + TrackQty + "', '" + OnHand + "')");

                            if (resultQtyN == 1){

                            }

                        }

                        int resultDel = stmt.executeUpdate("DELETE from tempProAdjustment where (itemID = '" + dictArr.get("itemId") + "' AND adj_type = '" + type + "')");

                        if (resultDel == 1){

                        }


                    }else{

                        System.out.println("Error in Adjusment " + dictArr.get("itemLU"));

                        ckExp = "ErrorAdj";

                    }


                } catch (SQLException e) {

                    Log.d("Error Type", "Exception : " + e.getMessage());
                    e.printStackTrace();
                    ckExp = "SqlExp";

                }
            }else{
                ckExp = "SqlExp";
            }

            System.out.println("The itemLookup : " + dictArr.get("itemId") + " Decimal " + dictArr.get("price") + " Formated value : " + aDouble2);
        }


        return ckExp;
    }

    private String selectDictArray(String type, String query){

        String ckExp = "";

        itemsDict.clear();
        stmt = mConnection();
        Integer count = 0;



        if (stmt != null) {
            try {



                ResultSet rs = stmt.executeQuery(query);

                //SampleArrayList.clear();
                while (rs.next()){
                    count = count + 1;
                    Dictionary dictTemp = new Hashtable();

                    //mobileArray.
                        /*
                        Item item = new Item();
                        item.setItemID(rs.getString("id"));
                        item.setItemLU(rs.getString("ItemLookup"));
                        item.setDescr(rs.getString("description"));
                        item.setInQty(rs.getString("in_qty"));
                        item.setPrice(rs.getString("price_changed"));
                        */


                    dictTemp.put("count", count);
                    dictTemp.put("fnType", "new");
                    dictTemp.put("id", rs.getString("id"));
                    dictTemp.put("itemId", rs.getString("idd"));
                    dictTemp.put("itemLU", rs.getString("ItemLookup"));
                    dictTemp.put("descr", rs.getString("description"));
                    dictTemp.put("barCode", rs.getString("barCode"));
                    dictTemp.put("price", rs.getString("price"));
                    dictTemp.put("stock", rs.getString("avaiable"));
                    dictTemp.put("in_qty", rs.getString("in_qty"));
                    dictTemp.put("price_changed", rs.getString("price_changed"));
                    dictTemp.put("reason", rs.getString("reason"));
                    dictTemp.put("adj_type", rs.getString("adj_type"));
                    dictTemp.put("adj_staff_id", rs.getString("staff_id"));
                    dictTemp.put("adj_dateTime", rs.getString("dateTime"));


                    itemsDict.add(dictTemp);
                    // items.add(item);
                    //  items.add(item);

                    //Log.d("myTag ", "This is my message rss" + rs.getString("price_changed") );

                }

                ckExp = "Success";




            } catch (SQLException e) {

                Log.d("Error Type", "Exception : " + e.getMessage() );
                e.printStackTrace();
                ckExp = "SqlExp";
            }
        }else{
            ckExp = "SqlExp";
        }

        return ckExp;
    }

    private String mEditItem(String type, int postion) {
        String ckExp = "";
        stmt = mConnection();
        if (stmt != null) {
            ckExp = "Success";
        }else{
            ckExp = "SqlExp";
            /*
            Item item = new Item();
            item = mAdapterPO.items.get(postion);
            ((MainActivity) getActivity()).loadEditFragment(item.getItemID());
            */
        }


        return ckExp;


    }

    private String mDeleteItem(String type, int postion) {
        String ckExp = "";

        Item item = new Item();
        item = mAdapterPO.items.get(postion);

        if (db.getPListTBCountByITL(item.getItemLU()) > 0) {
            PListTB pListTB;
            //item.getItemLU(),item.getItemCode(),
            pListTB = new PListTB(1,item.getItemLU(),item.getItemCode(), item.getDescr(), item.getInQty(), item.getPrice());
            db.deletePListTB(pListTB);

            ckExp = "Success";
            return ckExp;

        }else{
            ckExp = "NoRecord";
        }

/*        String ckExp = "";
        stmt = mConnection();
        if (stmt != null) {

            Item item = new Item();
            item = mAdapterPO.items.get(postion);
            //"select * from tempProAdjustment where (itemID = '" & dictVal.Item("itemId") & "' AND adj_type = '" & defaultType & "')"
            //if (mCount("select * from tempProAdjustment where itemID = '"+ item.getItemID()+"'") != 0){
           // System.out.println("The value is : " + item.getItemCode());
            if (mCount("select * from PoCode where PoCode = '" + item.getItemCode() + "'") != 0){
                System.out.println("The value is : " + item.getItemCode() );

                try {
                    int result = stmt.executeUpdate("delete from PoCode where PoCode = '" + item.getItemCode() + "'");
                    if(result == 1) {


                        if (mCount("select * from purchaseOrder where PoCode = '" + item.getItemCode() + "'") != 0){

                            int resultt = stmt.executeUpdate("delete from purchaseOrder where PoCode = '" + item.getItemCode() + "'");
                            if(resultt == 1) {

                            }

                        }

                        ckExp = "Success";
                        return ckExp;

                    }else{

                        ckExp = "Error";
                        return ckExp;

                    }
                } catch (SQLException e) {
                    ckExp = "SqlExp";
                    // ckExp = "SqlExp";
                    Log.d("Error Type", "Exception : " + e.getMessage() );
                    e.printStackTrace();

                }



            }else{
                System.out.println("no record found : " + item.getItemID());
                ckExp = "NoRecord";
            }
        }else {
            System.out.println("The statement is null!");
            ckExp = "SqlExp";
        }*/


        return ckExp;
        //return false;
    }

    private String mQuickAdd(String type, int postion) {
        String ckExp = "";
        stmt = mConnection();
        if (stmt != null) {
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

    private int mCount(String sqlStmt){

        Statement stmt = mConnection();


        Integer count = 0;

        if (stmt != null){
            try {

                //   EditText itemSearch = findViewById(R.id.input_search);
                ResultSet rs = stmt.executeQuery(sqlStmt);
                while (rs.next()){
                    count = count + 1;
                }

            } catch (SQLException e) {

                Log.d("Error Type", "Exception : " + e.getMessage() );
                e.printStackTrace();

            }
        }else{
            Log.d("Error Type", "Connection error!");
        }

        return count;

    }

    public void showLoadingActionButton() {
        loginLoader.setVisibility(View.VISIBLE);
        norecordPanel.setVisibility(View.GONE);
    }

    public void hideLoadingActionButton() {
        loginLoader.setVisibility(View.GONE);
    }
}
