package ibt.ortc.util;

import ibt.ortc.api.Proxy;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

/**
 * Created by msweer1 on 18-12-2015.
 */
public class IOUtil {
    public static void close(InputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(OutputStream stream) {
        if (stream == null) {
            return;
        }

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(Reader reader) {
        if (reader == null) {
            return;
        }

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close(Writer writer) {
        if (writer == null) {
            return;
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HttpURLConnection getHttpURLConnection(URL url, Proxy proxy) throws IOException {
      HttpURLConnection connection;
      if (proxy != null) {
        java.net.Proxy jnp = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
        connection = (HttpURLConnection) url.openConnection(jnp);
        connection.setRequestProperty("Connection", "Keep-alive");
        connection.setRequestProperty("Proxy-Connection", "Keep-alive");
          if (proxy.getProxyAuth() != null) {
              connection.setRequestProperty("Proxy-Authorization", "Basic " + proxy.getProxyAuth());
          }
      } else {
        connection = (HttpURLConnection) url.openConnection();
      }
      return connection;
    }

    public static String doGetRequest(URL url, Proxy proxy) throws IOException {
      HttpURLConnection connection = null;
      InputStream inputStream = null;
      try {
        connection = getHttpURLConnection(url, proxy);
        if (connection.getResponseCode() != 200 && connection.getResponseCode() != -1) {
          inputStream = connection.getErrorStream();
          String errorMsg;

          if (inputStream != null) {
            errorMsg = readResponseBody(inputStream);
          } else {
            errorMsg = "unknown error";
          }
          throw new IOException(errorMsg);
        } else {
          inputStream = connection.getInputStream();

          return readResponseBody(inputStream);
        }
      } finally {
        close(inputStream);
        if (connection != null) {
          connection.disconnect();
        }
      }
    }

    public static String doPostRequest(URL url, String postBody, Proxy proxy) throws IOException {
      HttpURLConnection connection = null;
      String result;
      InputStream inputStream = null;
      OutputStreamWriter wr = null;

      try {
        connection = getHttpURLConnection(url, proxy);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // CAUSE: Reliance on default encoding
        wr = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");

        wr.write(postBody);

        wr.flush();
        // CAUSE: Method may fail to close stream on exception

        if (connection.getResponseCode() != 200) {
          inputStream = connection.getErrorStream();
          String errorMsg;

          if (inputStream != null) {
            errorMsg = readResponseBody(inputStream);
          } else {
            errorMsg = "unknown error";
          }
          throw new IOException(errorMsg);
        } else {
          inputStream = connection.getInputStream();

          result = readResponseBody(inputStream);
        }
      } finally {
        close(wr);
        close(inputStream);
        if (connection != null) {
          connection.disconnect();
        }
      }

      return result.toString();
    }

    private static String readResponseBody(InputStream responseBody) throws IOException {
      // TODO: specify a correct capacity
      StringBuilder result = new StringBuilder(16);

      BufferedReader rd = null;

      try {
        // CAUSE: Reliance on default encoding
        rd = new BufferedReader(new InputStreamReader(responseBody, "UTF-8"));
        String line = rd.readLine();
        // CAUSE: Assignment expressions nested inside other expressions
        while (line != null) {
          // CAUSE: Method concatenates strings using + in a loop
          result.append(line);
          line = rd.readLine();
        }
      } catch (IOException e) {
        result = new StringBuilder(e.getMessage());
      } finally {
        close(rd);
      }

      return result.toString();
    }
}
