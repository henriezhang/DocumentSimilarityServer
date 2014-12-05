package com.qq.servers;

import com.google.common.base.Objects;
import com.google.common.hash.Hasher;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-11-7
 * Time: 下午1:24
 */
public class UrlInfo
{
    public final String url;
    public final Map<String, Double> keywords;

    public UrlInfo(String url, Map<String, Double> keywords)
    {
        this.url = url;
        this.keywords = keywords;
    }

    public Map<String, Double> getKeyWord()
    {
        return this.keywords;
    }

    @Override
    public int hashCode()
    {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other)
            return true;
        if (other == null)
            return false;
        if (other instanceof UrlInfo)
        {
            UrlInfo otherInfo = (UrlInfo) other;
            return this.url.equals(otherInfo.url);
        }
        return false;
    }

    public String toString()
    {
        return Objects.toStringHelper(this.getClass()).add("url", url).add("keywords", keywords.toString()).toString();
    }
}
