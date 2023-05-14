package com.gorchatov.shazamclone;

import static java.sql.Types.BIGINT;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Music {

    @PrimaryKey()
    int id;

    @NonNull
    private final String name;
    @NonNull
    private final String hash;
    @NonNull
    private final int time;


    public Music(@NonNull  int id, @NonNull String name, @NonNull String hash,@NonNull int time) {
        this.id = id;
        this.name = name;
        this.hash = hash;
        this.time = time;
    }

    public @NonNull String getName(){
        return this.name;
    }

    public @NonNull String getHash(){
        return this.hash;
    }

    public @NonNull int getId(){
        return this.id;
    }
    public @NonNull int getTime(){return this.time;}
}
