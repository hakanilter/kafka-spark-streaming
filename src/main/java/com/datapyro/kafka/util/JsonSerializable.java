package com.datapyro.kafka.util;

import com.google.gson.Gson;

import java.io.Serializable;

public abstract class JsonSerializable implements Serializable {

    private static final Gson GSON = new Gson();
    
    public String toJson() {
        return GSON.toJson(this);
    }

}
