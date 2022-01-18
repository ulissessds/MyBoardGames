package com.example.myboardgames;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // Declarar os widgets
    TextView criarNovaConta;
    EditText inputEmail, inputSenha;
    Button btnLogin;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; // Regex pattern para email

    ProgressDialog progressDialog; // Dialog para mostrar enquanto usuário espera

    FirebaseAuth mAuth; // Autenticador do Firebase
    FirebaseUser mUser; // Usuário do Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fullscreen pra remover a barra de cima do celular
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inicializações
        criarNovaConta = findViewById(R.id.criarNovaConta);
        inputEmail = findViewById(R.id.inputEmail);
        inputSenha = findViewById(R.id.inputSenha);
        btnLogin = findViewById(R.id.btnLogin);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        // Trocar pra tela de Registrar
        criarNovaConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });

        // Listener do botão de Login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Abaixar teclado
                InputMethodManager manager = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );
                manager.hideSoftInputFromWindow(inputSenha.getApplicationWindowToken(), 0);

                // Chamar método que autentica o login
                performLogin();
            }
        });
    }

    // Método que autentica o Login
    private void performLogin() {
        String email = inputEmail.getText().toString();
        String senha = inputSenha.getText().toString();

        if (!email.matches(emailPattern)) // Checar se não atende ao Padrão especificado de email
        {
            inputEmail.setError("Colocar um email válido");
            inputEmail.requestFocus();
        }
        else if (senha.isEmpty() || senha.length() < 6) // Checar tamanho da senha
        {
            inputSenha.setError("Tamanho no mínimo 6");
            inputSenha.requestFocus();
        }
        else // Se chegou aqui deu tudo certo
        {
            progressDialog.setMessage("Espere enquanto faz Login...");
            progressDialog.setTitle("Login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            // Tentar signin no firebase
            mAuth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                // Fechar o dialog de progresso e mostrar mensagem
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this,
                                        "Login feito com sucesso!", Toast.LENGTH_SHORT).show();
                                // Passar usuário pra próxima activity
                                sendUserToNextActivity();
                            }
                            else
                            {
                                // Caso dê erro ao tentar fazer login no firebase
                                progressDialog.dismiss();
                                Toast.makeText(MainActivity.this,
                                        ""+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    // Método que manda para a próxima activity
    private void sendUserToNextActivity() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);

        // Parar a activity atual e passar a bola pra próxima
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }
}