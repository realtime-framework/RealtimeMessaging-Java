package ibt.ortc.api;

import ibt.ortc.util.Base64;

/**
 * Class containing definition of proxy connection
 */
public class Proxy {
	private String host;
	private int port;
	private String proxyAuth;

	/**
	 * Creates an instance with proxy url and port defined
	 *
	 *  @param host Proxy host
	 * @param port Proxy port
	 */
	public Proxy(String host, int port){
		this(host, port, null, null);
	}
	
	/**
	 * Creates proxy config using given host, port and optional credentials
	 *
	 * @param host	proxy host
	 * @param port	proxy port
	 * @param user	proxy user or null if no authentication should be used
	 * @param pwd	proxy password or null if no authentication should be used
	 */
	public Proxy(String host, int port, String user, String pwd){
		this.host = host;
		this.port = port;
		if (user != null && pwd != null) {
			proxyAuth = Base64.encode((user + ":" + pwd).getBytes());
		}
	}

	/**
	 * Obtain defined proxy host
	 * 
	 * @return porxy host
	 */
	public String getHost(){
		return this.host;
	}
	/**
	 * Obtain defined proxy port
	 * 
	 * @return porxy port
	 */
	public int getPort(){
		return this.port;
	}

	/**
	 * returns the encoded password hash according to basic authentication scheme "user:password"
	 *
	 * @return	encoded password hash or null if no authentication is used
	 */
	public String getProxyAuth() {
		return proxyAuth;
	}
}
