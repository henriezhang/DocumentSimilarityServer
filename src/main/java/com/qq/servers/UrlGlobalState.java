package com.qq.servers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
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
    private Map<String, UrlInfo> state;
    private CosineDistanceMeasure measure;
    private double similarity;
    private int today;
    private Calendar calendar;


    public UrlGlobalState(double similarity)
    {
        this.similarity = similarity;
        this.state = new ConcurrentHashMap<String, UrlInfo>();
        this.measure = new CosineDistanceMeasure();
        this.calendar = Calendar.getInstance();
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
        return calendar.get(Calendar.DAY_OF_MONTH);
    }


    public boolean isExist(UrlInfo urlInfo)
    {
        return state.get(urlInfo.url) != null;
    }


    final List<UrlInfo> EMPTY_LIST = Lists.newArrayList();


    /**
     * find all similar url, then remove this url from state.
     *
     * @param urlInfo
     * @return
     */
    public synchronized List<UrlInfo> getOldUrlToDelete(UrlInfo urlInfo)
    {
        reinitializeIfNecessary();
        if (isExist(urlInfo))
        {
            UrlInfo old = state.get(urlInfo.url);
            state.remove(old.url);
            List<UrlInfo> result = findSimilarUrl(old);
            state.remove(old.url);
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Old url " + old.toString() + ", new url " + urlInfo.toString());
            }
            return result;
        }
        return EMPTY_LIST;
    }


    public synchronized List<UrlInfo> findSimilarUrl(UrlInfo info)
    {

        reinitializeIfNecessary();

//        if (isExist(info))
//        {
//            //first delete url from related urls' similar url set.
//            //recompute url's
//            return EMPTY_LIST;
//        }

        List<UrlInfo> result = Lists.newArrayList();
        for (Map.Entry<String, UrlInfo> entry : state.entrySet())
        {
            double computeValue = measure.similarity(info, entry.getValue());

            if (LOG.isDebugEnabled())
            {
                LOG.debug("#" + computeValue + "#");
            }

            if (computeValue >= similarity)
            {
                result.add(entry.getValue());
            }
        }

        //keep this new comming url
        state.put(info.url, info);
        return result;
    }

}
