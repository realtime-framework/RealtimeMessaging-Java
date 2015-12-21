/**
 * @fileoverview This file contains the ortc client abstract class
 * @author ORTC team members (ortc@ibt.pt)
 */
package ibt.ortc.extensibility;

import ibt.ortc.api.Balancer;
import ibt.ortc.api.InvalidBalancerServerException;
import ibt.ortc.api.OnDisablePresence;
import ibt.ortc.api.OnEnablePresence;
import ibt.ortc.api.OnPresence;
import ibt.ortc.api.Ortc;
import ibt.ortc.api.Pair;
import ibt.ortc.api.Proxy;
import ibt.ortc.api.Strings;
import ibt.ortc.extensibility.exception.OrtcAlreadyConnectedException;
import ibt.ortc.extensibility.exception.OrtcDoesNotHavePermissionException;
import ibt.ortc.extensibility.exception.OrtcEmptyFieldException;
import ibt.ortc.extensibility.exception.OrtcInvalidCharactersException;
import ibt.ortc.extensibility.exception.OrtcMaxLengthException;
import ibt.ortc.extensibility.exception.OrtcNotConnectedException;
import ibt.ortc.extensibility.exception.OrtcNotSubscribedException;
import ibt.ortc.extensibility.exception.OrtcSubscribedException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;

/**
 * Abstract class representing an Ortc Client
 * 
 * <br>
 * <b>How to use in android:<b>
 * 
 * <pre>
 * try {
 * 	Ortc ortc = new Ortc();
 * 
 * 	OrtcFactory factory;
 * 
 * 	factory = ortc.loadOrtcFactory(&quot;IbtRealtimeSJ&quot;);
 * 
 * 	client = factory.createClient();
 * 
 * 	HashMap&lt;String, ChannelPermissions&gt; permissions = new HashMap&lt;String, ChannelPermissions&gt;();
 * 	permissions.put(&quot;channel1:*&quot;, ChannelPermissions.Write);
 * 	permissions.put(&quot;channel1&quot;, ChannelPermissions.Write);
 * 
 * 	if (!Ortc.saveAuthentication(
 * 			&quot;http://ortc-developers.realtime.co/server/2.1/&quot;, true,
 * 			&quot;SessionId&quot;, false, &quot;APPKEY&quot;, 1800, &quot;PVTKEY&quot;, permissions)) {
 * 		throw new Exception(&quot;Was not possible to authenticate&quot;);
 * 	}
 * 
 * 	client.setClusterUrl(defaultServerUrl);
 * 	client.setConnectionMetadata(&quot;DroidApp&quot;);
 * 
 * 	client.onConnected = new OnConnected() {
 * 		&#064;Override
 * 		public void run(final Object sender) {
 * 			runOnUiThread(new Runnable() {
 * 				&#064;Override
 * 				public void run() {
 * 					TextView t = ((TextView) findViewById(R.id.TextViewTitle));
 * 					t.setText(&quot;Client connected to: &quot;
 * 							+ ((OrtcClient) sender).getUrl());
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.onDisconnected = new OnDisconnected() {
 * 		&#064;Override
 * 		public void run(Object arg0) {
 * 			runOnUiThread(new Runnable() {
 * 				&#064;Override
 * 				public void run() {
 * 					TextView t = ((TextView) findViewById(R.id.TextViewTitle));
 * 					t.setText(&quot;Client disconnected&quot;);
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.onSubscribed = new OnSubscribed() {
 * 		&#064;Override
 * 		public void run(Object sender, String channel) {
 * 			final String subscribedChannel = channel;
 * 			runOnUiThread(new Runnable() {
 * 				&#064;Override
 * 				public void run() {
 * 					TextView textViewLog = (TextView) findViewById(R.id.TextViewLog);
 * 					textViewLog.append(String.format(&quot;Channel subscribed %s\n&quot;,
 * 							subscribedChannel));
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.onUnsubscribed = new OnUnsubscribed() {
 * 		&#064;Override
 * 		public void run(Object sender, String channel) {
 * 			final String subscribedChannel = channel;
 * 			runOnUiThread(new Runnable() {
 * 				&#064;Override
 * 				public void run() {
 * 					TextView textViewLog = (TextView) findViewById(R.id.TextViewLog);
 * 					textViewLog.append(String.format(
 * 							&quot;Channel unsubscribed %s\n&quot;, subscribedChannel));
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.onException = new OnException() {
 * 		&#064;Override
 * 		public void run(Object send, Exception ex) {
 * 			final Exception exception = ex;
 * 			runOnUiThread(new Runnable() {
 * 				&#064;Override
 * 				public void run() {
 * 					TextView textViewLog = (TextView) findViewById(R.id.TextViewLog);
 * 					textViewLog.append(String.format(&quot;Ortc Error: %s\n&quot;,
 * 							exception.getMessage()));
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.onReconnected = new OnReconnected() {
 * 		&#064;Override
 * 		public void run(final Object sender) {
 * 			runOnUiThread(new Runnable() {
 * 				&#064;Override
 * 				public void run() {
 * 					reconnectingTries = 0;
 * 					TextView textViewLog = (TextView) findViewById(R.id.TextViewTitle);
 * 					textViewLog.setText(&quot;Client reconnected to: &quot;
 * 							+ ((OrtcClient) sender).getUrl());
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.onReconnecting = new OnReconnecting() {
 * 		&#064;Override
 * 		public void run(Object sender) {
 * 			runOnUiThread(new Runnable() {
 * 				&#064;Override
 * 				public void run() {
 * 					reconnectingTries++;
 * 					TextView textViewLog = (TextView) findViewById(R.id.TextViewTitle);
 * 					textViewLog.setText(String.format(&quot;Client reconnecting %s&quot;,
 * 							reconnectingTries));
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.connect(defaultApplicationKey, defaultAuthenticationToken);
 * 
 * } catch (Exception e) {
 * 	System.out.println(&quot;ORTC ERROR: &quot; + e.toString());
 * }
 * </pre>
 * 
 * <br>
 * <b>How to use in java:<b>
 * 
 * <pre>
 * try {
 * 	boolean isBalancer = true;
 * 
 * 	Ortc api = new Ortc();
 * 
 * 	OrtcFactory factory = api.loadOrtcFactory(&quot;IbtRealtimeSJ&quot;);
 * 
 * 	final OrtcClient client = factory.createClient();
 * 
 * 	if (isBalancer) {
 * 		client.setClusterUrl(serverUrl);
 * 	} else {
 * 		client.setUrl(serverUrl);
 * 	}
 * 
 * 	System.out.println(String.format(&quot;Connecting to server %s&quot;, serverUrl));
 * 
 * 	client.onConnected = new OnConnected() {
 * 		&#064;Override
 * 		public void run(Object sender) {
 * 			System.out
 * 					.println(String.format(&quot;Connected to %s&quot;, client.getUrl()));
 * 
 * 			client.subscribe(&quot;channel1&quot;, true, new OnMessage() {
 * 				&#064;Override
 * 				public void run(Object sender, String channel, String message) {
 * 					System.out.println(String.format(
 * 							&quot;Message received on channel %s: '%s'&quot;, channel,
 * 							message));
 * 
 * 					((OrtcClient) sender).send(channel, &quot;Echo &quot; + message);
 * 				}
 * 			});
 * 		}
 * 	};
 * 
 * 	client.onException = new OnException() {
 * 		&#064;Override
 * 		public void run(Object send, Exception ex) {
 * 			System.out.println(String.format(&quot;Error: '%s'&quot;, ex.toString()));
 * 		}
 * 	};
 * 
 * 	client.onDisconnected = new OnDisconnected() {
 * 		&#064;Override
 * 		public void run(Object sender) {
 * 			System.out.println(&quot;Disconnected&quot;);
 * 		}
 * 	};
 * 
 * 	client.onReconnected = new OnReconnected() {
 * 		&#064;Override
 * 		public void run(Object sender) {
 * 			System.out.println(String.format(&quot;Reconnected to %s&quot;,
 * 					client.getUrl()));
 * 		}
 * 	};
 * 
 * 	client.onReconnecting = new OnReconnecting() {
 * 		&#064;Override
 * 		public void run(Object sender) {
 * 			System.out.println(String.format(&quot;Reconnecting to %s&quot;,
 * 					client.getUrl()));
 * 		}
 * 	};
 * 
 * 	client.onSubscribed = new OnSubscribed() {
 * 		&#064;Override
 * 		public void run(Object sender, String channel) {
 * 			System.out.println(String.format(&quot;Subscribed to channel %s&quot;,
 * 					channel));
 * 		}
 * 	};
 * 
 * 	client.onUnsubscribed = new OnUnsubscribed() {
 * 		&#064;Override
 * 		public void run(Object sender, String channel) {
 * 			System.out.println(String.format(&quot;Unsubscribed from channel %s&quot;,
 * 					channel));
 * 		}
 * 	};
 * 
 * 	System.out.println(&quot;Connecting...&quot;);
 * 	client.connect(&quot;APPLICATION_KEY&quot;, &quot;AUTHENTICATION_TOKEN&quot;);
 * 
 * } catch (Exception e) {
 * 	System.out.println(&quot;ORTC ERROR: &quot; + e.toString());
 * }
 * </pre>
 * 
 * @version 2.1.0 23 Mar 2013
 * @author IBT
 * 
 */
public abstract class OrtcClient {

	// ========== Constants ==========

	protected static final int MAX_MESSAGE_SIZE = 800;
	protected static final int MAX_CHANNEL_SIZE = 100;
	public static final int MAX_CONNECTION_METADATA_SIZE = 256;
	protected static final int CONNECTION_TIMEOUT_DEFAULT_VALUE = 5000;

	// ========== Constants ==========

	// ========== Enumerators ==========

	private enum ChannelPermission {
		Read, Write
	}

	// ========== Enumerators ==========

	// ========== Private Classes ==========

	public static class BufferedMessage implements Comparable<BufferedMessage> {
		private int messagePart;
		private String content;

		// CAUSE: Constructor is declared public in non-public class
		private BufferedMessage(int messagePart, String content) {
			this.messagePart = messagePart;
			this.content = content;
		}

		public String getContent() {
			return content;
		}

		@Override
		// CAUSE: Class defines compareTo(...) and uses Object.equals()
		public int compareTo(BufferedMessage o) {
			int result = o.messagePart == this.messagePart ? 0
					: o.messagePart > this.messagePart ? -1 : 1;
			return result;
		}

		@Override
		// CAUSE: Class defines compareTo(...) and uses Object.equals()
		public boolean equals(Object o) {
			return o instanceof BufferedMessage && super.equals(o);
		}

		@Override
		// CAUSE: Class defines compareTo(...) and uses Object.equals()
		public int hashCode() {
			return super.hashCode();
		}
	}

	// ========== Private Classes ==========

	// ========== Pseudo Delegates ==========

	/**
	 * Event fired when a connection is established
	 */
	public OnConnected onConnected;
	/**
	 * Event fired when a connection is closed
	 */
	public OnDisconnected onDisconnected;
	/**
	 * Event fired when a exception occurs
	 */
	public OnException onException;
	/**
	 * Event fired when a connection is reestablished after being closed
	 * unexpectedly
	 */
	public OnReconnected onReconnected;
	/**
	 * Event fired when a connection is trying to be reestablished after being
	 * closed unexpectedly
	 */
	public OnReconnecting onReconnecting;
	/**
	 * Event fired when a channel is subscribed
	 */
	public OnSubscribed onSubscribed;
	/**
	 * Event fired when a channel is unsubscribed
	 */
	public OnUnsubscribed onUnsubscribed;

	// ========== Pseudo Delegates ==========

	// ========== Properties ==========

	protected String clusterUrl;
	protected String url;
	protected String connectionMetadata;
	protected String announcementSubChannel;
	protected String applicationKey;
	protected String authenticationToken;

	protected URI uri;

	protected int connectionTimeout;
	protected int id;
	protected ConnectionProtocol protocol;

	protected Hashtable<String, ChannelSubscription> subscribedChannels;
	protected Hashtable<String, String> channelsPermissions;

	private Hashtable<String, LinkedList<BufferedMessage>> multiPartMessagesBuffer;

	private boolean isCluster;

	protected boolean isConnected;
	protected boolean isDisconnecting;
	protected boolean isReconnecting;
	protected boolean isConnecting;
	
	protected Proxy proxy;

	protected static final int heartbeatMaxTime = 60;
	protected static final int heartbeatMinTime = 10;

	protected static final int heartbeatMaxFails = 6;
	protected static final int heartbeatMinFails = 1;

	protected boolean heartbeatActive = false;
	protected int heartbeatFails = 3;
	protected int heartbeatTime = 15;
	protected HeartbeatSender heartbeatSender = null;
	
	// ========== Properties ==========

	// ========== Construtctor ==========
	
	/**
	 * Creates an instance of Ortc Client
	 */
	public OrtcClient() {
		this.connectionTimeout = CONNECTION_TIMEOUT_DEFAULT_VALUE;
		this.isConnected = false;
		this.isCluster = false;
		this.isDisconnecting = false;
		this.isReconnecting = false;
		this.isConnecting = false;
		this.proxy = null;

		this.subscribedChannels = new Hashtable<String, ChannelSubscription>(11);
		this.channelsPermissions = new Hashtable<String, String>(11);
		this.multiPartMessagesBuffer = new Hashtable<String, LinkedList<BufferedMessage>>(
				11);
	}

	// ========== Construtctor ==========

	/**
	 * Connects the ortc client to the url previously specified
	 * 
	 * <pre>
	 * client.setClusterUrl(defaultServerUrl);
	 * client.connect(&quot;APPKEY&quot;, &quot;PVTKEY&quot;);
	 * </pre>
	 * 
	 * @param applicationKey
	 *            Application Key provided by the Ortc Services
	 * @param authenticationToken
	 *            Authentication Token representing the connection. This should
	 *            be generated by the application, such as session id for
	 *            example.
	 */
	public void connect(final String applicationKey, final String authenticationToken) {
		/*
		 * Sanity checks
		 */
		if (isConnected) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcAlreadyConnectedException());
		} else if (Strings.isNullOrEmpty(clusterUrl)
				&& Strings.isNullOrEmpty(url)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("URL"));
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Cluster URL"));
		} else if (Strings.isNullOrEmpty(applicationKey)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Application Key"));
		} else if (Strings.isNullOrEmpty(authenticationToken)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Authentication Token"));
		} else if (!isCluster && !Strings.ortcIsValidUrl(url)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("URL"));
		} else if (isCluster && !Strings.ortcIsValidUrl(clusterUrl)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("Cluster URL"));
		} else if (!Strings.ortcIsValidInput(applicationKey)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("Application Key"));
		} else if (!Strings.ortcIsValidInput(authenticationToken)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("Authentication Token"));
		} else if (!Strings.isNullOrEmpty(announcementSubChannel)
				&& !Strings.ortcIsValidInput(announcementSubChannel)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException(
							"Announcement Subchannel"));
		} else if (!Strings.isNullOrEmpty(connectionMetadata)
				&& connectionMetadata.length() > MAX_CONNECTION_METADATA_SIZE) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcMaxLengthException("Connection metadata",
							MAX_CONNECTION_METADATA_SIZE));
		} else if (isConnecting && !isReconnecting) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcNotConnectedException("Already trying to connect"));
		} else {
			final OrtcClient self = this;
			new Thread(new Runnable() {        
        @Override
        public void run() {
          Exception e = null;
          try {
            if(!isReconnecting){
              self.isConnecting = true;
            }       
            self.applicationKey = applicationKey;
            self.authenticationToken = authenticationToken;

            if (self.isCluster) {
              String clusterServer = Balancer.getServerFromBalancer(
                  self.clusterUrl, self.applicationKey, self.proxy);
              self.setUrl(clusterServer);
              self.isCluster = true;
            }

            self.uri = new URI(self.url);
            self.protocol = "http".equals(uri.getScheme()) ? ConnectionProtocol.Unsecure
                : ConnectionProtocol.Secure;
            
            self.connect();
          } 
          catch (IllegalArgumentException ex) {
             e = ex;
          }
          catch (URISyntaxException ex) {
            e = ex;
          } catch (MalformedURLException ex) {
            e = ex;
          } catch (IOException ex) {
            e = ex;
          } catch (InvalidBalancerServerException ex) {
            e = ex;
          } finally {
            if (e != null) {          
              self.isReconnecting = true;
              self.raiseOrtcEvent(EventEnum.OnException, self, new Exception("Unable to connect"));    
              self.raiseOrtcEvent(EventEnum.OnReconnecting, self);                
            }
          }
        }
      }).start();
		}
	}

	protected abstract void connect();

	/**
	 * Closes the current connection
	 */
	public void disconnect() {
		if(!isConnected){
			if(this.isReconnecting){
				stopReconnecting();
				this.isConnecting = false;
			} else {
				raiseOrtcEvent(EventEnum.OnException,this, new OrtcNotConnectedException());
			}
		}else{
			stopHeartBeatInterval();
			this.disconnectIntern();
		}
	}

	protected abstract void disconnectIntern();

	private Pair<Boolean, String> channelHasPermission(String channelName,
			ChannelPermission permission) {
		Pair<Boolean, String> result = new Pair<Boolean, String>(true, null);

		if (channelsPermissions.size() > 0) {
			int domainChannelCharacterIndex = channelName.indexOf(':');
			String channelToValidate = channelName;

			if (domainChannelCharacterIndex > 0) {
				channelToValidate = String.format("%s%s", channelName
						.substring(0, domainChannelCharacterIndex + 1), "*");
			}

			String hash = channelsPermissions.get(channelName);
			if (Strings.isNullOrEmpty(hash)) {
				hash = channelsPermissions.get(channelToValidate);
			}

			result = new Pair<Boolean, String>(!Strings.isNullOrEmpty(hash),
					hash);
		}

		if (!result.first) {
			String errorMessage = String
					.format("No permission found to %s to the channel %s",
							permission == ChannelPermission.Read ? "subscribe"
									: "send", channelName);
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcDoesNotHavePermissionException(errorMessage));
		}

		return result;
	}

	private Pair<Boolean, String> isSendValid(String channelName, String message, String applicationKey, String privateKey) {
		// NOTE: Sanity check for send method
		Pair<Boolean, String> result = new Pair<Boolean, String>(true, null);

		if (!isConnected) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcNotConnectedException());
			result.first = false;
		} else if (Strings.isNullOrEmpty(channelName)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Channel"));
			result.first = false;
		} else if (!Strings.ortcIsValidInput(channelName)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("Channel"));
			result.first = false;
		} else if (Strings.isNullOrEmpty(message)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Message"));
			result.first = false;
		} else if (channelName.length() > MAX_CHANNEL_SIZE) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcMaxLengthException("Channel", MAX_CHANNEL_SIZE));
			result.first = false;
		}

		if (result.first && Strings.isNullOrEmpty(applicationKey) && Strings.isNullOrEmpty(privateKey)) {
			Pair<Boolean, String> channelPermission = channelHasPermission(
					channelName, ChannelPermission.Write);
			result.first = channelPermission.first;
			result.second = channelPermission.second;
		}

		return result;
	}

	/**
	 * Sends a message to the specified channel.
	 * 
	 * @param channel
	 *            Channel to wich the message should be sent
	 * @param message
	 *            The content of the message to be sent
	 */
	public void send(String channel, String message) {
		// CAUSE: Assignment to method parameter
		Pair<Boolean, String> sendValidation = isSendValid(channel, message, null, null);

		//String lMessage = message.replace("\n", "\\n");

		if (sendValidation != null && sendValidation.first) {
			try {
				String messageId = Strings.randomString(8);
				ArrayList<Pair<String, String>> messagesToSend = multiPartMessage(
						message, messageId);
				for (Pair<String, String> messageToSend : messagesToSend) {
					send(channel, messageToSend.second, messageToSend.first,
							sendValidation.second);
				}
			} catch (IOException e) {
				raiseOrtcEvent(EventEnum.OnException, this, e);
			}
		}
	}
	
	public void sendProxy(String applicationKey, String privateKey, String channel, String message) {
		// CAUSE: Assignment to method parameter
		Pair<Boolean, String> sendValidation = isSendValid(channel, message, applicationKey,privateKey);

		//String lMessage = message.replace("\n", "\\n");

		if (sendValidation != null && sendValidation.first) {
			try {
				String messageId = Strings.randomString(8);
				ArrayList<Pair<String, String>> messagesToSend = multiPartMessage(
						message, messageId);
				for (Pair<String, String> messageToSend : messagesToSend) {
					send(applicationKey,privateKey, channel, messageToSend.second, messageToSend.first,
							sendValidation.second);
				}
			} catch (IOException e) {
				raiseOrtcEvent(EventEnum.OnException, this, e);
			}
		}
	}

	private ArrayList<Pair<String, String>> multiPartMessage(String message,
			String messageId) throws IOException {
		// CAUSE: Reliance on default encoding
		byte[] messageBytes = message.getBytes("UTF-8");
		// CAUSE: Instantiating collection without specified initial capacity
		ArrayList<Pair<String, String>> messageParts = new ArrayList<Pair<String, String>>(
				10);

		int totalParts = (messageBytes.length % MAX_MESSAGE_SIZE) == 0 ? (messageBytes.length / MAX_MESSAGE_SIZE)
				: (messageBytes.length / MAX_MESSAGE_SIZE) + 1;

		int messagePartIndex = 1;
		int currentPosition = 0;

		do {
			int messagePartSize = messageBytes.length - currentPosition > MAX_MESSAGE_SIZE ? MAX_MESSAGE_SIZE
					: messageBytes.length - currentPosition;
			if (messagePartSize > 0) {
				byte[] messagePartBytes = new byte[messagePartSize];
				System.arraycopy(messageBytes, currentPosition,
						messagePartBytes, 0, messagePartSize);

				String messagePartIdentifier = String.format("%s_%s-%s",
						messageId, messagePartIndex, totalParts);

				messageParts.add(new Pair<String, String>(
				// CAUSE: Reliance on default encoding
						messagePartIdentifier, new String(messagePartBytes,
								"UTF-8")));
			}

			currentPosition += messagePartSize;
			messagePartIndex++;
		} while (currentPosition < messageBytes.length);

		return messageParts;
	}

	protected abstract void send(String channel, String message,
			String messagePartIdentifier, String permission);
	
	protected abstract void send(String applicationKey, String privateKey, String channel, String message,
			String messagePartIdentifier, String permission);

	private Pair<Boolean, String> isSubscribeValid(String channelName,
			ChannelSubscription channel) {
		// NOTE: Sanity check for subscribe method
		Pair<Boolean, String> result = new Pair<Boolean, String>(true, null);

		// CAUSE: Unused assignment
		Exception error;

		if (!isConnected) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcNotConnectedException());
			result.first = false;
		} else if (Strings.isNullOrEmpty(channelName)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Channel"));
			result.first = false;
		} else if (!Strings.ortcIsValidInput(channelName)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("Channel"));
			result.first = false;
		} else if (channel != null && channel.isSubscribing()) {
			raiseOrtcEvent(
					EventEnum.OnException,
					this,
					new OrtcSubscribedException(String.format(
							"Already subscribing to the channel %s",
							channelName)));
			result.first = false;
		} else if (channel != null && channel.isSubscribed()) {
			error = new OrtcSubscribedException(String.format(
					"Already subscribed to the channel %s", channelName));
			raiseOrtcEvent(EventEnum.OnException, this, error);
			result.first = false;
		} else if (channelName.length() > MAX_CHANNEL_SIZE) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcMaxLengthException("Channel", MAX_CHANNEL_SIZE));
			result.first = false;
		}

		if (result.first) {
			Pair<Boolean, String> channelPermission = channelHasPermission(
					channelName, ChannelPermission.Read);
			result.first = channelPermission.first;
			result.second = channelPermission.second;
		}

		return result;
	}

	/**
	 * Subscribe the specified channel in order to receive messages in that
	 * channel
	 * 
	 * @param channel
	 *            Channel to be subscribed
	 * @param subscribeOnReconnect
	 *            Indicates if the channel should be subscribe if the event on
	 *            reconnected is fired
	 * @param onMessage
	 *            Event handler that will be called when a message will be
	 *            received on the subscribed channel
	 */
	public void subscribe(String channel, boolean subscribeOnReconnect,
			OnMessage onMessage) {
		ChannelSubscription subscribedChannel = subscribedChannels.get(channel);
		Pair<Boolean, String> subscribeValidation = isSubscribeValid(channel,
				subscribedChannel);

		if (subscribeValidation != null && subscribeValidation.first) {
			subscribedChannel = new ChannelSubscription(subscribeOnReconnect,
					onMessage);
			subscribedChannel.setSubscribing(true);
			subscribedChannels.put(channel, subscribedChannel);

			subscribe(channel, subscribeValidation.second);
		}
	}

	protected abstract void subscribe(String channel, String permission);

	private boolean isUnsubscribeValid(String channelName,
			ChannelSubscription channel) {
		boolean result = true;

		if (!isConnected) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcNotConnectedException());
			result = false;
		} else if (channel == null) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Channel"));
			result = false;
		} else if (!Strings.ortcIsValidInput(channelName)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("Channel"));
			result = false;
		} else if (!channel.isSubscribed()) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcNotSubscribedException(channelName));
			result = false;
		} else if (channelName.length() > MAX_CHANNEL_SIZE) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcMaxLengthException("Channel", MAX_CHANNEL_SIZE));
			result = false;
		}

		return result;
	}

	/**
	 * Stop receiving messages in the specified channel
	 * 
	 * @param channel
	 *            Channel to be unsubscribed
	 */
	public void unsubscribe(String channel) {
		ChannelSubscription subscribedChannel = subscribedChannels.get(channel);

		if (isUnsubscribeValid(channel, subscribedChannel)) {
			subscribedChannel.setSubscribeOnReconnected(false);
			unsubscribe(channel, true);
		}
	}

	protected abstract void unsubscribe(String channel, boolean isValid);

	protected void stopReconnecting() {
		this.isDisconnecting = false;
		this.isReconnecting = false;
	}

	protected void cancelSubscription(String channel) {
		if (!Strings.isNullOrEmpty(channel)
				&& subscribedChannels.containsKey(channel)) {
			subscribedChannels.get(channel).setSubscribing(false);
		}
	}

	/**
	 * Gets the subscriptions in the specified channel and if active the first
	 * 100 unique metadata.
	 * 
	 * <pre>
	 * Ortc.presence(&quot;CHANNEL&quot;, new onPresence() {
	 * 
	 * 	public void run(Exception error, Presence presence) {
	 * 		if (error != null) {
	 * 			System.out.println(error.getMessage());
	 * 		} else {
	 * 			System.out.println(&quot;Subscriptions - &quot; + presence.getSubscriptions());
	 * 
	 * 			Iterator&lt;?&gt; metadataIterator = presence.getMetadata().entrySet()
	 * 					.iterator();
	 * 			while (metadataIterator.hasNext()) {
	 * 				Map.Entry&lt;String, Long&gt; entry = (Map.Entry&lt;String, Long&gt;) metadataIterator
	 * 						.next();
	 * 				System.out.println(entry.getKey() + &quot; - &quot; + entry.getValue());
	 * 			}
	 * 		}
	 * 	}
	 * });
	 * </pre>
	 * 
	 * @param channel
	 *            Channel with presence data active.
	 * @param callback
	 *            Callback with error and result.
	 * @throws ibt.ortc.extensibility.exception.OrtcNotConnectedException
	 */
	public void presence(String channel, OnPresence callback)
			throws OrtcNotConnectedException {
		if (!this.isConnected) {
			throw new OrtcNotConnectedException();
		} else {
			String presenceUrl = this.isCluster ? this.clusterUrl : this.url;

			Ortc.presence(presenceUrl, this.isCluster, this.applicationKey, this.authenticationToken, channel, this.proxy, callback);
		}
	}

	/**
	 * Enables presence for the specified channel with first 100 unique metadata
	 * if metadata is set to true.
	 * 
	 * <pre>
	 * Ortc.enablePresence(&quot;PRIVATE_KEY&quot;, &quot;CHANNEL&quot;, true, new onEnablePresence() {
	 * 
	 * 	public void run(Exception error, String result) {
	 * 		if (error != null) {
	 * 			System.out.println(error.getMessage());
	 * 		} else {
	 * 			System.out.println(result);
	 * 
	 * 		}
	 * 	}
	 * });
	 * </pre>
	 * 
	 * @param privateKey
	 *            The private key provided when the ORTC service is purchased.
	 * @param channel
	 *            Channel with presence data active.
	 * @param metadata
	 *            Defines if to collect first 100 unique metadata.
	 * @param callback
	 *            Callback with error and result.
	 * @throws ibt.ortc.extensibility.exception.OrtcNotConnectedException
	 */
	public void enablePresence(String privateKey, String channel,
			Boolean metadata, OnEnablePresence callback)
			throws OrtcNotConnectedException {
		if (!this.isConnected) {
			throw new OrtcNotConnectedException();
		} else {
			String presenceUrl = this.isCluster ? this.clusterUrl : this.url;

			Ortc.enablePresence(presenceUrl, this.isCluster, this.applicationKey, privateKey, channel, metadata, this.proxy, callback);
		}
	}

	/**
	 * Disables presence for the specified channel.
	 * 
	 * <pre>
	 * Ortc.disablePresence(&quot;PRIVATE_KEY&quot;, &quot;CHANNEL&quot;, new onDisablePresence() {
	 * 
	 * 	public void run(Exception error, String result) {
	 * 		if (error != null) {
	 * 			System.out.println(error.getMessage());
	 * 		} else {
	 * 			System.out.println(result);
	 * 
	 * 		}
	 * 	}
	 * });
	 * </pre>
	 * 
	 * @param privateKey
	 *            The private key provided when the ORTC service is purchased.
	 * @param channel
	 *            Channel to disable presence
	 * @param callback
	 *            Callback with error and result.
	 * @throws ibt.ortc.extensibility.exception.OrtcNotConnectedException
	 */
	public void disablePresence(String privateKey, String channel,
			OnDisablePresence callback) throws OrtcNotConnectedException {
		if (!this.isConnected) {
			throw new OrtcNotConnectedException();
		} else {
			String presenceUrl = this.isCluster ? this.clusterUrl : this.url;

			Ortc.disablePresence(presenceUrl, this.isCluster, this.applicationKey, privateKey, channel, this.proxy, callback);
		}
	}

	// ========== Getters and Setters ==========

	/**
	 * Indicates if the channel is subscribed
	 * 
	 * @param channel
	 *            Channel to check
	 * @return boolean True if the channel is subscribed otherwise false
	 */
	public Boolean isSubscribed(String channel) {
		Boolean result = null;

		/*
		 * Sanity checks
		 */
		if (!isConnected) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcNotConnectedException());
		} else if (Strings.isNullOrEmpty(channel)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcEmptyFieldException("Channel"));
		} else if (!Strings.ortcIsValidInput(channel)) {
			raiseOrtcEvent(EventEnum.OnException, this,
					new OrtcInvalidCharactersException("Channel"));
		} else {
			ChannelSubscription subscribedChannel = subscribedChannels
					.get(channel);
			result = subscribedChannel != null
					&& subscribedChannel.isSubscribed();
		}

		return result;
	}

	/**
	 * Gets the Ortc client connection metadata
	 * 
	 * @return String Connection metadata content
	 */
	public String getConnectionMetadata() {
		return this.connectionMetadata;
	}

	/**
	 * Sets the client connection metadata
	 * 
	 * @param connectionMetadata
	 *            Connection metada content
	 */
	public void setConnectionMetadata(String connectionMetadata) {
		this.connectionMetadata = connectionMetadata;
	}

	/**
	 * Gets the announcement sub channel to where the announcement messages are
	 * going to be sent
	 * 
	 * @return String The announcement sub channel
	 */
	public String getAnnouncementSubChannel() {
		return this.announcementSubChannel;
	}

	/**
	 * Sets the announcement sub channel to where the announcement messages are
	 * going to be sent
	 * 
	 * @param channel
	 *            Announcement sub channel
	 */
	public void setAnnouncementSubChannel(String channel) {
		announcementSubChannel = channel;
	}

	/**
	 * Gets the cluster gateway url to wich the Ortc client is connecting
	 * 
	 * @return String The cluster gateway url
	 */
	public String getClusterUrl() {
		return this.clusterUrl;
	}

	/**
	 * Sets the cluster gateway url to wich the Ortc client should connect
	 * 
	 * @param clusterUrl
	 *            Ortc cluster gateway url
	 */
	public void setClusterUrl(String clusterUrl) {
		this.isCluster = true;
		this.clusterUrl = Strings.treatUrl(clusterUrl);
	}

	/**
	 * Gets the Ortc server url to wich the Ortc client is connecting
	 * 
	 * @return String Ortc server url
	 */
	public String getUrl() {
		return this.url;
	}

	/**
	 * Sets the Ortc server url to wich the Ortc client should connect
	 * 
	 * @param url
	 *            Ortc server url
	 */
	public void setUrl(String url) {
		this.isCluster = false;
		this.url = Strings.treatUrl(url);
	}

	/**
	 * Gets the connection timeout before trying a new reconnection attempt
	 * 
	 * @return int Reconnect connection timeout
	 */
	public int getConnectionTimeout() {
		return this.connectionTimeout;
	}

	/**
	 * Sets the connection timeout before trying a new reconnection attempt
	 * 
	 * @param connectionTimeout
	 *            Reconnect connection timeout
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * Gets Ortc client unique identifier
	 * 
	 * @return int Ortc client unique identifier
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Sets Ortc client unique identifier
	 * 
	 * @param id
	 *            Ortc client unique identifier
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Indicates if the ortc client is connected
	 * 
	 * @return boolean True if is connected otherwise false
	 */
	public boolean getIsConnected() {
		return this.isConnected;
	}

	/**
	 * Defines the proxy connection settings for the ortc client
	 * 
	 * @param proxyHost Proxy host
	 * @param proxyPort Proxy port
	 */
	public void setProxy(String proxyHost, int proxyPort){
		setProxy(proxyHost, proxyPort, null, null);
	}

	/**
	 * sets proxy, optionally using basic authentication
	 *
	 * @param host	proxy host
	 * @param port	proxy port
	 * @param user	proxy user or null when no authentication is needed
	 * @param pwd	proxy password or null when no authentication is needed
	 */
	public void setProxy(String host, int port, String user, String pwd) {
		this.proxy = new Proxy(host, port, user, pwd);
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	// ========== Getters and Setters ==========

	// ========== Raise of events ==========

	protected void raiseOrtcEvent(EventEnum eventToRaise, Object... args) {
		switch (eventToRaise) {
		case OnConnected:
			raiseOnConnected(args);
			break;
		case OnDisconnected:
			raiseOnDisconnected(args);
			break;
		case OnException:
			raiseOnException(args);
			break;
		case OnReconnected:
			raiseOnReconnected(args);
			break;
		case OnReconnecting:
			raiseOnReconnecting(args);
			break;
		case OnSubscribed:
			raiseOnSubscribed(args);
			break;
		case OnUnsubscribed:
			raiseOnUnsubscribed(args);
			break;
		case OnReceived:
			raiseOnReceived(args);
			break;
		}
	}

	private void raiseOnConnected(Object... args) {
		this.isConnected = true;
		this.isDisconnecting = false;		
		if (isReconnecting && !isConnecting) {		  
			raiseOrtcEvent(EventEnum.OnReconnected, args);
		} else {
		  this.isConnecting = false;
			if (onConnected != null) {
				OrtcClient sender = args != null
						&& args.length == 1 ? (OrtcClient) args[0] : null;

				onConnected.run(sender);
				startHeartBeatInterval();
			}
		}

	}

	private void raiseOnDisconnected(Object... args) {
		stopHeartBeatInterval();
		OrtcClient sender = args != null && args.length == 1 ? (OrtcClient) args[0]
				: null;
		this.channelsPermissions = new Hashtable<String, String>(11);
		if (isDisconnecting || isConnecting) {
			this.isConnected = false;
			this.isDisconnecting = false;
			this.isConnecting = false;
			this.subscribedChannels = new Hashtable<String, ChannelSubscription>(
					11);
			if (onDisconnected != null) {
				onDisconnected.run(sender);
			}
		} else {
		  if(!isReconnecting){
		    if (onDisconnected != null) {
	        onDisconnected.run(sender);
	      }
		  }
			raiseOrtcEvent(EventEnum.OnReconnecting, args);
		}
	}

	private void raiseOnException(Object... args) {
		if (onException != null) {
			OrtcClient sender = args != null && args.length == 2 ? (OrtcClient) args[0]
					: null;
			Exception exception = args != null && args.length == 2 ? (Exception) args[1]
					: null;
			onException.run(sender, exception);
		}
	}

	private void raiseOnReconnected(Object... args) {
		this.isReconnecting = false;

		LinkedList<String> channelsToRemove = new LinkedList<String>();
		Set<String> subscribedChannelsSet = this.subscribedChannels.keySet();

		for (String channelName : subscribedChannelsSet) {
			ChannelSubscription subscribedChannel = this.subscribedChannels
					.get(channelName);

			if (subscribedChannel.subscribeOnReconnected()) {
				subscribedChannel.setSubscribing(true);
				subscribedChannel.setSubscribed(false);

				Pair<Boolean, String> channelPermission = channelHasPermission(
						channelName, ChannelPermission.Read);

				if (channelPermission != null && channelPermission.first) {
					subscribe(channelName, channelPermission.second);
				}
			} else {
				channelsToRemove.add(channelName);
			}
		}

		for (String channelToRemove : channelsToRemove) {
			this.subscribedChannels.remove(channelToRemove);
		}

		if (onReconnected != null) {
			OrtcClient sender = args != null && args.length == 1 ? (OrtcClient) args[0]
					: null;
			onReconnected.run(sender);
			startHeartBeatInterval();
		}
	}

	private void raiseOnReconnecting(Object... args) {
		stopHeartBeatInterval();
		if (isReconnecting) {
			try {
				Thread.sleep(this.connectionTimeout);
			} catch (InterruptedException e) {
				raiseOrtcEvent(EventEnum.OnException, this, e);
			}
		}

		this.isConnected = false;
		this.isDisconnecting = false;
		this.isReconnecting = true;		

		if (onReconnecting != null) {
			OrtcClient sender = args != null && args.length == 1 ? (OrtcClient)  args[0]
					: null;
			onReconnecting.run(sender);
		}

		this.connect(this.applicationKey, this.authenticationToken);
	}

	private void raiseOnSubscribed(Object... args) {

		OrtcClient sender = args != null && args.length == 2 ? (OrtcClient) args[0]
				: null;
		String channel = args != null && args.length == 2 ? (String) args[1]
				: null;

		ChannelSubscription subscribeChannel = subscribedChannels.get(channel);
		subscribeChannel.setSubscribed(true);
		subscribeChannel.setSubscribing(false);

		if (onSubscribed != null) {
			onSubscribed.run(sender, channel);
		}

	}

	private void raiseOnUnsubscribed(Object... args) {
		OrtcClient sender = args != null && args.length == 2 ? (OrtcClient) args[0]
				: null;
		String channel = args != null && args.length == 2 ? (String) args[1]
				: null;

		ChannelSubscription subscribeChannel = subscribedChannels.get(channel);
		subscribeChannel.setSubscribed(false);
		subscribeChannel.setSubscribing(false);

		if (onUnsubscribed != null) {
			onUnsubscribed.run(sender, channel);
		}
	}

	private void raiseOnReceived(Object... args) {
		String channel = args != null && args.length == 5 ? (String) args[0]
				: null;
		String message = args != null && args.length == 5 ? (String) args[1]
				: null;
		String messageId = args != null && args.length == 5 ? (String) args[2]
				: null;

		// CAUSE: Possible null pointer dereference
		Integer messagePart = args != null && args.length == 5 ? (Integer) args[3]
				: null;
		// CAUSE: Possible null pointer dereference
		Integer messageTotalParts = args != null && args.length == 5 ? (Integer) args[4]
				: null;

		if (messagePart != null && messagePart == -1
				|| (messagePart != null && messageTotalParts != null)
				&& (messagePart == 1 && messageTotalParts == 1)) {
			ChannelSubscription subscription = subscribedChannels.get(channel);
			if (subscription != null) {
				OnMessage onMessageEventHandler = subscription.getOnMessage();
				if (onMessageEventHandler != null) {
					message = CharEscaper.removeEsc(message);
				  onMessageEventHandler.run(this, channel, message);
					try {						
						if (messageId != null
								&& multiPartMessagesBuffer
										.containsKey(messageId)) {
							multiPartMessagesBuffer.remove(messageId);
						}
					} catch (Exception e) {
						raiseOrtcEvent(EventEnum.OnException, this, e);
					}
				}
			}
		} else {
			if (!multiPartMessagesBuffer.containsKey(messageId)) {
				multiPartMessagesBuffer.put(messageId,
						new LinkedList<BufferedMessage>());
			}

			// CAUSE: Possible null pointer dereference
			if (multiPartMessagesBuffer.get(messageId) != null) {
				multiPartMessagesBuffer.get(messageId).add(
						new BufferedMessage(messagePart, message));
			}

			LinkedList<BufferedMessage> messageParts = multiPartMessagesBuffer
					.get(messageId);
			// CAUSE: Possible null pointer dereference
			if (messageParts != null
					&& messageParts.size() == messageTotalParts) {
				Collections.sort(messageParts);
				String fullMessage = "";
				for (BufferedMessage part : messageParts) {
					fullMessage = String.format("%s%s", fullMessage,
							part.getContent());
				}
				raiseOnReceived(channel, fullMessage, messageId, -1, -1);
			}
		}

	}


	/**
	 * Get if heartbeat active.
	 *
	 * @return if heartbeat is active.
	 */
	public boolean getHeartbeatActive() {
		return heartbeatActive;
	}

	/**
	 * Set heart beat active. Heart beat provides better accuracy for presence data.
	 *
	 * @param active
	 * 		true to activate heartbeat and false to deactivate.
	 */
	public void setHeartbeatActive(boolean active) {
		this.heartbeatActive = active;
	}

	/**
	 * Get how many times can the client fail the heartbeat.
	 *
	 * @return amount of fails the heartbeat can have before disconnecting.
	 */
	public int getHeartbeatFails() {
		return heartbeatFails;
	}

	/**
	 * Set heartbeat fails. Defines how many times can the client fail the heartbeat.
	 *
	 * @param newHeartbeatFails
	 */
	public void setHeartbeatFails(int newHeartbeatFails) {
		if (newHeartbeatFails > 0) {
			if (newHeartbeatFails > heartbeatMaxFails
					|| newHeartbeatFails < heartbeatMinFails) {
				raiseOrtcEvent(EventEnum.OnException, this, new Exception(
						"Heartbeat fails is out of limits - Min: "
								+ heartbeatMinFails + " | Max: "
								+ heartbeatMaxFails));
			} else {
				heartbeatFails = newHeartbeatFails;
			}
		} else {
			raiseOrtcEvent(EventEnum.OnException, this, new Exception(
					"Invalid heartbeat fails " + newHeartbeatFails));
		}
	}

	/**
	 * Get heartbeat interval.
	 *
	 * @return interval between heartbeats.
	 */
	public int getHeartbeatTime() {
		return heartbeatTime;
	}

	/**
	 * Set heartbeat fails. Defines how many times can the client fail the heartbeat.
	 *
	 * @param newHeartbeatTime
	 */
	public void setHeartbeatTime(int newHeartbeatTime) {
		if (newHeartbeatTime > 0) {
			if (newHeartbeatTime > heartbeatMaxTime
					|| newHeartbeatTime < heartbeatMinTime) {
				raiseOrtcEvent(EventEnum.OnException, this, new Exception(
						"Heartbeat time is out of limits - Min: "
								+ heartbeatMinTime + " | Max: "
								+ heartbeatMaxTime));
			} else {
				heartbeatTime = newHeartbeatTime;
			}
		} else {
			raiseOrtcEvent(EventEnum.OnException, this, new Exception(
					"Invalid heartbeat time " + newHeartbeatTime));
		}
	}

	public void startHeartBeatInterval() {
		if (heartbeatSender == null && heartbeatActive) {
			heartbeatSender = new HeartbeatSender(this);
		}
	}

	public void stopHeartBeatInterval() {
		if (heartbeatSender != null) {
			heartbeatSender.stop();
			heartbeatSender = null;
		}
	}

	protected abstract void sendHeartbeat();

	// ========== Raise of events ==========
}
