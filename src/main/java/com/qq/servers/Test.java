package com.qq.servers;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-12-31
 * Time: 上午10:06
 */
public class Test
{
    public static void main(String[] args)
    {
        Map<Float, String> mySortedMap = new TreeMap<Float, String>();
        // Put some values in it
        mySortedMap.put(-1.0f, "One");
        mySortedMap.put(-0.0f, "Zero");
        mySortedMap.put(-3.0f, "Three");

        // Iterate through it and it'll be in order!
        for (Map.Entry<Float, String> entry : mySortedMap.entrySet())
        {
            System.out.println(entry.getValue());
        } // outputs Zero One Three
    }
}
