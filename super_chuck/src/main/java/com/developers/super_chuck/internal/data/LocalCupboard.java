package com.developers.super_chuck.internal.data;

import nl.qbusict.cupboard.Cupboard;
import nl.qbusict.cupboard.CupboardBuilder;

/**
 * @Author yinzh
 * @Date 2019/3/25 15:19
 * @Description
 */
public class LocalCupboard {
    private static Cupboard cupboard;

    static {
        getInstance().register(HttpTransaction.class);
    }

    public static Cupboard getInstance() {
        if (cupboard == null) {
            cupboard = new CupboardBuilder().build();
        }
        return cupboard;
    }

    public static Cupboard getAnnotatedInstance() {
        return new CupboardBuilder(getInstance())
                .useAnnotations()
                .build();
    }

    private LocalCupboard() {
    }
}
