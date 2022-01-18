package com.example.myboardgames.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myboardgames.R;
import com.example.myboardgames.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShareQRCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareQRCodeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String MY_USER_KEY = "myUserKey";

    // TODO: Rename and change types of parameters
    private String mMyUserKey;

    public ShareQRCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param myUserKey Parameter 1.
     * @return A new instance of fragment ShareQRCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShareQRCodeFragment newInstance(String myUserKey) {
        ShareQRCodeFragment fragment = new ShareQRCodeFragment();
        Bundle args = new Bundle();
        args.putString(MY_USER_KEY, myUserKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMyUserKey = getArguments().getString(MY_USER_KEY);
        }
    }

    // Widgets
    TextView nameUser;
    ImageView imageProfile, outputQRcode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share_q_r_code, container, false);

        // Inicializar
        nameUser = view.findViewById(R.id.nameUser);
        imageProfile = view.findViewById(R.id.imageProfile);
        outputQRcode = view.findViewById(R.id.outputQRcode);

        // Inicializar multi format writer
        MultiFormatWriter writer = new MultiFormatWriter();

        // Gerar o QR Code baseado no id do Usuário que está usando o app
        try {
            // Inicializar bit matrix
            BitMatrix matrix = writer.encode(mMyUserKey, BarcodeFormat.QR_CODE, 300, 300);
            // Inicializar barcode encoder
            BarcodeEncoder encoder = new BarcodeEncoder();
            // Inicializar bitmap
            Bitmap bitmap = encoder.createBitmap(matrix);
            // Setar bitmap no image view
            outputQRcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        // Colocar a imagem e o nome do usuário nos widgets
        DatabaseReference DataRef = FirebaseDatabase.getInstance().getReference().child("users");
        StorageReference StorageRef = FirebaseStorage.getInstance().getReference().child("usersProfilePhoto");
        DataRef.child(mMyUserKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Setar o nome do usuário
                User user = snapshot.getValue(User.class);
                nameUser.setText(user.getName());
                // Pegar a imagem e setar
                StorageReference profileImageJSON = StorageRef.child(user.getProfilePhotoId() + ".png");
                profileImageJSON.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri.toString()).into(imageProfile);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}