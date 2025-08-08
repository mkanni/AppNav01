package com.franco.epos.appnav01;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
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

import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.SettingsTB;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SettingsFragment  extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    //FloatingActionButton fab;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SettingsFragment.OnFragmentInteractionListener mListener;

    private Button btnSave;
    private Button btnClr;
    private EditText txtIPAddress;
    private EditText txtPort;

    private String trackIPAddress, trackPort;

    DatabaseHelper db;
  //  private DatabaseHelper db;

    public SettingsFragment() {
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
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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





        View view = inflater.inflate(R.layout.fragment_settings,   container, false);

        final FloatingActionButton fab = ((MainActivity) getActivity()).getFloatingActionButton();

        if (fab != null) {
            ((MainActivity) getActivity()).hideFloatingActionButton();
        }

        db = new DatabaseHelper(getContext());
        txtIPAddress = (EditText) view.findViewById(R.id.input_ip);
        txtPort = (EditText) view.findViewById(R.id.input_port);

        if (db.getSettingsTBCount() > 0){

            SettingsTB setTB = db.getSetTBByType("SERVER");
             txtIPAddress.setText(setTB.getIpaddress());
             txtPort.setText(setTB.getPort());

             trackIPAddress = setTB.getIpaddress();
             trackPort = setTB.getPort();
            //db.updateSettingsTB(settingsTB);
        }else {
           //db.insertSettingsTB(settingsTB);
            trackIPAddress = "";
            trackPort = "";
        }




        btnSave =(Button)view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                if (txtIPAddress.getText().toString().trim().length() < 1) {

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Please enter the ip address");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                   // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                    //editText.getText().clear();
                                    txtIPAddress.requestFocus();
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();


                }else if(txtPort.getText().toString().trim().length() < 1){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setMessage("Please enter the port");
                    alertDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // Toast.makeText(MainActivity.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                                    //editText.getText().clear();
                                    txtPort.requestFocus();
                                }
                            });


                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else{

                    /*InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);*/
                    hideSoftKeyBoard();
                    Log.d("myTag", "This is my message rs" + db.getSettingsTBCount() );

                    SettingsTB settingsTB = new SettingsTB(1,"SERVER",txtIPAddress.getText().toString().trim(),txtPort.getText().toString().trim());

                    if (db.getSettingsTBCount() > 0){
                        db.updateSettingsTB(settingsTB);
                    }else {
                        db.insertSettingsTB(settingsTB);
                    }

                    if ((!txtIPAddress.getText().toString().equals(trackIPAddress) && !trackIPAddress.equals("")) || (!txtPort.getText().toString().equals(trackPort) && !trackPort.equals(""))){

                        trackIPAddress = txtIPAddress.getText().toString();
                        trackPort = txtPort.getText().toString();

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage("Successfully updated");
                        alertDialogBuilder.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        ((MainActivity) getActivity()).mLogout("SERVER_DETAILS");
                                    }
                                });


                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();

                    }else{
                        trackIPAddress = txtIPAddress.getText().toString();
                        trackPort = txtPort.getText().toString();

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                        alertDialogBuilder.setMessage("Successfully Saved");
                        alertDialogBuilder.setPositiveButton("OK",
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

        });

        btnClr =(Button)view.findViewById(R.id.btn_clear);
        btnClr.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("myTag", "This is my message rs" );
                //finish();
                txtIPAddress.getText().clear();
                txtPort.getText().clear();
            }

        });

        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
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
