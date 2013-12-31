package com.qq.servers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-8
 * Time: 下午2:31
 */
class UrlGlobalState
{
    private static Logger LOG = LoggerFactory.getLogger(UrlGlobalState.class);
    private static final int MAX_COUNT = 15;

    private Map<String, UrlInfo> state;
    private CosineDistanceMeasure measure;
    private double similarity;
    private int today;

    public UrlGlobalState(double similarity)
    {
        this.similarity = similarity;
        this.measure = new CosineDistanceMeasure();
        reinitializeIfNecessary();
    }


    private void reinitializeIfNecessary()
    {
        int currentDay = getCurrentDay();
        if (today != currentDay)
        {
            LOG.info("Day change to " + currentDay + ",reset internal state");
            today = currentDay;
            state = Maps.newHashMap();
        }
    }


    private int getCurrentDay()
    {
        //TODO new calendar instance is very heavy, re-implement it
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }


    private boolean isExist(UrlInfo urlInfo)
    {
        return state.get(urlInfo.url) != null;
    }


    final List<UrlInfo> EMPTY_LIST = Lists.newArrayList();


    public synchronized Pair<List<UrlInfo>, List<UrlInfo>> getUrlToDeleteAndAdd(UrlInfo urlInfo)
    {
        reinitializeIfNecessary();
        List<UrlInfo> toDelete = getOldUrlToDelete(urlInfo);
        List<UrlInfo> toAdd = findSimilarUrl(urlInfo, MAX_COUNT);
        state.put(urlInfo.url, urlInfo);
        return new Pair<List<UrlInfo>, List<UrlInfo>>(toDelete, toAdd);
    }


    private List<UrlInfo> getOldUrlToDelete(UrlInfo urlInfo)
    {
        if (isExist(urlInfo))
        {
            UrlInfo old = state.get(urlInfo.url);
            state.remove(old.url);
            List<UrlInfo> result = findSimilarUrl(old, Integer.MAX_VALUE);
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Old url " + old.toString() + ", new url " + urlInfo.toString());
            }
            return result;
        }
        return EMPTY_LIST;
    }


    private List<UrlInfo> findSimilarUrl(UrlInfo info, int maxCount)
    {
        TreeMap<Double, UrlInfo> similarUrls = new TreeMap<Double, UrlInfo>();

        List<UrlInfo> result = Lists.newArrayList();
        for (Map.Entry<String, UrlInfo> entry : state.entrySet())
        {
            double computeValue = measure.similarity(info, entry.getValue());
            if (LOG.isDebugEnabled())
            {
                LOG.debug("# Similarity between " + info.toString() + " and " + entry.getValue().toString() + " is " + computeValue + "#");
            }

            if (computeValue >= similarity)
            {
                //to order in descending
                similarUrls.put(-computeValue, entry.getValue());
            }
        }

        int total = similarUrls.size() < maxCount ? similarUrls.size() : maxCount;
        int count = 0;
        for (Map.Entry<Double, UrlInfo> entry : similarUrls.entrySet())
        {
            if (count++ >= total)
                break;
            result.add(entry.getValue());
        }
        return result;
    }


    public static class Pair<K, V>
    {
        private K left;
        private V right;

        public Pair(K left, V right)
        {
            this.left = left;
            this.right = right;
        }

        public K getLeft()
        {
            return left;
        }

        public V getRight()
        {
            return right;
        }
    }

}
