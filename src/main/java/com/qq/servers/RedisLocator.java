package com.qq.servers;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-27
 * Time: 下午5:55
 */
public class RedisLocator
{
    private static final Logger LOG = LoggerFactory.getLogger(RedisLocator.class);

    private String name;

    public RedisLocator(String name)
    {
        this.name = name;
    }

    public static class RedisLocation
    {

        private String ip;
        private int port;

        public RedisLocation(String ip, int port)
        {
            this.ip = ip;
            this.port = port;
        }

        public String getIp()
        {
            return ip;
        }

        public int getPort()
        {
            return port;
        }
    }

    public RedisLocation getRedisLocation()
    {
        try
        {
            Runtime run = Runtime.getRuntime();
            Process p = run.exec("/usr/bin/zkname " + name);
            BufferedInputStream in = new BufferedInputStream(p.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String s;
            RedisLocation location = null;
            while ((s = br.readLine()) != null)
            {
                String[] fields = s.split("\\t");
                if (fields.length == 2)
                {
                    location = new RedisLocation(fields[0], Integer.parseInt(fields[1]));
                    break;
                }
            }
            if (location == null)
            {
                throw new RuntimeException("Can't get ips for name " + name);
            }
            return location;
        }
        catch (Exception e)
        {
            LOG.error(Throwables.getStackTraceAsString(e));
            throw new RuntimeException("Can't get ips for name " + name, e);
        }
    }


}
