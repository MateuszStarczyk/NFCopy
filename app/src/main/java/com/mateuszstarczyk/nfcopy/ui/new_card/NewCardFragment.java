package com.mateuszstarczyk.nfcopy.ui.new_card;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.mateuszstarczyk.nfcopy.R;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcCard;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcMessage;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcReader;
import com.mateuszstarczyk.nfcopy.service.nfc.db.TinyDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static android.app.Activity.RESULT_OK;
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
            public void onTagDiscovered(Tag tag) {
                Log.i("INFO", tag.toString());
                NfcMessage nfcMessage = nfcReader.readTag(tag);
                nfcReader.disableReaderMode();
                NfcCard nfcCard = new NfcCard(byteToString(tag.getId()), "",
                        tag.getTechList(), null, nfcMessage);
                newCardViewModel.setNfcCard(nfcCard);

                if (nfcMessage != null)
                    showSuccessfulPopUp(nfcMessage.getPopUpMessage(getActivity()));
                else
                showFailedPopUp();
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
                    final int THUMBNAIL_SIZE = 256;

                    ImageDecoder.Source source = ImageDecoder
                            .createSource(Objects.requireNonNull(getContext())
                                    .getContentResolver(), Objects.requireNonNull(data.getData()));
                    Bitmap imageBitmap = ImageDecoder.decodeBitmap(source);

                    imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);

                    File destFile = new File(Objects.requireNonNull(getActivity()).getApplicationInfo().dataDir,
                            java.util.UUID.randomUUID() + ".png");
                    FileOutputStream outStream = new FileOutputStream(destFile);
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 0, outStream);
                    newCardViewModel.getNfcCard().setImagePath(destFile.getPath());
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == PICK_CAMERA_PHOTO && resultCode == RESULT_OK) {
            try {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) Objects.requireNonNull(extras).get("data");
                File destFile = new File(Objects.requireNonNull(getActivity()).getApplicationInfo().dataDir,
                        java.util.UUID.randomUUID() + ".png");
                FileOutputStream outStream = new FileOutputStream(destFile);
                Objects.requireNonNull(imageBitmap).compress(Bitmap.CompressFormat.PNG, 0, outStream);
                newCardViewModel.getNfcCard().setImagePath(destFile.getPath());

                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setEditMode() {
        newCardViewModel.setEditMode(true);
        nfcReader.disableReaderMode();
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            public void run() {
                pulsator.setVisibility(View.GONE);
                root.findViewById(R.id.iv_scan).setVisibility(View.GONE);
                newCardView.setVisibility(View.VISIBLE);

                tilCardId.setText(newCardViewModel.getNfcCard().getUID());
                tilCardClass.setText(Arrays.toString(newCardViewModel.getNfcCard().getTechList()));
                tilCardName.setText(newCardViewModel.getNfcCard().getName());
                if (newCardViewModel.getNfcCard().getImagePath() == null ||
                        newCardViewModel.getNfcCard().getImagePath().isEmpty()) {
                    ivPreview.setVisibility(View.GONE);
                } else {
                    ivPreview.setVisibility(View.VISIBLE);
                    ivPreview.setImageURI(Uri.fromFile(
                            new File(newCardViewModel.getNfcCard().getImagePath())));
                }
            }
        });
    }

    private void setScanMode() {
        newCardViewModel.setEditMode(false);
        nfcReader.enableReaderMode();
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            public void run() {
                newCardView.setVisibility(View.GONE);
                pulsator.setVisibility(View.VISIBLE);
                root.findViewById(R.id.iv_scan). setVisibility(View.VISIBLE);
            }
        });
    }

    private void showFailedPopUp() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()), R.style.ThemeOverlay_MaterialComponents_NFCopy_MaterialAlertDialog)
                        .setTitle(getActivity().getText(R.string.error))
                        .setMessage(getActivity().getText(R.string.read_failed))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setScanMode();
                            }
                        })
                        .setCancelable(false)
                        .show();

            }
        });
    }

    private void showSuccessfulPopUp(final String message) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()), R.style.ThemeOverlay_MaterialComponents_NFCopy_MaterialAlertDialog)
                        .setTitle(getActivity().getText(R.string.scanned_card))
                        .setMessage(message)
                        .setPositiveButton(getActivity().getText(R.string.accept), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setEditMode();
                            }
                        })
                        .setNegativeButton(getActivity().getText(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setScanMode();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }
        });
    }

    private void onClickCancel() {
        goToCardsView();
    }

    private void onClickAdd() {
        TinyDB tinydb = new TinyDB(Objects.requireNonNull(getActivity()));
        ArrayList<NfcCard> tagsUIDs = tinydb.getListObject("nfc_cards", NfcCard.class);

        newCardViewModel.getNfcCard().setName(Objects.requireNonNull(tilCardName.getText()).toString());
        tagsUIDs.add(newCardViewModel.getNfcCard());

        tinydb.putListObject("nfc_cards", tagsUIDs);
        newCardViewModel.setEditMode(false);
        goToCardsView();
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
        InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity())
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getActivity().getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(getActivity());
        }
        Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (newCardViewModel.isEditMode() && newCardViewModel.getNfcCard() != null) {
            nfcReader.disableReaderMode();
            setEditMode();
        } else
            nfcReader.enableReaderMode();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (newCardViewModel.isEditMode())
            newCardViewModel.getNfcCard().setName(Objects.requireNonNull(tilCardName.getText()).toString());
        nfcReader.disableReaderMode();
    }
}