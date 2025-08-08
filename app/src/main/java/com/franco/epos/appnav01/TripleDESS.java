package com.franco.epos.appnav01;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TripleDESS {

    //NOTES: KEY MUST BE 32 OR 48 CHAR.
    //IT CAN BE 16, BUT REPEAT FIRST 16 TO MAKE IT 32.
    private static final String KEY="jN3456!S9Dco";
    private static final String ALGORITHM = "DESede";
    private static final String CIPHER_PARAMETERS = "DESede/CBC/PKCS5Padding";

    private static final byte[] HEX_EIGHT_ZEROS = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
    private static final char[] HEXTAB = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    private static final byte[] KEY_BYTES = new byte[24];
    private static final IvParameterSpec ivSpec1 = new IvParameterSpec(HEX_EIGHT_ZEROS);

    private static String strFinalKey = KEY;


    static {
        if (strFinalKey.length() == 32) {
            strFinalKey = strFinalKey.concat(strFinalKey.substring(0, 16));
        }
    }
    private static final String KEY_LOWER = strFinalKey.toLowerCase();

    static{
        init();
    }

    public TripleDESS() {
    }

    private static void init(){
        TripleDESS objSelf = new TripleDESS();
        objSelf.binHexToBytes(KEY_LOWER, KEY_BYTES, 0, KEY_BYTES.length);
    }

    public String encrypt(String strNormalPassword) {
        String strEncryptedPassword = null;
        byte[] bytesFormatOfPassword = strNormalPassword.getBytes();
        try {
            if (binHexToBytes(KEY_LOWER, KEY_BYTES, 0, KEY_BYTES.length) != (KEY_BYTES.length)) {
            }
            SecretKey desEdeKey = new SecretKeySpec(KEY_BYTES, ALGORITHM);

            Cipher desEdeCipher = Cipher.getInstance(CIPHER_PARAMETERS);
            desEdeCipher.init(Cipher.ENCRYPT_MODE, desEdeKey, ivSpec1);
            byte[] cipherText = desEdeCipher.doFinal(bytesFormatOfPassword);
            strEncryptedPassword = bytesToBinHex(cipherText, 0, cipherText.length);
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
        } catch (Exception ae) {
            ae.printStackTrace();
        }
        return strEncryptedPassword;
    }

    public String decrypt(String strEncryptedPassword){
        String strPassLowerCase = strEncryptedPassword.toLowerCase();
        String strDecryptedPassword = null;
        byte[] cipherBytes = new byte[strPassLowerCase.length() / 2];
        try {
            int intBinToHaxByteCount = binHexToBytes(strPassLowerCase, cipherBytes, 0, strPassLowerCase.length() / 2);
            if (intBinToHaxByteCount != (strEncryptedPassword.length() / 2)) {

            }
            SecretKey desEdeKey = new SecretKeySpec(KEY_BYTES, ALGORITHM);

            Cipher desEdeCipher = Cipher.getInstance(CIPHER_PARAMETERS);
            desEdeCipher.init(Cipher.DECRYPT_MODE, desEdeKey, ivSpec1);
            byte[] plainText = desEdeCipher.doFinal(cipherBytes);
            strDecryptedPassword = new String(plainText, 0, plainText.length);
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
        } catch (Exception ae) {
            ae.printStackTrace();
        }
        return strDecryptedPassword;
    }

    private int binHexToBytes(String sBinHex, byte[] data, int nSrcPos, int nNumOfBytes) {
        // Dest pos set to zero.
        int nDstPos = 0;
        // check for correct ranges
        int nStrLen = sBinHex.length();
        int nAvailBytes = (nStrLen - nSrcPos) >> 1;
        if (nAvailBytes < nNumOfBytes) {
            nNumOfBytes = nAvailBytes;
        }
        int nOutputCapacity = data.length;
        if (nNumOfBytes > nOutputCapacity) {
            nNumOfBytes = nOutputCapacity;
        }
        // convert now
        int nResult = 0;
        for (int nI = 0; nI < nNumOfBytes; nI++) {
            byte bActByte = 0;
            boolean blConvertOK = true;
            for (int nJ = 0; nJ < 2; nJ++) {
                bActByte <<= 4;
                char cActChar = sBinHex.charAt(nSrcPos++);

                if ((cActChar >= 'a') && (cActChar <= 'f')) {
                    bActByte |= (byte) (cActChar - 'a') + 10;
                } else if ((cActChar >= 'A') && (cActChar <= 'F')) {
                    bActByte |= (byte) (cActChar - 'A') + 10;
                } else {
                    if ((cActChar >= '0') && (cActChar <= '9')) {
                        bActByte |= (byte) (cActChar - '0');
                    } else {
                        blConvertOK = false;
                    }
                }
            }
            if (blConvertOK) {
                data[nDstPos++] = bActByte;
                nResult++;
            }
        }
        return nResult;
    }

    private String bytesToBinHex(byte[] data, int nStartPos, int nNumOfBytes) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.setLength(nNumOfBytes << 1);

        int nPos = 0;
        for (int nI = 0; nI < nNumOfBytes; nI++) {
            sbuf.setCharAt(nPos++, HEXTAB[(data[nI + nStartPos] >> 4) & 0x0f]);
            sbuf.setCharAt(nPos++, HEXTAB[data[nI + nStartPos] & 0x0f]);
        }
        return sbuf.toString();
    }
}
