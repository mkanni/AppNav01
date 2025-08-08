package com.franco.epos.appnav01;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;



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
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class RegisterCk extends AsyncTask<Void, String, Boolean> {
    public static String Task_Status = null;
    boolean parser_complete;
    private static String WSDL_TARGET_NAMESPACE;
    private static String SOAP_ADDRESS;
    public static String task_Loyality =  "https://sandbox.happibe.com/New_SBeiKard/WSV5/WSBusiness_loyality.php?wsdl";
    private static String SOAP_ACTION = "urn:server#getProfiler";
    private static String OPERATION_NAME = "getProfiler";
    public static Context xml_con;
    SoapSerializationEnvelope envelope;
    String response, webservice_resp;

    public RegisterCk() {
        // Keep reference to resources
        // Initialise initial pre-execute message
        SOAP_ACTION = "urn:WSBusiness_loyality#getProfiler";
        OPERATION_NAME = "getProfiler";
        WSDL_TARGET_NAMESPACE = task_Loyality;
        SOAP_ADDRESS = task_Loyality;
       // xml_con = online_context;



    }

    /* UI Thread */
    @Override
    protected void onCancelled() {
        // Detach from progress tracker
        /*
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }

         */
    }

    /* UI Thread */
    @Override
    protected void onProgressUpdate(String... values) {
        // Update progress message

    }
    @Override
    protected void onPostExecute(Boolean result) {

    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        try {

            SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
            SoapObject parameters = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);

            parameters.addProperty("UCID", "76868996768");



            request.addProperty(OPERATION_NAME, parameters);
            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.setOutputSoapObject(request);

            HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);
            httpTransport.call(SOAP_ACTION, envelope);
            response = (String) envelope.getResponse();
            System.out.println("output value before replacing &:  " + response);
            /*
            AESCrypt AesEncp = new AESCrypt(bus_ucid);
            response = AesEncp.decrypt(response);

             */
            response = response.replaceAll("&", "&amp;");
            //response = response.replaceAll("&#039;", "\'");
            // System.out.println("~~ output value chaceck 2nd sep. " + response);
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
                System.out.println("correct: " + response);
            }

        } catch (Exception e) {
            System.out.println("Exception in parsing:" + e);
            Task_Status = "unknown exception";
        } catch (NoClassDefFoundError e) {
            System.out.println("No class found Exception in parsing:" + e);
            Task_Status = "unknown exception";
        }

        // This return causes onPostExecute call on UI thread
        parser_complete = true;

        return null;
    }


}
