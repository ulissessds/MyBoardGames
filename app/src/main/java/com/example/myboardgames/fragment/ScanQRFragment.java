package com.example.myboardgames.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myboardgames.R;
import com.example.myboardgames.control.CaptureAct;
import com.example.myboardgames.control.ItemBGViewHolder;
import com.example.myboardgames.model.ItemBG;
import com.example.myboardgames.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScanQRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanQRFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String FRIEND_KEY = "friendKey";

    // TODO: Rename and change types of parameters
    private String mFriendKey;

    public ScanQRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param friendKey Parameter 1.
     * @return A new instance of fragment ScanQRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ScanQRFragment newInstance(String friendKey) {
        ScanQRFragment fragment = new ScanQRFragment();
        Bundle args = new Bundle();
        args.putString(FRIEND_KEY, friendKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFriendKey = getArguments().getString(FRIEND_KEY);
        }
    }

    // Widgets
    RecyclerView friendRecyclerView;
    TextView gameListTitle, friendName;
    Button btnScan;

    // Firebase
    FirebaseDatabase mDatabase; // Database
    DatabaseReference DataRef;

    // Recycler view do firebase
    FirebaseRecyclerOptions<ItemBG> optionsBG;
    FirebaseRecyclerAdapter<ItemBG, ItemBGViewHolder> itemBGAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scan_q_r, container, false);

        // Inicializações
        btnScan = view.findViewById(R.id.btnScan);
        gameListTitle = view.findViewById(R.id.gameListTitle);
        friendName = view.findViewById(R.id.friendName);
        friendRecyclerView = view.findViewById(R.id.friendRecyclerView);

        // Referências no firebase
        mDatabase = FirebaseDatabase.getInstance();
        DataRef = mDatabase.getReference();

        // Botão de escanear
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chamar o método de escanear
                scanCode();
            }
        });

        // Criar lista se o argumento passado na hora de criar a instância não for nulo
        if (mFriendKey != null && !mFriendKey.isEmpty()) {
            createBGList(mFriendKey);
        }

        return view;
    }

    // Método que escaneia
    private void scanCode() {
        IntentIntegrator.forSupportFragment(this)
                .setCaptureActivity(CaptureAct.class) // Não fez diferença
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setBeepEnabled(false)
                .setOrientationLocked(true)
                .setPrompt("Escaneando outro Usuário")
                .initiateScan();
    }

    // Pegar o resultado do escaneamento
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getContext(), "Cancelado o Scan", Toast.LENGTH_LONG).show();
            } else {
                //Toast.makeText(getContext(), "Resultado: " + result.getContents(), Toast.LENGTH_LONG).show();

                DatabaseReference ref = DataRef.child("items");
                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        // Booleano só pra exibir uma mensagem caso não ache o usuário no banco
                        Boolean foundUser = false;

                        for (DataSnapshot userKey: snapshot.getChildren()) {
                            if (userKey.getKey().equals(result.getContents())) {
                                // Entrou aqui é porque deu certo
                                foundUser = true;
                                Fragment fragment = ScanQRFragment.newInstance(result.getContents());
                                getActivity().getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.container_root, fragment)
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            }
                        }
                        if (foundUser) {
                            // Se achou deu certo né
                            Toast.makeText(getContext(), "Scan COMPLETO", Toast.LENGTH_LONG).show();
                        } else {
                            // Se deu ruim o scan e não achou o usuário
                            Toast.makeText(getContext(), "Usuário NÃO encontrado", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void createBGList(String friendKey) {

        // Pegando o local dos JSON no banco de dados
        DatabaseReference friendItemsJSON = DataRef.child("items").child(friendKey);
        // Instanciando o adapter e a lista de opções
        optionsBG = new FirebaseRecyclerOptions.Builder<ItemBG>().setQuery(friendItemsJSON, ItemBG.class).build();
        itemBGAdapter = new FirebaseRecyclerAdapter<ItemBG, ItemBGViewHolder>(optionsBG) {
            @Override
            protected void onBindViewHolder(@NonNull ItemBGViewHolder itemBGViewHolder, @SuppressLint("RecyclerView") int i, @NonNull ItemBG itemBG) {
                itemBGViewHolder.cardName.setText(itemBG.getName());
                Picasso.get().load(itemBG.getPhotoUrl()).into(itemBGViewHolder.cardImage);
                itemBGViewHolder.viewBG.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fragment fragment = FriendBGDetailFragment.newInstance(friendKey, getRef(i).getKey());
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

        // Configurando o Recycler View
        itemBGAdapter.startListening();
        friendRecyclerView.setAdapter(itemBGAdapter);
        friendRecyclerView.setHasFixedSize(true);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Procurando o nome do dono da lista e atribuindo
        DatabaseReference allUsersJSON = DataRef.child("users").child(friendKey);
        allUsersJSON.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Seta o nome do usuário amigo
                friendName.setText(snapshot.getValue(User.class).getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}