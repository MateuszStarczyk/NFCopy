package com.mateuszstarczyk.nfcopy.service.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.mateuszstarczyk.nfcopy.service.nfc.Const.NFC_ADAPTER;
import static com.mateuszstarczyk.nfcopy.service.nfc.Const.READER_FLAGS;

public class NfcReader {

    private Activity activity;
    private NfcAdapter.ReaderCallback nfcReaderCallback;
    public static final String MIFAREULTRALIGHT_NAME = "android.nfc.tech.MifareUltralight";
    public static final String MIFARECLASSIC_NAME = "android.nfc.tech.MifareClassic";
    public static final String NFCA_NAME = "android.nfc.tech.NfcA";
    public static final String NFCB_NAME = "android.nfc.tech.NfcB";
    public static final String NFCF_NAME = "android.nfc.tech.NfcF";
    public static final String NFCV_NAME = "android.nfc.tech.NfcV";
    public static final String NDEF_NAME = "android.nfc.tech.Ndef";
    public static final String NDEFFORMATABLE_NAME = "android.nfc.tech.NdefFormatable";
    public static final String ISODEP_NAME = "android.nfc.tech.IsoDep";

    public NfcReader(Activity activity, NfcAdapter.ReaderCallback nfcReaderCallback) {
        this.activity = activity;
        this.nfcReaderCallback = nfcReaderCallback;
    }

    public void enableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.enableReaderMode(activity, nfcReaderCallback, READER_FLAGS, null);
            Log.i("INFO", NFC_ADAPTER + " enabled.");
        } else {
            Log.i("ERROR", NFC_ADAPTER + " not working!");
        }
    }

    public void disableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            nfc.disableReaderMode(activity);
            Log.i("INFO", NFC_ADAPTER + " disabled.");
        } else {
            Log.i("ERROR", NFC_ADAPTER + " not working!");
        }
    }

    public String readTag(Tag tag) {
        tag.getId();
        StringBuilder message = new StringBuilder();
        String temp = "";
        for (String tech: tag.getTechList()) {
            switch (tech) {
                case MIFAREULTRALIGHT_NAME:
                    temp = "android.nfc.tech.MifareUltralight";
                    break;
                case MIFARECLASSIC_NAME:
                    temp = readMifareClassic(tag);
                    break;
                case NFCA_NAME:
                    temp = "android.nfc.tech.NfcA";
                    break;
                case NFCB_NAME:
                    temp = "android.nfc.tech.NfcB";
                    break;
                case NFCF_NAME:
                    temp = "android.nfc.tech.NfcF";
                    break;
                case NFCV_NAME:
                    temp = "android.nfc.tech.NfcV";
                    break;
                case NDEF_NAME:
                    temp = "android.nfc.tech.Ndef";
                    break;
                case NDEFFORMATABLE_NAME:
                    temp = "android.nfc.tech.NdefFormatable";
                    break;
                case ISODEP_NAME:
                    temp = "android.nfc.tech.IsoDep";
                    break;
            }
            if (temp.equals("Failed to read tag!"))
                return temp;
            else
                message.append("\n").append(temp);
        }
        return message.toString();
    }

    public String readMifareClassic(Tag tag) {
        String message = "Type: MifareClassic\n";
        MifareClassic mifare = MifareClassic.get(tag);
        try {
            mifare.connect();
            boolean isAllRead = true;
            byte[] privateKey = new byte[0];
            int numberOfSectors = mifare.getSectorCount();
            for(int i=0; i< numberOfSectors; i++) {

                boolean isAuthenticated = false;

                if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                    privateKey = MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY;
                    isAuthenticated = true;
                } else if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                    privateKey = MifareClassic.KEY_DEFAULT;
                    isAuthenticated = true;
                } else if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_NFC_FORUM)) {
                    privateKey = MifareClassic.KEY_NFC_FORUM;
                    isAuthenticated = true;
                } else {
                    isAllRead = false;
                }

                if (isAuthenticated) {
                    int block_index = mifare.sectorToBlock(i);
                    byte[] block = mifare.readBlock(block_index);
                    String s_block = new String(block, Charset.forName("US-ASCII"));
                    Log.d("INFO", s_block);
                }
            }
            if (isAllRead)
                message += "Read with key: " + byteToString(privateKey) + "\nCard can be copied";
            else
                message += "Card is protected with private key\nCan't copy all information";

        } catch (IOException e) {
            message = "Failed to read tag!";
        } finally {
            if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException e) {
                    Log.e("ERROR", "Error closing tag...", e);
                }
            }
        }
        return message;
    }
    public static String byteToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }
}
