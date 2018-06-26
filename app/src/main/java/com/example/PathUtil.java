package com.example;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public   class PathUtil {
    private static final String TAG = "PathUtil";
    //  获取内部目录
    public static String getPath(Context context){
        File file = context.getFilesDir();
        return file.getPath();
    }

    //获得  mGame  目录
    private static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在

        if (sdCardExist)  //如果SD卡存在，则获取跟目录
        {
            sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);//获取数据目录
//            sdDir = Environment.getDataDirectory();
        }

        String mGamePath = sdDir.toString()+"/mGame/";
        File mGameFile = new File(mGamePath);
        if (!mGameFile.exists()){
            mGameFile.mkdir();
        }
        Log.d(TAG, "getSDPath: "+mGamePath);
        return mGamePath;
    }
    public  static String getImagePath(){
        String imagePath =   getSDPath()+"images/";
        File imageDir = new File(imagePath);
        if (!imageDir.exists()){
            imageDir.mkdirs();
        }

        return imagePath;
    }
    public  static String getRecordPath(){
        String recordPath = getSDPath()+"records/";
        File recordDir = new File(recordPath);
        if (!recordDir.exists()){
            recordDir.mkdirs();
        }
        return recordPath;
    }
}
