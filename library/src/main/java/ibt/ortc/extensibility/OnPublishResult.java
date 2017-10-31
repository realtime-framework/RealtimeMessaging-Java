package ibt.ortc.extensibility;

public interface OnPublishResult {
    /**
     * Fired when a message seqId arrives from the server or publish timeout expires
     * @param error Message not publish, error description
     * @param seqId The message sequence identifier
     */

    public void run(String error, String seqId);
}
