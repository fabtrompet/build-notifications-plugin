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

import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.net.URIBuilder;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * A class that represents a Telegram message
 * Updated to use Apache HttpComponents HttpClient 5.x
 */
public class TelegramMessage implements Message {

  private static final Logger LOGGER = Logger.getLogger(TelegramMessage.class.getName());

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

  public TelegramMessage(String botToken, String chatIds, String extraMessage,
                         String tProxy, String tProxyUsr, String tProxyPwd) {
    LOGGER.info("TelegramMessage()");
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
    boolean result = true;

    for (String chatId : ids) {
      try {
        LOGGER.info("Sending [" + getMessage() + "] to chat_id=[" + chatId + "]");
        URIBuilder uriBuilder = new URIBuilder(String.format("https://api.telegram.org/bot%s/sendMessage", botToken));
        uriBuilder.addParameter("chat_id", chatId);
        uriBuilder.addParameter("text", getMessage());

        String response = Request.post(uriBuilder.build())
                .connectTimeout(3000) // Optional: Set connection timeout
                .responseTimeout(5000) // Optional: Set response timeout
                .execute()
                .returnContent()
                .asString();

        LOGGER.info("Response: " + response);
      } catch (IOException e) {
        result = false;
        LOGGER.warning("Error while sending notification: " + e.getMessage());
      }
    }

    return result;
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
