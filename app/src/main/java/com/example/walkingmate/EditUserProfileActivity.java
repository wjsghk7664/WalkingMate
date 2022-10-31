package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditUserProfileActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage=FirebaseStorage.getInstance();
    StorageReference storageReference=storage.getReference();

    UserData userData;

    EditText username;
    Spinner usertitle;
    ImageButton back;
    Button setfinish,resetimg,editimg,checkname;
    CircleImageView circleImageView;
    TextView curappname,loading;

    Bitmap curimg;

    String appname,finalappname,profileImage;

    Uri downloadUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        userData=UserData.loadData(this);
        appname="";
        finalappname=userData.appname;
        profileImage="";

        username=findViewById(R.id.userappname_userset);
        usertitle=findViewById(R.id.titlespin_userset);//이부분은 구현되면 추가
        back=findViewById(R.id.back_userset);
        setfinish=findViewById(R.id.setprofile_userset);
        resetimg=findViewById(R.id.profiledefault_userset);
        editimg=findViewById(R.id.profileEdit_userset);
        checkname=findViewById(R.id.checkdup_userset);
        circleImageView=findViewById(R.id.profileImage_userset);
        curappname=findViewById(R.id.curappname_userset);
        loading=findViewById(R.id.loading_userset);

        curappname.setText("현재 선택한 닉네임: "+finalappname);

        curimg=UserData.loadImageToBitmap(this);
        if(curimg==null){
            curimg=BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.blank_profile);
        }
        circleImageView.setImageBitmap(curimg);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),FeedCalendarActivity.class));
                finish();
            }
        });
        resetimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curimg=BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.blank_profile);
                circleImageView.setImageResource(R.drawable.blank_profile);
            }
        });
        editimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryintent=new Intent();
                galleryintent.setType("image/*");
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryintent,1);
            }
        });
        checkname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appname="";
                appname+=username.getText().toString();
                if(appname.equals("")){
                    return;
                }
                db.collection("users").whereEqualTo("appname",appname).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(!task.getResult().getDocuments().isEmpty()){
                            Toast.makeText(getApplicationContext(),"이미 존재하는 닉네임입니다.",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"사용 가능한 닉네임입니다.",Toast.LENGTH_SHORT).show();
                            finalappname=appname;
                            curappname.setText("현재 선택한 닉네임: "+finalappname);
                        }
                    }
                });
            }
        });

        setfinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                uploadImage();
            }
        });
    }


    public void uploadImage(){
        StorageReference uploadRef=storageReference.child(userData.userid+"_profile.jpg");

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        curimg.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] datas=baos.toByteArray();

        UploadTask uploadTask=uploadRef.putBytes(datas);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"이미지 업로드 실패",Toast.LENGTH_SHORT).show();
                Log.d("로그인_이미지실패",e.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        // Continue with the task to get the download URL
                        return uploadRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUri = task.getResult();
                            profileImage=downloadUri.toString();
                            Log.d("로그인 프로필 이미지 url",downloadUri.toString());
                            userData.appname=finalappname;
                            userData.profileImage=profileImage;
                            UserData.saveBitmapToJpeg(curimg,EditUserProfileActivity.this);
                            //userData.title=title;

                            db.collection("users").document(userData.userid).set(UserData.getHashmap(userData)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(),"프로필 변경 완료.",Toast.LENGTH_SHORT).show();
                                    UserData.saveData(userData,EditUserProfileActivity.this);
                                    loading.setVisibility(View.INVISIBLE);
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(),"다시 시도해 주세요.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }


    @Override
    public void onBackPressed() {
        back.performClick();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            try{
                InputStream in=getContentResolver().openInputStream(data.getData());
                curimg= BitmapFactory.decodeStream(in);
                circleImageView.setImageBitmap(curimg);
                in.close();
            }catch (Exception e){e.printStackTrace();}
        }
    }
}