package com.qq.servers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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


    final Set<UrlInfo> EMPTY_LIST = Sets.newHashSet();


    public synchronized Pair<Set<UrlInfo>, Set<UrlInfo>> getUrlToDeleteAndAdd(UrlInfo urlInfo)
    {
        reinitializeIfNecessary();
        Set<UrlInfo> toDelete = getOldUrlToDelete(urlInfo);
        Set<UrlInfo> toAdd = findSimilarUrl(urlInfo, MAX_COUNT);
        state.put(urlInfo.url, urlInfo);
        return new Pair<Set<UrlInfo>, Set<UrlInfo>>(toDelete, toAdd);
    }


    private Set<UrlInfo> getOldUrlToDelete(UrlInfo urlInfo)
    {
        if (isExist(urlInfo))
        {
            UrlInfo old = state.get(urlInfo.url);
            state.remove(old.url);
            Set<UrlInfo> result = findSimilarUrl(old, Integer.MAX_VALUE);
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Old url " + old.toString() + ", new url " + urlInfo.toString());
            }
            return result;
        }
        return EMPTY_LIST;
    }


    private Set<UrlInfo> findSimilarUrl(UrlInfo info, int maxCount)
    {
        TreeMap<Double, UrlInfo> similarUrls = new TreeMap<Double, UrlInfo>();

        Set<UrlInfo> result = Sets.newHashSet();
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
