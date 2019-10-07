package com.mateuszstarczyk.nfcopy.ui.new_card;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.mateuszstarczyk.nfcopy.R;

public class NewCardFragment extends Fragment {

    private NewCardViewModel newCardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newCardViewModel =
                ViewModelProviders.of(this).get(NewCardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_new_card, container, false);
        final TextView textView = root.findViewById(R.id.text_gallery);
        newCardViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}