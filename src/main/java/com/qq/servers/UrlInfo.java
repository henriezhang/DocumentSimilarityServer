package com.qq.servers;

import com.google.common.base.Objects;

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

    public String toString()
    {
        return Objects.toStringHelper(this.getClass()).add("url", url).add("keywords", keywords.toString()).toString();
    }
}
