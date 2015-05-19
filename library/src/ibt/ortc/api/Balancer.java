/**
 * @fileoverview This file contains the static class balancer
 * @author ORTC team members (ortc@ibt.pt) 
 */
package ibt.ortc.api;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * A static class containing all the methods to communicate with the Ortc
 * Balancer
 * 
 * How to use:
 * 
 * <pre>
 * String balancerUrl = &quot;http://developers2.realtime.livehtml.net/server/2.1/&quot;;
 * String connectionUrl = Balancer.getServerFromBalancer(balancerUrl);
 * </pre>
 * 
 * @version 2.1.0 27 Mar 2013
 * @author IBT
 * 
 */
public class Balancer{
	private static final String BALANCER_SERVER_PATTERN = "^var SOCKET_SERVER = \"(http.*)\";$";
	//private static final String BALANCER_NO_SERVER_PATTERN = ".*No server available.*";
	private static final String URL_PROTOCOL_PATTERN = "^(http(s)?).*$";

	private static final Pattern balancerServerPattern = Pattern.compile(BALANCER_SERVER_PATTERN);
	//private static final Pattern balancerNoServerPattern = Pattern.compile(BALANCER_NO_SERVER_PATTERN);
	private static final Pattern urlProtocolPattern = Pattern.compile(URL_PROTOCOL_PATTERN);

	private static final String UserAgent = "ortc-server-side-api";

	protected static String getServerUrl(String url,Boolean isCluster,String applicationKey, Proxy proxy){
		// CAUSE: Unused assignment
		String result;

		if (!Strings.isNullOrEmpty(url) && isCluster)
		{
			try {
				result = getServerFromBalancer(url, applicationKey, proxy);
				// CAUSE: Prefer throwing/catching meaningful exceptions instead of Exception
			} catch (MalformedURLException ex) {
				result = null;
			} catch (IOException ex) {
				result = null;
			} catch (InvalidBalancerServerException ex) {
				result = null;
			}
		}
		else
		{
			result = url;
		}

		return result;
	}

	/**
	 * Retrieves an Ortc Server url from the Ortc Balancer
	 * 
	 * @param balancerUrl
	 *            The Ortc Balancer url
	 * @return An Ortc Server url
	 * @throws java.io.IOException
	 * @throws UnknownHostException 
	 * @throws InvalidBalancerServerException
	 */
	public static String getServerFromBalancer(String balancerUrl,String applicationKey, Proxy proxy) throws IOException, InvalidBalancerServerException {
		Matcher protocolMatcher = urlProtocolPattern.matcher(balancerUrl);

		String protocol = protocolMatcher.matches() ? "" : protocolMatcher.group(1);

		String parsedUrl = String.format("%s%s", protocol, balancerUrl);

		if(!Strings.isNullOrEmpty(applicationKey)){
			// CAUSE: Prefer String.format to +
			parsedUrl += String.format("?appkey=%s", applicationKey);
		}

		URL url = new URL(parsedUrl);

		// CAUSE: Unused assignment
		String clusterServer;

		clusterServer = unsecureRequest(url, proxy); //"https".equals(protocolMatcher.group(1)) ? mkSecureRequest(url, proxy) : unsecureRequest(url, proxy);

		return clusterServer;
	}

	private static String unsecureRequest(URL url, Proxy proxy) throws InvalidBalancerServerException, IOException  {
		HttpURLConnection connection = null;
		String result = "";

		try {
			if(proxy.isDefined()){
				java.net.Proxy jnp = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
				connection = (HttpURLConnection) url.openConnection(jnp);
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}
			connection.setReadTimeout(1000*15);
			connection.setRequestProperty("user-agent", UserAgent);

			BufferedReader rd = null;
			try {
				// CAUSE: Reliance on default encoding
				rd = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
				// TODO: specify a correct capacity
				StringBuilder lResult = new StringBuilder(16);

				String line = rd.readLine();
				// CAUSE: Assignment expressions nested inside other expressions
				while (line != null) {
					// CAUSE: Method concatenates strings using + in a loop
					lResult.append(line);
					line = rd.readLine();
				}

				Matcher matcher = balancerServerPattern.matcher(lResult);
				if (!matcher.matches()) {
					throw new InvalidBalancerServerException(lResult.toString());
				}
				result = matcher.group(1);
				// CAUSE: Method may fail to close stream on exception
			} catch (Exception ex){
				throw new InvalidBalancerServerException(ex.getMessage());
			} finally {
				if (rd != null) {
					rd.close();
				}
			}
			// CAUSE: Method may fail to close connection on exception
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return result;
	}

	private static String mkSecureRequest(URL url, Proxy proxy) throws IOException, InvalidBalancerServerException{
		HttpsURLConnection.setDefaultSSLSocketFactory(SecureWebConnections.getFullTrustSSLFactory());
		String result = null;
		URLConnection conn;
		try{
			if(proxy.isDefined()){
				java.net.Proxy jnp = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxy.getHost(), proxy.getPort()));
				conn = (HttpsURLConnection) url.openConnection(jnp);
			} else {
				conn = (HttpsURLConnection) url.openConnection();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder lResult = new StringBuilder(16);
			String line;
			while ((line = reader.readLine()) != null) {
				lResult.append(line);
			}
			Matcher matcher = balancerServerPattern.matcher(lResult);
			if (!matcher.matches()) {
				throw new InvalidBalancerServerException(lResult.toString());
			}
			result = matcher.group(1);
		} catch (Exception ex){
			throw new InvalidBalancerServerException(ex.getMessage());
		}
		return result;
	}


	// CAUSE: Utility class contains only static elements and is still instantiable
	private Balancer() {
	}
}
