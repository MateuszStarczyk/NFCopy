package com.mateuszstarczyk.nfcopy.be.nfc;

import android.nfc.NfcAdapter;

public final class Const {

    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_V | NfcAdapter.FLAG_READER_NFC_B |
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    public static String NFC_ADAPTER = "Nfc adapter";
}
