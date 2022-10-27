package com.example.walkingmate;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.auth.User;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;


//로컬 저장소내 유저 데이터 관리

public class UserData {
    String userid;
    String profileImage;
    String appname;

    String nickname;
    String name;
    String age;
    String gender;
    String birthyear;

    //타이틀은 없으면 "없음"으로
    String title;

    public UserData(String userid, String profileImage, String appname, String nickname, String name, String age, String gender, String birthyear, String title) {
        this.userid = userid;
        this.profileImage = profileImage;
        this.appname = appname;
        this.nickname = nickname;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.birthyear = birthyear;
        this.title=title;
    }

    public static HashMap<String,Object> getHashmap(UserData userData){
        HashMap<String, Object> user=new HashMap<>();
        user.put("nickname",userData.nickname);
        user.put("name",userData.name);
        user.put("age",userData.age);
        user.put("gender",userData.gender);
        user.put("birthyear",userData.birthyear);
        user.put("profileImage",userData.profileImage);
        user.put("appname",userData.appname);
        user.put("title", userData.title);
        return user;
    }

    public static String scanUserData(Activity activity){
        String path=activity.getFilesDir().getAbsolutePath()+ "/userData/";
        File f;
        File[] files;
        String result=null;
        try{
            f=new File(path);

            //디렉토리가 없으면 false반환
            if(!f.isDirectory()){
                return null;
            }
            files = f.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.getName().toLowerCase(Locale.US).endsWith(".txt");
                }
            });
            //파일이 없으면 false반환
            if(files.length == 0){
                return null;
            }
            result=files[0].getName();

        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }


    //유저 정보 로컬에 저장
    //이미지파일 따로, 유저 데이터 따로.
    public static void saveData(UserData userData, Activity activity){
        String datastr=UserData.encode(userData);

        String folder= activity.getFilesDir().getAbsolutePath() + "/userData/";
        String filename=userData.userid+".txt";

        Log.d("save data",filename);

        File file_path;
        try{
            file_path=new File(folder);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
                Log.d("유저 데이터 저장","경로 생성");
            }
            File file=new File(folder+filename);
            FileOutputStream out=new FileOutputStream(folder+filename,false);
            out.write(datastr.getBytes());
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    //이미지 파일, 유저데이터 반환
    public static UserData loadData(Activity activity) {
        UserData resultData = null;


        String folder = activity.getFilesDir().getAbsolutePath() + "/userData/";
        String result = "";

        //파일이 없으면 null반환
        String fileName = scanUserData(activity);
        if (fileName == null) {
            return null;
        }

        File check;
        try {
            check = new File(folder);

            //경로가 없는경우 그냥 null반환하고 끝
            if (!check.isDirectory()) {
                return null;
            }
            String dir = folder + fileName;
            File file = new File(dir);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            result = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultData = UserData.decode(result);

        return resultData;
    }

    public static String encode(UserData userData){
        String result="";
        result+=userData.userid+"_"+userData.profileImage+"_"+
                userData.appname+"_"+userData.nickname+"_"+
                userData.name+"_"+userData.age+"_"+
                userData.gender+"_"+userData.birthyear+"_"+userData.title;
        return result;
    }

    public static UserData decode(String userDataString){
        String[] result=userDataString.split("_");
        return new UserData(result[0],result[1],result[2],result[3],result[4],result[5],result[6],result[7],result[8]);
    }


    //thread생성해서 사용하기
    public static Bitmap GetBitmapfromURL(String urlstr){
        HttpURLConnection connection = null;
        InputStream is = null;
        Bitmap retBitmap = null;
        try {
            URL imgUrl = new URL(urlstr);
            connection = (HttpURLConnection) imgUrl.openConnection();
            connection.setDoInput(true); //url로 input받는 flag 허용
            connection.connect(); //연결
            is = connection.getInputStream(); // get inputstream
            retBitmap = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return retBitmap;
    }

    public static void saveBitmapToJpeg(Bitmap bitmap, Activity activity) {
        if(bitmap==null){
            return;
        }

        String path=activity.getFilesDir().getAbsolutePath()+ "/userData/";
        File storage = new File(path);
        String fileName ="profile.jpg";
        File tempFile = new File(storage, fileName);
        try {

            tempFile.createNewFile();
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
    }

    public static Bitmap loadImageToBitmap(Activity activity){
        String path=activity.getFilesDir().getAbsolutePath()+ "/userData/profile.jpg";
        Bitmap bitmap=BitmapFactory.decodeFile(path);
        return bitmap;
    }
}
