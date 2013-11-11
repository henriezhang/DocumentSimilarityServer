package com.qq.servers;

import com.google.common.collect.Maps;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-8
 * Time: 上午10:49
 */
public class CosineDistanceMeasureTest extends TestCase
{
    @Test
    public void testSimilarity() throws Exception
    {
        CosineDistanceMeasure measure = new CosineDistanceMeasure();


        Map<String, Double> map1 = Maps.newHashMap();
        map1.put("北京", 12d);
        map1.put("12", 12d);
        map1.put("123", 12d);
        map1.put("sdf", 12d);
        map1.put("rrr", 12d);


        Map<String, Double> map2 = Maps.newHashMap();
        map2.put("北京", 11d);
        map2.put("12", 10d);
        map2.put("1235", 12d);
        map2.put("sdfd", 12d);
        map2.put("rrr", 6d);

        UrlInfo info1 = new UrlInfo("url1", map1);
        UrlInfo info2 = new UrlInfo("url2", map2);

        System.out.println(measure.similarity(info1, info2));

    }
}
