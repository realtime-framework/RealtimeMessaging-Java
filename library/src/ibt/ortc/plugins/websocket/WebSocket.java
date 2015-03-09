/*
 *  Copyright (C) 2012 Roderick Baier
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */

package ibt.ortc.plugins.websocket;

import ibt.ortc.api.Proxy;
import ibt.ortc.api.SecureWebConnections;

import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


public class WebSocket
{
	private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	private static final int VERSION = 13;
	
	static final byte OPCODE_TEXT = 0x1;
	static final byte OPCODE_BINARY = 0x2;
	static final byte OPCODE_CLOSE = 0x8;
	static final byte OPCODE_PING = 0x9;
	static final byte OPCODE_PONG = 0xA;
	
	private URI url = null;
	private WebSocketEventHandler eventHandler = null;
	
	private volatile boolean connected = false;
	
	private Socket socket = null;
	private DataInputStream input = null;
	private PrintStream output = null;
	
	private WebSocketReceiver receiver = null;
	//private WebSocketSender sender = null;
	private WebSocketHandshake handshake = null;
	
	private final Random random = new SecureRandom();

	private Proxy proxy = null;
	
	public WebSocket(URI url, Proxy proxy)
	{
		this(url, null, null, proxy);
	}

	public WebSocket(URI url, String protocol, Proxy proxy)
	{
		this(url, protocol, null, proxy);
	}

	public WebSocket(URI url, String protocol, Map<String, String> extraHeaders, Proxy proxy)
	{
		this.url = url;
		this.proxy = proxy;
		handshake = new WebSocketHandshake(url, protocol, extraHeaders);
	}

	public void setEventHandler(WebSocketEventHandler eventHandler)
	{
		this.eventHandler = eventHandler;
	}

	public WebSocketEventHandler getEventHandler()
	{
		return this.eventHandler;
	}

	public void connect() throws WebSocketException, WebSocketProxyException
	{
		try {
			if (connected) {
				throw new WebSocketException("already connected");
			}

			socket = createSocket();
			input = new DataInputStream(socket.getInputStream());
			output = new PrintStream(socket.getOutputStream());

			output.write(handshake.getHandshake());

			boolean handshakeComplete = false;
			int len = 1000;
			byte[] buffer = new byte[len];
			int pos = 0;
			ArrayList<String> handshakeLines = new ArrayList<String>();

			while (!handshakeComplete) {
				int b = input.read();
				buffer[pos] = (byte) b;
				pos += 1;

				if (buffer[pos - 1] == 0x0A && buffer[pos - 2] == 0x0D) {
					String line = new String(buffer, "UTF-8");
					if (line.trim().equals("")) {
						handshakeComplete = true;
					} else {
						handshakeLines.add(line.trim());
					}

					buffer = new byte[len];
					pos = 0;
				}
			}

			/*for (String line : handshakeLines) {
				System.out.println(line);
			}*/
			handshake.verifyServerStatusLine(handshakeLines.get(0));
			handshakeLines.remove(0);

			HashMap<String, String> headers = new HashMap<String, String>();
			for (String line : handshakeLines) {
				String[] keyValue = line.split(": ", 2);
				headers.put(keyValue[0], keyValue[1]);
			}
			handshake.verifyServerHandshakeHeaders(headers);

			receiver = new WebSocketReceiver(input, this);
			//sender = new WebSocketSender(output, this);
			receiver.start();
			connected = true;
			eventHandler.onOpen();
		} catch (WebSocketException wse) {
			throw wse;
		} catch (IOException ioe) {
			throw new WebSocketException("error while connecting: " + ioe.getMessage(), ioe);
		}
	}

	public synchronized void send(String data) throws WebSocketException
	{
		/*if(sender != null && data != null){
			sender.send(OPCODE_TEXT, true, data.getBytes());
		}*/
		if (!connected) {
			throw new WebSocketException("error while sending text data: not connected");
		}

		try {
			this.sendFrame(OPCODE_TEXT, true, data.getBytes());
		} catch (IOException e) {
			throw new WebSocketException("error while sending text data: " + e.toString());
		}
	}

	private synchronized void sendFrame(byte opcode, boolean masking, byte[] data) throws WebSocketException, IOException
	{
		int headerLength = 2; // This is just an assumed headerLength, as we use a ByteArrayOutputStream
		if (masking) {
			headerLength += 4;
		}
		ByteArrayOutputStream frame = new ByteArrayOutputStream(data.length + headerLength);

		byte fin = (byte) 0x80;
		byte startByte = (byte) (fin | opcode);
		frame.write(startByte);
		int length = data.length;
		int length_field = 0;

		if (length < 126) {
			if (masking) {
				length = 0x80 | length;
			}
			frame.write((byte) length);
		} else if (length <= 65535) {
			length_field = 126;
			if (masking) {
				length_field = 0x80 | length_field;
			}
			frame.write((byte) length_field);
			byte[] lengthBytes = intToByteArray(length);
			frame.write(lengthBytes[2]);
			frame.write(lengthBytes[3]);
		} else {
			length_field = 127;
			if (masking) {
				length_field = 0x80 | length_field;
			}
			frame.write((byte) length_field);
			// Since an integer occupies just 4 bytes we fill the 4 leading length bytes with zero
			frame.write(new byte[]{0x0, 0x0, 0x0, 0x0});
			frame.write(intToByteArray(length));
		}

		byte[] mask = null;
		if (masking) {
			mask = generateMask();
			frame.write(mask);

			for (int i = 0; i < data.length; i++) {
				data[i] ^= mask[i % 4];
			}
		}

		frame.write(data);
		output.write(frame.toByteArray());
		output.flush();
	}

	private byte[] generateMask()
	{
		final byte[] mask = new byte[4];
		random.nextBytes(mask);
		return mask;
	}

	private byte[] intToByteArray(int number)
	{
		byte[] bytes = ByteBuffer.allocate(4).putInt(number).array();
		return bytes;
	}

	public void handleReceiverError()
	{
		try {
			if (connected) {
				close(true);
			}
		} catch (WebSocketException wse) {
			wse.printStackTrace();
		}
	}

	/*public synchronized void close() throws WebSocketException
	{
		if (!connected) {
			return;
		}

		sendCloseHandshake();

		if (receiver.isRunning()) {
			receiver.stopit();
		}

		closeStreams();

		eventHandler.onClose();
	}*/

	public synchronized void close(final boolean isForced) throws WebSocketException {
		if (!connected) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					sendCloseHandshake();

					if (receiver.isRunning()) {
						receiver.stopit();
					}

					closeStreams();

					if(!isForced){
						eventHandler.onClose();
					}else{
						eventHandler.onForcedClose();
					}
				} catch (Exception e) {
					eventHandler.onException(e);
				}
			}
		}).start();
	}

	private synchronized void sendCloseHandshake() throws WebSocketException
	{
		if (!connected) {
			throw new WebSocketException("error while sending close handshake: not connected");
		}

		//System.out.println("Sending close");
		if (!connected) {
			throw new WebSocketException("error while sending close");
		}

		try {
			this.sendFrame(OPCODE_CLOSE, true, new byte[0]);
			//sender.send(OPCODE_CLOSE, true, new byte[0]);
		} catch (WebSocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new WebSocketException("error while sending text data", e);
		}

		connected = false;
	}

	private Socket createSocket() throws WebSocketException, WebSocketProxyException
	{
		String scheme = url.getScheme();
		String host = url.getHost();
		int port = url.getPort();

		Socket socket = null;

		if (scheme != null && scheme.equals("ws")) {
			if (port == -1) {
				port = 80;
			}
			try {
				if(proxy.isDefined()){
					socket = createProxyTunnel(proxy.getHost(), proxy.getPort(), host, port);
				} else {
					socket = new Socket(host, port);
				}
				socket.setKeepAlive(true);
				socket.setSoTimeout(0);
			} catch (UnknownHostException uhe) {
				throw new WebSocketException("unknown host: " + host, uhe);
			} catch (IOException ioe) {
				throw new WebSocketException("error while creating socket to " + url, ioe);
			} catch (Exception e) {
				//Exception from createProxyTunnel
				throw new WebSocketProxyException(e.getMessage());
			}
		} else if (scheme != null && scheme.equals("wss")) {
			if (port == -1) {
				port = 443;
			}
			try {
				SSLSocketFactory factory = SecureWebConnections.getFullTrustSSLFactory();

				if(proxy.isDefined()){
					Socket tunnel = createProxyTunnel(proxy.getHost(), proxy.getPort(), host, port);
					socket = factory.createSocket(tunnel, host, port, true);
				} else {
					socket = factory.createSocket(host, port);
				}
			} catch (UnknownHostException uhe) {
				throw new WebSocketException("unknown host: " + host, uhe);
			} catch (IOException ioe) {
				throw new WebSocketException("error while creating secure socket to " + url, ioe);
			} catch (Exception e) {
				// Exception from createProxyTunnel
				throw new WebSocketProxyException(e.getMessage());
			}
		} else {
			throw new WebSocketException("unsupported protocol: " + scheme);
		}

		return socket;
	}

	private Socket createProxyTunnel(String proxyHost, int proxyPort, String targetHost, int targetPort) throws Exception{
		Socket tunnel = new Socket(proxyHost, proxyPort);
		OutputStream out = tunnel.getOutputStream();
		String msg = "CONNECT " + targetHost + ":" + targetPort + " HTTP/1.1\n"
				+ "User-Agent: "
				+ sun.net.www.protocol.http.HttpURLConnection.userAgent
				+ "\r\n\r\n";
		byte b[];
		try {
            /*
             * We really do want ASCII7 -- the http protocol doesn't change
             * with locale.
             */
			b = msg.getBytes("ASCII7");
		} catch (UnsupportedEncodingException ignored) {
            /*
             * If ASCII7 isn't there, something serious is wrong, but
             * Paranoia Is Good (tm)
             */
			b = msg.getBytes();
		}
		out.write(b);
		out.flush();

        /*
         * We need to store the reply so we can create a detailed
         * error message to the user.
         */
		byte            reply[] = new byte[200];
		int             replyLen = 0;
		int             newlinesSeen = 0;
		boolean         headerDone = false;     /* Done on first newline */

		InputStream     in = tunnel.getInputStream();

		while (newlinesSeen < 2) {
			int i = in.read();
			if (i < 0) {
				throw new Exception("Unexpected EOF from proxy");
			}
			if (i == '\n') {
				headerDone = true;
				++newlinesSeen;
			} else if (i != '\r') {
				newlinesSeen = 0;
				if (!headerDone && replyLen < reply.length) {
					reply[replyLen++] = (byte) i;
				}
			}
		}

        /*
         * Converting the byte array to a string is slightly wasteful
         * in the case where the connection was successful, but it's
         * insignificant compared to the network overhead.
         */
		String replyStr;
		try {
			replyStr = new String(reply, 0, replyLen, "ASCII7");
		} catch (UnsupportedEncodingException ignored) {
			replyStr = new String(reply, 0, replyLen);
		}

        /* We asked for HTTP/1.0, so we should get that back */
		if (!replyStr.startsWith("HTTP/1.0 200")) {
			throw new Exception("Unable to tunnel through. Proxy returns \"" + replyStr + "\"");
		}

        /* tunneling Handshake was successful! */
		return tunnel;
	}

	private void closeStreams() throws WebSocketException
	{
		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException ioe) {
			throw new WebSocketException("error while closing websocket connection: ", ioe);
		}
	}

	public static int getVersion()
	{
		return VERSION;
	}

	public boolean isConnected() {
		return connected;
	}

}
