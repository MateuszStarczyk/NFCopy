package com.mateuszstarczyk.nfcopy.service.nfc;

import android.widget.ImageView;

public class NfcCard {
    private String UID;
    private String name;
    private String className;
    private transient ImageView image;

    public NfcCard(String UID, String name, String className, ImageView image) {
        this.UID = UID;
        this.name = name;
        this.className = className;
        this.image = image;
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

    public ImageView getImage() {
        return image;
    }
}
