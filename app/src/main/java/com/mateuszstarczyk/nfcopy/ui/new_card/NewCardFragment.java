package com.mateuszstarczyk.nfcopy.ui.new_card;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.inputmethodservice.Keyboard;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mateuszstarczyk.nfcopy.R;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcCard;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcReader;
import com.mateuszstarczyk.nfcopy.service.nfc.db.TinyDB;

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
    private View newCardView;
    private TextInputEditText tilCardId;
    private TextInputEditText tilCardName;
    private TextInputEditText tilCardClass;
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
                        tilCardId.setText(byteToString(tag.getId()));
                        tilCardClass.setText(Arrays.toString(tag.getTechList()));
                    }
                });
            }
        });

        newCardView = root.findViewById(R.id.cl_new_card);
        tilCardId = root.findViewById(R.id.et_card_id);
        tilCardName = root.findViewById(R.id.et_card_name);
        tilCardClass = root.findViewById(R.id.tv_card_class);
        ivPreview = root.findViewById(R.id.iv_preview);

        MaterialButton btnAdd = root.findViewById(R.id.btnAdd);
        MaterialButton btnCancel = root.findViewById(R.id.btnCancel);
        MaterialButton btnGallery = root.findViewById(R.id.btn_gallery);
        MaterialButton btnCamera = root.findViewById(R.id.btn_camera);

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

        if (newCardViewModel.isEditMode()) {
            setEditMode();
        } else {
            pulsator.start();
            setScanMode();
        }

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
                    newCardViewModel.setNfcCard(new NfcCard(tilCardId.getText().toString(), tilCardName.getText().toString(), tilCardClass.getText().toString(), BitmapFactory.decodeStream(inputStream)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == PICK_CAMERA_PHOTO && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            newCardViewModel.setNfcCard(new NfcCard(tilCardId.getText().toString(), tilCardName.getText().toString(), tilCardClass.getText().toString(), (Bitmap)extras.get("data")));
        }
    }

    private void setEditMode() {
        newCardViewModel.setEditMode(true);
        nfcReader.disableReaderMode();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                pulsator.setVisibility(View.GONE);
                root.findViewById(R.id.iv_scan).setVisibility(View.GONE);
                newCardView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setScanMode() {
        newCardViewModel.setEditMode(false);
        nfcReader.enableReaderMode();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                newCardView.setVisibility(View.GONE);
                pulsator.setVisibility(View.VISIBLE);
                root.findViewById(R.id.iv_scan). setVisibility(View.VISIBLE);
            }
        });
    }

    private void onClickCancel() {
        goToCardsView();
    }

    private void onClickAdd() {
        TinyDB tinydb = new TinyDB(getActivity());
        ArrayList<NfcCard> tagsUIDs = tinydb.getListObject("nfc_cards", NfcCard.class);

        tagsUIDs.add(new NfcCard(tilCardId.getText().toString(), tilCardName.getText().toString(), tilCardClass.getText().toString(), ivPreview));
        tinydb.putListObject("nfc_cards", tagsUIDs);
        newCardViewModel.setEditMode(false);
        goToCardsView();
    }

    public void readTag(Tag tag){
        System.out.println(tag.toString());
    }

    private void goToCardsView() {
        hideKeyboard();
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                navController.navigate(R.id.nav_cards);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getActivity());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (newCardViewModel.isEditMode()) {
            nfcReader.disableReaderMode();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tilCardId.setText(newCardViewModel.getNfcCard().getUID());
                    tilCardName.setText(newCardViewModel.getNfcCard().getName());
                    tilCardClass.setText(newCardViewModel.getNfcCard().getClassName());
                    if (newCardViewModel.getNfcCard().getBitmap() == null) {
                        ivPreview.setVisibility(View.GONE);
                    } else {
                        ivPreview.setVisibility(View.VISIBLE);
                        ivPreview.setImageBitmap(newCardViewModel.getNfcCard().getBitmap());
                    }
                }
            });
        } else
            nfcReader.enableReaderMode();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (newCardViewModel.isEditMode()) {
            newCardViewModel.setNfcCard(new NfcCard(tilCardId.getText().toString(), tilCardName.getText().toString(), tilCardClass.getText().toString(), ivToBitmap(ivPreview)));
        }
        nfcReader.disableReaderMode();
    }
}