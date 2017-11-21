package com.mark.demo.myocrdemo_mark.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by Mark on 2017/11/21.
 */

public class FileUtils {

    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        return file;
    }
}
