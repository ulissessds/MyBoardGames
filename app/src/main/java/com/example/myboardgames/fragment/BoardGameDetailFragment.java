package com.example.myboardgames.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myboardgames.R;
import com.example.myboardgames.model.ItemBG;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BoardGameDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BoardGameDetailFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USER_KEY = "userKey";
    private static final String ITEM_BG_KEY = "itemBGKey";

    // TODO: Rename and change types of parameters
    private String mUserKey;
    private String mItemBGKey;

    public BoardGameDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userKey Parameter 1.
     * @param itemBGKey Parameter 2.
     * @return A new instance of fragment BoardGameDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BoardGameDetailFragment newInstance(String userKey, String itemBGKey) {
        BoardGameDetailFragment fragment = new BoardGameDetailFragment();
        Bundle args = new Bundle();
        args.putString(USER_KEY, userKey);
        args.putString(ITEM_BG_KEY, itemBGKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserKey = getArguments().getString(USER_KEY);
            mItemBGKey = getArguments().getString(ITEM_BG_KEY);
        }
    }

    // Widgets
    ImageView imageBG;
    TextView nameBG, categoryBG, playersBG, playTimeBG, priceBG;
    Button btnDeleteBG;

    ProgressDialog progressDialog; // Dialog para mostrar enquanto usuário espera

    // Firebase
    FirebaseAuth mAuth; // Autenticador
    FirebaseUser mUser; // Usuário
    FirebaseDatabase mDatabase; // Database
    DatabaseReference DataRef;
    FirebaseStorage mStorage; // Storage
    StorageReference StorageRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_board_game_detail, container, false);

        // Inicializações
        imageBG = view.findViewById(R.id.imageBG);
        nameBG = view.findViewById(R.id.nameBG);
        categoryBG = view.findViewById(R.id.categoryBG);
        playersBG = view.findViewById(R.id.playersBG);
        playTimeBG = view.findViewById(R.id.playTimeBG);
        priceBG = view.findViewById(R.id.priceBG);
        btnDeleteBG = view.findViewById(R.id.btnDeleteBG);
        progressDialog = new ProgressDialog(getContext());

        // Firebase inicializações
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        DataRef = mDatabase.getReference().child("items");
        mStorage = FirebaseStorage.getInstance();
        StorageRef = mStorage.getReference().child("usersItems").child(mUserKey);

        // Local no banco de dados do item específico desse usuário
        DatabaseReference itemJSON = DataRef.child(mUserKey).child(mItemBGKey);
        // Pegar os dados do item
        itemJSON.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Se existe o item no banco de dados, converte na classe de ItemBG
                    ItemBG itemBG = snapshot.getValue(ItemBG.class);

                    // Setar os widgets com os dados do item
                    Picasso.get().load(itemBG.getPhotoUrl()).into(imageBG);
                    nameBG.setText(itemBG.getName());
                    categoryBG.setText(itemBG.getCategory());
                    playersBG.setText(itemBG.getNumberOfPlayers());
                    playTimeBG.setText(itemBG.getPlayTime());
                    priceBG.setText(itemBG.getPrice().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Listener do botão de deletar o jogo
        btnDeleteBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dialog de progresso
                progressDialog.setMessage("Excluindo o jogo...");
                progressDialog.setTitle("Excluir Jogo");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                // Pegar a referência no Storage
                StorageReference imageJSON = StorageRef.child(mItemBGKey + ".png");

                itemJSON.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        imageJSON.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                // Fechar o dialog de progresso e mostrar mensagem
                                progressDialog.dismiss();
                                Toast.makeText(getContext(),
                                        "Jogo excluído com sucesso!", Toast.LENGTH_SHORT).show();

                                // Mandar de volta pra prateleira
                                getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .replace(R.id.container_root, new BoardGameShelfFragment())
                                        .commit();
                            }
                        });
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            // Fechar o dialog de progresso e mostrar mensagem
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),
                                    "" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return view;
    }
}