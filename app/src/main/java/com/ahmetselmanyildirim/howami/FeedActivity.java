package com.ahmetselmanyildirim.howami;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<String> userEmailFromFB;
    ArrayList<String> userExplanationFromFB;
    ArrayList<String> userImageFromFB;
    FeedRecyclerAdapter feedRecyclerAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        userExplanationFromFB = new ArrayList<String>();
        userEmailFromFB = new ArrayList<String>();
        userImageFromFB = new ArrayList<String>();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getDataFromFireStore();

        //RecyclerView

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedRecyclerAdapter = new FeedRecyclerAdapter(userEmailFromFB,userExplanationFromFB,userImageFromFB);
        recyclerView.setAdapter(feedRecyclerAdapter);

    }

    public void getDataFromFireStore(){

        CollectionReference collectionReference = firebaseFirestore.collection("Posts");
        //verileri veritabanından almak
        collectionReference.orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null){
                    Toast.makeText(FeedActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }

                if(queryDocumentSnapshots != null){
                    //databasedeki dökümanları dizisine ulaşmak
                    for(DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){

                        Map<String,Object> data = snapshot.getData();

                        //Casting
                        String explanation = (String) data.get("explanation");
                        String userEmail = (String) data.get("useremail");
                        String downloadURL = (String) data.get("downloadURL");

                        //Recycler viewe eklemek için Array Liste ekleme
                        userExplanationFromFB.add(explanation);
                        userEmailFromFB.add(userEmail);
                        userImageFromFB.add(downloadURL);

                        System.out.println(userExplanationFromFB);

                        feedRecyclerAdapter.notifyDataSetChanged();

                    }
                }
            }
        });
    }

    public void addPost(View view){

        Intent intentToUpload = new Intent(FeedActivity.this, UploadActivity.class);
        startActivity(intentToUpload);

    }

    public void signOut(View view){

        firebaseAuth.signOut();

        Intent intentToSignUp = new Intent(FeedActivity.this, SignUpActivity.class);
        startActivity(intentToSignUp);
        finish();

    }

}
