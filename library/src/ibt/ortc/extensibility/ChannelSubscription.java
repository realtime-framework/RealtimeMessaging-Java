/**
 * @fileoverview This file contains the class to create ortc factories
 * @author ORTC team members (ortc@ibt.pt) 
 */
package ibt.ortc.extensibility;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents a channel subscription
 *  
 * How to use:
 * <pre>
 * subscribedChannel = new ChannelSubscription(subscribeOnReconnect, onMessageEventHandler);
 * </pre>
 * @version 2.1.0 27 Mar 2013
 * @author IBT
 * 
 */
public class ChannelSubscription {
	private boolean isSubscribing;
	private boolean isSubscribed;
	private boolean subscribeOnReconnected;
	private OnMessage onMessage;
	
	/**
	 * Creates an instance of a channel subscription
	 * @param subscribeOnReconnected Indicates if the channel should be subscribe if a reconnect happens
	 * @param onMessageT Event handler that is fired when a message is received in the channel
	 */
	public <T> ChannelSubscription(boolean subscribeOnReconnected,T onMessageT){
		this.subscribeOnReconnected = subscribeOnReconnected;

		if(onMessageT instanceof OnMessage){
			this.onMessage = (OnMessage) onMessageT;
		}

		this.isSubscribed = false;
		this.isSubscribing = false;
	}
	
	/**
	 * Indicates whether the channel is being subscribed or not
	 * @return boolean True if is subscribing the channel otherwise false
	 */
	public boolean isSubscribing() {
		return isSubscribing;
	}

	/**
	 * Changes the channel subscribing status
	 * @param isSubscribing True indicates the channel is being subscribed
	 */
	public void setSubscribing(boolean isSubscribing) {
		this.isSubscribing = isSubscribing;
	}

	/**
	 * Indicates whether the channel is subscribed or not
	 * @return boolean True if the channel is subscribed otherwise false
	 */
	public boolean isSubscribed() {
		return isSubscribed;
	}

	/**
	 * Changes the channel subscribed status
	 * @param isSubscribed True indicates the channel is being subscribed
	 */
	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	/**
	 * Fires the event handler that is associated the subscribed channel (with payload)
	 * @param channel 
	 * @param sender
	 * @param message
	 */
	public void runHandler(OrtcClient sender, String channel, String message){
			this.onMessage.run(sender, channel, message);
	}
	
	/**
	 * Indicates where the channel should be subscribed if a reconnect happens
	 * @return boolean True if should be subscribed otherwise false
	 */
	public boolean subscribeOnReconnected() {
		return subscribeOnReconnected;
	}
	
	/**
	 * Indicates where the channel should be subscribed if a reconnect happens
	 */
	public void setSubscribeOnReconnected(boolean value) {
		subscribeOnReconnected = value;
	}

}
