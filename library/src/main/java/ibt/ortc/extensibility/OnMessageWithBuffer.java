package ibt.ortc.extensibility;


public interface OnMessageWithBuffer {
    /**
     * Fired when a message was received in the specified channel
     * @param sender Ortc client instance that fired the event
     * @param channel The channel where the message was received
     * @param seqId The message sequence id
     * @param message The message received
     */

    public void run(OrtcClient sender, String channel, String seqId, String message);
}
