package com.franco.epos.appnav01;

import android.app.Activity;
import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class POFilterActivity extends Activity {


    private Button btnScan;
    private Button btnSr;
    private Button btnSave;
    private Button btnCancel;
    private Button btnPckQty;
    private ArrayList<String> SampleArrayList = new ArrayList<String>();
    private AppCompatSpinner departSpinner;

    RecyclerView recyclerView;
    private ItemPOFilterDataAdapter mAdapter;
    SwipeController swipeController = null;

    private String actionType = "";
    private String aactionType = "";
    private String poCode = "";
    private String poStatus = "";



    private EditText itemSearch, itemDescription, itemQty, itemReason, itemStock, itemPrice;

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
    private RelativeLayout loginLoader, norecordPanel;
    private TextView msgAltText, departVal;
    private Button retryBtn;

    List<String> spinnerDepartArray;
    ArrayAdapter<String> adapterDepart;
    private List<Dictionary> departsDict;
    int selDepartItem = -1;
//    Boolean checkDUN = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.po_filter_list);
        db = new DatabaseHelper(context);
        actionType = getIntent().getStringExtra("ActType");
        aactionType = getIntent().getStringExtra("AActType");
        poCode = getIntent().getStringExtra("POCode");
        poStatus = getIntent().getStringExtra("POStatus");
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode




        SettingsTB setTb = db.getSetTBByType("SERVER");
        ipaddress = setTb.getIpaddress();
        port = setTb.getPort();
        staffID = pref.getString("staffID",null);

        Log.d("myTag session ", "This is my message rs" + actionType + " DDDD : " + getIntent().getStringExtra("ActFrom"));

        loginLoader = (RelativeLayout) findViewById(R.id.loadingPanel);
        norecordPanel = (RelativeLayout) findViewById(R.id.noRecordPanel);
        msgAltText = (TextView) findViewById(R.id.txt_msg_alt);
        departVal = (TextView) findViewById(R.id.depart_val);
       // retryBtn = (Button) findViewById(R.id.btn_retry);

        loginLoader.setVisibility(View.GONE);
        norecordPanel.setVisibility(View.GONE);

        departVal.setText(aactionType);

        departSpinner = findViewById(R.id.depart_spinner);
        recyclerView = findViewById(R.id.recyclerView);

        departsDict = new ArrayList<>();
        spinnerDepartArray =  new ArrayList<String>();

        if(aactionType.equals("Department")){
            spinnerDepartArray =  setAdapterList("depart","Select depart_id, description, ageCheck, vat from department order by description asc");
        }else if(aactionType.equals("Category")){
            spinnerDepartArray =  setAdapterList("cate","Select id, departID, category, vat from category order by category asc");
        }else if(aactionType.equals("Supplier")){
            spinnerDepartArray =  setAdapterList("supp","select id, code, vedName from vendor order by code asc");
        }





        adapterDepart = new ArrayAdapter<String>(context, R.layout.spinner_layout, spinnerDepartArray){

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



                return v;
            }
        };
        adapterDepart.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departSpinner.setAdapter(adapterDepart);

        selDepartItem = 0;
        departSpinner.setSelection(selDepartItem);

        departSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int myPosition, long myID) {

                Log.i("renderSpinner -> ", "onItemSelected: " + myPosition + "/" + myID + "/" + departSpinner.getSelectedItem().toString());

                Dictionary dictArr = departsDict.get(myPosition);

                // Toast.makeText(getActivity(), "Depart ID : " + dictArr.get("departID").toString() + "Depart Name : " + dictArr.get("departName").toString() + "Depart Vat : " + dictArr.get("departVat").toString(), Toast.LENGTH_LONG).show();



                /*if (dictArr.get("departName").toString().equals("Select")) {
                    itemDescription.setText("");
                } else {
                    itemDescription.setText(dictArr.get("departName").toString());
                }*/


                /*if (checkDUN) {

                } else {

                }*/




                selDepartItem = myPosition;

                if(departSpinner.getSelectedItem().toString().equals("Select")){

                }else{
                    showLoadingActionButton();
                    String ckConn = mConnectionCheck("");
                    if (ckConn.equals("WifiCon")){

                        String filVal = "";

                        if(aactionType.equals("Department")){

                            filVal = dictArr.get("departID").toString();
                        }else if(aactionType.equals("Category")){
                            filVal = dictArr.get("cateID").toString();
                        }else if(aactionType.equals("Supplier")){
                            filVal = dictArr.get("suppCode").toString();
                        }

                        POFilterTaskParams mParams = new POFilterTaskParams("Load", filVal, aactionType, 0);
                        new LongOperation(false).execute(mParams);

                    }else{

                        hideLoadingActionButton();
                        // finishLoad("ErrorWifi","Connection error");
                    }
                }




            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });


        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*List<ItemFilter> items = new ArrayList<>();*/
                String data = "";
                List<Object> listItemLU = new ArrayList<>();
//                ArrayList<String> listItemLU = new ArrayList<String>();

                /*
                ItemFilter item = new ItemFilter();
                        item.setItemLU(rs.getString("ItemLookup"));
                        item.setInQty(rs.getString("avaiable"));
                        item.setDescr(rs.getString("description"));
                        item.setInQty(rs.getString("restockLevel"));
                        item.setSupCode(supCode);
                        item.setSelected(false);
                        items.add(item);
                 */
                //SettingsTB setTb = db.getSetTBByType("SERVER");
                if (db.getOPListTBCount() > 0){
                    Log.d("myTag ", "There is a record in the table " + db.getOPListTBCount());
                }else {
                    Log.d("myTag ", "There is a record in the table");
                }

                OPListTB oPListTB;
                for (int j=0; j<mAdapter.items.size();j++){

                    if (mAdapter.items.get(j).isSelected() == true){
                        //ItemParcelable item = new ItemParcelable();
                        /*item.setItemLU(rs.getString("ItemLookup"));
                        item.setInQty(rs.getString("avaiable"));
                        item.setDescr(rs.getString("description"));
                        item.setInQty(rs.getString("restockLevel"));
                        item.setSupCode(supCode);
                        item.setSelected(false);
                        items.add(item);*/
                        //item = mAdapter.items.get(j);

                        /*item.setItemLU(mAdapter.items.get(j).getItemLU());
                        item.setInQty(mAdapter.items.get(j).getInQty());
                        item.setDescr(mAdapter.items.get(j).getDescr());
                        item.setSupCode(mAdapter.items.get(j).getSupCode());*/


                        data = data + "\n" + mAdapter.items.get(j).getDescr().toString() + "   " + mAdapter.items.get(j).getInQty().toString();
                        listItemLU.add(mAdapter.items.get(j).getItemLU().toString());

                        oPListTB = new OPListTB(1,"","",mAdapter.items.get(j).getItemLU(),mAdapter.items.get(j).getInQty(),"Primary",mAdapter.items.get(j).getSupCode(),mAdapter.items.get(j).getDescr(),mAdapter.items.get(j).getMidasCode(),"0");
                        long trest =  db.insertOPListTB(oPListTB);
                        Log.d("myTag ", "This is my message Filter" + trest);
                       // Log.d("myTag ", "There is a record in the table " + db.getOPListTBCountByITL(mAdapter.items.get(j).getItemLU()));
                        /*if (db.getOPListTBCountByITL(mAdapter.items.get(j).getItemLU()) > 0) {
                            Log.d("myTag ", "There is a record in the table" + mAdapter.items.get(j).getItemLU());
                        }else{
                            Log.d("myTag ", "There is no record in the table" + mAdapter.items.get(j).getItemLU());
                        }*/

                    }
                }
                Log.d("myTag ", "This is my message Filter" + data);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("NavValue", "OPO List");
                resultIntent.putExtra("AActType", aactionType);
                resultIntent.putExtra("POCode", poCode);
                resultIntent.putExtra("POStatus", poStatus);
                //resultIntent.putParcelableArrayListExtra("ILUList", listItemLU);
                //resultIntent.putParcelable("mylist", Parcels.wrap(listItemLU));
                //resultIntent.putExtra("ILUList", (Serializable) listItemLU);
                //resultIntent.putParcelableArrayListExtra("ILUList", (ArrayList<? extends Parcelable>) listItemLU);

                setResult(Activity.RESULT_OK, resultIntent);

                finish();

            }

        });

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                /*List<ItemFilter> items = new ArrayList<>();*/
                /*String data = "Testing";
                for (int j=0; j<mAdapter.items.size();j++){

                    if (mAdapter.items.get(j).isSelected() == true){
                        data = data + "\n" + mAdapter.items.get(j).getDescr().toString() + "   " + mAdapter.items.get(j).getInQty().toString();
                    }
                }
                Log.d("myTag ", "This is my message Filter" + data);*/


                Intent resultIntent = new Intent();
                resultIntent.putExtra("NavValue", "OPO List");
                resultIntent.putExtra("AActType", aactionType);
                resultIntent.putExtra("POCode", poCode);
                resultIntent.putExtra("POStatus", poStatus);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();


            }

        });

        List<ItemFilter> items = new ArrayList<>();
        mAdapter = new ItemPOFilterDataAdapter(POFilterActivity.this,items);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
        // setupRecyclerView();


    }





    private static class POFilterTaskParams {
        String hParaOne;
        String hParaTwo;
        String hParaThree;
        int recPos;


        POFilterTaskParams(String hParaOne, String hParaTwo, String hParaThree, int recPos) {
            this.hParaOne = hParaOne;
            this.hParaTwo = hParaTwo;
            this.hParaThree = hParaThree;
            this.recPos = recPos;

        }
    }

    private class LongOperation extends AsyncTask<POFilterTaskParams, Void, String> {
        String loadMStrOne = "", loadMStrTwo = "", loadMStrThree = "";
        int mPos = 0;

        public LongOperation(boolean showCommit) {
            super();
            // do stuff
            if (showCommit){
              //  btnCommit.setVisibility(View.VISIBLE);
            }else{
              //  btnCommit.setVisibility(View.GONE);
            }

        }

        @Override
        protected String doInBackground(POFilterTaskParams... params) {




            loadMStrOne  = params[0].hParaOne;
            loadMStrTwo  = params[0].hParaTwo;
            loadMStrThree  = params[0].hParaThree;
            mPos = params[0].recPos;



            //String mVal = params[0];

            System.out.println("The value in doInBackground : " + loadMStrOne);

            String result = "0";

            if (loadMStrOne.equals("Load")) {
               // result = setSerItemDataAdapter(ActType);
                result = setSerItemDataAdapter(loadMStrThree,loadMStrTwo);
            }else if(loadMStrOne.equals("Delete")) {
               // result = mDeleteItem(ActType, mPos);

            }else if(loadMStrOne.equals("EDIT")){
               // result = mEditItem(ActType, mPos);
            }else if(loadMStrOne.equals("Commit")) {

               // result = mCommitItems(ActType);
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

                    Toast.makeText(context, "Deleted sucessfully", Toast.LENGTH_LONG).show();
                    mAdapter.items.remove(mPos);
                    mAdapter.notifyItemRemoved(mPos);
                    mAdapter.notifyItemRangeChanged(mPos, mAdapter.getItemCount());


                    if (mAdapter.getItemCount() > 0) {
                       // btnCommit.setVisibility(View.VISIBLE);
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

                    /*Item item = new Item();
                    item = mAdapter.items.get(mPos);
                    ((MainActivity) getActivity()).loadEditFragment(item.getItemID());*/

                }else{
                    finishDelete("SqlExp", "Connection error");
                }


            }else if(loadMStrOne.equals("Commit")) {

                System.out.println("The commitement value : " + result);

                if (result.equals("Success")){

                    //((MainActivity) getActivity()).loadCommitFragment(ActType);

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


    private void finishDelete (String action,String val){

        if (action.equals("Success")){
        }else if(action.equals("Error") || action.equals("SqlExp")) {
            /*btnCommit.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Connection error, Please check the connection");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();*/

        }else if(action.equals("NoRecordDel")) {

            /*btnCommit.setVisibility(View.VISIBLE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("There is no recored found to delete!");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();*/

        }else if(action.equals("ErrorWifi")){



            /*btnCommit.setVisibility(View.VISIBLE);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Connection error, Please connect to WIFI");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();*/
        }else if(action.equals("ErrorCommit")){

            /*btnCommit.setVisibility(View.VISIBLE);


            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Connection error, While commiting!");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();*/

        }else{
            hideLoadingActionButton();
        }


    }

    private void finishLoad (String action,String val){

        if (action.equals("Success")) {

            norecordPanel.setVisibility(View.GONE);
            //btnCommit.setVisibility(View.VISIBLE);

        }else if(action.equals("NoRecord")){

           // btnCommit.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("No record found");
           // retryBtn.setText("Refresh");

        }else if(action.equals("ErrorWifi")){


           // btnCommit.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("Wifi Connection error");
           // retryBtn.setText("Retry");

        }else if(action.equals("Error")){

           // btnCommit.setVisibility(View.GONE);
            norecordPanel.setVisibility(View.VISIBLE);
            msgAltText.setText("Connection error");
           // retryBtn.setText("Retry");

        }else{

            hideLoadingActionButton();

        }


    }

    private String setSerItemDataAdapter(String type, String value) {

        Integer count = 0;
        String ckExp = "";
        List<ItemFilter> items = new ArrayList<>();
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
                    ResultSet rs = stmt.executeQuery("select id, ItemLookup, description, avaiable, restockLevel, midasCode From itemProduct where stockCk <> 0");
                    if(type.equals("Department")) {
                        rs = stmt.executeQuery("select id, ItemLookup, description, avaiable, restockLevel, midasCode From itemProduct where stockCk <> 0 AND department = '" + value + "'");
                    }else if(type.equals("Category")){
                        rs = stmt.executeQuery("select id, ItemLookup, description, avaiable, restockLevel, midasCode From itemProduct where stockCk <> 0 AND category = '" + value + "'");
                    }else if(type.equals("Supplier")){
                        rs = stmt.executeQuery("select p.id, p.ItemLookup, p.description, p.avaiable, p.restockLevel, p.midasCode from itemProduct p join itemVendor d on p.ItemLookup = d.itemLookup where (d.vendorCode = '" + value + "') AND p.stockCk <> 0");
                    }else{

                    }

                    //select * from PoCode
                    //SampleArrayList.clear();
                    while (rs.next()){
                        count = count + 1;
//                        itemlookup, qty, type, vendorCode, datetim, delvDatetim
//                        0, itemlookup, description, qtyt, "Primary", supcode, midasCode
                        String supCode = "";

                        //mobileArray.

                        if (db.getOPListTBCountByITL(rs.getString("ItemLookup")) > 0) {
                            Log.d("myTag ", "There is a record in the table" + rs.getString("ItemLookup"));
                        }else{
                            supCode = getDesc("ItemSupCode","Select i.vendorCode FROM itemProduct as p JOIN itemVendor as i ON p.ItemLookup = i.itemlookup where i.itemLookup = '" + rs.getString("ItemLookup") + "' and i.venprimary = 'True'");
                            ItemFilter item = new ItemFilter();
                            item.setItemLU(rs.getString("ItemLookup"));
                            item.setInQty(rs.getString("avaiable"));
                            item.setDescr(rs.getString("description"));
                            item.setInQty(rs.getString("restockLevel"));
                            item.setSupCode(supCode);
                            item.setMidasCode(rs.getString("midasCode"));
                            item.setSelected(false);
                            items.add(item);
                        }


                        /*items.add(item);
                        items.add(item);
                        items.add(item);
                        items.add(item);
                        items.add(item);
                        items.add(item);restockLevel*/


                        Log.d("myTag ", "This is my message rss" + rs.getString("description") );

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

        mAdapter = new ItemPOFilterDataAdapter(POFilterActivity.this,items);
        return ckExp;

    }

    private void setupRecyclerView() {


        // RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);



        /*swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {

                showLoadingActionButton();
                String ckConn = mConnectionCheck("");
                if (ckConn.equals("WifiCon")){

                    // new LongOperation().execute("Delete");

                    POFragment.HomeTaskParams mParams = new POFragment.HomeTaskParams("Delete", "", position);
                    new POFragment.LongOperation(true).execute(mParams);



                    *//*if (mDeleteItem(ActType,position)){

                        Toast.makeText(getActivity(),"Deleted sucessfully",Toast.LENGTH_LONG).show();
                        mAdapter.items.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        mAdapter.notifyItemRangeChanged(position, mAdapter.getItemCount());

                    }else{


                    }*//*


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
                    POFragment.HomeTaskParams mParams = new POFragment.HomeTaskParams("EDIT", "", position);
                    new POFragment.LongOperation(true).execute(mParams);
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

        */
        /*recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(context, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // TODO Handle item click

                        showLoadingActionButton();
                        String ckConn = mConnectionCheck("");
                        if (ckConn.equals("WifiCon")){

                            // mEditItem(ActType,position);
                            *//*POFragment.HomeTaskParams mParams = new POFragment.HomeTaskParams("EDIT", "", position);
                            new POFragment.LongOperation(true).execute(mParams);*//*

                        }else{

                            hideLoadingActionButton();
                            finishDelete("ErrorWifi","Connection error");

                        }
                        // Toast.makeText(getActivity(),"You clicked yes button" + position,Toast.LENGTH_LONG).show();
                    }
                })
        );*/




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
            }else if(type.equals("cate")){
                spinnerArray.add("Select");
                departsDict.clear();
                Dictionary dictTempSel = new Hashtable();
                dictTempSel.put("cateID","");
                dictTempSel.put("departID", "");
                dictTempSel.put("cateName", "Select");
                dictTempSel.put("cateVat", "0%");
                departsDict.add(dictTempSel);
            }else if(type.equals("supp")){
                spinnerArray.add("Select");
                departsDict.clear();
                Dictionary dictTempSel = new Hashtable();
                dictTempSel.put("suppID","");
                dictTempSel.put("suppName", "Select");
                dictTempSel.put("suppCode", "");
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


                }else if(type.equals("cate")){
                    spinnerArray.add(rs.getString("category"));

                    dictTemp.put("cateID", rs.getString("id"));
                    dictTemp.put("departID", rs.getString("departID"));
                    dictTemp.put("cateName", rs.getString("category"));
                    if (rs.getString("vat").toString().equals("")){
                        dictTemp.put("cateVat", "0%");
                    }else{
                        dictTemp.put("cateVat", rs.getString("vat"));
                    }

                    departsDict.add(dictTemp);
                }else if (type.equals("supp")) {
                    spinnerArray.add(rs.getString("vedName") + " ("+ rs.getString("code") +")");

                    dictTemp.put("suppID", rs.getString("id"));
                    dictTemp.put("suppCode", rs.getString("code"));
                    dictTemp.put("suppName", rs.getString("vedName"));


                    departsDict.add(dictTemp);
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

    private String getDesc(String type, String query){

        String rVal = "";


        stmt = mConnection();

        if (stmt != null) try {


            ResultSet rs = stmt.executeQuery(query);

            //SampleArrayList.clear();

            while (rs.next()) {

                rVal = rs.getString("vendorCode");


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





    private Dictionary selectArray(String type, String query) {

        Dictionary dict = new Hashtable();

        Integer count = 0;
        String id = "",itemid = "", itemLU = "", descr = "", barCode = "", price = "", stock = "", in_qty = "", reason = "", adj_type = "", error_type = "Success";

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
                    itemLU = rs.getString("ItemLookup");
                    descr = rs.getString("description");
                    barCode = rs.getString("barCode");
                    price = rs.getString("price_changed");
                    stock = rs.getString("avaiable");
                    in_qty = rs.getString("in_qty");
                    reason = rs.getString("reason");
                    adj_type = rs.getString("adj_type");


                }else if (type.equals("itemCheck")){

                    adj_type = rs.getString("adj_type");

                } else {

                    id = rs.getString("id");
                    itemid = id;
                    itemLU = rs.getString("ItemLookup");
                    descr = rs.getString("description");
                    barCode = rs.getString("barCode");
                    price = rs.getString("price");
                    stock = rs.getString("avaiable");


                }

                error_type = "Success";

               // Log.d("myTag", "This is my message rss dfdf " + rs.getString("ItemLookup") + itemSearch.getText());


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

        if (type.equals("itemTemp")) {


            dict.put("count", count);
            dict.put("fnType", "new");
            dict.put("id", id);
            dict.put("itemId", itemid);
            dict.put("itemLU", itemLU);
            dict.put("descr", descr);
            dict.put("barCode", barCode);
            dict.put("price", price);
            dict.put("stock", stock);
            dict.put("in_qty", in_qty);
            dict.put("reason", reason);
            dict.put("adj_type", adj_type);
            dict.put("error_type", error_type);



        }else if (type.equals("itemCheck")){

            dict.put("count", count);
            dict.put("fnType", "edit");
            dict.put("id", id);
            dict.put("itemId", itemid);
            dict.put("itemLU", itemLU);
            dict.put("descr", descr);
            dict.put("barCode", barCode);
            dict.put("price", price);
            dict.put("stock", stock);
            dict.put("in_qty", in_qty);
            dict.put("reason", reason);
            dict.put("adj_type", adj_type);
            dict.put("error_type", error_type);

        }else{

            dict.put("count", count);
            dict.put("fnType", "edit");
            dict.put("id", id);
            dict.put("itemId", itemid);
            dict.put("itemLU", itemLU);
            dict.put("descr", descr);
            dict.put("barCode", barCode);
            dict.put("price", price);
            dict.put("stock", stock);
            dict.put("in_qty", in_qty);
            dict.put("reason", reason);
            dict.put("adj_type", adj_type);
            dict.put("error_type", error_type);

        }

        return  dict;


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
}
