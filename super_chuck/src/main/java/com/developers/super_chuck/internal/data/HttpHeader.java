package com.developers.super_chuck.internal.data;

/**
 * @Author yinzh
 * @Date 2019/3/25 14:57
 * @Description
 */
public class HttpHeader {
    private final String name;
    private final String value;

    HttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}

