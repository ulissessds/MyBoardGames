package com.example.myboardgames.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myboardgames.HomeActivity;
import com.example.myboardgames.R;
import com.example.myboardgames.control.ItemBGViewHolder;
import com.example.myboardgames.model.ItemBG;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BoardGameShelfFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BoardGameShelfFragment extends Fragment {

    // Declarar widgets
    RecyclerView shelfRecyclerView;

    // Firebase inicializações
    FirebaseAuth mAuth; // Autenticador
    FirebaseUser mUser; // Usuário
    FirebaseDatabase mDatabase; // Database
    DatabaseReference DataRef;
    FirebaseRecyclerOptions<ItemBG> optionsBG; // Os BGs do usuário
    // Adapter do firebase, tem que criar a classe ViewHolder
    FirebaseRecyclerAdapter<ItemBG, ItemBGViewHolder> itemBGAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BoardGameShelfFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BoardGameShelfFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BoardGameShelfFragment newInstance(String param1, String param2) {
        BoardGameShelfFragment fragment = new BoardGameShelfFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar o layout pra esse fragment
        View view = inflater.inflate(R.layout.fragment_board_game_shelf, container, false);

        // Inicializar RecyclerView
        shelfRecyclerView = view.findViewById(R.id.shelfRecyclerView);
        shelfRecyclerView.setHasFixedSize(true);
        shelfRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Firebase inicializações
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        DataRef = mDatabase.getReference();

        // Chamar método que carrega os cards no recycler view
        LoadData();

        return view;
    }

    // Método para carregar nos cards cada jogo
    private void LoadData() {
        // Pegando o local dos JSON no banco de dados
        DatabaseReference userItemsJSON = DataRef.child("items").child(mUser.getUid());
        // Instanciando o adapter e a lista de opções
        optionsBG = new FirebaseRecyclerOptions.Builder<ItemBG>().setQuery(userItemsJSON, ItemBG.class).build();
        itemBGAdapter = new FirebaseRecyclerAdapter<ItemBG, ItemBGViewHolder>(optionsBG) {
            @Override
            protected void onBindViewHolder(@NonNull ItemBGViewHolder itemBGViewHolder, @SuppressLint("RecyclerView") int i, @NonNull ItemBG itemBG) {
                itemBGViewHolder.cardName.setText(itemBG.getName());
                Picasso.get().load(itemBG.getPhotoUrl()).into(itemBGViewHolder.cardImage);
                itemBGViewHolder.viewBG.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment fragment = BoardGameDetailFragment.newInstance(mUser.getUid(), getRef(i).getKey());
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container_root, fragment)
                                .addToBackStack(null)
                                .commit();
                    }
                });
            }

            @NonNull
            @Override
            public ItemBGViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_bg, parent, false);
                return new ItemBGViewHolder(view);
            }
        };

        // Setar o adapter no recyclerview
        itemBGAdapter.startListening();
        shelfRecyclerView.setAdapter(itemBGAdapter);
    }
}