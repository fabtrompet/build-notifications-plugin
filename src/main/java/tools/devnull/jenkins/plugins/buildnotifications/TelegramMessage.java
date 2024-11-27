/*
 * The MIT License
 *
 * Copyright (c) 2016-2017 Marcelo "Ataxexe" Guimarães
 * <ataxexe@devnull.tools>
 *
 * ----------------------------------------------------------------------
 * Permission  is hereby granted, free of charge, to any person obtaining
 * a  copy  of  this  software  and  associated  documentation files (the
 * "Software"),  to  deal  in the Software without restriction, including
 * without  limitation  the  rights to use, copy, modify, merge, publish,
 * distribute,  sublicense,  and/or  sell  copies of the Software, and to
 * permit  persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this  permission  notice  shall be
 * included  in  all  copies  or  substantial  portions  of the Software.
 *                        -----------------------
 * THE  SOFTWARE  IS  PROVIDED  "AS  IS",  WITHOUT  WARRANTY OF ANY KIND,
 * EXPRESS  OR  IMPLIED,  INCLUDING  BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN  NO  EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM,  DAMAGES  OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT  OR  OTHERWISE,  ARISING  FROM,  OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE   OR   THE   USE   OR   OTHER   DEALINGS  IN  THE  SOFTWARE.
 */
package tools.devnull.jenkins.plugins.buildnotifications;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpHost;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.auth.UsernamePasswordCredentials;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class TelegramMessage implements Message {


    private final String botToken;
    private final String chatIds;
    private final String tProxy;
    private final String tProxyUsr;
    private final String tProxyPwd;
    

    private String extraMessage;
    private String content;
    private String title;
    private String url;
    private String urlTitle;


    public TelegramMessage(String botToken, String chatIds, String extraMessage, String tProxy, String tProxyUsr, String tProxyPwd) {
        this.botToken = botToken;
        this.chatIds = chatIds;
        this.extraMessage = extraMessage;
        this.tProxy = tProxy;
        this.tProxyUsr = tProxyUsr;
        this.tProxyPwd = tProxyPwd;
    }

    public TelegramMessage(String botToken, String chatIds, String extraMessage) {
        this(botToken, chatIds, extraMessage, null, null, null);
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public void setUrl(String url, String title) {
        this.url = url;
        this.urlTitle = title;
    }

    @Override
    public void highPriority() {
        // Not possible with Telegram
    }

    @Override
    public void normalPriority() {
        // Not possible with Telegram
    }

    @Override
    public void lowPriority() {
        // Not possible with Telegram
    }

    public boolean send() {
        String[] ids = chatIds.split("\\s*,\\s*");

        // Set proxy if required
        HttpHost proxy = null;
        if (tProxy != null) {
            String[] split = tProxy.split(":");
            if (split.length == 2) {
                proxy = new HttpHost(split[0], Integer.parseInt(split[1]));
            }
        }

        // Create HttpClient with proxy if necessary
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();

        for (String chatId : ids) {
            HttpPost post = new HttpPost(String.format("https://api.telegram.org/bot%s/sendMessage", botToken));
            List<BasicNameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("chat_id", chatId));
            params.add(new BasicNameValuePair("text", getMessage()));
            try {
                post.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse response = client.execute(post);
                System.out.println("Response: " + response.getStatusLine());
                String responseString = EntityUtils.toString(response.getEntity());
                System.out.println("Response Body: " + responseString);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private String getMessage() {
        return String.format(
            "%s%n%n%s%n%n%s <%s>%n%n%s",
                title,
                content,
                urlTitle,
                url,
                extraMessage
        );
    }
}