package com.mateuszstarczyk.nfcopy.ui.nfc;

import androidx.navigation.NavController;
import android.view.View;

import com.mateuszstarczyk.nfcopy.R;

public class NfcView implements View.OnClickListener {

    private NavController navController;

    public NfcView(NavController navController) {
        this.navController = navController;
    }

    @Override
    public void onClick(View v) {
        navController.navigate(R.id.nav_new_card);
    }
}
