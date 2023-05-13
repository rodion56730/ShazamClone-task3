package com.gorchatov.shazamclone.shazamclone;

import android.app.Application;
import android.os.Environment;

import androidx.room.Room;

import java.io.File;

public class App extends Application {
    private static App instance;

    private MusicDatabase musicDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        musicDatabase = Room.databaseBuilder(this, MusicDatabase.class, "Musics.db")
                .createFromFile(new File(Environment.getExternalStorageDirectory() + "/Download/myfin.db"))
                .build();//сюда надо путь к готовой БД
    }

    public static App getInstance() {
        return instance;

    }

    public MusicDatabase getDatabase() {
        return musicDatabase;
    }
}
