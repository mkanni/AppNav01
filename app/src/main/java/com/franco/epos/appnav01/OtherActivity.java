package com.franco.epos.appnav01;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.SettingsTB;

public class OtherActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SharedPreferences pref;
    private String ipaddress, port;
    public static String CURRENT_TAG = "";
    public static String CURRENT_AVAL = "";
    final Context context = this;

    private String ActType, ActFrom, itmLU, ActFromSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitvity_other);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = new DatabaseHelper(this);
        pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode




        if (db.getSettingsTBCount() > 0){

            SettingsTB setTb = db.getSetTBByType("SERVER");
            if (setTb != null){
                ipaddress = setTb.getIpaddress();
                port = setTb.getPort();
            }

        }else {
            //db.insertSettingsTB(settingsTB);
            ipaddress = "";
            port = "";
        }



        Log.d("myTag", "This is my message rs" + db.getSettingsTBCount() );

        Bundle bundle = new Bundle();
        Fragment fragment = null;

        System.out.println("The value ActType : " + getIntent().getStringExtra("ActType") + " ActFrom" + getIntent().getStringExtra("ActFrom") + " itmLU" + getIntent().getStringExtra("itmLU"));

        ActType = getIntent().getStringExtra("ActType");
        ActFrom = getIntent().getStringExtra("ActFrom");
        itmLU = getIntent().getStringExtra("itmLU");
        ActFromSub = getIntent().getStringExtra("ActFromSub");
        if (ActFrom.equals("Update")) {
            if (ActFromSub.equals("PackQty")) {
                fragment = new PackQtyFragment();
                bundle.putString("PageName", ActFrom);
                bundle.putString("ActType", ActType);
                bundle.putString("itemLU", itmLU);
                CURRENT_AVAL = "Pack Update";
            }else{
                fragment = new QuickAddFragment();
                bundle.putString("PageName", ActFrom);
                bundle.putString("ActType", ActType);
                bundle.putString("itemLU", itmLU);
                CURRENT_AVAL = "Quick Add";
            }


        }else if(ActFrom.equals("PriceChange")){
            if (ActFromSub.equals("PackQty")) {
                fragment = new PackQtyFragment();
                bundle.putString("PageName", ActFrom);
                bundle.putString("ActType", ActType);
                bundle.putString("itemLU", itmLU);
                CURRENT_AVAL = "Pack Update";
            }else{
                fragment = new QuickAddFragment();
                bundle.putString("PageName", ActFrom);
                bundle.putString("ActType", ActType);
                bundle.putString("itemLU", itmLU);
                CURRENT_AVAL = "Quick Add";
            }


        }else{

            fragment = new PriceChangeFragment();
            bundle.putString("PageName", "Other");
            bundle.putString("ActType", "Price change");
            CURRENT_AVAL = "Price change";


        }


        changeActionBarTitle(CURRENT_AVAL);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
        fragment.setArguments(bundle);
        fragmentTransaction.commitAllowingStateLoss();

    }

    public void changeActionBarTitle(String val){

        String titleVal = "Franco ePos";

        if (val.equals("Quick Add")) {
            titleVal = "Quick Add";
        }else if(val.equals("Price change")){
            titleVal = "Price change";
        }else if(val.equals("Pack Update")){
            titleVal = "Pack Update";

        }else {

        }

        getSupportActionBar().setTitle(titleVal);

    }
}
