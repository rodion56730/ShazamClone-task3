package com.gorchatov.shazamclone.shazamclone;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Music {

    @PrimaryKey(autoGenerate = true)
    long id;

    @NonNull
    private final String name;
    @NonNull
    private final long hash;
    @NonNull
    private final int time;


    public Music( @NonNull String name, @NonNull long hash,@NonNull int time) {
        this.name = name;
        this.hash = hash;
        this.time = time;
    }

    public @NonNull String getName(){
        return this.name;
    }

    public @NonNull long getHash(){
        return this.hash;
    }

    public @NonNull long getId(){
        return this.id;
    }
    public @NonNull int getTime(){return this.time;}
}
