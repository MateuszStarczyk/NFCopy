package com.mateuszstarczyk.nfcopy.service.nfc;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class NfcCard {
    private String UID;
    private String name;
    private String[] techList;
    private String imagePath;
    private NfcMessage nfcMessage;

    public NfcCard(String UID, String name, String[] techList, String imagePath, NfcMessage nfcMessage) {
        this.UID = UID;
        this.name = name;
        this.techList = techList;
        this.imagePath = imagePath;
        this.nfcMessage = nfcMessage;
    }

    public String getUID() {
        return UID;
    }

    public String getName() {
        return name;
    }

    public String[] getTechList() {
        return techList;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTechList(String[] techList) {
        this.techList = techList;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public NfcMessage getNfcMessage() {
        return nfcMessage;
    }

    public void setNfcMessage(NfcMessage nfcMessage) {
        this.nfcMessage = nfcMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return "UUID: " + UID + "\n\n" +
                "Name: " + name + "\n\n" +
                "TechList: " + Arrays.toString(techList) + "\n\n" +
                nfcMessage.toString() + "\n\n" +
                nfcMessage.getMessage();
    }
}
