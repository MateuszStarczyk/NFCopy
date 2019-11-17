package com.mateuszstarczyk.nfcopy.service.nfc;

public class NfcCard {
    private String UID;
    private String name;
    private String className;
    private String imagePath;

    public NfcCard(String UID, String name, String className, String imagePath) {
        this.UID = UID;
        this.name = name;
        this.className = className;
        this.imagePath = imagePath;
    }
    public String getUID() {
        return UID;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getImagePath() {
        return imagePath;
    }
}
