package com.gorchatov.shazamclone;

import android.app.Application;

import androidx.room.Room;

public class App extends Application {
    private static App instance;

    private MusicDatabase musicDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        musicDatabase = Room.databaseBuilder(this, MusicDatabase.class, "Music")
                .allowMainThreadQueries()
                .createFromAsset("database/myfin.db")
                .build();//сюда надо путь к готовой БД
    }

    public static App getInstance() {
        return instance;

    }

    public MusicDatabase getDatabase() {
        return musicDatabase;
    }
}
