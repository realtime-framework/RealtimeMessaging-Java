package ibt.ortc.api;

import org.apache.commons.codec.binary.Base64;

/**
 * Class containing definition of proxy connection
 */
public class Proxy {
	private String host;
	private int port;
	private String proxyAuth;

	/**
	 * Creates an instance with proxy url and port defined
	 *  @param host Proxy host
	 * @param port Proxy port
	 * @param user
	 * @param pwd
	 */
	public Proxy(String host, int port, String user, String pwd){
		this.host = host;
		this.port = port;
		if (user != null && pwd != null) {
			proxyAuth = Base64.encodeBase64String((user + ":" + pwd).getBytes());
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

	public String getProxyAuth() {
		return proxyAuth;
	}
}
