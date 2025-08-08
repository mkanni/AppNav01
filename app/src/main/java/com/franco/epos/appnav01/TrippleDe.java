package com.franco.epos.appnav01;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
public class TrippleDe {


    //    public static String ALGO = "DESede/CBC/PKCS7Padding";
   // public static String ALGO = "DESede/ECB/PKCS7Padding";
    //public static String ALGO = "DESede/ECB/PKCS5Padding";
    //public static String ALGO = "DESede/CBC/NoPadding";

    //public static String ALGO = "AES/ECB/NoPadding";
    //public static String ALGO = "AES/ECB/PKCS7PADDING";
    public static String ALGO = "DES/CBC/PKCS5Padding";



    public static String _encrypt(String message, String secretKey) throws Exception {

        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, getSecreteKey(secretKey));

        byte[] plainTextBytes = message.getBytes("UTF-8");
        byte[] buf = cipher.doFinal(plainTextBytes);
        byte[] base64Bytes = Base64.encode(buf, Base64.DEFAULT);
        String base64EncryptedString = new String(base64Bytes);
        return base64EncryptedString;
    }

    public static String _decrypt(String encryptedText, String secretKey) throws Exception {

        byte[] message = Base64.decode(encryptedText.getBytes(), Base64.DEFAULT);

        Cipher decipher = Cipher.getInstance(ALGO);
       // Cipher decipher = Cipher.getInstance("");
        decipher.init(Cipher.DECRYPT_MODE, getSecreteKey(secretKey));

        byte[] plainText = decipher.doFinal(message);

        return new String(plainText, "UTF-8");
    }

    public static SecretKey getSecreteKey(String secretKey) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
        byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
        SecretKey key = new SecretKeySpec(keyBytes, "DES");
        return key;
    }
}
