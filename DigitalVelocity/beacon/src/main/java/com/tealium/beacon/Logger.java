package com.tealium.beacon;


import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Logger {

    private final ExecutorService service;
    private final File file;

    public Logger(Context context) {
        this.service = Executors.newSingleThreadExecutor();
        this.file = new File(Environment.getExternalStorageDirectory(), "estimote_manager.log");
    }

    public void log(final String message) {
        this.service.submit(new Runnable() {
            @Override
            public void run() {

                final String state = Environment.getExternalStorageState();

                if (!Environment.MEDIA_MOUNTED.equals(state)) {
                    Log.e(Constant.TAG, "Error writing " + file.getAbsolutePath() + '(' + state + ')');
                    return;
                }

                try {
                    FileWriter fileWritter = new FileWriter(file, file.exists());
                    BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                    bufferWritter.write(message);
                    bufferWritter.close();
                } catch (IOException e) {
                    Log.e(Constant.TAG, "Error writing " + file.getAbsolutePath(), e);
                }
            }
        });
    }
}
