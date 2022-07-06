package com.wustzdy.springboot.flowable.demo.util;

/**
 * EnvKit
 */
public class EnvKit {

    public static boolean isWin() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().startsWith("win");
    }
}
