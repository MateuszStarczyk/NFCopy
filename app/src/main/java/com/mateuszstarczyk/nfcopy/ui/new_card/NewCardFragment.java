package com.mateuszstarczyk.nfcopy.ui.new_card;

import android.media.Image;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.mateuszstarczyk.nfcopy.R;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcCard;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcReader;
import com.mateuszstarczyk.nfcopy.service.nfc.db.TinyDB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static com.mateuszstarczyk.nfcopy.service.nfc.NfcReader.byteToString;

public class NewCardFragment extends Fragment {

    private NewCardViewModel newCardViewModel;
    private NfcReader nfcReader;
    private NavController navController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newCardViewModel =
                ViewModelProviders.of(this).get(NewCardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_new_card, container, false);

        PulsatorLayout pulsator = root.findViewById(R.id.pulsator);
        pulsator.start();

        navController = Navigation.findNavController(Objects.requireNonNull(getActivity()), R.id.nav_host_fragment);

        nfcReader = new NfcReader(getActivity(), new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(Tag tag) {
                nfcReader.disableReaderMode();
                Log.i("INFO", tag.toString());

                TinyDB tinydb = new TinyDB(getContext());
                ArrayList<NfcCard> tagsUIDs = tinydb.getListObject("nfc_cards", NfcCard.class);


                ImageView imageView = new ImageView(getContext());
                imageView.setImageDrawable(Objects.requireNonNull(getActivity()).getDrawable(R.drawable.ic_menu_card));
                tagsUIDs.add(new NfcCard(byteToString(tag.getId()), String.valueOf(tagsUIDs.size()), Arrays.toString(tag.getTechList()), imageView));
                tinydb.putListObject("nfc_cards", tagsUIDs);

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getContext(), R.string.action_card_added, Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.nav_cards);
                    }
                });
            }
        });
        nfcReader.enableReaderMode();

        return root;
    }

    public void readTag(Tag tag){
        System.out.println(tag.toString());
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