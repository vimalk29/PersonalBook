package com.cdac.personalbook.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cdac.personalbook.POJO.DiaryContentsPojo;
import com.cdac.personalbook.R;
import com.cdac.personalbook.adapter.DiaryContentsAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<DiaryContentsPojo> arrayList;
    RecyclerView recyclerView;
    DiaryContentsAdapter adapter;
    Button addButton;
    ImageButton profileButton;
    FirebaseUser user;
    TextView emptyText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        recyclerView = findViewById(R.id.list_view);
        addButton = findViewById(R.id.addButton);
        profileButton = findViewById(R.id.profileImageButton);
        emptyText = findViewById(R.id.emptyList);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        addButton.setOnClickListener(this);
        profileButton.setOnClickListener(this);

        user = FirebaseAuth.getInstance().getCurrentUser();
        arrayList = new ArrayList<>();
        adapter = new DiaryContentsAdapter(arrayList, DashboardActivity.this);
        recyclerView.setAdapter(adapter);

        populateContent(user.getUid());
    }
    private void populateContent(String id){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(id);
        databaseReference.child("data").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayList.clear();
                if (dataSnapshot.exists()){
                    emptyText.setText("Loading...");
                    for (DataSnapshot content: dataSnapshot.getChildren()){
                        arrayList.add(content.getValue(DiaryContentsPojo.class));
                    }
                    adapter.notifyDataSetChanged();
                    emptyText.setVisibility(View.GONE);
                }else {
                    emptyText.setText("Empty");
                    emptyText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.addButton:
                startActivity(new Intent(DashboardActivity.this, MakeEntry.class));
                break;
            case R.id.profileImageButton:
                startActivity(new Intent(DashboardActivity.this, Profile.class));
                break;
        }
    }
}
