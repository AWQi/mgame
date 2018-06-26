package com.example;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GameImageShowActivity extends FragmentActivity {
    private MyAdapter myAdapter = null;
    private ListView imageShowLV = null;
    private File[] files;
    private List<ImageFile> imageList = new ArrayList<ImageFile>();
    boolean op = true;//  使用时为true 就是图片放大效果 false就是 图片缩小效果
    ImageView showBigImage = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_show);
        getDate();
        showBigImage = (ImageView) findViewById(R.id.showBigImage);
        imageShowLV = (ListView) findViewById(R.id.imageShowLV);
        myAdapter = new MyAdapter(GameImageShowActivity.this,R.layout.game_image_item,imageList);
        imageShowLV.setAdapter(myAdapter);
        imageShowLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                float view_width=GameImageShowActivity.this.getResources().getDisplayMetrics().widthPixels;//获取屏幕宽度
                float view_height=GameImageShowActivity.this.getResources().getDisplayMetrics().heightPixels;//获取屏幕高度
                if(op){
                    showBigImage.setImageBitmap(imageList.get(i).imageBitmp);
                    showBigImage.setVisibility(View.VISIBLE);
                    Animation  animation = new ScaleAnimation(0,view_width,0,view_height,0,0);
                    animation.setDuration(500);
                    showBigImage.startAnimation(animation);
                    op = false;
                }else {
                    Animation  animation = new ScaleAnimation(view_width,0,view_height,0,0,0);
                    animation.setDuration(500);
                    showBigImage.startAnimation(animation);
                    showBigImage.setVisibility(View.GONE);
                    op = true;
                }
            }
        });
    }

    public void getDate() {
        String imagePath = PathUtil.getImagePath();
        File imageDir = new File(imagePath);
        files = imageDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().matches(".*\\.png");
            }
        });
        try {
            for (int i = 0; i < files.length; i++) {
                FileInputStream fis = new FileInputStream(files[i].getAbsolutePath());
                Bitmap bitmap= BitmapFactory.decodeStream(fis);
                imageList.add(new ImageFile(bitmap,files[i].getName()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }
class  MyAdapter extends ArrayAdapter<ImageFile>{
private  int resouceId;
    public MyAdapter(Context context, int resource, List<ImageFile> objects) {
        super(context, resource, objects);
        resouceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageFile imageFile = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resouceId,parent,false);
        ImageView itemImage = (ImageView) view.findViewById(R.id.gameImageItemIV);
        TextView itemText = (TextView) view.findViewById(R.id.gameImageItemTV);
        /* 填充数据*/
        itemImage.setImageBitmap(imageFile.imageBitmp);
        itemText.setText(imageFile.name);
        return view;
    }
}
    class  ImageFile{
        public Bitmap imageBitmp ;
        public String name ;

        public ImageFile(Bitmap imageBitmp, String name) {
            this.imageBitmp = imageBitmp;
            this.name = name;
        }

        public ImageFile() {
        }
    }
}
