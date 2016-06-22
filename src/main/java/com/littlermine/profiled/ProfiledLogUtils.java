package com.littlermine.profiled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProfiledLogUtils {

    public static void log(String desc, long elapsed) {
        String msg = String.format("elapsed:%s desc:%s", elapsed, desc);
        log.info(msg);
    }

    static String toStr(Object object) {
        if (object == null) {
            return "nil";
        }
        return object.toString();
    }

}
