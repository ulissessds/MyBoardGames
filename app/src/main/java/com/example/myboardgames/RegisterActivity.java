package com.example.myboardgames;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.myboardgames.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    // Declarar os widgets
    TextView jaPossuoConta;
    ImageView inputImage;
    FloatingActionButton fabCamera;
    EditText inputEmail, inputNome, inputPhone, inputSenha, inputConfirmarSenha;
    Button btnRegistrar;

    // Constantes
    private static final int PICK_IMAGE = 1;
    private static final int CAMERA_IMAGE = 2;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; // Regex pattern para email

    ProgressDialog progressDialog; // Dialog para mostrar enquanto usuário espera

    // Firebase
    FirebaseAuth mAuth; // Autenticador
    FirebaseUser mUser; // Usuário
    FirebaseDatabase mDatabase; // Database
    DatabaseReference DataRef;
    FirebaseStorage mStorage; // Storage
    StorageReference StorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Fullscreen pra remover a barra de cima do celular
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inicializações
        jaPossuoConta = findViewById(R.id.jaPossuoConta);
        inputImage = findViewById(R.id.inputImage);
        fabCamera = findViewById(R.id.fabCamera);
        inputEmail = findViewById(R.id.inputEmail);
        inputNome = findViewById(R.id.inputNome);
        inputPhone = findViewById(R.id.inputPhone);
        inputSenha = findViewById(R.id.inputSenha);
        inputConfirmarSenha = findViewById(R.id.inputConfirmarSenha);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        DataRef = mDatabase.getReference();
        mStorage = FirebaseStorage.getInstance();
        StorageRef = mStorage.getReference();

        // Trocar imagem do perfil escolhendo da galeria
        inputImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent();
                gallery.setType("image/*");
                gallery.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(gallery, "Escolha a Imagem"), PICK_IMAGE);
            }
        });

        // Trocar imagem do perfil tirando foto da camera
        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(camera, CAMERA_IMAGE);
            }
        });

        // Trocar pra tela de Login
        jaPossuoConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });

        // Listener do botão de Registrar
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Abaixar teclado
                InputMethodManager manager = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );
                manager.hideSoftInputFromWindow(inputConfirmarSenha.getApplicationWindowToken(), 0);

                // Chamar método de autenticação
                PerformAuth();
            }
        });
    }

    // Fazer autenticação com base nos dados inseridos
    private void PerformAuth() {
        String email = inputEmail.getText().toString();
        String nome = inputNome.getText().toString();
        String phone = inputPhone.getText().toString();
        String senha = inputSenha.getText().toString();
        String confirmarSenha = inputConfirmarSenha.getText().toString();

        if (!email.matches(emailPattern)) // Checar se não atende ao Padrão especificado de email
        {
            inputEmail.setError("Colocar um email válido");
            inputEmail.requestFocus();
        }
        else if (nome.isEmpty() || nome.length() < 3) // Checar a entrada do nome
        {
            inputNome.setError("Coloque um nome válido");
            inputNome.requestFocus();
        }
        else if (phone.isEmpty() || phone.length() < 9) // Checar o telefone
        {
            inputPhone.setError("Coloque um telefone válido");
            inputPhone.requestFocus();
        }
        else if (senha.isEmpty() || senha.length() < 6) // Checar tamanho da senha
        {
            inputSenha.setError("Tamanho no mínimo 6");
            inputSenha.requestFocus();
        }
        else if (!senha.equals(confirmarSenha)) // Checar se as duas senhas são diferentes
        {
            inputConfirmarSenha.setError("As senhas não são iguais");
            inputConfirmarSenha.requestFocus();
        }
        else // Se chegou aqui deu tudo certo
        {
            progressDialog.setMessage("Espere enquanto registra...");
            progressDialog.setTitle("Registrar");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            // Criar usuário com o Autenticador, não pode usar valores nulos!
            mAuth.createUserWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        // Usuário criado
                        mUser = mAuth.getCurrentUser();

                        // Salvar usuário no banco de dados
                        User newUser = new User ();
                        newUser.setName(nome);
                        newUser.setEmail(email);
                        newUser.setPhone(phone);
                        createUser(newUser);
                    }
                    else
                    {
                        // Caso dê erro ao tentar registrar no firebase
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                ""+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Método que cria um usuário no banco de dados
    private void createUser(User user) {
        DatabaseReference mAllUsersJSON = DataRef.child("users");
        DatabaseReference mUserJSON = mAllUsersJSON.child(mUser.getUid());
        user.setProfilePhotoId(mUserJSON.push().getKey());

        if (mUser != null) {
            mUserJSON.setValue(user)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // Upar foto do perfil do usuário no storage
                        uploadImage(user.getProfilePhotoId());
                    } else {
                        // Caso dê erro ao tentar registrar no firebase
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                ""+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Método que envia a foto do perfil pro storage
    private void uploadImage(String profilePhotoId) {
        Bitmap bitmap = ((BitmapDrawable) inputImage.getDrawable()).getBitmap();
        Uri perfilFotoUri = getImageUri(getApplicationContext(), bitmap);
        StorageReference mPerfilFotoJSON = StorageRef.child("usersProfilePhoto");

        if (mUser != null) {
            mPerfilFotoJSON.child(profilePhotoId + ".png").putFile(perfilFotoUri)
            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        // Fechar o dialog de progresso e mostrar mensagem
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                "Registrado com sucesso!", Toast.LENGTH_SHORT).show();
                        // Passar usuário pra próxima activity
                        sendUserToNextActivity();
                    } else {
                        // Caso dê erro ao tentar registrar no firebase
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this,
                                ""+task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Método que pega o Uri dado um Bitmap
    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(RegisterActivity.this.getContentResolver(), inImage, UUID.randomUUID().toString() + ".png", "drawing");
        return Uri.parse(path);
    }

    // Método que manda para a próxima activity
    private void sendUserToNextActivity() {
        Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);

        // Parar a activity atual e passar a bola pra próxima
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    // Results das activities para setar a imagem de perfil
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            Uri imageURI = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageURI);
                inputImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == CAMERA_IMAGE && resultCode == RESULT_OK)
        {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            inputImage.setImageBitmap(bitmap);
        }
    }
}