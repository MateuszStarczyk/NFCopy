package com.mateuszstarczyk.nfcopy.ui.cards;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mateuszstarczyk.nfcopy.R;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcCard;
import com.mateuszstarczyk.nfcopy.service.nfc.db.TinyDB;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardsFragment extends Fragment {

    private CardsViewModel cardsViewModel;
    private List<NfcCard> cards;
    private TinyDB tinydb;
    private RecyclerView rv;
    private FloatingActionButton fab;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cardsViewModel =
                ViewModelProviders.of(this).get(CardsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cards, container, false);
        setHasOptionsMenu(true);
        NavController navController = Navigation.findNavController(Objects.requireNonNull(getActivity()), R.id.nav_host_fragment);
        fab = getActivity().findViewById(R.id.fab);
//        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new FabViewListener(navController));
        fab.show();
        tinydb = new TinyDB(getContext());
        cards = tinydb.getListObject("nfc_cards", NfcCard.class);

        rv = root.findViewById(R.id.rv_cards);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        rv.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(cards);
        rv.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        cards = tinydb.getListObject("nfc_cards", NfcCard.class);
        RVAdapter adapter = new RVAdapter(cards);
        rv.setAdapter(adapter);
        fab.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        fab.hide();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all){
            tinydb.putListObject("nfc_cards", new ArrayList<NfcCard>());
            onResume();
        }
        return super.onOptionsItemSelected(item);
    }

    private class FabViewListener implements View.OnClickListener {

        private final NavController navController;

        FabViewListener(NavController navController) {
            this.navController = navController;
        }

        @Override
        public void onClick(View v) {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    navController.navigate(R.id.nav_new_card);
                }
            });
        }
    }

    private class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardsViewHolder>{

        List<NfcCard> cards;

        RVAdapter(List<NfcCard> cards){
            this.cards = cards;
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @NonNull
        @Override
        public CardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
            return new CardsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull CardsViewHolder holder, int position) {
            holder.cardName.setText(cards.get(position).getName());
            holder.cardUID.setText(cards.get(position).getUID());
            Bitmap bitmap = cards.get(position).getBitmap();
            if (bitmap != null)
                holder.cardPhoto.setImageBitmap(bitmap);
            else
                holder.cardPhoto.setImageDrawable(getActivity().getDrawable(R.drawable.ic_menu_card));
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }

        class CardsViewHolder extends RecyclerView.ViewHolder {
            CardView cv;
            TextView cardName;
            TextView cardUID;
            ImageView cardPhoto;

            CardsViewHolder(View itemView) {
                super(itemView);
                cv = itemView.findViewById(R.id.cv_cards);
                cardName = itemView.findViewById(R.id.card_name);
                cardUID = itemView.findViewById(R.id.card_uid);
                cardPhoto = itemView.findViewById(R.id.card_photo);
            }
        }

    }
}