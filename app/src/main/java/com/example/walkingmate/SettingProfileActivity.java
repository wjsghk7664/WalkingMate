package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

public class SettingProfileActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage=FirebaseStorage.getInstance();
    StorageReference storageReference=storage.getReference();

    boolean checkdup;

    ImageView profile;
    EditText appnametxt;

    TextView curappname;
    Bitmap bitmap=null;

    String userid,age,gender,name,nickname,birthyear, finalappname, appname,profileImage;

    Uri downloadUri;

    HashMap<String, Object> user=new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);

        finalappname="";
        profileImage="";

        curappname=findViewById(R.id.curappname);

        Intent userdata=getIntent();
        userid=userdata.getStringExtra("userid");
        age=userdata.getStringExtra("age");
        gender=userdata.getStringExtra("gender");
        name=userdata.getStringExtra("name");
        nickname=userdata.getStringExtra("nickname");
        birthyear=userdata.getStringExtra("birthyear");

        appnametxt=findViewById(R.id.userappname);

        profile=findViewById(R.id.profileImage);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryintent=new Intent();
                galleryintent.setType("image/*");
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryintent,1);
            }
        });

        findViewById(R.id.profiledefault).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap=null;
                profile.setImageResource(R.drawable.blank_profile);
            }
        });

        findViewById(R.id.checkdup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appname="";
                appname+=appnametxt.getText().toString();
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

        findViewById(R.id.setprofile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(finalappname.equals("")){
                    return;
                }
                else{
                    findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    if(bitmap!=null){
                        uploadImage();
                    }
                    else{
                        user.put("nickname",nickname);
                        user.put("name",name);
                        user.put("age",age);
                        user.put("gender",gender);
                        user.put("birthyear",birthyear);
                        user.put("appname",finalappname);
                        user.put("profileImage",profileImage);
                        db.collection("users").document(userid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(getApplicationContext(),"회원가입 성공.",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SettingProfileActivity.this, FeedCalendarActivity.class));
                                finish();
                            }
                        });
                    }
                }

            }
        });

    }

    public void uploadImage(){
        StorageReference uploadRef=storageReference.child(userid+"_profile.jpg");
        Log.d("로그인_이미지명",userid+"_profile.jpg");

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
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
                            user.put("nickname",nickname);
                            user.put("name",name);
                            user.put("age",age);
                            user.put("gender",gender);
                            user.put("birthyear",birthyear);
                            user.put("appname",finalappname);
                            user.put("profileImage",profileImage);
                            user.put("title","없음");

                            db.collection("users").document(userid).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(),"회원가입 성공.",Toast.LENGTH_SHORT).show();
                                    UserData.saveData(new UserData(userid,profileImage,appname,nickname,name,age,gender,birthyear,"없음"),SettingProfileActivity.this);
                                    UserData.saveBitmapToJpeg(bitmap,SettingProfileActivity.this);
                                    startActivity(new Intent(SettingProfileActivity.this, FeedCalendarActivity.class));
                                    finish();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            try{
                InputStream in=getContentResolver().openInputStream(data.getData());
                bitmap= BitmapFactory.decodeStream(in);
                profile.setImageBitmap(bitmap);
                in.close();
            }catch (Exception e){e.printStackTrace();}
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,StartActivity.class));
        finish();
    }
}