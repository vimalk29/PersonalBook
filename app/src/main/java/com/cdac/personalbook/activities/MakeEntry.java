package com.cdac.personalbook.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cdac.personalbook.POJO.DiaryContentsPojo;
import com.cdac.personalbook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MakeEntry extends AppCompatActivity implements View.OnClickListener {

    private String entryId, strDate;
    private ImageButton editButton;
    private Button saveButton;
    private TextInputEditText titleEditText, contentEditText;
    private TextView date;
    private AVLoadingIndicatorView progressbar;
    private FirebaseUser user;
    private DiaryContentsPojo diaryContentsPojo = new DiaryContentsPojo();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_entry);

        getSupportActionBar().setTitle("Entry");

        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        titleEditText = findViewById(R.id.titleBox);
        contentEditText = findViewById(R.id.contentBox);
        progressbar = findViewById(R.id.progressBar);
        date = findViewById(R.id.date);
        Intent intent = getIntent();
        entryId = intent.getStringExtra("id");
        user = FirebaseAuth.getInstance().getCurrentUser();

        saveButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        strDate  = new SimpleDateFormat("dd-MM-YYYY", Locale.getDefault()).format(new Date());
        Log.d("entryId", "onCreate: "+ entryId);
        if(entryId == null){
            editButton.setVisibility(View.GONE);
            enableButtons(true);
            date.setText(strDate);
        }else{
            enableButtons(false);
            getEntry();
        }
    }
    private void uploadEntry(){
        //enableButtons(false);
        String strTitle, strContent;
        strTitle = titleEditText.getText().toString();
        strContent = contentEditText.getText().toString();
        Log.d("entry", "uploadEntry: "+strTitle+"  "+ strContent);
        if (strContent.length()== 0) {
            Toast.makeText(MakeEntry.this, "Content Can not be Empty", Toast.LENGTH_SHORT).show();
        }else{
            if (strTitle.length()== 0){
                strTitle = strContent.substring(0,5)+"...";
                titleEditText.setText(strTitle);
            }
            if (entryId == null){
                entryId = databaseReference.push().getKey();
            }
            diaryContentsPojo.setId(entryId);
            diaryContentsPojo.setTitle(strTitle);
            diaryContentsPojo.setContent(strContent);
            diaryContentsPojo.setDate(strDate);
            databaseReference.child("User").child(user.getUid()).child("data")
                    .child(entryId).setValue(diaryContentsPojo).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    getSupportActionBar().setTitle(diaryContentsPojo.getTitle());
                    Toast.makeText(MakeEntry.this,"Entry Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    editButton.setVisibility(View.VISIBLE);
                    enableTextBox(false);
                }
            });
        }
        enableButtons(true);
    }
    private void getEntry(){
        databaseReference.child("User").child(user.getUid()).child("data")
                .child(entryId).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                diaryContentsPojo = dataSnapshot.getValue(DiaryContentsPojo.class);
                titleEditText.setText(diaryContentsPojo.getTitle());
                contentEditText.setText(diaryContentsPojo.getContent());
                date.setText(diaryContentsPojo.getDate());
                strDate = diaryContentsPojo.getDate();

                getSupportActionBar().setTitle(diaryContentsPojo.getTitle());
                date.setText(strDate);
                editButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MakeEntry.this, "Error connecting to database!", Toast.LENGTH_SHORT).show();
                progressbar.hide();
            }
        });
        enableButtons(true);
        enableTextBox(false);
    }

    private void enableButtons(boolean b) {
        if(b)
            progressbar.hide();
        else
            progressbar.show();
        saveButton.setEnabled(b);
        editButton.setEnabled(b);
        contentEditText.setEnabled(b);
        titleEditText.setEnabled(b);
    }
    private void enableTextBox(boolean enable){
        contentEditText.setEnabled(enable);
        titleEditText.setEnabled(enable);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editButton:
                Toast.makeText(MakeEntry.this,"Edit",Toast.LENGTH_SHORT).show();
                enableTextBox(true);
                break;
            case R.id.saveButton:
                Toast.makeText(MakeEntry.this,"Save",Toast.LENGTH_SHORT).show();
                uploadEntry();
                break;
        }
    }
}
