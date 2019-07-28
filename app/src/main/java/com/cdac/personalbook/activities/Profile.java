package com.cdac.personalbook.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cdac.personalbook.R;
import com.cdac.personalbook.common.AlertMessage;
import com.cdac.personalbook.common.CommonValues;
import com.cdac.personalbook.common.SelectImageHelper;
import com.cdac.personalbook.common.SessionManagement;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.io.InputStream;

public class Profile extends AppCompatActivity implements View.OnClickListener {

    private ImageButton  editProfileInfo;
    private Button editProfilePic, btnLogout;
    private ImageView profilePic;
    private TextView tvName, tvEmail, tvMobile;
    private AVLoadingIndicatorView progressbar;
    private FirebaseAuth firebaseAuth;
    private SelectImageHelper helper;
    private FirebaseUser user;
    private Context context;
    private Uri profileImgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context=this;
        tvName = findViewById(R.id.textViewUsernameSurname);
        tvEmail = findViewById(R.id.textViewEmailProfile);
        tvMobile = findViewById(R.id.textViewPhoneProfile);
        profilePic = findViewById(R.id.imageViewProfile);
        editProfilePic = findViewById(R.id.buttonEditProfilePicture);
        editProfileInfo = findViewById(R.id.buttonEditProfileInfo);
        btnLogout = findViewById(R.id.buttonLogout);
        progressbar = findViewById(R.id.progressBar);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        editProfilePic.setVisibility(View.GONE);
        editProfilePic.setOnClickListener(this);
        editProfileInfo.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        editProfilePic.setOnClickListener(this);
        profilePic.setOnClickListener(this);
        helper = new SelectImageHelper(this, profilePic);

        loadData();
    }

    private void loadData() {
        enableButtons(false);
        if(user!=null){
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri pic = user.getPhotoUrl();
            if(name==null || name.isEmpty())
                name = "Name";
            if(email==null || email.isEmpty())
                email= "Email";
            if(pic!=null)
                Picasso.with(this).load(pic).into(profilePic, new Callback() {
                    @Override
                    public void onSuccess() {
                        enableButtons(true);
                    }
                    @Override
                    public void onError() {
                        enableButtons(true);
                    }
                });
            else
                enableButtons(true);

            tvName.setText(name);
            tvEmail.setText(email);
            tvMobile.setText(user.getPhoneNumber());

        }
    }

    private void enableButtons(boolean b) {
        if(b)
            progressbar.hide();
        else
            progressbar.show();
        editProfilePic.setEnabled(b);
        btnLogout.setEnabled(b);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imageViewProfile:
                    helper.selectImageOption();
                    editProfilePic.setVisibility(View.VISIBLE);
                break;

            case R.id.buttonEditProfilePicture:
                profileImgPath = helper.getURI_FOR_SELECTED_IMAGE();
                if (profileImgPath != null){
                    progressbar.show();
                    final StorageReference ref = FirebaseStorage.getInstance().getReference().child("profileImg/"+user.getUid());
                    CommonValues.storageTask = ref.putFile(profileImgPath)
                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(context, "updated successfully", Toast.LENGTH_SHORT).show();
                                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                updateUserProfile(user.getDisplayName(),uri);
                                            }
                                        });
                                        editProfilePic.setVisibility(View.GONE);
                                    }else{
                                        Toast.makeText(context, "Couldn't upload", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    progressbar.hide();
                }else
                    Toast.makeText(this, "Please select an Image", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonEditProfileInfo:
                takeUserInput();
                break;
            case R.id.buttonLogout:
                LogOut();
                break;
        }
    }

    private void takeUserInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogBox = this.getLayoutInflater().inflate(R.layout.dialog_edit_info, null);
        final EditText editTextName = dialogBox.findViewById(R.id.editProfileName);
        final EditText editTextEmail = dialogBox.findViewById(R.id.editProfileEmail);
        //final EditText editTextPhone = dialogBox.findViewById(R.id.editProfileNumber);

        editTextName.setText(user.getDisplayName());
        editTextEmail.setText(user.getEmail());
        //editTextPhone.setText(user.getPhoneNumber());

        builder.setTitle("Enter Info")
                .setView(dialogBox)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strname= editTextName.getText().toString().trim();
                        String strEmail = editTextEmail.getText().toString();
                        //String strNumber = editTextPhone.getText().toString();
                        if( !strname.equals("")&& strname.matches("^[a-zA-Z\\s]*$")){
                            updateUserProfile(strname,user.getPhotoUrl());
                        }
                        else
                            Toast.makeText(context,"Invalid Name", Toast.LENGTH_LONG).show();
                        if((!TextUtils.isEmpty(strEmail) && Patterns.EMAIL_ADDRESS.matcher(strEmail).matches())){
                            enableButtons(false);
                            user.updateEmail(strEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    loadData();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    if(e instanceof FirebaseAuthRecentLoginRequiredException){
                                        AlertMessage.showMessageDialog((Profile) context, "Session time out...Login Again to continue...", new AlertMessage.OkListener() {
                                            @Override
                                            public void onOkClicked() {
                                                LogOut();
                                            }
                                        });
                                    }
                                }
                            })
                            ;
                        }else
                            Toast.makeText(context,"Invalid Email", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        builder.show();
    }

    public void updateUserProfile(String username,Uri pic){
        enableButtons(false);
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(pic)
                .build();
        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadData();
            }
        });
    }

    private void LogOut() {
        new SessionManagement(this).logOut();
        startActivity(new Intent(Profile.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        helper.handleResult(requestCode, resultCode, result);  // call this helper class method
    }
    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        helper.handleGrantedPermisson(requestCode, grantResults);   // call this helper class method
    }

}
