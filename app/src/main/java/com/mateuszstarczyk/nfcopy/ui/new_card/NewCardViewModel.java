package com.mateuszstarczyk.nfcopy.ui.new_card;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mateuszstarczyk.nfcopy.service.nfc.NfcCard;

public class NewCardViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private boolean isEditMode;
    private Bitmap image;
    private NfcCard nfcCard;

    public NewCardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is scan new card fragment");
        isEditMode = false;
    }

    public LiveData<String> getText() {
        return mText;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public NfcCard getNfcCard() {
        return nfcCard;
    }

    public void setNfcCard(NfcCard nfcCard) {
        this.nfcCard = nfcCard;
    }
}