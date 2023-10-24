package com.example.outfitrateripro;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText nameField, phoneField;
    private Button backBtn, confirmBtn;
    private ImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private String userId, name, phone, profileImageUrl;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        nameField = findViewById(R.id.name);
        phoneField = findViewById(R.id.phone);
        profileImage = findViewById(R.id.profileImage);
        backBtn = findViewById(R.id.back_button);
        confirmBtn = findViewById(R.id.confirm_button);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);


        getUserInfo();

        profileImage.setOnClickListener(view -> openGallery());

        confirmBtn.setOnClickListener(view -> saveUserInformation());
        backBtn.setOnClickListener(view -> finish());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    // Declare an ActivityResultLauncher for picking an image
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    final Uri imageUri = result.getData().getData();
                    resultUri = imageUri;
                    profileImage.setImageURI(resultUri);
                }
            }
    );

    private void getUserInfo() {
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    if (map != null) {
                        name = map.get("name") != null ? map.get("name").toString() : "";
                        phone = map.get("phone") != null ? map.get("phone").toString() : "";
                        profileImageUrl = map.get("profileImageUrl") != null ? map.get("profileImageUrl").toString() : "";

                        nameField.setText(name);
                        phoneField.setText(phone);

                        // Load the profile image using Glide
                        if (!profileImageUrl.isEmpty()) {
                            Glide.with(getApplicationContext()).load(profileImageUrl).into(profileImage);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    private void saveUserInformation() {
        name = nameField.getText().toString();
        phone = phoneField.getText().toString();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", name);
        userInfo.put("phone", phone);

        userDatabase.updateChildren(userInfo);

        if (resultUri != null) {
            uploadProfileImage();
        } else {
            finish();
        }
    }

    private void uploadProfileImage() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages").child(userId);

        try {
            Bitmap bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), resultUri));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageReference.putBytes(data);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    Map<String, Object> imageInfo = new HashMap<>();
                    imageInfo.put("profileImageUrl", downloadUrl);
                    userDatabase.updateChildren(imageInfo);

                    finish();
                });
            }).addOnFailureListener(e -> {
                // Handle the failure to upload the image
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
