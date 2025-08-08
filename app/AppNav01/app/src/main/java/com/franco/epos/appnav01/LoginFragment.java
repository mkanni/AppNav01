package com.franco.epos.appnav01;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class LoginFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //FloatingActionButton fab;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LoginFragment.OnFragmentInteractionListener mListener;

    private Button btnLogin;
    private Button btnClr;

    private EditText txtUserName;
    private EditText txtPwd;
    DatabaseHelper db;
    private ArrayList<String> SampleArrayList = new ArrayList<String>();

    private String ipaddress, port;
    private SharedPreferences pref;

    Handler mHandler;
    Runnable mRunnable;

    private RelativeLayout loginLoader;

  //  private DatabaseHelper db;

    public LoginFragment() {
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
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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


        //final View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        /*


        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        fab.hide();
        */
        /*StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);*/


        final View view = inflater.inflate(R.layout.fragment_login,   container, false);

        final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();

        if (fab != null) {
            ((MainActivity) getActivity()).hideFloatingActionButton();
        }
        db = new DatabaseHelper(getContext());
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode

        mHandler = new Handler();

        SettingsTB setTb = db.getSetTBByType("SERVER");
        ipaddress = setTb.getIpaddress();
        port = setTb.getPort();

        txtUserName = (EditText) view.findViewById(R.id.input_usr);
        txtPwd = (EditText) view.findViewById(R.id.input_pwd);

        loginLoader = (RelativeLayout) view.findViewById(R.id.loadingPanel);
        loginLoader.setVisibility(View.GONE);

        /*loginLoader = (RelativeLayout) view.findViewById(R.id.loadingPanel);
       // findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        loginLoader.setVisibility(view.GONE);*/


        btnLogin =(Button)view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                if (txtUserName.getText().toString().trim().length() < 1) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Please enter username");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                    //editText.getText().clear();
                                    txtUserName.requestFocus();
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();


                }else if(txtPwd.getText().toString().trim().length() < 1){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Please enter the password");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                    //editText.getText().clear();
                                    txtPwd.requestFocus();
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{

                    /*InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);*/

                    //((MainActivity) getActivity()).showLoadingActionButton();
                    //submitLogin();
                    showLoadingActionButton();
                    String ckConn = mConnectionCheck("");

                    if (ckConn.equals("WifiCon")){

                        /*mRunnable = new Runnable() {
                            @Override
                            public void run() {

                                submitLogin();
                            }
                        };
                        mHandler.postDelayed(mRunnable, 100);*/
                        new LongOperation().execute("");


                    }else{

                        hideLoadingActionButton();
                        finishLogin("ErrorWifi","Connection error");
                    }












                }



            }

        });

        btnClr =(Button)view.findViewById(R.id.btn_clear);
        btnClr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("myTag", "This is my message rs" );
                //finish();
                txtUserName.getText().clear();
                txtPwd.getText().clear();
            }

        });



        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    private class LongOperation extends AsyncTask<String, Void, String > {

        @Override
        protected String doInBackground(String... params) {

            String result = "0";

            result = submitLogin();

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            hideLoadingActionButton();


            System.out.println("The check value : " + result);

            if (result.equals("Success")) {
                finishLogin("Success", "Success");
                // ckExp = "Success";
            }else if(result.equals("InvalidUNP")) {


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setMessage("Invalid username and password");
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                //editText.getText().clear();

                                txtUserName.requestFocus();
                            }
                        });


                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }else if (result.equals("Exp") || result.equals("SqlExp") || result.equals("ClassExp")){
                finishLogin("Error","Connection error");

            }else{

            }



        }

        @Override
        protected void onPreExecute() {
           // btnCommit.setVisibility(View.GONE);
            showLoadingActionButton();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private String submitLogin(){


        //((MainActivity) getActivity()).showLoadingActionButton();


        Connection connection = null;
        String ConnectionURL = null;
        Statement stmt = null;
        Integer count = 0;

        String tillValue = "";
        String serIPAdd = "";
        Set<String> setClient = new HashSet<String>();


        /*ConnectionURL = "jdbc:jtds:sqlserver://"+ ipaddress +":"+ port +"/ePos_Master;user=eposLourdes;password=KrishnaAndJesus;";

        System.out.println("The connection : " + ConnectionURL);*/

        String ckExp = "";


        try
        {
            //ipaddress = "192.168.1.13";

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://"+ ipaddress +":"+ port +"/ePos_Master;user=eposLourdes;password=KrishnaAndJesus;";
            connection = DriverManager.getConnection(ConnectionURL);



            try {

                stmt = connection.createStatement();
                ArrayList<String> SampleArrayList = new ArrayList<String>();
                ResultSet rs = stmt.executeQuery("select staffID,staffName,password,permission,staffType from staffLogin where staffID='"+ txtUserName.getText().toString().trim() +"' And password ='"+ txtPwd.getText().toString() +"'");
                SampleArrayList.clear();
                while (rs.next()){

                    count = count + 1;

                    SampleArrayList.add(rs.getString("staffID"));
                    SampleArrayList.add(rs.getString("staffName"));
                    SampleArrayList.add(rs.getString("password"));
                    SampleArrayList.add(rs.getString("permission"));
                    SampleArrayList.add(rs.getString("staffType"));

                }


                // stmt = connection.createStatement();
                ResultSet rsTill = stmt.executeQuery("select machineType from storeConfig");


                while (rsTill.next()){

                    tillValue = rsTill.getString("machineType");

                }

                if (tillValue.equals("Server")){
                    ResultSet rsSer = stmt.executeQuery("select concat(ipaddress,',', port) As ipadd from IPclient");

                    while (rsSer.next()){
                        // tillValue = rs.getString("machineType");
                        setClient.add(rsSer.getString("ipadd"));
                    }
                }else if(tillValue.equals("Client")){

                    ResultSet rsClt = stmt.executeQuery("select concat(ipAddress,',', port) As ipadd from IPserver");
                    // SampleArrayList.clear();
                    while (rsClt.next()){
                        serIPAdd = rsClt.getString("ipadd");
                    }

                }

                //"select * from IPclient"

                if (count > 0){


                    // setClient.add("Test 1");
                    // setClient.add("Test 2");

                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("ckLogin", true); // Storing boolean - true/false
                editor.putString("staffID", SampleArrayList.get(0)); // Storing string
                editor.putString("staffName", SampleArrayList.get(1)); // Storing string
                editor.putString("password", SampleArrayList.get(2)); // Storing string
                editor.putString("permission", SampleArrayList.get(3)); // Storing string
                editor.putString("staffType", SampleArrayList.get(4)); // Storing string
                editor.putString("tillType",tillValue);
                editor.putStringSet("CIPAddArray", setClient);
                editor.putString("SIPAdd", serIPAdd);
                editor.commit();

                // finishLogin("Success","Success");
                ckExp = "Success";

                //handler.removeCallbacks(mPendingRunnable);

            }else{

            ckExp = "InvalidUNP";

        }

            Log.d("myTag", "This is my message stmt" + count + SampleArrayList.get(2));

            } catch (SQLException e) {
                Log.d("myTag", "This is my message stmt");
                e.printStackTrace();
                ckExp = "SqlExp";
            }
            catch (Exception e)
            {
                Log.e("error here 6 : ", e.getMessage());
                ckExp = "InvalidUNP";
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

        /*if (!ckExp.equals("")){
            finishLogin("Error","Connection error");
        }else{
            finishLogin("Error","Connection error");
        }*/

        return ckExp;


    }

    private void finishLogin (String action,String val){

        if (action.equals("Success")) {

            hideSoftKeyBoard();
           // hideLoadingActionButton();
            ((MainActivity) getActivity()).loadFragment();

        }else if(action.equals("ErrorWifi")){

           // hideLoadingActionButton();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Connection error, Please connect to WIFI");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                            //editText.getText().clear();
                            //txtPwd.getText().clear();
                            //txtUserName.getText().clear();
                            txtUserName.requestFocus();
                        }
                    });


            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }else if(action.equals("Error")){

           // hideLoadingActionButton();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setMessage("Connection error, Please check the connection");
            alertDialogBuilder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                            //editText.getText().clear();
                            txtPwd.getText().clear();
                            txtUserName.getText().clear();
                            txtUserName.requestFocus();
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
       /* handler = null;
        mPendingRunnable = null;*/
      // handler.removeCallbacks(mPendingRunnable);
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
