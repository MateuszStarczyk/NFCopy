package com.mateuszstarczyk.nfcopy.ui.new_card;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NewCardViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public NewCardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is scan new card fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}