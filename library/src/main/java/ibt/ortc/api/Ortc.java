/**
 * @fileoverview This file contains the class to create ortc factories
 * @author ORTC team members (ortc@ibt.pt) 
 */
package ibt.ortc.api;

import ibt.ortc.extensibility.OrtcFactory;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Class containing the methods to create Ortc Client factories and use the Ortc Rest services <br>
 * <b>How to use in android:<b>
 * 
 * <pre>
 * try {
 *   Ortc ortc = new Ortc();
 * 
 *   OrtcFactory factory;
 * 
 *   factory = ortc.loadOrtcFactory(&quot;IbtRealtimeSJ&quot;);
 * 
 *   client = factory.createClient();
 * 
 *   HashMap&lt;String, ChannelPermissions&gt; permissions = new HashMap&lt;String, ChannelPermissions&gt;();
 *   permissions.put(&quot;channel1:*&quot;, ChannelPermissions.Write);
 *   permissions.put(&quot;channel1&quot;, ChannelPermissions.Write);
 * 
 *   if (!Ortc.saveAuthentication(&quot;http://ortc-developers.realtime.co/server/2.1/&quot;, true, &quot;SessionId&quot;, false, &quot;APPKEY&quot;, 1800,
 *       &quot;PVTKEY&quot;, permissions)) {
 *     throw new Exception(&quot;Was not possible to authenticate&quot;);
 *   }
 * 
 *   client.setClusterUrl(defaultServerUrl);
 *   client.setConnectionMetadata(&quot;DroidApp&quot;);
 * 
 *   client.OnConnected = new OnConnected() {
 *     &#064;Override
 *     public void run(final OrtcClient sender) {
 *       runOnUiThread(new Runnable() {
 *         &#064;Override
 *         public void run() {
 *           TextView t = ((TextView) findViewById(R.id.TextViewTitle));
 *           t.setText(&quot;Client connected to: &quot; + ((OrtcClient) sender).getUrl());
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.OnDisconnected = new OnDisconnected() {
 *     &#064;Override
 *     public void run(OrtcClient sender) {
 *       runOnUiThread(new Runnable() {
 *         &#064;Override
 *         public void run() {
 *           TextView t = ((TextView) findViewById(R.id.TextViewTitle));
 *           t.setText(&quot;Client disconnected&quot;);
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.OnSubscribed = new OnSubscribed() {
 *     &#064;Override
 *     public void run(OrtcClient sender, String channel) {
 *       final String subscribedChannel = channel;
 *       runOnUiThread(new Runnable() {
 *         &#064;Override
 *         public void run() {
 *           TextView textViewLog = (TextView) findViewById(R.id.TextViewLog);
 *           textViewLog.append(String.format(&quot;Channel subscribed %s\n&quot;, subscribedChannel));
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.OnUnsubscribed = new OnUnsubscribed() {
 *     &#064;Override
 *     public void run(OrtcClient sender, String channel) {
 *       final String subscribedChannel = channel;
 *       runOnUiThread(new Runnable() {
 *         &#064;Override
 *         public void run() {
 *           TextView textViewLog = (TextView) findViewById(R.id.TextViewLog);
 *           textViewLog.append(String.format(&quot;Channel unsubscribed %s\n&quot;, subscribedChannel));
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.OnException = new OnException() {
 *     &#064;Override
 *     public void run(OrtcClient send, Exception ex) {
 *       final Exception exception = ex;
 *       runOnUiThread(new Runnable() {
 *         &#064;Override
 *         public void run() {
 *           TextView textViewLog = (TextView) findViewById(R.id.TextViewLog);
 *           textViewLog.append(String.format(&quot;Ortc Error: %s\n&quot;, exception.getMessage()));
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.OnReconnected = new OnReconnected() {
 *     &#064;Override
 *     public void run(final OrtcClient sender) {
 *       runOnUiThread(new Runnable() {
 *         &#064;Override
 *         public void run() {
 *           reconnectingTries = 0;
 *           TextView textViewLog = (TextView) findViewById(R.id.TextViewTitle);
 *           textViewLog.setText(&quot;Client reconnected to: &quot; + ((OrtcClient) sender).getUrl());
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.OnReconnecting = new OnReconnecting() {
 *     &#064;Override
 *     public void run(OrtcClient sender) {
 *       runOnUiThread(new Runnable() {
 *         &#064;Override
 *         public void run() {
 *           reconnectingTries++;
 *           TextView textViewLog = (TextView) findViewById(R.id.TextViewTitle);
 *           textViewLog.setText(String.format(&quot;Client reconnecting %s&quot;, reconnectingTries));
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.connect(defaultApplicationKey, defaultAuthenticationToken);
 * 
 * } catch (Exception e) {
 *   System.out.println(&quot;ORTC ERROR: &quot; + e.toString());
 * }
 * </pre>
 * 
 * <br>
 * <b>How to use in java:<b>
 * 
 * <pre>
 * try {
 *   boolean isBalancer = true;
 * 
 *   Ortc api = new Ortc();
 * 
 *   OrtcFactory factory = api.loadOrtcFactory(&quot;IbtRealtimeSJ&quot;);
 * 
 *   final OrtcClient client = factory.createClient();
 * 
 *   if (isBalancer) {
 *     client.setClusterUrl(serverUrl);
 *   } else {
 *     client.setUrl(serverUrl);
 *   }
 * 
 *   System.out.println(String.format(&quot;Connecting to server %s&quot;, serverUrl));
 * 
 *   client.OnConnected = new OnConnected() {
 *     &#064;Override
 *     public void run(OrtcClient sender) {
 *       System.out.println(String.format(&quot;Connected to %s&quot;, client.getUrl()));
 *       System.out.println(String.format(&quot;Session ID: %s\n&quot;, ((OrtcClient) sender).getSessionId()));
 * 
 *       client.subscribe(&quot;channel1&quot;, true, new OnMessage() {
 *         &#064;Override
 *         public void run(Object sender, String channel, String message) {
 *           System.out.println(String.format(&quot;Message received on channel %s: '%s'&quot;, channel, message));
 * 
 *           ((OrtcClient) sender).send(channel, &quot;Echo &quot; + message);
 *         }
 *       });
 *     }
 *   };
 * 
 *   client.OnException = new OnException() {
 *     &#064;Override
 *     public void run(OrtcClient send, Exception ex) {
 *       System.out.println(String.format(&quot;Error: '%s'&quot;, ex.toString()));
 *     }
 *   };
 * 
 *   client.OnDisconnected = new OnDisconnected() {
 *     &#064;Override
 *     public void run(OrtcClient sender) {
 *       System.out.println(&quot;Disconnected&quot;);
 *     }
 *   };
 * 
 *   client.OnReconnected = new OnReconnected() {
 *     &#064;Override
 *     public void run(OrtcClient sender) {
 *       System.out.println(String.format(&quot;Reconnected to %s&quot;, client.getUrl()));
 *     }
 *   };
 * 
 *   client.OnReconnecting = new OnReconnecting() {
 *     &#064;Override
 *     public void run(OrtcClient sender) {
 *       System.out.println(String.format(&quot;Reconnecting to %s&quot;, client.getUrl()));
 *     }
 *   };
 * 
 *   client.OnSubscribed = new OnSubscribed() {
 *     &#064;Override
 *     public void run(OrtcClient sender, String channel) {
 *       System.out.println(String.format(&quot;Subscribed to channel %s&quot;, channel));
 *     }
 *   };
 * 
 *   client.OnUnsubscribed = new OnUnsubscribed() {
 *     &#064;Override
 *     public void run(OrtcClient sender, String channel) {
 *       System.out.println(String.format(&quot;Unsubscribed from channel %s&quot;, channel));
 *     }
 *   };
 * 
 *   System.out.println(&quot;Connecting...&quot;);
 *   client.connect(&quot;APPLICATION_KEY&quot;, &quot;AUTHENTICATION_TOKEN&quot;);
 * 
 * } catch (Exception e) {
 *   System.out.println(&quot;ORTC ERROR: &quot; + e.toString());
 * }
 * </pre>
 * 
 * @version 2.1.0 27 Mar 2013
 * @author IBT
 * 
 */
public class Ortc {
  public Ortc() {
  }

  /**
   * Creates an instance of a factory of the specified Ortc plugin type
   * 
   * @param ortcType
   *          The Ortc plugin type
   * @return Instance of Ortc factory
   * @throws InstantiationException
   * @throws IllegalAccessException
   * @throws ClassNotFoundException
   */
  public OrtcFactory loadOrtcFactory(String ortcType) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    OrtcFactory result = null;

    // Gets the plugin class definition
    Class<?> factoryClass = this.getClass().getClassLoader()
        .loadClass(String.format("ibt.ortc.plugins.%s.%sFactory", ortcType, ortcType));

    if (factoryClass != null) {
      // Creates an instance of the plugin class
      result = OrtcFactory.class.cast(factoryClass.newInstance());
    }

    return result;
  }

  /**
   * Saves the authentication token channels permissions in the ORTC server (optionally using proxy connection).
   *
   * <pre>
   * HashMap&lt;String, ChannelPermissions&gt; permissions = new HashMap&lt;String, ChannelPermissions&gt;();
   * permissions.put(&quot;channel1:*&quot;, ChannelPermissions.Write);
   * permissions.put(&quot;channel1&quot;, ChannelPermissions.Write);
   *
   * if (!Ortc.saveAuthentication(&quot;http://ortc-developers.realtime.co/server/2.1/&quot;, true, &quot;SessionId&quot;, false, &quot;APPKEY&quot;, 1800,
   * 	&quot;PVTKEY&quot;, new Proxy(&quot;my.proxy.net&quot;, 1234), permissions)) {
   *   throw new Exception(&quot;Was not possible to authenticate&quot;);
   * }
   * </pre>
   *
   * @param url
   *          Ortc Server Url
   * @param isCluster
   *          Indicates whether the ORTC server is in a cluster.
   * @param authenticationToken
   *          Authentication Token which is generated by the application server, for instance a unique session ID.
   * @param authenticationTokenIsPrivate
   *          Indicates whether the authentication token is private (true) or not (false)
   * @param applicationKey
   *          Application Key that was provided to you together with the ORTC service purchasing.
   * @param timeToLive
   *          The authentication token time to live, in other words, the allowed activity time (in seconds).
   * @param privateKey
   *          The private key provided to you together with the ORTC service purchasing.
   * @param permissions
   *          The channels and their permissions (w: write/read or r: read or p: presence, case sensitive).
   * @param proxy Object with definition of proxy connection (ibt.ortc.api.Proxy) or null if no proxy should be used
   * @return True if the authentication was successful or false if it was not.
   * @throws OrtcAuthenticationNotAuthorizedException
   */
  public static boolean saveAuthentication(String url, boolean isCluster, String authenticationToken,
      boolean authenticationTokenIsPrivate, String applicationKey, int timeToLive, String privateKey,
      Map<String, ChannelPermissions> permissions, Proxy proxy) throws IOException, InvalidBalancerServerException,
      OrtcAuthenticationNotAuthorizedException {
    String connectionUrl = url;
    if (isCluster) {
      connectionUrl = Balancer.getServerFromBalancer(url,applicationKey, proxy);
    }

    boolean isAuthenticated = false;

    try {
      URL authenticationUrl = new URL(String.format("%s/authenticate", connectionUrl));

      Map<String, LinkedList<ChannelPermissions>> permissionsMap = new HashMap<String, LinkedList<ChannelPermissions>>();

      Set<String> channels = permissions.keySet();
      for(String channelName : channels){
        LinkedList<ChannelPermissions> channelPermissionList = new LinkedList<ChannelPermissions>();
        channelPermissionList.add(permissions.get(channelName));
        permissionsMap.put(channelName, channelPermissionList);
      }

      isAuthenticated = Authentication.saveAuthentication(authenticationUrl,authenticationToken, authenticationTokenIsPrivate, applicationKey, timeToLive, privateKey, permissionsMap, proxy);
    } catch (Exception e) {
      throw new OrtcAuthenticationNotAuthorizedException(e.getMessage());
    }

    return isAuthenticated;
  }

  /**
   * Saves the authentication token channels permissions in the ORTC server (optionally using proxy connection).
   *
   * <pre>
   * HashMap&lt;String, LinkedList&lt;ChannelPermissions&gt;&gt; permissions = new HashMap&lt;String, LinkedList&lt;ChannelPermissions&gt;&gt;();
   *
   * LinkedList&lt;ChannelPermissions&gt; channelPermissions = new LinkedList&lt;ChannelPermissions&gt;();
   * channelPermissions.add(ChannelPermissions.Write);
   * channelPermissions.add(ChannelPermissions.Presence);
   *
   * permissions.put(&quot;channel&quot;, channelPermissions);
   *
   * if (!Ortc.saveAuthentication(&quot;http://ortc-developers.realtime.co/server/2.1/&quot;, true, &quot;SessionId&quot;, false, &quot;APPKEY&quot;, 1800,
   * 	&quot;PVTKEY&quot;, new Proxy(&quot;my.proxy.net&quot;, 1234), permissions)) {
   *   throw new Exception(&quot;Was not possible to authenticate&quot;);
   * }
   * </pre>
   *
   * @param url
   *          Ortc Server Url
   * @param isCluster
   *          Indicates whether the ORTC server is in a cluster.
   * @param authenticationToken
   *          Authentication Token which is generated by the application server, for instance a unique session ID.
   * @param authenticationTokenIsPrivate
   *          Indicates whether the authentication token is private (true) or not (false)
   * @param applicationKey
   *          Application Key that was provided to you together with the ORTC service purchasing.
   * @param timeToLive
   *          The authentication token time to live, in other words, the allowed activity time (in seconds).
   * @param privateKey
   *          The private key provided to you together with the ORTC service purchasing.
   * @param permissions
   *          &ltString,LinkedList&lt;String,ChannelPermissions&gt;&gt; permissions& The channels and their permissions (w:
   *          write/read or r: read or p: presence, case sensitive).
   * @param proxy Object with definition of proxy connection (ibt.ortc.api.Proxy) or null if no proxy should be used
   * @return True if the authentication was successful or false if it was not.
   * @throws OrtcAuthenticationNotAuthorizedException
   */
  // CAUSE: Prefer throwing/catching meaningful exceptions instead of Exception
  public static boolean saveAuthentication(String url, boolean isCluster, String authenticationToken,
      boolean authenticationTokenIsPrivate, String applicationKey, int timeToLive, String privateKey,
      HashMap<String, LinkedList<ChannelPermissions>> permissions, Proxy proxy) throws IOException, InvalidBalancerServerException,
      OrtcAuthenticationNotAuthorizedException {
    String connectionUrl = url;
    if (isCluster) {
      connectionUrl = Balancer.getServerFromBalancer(url, applicationKey, proxy);
    }

    boolean isAuthenticated = false;

    try {
      URL authenticationUrl = new URL(String.format("%s/authenticate", connectionUrl));

      isAuthenticated = Authentication.saveAuthentication(authenticationUrl, authenticationToken, authenticationTokenIsPrivate,
          applicationKey, timeToLive, privateKey, permissions, proxy);
    } catch (Exception e) {
      throw new OrtcAuthenticationNotAuthorizedException(e.getMessage());
    }

    return isAuthenticated;
  }

  /**
   * Gets the subscriptions in the specified channel and if active the first 100 unique metadata (optionally using proxy connection).
   * 
   * <pre>
   * Ortc.presence(&quot;http://ortc-developers.realtime.co/server/2.1/&quot;, true, &quot;APPLICATION_KEY&quot;, &quot;AUTHENTICATION_TOKEN&quot;,
   * 	&quot;CHANNEL&quot;, new Proxy(&quot;my.proxy.net&quot;, 1234), new onPresence() {
   * 
   *       public void run(Exception error, Presence presence) {
   *         if (error != null) {
   *           System.out.println(error.getMessage());
   *         } else {
   *           System.out.println(&quot;Subscriptions - &quot; + presence.getSubscriptions());
   * 
   *           Iterator&lt;?&gt; metadataIterator = presence.getMetadata().entrySet().iterator();
   *           while (metadataIterator.hasNext()) {
   *             Map.Entry&lt;String, Long&gt; entry = (Map.Entry&lt;String, Long&gt;) metadataIterator.next();
   *             System.out.println(entry.getKey() + &quot; - &quot; + entry.getValue());
   *           }
   *         }
   *       }
   *     });
   * </pre>
   * 
   * @param url  Server containing the presence service.
   * @param isCluster Specifies if url is cluster.
   * @param applicationKey Application key with access to presence service.
   * @param authenticationToken Authentication token with access to presence service.
   * @param channel Channel with presence data active.
   * @param proxy Object with definition of proxy connection (ibt.ortc.api.Proxy) or null if no proxy should be used
   * @param callback Callback with error and result.
   */
  public static void presence(String url, Boolean isCluster, String applicationKey, String authenticationToken, String channel, Proxy proxy, OnPresence callback) {
	    Presence.getPresence(url, isCluster, applicationKey, authenticationToken, channel, proxy, callback);
  }

  /**
   * Enables presence for the specified channel with first 100 unique metadata if metadata is set to true (using proxy connection).
   * 
   * <pre>
   * Ortc.enablePresence(&quot;http://ortc-developers.realtime.co/server/2.1/&quot;, true, &quot;APPLICATION_KEY&quot;, &quot;PRIVATE_KEY&quot;,
   * 	&quot;CHANNEL&quot;, true, new Proxy(&quot;my.proxy.net&quot;, 1234), new onEnablePresence() {
   * 
   *       public void run(Exception error, String result) {
   *         if (error != null) {
   *           System.out.println(error.getMessage());
   *         } else {
   *           System.out.println(result);
   * 
   *         }
   *       }
   *     });
   * </pre>
   * 
   * @param url
   *          Server containing the presence service.
   * @param isCluster
   *          Specifies if url is cluster.
   * @param applicationKey
   *          Application key with access to presence service.
   * @param privateKey
   *          The private key provided when the ORTC service is purchased.
   * @param channel
   *          Channel with presence data active.
   * @param metadata
   *          Defines if to collect first 100 unique metadata.
   * @param proxy  Object with definition of proxy connection (ibt.ortc.api.Proxy)			
   * @param callback
   *          Callback with error and result.
   */
  public static void enablePresence(String url, Boolean isCluster, String applicationKey, String privateKey, String channel,
	      Boolean metadata, Proxy proxy, OnEnablePresence callback) {
	    Presence.enablePresence(url, isCluster, applicationKey, privateKey, channel, metadata, proxy, callback);
  }

  /**
   * Disables presence for the specified channel (optionally using proxy connection).
   * 
   * <pre>
   * Ortc.disablePresence(&quot;http://ortc-developers.realtime.co/server/2.1/&quot;, true, &quot;APPLICATION_KEY&quot;, &quot;PRIVATE_KEY&quot;,
   * 	&quot;CHANNEL&quot;, new Proxy(&quot;my.proxy.net&quot;, 1234), new onDisablePresence() {
   * 
   *       public void run(Exception error, String result) {
   *         if (error != null) {
   *           System.out.println(error.getMessage());
   *         } else {
   *           System.out.println(result);
   * 
   *         }
   *       }
   *     });
   * </pre>
   * 
   * @param url
   *          Server containing the presence service.
   * @param isCluster
   *          Specifies if url is cluster.
   * @param applicationKey
   *          Application key with access to presence service.
   * @param privateKey
   *          The private key provided when the ORTC service is purchased.
   * @param channel
   *          Channel to disable presence
   * @param proxy  Object with definition of proxy connection (ibt.ortc.api.Proxy) or null if no proxy should be used
   * @param callback
   *          Callback with error and result.
   */
  public static void disablePresence(String url, Boolean isCluster, String applicationKey, String privateKey, String channel, Proxy proxy, OnDisablePresence callback) {
	  Presence.disablePresence(url, isCluster, applicationKey, privateKey, channel, proxy, callback);
  }
}
