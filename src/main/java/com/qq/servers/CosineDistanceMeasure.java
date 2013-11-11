package com.qq.servers;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-7
 * Time: 下午12:52
 */
public class CosineDistanceMeasure
{

    public double similarity(UrlInfo p1, UrlInfo p2)
    {
        Set<String> words = Sets.newHashSet();
        words.addAll(p1.getKeyWord().keySet());
        words.addAll(p2.getKeyWord().keySet());

        double[] vector1 = new double[words.size()];
        double[] vector2 = new double[words.size()];

        int i = 0;
        for (String word : words)
        {
            Double weight1 = p1.getKeyWord().get(word);
            Double weight2 = p2.getKeyWord().get(word);

            vector1[i] = weight1 == null ? 0 : weight1;
            vector2[i] = weight2 == null ? 0 : weight2;
            i++;
        }
        return 1 - distance(vector1, vector2);
    }


    public static double distance(double[] p1, double[] p2)
    {
        double dotProduct = 0.0;
        double lengthSquaredp1 = 0.0;
        double lengthSquaredp2 = 0.0;
        for (int i = 0; i < p1.length; i++)
        {
            lengthSquaredp1 += p1[i] * p1[i];
            lengthSquaredp2 += p2[i] * p2[i];
            dotProduct += p1[i] * p2[i];
        }
        double denominator = Math.sqrt(lengthSquaredp1) * Math.sqrt(lengthSquaredp2);

        // correct for floating-point rounding errors
        if (denominator < dotProduct)
        {
            denominator = dotProduct;
        }

        // correct for zero-vector corner case
        if (denominator == 0 && dotProduct == 0)
        {
            return 0;
        }

        return 1.0 - dotProduct / denominator;
    }

}
