package com.mateuszstarczyk.nfcopy.ui.new_card;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
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
import com.mateuszstarczyk.nfcopy.be.nfc.NfcReader;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class NewCardFragment extends Fragment {

    private NewCardViewModel newCardViewModel;
    private NfcReader nfcReader;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newCardViewModel =
                ViewModelProviders.of(this).get(NewCardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_new_card, container, false);
//        final TextView textView = root.findViewById(R.id.text_gallery);
//        newCardViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        PulsatorLayout pulsator = root.findViewById(R.id.pulsator);
        pulsator.start();

        nfcReader = new NfcReader(getActivity(), new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                Log.i("INFO", tag.toString());
                onTagDiscovered(tag);
            }
        });
        nfcReader.enableReaderMode();
        return root;
    }

    private void onTagDiscovered(Tag tag){

    }

    @Override
    public void onResume() {
        super.onResume();
        nfcReader.enableReaderMode();
    }

    @Override
    public void onStop() {
        super.onStop();
        nfcReader.disableReaderMode();
    }
}