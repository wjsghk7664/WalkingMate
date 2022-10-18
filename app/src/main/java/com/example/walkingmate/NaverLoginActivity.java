package com.example.walkingmate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

public class NaverLoginActivity extends AppCompatActivity {

    private static final String TAG = "NaverLoginActivity";

    //client 정보
    private static String OAUTH_CLIENT_ID = "44zyTYNDrWVbWum6tiX8";
    private static String OAUTH_CLIENT_SECRET = "_c8Q0OS6Mj";
    private static String OAUTH_CLIENT_NAME = "WalkingMate";

    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;

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
                //mOauthRT.setText(refreshToken);
                //mOauthExpires.setText(String.valueOf(expiresAt));
                //mOauthTokenType.setText(tokenType);
                //mOAuthState.setText(mOAuthLoginInstance.getState(mContext).toString());

                redirectSignupActivity();
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        };
    };

    // 성공 후 이동할 액티비티
    protected void redirectSignupActivity() {
        final Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}