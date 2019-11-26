package com.mateuszstarczyk.nfcopy.service.nfc;

import android.nfc.NfcAdapter;

final class Const {

    static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B |
            NfcAdapter.FLAG_READER_NFC_BARCODE | NfcAdapter.FLAG_READER_NFC_F |
            NfcAdapter.FLAG_READER_NFC_V;
    static String NFC_ADAPTER = "Nfc adapter";


    static final String MIFAREULTRALIGHT_NAME = "android.nfc.tech.MifareUltralight";
    static final String MIFARECLASSIC_NAME = "android.nfc.tech.MifareClassic";
    static final String NFCA_NAME = "android.nfc.tech.NfcA";
    static final String NFCB_NAME = "android.nfc.tech.NfcB";
    static final String NFCF_NAME = "android.nfc.tech.NfcF";
    static final String NFCV_NAME = "android.nfc.tech.NfcV";
    static final String NDEF_NAME = "android.nfc.tech.Ndef";
    static final String NDEFFORMATABLE_NAME = "android.nfc.tech.NdefFormatable";
    static final String ISODEP_NAME = "android.nfc.tech.IsoDep";
}
