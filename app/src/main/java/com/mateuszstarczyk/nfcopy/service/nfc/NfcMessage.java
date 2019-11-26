package com.mateuszstarczyk.nfcopy.service.nfc;

import android.content.Context;

import androidx.annotation.NonNull;

import com.mateuszstarczyk.nfcopy.R;

public class NfcMessage {
    private String message;
    private String sectors;
    private String sectorsASCII;
    private String keys;
    private boolean isCopyable;

    NfcMessage() {
        message = "";
        sectors = "";
        sectorsASCII = "";
        keys = "";
        isCopyable = true;
    }

    public String getPopUpMessage(Context context) {
        return isCopyable?context.getText(R.string.card_can_be_copied).toString()
                :context.getText(R.string.card_cannot_be_copied).toString();
    }

    String getMessage() {
        return message;
    }

    NfcMessage addMessage(String message) {
        this.message += "\n" +  message;
        return this;
    }

    public String getSectors() {
        return sectors;
    }

    NfcMessage addSectors(String sectors) {
        this.sectors += sectors;
        return this;
    }

    public String getSectorsASCII() {
        return sectorsASCII;
    }

    NfcMessage addSectorsASCII(String sectorsASCII) {
        this.sectorsASCII += sectorsASCII;
        return this;
    }

    public String getKeys() {
        return keys;
    }

    NfcMessage addKeys(String keys) {
        this.keys += keys;
        return this;
    }

    public boolean isCopyable() {
        return isCopyable;
    }

    void setCopyable(boolean copyable) {
        isCopyable = copyable;
    }

    @NonNull
    @Override
    public String toString() {
        return "Card sectors(HEX):\n" + sectors + '\n' +
               "Card sectors(ASCII):\n" + sectorsASCII + '\n' +
               "Keys:\n" + keys;
    }
}
