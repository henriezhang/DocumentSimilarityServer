package com.qq.servers;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.util.List;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-8
 * Time: 上午9:38
 */
public class RedisClient
{

    private static Logger LOG = LoggerFactory.getLogger(RedisClient.class);


    private static String URL_PREFIX = "url_";
    private static String host;
    private static int port;

    static
    {
        Properties props = new Properties();
        try
        {
            props.load(RedisClient.class.getClassLoader().getResourceAsStream("redis-host"));
        }
        catch (Exception e)
        {
            throw new RuntimeException("can't find redis locations", e);
        }

        String strValue = (String) props.get("host");
        if (Strings.isNullOrEmpty(strValue))
        {
            throw new RuntimeException("properties host can't find in redis-host configuration file");
        }

        String[] values = strValue.split(":");
        host = values[0];
        port = Integer.parseInt(values[1]);
        LOG.info("Find redis host " + host + ":" + port);
    }

    public static Jedis createRedisInstance()
    {
        return new Jedis(host, port);
    }


    private static final String REDIS_FIELD_NAME = "lk";

    private Jedis client;
    private Pipeline redisPipeline;

    public RedisClient()
    {
        init();
    }

    private void init()
    {
        client = createRedisInstance();
        this.redisPipeline = client.pipelined();
    }

    public void updateRedis(UrlInfo urlInfo, List<UrlInfo> similarUrls)
    {
        //1. update redis for passed in urlInfo
        String value = composeNewValue(similarUrls);
        client.hset(appendPrefix(urlInfo.url), REDIS_FIELD_NAME, value);

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Write URl info to redis : url = " + urlInfo.toString() + "; value = " + value);
        }

        //2. update related urlInfos.
        boolean usePipeLine = similarUrls.size() > 5;
        for (UrlInfo info : similarUrls)
        {
            String oldValue = client.hget(appendPrefix(info.url), REDIS_FIELD_NAME);
            String updatedValue;
            if (Strings.isNullOrEmpty(oldValue))
            {
                updatedValue = urlInfo.url;
            }
            else
            {
                updatedValue = Joiner.on(",").join(oldValue, urlInfo.url);
            }

            if (usePipeLine)
            {
                redisPipeline.hset(info.url, REDIS_FIELD_NAME, updatedValue);
            }
            else
            {
                client.hset(appendPrefix(info.url), REDIS_FIELD_NAME, updatedValue);
            }
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Write " + urlInfo.toString() + " related similar URl info to redis : url = " +
                        info.toString() + "; value = " + updatedValue);
            }
        }

        if (usePipeLine)
        {
            redisPipeline.sync();
        }

    }

    private String appendPrefix(String url)
    {
        return URL_PREFIX + url;
    }

    private String composeNewValue(List<UrlInfo> similarUrls)
    {
        Iterable<String> iterable = Iterables.transform(similarUrls, new Function<UrlInfo, String>()
        {
            @Override
            public String apply(UrlInfo input)
            {
                return input.url;
            }
        });
        return Joiner.on(",").join(iterable);
    }


}
