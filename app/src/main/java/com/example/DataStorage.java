package com.example;

import java.util.LinkedList;
import java.util.List;

public   class DataStorage {
    static  int DRAW = 100;
    static final  public  int DRAW_CIRCLE = 100;
    static final  public  int DRAW_TRANGLE = 101;
    static final  public  int DRAW_LINE = 102;

    /* 加速度*/
    static  public  List<Float> aX = new LinkedList<Float>();
    static  public  List<Float> aY = new LinkedList<Float>();
    static  public  List<Float> aZ = new LinkedList<Float>();
    static {
        aX.add(0f);aX.add(0f);
        aY.add(1f);aY.add(1f);
        aZ.add(0f);aZ.add(0f);
    }
    static  public  int size = 3;
    /*  高度*/
    static public  List<Float> height = new LinkedList<Float>();
//    static  public  List<Float> t = new LinkedList<Float>();
//    static {
//        t.add(1f);t.add(2f);t.add(3f);t.add(4f);t.add(5f);
//        t.add(6f);t.add(7f);t.add(8f);t.add(9f);t.add(10f);
//    }
    static public int  Analysis(){
       return 0;
    }
}
