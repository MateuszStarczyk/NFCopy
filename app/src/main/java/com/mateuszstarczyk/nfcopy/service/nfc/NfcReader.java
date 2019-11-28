package com.mateuszstarczyk.nfcopy.service.nfc;

import android.app.Activity;
import android.nfc.FormatException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.util.Log;

import java.io.IOException;

import static com.mateuszstarczyk.nfcopy.service.nfc.Const.*;

public class NfcReader {

    private Activity activity;
    private NfcAdapter.ReaderCallback nfcReaderCallback;

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

    public NfcMessage readTag(Tag tag) {
        tag.getId();
        NfcMessage nfcMessage = new NfcMessage();
        boolean isSuccess = true;
        for (String tech: tag.getTechList()) {
            switch (tech) {
                case MIFAREULTRALIGHT_NAME:
                    isSuccess = readMifareUltralight(tag, nfcMessage);
                    break;
                case MIFARECLASSIC_NAME:
                    isSuccess = readMifareClassic(tag, nfcMessage);
                    break;
                case NFCA_NAME:
                    NfcA nfcA = NfcA.get(tag);
                    nfcMessage.addMessage(NFCA_NAME)
                            .addMessage("Atqa: ")
                            .addMessage(byteToString(nfcA.getAtqa()));
                    break;
                case NFCB_NAME:
                    NfcB nfcB = NfcB.get(tag);
                    nfcMessage.addMessage(NFCB_NAME)
                            .addMessage("ApplicationData: ")
                            .addMessage(byteToString(nfcB.getApplicationData()))
                            .addMessage("ProtocolInfo: ")
                            .addMessage(byteToString(nfcB.getProtocolInfo()));
                    break;
                case NFCF_NAME:
                    NfcF nfcF = NfcF.get(tag);
                    nfcMessage.addMessage(NFCF_NAME)
                            .addMessage("Manufacturer: ")
                            .addMessage(convertHexToString(byteToString(nfcF.getManufacturer())))
                            .addMessage("SystemCode: ")
                            .addMessage(convertHexToString(byteToString(nfcF.getSystemCode())));
                    break;
                case NFCV_NAME:
                    NfcV nfcV = NfcV.get(tag);
                    nfcMessage.addMessage(NFCV_NAME)
                            .addMessage("ResponseFlags: ")
                            .addMessage(convertHexToString(String.format("%02X", nfcV.getResponseFlags())));
                    break;
                case NDEF_NAME:
                    Ndef ndef = Ndef.get(tag);
                    try {
                        ndef.connect();
                        nfcMessage.addMessage(NDEF_NAME)
                                .addMessage("ResponseFlags: ")
                                .addMessage(convertHexToString(ndef.getNdefMessage().toString()));
                    } catch (FormatException | IOException e) {
                        e.printStackTrace();
                        isSuccess = false;
                    }
                    break;
                case NDEFFORMATABLE_NAME:
                    break;
                case ISODEP_NAME:
                    IsoDep isoDep = IsoDep.get(tag);
                    isoDep.getHiLayerResponse();

                    nfcMessage.addMessage(ISODEP_NAME)
                            .addMessage("HiLayerResponse: ")
                            .addMessage(byteToString(isoDep.getHiLayerResponse()))
                            .addMessage("HistoricalBytes: ")
                            .addMessage(byteToString(isoDep.getHistoricalBytes()));
                    break;
            }
            if (!isSuccess)
                return null;
        }
        return nfcMessage;
    }

    private String convertHexToString(String hex){

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for( int i=0; i<hex.length()-1; i+=2 ){

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char)decimal);

            temp.append(decimal);
        }
        System.out.println("Decimal : " + temp.toString());

        return sb.toString();
    }

    private boolean readMifareClassic(Tag tag, NfcMessage nfcMessage) {
        MifareClassic mifare = MifareClassic.get(tag);
        try {
            mifare.connect();
            StringBuilder privateKeys = new StringBuilder();
            StringBuilder readSectors = new StringBuilder();
            StringBuilder readSectorsASCII = new StringBuilder();
            int numberOfSectors = mifare.getSectorCount();
            for(int i = 0; i < numberOfSectors; i++) {

                boolean isAuthenticated = false;

                if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)) {
                    privateKeys.append("Sector").append(i).append(": ").append(byteToString(MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY)).append("\n");
                    isAuthenticated = true;
                } else if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                    privateKeys.append("Sector").append(i).append(": ").append(byteToString(MifareClassic.KEY_DEFAULT)).append("\n");
                    isAuthenticated = true;
                } else if (mifare.authenticateSectorWithKeyA(i, MifareClassic.KEY_NFC_FORUM)) {
                    privateKeys.append("Sector").append(i).append(": ").append(byteToString(MifareClassic.KEY_NFC_FORUM)).append("\n");
                    isAuthenticated = true;
                } else {
                    privateKeys.append("Sector").append(i).append(": NOT STANDARD KEY").append("\n");
                    nfcMessage.setCopyable(false);
                }

                if (isAuthenticated) {
                    int block_index = mifare.sectorToBlock(i);
                    byte[] block = mifare.readBlock(block_index);
                    String sBlock = byteToString(block);
                    readSectors.append("Sector").append(i).append(": ").append(sBlock).append("\n");
                    readSectorsASCII.append("Sector").append(i).append(": ")
                            .append(convertHexToString(sBlock)).append("\n");
                }
            }

            nfcMessage.addKeys(MIFARECLASSIC_NAME)
                    .addKeys("\n")
                    .addKeys(privateKeys.toString());
            nfcMessage.addSectors(MIFARECLASSIC_NAME)
                    .addSectors("\n")
                    .addSectors(readSectors.toString());
            nfcMessage.addSectorsASCII(MIFARECLASSIC_NAME)
                    .addSectorsASCII("\n")
                    .addSectorsASCII(readSectorsASCII.toString());

        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
        return true;
    }

    private boolean readMifareUltralight(Tag tag, NfcMessage nfcMessage) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        try {
            mifare.connect();
            StringBuilder readPages = new StringBuilder();
            StringBuilder readPagesASCII = new StringBuilder();
            int numberOfSectors = 0;
            if(mifare.getType() == MifareUltralight.TYPE_ULTRALIGHT)
                numberOfSectors = 16;
            else if (mifare.getType() == MifareUltralight.TYPE_ULTRALIGHT_C)
                numberOfSectors = 48;
            for(int i = 0; i < numberOfSectors; i++) {
                byte[] block = mifare.readPages(i);
                String sBlock = byteToString(block);
                readPages.append("Page").append(i).append(": ").append(sBlock).append("\n");
                readPagesASCII.append("Page").append(i).append(": ")
                        .append(convertHexToString(sBlock)).append("\n");

            }
            nfcMessage.addMessage("\n")
                    .addMessage(MIFAREULTRALIGHT_NAME)
                    .addMessage("\nPages:\n")
                    .addMessage(readPages.toString());
            nfcMessage.addMessage("Pages (ASCII):\n")
                    .addMessage(readPagesASCII.toString());

        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
        return true;
    }

    public static String byteToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        if (bytes != null)
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }

        return sb.toString();
    }
}
