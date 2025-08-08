package com.franco.epos.appnav01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CPOFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String PageName;
    private String ActType;
    private String AActType;
    private String POCode;
    private String POStatus;



    private HomeFragment.OnFragmentInteractionListener mListener;

    private ItemCPODataAdapter mAdapter;
    SwipeController swipeController = null;

    RecyclerView recyclerView;


   // private Button btnCommit;
    private Button btnSave;
   // private Button btnAdd;
    private Button btnCancel;

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
    String typeVal = "";

    private List<Dictionary> itemsDict;
    private Map<String,String> itemRSTemp;


    final Context context = getContext();

    Handler mHandler;
    Runnable mRunnable;

    private RelativeLayout loginLoader, norecordPanel;
    private TextView msgAltText;
    private Button retryBtn;

    public CPOFragment() {
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
    public static CPOFragment newInstance(String param1, String param2) {
        CPOFragment fragment = new CPOFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
//        args.putSerializable(ARG_PARAM3,param3);
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
        itemRSTemp = new HashMap<String, String>();

        System.out.println("---------key_value."+inflater +"  The width : "+ width);
        readBundle(getArguments());
        System.out.println("Page: " + PageName);
        System.out.println("Types: " + ActType);



        //   mNameTextView.setText(String.format("Name: %s", name));
        //   mAgeTextView.setText(String.format("Age: %d", age));



        View view = inflater.inflate(R.layout.fragment_cpo_list,   container, false);




        btnSave  = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage("Do you want to Close the purchase order?");
                alertDialogBuilder.setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {


                                //commitSave("SAVE");
                               // ((MainActivity) getActivity()).loadPOCSEditFragment("SAVE",POCode,POCode,POStatus);

                                /*showLoadingActionButton();
                                String ckConn = mConnectionCheck("");
                                if (ckConn.equals("WifiCon")){

                                    OPOFragment.HomeTaskParams mParams = new OPOFragment.HomeTaskParams("Save", "", 0);
                                    new OPOFragment.LongOperation(true).execute(mParams);

                                }else{

                                    hideLoadingActionButton();
                                    finishDelete("ErrorWifi","Connection error");
                                }*/

                                showLoadingActionButton();
                                String ckConn = mConnectionCheck("");
                                if (ckConn.equals("WifiCon")){

                                    CPOFragment.HomeTaskParams mParams = new CPOFragment.HomeTaskParams("Commit", "", 0);
                                    new CPOFragment.LongOperation(false,false,true).execute(mParams);

                                }else{

                                    hideLoadingActionButton();
                                    finishDelete("ErrorWifi","Connection error");
                                }



                            }
                        });

                alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

        });



        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).loadBackFragment("Close PO");



            }
        });



        final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();

        if (fab != null) {
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

                    CPOFragment.HomeTaskParams mParams = new CPOFragment.HomeTaskParams("Load", "", 0);
                    new CPOFragment.LongOperation(false,false,true).execute(mParams);

                }else{

                    hideLoadingActionButton();
                    finishLoad("ErrorWifi","Connection error");
                }

            }

        });



        List<Item> items = new ArrayList<>();
        mAdapter = new ItemCPODataAdapter(items);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
        // setupRecyclerView();

        if(POStatus.equals("PROCESSING") || POStatus.equals("CLOSED")){

            btnSave.setVisibility(View.GONE);

        }else{
            btnSave.setVisibility(View.VISIBLE);
        }



        return view;
    }

    public void onStart() {
        super.onStart();


        showLoadingActionButton();
        String ckConn = mConnectionCheck("");
        if (ckConn.equals("WifiCon")){

            CPOFragment.HomeTaskParams mParams = new CPOFragment.HomeTaskParams("Load", "", 0);
            new CPOFragment.LongOperation(false,false,true).execute(mParams);

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

    private class LongOperation extends AsyncTask<CPOFragment.HomeTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "";
        int mPos = 0;

        public LongOperation(boolean showCommit, boolean showSave, boolean showAdd) {
            super();
            // do stuff
            /*if (showCommit){
                //btnCommit.setVisibility(View.VISIBLE);
                showCommitSave();
            }else{
                btnCommit.setVisibility(View.GONE);
            }*/
            if (showSave){
                showCommitSave();
                //btnSave.setVisibility(View.VISIBLE);
            }else{
                btnSave.setVisibility(View.GONE);
            }
            /*if (showAdd){
                btnAdd.setVisibility(View.VISIBLE);
            }else{
                btnAdd.setVisibility(View.GONE);
            }*/

        }

        @Override
        protected String doInBackground(CPOFragment.HomeTaskParams... params) {




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
                    mAdapter.items.remove(mPos);
                    mAdapter.notifyItemRemoved(mPos);
                    mAdapter.notifyItemRangeChanged(mPos, mAdapter.getItemCount());


                    if (mAdapter.getItemCount() > 0) {
                        /*btnCommit.setVisibility(View.VISIBLE);
                        btnSave.setVisibility(View.VISIBLE);*/
                        showCommitSave();
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
                    item = mAdapter.items.get(mPos);
                   // ((MainActivity) getActivity()).loadEditFragment(item.getItemID());
                    ((MainActivity) getActivity()).loadCPOEditFragment("ScanAdd",item.getItemLU(), POCode, POStatus);
                }else{
                    finishDelete("SqlExp", "Connection error");
                }


            }else if(loadMStrOne.equals("Commit")) {

                System.out.println("The commitement value : " + result);

                if (result.equals("Success")){

                    ((MainActivity) getActivity()).loadCPOCSEditFragment(ActType,"",POCode,POStatus);

                }else if(result.equals("SqlExp") || result.equals("Error")) {

                    finishDelete("SqlExp", "Connection error");

                }else if(result.equals("ErrorAdj") || result.equals("ErrorSav")) {
                    finishDelete("ErrorCommit", "Connection error commit");
                }
            }else{


            }





        }

        @Override
        protected void onPreExecute() {
            /*if(loadMStrOne.equals("Delete") || loadMStrOne.equals("Commit")){

            }else{
                btnCommit.setVisibility(View.GONE);
            }*/

            showLoadingActionButton();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private void commitSave(String type){
        String qtyCk = "YES";
        String vendorCk = "YES";

        for (int j=0; j<mAdapter.items.size();j++){

            if (mAdapter.items.get(j).getInQty().equals("0") || mAdapter.items.get(j).getInQty().equals("")){
                qtyCk = "NO";
                break;
            }
            if (mAdapter.items.get(j).getSupCode().equals("")){
                vendorCk = "NO";
                break;
            }

        }

        if (qtyCk.equals("NO")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please add quantity!");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else  if (vendorCk.equals("NO")){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Please add vendor!");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }else{
            ((MainActivity) getActivity()).loadPOCSEditFragment(type,POCode,POCode,POStatus);
        }



    }

    private void finishDelete (String action,String val){

        if (action.equals("Success")){
        }else if(action.equals("Error") || action.equals("SqlExp")) {
            /*btnCommit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);*/
            showCommitSave();
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

            /*btnCommit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);*/
            showCommitSave();

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
            /*btnCommit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);*/
            showCommitSave();

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

            /*btnCommit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);*/
            showCommitSave();
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
            /*btnCommit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);*/
            showCommitSave();

        }else if(action.equals("NoRecord")){

            //btnCommit.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("No record found");
            retryBtn.setText("Refresh");

        }else if(action.equals("ErrorWifi")){


           // btnCommit.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("Wifi Connection error");
            retryBtn.setText("Retry");

        }else if(action.equals("Error")){

           // btnCommit.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("Connection error");
            retryBtn.setText("Retry");

        }else{

            hideLoadingActionButton();

        }


    }
    public void showCommitSave(){
        if(POStatus.equals("PROCESSING") || POStatus.equals("CLOSED")){
           // btnCommit.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
        }else{
           // btnCommit.setVisibility(View.GONE);
            btnSave.setVisibility(View.VISIBLE);


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
            AActType = bundle.getString("AActType");
            POCode = bundle.getString("POCode");
            POStatus = bundle.getString("POStatus");


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
        itemRSTemp.clear();
        ckExp = "Success";
        if(AActType.equals("Edit")){

            db.emptyOPListTB();
            OPListTB oPListTB;

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

                        //ResultSet rs = stmt.executeQuery("select id, PoCode, descp, datetim, status From PoCode ORDER BY datetim DESC");
                        ResultSet rs;
                        if(POStatus.equals("PENDING") || POStatus.equals("CLOSED")){
                            rs = stmt.executeQuery("Select p.itemlookup, p.description, p.midasCode, j.descp, i.qty, i.type, i.vendorCode, i.stock FROM ClosePurchase as i INNER JOIN itemProduct as p On i.itemlookup = p.itemlookup INNER JOIN PoCode as j on i.PoCode = j.PoCode where i.PoCode = '" + POCode + "'");
                        }else{
                            rs = stmt.executeQuery("Select p.itemlookup, p.description, p.midasCode, j.descp, i.qty, i.type, i.vendorCode FROM purchaseOrder as i INNER JOIN itemProduct as p On i.itemlookup = p.itemlookup INNER JOIN PoCode as j on i.PoCode = j.PoCode where i.PoCode = '" + POCode + "'");
                        }

                        //select * from PoCode
                        //SampleArrayList.clear();
                        while (rs.next()){
                            count = count + 1;

                            //mobileArray.
                            /*Item item = new Item();
                            item.setItemID(rs.getString("id"));
                            item.setItemCode("PO0002");
                            item.setItemLU(rs.getString("itemlookup"));
                            item.setDescr(rs.getString("description"));
                            item.setSupCode(rs.getString("vendorCode"));
                            item.setInQty(rs.getString("qty"));
                            item.setMidasCode(rs.getString("midasCode"));
                            items.add(item);*/
                            String reStock = "";
                            if(POStatus.equals("PENDING") || POStatus.equals("CLOSED")){
                                reStock = rs.getString("stock");
                            }else{
                                reStock = rs.getString("qty");
                            }
                            itemRSTemp.put(rs.getString("itemlookup"), reStock);

                            oPListTB = new OPListTB(1,"","",rs.getString("itemlookup"),rs.getString("qty"),rs.getString("type"),rs.getString("vendorCode"),rs.getString("description"),rs.getString("midasCode"),reStock);
                            long trest =  db.insertOPListTB(oPListTB);
                            Log.d("myTag ", "This is my message rss" + trest);


                            //Log.d("myTag ", "This is my message rss" + rs.getString("vendorCode") );

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


            Log.d("myTag", "This is my message rss" + count );
        }else{

        }

        if (ckExp.equals("Success")){
            List<OPListTB> itemsPO = db.getAllOPListTB();
            if (itemsPO.size() > 0){
                for (int j=0; j<itemsPO.size();j++){
                    Log.d("myTag", "This is my message rss" + itemsPO.get(j).getItem_lookup());

                    Item item = new Item();
                    //item.setItemID(rs.getString("id"));
                    item.setItemCode(itemsPO.get(j).getPo_code());
                    item.setItemLU(itemsPO.get(j).getItem_lookup());
                    item.setDescr(itemsPO.get(j).getItem_descr());
                    item.setSupCode(itemsPO.get(j).getVendor_code());
                    item.setInQty(itemsPO.get(j).getQty());
                    item.setStock(itemsPO.get(j).getRestock());
                    item.setMidasCode(itemsPO.get(j).getMidas_code());
                    items.add(item);

                }
            }else{
                ckExp = "NoRecord";
            }

        }else{

        }



        mAdapter = new ItemCPODataAdapter(items);
        return ckExp;

    }

    private void setupRecyclerView() {


        // RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

        if(POStatus.equals("PROCESSING") || POStatus.equals("CLOSED")){

        }else{
            swipeController = new SwipeController(new SwipeControllerActions() {



                @Override
                public void onLeftClicked(int position) {
                    super.onLeftClicked(position);
                    showLoadingActionButton();
                    String ckConn = mConnectionCheck("");
                    if (ckConn.equals("WifiCon")){

                        //mEditItem(ActType,position);
                        CPOFragment.HomeTaskParams mParams = new CPOFragment.HomeTaskParams("EDIT", "", position);
                        new CPOFragment.LongOperation(true, true,true).execute(mParams);
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
                                CPOFragment.HomeTaskParams mParams = new CPOFragment.HomeTaskParams("EDIT", "", position);
                                new CPOFragment.LongOperation(true, true, true).execute(mParams);

                            }else{

                                hideLoadingActionButton();
                                finishDelete("ErrorWifi","Connection error");

                            }
                            // Toast.makeText(getActivity(),"You clicked yes button" + position,Toast.LENGTH_LONG).show();
                        }
                    })
            );
        }









    }




    private String mCommitItems(String type){

        String ckExp = "";

        int flag = 0;
        String datttm = DateFormat.getDateTimeInstance().format(new Date());

        String poDescr = "";
        poDescr = getDesc("Descr","Select descp FROM PoCode where PoCode = '" + POCode + "'");


        stmt = mConnection();
        if (stmt != null) {

            List<OPListTB> itemsPO = db.getAllOPListTB();
            if (itemsPO.size() > 0){
                for (int j=0; j<itemsPO.size();j++){
                    Log.d("myTag", "This is my message rss" + itemsPO.get(j).getItem_lookup());

                    String itemcode = itemsPO.get(j).getItem_lookup();
                    String stockDesa = itemsPO.get(j).getItem_descr();
                    String qty = itemsPO.get(j).getQty();
                    String supty = itemsPO.get(j).getType();
                    String vens = itemsPO.get(j).getVendor_code();
                    String restock = itemsPO.get(j).getRestock();


                    try {
                        int resultProd = stmt.executeUpdate("update itemProduct set oneHand = oneHand + '" + restock + "' where ItemLookup = '" + itemcode + "'");
                        if(resultProd == 1) {
                            int resultCPO = mCount("select PoCode from ClosePurchase where PoCode = '" + POCode + "' and itemlookup = '" + itemcode + "'");
                            if (resultCPO == 0){
                                int resultICPO = stmt.executeUpdate("Insert into ClosePurchase(PoCode, PoDesc, itemlookup, qty, type, vendorCode, stock, datetim) VALUES ('" + POCode + "', '" + poDescr + "','" + itemcode + "','" + qty + "','" + supty + "','" + vens + "', '" + restock + "', '" + datttm + "')");

                                if(resultICPO == 1) {

                                    if(Integer.parseInt(qty) > Integer.parseInt(restock)) {
                                        flag = 1;
                                    }

                                }
                            }else {

                                int resultUCPO = stmt.executeUpdate("update ClosePurchase set stock = '" + restock + "' where PoCode = '" + POCode + "' and itemlookup = '" + itemcode + "'");
                                if(resultUCPO == 1) {
                                    if(Integer.parseInt(itemRSTemp.get(itemcode)) > Integer.parseInt(restock)) {
                                        flag = 1;
                                    }

                                }

                            }
                        }
                    }catch (SQLException e) {

                        Log.d("Error Type", "Exception : " + e.getMessage());
                        e.printStackTrace();
                        ckExp = "SqlExp";

                    }



                }
                try {
                    int resultUPO;
                    if (flag == 0){
                        resultUPO = stmt.executeUpdate("update PoCode set status = 'CLOSED' where PoCode = '" + POCode + "'");
                        if(resultUPO == 1) {

                        }
                    }else{

                        resultUPO = stmt.executeUpdate("Update PoCode set status = 'PENDING' where PoCode = '" + POCode + "'");
                        if(resultUPO == 1) {

                        }

                    }
                    ckExp = "Success";

                }catch (SQLException e) {

                    Log.d("Error Type", "Exception : " + e.getMessage());
                    e.printStackTrace();
                    ckExp = "SqlExp";


                }

            }else{
                ckExp = "SqlExp";
            }
        }else{
            ckExp = "SqlExp";
        }






        return ckExp;
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
                }else if (type.equals("Descr")){
                    rVal = rs.getString("descp");

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
            item = mAdapter.items.get(postion);
            ((MainActivity) getActivity()).loadEditFragment(item.getItemID());
            */
        }


        return ckExp;


    }

    private String mDeleteItem(String type, int postion) {
        String ckExp = "";

        Item item = new Item();
        item = mAdapter.items.get(postion);

        if (db.getOPListTBCountByITL(item.getItemLU()) > 0) {
            OPListTB oPListTB;
            oPListTB = new OPListTB(1,"","",item.getItemLU(),item.getInQty(),"Primary",item.getSupCode(),item.getDescr(),item.getMidasCode(),"0");
            db.deleteOPListTB(oPListTB);

            ckExp = "Success";
            return ckExp;

        }else{
            ckExp = "NoRecord";
        }

        /*stmt = mConnection();
        if (stmt != null) {

            Item item = new Item();
            item = mAdapter.items.get(postion);
            //"select * from tempProAdjustment where (itemID = '" & dictVal.Item("itemId") & "' AND adj_type = '" & defaultType & "')"
            //if (mCount("select * from tempProAdjustment where itemID = '"+ item.getItemID()+"'") != 0){
            if (mCount("select * from tempProAdjustment where (itemID = '" + item.getItemID() + "' AND adj_type = '" + type + "')") != 0){
                System.out.println("The value is : " + item.getItemID() );

                try {
                    int result = stmt.executeUpdate("DELETE from tempProAdjustment where (itemID = '" + item.getItemID() + "' AND adj_type = '" + type + "')");
                    if(result == 1) {


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
                count = -1;
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
