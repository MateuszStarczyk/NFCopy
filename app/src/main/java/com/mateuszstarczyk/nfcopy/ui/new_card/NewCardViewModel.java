package com.mateuszstarczyk.nfcopy.ui.new_card;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NewCardViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private boolean isEditMode;

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
}