package ibt.ortc.api;

import ibt.ortc.util.IOUtil;

import java.io.*;
import java.net.URL;

public class RestWebservice {
  
  protected static void getAsync(URL url, Proxy proxy, OnRestWebserviceResponse callback) {
    requestAsync(url, null, proxy, callback);
  }

  protected static void postAsync(URL url, String content, Proxy proxy, OnRestWebserviceResponse callback) {
    requestAsync(url, content, proxy, callback);
  }

  private static void requestAsync(final URL url, final String postContent, final Proxy proxy, final OnRestWebserviceResponse callback) {
    Runnable task = new Runnable() {

      @Override
      public void run() {
        try {
          String response;
          if (postContent == null) {
            // get request
            response = IOUtil.doGetRequest(url, proxy);
          } else {
            // post request
            response = IOUtil.doPostRequest(url, postContent, proxy);
          }
          callback.run(null, response);
        } catch (IOException error) {
          callback.run(error, null);
        }
      }
    };

    new Thread(task).start();
  }

  private RestWebservice() {
  }
}
