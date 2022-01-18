package com.example.myboardgames;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.WindowManager;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.myboardgames.fragment.BoardGameShelfFragment;
import com.example.myboardgames.fragment.ProfileFragment;
import com.example.myboardgames.fragment.ScanQRFragment;
import com.example.myboardgames.fragment.ShareQRCodeFragment;
import com.example.myboardgames.fragment.addBoardGameFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    // Declarar Meow Bottom Navigation Bar
    MeowBottomNavigation bottomNavBar;

    // Firebase
    FirebaseAuth mAuth; // Autenticador
    FirebaseUser mUser; // Usuário

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Fullscreen pra remover a barra de cima do celular
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inicializar
        bottomNavBar = findViewById(R.id.bottom_nav_bar);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        // Adicionar os ícones das abas
        bottomNavBar.add(new MeowBottomNavigation.Model(1, R.drawable.ic_board_games));
        bottomNavBar.add(new MeowBottomNavigation.Model(2, R.drawable.ic_baseline_share_24));
        bottomNavBar.add(new MeowBottomNavigation.Model(3, R.drawable.ic_baseline_add_a_photo_24));
        bottomNavBar.add(new MeowBottomNavigation.Model(4, R.drawable.ic_baseline_qr_code_scanner_24));
        bottomNavBar.add(new MeowBottomNavigation.Model(5, R.drawable.ic_baseline_perm_identity_24));

        // Tem que ter o método
        bottomNavBar.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
            }
        });

        // Iniciar na estante de BGs
        bottomNavBar.show(1, true);
        loadFragment(new BoardGameShelfFragment());

        // Método chamado quando clica em algum item do menu
        bottomNavBar.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
                // Mudar de aba de acordo com o botão clicado
                switch(item.getId()) {
                    case 1:
                        // Estante com todos os jogos
                        loadFragment(new BoardGameShelfFragment());
                        break;
                    case 2:
                        // Compartilhar QR code do jogo
                        loadFragment(ShareQRCodeFragment.newInstance(mUser.getUid()));
                        break;
                    case 3:
                        // Adicionar novo jogo
                        loadFragment(new addBoardGameFragment());
                        break;
                    case 4:
                        // Scanear outro usuário
                        loadFragment(ScanQRFragment.newInstance(""));
                        break;
                    case 5:
                        // Profile do usuário
                        loadFragment(new ProfileFragment());
                        break;
                }
            }
        });

        // Tem que ter o método
        bottomNavBar.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {
            }
        });
    }

    // Método para carregar um fragment no frame layout da home
    private void loadFragment(Fragment fragment) {
        //Trocar fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_root, fragment)
                .commit();
    }
}