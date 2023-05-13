package com.gorchatov.shazamclone;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Music.class}, version = 5)
public abstract class MusicDatabase extends RoomDatabase {
    public abstract MusicDao musicDao();
}
