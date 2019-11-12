package com.mateuszstarczyk.nfcopy.service.nfc;

import android.graphics.Bitmap;
import android.widget.ImageView;

import static com.mateuszstarczyk.nfcopy.service.ImageService.bitMapToString;
import static com.mateuszstarczyk.nfcopy.service.ImageService.ivToBitmap;
import static com.mateuszstarczyk.nfcopy.service.ImageService.stringToBitMap;

public class NfcCard {
    private String UID;
    private String name;
    private String className;
    private String bitmap;
    private byte atqa;
    private byte sak;
    private byte[] hist;
    private byte[] uid;

    public NfcCard(String UID, String name, String className, ImageView image) {
        this.UID = UID;
        this.name = name;
        this.className = className;
        this.bitmap = bitMapToString(ivToBitmap(image));
    }

    public NfcCard(String UID, String name, String className, Bitmap bitmap) {
        this.UID = UID;
        this.name = name;
        this.className = className;
        this.bitmap = bitMapToString(bitmap);
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

    public Bitmap getBitmap() {
        return stringToBitMap(bitmap);
    }
}
