package com.example.myboardgames.fragment;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myboardgames.HomeActivity;
import com.example.myboardgames.MainActivity;
import com.example.myboardgames.R;
import com.example.myboardgames.model.ItemBG;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link addBoardGameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class addBoardGameFragment extends Fragment {

    // Declarar widgets
    ImageView inputBGImage;
    FloatingActionButton fabBGCamera;
    EditText inputBGName, inputBGCategory, inputBGNumberOfPlayers, inputBGPlayTime, inputBGPrice;
    Button btnRegisterBG;

    // Constantes
    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_IMAGE = 2;

    ProgressDialog progressDialog; // Dialog para mostrar enquanto usuário espera

    // Firebase inicializações
    FirebaseAuth mAuth; // Autenticador
    FirebaseUser mUser; // Usuário
    FirebaseDatabase mDatabase; // Database
    DatabaseReference DataRef;
    FirebaseStorage mStorage; // Storage
    StorageReference StorageRef;

    // Requer um construtor público e vazio
    public addBoardGameFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar o layout pra esse fragment
        View view = inflater.inflate(R.layout.fragment_add_board_game, container, false);

        // Inicializações
        inputBGImage = view.findViewById(R.id.inputBGImage);
        fabBGCamera = view.findViewById(R.id.fabBGCamera);
        inputBGName = view.findViewById(R.id.inputBGName);
        inputBGCategory = view.findViewById(R.id.inputBGCategory);
        inputBGNumberOfPlayers = view.findViewById(R.id.inputBGNumberOfPlayers);
        inputBGPlayTime = view.findViewById(R.id.inputBGPlayTime);
        inputBGPrice = view.findViewById(R.id.inputBGPrice);
        btnRegisterBG = view.findViewById(R.id.btnRegisterBG);
        progressDialog = new ProgressDialog(getContext());

        // Firebase inicializações
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        DataRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        StorageRef = mStorage.getReference();

        // Trocar imagem do jogo escolhendo da galeria
        inputBGImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "Escolha a Imagem"), PICK_IMAGE);
            }
        });

        // Trocar imagem do perfil tirando foto da camera
        fabBGCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(camera, CAMERA_IMAGE);
            }
        });

        // Listener do botão de cadastrar o jogo
        btnRegisterBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Abaixar teclado depois de digitar o preço do BG
                InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );
                manager.hideSoftInputFromWindow(inputBGPrice.getApplicationWindowToken(), 0);

                // Chamar método de validação do jogo
                AuthGame();
            }
        });

        return view;
    }

    // Checar os dados inseridos e mandar pro firebase
    private void AuthGame() {
        String nameBG = inputBGName.getText().toString();
        String categoryBG = inputBGCategory.getText().toString();
        String numberOfPlayersBG = inputBGNumberOfPlayers.getText().toString();
        String playTimeBG = inputBGPlayTime.getText().toString();
        Double priceBG = Double.parseDouble(inputBGPrice.getText().toString());

        if (nameBG.isEmpty() || nameBG.length() < 1) // Checar a entrada do nome
        {
            inputBGName.setError("Coloque um nome válido");
            inputBGName.requestFocus();
        }
        else if (categoryBG.isEmpty() || categoryBG.length() < 1)
        {
            inputBGCategory.setError("Coloque uma categoria válida");
            inputBGCategory.requestFocus();
        }
        else if (numberOfPlayersBG.isEmpty() || numberOfPlayersBG.length() < 1)
        {
            inputBGNumberOfPlayers.setError("Coloque uma entrada válida");
            inputBGNumberOfPlayers.requestFocus();
        }
        else if (playTimeBG.isEmpty() || playTimeBG.length() < 1)
        {
            inputBGPlayTime.setError("Coloque uma entrada válida");
            inputBGPlayTime.requestFocus();
        }
        else if (priceBG.isNaN()
                || priceBG.isInfinite()
                || priceBG < 0) // O preço pode ser zero, mas não negativo
        {
            inputBGPrice.setError("Coloque um valor válido");
            inputBGPrice.requestFocus();
        }
        else // Se chegou aqui os campos não são nulos nem inválidos
        {
            progressDialog.setMessage("Adicionando o jogo...");
            progressDialog.setTitle("Novo Jogo");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            // Criando uma chave para o item
            DatabaseReference mAllItemsJSON = DataRef.child("items");
            DatabaseReference mUserItemJSON = mAllItemsJSON.child(mUser.getUid());
            String key = mUserItemJSON.push().getKey();

            // Pegando o Uri da imagem do ImageView
            Bitmap bitmap = ((BitmapDrawable) inputBGImage.getDrawable()).getBitmap();
            Uri gameUri = getImageUri(getContext(), bitmap);

            // Local no Storage para gravar a imagem
            StorageReference mAllUsersItems = StorageRef.child("usersItems");
            StorageReference mUserJSON = mAllUsersItems.child(mUser.getUid());

            // Upar foto do jogo no storage
            mUserJSON.child(key + ".png").putFile(gameUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    mUserJSON.child(key + ".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap hashMap = new HashMap();
                            hashMap.put("photoUrl", uri.toString());
                            hashMap.put("name", nameBG);
                            hashMap.put("category", categoryBG);
                            hashMap.put("numberOfPlayers", numberOfPlayersBG);
                            hashMap.put("playTime", playTimeBG);
                            hashMap.put("price", priceBG);

                            mUserItemJSON.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // Fechar o dialog de progresso e mostrar mensagem
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(),
                                    "Jogo adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                                    // Mandar de volta pro início
                                    sendUserToHome();
                                }
                            });
                        }
                    });
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (!task.isSuccessful()) {
                        // Fechar o dialog de progresso e mostrar mensagem
                        progressDialog.dismiss();
                        Toast.makeText(getContext(),
                                "" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendUserToHome() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);

        // Limpar a task antes de passar a nova
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    // Método que pega o Uri dado um Bitmap
    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), inImage, UUID.randomUUID().toString() + ".png", "drawing");
        return Uri.parse(path);
    }

    // Results das activities para setar a imagem do jogo
    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            Uri imageURI = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageURI);
                inputBGImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA_IMAGE && resultCode == RESULT_OK)
        {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            inputBGImage.setImageBitmap(bitmap);
        }
    }

    /* CÓDIGO PADRÃO QUE VEM NO FRAGMENTO */
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment addBoardGameFragment.
     */

    // TODO: Rename and change types and number of parameters
    public static addBoardGameFragment newInstance(String param1, String param2) {
        addBoardGameFragment fragment = new addBoardGameFragment();
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
}