package com.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
//import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;


public class GameActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_STORAGE_GROUP_CODE = 1;
    private DrawView dv = null;
    private Button saveBtn = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 去标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_game);
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        DataStorage.Analysis();
//        requestPermission();
        dv=(DrawView)findViewById(R.id.drawView1);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dv.save();//保存绘画
            }
        });

    }

    /**
     *
     *     android 6  一下
      * @param
     * @return
     */

//    // 下载获取权限
//    public  void requestPermission(){
//        if (ContextCompat.checkSelfPermission(GameActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED
//                ||ContextCompat.checkSelfPermission(GameActivity.this, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "没有权限: ");
//            ActivityCompat.requestPermissions(this, new String[]{
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.MOUNT_FORMAT_FILESYSTEMS,
//                    Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS}, REQUEST_STORAGE_GROUP_CODE);
//            return;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        switch (requestCode){
//            case  REQUEST_STORAGE_GROUP_CODE :
//                if (grantResults.length>0&&
//                        grantResults[0]==PackageManager.PERMISSION_GRANTED
//                        &&grantResults[1]==PackageManager.PERMISSION_GRANTED
//                        &&grantResults[2]==PackageManager.PERMISSION_GRANTED
//                        &&grantResults[3]==PackageManager.PERMISSION_GRANTED){
//                    Toast.makeText(GameActivity.this,"权限获取成功",Toast.LENGTH_SHORT).show();
//                }
//                break;
//        }
//    }

    /*
     * 创建选项菜单
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflator=new MenuInflater(this);
        inflator.inflate(R.menu.toolsmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /*
     * 当菜单项被选择时，做出相应的处理
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //获取自定义的绘图视图
        dv.paint.setXfermode(null);//取消擦除效果
        dv.paint.setStrokeWidth(1);//初始化画笔的宽度
        switch(item.getItemId()){
            case R.id.red:
                dv.paint.setColor(Color.RED);//设置笔的颜色为红色
                item.setChecked(true);
                break;
            case R.id.green:
                dv.paint.setColor(Color.GREEN);//设置笔的颜色为绿色
                item.setChecked(true);
                break;
            case R.id.blue:
                dv.paint.setColor(Color.BLUE);//设置笔的颜色为蓝色
                item.setChecked(true);
                break;
            case R.id.width_1:
                dv.paint.setStrokeWidth(5);//设置笔触的宽度为1像素
                break;
            case R.id.width_2:
                dv.paint.setStrokeWidth(10);//设置笔触的宽度为5像素
                break;
            case R.id.width_3:
                dv.paint.setStrokeWidth(15);//设置笔触的宽度为10像素
                break;
            case R.id.clear:
                dv.clear();//擦除绘画
                break;
            case R.id.save:
                dv.save();//保存绘画
                break;
        }
        return true;
    }
}

