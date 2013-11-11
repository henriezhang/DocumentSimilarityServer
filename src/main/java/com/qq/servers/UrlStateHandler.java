package com.qq.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-7
 * Time: 下午2:14
 */
public class UrlStateHandler
{

    private static final Logger LOG = LoggerFactory.getLogger(UrlStateHandler.class);

    private RedisClient redisClient;
    private UrlGlobalState state;

    public UrlStateHandler(UrlGlobalState state)
    {
        this.state = state;
        this.redisClient = new RedisClient();
    }

    public void process(UrlInfo info)
    {

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Receiving URL " + info.toString());
        }

        List<UrlInfo> similarUrls = state.findSimilarUrl(info);
        //passed info has no similarity with other urls
        if (similarUrls.size() == 0)
        {
            return;
        }
        if (LOG.isDebugEnabled())
        {
            LOG.debug("find document " + info.toString() + " with same similarity with " +
                    Arrays.toString(similarUrls.toArray(new UrlInfo[similarUrls.size()])));
        }
        redisClient.updateRedis(info, similarUrls);

    }

}
