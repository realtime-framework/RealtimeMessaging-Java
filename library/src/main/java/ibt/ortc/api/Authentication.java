/**
 * 
 */
package ibt.ortc.api;

//import java.io.BufferedWriter;
import ibt.ortc.util.IOUtil;

import java.io.IOException;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
        import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
//import javax.net.ssl.SSLSocket;
//import javax.net.ssl.SSLSocketFactory;
//import org.apache.http.HttpException;

/**
 * @author ORTC team members (ortc@ibt.pt)
 * 
 */
public class Authentication {
  // CAUSE: Prefer throwing/catching meaningful exceptions instead of Exception
  protected static boolean saveAuthentication(URL url, String authenticationToken, boolean authenticationTokenIsPrivate,
      String applicationKey, int timeToLive, String privateKey, Map<String, LinkedList<ChannelPermissions>> permissions, Proxy proxy)
      throws IOException {
    String postBody = String.format("AT=%s&AK=%s&PK=%s&TTL=%s&TP=%s&PVT=%s", authenticationToken, applicationKey, privateKey,
        timeToLive, permissions.size(), (authenticationTokenIsPrivate ? "1" : "0"));

    // CAUSE: Inefficient use of keySet iterator instead of entrySet iterator
    for (Map.Entry<String, LinkedList<ChannelPermissions>> channelNamePerms : permissions.entrySet()) {
      LinkedList<ChannelPermissions> channelPermissions = channelNamePerms.getValue();
      // CAUSE: Method concatenates strings using + in a loop
      // TODO: specify a correct capacity
      StringBuilder channelPermissionText = new StringBuilder(16);
      for (ChannelPermissions channelPermission : channelPermissions) {
        channelPermissionText.append(channelPermission.getPermission());
      }

      String channelPermission = String.format("&%s=%s", channelNamePerms.getKey(), channelPermissionText);
      postBody = String.format("%s%s", postBody, channelPermission);
    }

    return postSaveAuthentication(url, postBody, proxy);
  }

  private static boolean postSaveAuthentication(URL url, String postBody, Proxy proxy) throws IOException {
	boolean isAuthenticated = false;
    HttpURLConnection connection = null;
    OutputStreamWriter wr = null;

    try {
      connection = IOUtil.getHttpURLConnection(url, proxy);
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);

      // CAUSE: Reliance on default encoding
      wr = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");

      wr.write(postBody);

      wr.flush();

      if (connection.getResponseCode() == 201) 
    	  isAuthenticated = true;

    } finally {
      IOUtil.close(wr);
      if (connection != null) {
        connection.disconnect();
      }
    }

    
    return isAuthenticated;
  }

  // CAUSE: Utility class contains only static elements and is still instantiable
  private Authentication() {
  }
}
