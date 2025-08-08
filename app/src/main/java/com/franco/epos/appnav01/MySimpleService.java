package com.franco.epos.appnav01;

import static android.app.PendingIntent.getActivity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ResultReceiver;


import com.franco.epos.appnav01.database.DatabaseHelper;
import com.franco.epos.appnav01.database.model.ItemUploadTB;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;



import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class MySimpleService  extends IntentService {
    private SharedPreferences pref;
    public static String Task_Status = null;
    boolean parser_complete;
    private static String WSDL_TARGET_NAMESPACE;
    private static String SOAP_ADDRESS;
    public static String task_Loyality =  "http://ws.benait.co.uk/ePosAndroidActivationckNew.php?wsdl";
    private static String SOAP_ACTION = "urn:server#getProfiler";
    private static String OPERATION_NAME = "getProfiler";
    public static Context xml_con;
    SoapSerializationEnvelope envelope;
    String response, webservice_resp;
    DatabaseHelper db;


    private static String WSDL_TARGET_NAMESPACEIU;
    private static String SOAP_ADDRESSIU;
    public static String task_LoyalityIU =  "http://ws.benait.co.uk/ePosAndroidPushItemproductNew.php?wsdl";
    private static String SOAP_ACTIONIU = "urn:server#getProfiler";
    private static String OPERATION_NAMEIU = "getProfiler";
    public static Context xml_conIU;
    SoapSerializationEnvelope envelopeIU;
    String responseIU, webservice_respIU;


    public MySimpleService() {
        super("MySimpleService");
        System.out.println("On start 02 service database Access1");

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SOAP_ACTION = "urn:ePosAndroidActivationckNew#getProfiler";
        OPERATION_NAME = "getProfiler";
        WSDL_TARGET_NAMESPACE = task_Loyality;
        SOAP_ADDRESS = task_Loyality;
        System.out.println("On start 02 service database Access1 " + intent.getStringExtra("PCID"));
        db = new DatabaseHelper(getApplicationContext());

        try {



            if(isNetworkAvailable()){
                SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
                SoapObject parameters = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);

                parameters.addProperty("PCID", intent.getStringExtra("PCID"));



                request.addProperty(OPERATION_NAME, parameters);
                envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                envelope.setOutputSoapObject(request);

                HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
                httpTransport.call(SOAP_ACTION, envelope);
                response = (String) envelope.getResponse();
                System.out.println("output value before replacing &:  " + response);

                response = response.replaceAll("&", "&amp;");

                DocumentBuilderFactory docFactory = null;
                DocumentBuilder docBuilder = null;
                Document document = null;
                docFactory = DocumentBuilderFactory.newInstance();
                docBuilder = docFactory.newDocumentBuilder();
                document = docBuilder.parse(new InputSource(new StringReader(response)));
                Node result = document.getFirstChild();
                Node message_type = result.getFirstChild();
                webservice_resp = message_type.getTextContent();
                if (webservice_resp.equalsIgnoreCase("OK")) {

                    SOAP_ACTIONIU = "urn:ePosAndroidPushItemproductNew#getProfiler";
                    OPERATION_NAMEIU = "getProfiler";
                    WSDL_TARGET_NAMESPACEIU = task_LoyalityIU;
                    SOAP_ADDRESSIU = task_LoyalityIU;



                    List<ItemUploadTB> itemsP = db.getAllItemUploadTB();
                    if (itemsP.size() > 0){


                        for (int j=0; j<itemsP.size();j++){
                            // Log.d("myTag", "This is my message rss" + itemsP.get(j).getItem_lookup());

                            SoapObject requestIU = new SoapObject(WSDL_TARGET_NAMESPACEIU, OPERATION_NAMEIU);
                            SoapObject parametersIU = new SoapObject(WSDL_TARGET_NAMESPACEIU, OPERATION_NAMEIU);

                            parametersIU.addProperty("TILLID", intent.getStringExtra("PCID"));
                            parametersIU.addProperty("ITEMLOOKUP", itemsP.get(j).getItem_lookup());
                            parametersIU.addProperty("ITEM_DESCR", itemsP.get(j).getDescr());
                            parametersIU.addProperty("ITEM_BARCODE", itemsP.get(j).getBarcode());
                            parametersIU.addProperty("ITEM_VAT", itemsP.get(j).getVat());
                            parametersIU.addProperty("ITEM_PRICE", itemsP.get(j).getPrice());
                            parametersIU.addProperty("ITEM_COST", itemsP.get(j).getCost());
                            parametersIU.addProperty("DEPT_DESCR", itemsP.get(j).getDep_descr());
                            parametersIU.addProperty("DEPT_GROUPS", itemsP.get(j).getDep_groups());
                            parametersIU.addProperty("DEPT_AGE_CHECK", itemsP.get(j).getDep_age_check());
                            parametersIU.addProperty("DEPT_COMM", itemsP.get(j).getDep_commission());
                            parametersIU.addProperty("DEPT_VAT", itemsP.get(j).getDep_vat());
                            parametersIU.addProperty("CATE_DESCR", itemsP.get(j).getCat_descr());
                            parametersIU.addProperty("CATE_VAT", itemsP.get(j).getCat_vat());
                            parametersIU.addProperty("ACTION", itemsP.get(j).getAction());



                            requestIU.addProperty(OPERATION_NAMEIU, parametersIU);
                            envelopeIU = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                            envelopeIU.setOutputSoapObject(requestIU);

                            HttpTransportSE httpTransportIU = new HttpTransportSE(SOAP_ADDRESSIU);
                            httpTransportIU.call(SOAP_ACTIONIU, envelopeIU);
                            responseIU = (String) envelopeIU.getResponse();
                            System.out.println("output value before replacing &:  " + responseIU);

                            responseIU = responseIU.replaceAll("&", "&amp;");

                            DocumentBuilderFactory docFactoryIU = null;
                            DocumentBuilder docBuilderIU = null;
                            Document documentIU = null;
                            docFactoryIU = DocumentBuilderFactory.newInstance();
                            docBuilderIU = docFactoryIU.newDocumentBuilder();
                            documentIU = docBuilderIU.parse(new InputSource(new StringReader(responseIU)));
                            Node resultIU = documentIU.getFirstChild();
                            Node message_typeIU = resultIU.getFirstChild();
                            webservice_respIU = message_typeIU.getTextContent();
                            if (webservice_respIU.equalsIgnoreCase("OK")) {
                                db.deleteItemUploadTBID(itemsP.get(j));
                            }




                            System.out.println("Inserted details myservic details : "  + itemsP.get(j).getId() + itemsP.get(j).getItem_lookup() + itemsP.get(j).getDep_descr() + itemsP.get(j).getPrice());

                        }





                    }

                    System.out.println("correct: " + response);
                    ResultReceiver rec = intent.getParcelableExtra("receiverTag");
                    Bundle b= new Bundle();
                    b.putString("ServiceTag","ACTIVE");
                    rec.send(0, b);

                }else{






                    //((MainActivity) getActivity()).mLogout("SERVER_DETAILS");
                    //MainActivity.ServiceCallbacks();
                    ResultReceiver rec = intent.getParcelableExtra("receiverTag");
                    //String recName= intent.getStringExtra("PCID");
                    //Log.d("sohail","received name="+recName);

                    // Log.d("sohail","sending data back to activity");

                    Bundle b= new Bundle();
                    b.putString("ServiceTag","INACTIVE");
                    //b.putString("ServiceTag","ACTIVE");
                    rec.send(0, b);
                }

            }else{
                String exprDate = intent.getStringExtra("EXPDATE");



                String[] separatedDT = exprDate.split("-");

                String serDateTime = separatedDT[0]+"-"+separatedDT[1]+"-"+separatedDT[1]+"T00:00:00";
                LocalDateTime serLdatetime = LocalDateTime.parse(serDateTime);



                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                System.out.println("generated LocalDaddteTime: " + currentDate+"T"+currentTime);
                System.out.println("generateddddd LocalDateTime: " + serLdatetime);
                String locDateTime = currentDate+"T"+currentTime;
                LocalDateTime locLdatetime = LocalDateTime.parse(locDateTime);

                System.out.println("generateddddd LocalDateTime: " + serLdatetime +" ::: "+locLdatetime);
                if(locLdatetime.isAfter(serLdatetime)){
                    System.out.println("After: " + serLdatetime +" ::: "+locLdatetime +" Expired");

                    ResultReceiver rec = intent.getParcelableExtra("receiverTag");
                    Bundle b= new Bundle();
                    b.putString("ServiceTag","INACTIVE");
                   // b.putString("ServiceTag","ACTIVE");
                    rec.send(0, b);

                }else{
                    ResultReceiver rec = intent.getParcelableExtra("receiverTag");
                    Bundle b= new Bundle();
                    b.putString("ServiceTag","ACTIVE");
                    rec.send(0, b);


                }
            }



        } catch (Exception e) {
            System.out.println("Exception in parsing:" + e);
            Task_Status = "unknown exception";
        } catch (NoClassDefFoundError e) {
            System.out.println("No class found Exception in parsing:" + e);
            Task_Status = "unknown exception";
        }
    }



    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
