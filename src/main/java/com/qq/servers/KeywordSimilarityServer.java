package com.qq.servers;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: antyrao
 * Date: 13-10-12
 * Time: 下午1:12
 */
public class KeywordSimilarityServer
{
    private static final Logger LOG = LoggerFactory.getLogger(KeywordSimilarityServer.class);

    private final int port;
    private final double similarity;

    public KeywordSimilarityServer(int port, double similarity)
    {
        this.port = port;
        this.similarity = similarity;
    }

    public void run()
    {
        ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool()));

        bootstrap.setPipelineFactory(new KeywordSimilarityPipelineFactory(similarity));

        bootstrap.bind(new InetSocketAddress(port));
    }

    public static void printUsage()
    {
        System.err.println("Usage: [port] [similarity]");
    }

    public static void main(String[] args)
    {
        int port = 8083;
        double similarity = 0.7;
        try
        {
            if (args.length > 0)
            {
                port = Integer.parseInt(args[0]);
            }
            if (args.length > 1)
            {
                similarity = Double.parseDouble(args[1]);
            }

        }
        catch (NumberFormatException e)
        {
            printUsage();
            System.exit(1);
        }

        LOG.info("Starting server with port " + port + " with similarity value of " + similarity);
        new KeywordSimilarityServer(port, similarity).run();
    }

}
