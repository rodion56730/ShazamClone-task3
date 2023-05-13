package com.gorchatov.shazamclone.shazamclone;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Music.class}, version = 5)
public abstract class MusicDatabase extends RoomDatabase {
    public abstract MusicDao musicDao();
}
