package com.qq.servers;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Collection;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-8
 * Time: 上午9:38
 */
public class RedisClient
{

    private static Logger LOG = LoggerFactory.getLogger(RedisClient.class);

    private static final String URL_PREFIX = "url_";
    private static final String REDIS_FIELD_NAME = "lk";

    //fields
    private RedisLocator locator;
    private JedisPool jedisPool;

    public RedisClient(String name)
    {
        locator = new RedisLocator(name);
        initRedis();
    }

    private void initRedis()
    {
        JedisPoolConfig conf = new JedisPoolConfig();
        conf.setMaxActive(Integer.MAX_VALUE);
        RedisLocator.RedisLocation location = locator.getRedisLocation();
        jedisPool = new JedisPool(conf, location.getIp(), location.getPort());
    }

    Splitter splitter = Splitter.on(",");
    Joiner joiner = Joiner.on(",");

    public void deleteFromRelatedUrls(UrlInfo toDeleteUrl, Collection<UrlInfo> srcUrls)
    {
        LOG.info("Update existing url " + toDeleteUrl.url + ",  delete existing url from similar url");
        Jedis client = null;
        try
        {
            //TODO consider using redis pipeline
            client = jedisPool.getResource();
            for (UrlInfo delete : srcUrls)
            {
                String key = appendPrefix(delete.url);
                String strUrl = client.hget(key, REDIS_FIELD_NAME);
                Set<String> urls = getUrlList(strUrl);
                urls.remove(toDeleteUrl);
                String newValue = joiner.join(urls);
                client.hset(key, REDIS_FIELD_NAME, newValue);
            }
        }
        catch (Exception e)
        {
            if (client != null)
            {
                jedisPool.returnBrokenResource(client);
                client = null;
                LOG.warn("Catch Unexpected exception when deleting from redis" + e.toString());
            }

            //currently ,there is no way to differ between exception from connection or exception from server,so it's safe bet
            //to re-initialize jedis pool
            jedisPool.destroy();
            client = null;
            initRedis();
        }
        finally
        {
            if (client != null)
            {
                jedisPool.returnResource(client);
            }
        }
    }

    private Set<String> getUrlList(String strUrl)
    {
        Set<String> urls = Sets.newHashSet();
        Iterable<String> iterate = splitter.split(strUrl);
        for (String url : iterate)
        {
            urls.add(url);
        }
        return urls;
    }

    public void updateRedis(UrlInfo urlInfo, Collection<UrlInfo> similarUrls)
    {

        Jedis client = null;
        try
        {
            client = jedisPool.getResource();

            //1. update redis for passed in urlInfo
            String value = joinUrls(similarUrls);
            client.hset(appendPrefix(urlInfo.url), REDIS_FIELD_NAME, value);

            if (LOG.isDebugEnabled())
            {
                LOG.debug("Write URl info to redis : url = " + urlInfo.toString() + "; value = " + value);
            }

            //2. update related urlInfos.
            for (UrlInfo info : similarUrls)
            {
                String key = appendPrefix(info.url);
                String oldValue = client.hget(key, REDIS_FIELD_NAME);
                String updatedValue = getUpdatedValue(urlInfo, oldValue);
                client.hset(key, REDIS_FIELD_NAME, updatedValue);
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Write " + urlInfo.toString() + " related similar URl info to redis : url = " +
                            info.toString() + "; value = " + updatedValue);
                }
            }

        }
        catch (Exception e)
        {
            //TODO duplicate code,refactor the code.
            if (client != null)
            {
                jedisPool.returnBrokenResource(client);
                client = null;
                LOG.warn("Catch Unexpected exception" + e.toString());
            }

            //currently ,there is no way to differ between exception from connection or exception from server,so it's safe bet
            //to re-initialize jedis pool
            jedisPool.destroy();
            client = null;
            initRedis();
        }
        finally
        {
            if (client != null)
            {
                jedisPool.returnResource(client);
            }
        }
    }

    private String getUpdatedValue(UrlInfo urlInfo, String oldValue)
    {
        String updatedValue;
        if (Strings.isNullOrEmpty(oldValue))
        {
            updatedValue = urlInfo.url;
        }
        else
        {
            updatedValue = joiner.join(oldValue, urlInfo.url);
        }
        return updatedValue;
    }

    private String appendPrefix(String url)
    {
        return URL_PREFIX + url;
    }

    private String joinUrls(Collection<UrlInfo> similarUrls)
    {
        Iterable<String> iterable = Iterables.transform(similarUrls, new Function<UrlInfo, String>()
        {
            @Override
            public String apply(UrlInfo input)
            {
                return input.url;
            }
        });
        return joiner.join(iterable);
    }


}
