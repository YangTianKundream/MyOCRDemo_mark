package com.mark.demo.myocrdemo_mark.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类，解决多个Toast时长问题
 * Created by Mark on 2017/11/21.
 */

public class ToastUtils {
    private static Toast toast;

    public synchronized static void showToast(Context context,String text){
        if (toast == null){
            toast = Toast.makeText(context.getApplicationContext(),text,Toast.LENGTH_SHORT);
        }else {
            toast.setText(text);
        }
        toast.show();
    }
}
