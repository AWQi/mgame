package com.example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebHistoryItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Timer;

import static android.support.v4.content.ContextCompat.*;

public class DrawView extends View {
    int[][] color = {{184, 216, 174}
            , {114, 126, 241}
            , {140, 232, 217}
            , {243, 232, 90}
            , {252, 227, 217}
            , {81, 179, 196}};
    protected static final String TAG = "DrawView";
    static final int DRAW_CIRCLE = 100;
    static final int DRAW_TRANGLE = 101;
    static final int DRAW_LINE = 102;
    private int view_width;//屏幕的宽度
    private int view_height;//屏幕的高度
    private int cellSize;//  划分画布到单元格 设置单元格大小
    private int x = 0;// 当前行
    private int y = 0;// 当前列
    private int n = 30; // 定义每行 几个图形
    private Path path;//路径
    public Paint paint;//画笔
    public int curShapeX = 0; // 保存当前画笔的  x  坐标
    public int curLineX = 0; // 保存当前画笔的  x  坐标
    Bitmap cacheBitmap = null;//定义一个内存中的图片，该图片将作为缓冲区
    Bitmap bmp = null; // 存放背景图
    Canvas cacheCanvas = null;//定义cacheBitmap上的Canvas对象
    private Handler handler;
    private Context context;
    private int lineHeight = 0;// 平线的高度
    private int lineColor = 0;// 平线的颜色
    private int sPre = 1;// 前一条线的状态  0代表平，1代表不平

    /*
     * 功能：构造方法
     * */
    public DrawView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        view_width = context.getResources().getDisplayMetrics().widthPixels;//获取屏幕宽度
        view_height = context.getResources().getDisplayMetrics().heightPixels;//获取屏幕高度
        Log.d(TAG, "屏幕高: " + view_width);
        Log.d(TAG, "屏幕宽: " + view_height);
        cellSize = view_width / n;// 一行n个图形
        //创建一个与该View相同大小的缓存区
        cacheBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();//创建一个新的画布
        path = new Path();
        cacheCanvas.setBitmap(cacheBitmap);
        paint = new Paint(Paint.DITHER_FLAG);//Paint.DITHER_FLAG防抖动的
        //画背景
        bmp = getBitmap(context, R.drawable.bg);
        cacheCanvas.drawBitmap(bmp, 0, 0, paint);
        cacheCanvas.save();
        invalidate();
        paint.setColor(Color.RED);
        //设置画笔风格
        paint.setStyle(Paint.Style.FILL);//设置填充方式为描边
        paint.setStrokeJoin(Paint.Join.ROUND);//设置笔刷转弯处的连接风格
        paint.setStrokeCap(Paint.Cap.ROUND);//设置笔刷的图形样式(体现在线的端点上)
        paint.setStrokeWidth(20);//设置默认笔触的宽度为1像素
        paint.setAntiAlias(true);//设置抗锯齿效果
        paint.setDither(true);//使用抖动效果
        Log.d(TAG, "构造器: ");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DRAW_CIRCLE:
                        drawCircleByData(cacheCanvas, cellSize, x, y, 20, paint);
                        break;
                    case DRAW_TRANGLE:
                        drawTriangleByData(cacheCanvas, cellSize, x, y, 20, paint);
                        break;
                    case DRAW_LINE:
                        Bundle bundle = msg.getData();
                        float k = bundle.getFloat("k");
                        float time = bundle.getFloat("time");
                        drawLineByData(cacheCanvas, cellSize, x, y, time, k, 20, paint);
                        break;
                    default:
                        break;
                }
                invalidate();//每次画完都要刷新
//                x++;
//                if (x == n) {
//                    y++;
//                    x = 0;
//                }
//                if (y * cellSize > view_height) {
//                    Log.d(TAG, "handleMessage: " + "画布  以满");
//                }
            }

        };

        /**
         *  子线程发消息绘图
         */
        final int[] k = {0,0};// 当前两秒内的周期数   //  上一个两秒内的周期数
        //周期开始时间    //周期结束时间    // 上个周期时间段  //当前周期时间段
        final long[] times = {System.currentTimeMillis(), System.currentTimeMillis(), 0, 0};
        final int [] isRuning = {0};// 是否进入跑步状态 ，2是跑 ，1是蹲或跳 ，0是初始值
        final  boolean[] isClear={false};// 表示一段时间内状态没有跳变 周期数清零
        final float[] a = {0};
        final Thread th = new Thread(new Runnable() {
            @Override
            public void run() {
//                while (true)
                try {
                    Log.d(TAG, "检测: ");
                    Thread.sleep(2000);// 一次数，判断是否开始跑
                    synchronized (k) { //锁住 k
                        synchronized (isRuning) { //锁住 isRunning
//                            Log.d(TAG, "isRunning[0]: "+isRuning[0]);
                            if (k[0] > 2) {// 大于一个周期判断为跑
                                Log.d(TAG, "2s周期数: k[0]"+k[0]);
                                Log.d(TAG, "2s周期数: k[1]"+k[1]);

                                if (k[0]>k[1]){
                                    a[0] =1;} // k 值变大 就加速
                                else if (k[0]<k[1]){
                                    a[0] =-1;} // k值 变小 就  减速
                                else {
                                    a[0] =0;} //  k 值不变  就  不变
                                k[1] = k[0];
                                k[0] = 0;//判断为跑之后周期数清零
                                isRuning[0] = 2; //进入跑步状态
                            } else { // 不在跑步状态，处理蹲或者跳
                                isRuning[0] = 1;
                                k[0] = 0;//判断为跳之后周期数清零
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                int n = 0;// 状态数据累加
                boolean isPre = false; // 上个数据 是否大于  1
                boolean is = false;//   当前数据 是否大于  1
                int h = 0;// 状态改变
               int uod = 0; //  1 跳 2 蹲  置0
                while (true) {
                    try {
                        Thread.sleep(10);// 延时防止取重复数据
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    float X = DataStorage.aX.get(DataStorage.aX.size() - 1);
//                    float Y = DataStorage.aY.get(DataStorage.aY.size() - 1);
//                    float Z = DataStorage.aZ.get(DataStorage.aZ.size() - 1);

                    float X = DataStorage.aX.get(1);
                    float Y = DataStorage.aY.get(1);
                    float Z = DataStorage.aZ.get(1);


                    // 下蹲前的抖动 、起跳前的抖动  0.7~~~1.3
//                    Log.d(TAG, "Y: " + Y);
                    if (uod == 0) {  //  之前还未做出判断就开始判断
                        // 预判断当前周期是起跳或下蹲
                        if ((Y > 1.5 && Y < 2.0 && X < 1 && Z < 1)) {//起跳
                            uod = 1;
                        } else if (Y < 0.3 && Y > 0.0 && X < 0.5 && Z < 0.5) {//  下蹲
                            uod = 2;
                        }
                    }
                    // 判断是否当前数据会导致状态改变
                    if (Y > 1) {
                        is = true;
                    } else {
                        is = false;
                    }
                    if (is == isPre) { // 状态相同
                        n++;
                    } else { // 状态不同
//                        Log.d(TAG, "状态改变: ");
//                        Log.d(TAG, "t.isAlive() "+t.isAlive());
                        if (!th.isAlive()) {// 当状态改变时开始检测是否在跑
                            th.start();
                        }
                        if (n > 5) { //周期状态改变成功，不是抖动
                            isPre = is;
                            h++; // 正常的状态变化
                        } else { //不是正常周期变化
                            h = 0;//
                        }
                        n = 0; //  完成一次变化  n  归零
                    }
                    if (h == 2) {//h等于2 完成一个周期
                        h = 0; //完成一个周期 h 归零
                        synchronized (k) {
                            k[0]++;
                        }// 周期数加一
                        // 周期完成
//                        Log.d(TAG, "周期完成: ");

                        times[0] = times[1];// 当前周期开始时间（上个周期结束时间）
                        times[1] = System.currentTimeMillis();// 取当前周期结束时间
                        times[2] = times[3]; // 新周期完成，当前周期 时间段变为上个周期
                        times[3] = times[1] - times[0];//取当前周期时间段
//                        Log.d(TAG, "当前周期 时间"+times[3]);
//                        Log.d(TAG, "上个周期 时间"+times[2]);

                        synchronized (isRuning) {
                            if (isRuning[0]==2) {// 在跑步状态时，进行比较
                                Log.d(TAG, "跑: ");

                                Message message = new Message();
                                message.what = DRAW_LINE;
                                Bundle bundle = new Bundle();
                                bundle.putFloat("k", a[0]);
                                bundle.putFloat("time",2*cellSize);
                                message.setData(bundle);
                                handler.sendMessage(message);
//                                    Log.d(TAG, "时间: " + k);
//                                    k[1] = k[0];// 当前周期数变成上个周期
//                                    k[0] = 0;// 当前周期数清零
                                float t = times[3]-times[2];

                                isRuning[0]=0;
                                h = 0;
                                n=0;
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else if (isRuning[0]==1){// 不在跑步状态，判断是蹲或者跳

                                if (uod == 1) { //跳
                                    Log.d(TAG, "跳");

                                    Message message = new Message();
                                    message.what = DRAW_CIRCLE;
                                    handler.sendMessage(message);
//                                    k[1] = k[0];// 当前周期数变成上个周期
//                                    k[0] = 0;// 周期数

                                    isRuning[0]=0;

                                    h = 0;
                                    n=0;
//                                    try {
//                                       Thread.sleep(2000);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
                                } else if (uod == 2) {//蹲
                                    Log.d(TAG, "蹲: ");
                                    Message message = new Message();
                                    message.what = DRAW_TRANGLE;
                                    handler.sendMessage(message);

                                    isRuning[0]=0;

//                                        k[1] = k[0];// 当前周期数变成上个周期
//                                        k[0] = 0;// 周期数
                                    h = 0;
                                    n=0;
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        }
                        uod =0;
                        // 打开新周期
                        //每个新周期 跳或蹲的状态 清零
                    }

                }
            }
        }).start();


//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
////                try {
////                    Thread.sleep(3000);
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
//                while (true) {
//                    try {
//                        Thread.sleep(10);
//                        Message msg = new Message();
//                        float sumX = 0;
//                        float sumY = 0;
//                        float sumZ = 0;
//                        int maxXNum = 0;// 全速跑// 300个数据中判定加速度达标的个数
//                        int midXNum = 0;// 均速跑
//                        for (int i = 1; i < DataStorage.aX.size(); i++) {
//                            if (DataStorage.aX.get(i) > 0.7) {
//                                maxXNum++;
//                            } else if (DataStorage.aX.get(i) > 0.3) {
//                                midXNum++;
//                            }
//                            sumX += DataStorage.aX.get(i);
//                            sumY += DataStorage.aY.get(i);
//                            sumZ += DataStorage.aZ.get(i);
//                        }
//                        float avgX = sumX / DataStorage.size;
//                        float avgY = sumY / DataStorage.size;
//                        float avgZ = sumZ / DataStorage.size;
//
//                        float X = DataStorage.aX.get(DataStorage.aX.size()-1);
//                        float Y = DataStorage.aY.get(DataStorage.aX.size()-1);
//                        float Z = DataStorage.aZ.get(DataStorage.aX.size()-1);
//                        if (maxXNum > 200 || midXNum > 200) {
//                            Log.d(TAG, "判定为  ： 跑: ");
//                            final int[] time = {0};
//
//                            boolean flag = true;
//                            float f = 0;// 判断跑的过程中X的加速度
//                            while (flag) {
//                                int minXNum = 0;
////                                                    try {
////                                                        Thread.sleep(10);
////                                                    } catch (InterruptedException e) {
////                                                        e.printStackTrace();
////                                                    }
//                                for (int i = 1; i <DataStorage.aX.size(); i++) {// 统计减速
//                                    if ((f = DataStorage.aX.get(i)) < 0) {
//                                        minXNum++;
//                                    }
//                                }
//                                if (minXNum > 70) {
//                                    flag = false;
//                                }
//                                if (time[0] > 500) {// 限定加速跑最大时间
//                                    flag = false;
//                                }
//                                time[0]++;
//                            }
//                            float s = 0; //选用的速度
//                            if (maxXNum > midXNum) {
//                                s = -0.3f;
//                            } else {
//                                s = -0.9f;
//                            }
//                            msg.what = DRAW_LINE;
//                            Bundle bundle = new Bundle();
//                            bundle.putDouble("k", s);
//                            bundle.putFloat("time", time[0]);
//                            msg.setData(bundle);
//                            handler.sendMessage(msg);
//
//                            Thread.sleep(2000);
//                        } else if ((Y > -0.3 && Y < 0.3
//                                && X < 1
//                                && Z < 1)) {//起跳
//                            Log.d(TAG, "判定为 ： 跳");
//                            msg.what = DRAW_CIRCLE;
//                            handler.sendMessage(msg);
//                            Thread.sleep(2000);
//                        } else if (Y < 0.6 && Y > 0.3
//                                && X < 0.5
//                                && Z < 0.5) {//  下蹲
//                            Log.d(TAG, "判定为 ：下蹲: ");
//                            msg.what = DRAW_TRANGLE;
//                            handler.sendMessage(msg);
//                            Thread.sleep(2000);
//
//                        } else {
//                            Log.d(TAG, "其他: ");
//                        }
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        t.start();


    }

    /*
     * 功能：重写onDraw方法
     * */
    @Override
    protected void onDraw(final Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);
        bmp = getBitmap(context, R.drawable.bg);
        canvas.drawBitmap(cacheBitmap, 0, 0, paint);//绘制cacheBitmap// 用来临时保存绘制的图形
        canvas.drawPath(path, paint);//绘制路径
    }

    /***
     *
     *  触摸事件
     *
     */
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //获取触摸事件发生的位置
//        float x=event.getX();
//        float y=event.getY();
//        switch(event.getAction()){
//            case MotionEvent.ACTION_DOWN:
//                //将绘图的起始点移到(x,y)坐标点的位置
//                path.moveTo(x, y);
//                preX=x;
//                preY=y;
//                break;
//            case MotionEvent.ACTION_MOVE:
//                //保证横竖绘制距离不能超过625
//                float dx= Math.abs(x-preX);
//                float dy= Math.abs(y-preY);
//                if(dx>5||dy>5){
//                    //.quadTo贝塞尔曲线，实现平滑曲线(对比lineTo)
//                    //x1，y1为控制点的坐标值，x2，y2为终点的坐标值
//                    path.quadTo(preX, preY, (x+preX)/2, (y+preY)/2);
//                    preX=x;
//                    preY=y;
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                cacheCanvas.drawPath(path, paint);//绘制路径
//                path.reset();
//                break;
//        }
//        invalidate();
//        return true;//返回true,表明处理方法已经处理该事件
//    }
    public void clear() {
        //设置图形重叠时的处理方式
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        //设置笔触的宽度
        paint.setStrokeWidth(50);
    }

    public void save() {
        try {
            saveBitmap(getCurrentTime() + "-myPitcture");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前时间  来构建  文件名
     *
     * @return
     */
    public String getCurrentTime() {
        Date date = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd:hh:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm-dd:hh:mm:ss");
        String str = dateFormat.format(date);
        return str;
    }

    /**
     * 保存画布
     *
     * @param fileName
     * @throws IOException
     */
    private void saveBitmap(String fileName) throws IOException {
        Log.d(TAG, "saveBitmap: " + PathUtil.getImagePath() + fileName + ".png");
        File file = new File(PathUtil.getImagePath() + fileName + ".png");
//        File file=new File(getPath()+fileName+".png");
        file.createNewFile();
        FileOutputStream fileOS = new FileOutputStream(file);
        //将绘图内容压缩为PNG格式输出到输出流对象中
        cacheBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOS);
        fileOS.flush();//将缓冲区中的数据全部写出到输出流中
        fileOS.close();//关闭文件输出流对象
        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 根据数据画图形
     *
     * @param canvas
     * @param cellSize
     * @param x
     * @param y
     * @param margin
     * @param paint
     */
    public void drawCircleByData(Canvas canvas, int cellSize, int x, int y, int margin, Paint paint) {

        x = curShapeX + margin + cellSize / 2;
        Random rdm = new Random();
        y = rdm.nextInt(view_height * 1 / 2-cellSize)+2*cellSize;//随机高度
//        Log.d(TAG, "width: " + view_width);
//        Log.d(TAG, "heigh: " + view_height);
//        Log.d(TAG, "y: " + y);
//        Log.d(TAG, "x: " + x);
        paint.setColor(Color.RED);
        drawCirCle(cacheCanvas, x, y, cellSize / 2 , paint);
        postInvalidate();
        curShapeX = curShapeX + 3*cellSize +3*margin;
    }

    public void drawTriangleByData(Canvas canvas, int cellSize, int x, int y, int margin, Paint paint) {
        x = curShapeX + margin + cellSize / 2;
        Random rdm = new Random();
        y = rdm.nextInt(view_height * 1 / 2 - cellSize) + 2*cellSize;//随机高度
        int c = rdm.nextInt(5);
        paint.setColor(Color.rgb(color[c][0], color[c][1], color[c][2]));
        drawTriangle(cacheCanvas, x, y
                , x + cellSize, y
                , x + cellSize / 2, y - cellSize, paint);
        postInvalidate();
        curShapeX = curShapeX + 3*cellSize+3*margin ;
    }

    public void drawLineByData(Canvas canvas, int cellSize, int x, int y, float time, float k, int margin, Paint paint) {
        // 前一条线的状态  0代表平，1代表不平
        Random rdm = new Random();
        int c;


        if (k==0){
            y = lineHeight;

            if (sPre==1){ curLineX = curLineX +5*margin;c = rdm.nextInt(5);
            }else { c = lineColor;}

            lineHeight=y;
            lineColor=c;
            sPre = 0;
        }else {
            y = rdm.nextInt(view_height * 1 / 3 - cellSize) + 3*cellSize+view_height * 1 / 3;//随机高度
            c = rdm.nextInt(5);
            curLineX = curLineX +5*margin;

            lineHeight=y;
            lineColor=c;
            sPre=1;
        }
        x = curLineX ;
        paint.setColor(Color.rgb(color[c][0], color[c][1], color[c][2]));// 随机颜色
        if (k==1){k = rdm.nextFloat();}
        if (k==-1){k = rdm.nextFloat()*(-1);}
        if (k==0){k=0;}
        canvas.drawLine(x, y
                , x + time, (float) (y - time * k)
                , paint);// 画线  从左下角
        postInvalidate();
        curLineX +=time;
    }

    /**
     * 画文本
     */
    public void drawText(Canvas canvas, String text, int x, int y, float textSize, Paint bmpPaint) {
        bmpPaint.setTextSize(textSize);//  size  以xp为单位
        canvas.drawText("画图：", x, y, bmpPaint); //  x y  为基线的坐标
    }

    /**
     * 画圆
     */
    public void drawCirCle(Canvas canvas, int x, int y, int rad, final Paint bmpPaint) {
//        bmpPaint.setStyle(Paint.Style.STROKE);// 设置空心
//        bmpPaint.setStyle(Paint.Style.FILL);
        bmpPaint.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
        canvas.drawCircle(x, y, rad, bmpPaint);// x y 是坐标  rad 是半径
    }

    /**
     * 画三角
     */
    public void drawTriangle(Canvas canvas, int x1, int y1, int x2, int y2, int x3, int y3, final Paint bmpPaint) {
        Path path = new Path();
        path.moveTo(x1, y1);// 此点为多边形的起点
        path.lineTo(x2, y2);
        path.lineTo(x3, y3);
        path.close(); // 使这些点构成封闭的多边形
        bmpPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, bmpPaint);
    }

    /**
     * 画线
     */
    public void drawLine(Canvas canvas, int startX, int startY, int endX, int endY, Paint bmpPaint) {
        canvas.drawLine(startX, startY, endX, endY, bmpPaint);// 画线
    }
//    /**
//     * 画上升直线
//     */
//    public  void drawAscendingLine(){
//
//    }
//
//    /**
//     *    画下降直线
//     */
//    public  void drawDescendingLine(){
//
//    }
//    /**
//     *  画水平直线
//     */
//    public  void drawHorizontalLine(){
//
//    }


    private static Bitmap getBitmap(Context context, int vectorDrawableId) {
        Bitmap bitmap = null;
        Resources r = context.getResources();
        @SuppressLint("ResourceType") InputStream is = r.openRawResource(R.drawable.bg);
//    Log.d(TAG, "getBitmap: is"+is);
        BitmapDrawable bmpDraw = new BitmapDrawable(is);
//    Log.d(TAG, "getBitmap: draw"+bmpDraw);
        Bitmap bmp = bmpDraw.getBitmap();
//    Log.d(TAG, "getBitmap: bimap"+bmp);
        return bmp;
    }
}