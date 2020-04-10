package com.ahmetselmanyildirim.howami;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {

    //fotoğrafı kaydedebilmek için bitmap kullanılması gerekir.
    Bitmap selectedImage;
    ImageView imageView;
    EditText explanationText;
    Uri imageData;
    EditText downloadLink;

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        downloadLink = findViewById(R.id.downloadLink);

        imageView = findViewById(R.id.imageView2);
        explanationText = findViewById(R.id.explanationText);

        //deponun tanımlanması
        firebaseStorage = firebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

    }

    //Fotoğraf seçimi için buton
    public void selectImage(View view){
        //Galeriye erişim izninin verilip verilmediğini kontrol etme
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //eğer yoksa izin iste
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
        // izin olması durumunda galerinin açılması ve fotoğraf seçme
        else{
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }
    }

    //galeriye erişim izni verildiğinde yapılacaklar
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    //else'e girmediği durumda if de izin alınmışsa fotoğraf seçme
        if (requestCode == 1) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //galeriye gidilebildiyse yapılacaklar
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            //alınan fotoğrafın adresinin alınması
            imageData = data.getData();

            try {
                //28 ve üstü sdk'lerde getbitmap fonksiyonu çalışamayabileceği için kontrol yapmamız gerekir.
                //alınan fotoğrafın bitmap olarak kaydedilmesi
                if(Build.VERSION.SDK_INT >= 28){
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                }
                else{
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
                    imageView.setImageBitmap(selectedImage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //Database'e fotoğrafı yüklemek için buton
    public void upload (View view){

        UUID uuid = UUID.randomUUID();
        final String imageName = "images/" + uuid + ".jpg";

        if(imageData == null){
            Toast.makeText(UploadActivity.this,"Kardeşim Önce Görsel Seç",Toast.LENGTH_LONG).show();
        }
        else{

            storageReference.child(imageName).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Download URL
                    StorageReference newReference = FirebaseStorage.getInstance().getReference(imageName);
                    newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String downloadUrl = uri.toString();
                            downloadLink.setText(downloadUrl);


                        }
                    });


                    Toast.makeText(UploadActivity.this,"Resminiz kaydedilmiştir.".toString(),Toast.LENGTH_LONG).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage().toString(),Toast.LENGTH_LONG).show();
                }
            });

        }




    }
}
