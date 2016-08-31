package ibt.ortc.extensibility;

/**
 * Created by joaocaixinha on 31/08/16.
 */
public interface OnMessageWithFilter {
    /**
     * Fired when a message was received in the specified channel
     * @param sender Ortc client instance that fired the event
     * @param channel Channel where the message was received
     * @param filtered Specifies if server as filtered the message
     * @param message Content of the received message
     */
    public void run(OrtcClient sender, String channel, boolean filtered, String message);
}
