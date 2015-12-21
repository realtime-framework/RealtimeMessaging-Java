/**
 * @fileoverview This file contains the static class balancer
 * @author ORTC team members (ortc@ibt.pt) 
 */
package ibt.ortc.api;

import ibt.ortc.util.IOUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		String balancer = IOUtil.doGetRequest(url, proxy);
		Matcher matcher = balancerServerPattern.matcher(balancer);
		if (!matcher.matches()) {
			throw new InvalidBalancerServerException(balancer);
		}
		return matcher.group(1);
	}

	// CAUSE: Utility class contains only static elements and is still instantiable
	private Balancer() {
	}
}
