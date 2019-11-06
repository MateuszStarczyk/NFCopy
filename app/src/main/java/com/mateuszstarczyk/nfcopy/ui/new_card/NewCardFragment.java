package com.mateuszstarczyk.nfcopy.ui.new_card;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.mateuszstarczyk.nfcopy.R;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcCard;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcReader;
import com.mateuszstarczyk.nfcopy.service.nfc.db.TinyDB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static android.app.Activity.RESULT_OK;
import static com.mateuszstarczyk.nfcopy.service.ImageService.ivToBitmap;
import static com.mateuszstarczyk.nfcopy.service.nfc.NfcReader.byteToString;

public class NewCardFragment extends Fragment {

    private static final int PICK_IMAGE = 1;
    private static final int PICK_CAMERA_PHOTO = 2;
    private NewCardViewModel newCardViewModel;
    private NfcReader nfcReader;
    private NavController navController;
    private View root;
    private PulsatorLayout pulsator;
    private EditText etCardId;
    private EditText etCardName;
    private TextView tvCardClass;
    private ImageView ivPreview;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        newCardViewModel =
                ViewModelProviders.of(this).get(NewCardViewModel.class);
        root = inflater.inflate(R.layout.fragment_new_card, container, false);

        pulsator = root.findViewById(R.id.pulsator);

        nfcReader = new NfcReader(getActivity(), new NfcAdapter.ReaderCallback() {
            @Override
            public void onTagDiscovered(final Tag tag) {
                nfcReader.disableReaderMode();
                Log.i("INFO", tag.toString());

                setEditMode();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        etCardId.setText(byteToString(tag.getId()));
                        tvCardClass.setText(Arrays.toString(tag.getTechList()));
                    }
                });
            }
        });
        if (newCardViewModel.isEditMode()) {
            setEditMode();
        } else {
            pulsator.start();
            setScanMode();
        }
        etCardId = root.findViewById(R.id.et_card_id);
        etCardName = root.findViewById(R.id.et_card_name);
        tvCardClass = root.findViewById(R.id.tv_card_class);
        ivPreview = root.findViewById(R.id.iv_preview);

        Button btnAdd = root.findViewById(R.id.btnAdd);
        Button btnCancel = root.findViewById(R.id.btnCancel);
        ImageButton btnGallery = root.findViewById(R.id.btn_gallery);
        ImageButton btnCamera = root.findViewById(R.id.btn_camera);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickAdd();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCancel();
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromCamera();
            }
        });

        navController = Navigation.findNavController(Objects.requireNonNull(getActivity()), R.id.nav_host_fragment);

        return root;
    }


    private void pickFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(
                Objects.requireNonNull(getContext()).getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PICK_CAMERA_PHOTO);
        }
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    InputStream inputStream = Objects.requireNonNull(getContext())
                            .getContentResolver()
                            .openInputStream(Objects.requireNonNull(data.getData()));
                    newCardViewModel.setNfcCard(new NfcCard(etCardId.getText().toString(), etCardName.getText().toString(), tvCardClass.getText().toString(), BitmapFactory.decodeStream(inputStream)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == PICK_CAMERA_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            newCardViewModel.setNfcCard(new NfcCard(etCardId.getText().toString(), etCardName.getText().toString(), tvCardClass.getText().toString(), (Bitmap)extras.get("data")));
        }
    }

    private void setEditMode() {
        newCardViewModel.setEditMode(true);
        nfcReader.disableReaderMode();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                pulsator.setVisibility(View.GONE);
                root.findViewById(R.id.iv_scan).setVisibility(View.GONE);
                root.findViewById(R.id.cl_new_card).setVisibility(View.VISIBLE);
            }
        });
    }

    private void setScanMode() {
        newCardViewModel.setEditMode(false);
        nfcReader.enableReaderMode();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                root.findViewById(R.id.cl_new_card).setVisibility(View.GONE);
                pulsator.setVisibility(View.VISIBLE);
                root.findViewById(R.id.iv_scan). setVisibility(View.VISIBLE);
            }
        });
    }

    private void onClickCancel() {
        goToCardsView();
    }

    private void onClickAdd() {
        setScanMode();
        goToCardsView();
        TinyDB tinydb = new TinyDB(getContext());
        ArrayList<NfcCard> tagsUIDs = tinydb.getListObject("nfc_cards", NfcCard.class);

        tagsUIDs.add(new NfcCard(etCardId.getText().toString(), etCardName.getText().toString(), tvCardClass.getText().toString(), ivPreview));
        tinydb.putListObject("nfc_cards", tagsUIDs);

    }

    public void readTag(Tag tag){
        System.out.println(tag.toString());
    }

    private void goToCardsView() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                navController.navigate(R.id.nav_cards);
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        if (newCardViewModel.isEditMode()) {
            nfcReader.disableReaderMode();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    etCardId.setText(newCardViewModel.getNfcCard().getUID());
                    etCardName.setText(newCardViewModel.getNfcCard().getName());
                    tvCardClass.setText(newCardViewModel.getNfcCard().getClassName());
                    ivPreview.setImageBitmap(newCardViewModel.getNfcCard().getBitmap());
                }
            });
        } else
            nfcReader.enableReaderMode();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (newCardViewModel.isEditMode()) {
            newCardViewModel.setNfcCard(new NfcCard(etCardId.getText().toString(), etCardName.getText().toString(), tvCardClass.getText().toString(), ivToBitmap(ivPreview)));
        }
        nfcReader.disableReaderMode();
    }
}