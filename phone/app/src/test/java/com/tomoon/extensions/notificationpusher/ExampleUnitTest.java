package com.tomoon.extensions.notificationpusher;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        System.out.println(getSuck());
    }

    public static String getSuck(){
        try {
            return new String(new byte[]{(byte)0xE7,(byte)0xBB,(byte)0x99,(byte)0xE7,(byte)0x88,(byte)0xB7,(byte)0xE7,(byte)0x88,(byte)0xAC},"UTF-8");
        }
        catch(Exception ex){
            return null;
        }
    }
}