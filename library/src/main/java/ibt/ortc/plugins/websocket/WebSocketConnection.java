package ibt.ortc.plugins.websocket;

import ibt.ortc.api.Proxy;
import ibt.ortc.api.SecureWebConnections;
import ibt.ortc.api.Strings;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class WebSocketConnection implements WebSocket {
  
    private URI url = null;
    private WebSocketEventHandler eventHandler = null;

    private volatile boolean connected = false;

    private Socket socket = null;
    private DataInputStream input = null;
    private PrintStream output = null;

    private WebSocketReceiver receiver = null;
    private WebSocketHandshake handshake = null;
    
    private Proxy proxy = null;

    // CAUSE: Reported exception is not thrown
    public WebSocketConnection(URI url, Proxy proxy) {
        this(url, proxy, null);
    }

    // CAUSE: Reported exception is not thrown
    public WebSocketConnection(URI url, Proxy proxy, String protocol) {
        this.url = url;
        this.proxy = proxy;
        handshake = new WebSocketHandshake(url, protocol);
    }

    @Override
    public void setEventHandler(WebSocketEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public WebSocketEventHandler getEventHandler() {
        return this.eventHandler;
    }

    @Override
    public void connect() throws WebSocketException, WebSocketProxyException {
        try {
            if (connected) {
                throw new WebSocketException("already connected");
            }

            socket = createSocket();
            input = new DataInputStream(socket.getInputStream());
            output = new PrintStream(socket.getOutputStream(), true, "UTF-8");          
            
            output.write(handshake.getHandshake());

            boolean handshakeComplete = false;
            boolean header = true;
            int len = 1000;
            byte[] buffer = new byte[len];
            int pos = 0;
            // CAUSE: Instantiating collection without specified initial capacity
            ArrayList<String> handshakeLines = new ArrayList<String>(10);

            // CAUSE: Not used

            while (!handshakeComplete) {
                int b = input.read();
                buffer[pos] = (byte) b;
                pos += 1;

                if (!header) {
                    if (b == -1) {
                        //in my case, the proxy closes the connection after the handshake when using unsecure websocket
                        //using secure websocket works fine / is allowed
                        throw new IOException("Connection closed by remote (if using proxy, it probably doesn't allow proxying unsecure websockets, try secure connection");
                    }
                    // CAUSE: Not used
                    if (pos == 16) {
                        handshakeComplete = true;
                    }
                } else if (buffer[pos - 1] == 0x0A && buffer[pos - 2] == 0x0D) {
                    String line = new String(buffer, "UTF-8");
                    if (Strings.isNullOrEmpty(line)) {
                        header = false;
                    } else {
                        handshakeLines.add(line.trim());
                    }

                    buffer = new byte[len];
                    pos = 0;
                }
            }

            handshake.verifyServerStatusLine(handshakeLines.get(0));

            handshakeLines.remove(0);

            // CAUSE: Instantiating collection without specified initial capacity
            HashMap<String, String> headers = new HashMap<String, String>(16);
            for (String line : handshakeLines) {
                String[] keyValue = line.split(": ", 2);
                headers.put(keyValue[0], keyValue[1]);
            }
            handshake.verifyServerHandshakeHeaders(headers);

            receiver = new WebSocketReceiver(input, this);
            receiver.start();
            connected = true;
            eventHandler.onOpen();
        // CAUSE: Prefer throwing/catching meaningful exceptions instead of Exception
        } catch (IOException ioe) {
            //ioe.printStackTrace();
            // CAUSE: Prefer String.format to +
            throw new WebSocketException(String.format("error while connecting: %s", ioe.getMessage()), ioe);
        }
    }

    @Override
    public synchronized void send(String data) throws WebSocketException, WebSocketStreamException {
        if (!connected) {
            throw new WebSocketException(
                    "error while sending text data: not connected");
        }

        try {            
            if(output.checkError() || !socket.isBound() || socket.isClosed() || !socket.isConnected() || socket.isInputShutdown() || socket.isOutputShutdown()){
              throw new WebSocketStreamException("error while sending text data");
            }
            output.write(0x00);
            output.write(data.getBytes(("UTF-8")));
            output.write(0xff);
        } catch (IOException ioe) {
            throw new WebSocketException("error while sending text data", ioe);
        }
    }

    public void handleReceiverError() {
        try {
            if (connected) {
                close();
            }
        } catch (WebSocketException wse) {
            // CAUSE: Thrown exception is hidden
            //wse.printStackTrace();
        }
    }

    @Override
    public synchronized void close() throws WebSocketException {
        if (!connected) {
            return;
        }

        sendCloseHandshake();

        if (receiver.isRunning()) {
            receiver.stopit();
            // CAUSE: Not used
        }

        closeStreams();
        eventHandler.onClose();
    }

    private synchronized void sendCloseHandshake() throws WebSocketException {
        if (!connected) {
            throw new WebSocketException(
                    "error while sending close handshake: not connected");
        }

        try {
            if(output.checkError()){
              throw new IOException();
            }
            output.write(0xff00);
            output.write("%n".getBytes("UTF-8"));
        } catch (IOException ioe) {
            throw new WebSocketException("error while sending close handshake",ioe);
        }

        connected = false;
    }

    private Socket createSocket() throws WebSocketException, WebSocketProxyException {
        String scheme = url.getScheme();
        String host = url.getHost();
        int port = url.getPort();

        Socket lSocket = null;

        if (scheme != null && scheme.equals("ws")) {
            if (port == -1) {
                port = 80;
            }
            try {

            	if (proxy != null) {
            		lSocket = createProxyTunnel(proxy, host, port);
            	} else {
            		lSocket = new Socket(host, port);
            	}
            	
                lSocket.setKeepAlive(true);
                lSocket.setSoTimeout(0);
			} catch (UnknownHostException uhe) {
                // uhe.printStackTrace();
                // CAUSE: Prefer String.format to +
                throw new WebSocketException(String.format("unknown host: %s", host), uhe);
            } catch (IOException ioe) {
                // ioe.printStackTrace();
                // CAUSE: Prefer String.format to +
                throw new WebSocketException(String.format("error while creating socket to %s", url), ioe);
            } catch (Exception e) {
            	// Exception from createProxyTunnel
            	throw new WebSocketProxyException(e.getMessage());
			}
        } else if (scheme != null && scheme.equals("wss")) {
            if (port == -1) {
                port = 443;
            }
            try {        	
                SSLSocketFactory factory = SecureWebConnections.getFullTrustSSLFactory();

                if (proxy != null) {
                	Socket tunnel = createProxyTunnel(proxy, host, port);
                	lSocket = factory.createSocket(tunnel, host, port, true);                	
                } else {
                	lSocket = factory.createSocket(host, port);
                }
                lSocket.setKeepAlive(true);
                lSocket.setSoTimeout(0);

                String[] protocols = { "TLSv1" };
                ((SSLSocket) lSocket).setEnabledProtocols(protocols);
                // CAUSE: Prefer throwing/catching meaningful exceptions instead of Exception
            } catch (IOException ioe) {
                // ioe.printStackTrace();
                throw new WebSocketException(
                        // CAUSE: Prefer String.format to +
                        String.format("error while creating secure socket to %s", url), ioe);
            } catch (Exception e) {
				// Exception from createProxyTunnel
            	throw new WebSocketProxyException(e.getMessage());
			} 
        } else {
            // CAUSE: Prefer String.format to +
            throw new WebSocketException(String.format("unsupported protocol: %s", scheme));
        }

        return lSocket;
    }

    private Socket createProxyTunnel(Proxy proxy, String targetHost, int targetPort) throws Exception{
    	Socket tunnel = new Socket(proxy.getHost(), proxy.getPort());
    	OutputStream out = tunnel.getOutputStream();
    	String msg = "CONNECT " + targetHost + ":" + targetPort + " HTTP/1.1\n"
                + (proxy.getProxyAuth() != null ? "Proxy-Authorization: Basic " + proxy.getProxyAuth() + "\n" : "")
                + "Connection: Keep-alive\n"
                + "Proxy-Connection: Keep-alive\n"
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

        if (!replyStr.contains("200")) {
            throw new Exception("Unable to tunnel through. Proxy returns \"" + replyStr + "\"");
        }

        /* tunneling Handshake was successful! */
        return tunnel;
    }
    
    private void closeStreams() throws WebSocketException {
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException ioe) {
            // ioe.printStackTrace();
            throw new WebSocketException(
                    "error while closing websocket connection: ", ioe);
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

}