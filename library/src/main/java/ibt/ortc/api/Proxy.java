package ibt.ortc.api;

/**
 * Class containing definition of proxy connection
 */
public class Proxy {	
	private boolean useProxy;
	private String host;
	private int port;
	
	/**
	 * Creates an empty instance
	 */
	public Proxy(){
		this.useProxy = false;
		this.host = null;
		this.port = -1;
	}
	
	/**
	 * Creates an instance with proxy url and port defined
	 * 
	 * @param host Proxy host
	 * @param port Proxy port
	 */
	public Proxy(String host, int port){
		this.useProxy = true;
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Verify if instance is not empty
	 * 
	 * @return false if instance is empty
	 */
	public boolean isDefined(){
		return this.useProxy;
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
}
