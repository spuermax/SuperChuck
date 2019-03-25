package com.developers.super_chuck.internal.helper;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.bind.DateTypeAdapter;

import java.util.Date;

/**
 * @Author yinzh
 * @Date 2019/3/25 15:04
 * @Description
 */
public class GsonConverter {
    private static Gson gson = null;

    private GsonConverter() {
    }

    public static Gson getInstance() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .registerTypeAdapter(Date.class, new DateTypeAdapter())
                    .create();
        }
        return gson;
    }
}
