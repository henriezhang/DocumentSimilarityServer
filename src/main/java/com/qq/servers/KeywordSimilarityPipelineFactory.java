package com.qq.servers;

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-10-12
 * Time: 下午1:16
 */
public class KeywordSimilarityPipelineFactory implements ChannelPipelineFactory
{

    private UrlGlobalState state;
    public KeywordSimilarityPipelineFactory(double similarity)
    {
        this.state = new UrlGlobalState(similarity);
    }
    @Override
    public ChannelPipeline getPipeline() throws Exception
    {

        ChannelPipeline pipeline = Channels.pipeline();

//        pipeline.addLast("printraw",new PrintRawHandler());
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(10485760));
        pipeline.addLast("encode", new HttpResponseEncoder());
        pipeline.addLast("chunkedwriter", new ChunkedWriteHandler());
        pipeline.addLast("handler", new KeywordSimilarityServerHandler(state));

        return pipeline;

    }
}