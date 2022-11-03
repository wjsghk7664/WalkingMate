package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class StartActivity extends AppCompatActivity {

    TextView StartText,permlist;

    Button permissionbtn;

    String reqperm;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "NaverLoginActivity";

    //client 정보
    private static String OAUTH_CLIENT_ID = "44zyTYNDrWVbWum6tiX8";
    private static String OAUTH_CLIENT_SECRET = "_c8Q0OS6Mj";
    private static String OAUTH_CLIENT_NAME = "WalkingMate";

    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;
    private NaverUserModel model;



    private OAuthLoginButton mOAuthLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        StartText=findViewById(R.id.StartText);
        permlist=findViewById(R.id.permlist);
        permissionbtn=findViewById(R.id.permission);
        mOAuthLoginButton=findViewById(R.id.buttonOAuthLoginImg);

        YoYo.with(Techniques.FadeIn).duration(1000).repeat(0).playOn(StartText);

        mContext=this;


        //백그라운드 위치권한은 요청이 불가능 하므로 직접 설정하도록 유도해 줘야함.
        //메인 화면에서 백그라운드 권한 요청이 안되어 있는경우 화면에 보여서 권한 승인 화면으로 가도록 유도
        String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION,Manifest.permission.READ_EXTERNAL_STORAGE};


        ActivityCompat.requestPermissions(this, Permissions, 1);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            mOAuthLoginButton.setVisibility(View.INVISIBLE);
            permissionbtn.setVisibility(View.VISIBLE);
            permlist.setVisibility(View.VISIBLE);
        }
        else{
            String useridcheck= UserData.scanUserData(this);
            if(useridcheck==null){
                useridcheck="없음";
            }
            else{
                useridcheck=useridcheck.replace(".txt","");
            }
            db.collection("users").document(useridcheck).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.getResult().exists()){
                        mOAuthLoginButton.setVisibility(View.INVISIBLE);
                        mOAuthLoginInstance.startOauthLoginActivity(StartActivity.this,mOAuthLoginHandler);
                    }
                    else{
                        mOAuthLoginButton.setVisibility(View.VISIBLE);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"서버접속에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            permissionbtn.setVisibility(View.INVISIBLE);
            permlist.setVisibility(View.INVISIBLE);
            Log.d("로그인_자동로그인체크",useridcheck);
        }

        reqperm="";
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="위치권한: 항상 허용으로 설정\n";
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="신체활동 권한: 허용으로 설정\n";
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            reqperm+="파일 및 미디어 권한: 허용으로 설정";
        }
        permlist.setText(reqperm);

        permissionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recogIntent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:com.example.walkingmate"));
                startActivity(recogIntent);
            }
        });

        //초기화
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            mOAuthLoginButton.setVisibility(View.INVISIBLE);
            permissionbtn.setVisibility(View.VISIBLE);
            permlist.setVisibility(View.VISIBLE);
        }
        else{
            mOAuthLoginButton.setVisibility(View.VISIBLE);
            permissionbtn.setVisibility(View.INVISIBLE);
            permlist.setVisibility(View.INVISIBLE);
        }
        reqperm="";
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="위치권한: 항상 허용으로 설정\n";
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED){
            reqperm+="신체활동 권한: 허용으로 설정";
        }
        permlist.setText(reqperm);
    }

    private void initData() {
        //초기화
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);

        //custom img로 변경시 사용
        //mOAuthLoginButton.setBgResourceId(R.drawable.btn_naver_white_kor);
    }

    /**
     * OAuthLoginHandler를 startOAuthLoginActivity() 메서드 호출 시 파라미터로 전달하거나 OAuthLoginButton
     객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다.
     */
    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                String refreshToken = mOAuthLoginInstance.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginInstance.getExpiresAt(mContext);
                String tokenType = mOAuthLoginInstance.getTokenType(mContext);
                Log.d(TAG, "success : " + accessToken);
                Log.d(TAG, "expiresAt : " + Long.toString(expiresAt));
                getUser(accessToken);


                //mOauthRT.setText(refreshToken);
                //mOauthExpires.setText(String.valueOf(expiresAt));
                //mOauthTokenType.setText(tokenType);
                //mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        };
    };

    private void getUser(String token){
        new StartActivity.GetUserTask().execute(token);

    }

    private class GetUserTask extends ThreadTask<String, String>{
        @Override
        protected String doInBackground(String s) {
            String header = "Bearer " + s;
            String url = "https://openapi.naver.com/v1/nid/me";

            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", header);
            String responseBody = get(url, requestHeaders);

            return responseBody;
        }

        private String get(String url, Map<String, String> requestHeaders){
            HttpURLConnection connection = connect(url);
            try {
                connection.setRequestMethod("GET");
                for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return readBody(connection.getInputStream());
                } else {
                    return readBody(connection.getErrorStream());
                }
            } catch (IOException e) {
                throw new RuntimeException("API 요청 및 응답 실패");
            } finally {
                connection.disconnect();
            }
        }

        private HttpURLConnection connect(String apiurl){
            try{
                URL url = new URL(apiurl);
                return (HttpURLConnection)url.openConnection();
            } catch (MalformedURLException e) {
                throw new RuntimeException("API URL이 잘못되었습니다. : " + apiurl, e);
            } catch (IOException e) {
                throw new RuntimeException("연결을 실패했습니다. : " + apiurl, e);
            }
        }

        private String readBody(InputStream body){
            InputStreamReader streamReader = new InputStreamReader(body);
            try(BufferedReader lineReader = new BufferedReader(streamReader)){
                StringBuilder responseBody = new StringBuilder();
                String line;
                while((line = lineReader.readLine()) != null){
                    responseBody.append(line);
                }
                return responseBody.toString();
            } catch (IOException e) {
                throw new RuntimeException("API 응답을 읽는데 실패했습니다. ", e);
            }
        }

        /*
        nickname, email, gender, birthday 외에도 회원 이름(본명?), 프로필 사진, 연령대도 가져올 수 있음.
         */
        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                if (jsonObject.getString("resultcode").equals("00")) {
                    JSONObject object = new JSONObject(jsonObject.getString("response"));
                    String id = object.getString("id");
                    String nickname = object.getString("nickname");
                    String name = object.getString("name");
                    String age = object.getString("age");
                    String gender = object.getString("gender");
                    String birthyear = object.getString("birthyear");
                    model = new NaverUserModel(id, nickname, name, age, gender, birthyear);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Map<String,String> user = new HashMap<>();

            user.put("nickname",model.getNickname());
            user.put("name",model.getName());
            user.put("age",model.getAge());
            user.put("gender",model.getGender());
            user.put("birthyear",model.getBirthyear());

            db.collection("users").document(model.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.getResult().exists()){
                        Log.d("시작로그인","success");
                        permlist.setVisibility(View.VISIBLE);
                        permlist.setText("접속중...");
                        UserData userDatatmp=new UserData(task.getResult().getId(), (String) task.getResult().get("profileImage"),
                                (String) task.getResult().get("appname"), model.getNickname(),model.getName(),model.getAge(),
                                model.getGender(),model.getBirthyear(),(String) task.getResult().get("title"),
                                (Long)task.getResult().get("reliability"));
                        UserData.saveData(userDatatmp,StartActivity.this);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UserData.saveBitmapToJpeg(UserData.GetBitmapfromURL((String) task.getResult().get("profileImage")),StartActivity.this);
                            }
                        }).start();

                        db.collection("users").document(userDatatmp.userid).set(UserData.getHashmap(userDatatmp)).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(StartActivity.this,FeedCalendarActivity.class));
                                finish();
                            }
                        });

                    }
                    else{
                        Intent setprofile=new Intent(StartActivity.this,SettingProfileActivity.class);
                        setprofile.putExtra("nickname",model.getNickname());
                        setprofile.putExtra("name",model.getName());
                        setprofile.putExtra("birthyear",model.getBirthyear());
                        setprofile.putExtra("userid",model.getId());
                        setprofile.putExtra("age",model.getAge());
                        setprofile.putExtra("gender",model.getGender());
                        startActivity(setprofile);
                        finish();
                    }
                }
            });


        }

    }
}