package com.mateuszstarczyk.nfcopy.service.nfc;

import android.nfc.NfcAdapter;

final class Const {

    static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_V | NfcAdapter.FLAG_READER_NFC_B |
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    static String NFC_ADAPTER = "Nfc adapter";
}
