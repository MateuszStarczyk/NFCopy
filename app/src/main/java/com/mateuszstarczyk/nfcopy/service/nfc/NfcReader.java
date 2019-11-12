package com.mateuszstarczyk.nfcopy.service.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.mateuszstarczyk.nfcopy.service.nfc.Const.NFC_ADAPTER;
import static com.mateuszstarczyk.nfcopy.service.nfc.Const.READER_FLAGS;

public class NfcReader {

    private Activity activity;
    private NfcAdapter.ReaderCallback nfcReaderCallback;

    public NfcReader(Activity activity) {
        this.activity = activity;

        this.nfcReaderCallback = new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                Log.i("INFO", tag.toString());
            }
        };
    }

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
        MifareClassic mifare = MifareClassic.get(tag);
        try {
            mifare.connect();
//            int numberOfBlocks = mifare.getBlockCount(); //64
//            int sizeOfTag = mifare.getSize(); //1024 SIZE_1K
            int numberOfSectors = mifare.getSectorCount();
//            byte[] payload = mifare.readBlock(0); //16
            for(int i=0; i< numberOfSectors; i++) {

                boolean isAuthenticated = false;

                if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                    isAuthenticated = true;
                } else if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                    isAuthenticated = true;
                } else if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_NFC_FORUM)) {
                    isAuthenticated = true;
                } else {
                    Log.d("ERROR", "Authorization denied ");
                }

                if (isAuthenticated) {
                    int block_index = mifare.sectorToBlock(i);
                    byte[] block = mifare.readBlock(block_index);
                    String s_block = new String(block, Charset.forName("US-ASCII"));
                    Log.d("INFO", s_block);
                }
            }
            return "";
        } catch (IOException e) {
            Log.e("ERROR", "IOException while reading MifareUltralight message...", e);
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
        return null;
    }

    public static String byteToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        return sb.toString();
    }

    public static byte[] stringToByte(String s) {
        return s.getBytes();
    }
}
