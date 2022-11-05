package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

    String appname,finalappname,profileImagebig,profileImagesmall;


    Uri downloadUribig, downloadUrismall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        userData=UserData.loadData(this);
        appname="";
        finalappname=userData.appname;
        profileImagebig="";
        profileImagesmall="";

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
                Intent backpage=new Intent(getApplicationContext(),FeedCalendarActivity.class);
                setResult(RESULT_OK, backpage);
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
        String userid= userData.userid;
        String bigfilename=userid+"bigprofile.jpg";
        String smallfilename=userid+"smallprofile.jpg";

        Bitmap bitmap=UserData.getResizedImage(curimg,true);
        Bitmap smallbitmap=UserData.getResizedImage(curimg,false);

        StorageReference uploadRefbig=storageReference.child(bigfilename);
        StorageReference uploadRefsmall=storageReference.child(smallfilename);
        Log.d("이미지명_big",bigfilename);
        Log.d("이미지명_small",smallfilename);
        ByteArrayOutputStream baosbig=new ByteArrayOutputStream();
        ByteArrayOutputStream baossmall=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baosbig);
        smallbitmap.compress(Bitmap.CompressFormat.JPEG,100,baossmall);
        byte[] datasbig=baosbig.toByteArray();
        byte[] datassmall=baossmall.toByteArray();

        UploadTask uploadTask=uploadRefbig.putBytes(datasbig);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"이미지 업로드 실패",Toast.LENGTH_SHORT).show();
                Log.d("이미지실패",e.toString());
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
                        return uploadRefbig.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            downloadUribig = task.getResult();
                            profileImagebig=downloadUribig.toString();
                            Log.d("로그인 프로필 이미지big url",downloadUribig.toString());
                            UploadTask uploadTask1=uploadRefsmall.putBytes(datassmall);
                            uploadTask1.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Task<Uri> uriTask1=uploadTask1.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if(!task.isSuccessful()){
                                                throw task.getException();
                                            }

                                            return uploadRefsmall.getDownloadUrl();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if(task.isSuccessful()){
                                                downloadUrismall=task.getResult();
                                                profileImagesmall=downloadUrismall.toString();
                                                Log.d("로그인 프로필 이미지small url",downloadUrismall.toString());
                                                userData.appname=finalappname;
                                                userData.profileImagebig=profileImagebig;
                                                userData.profileImagesmall=profileImagesmall;
                                                UserData.saveBitmapToJpeg(bitmap,smallbitmap,EditUserProfileActivity.this);
                                                //userData.title=title;

                                                db.collection("users").document(userData.userid).set(UserData.getHashmap(userData)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getApplicationContext(),"프로필 변경 완료.",Toast.LENGTH_SHORT).show();
                                                        UserData.saveData(userData,EditUserProfileActivity.this);
                                                        loading.setVisibility(View.INVISIBLE);
                                                    }
                                                });
                                            }
                                        }
                                    });
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
                Bitmap tmpbmp= BitmapFactory.decodeStream(in);

                Uri uri=data.getData();
                Log.d("uri체크",uri.toString());
                Uri PhotoUri=Uri.parse(getRealPathFromURI(uri));
                ExifInterface exif=new ExifInterface(PhotoUri.getPath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                curimg = rotateBitmap(tmpbmp, orientation);




                circleImageView.setImageBitmap(curimg);
                in.close();
            }catch (Exception e){e.printStackTrace();}
        }
    }

    private String getRealPathFromURI(Uri contentUri) {

        if (contentUri.getPath().startsWith("/storage")) {
            return contentUri.getPath();
        }

        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}