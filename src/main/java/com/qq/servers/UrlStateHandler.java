package com.qq.servers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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

    public UrlStateHandler(UrlGlobalState state, RedisClient client)
    {
        this.state = state;
        this.redisClient = client;
    }

    public void process(UrlInfo info)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Receiving URL " + info.toString());
        }

        UrlGlobalState.Pair<Set<UrlInfo>, Set<UrlInfo>> urls = state.getUrlToDeleteAndAdd(info);

        if (urls.getLeft().size() != 0)
        {
            redisClient.deleteFromRelatedUrls(info, urls.getLeft());
        }

        Set<UrlInfo> similarUrls = urls.getRight();

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
