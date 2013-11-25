package com.qq.servers;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;


/**
 *
 */
public class KeywordSimilarityServerHandler extends SimpleChannelUpstreamHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(KeywordSimilarityServerHandler.class);
    private static final String URL_KEY = "url";
    private static final String KEYWORD_KEY = "key";

    HttpRequest request;
    private UrlStateHandler handler;

    public KeywordSimilarityServerHandler(UrlStateHandler handler)
    {
        this.handler = handler;
    }


    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception
    {
        this.request = (HttpRequest) e.getMessage();
        if (request.getMethod().equals(HttpMethod.POST))
        {
            String body = request.getContent().toString(getContentCharset(request));
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Receive http message " + request.toString() + ",body = " + body);
            }
            if (Strings.isNullOrEmpty(body))
            {
                writeEmptyResponse(e);
                return;
            }
            QueryStringDecoder postQueryDecoder = new QueryStringDecoder("/?" + body, CharsetUtil.UTF_8);
            Map<String, List<String>> postParameters = postQueryDecoder.getParameters();
            if (postParameters.get(URL_KEY) == null || postParameters.get(KEYWORD_KEY) == null)
            {
                writeEmptyResponse(e);
                return;
            }
            String url = postParameters.get(URL_KEY).get(0);
            String keywords = postParameters.get(KEYWORD_KEY).get(0);

            Map<String, Double> value = toMap(keywords);
            if (value != null)
            {
                UrlInfo info = new UrlInfo(url, value);
                handler.process(info);
            }
        }

        writeEmptyResponse(e);
    }


    private void writeEmptyResponse(MessageEvent e)
    {
        writeResponse(e.getChannel(), "");
    }


    private void writeResponse(Channel channel, String responseContent)
    {
        // Convert the response content to a ChannelBuffer.
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent,
                CharsetUtil.UTF_8);

        // Decide whether to close the connection or not.
        boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request
                .getHeader(HttpHeaders.Names.CONNECTION))
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
                .getHeader(HttpHeaders.Names.CONNECTION));

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);
        response.setContent(buf);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (!close)
        {
            // There's no need to add 'Content-Length' header
            // if this is the last response.
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH,
                    String.valueOf(buf.readableBytes()));
        }

        // Write the response.
        ChannelFuture future = channel.write(response);
        // Close the connection after the write operation is done if necessary.
        if (close)
        {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static Charset getContentCharset(HttpRequest request)
    {
        String contentType = request.getHeader(HttpHeaders.Names.CONTENT_TYPE);
        if (contentType == null)
        {
            return CharsetUtil.UTF_8;
        }
        int indexOfCharset = contentType.indexOf("charset");
        if (indexOfCharset == -1)
        {
            return CharsetUtil.UTF_8;
        }
        String charsetName = contentType.substring(indexOfCharset + "charset".length() + 1).trim();
        return Charset.forName(charsetName);
    }


    private static Map<String, Double> toMap(String kwStr)
    {
        Map<String, Double> result = Maps.newHashMap();
        String[] keywords = kwStr.split(",");
        for (String keyword : keywords)
        {
            String[] keyAndWord = keyword.split(":");
            if (keyAndWord.length < 2)
            {
                LOG.warn("received ill-formatted key " + kwStr);
                return null;
            }
            result.put(keyAndWord[0].trim(), Double.valueOf(keyAndWord[1]));
        }
        return result;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception
    {

        LOG.warn("Unexpected exception from downstream", e.getCause());
        e.getChannel().close();

    }
}
