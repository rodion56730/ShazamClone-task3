package com.gorchatov.shazamclone;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MusicDao {
    @Query("SELECT * FROM Music")
    List<Music> getAll();

    @Query("SELECT * FROM Music WHERE id = :id")
    Music getById(long id);

    @Insert
    void insert(Music music);

    @Update
    void update(Music music);

    @Delete
    void delete(Music music);
}
