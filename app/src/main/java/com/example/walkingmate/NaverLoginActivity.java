package com.example.walkingmate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
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

public class NaverLoginActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_naver_login);

        mContext=this;

        //초기화
        initData();
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
                init();
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        };
    };

    private void getUser(String token){
        new GetUserTask().execute(token);

    }

    private void init() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
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

            Log.d(TAG, model.getId());

            db.collection("users").document(model.getId())
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "DocumentSnapshot successfully written!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    });

        }


    }

}

