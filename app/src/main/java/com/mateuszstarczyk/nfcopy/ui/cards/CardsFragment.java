package com.mateuszstarczyk.nfcopy.ui.cards;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mateuszstarczyk.nfcopy.R;
import com.mateuszstarczyk.nfcopy.service.nfc.NfcCard;
import com.mateuszstarczyk.nfcopy.service.nfc.db.TinyDB;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class CardsFragment extends Fragment {

    private CardsViewModel cardsViewModel;
    private TinyDB tinydb;
    private RecyclerView rv;
    private FloatingActionButton fab;
    private RVAdapter adapter;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cardsViewModel =
                ViewModelProviders.of(this).get(CardsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cards, container, false);
        setHasOptionsMenu(true);
        NavController navController = Navigation.findNavController(
                Objects.requireNonNull(getActivity()), R.id.nav_host_fragment);

        fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new FabViewListener(navController));
        fab.show();

        tinydb = new TinyDB(getActivity());
        ArrayList<NfcCard> cards = tinydb.getListObject("nfc_cards", NfcCard.class);

        rv = root.findViewById(R.id.rv_cards);
        rv.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(llm);

        adapter = new RVAdapter(cards);
        rv.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new SwipeToDeleteSimpleCallback(
                0, ItemTouchHelper.ANIMATION_TYPE_SWIPE_CANCEL);


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.cards.clear();
        adapter.cards = tinydb.getListObject("nfc_cards", NfcCard.class);
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

    private class SwipeToDeleteSimpleCallback extends ItemTouchHelper.SimpleCallback {

        private Drawable icon;
        private final Drawable background;

        public SwipeToDeleteSimpleCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
            icon = ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_delete_sweep_black_24dp);
            background = getActivity().getDrawable(R.drawable.layout_corner_radius_background);
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder, float dX,
                                float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            CardView cv = itemView.findViewById(R.id.cv_cards);
            int backgroundCornerOffset =
                    ((ViewGroup.MarginLayoutParams)cv
                            .getLayoutParams()).rightMargin;
            int cardViewCornerRadius = Math.round(cv.getRadius());

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
                int iconRight = itemView.getLeft() + iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset,
                        itemView.getBottom());
            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                int cardViewWidth = itemView.getRight() - 2 * backgroundCornerOffset;
                if (dX > - cardViewWidth) {
                    background.setBounds(itemView.getRight() + ((int) dX)
                                    - backgroundCornerOffset - 2 * cardViewCornerRadius,
                            itemView.getTop() + backgroundCornerOffset,
                            itemView.getRight() - backgroundCornerOffset,
                            itemView.getBottom() - backgroundCornerOffset);
                } else {
                    background.setBounds(itemView.getRight() - cardViewWidth
                                    - backgroundCornerOffset,
                            itemView.getTop() + backgroundCornerOffset,
                            itemView.getRight() - backgroundCornerOffset,
                            itemView.getBottom() - backgroundCornerOffset);
                }

            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }

            background.draw(c);
            icon.draw(c);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            tinydb.putInt("last_deleted_index", viewHolder.getAdapterPosition());
            tinydb.putObject("last_deleted", adapter.cards.get(viewHolder.getAdapterPosition()));

            adapter.cards.remove(viewHolder.getAdapterPosition());
            tinydb.putListObject("nfc_cards", adapter.cards);
            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());

            Snackbar mySnackbar = Snackbar.make(viewHolder.itemView,
                    R.string.action_deleted_card, Snackbar.LENGTH_SHORT);
            mySnackbar.setAction(R.string.action_undo_deleted, new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = tinydb.getInt("last_deleted_index");
                    adapter.cards.add(position, tinydb.getObject("last_deleted", NfcCard.class));
                    tinydb.putListObject("nfc_cards", adapter.cards);
                    adapter.notifyItemInserted(position);
                    tinydb.remove("last_deleted");
                    tinydb.remove("last_deleted_index");
                }
            });
            mySnackbar.show();
        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }
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

        ArrayList<NfcCard> cards;

        RVAdapter(ArrayList<NfcCard> cards){
            this.cards = cards;
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        @NonNull
        @Override
        public CardsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View cardsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
            return new CardsViewHolder(cardsView);
        }

        @Override
        public void onBindViewHolder(@NonNull CardsViewHolder holder, int position) {
            holder.cardName.setText(cards.get(position).getName());
            holder.cardUID.setText(cards.get(position).getUID());
            String path = cards.get(position).getImagePath();
            if (path != null && ! path.isEmpty())
                holder.cardPhoto.setImageURI(Uri.fromFile(new File(path)));
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