package com.mateuszstarczyk.nfcopy.be.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import static com.mateuszstarczyk.nfcopy.be.nfc.Const.NFC_ADAPTER;
import static com.mateuszstarczyk.nfcopy.be.nfc.Const.READER_FLAGS;

public class NfcReader {

    private Activity activity;
    private NfcAdapter.ReaderCallback nfcReaderCallback;

    public NfcReader(Activity activity, NfcAdapter.ReaderCallback nfcReaderCallback) {
        this.activity = activity;
        this.nfcReaderCallback = nfcReaderCallback;
    }

    public void enableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        final Tag tag;
        if (nfc != null) {
            if (!nfc.isEnabled()){
                nfc.enableReaderMode(activity, nfcReaderCallback, READER_FLAGS, null);
                Log.i("INFO", NFC_ADAPTER + " enabled.");
            }
            else {
                Log.i("INFO", NFC_ADAPTER + " was enabled.");
            }
        } else {
            Log.i("ERROR", NFC_ADAPTER + " not working!");
        }
    }

    public void disableReaderMode() {
        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
        if (nfc != null) {
            if (nfc.isEnabled()){
                 nfc.disableReaderMode(activity);
                Log.i("INFO", NFC_ADAPTER + " disabled.");

            } else
                Log.i("INFO", NFC_ADAPTER + " was disabled.");
        } else {
            Log.i("ERROR", NFC_ADAPTER + " not working!");
        }
    }
}
