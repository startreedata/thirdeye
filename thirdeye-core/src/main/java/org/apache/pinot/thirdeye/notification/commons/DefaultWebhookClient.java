package org.apache.pinot.thirdeye.notification.commons;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class DefaultWebhookClient {
  public static int doPost(String url, Object entity){
    HttpPost post = new HttpPost(url);
    CloseableHttpClient defaultClient = HttpClients.createDefault();
    try {
      StringEntity temp = new StringEntity("{\"data\":"+entity.toString()+"}");
      post.setEntity(temp);
      post.setHeader("Accept", "application/json");
      post.setHeader("Content-type", "application/json");
      return defaultClient.execute(post).getStatusLine().getStatusCode();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (ClientProtocolException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        defaultClient.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return -1;
  }
}
