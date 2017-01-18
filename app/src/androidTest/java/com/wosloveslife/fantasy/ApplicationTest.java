package com.wosloveslife.fantasy;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.wosloveslife.fantasy.utils.FormatUtils;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void test() {
        long l = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            FormatUtils.stringForTime(i * 1000L);
        }

        long m = System.currentTimeMillis();
        System.out.println("1 耗时 = " + (m - l));

        for (int i = 0; i < 10000; i++) {
            FormatUtils.stringForTime2(i * 1000L);
        }

        System.out.println("2 耗时 = " + (System.currentTimeMillis() - m));
    }
}