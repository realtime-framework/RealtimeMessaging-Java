package ibt.ortc.util;

/**
 * Base64 encoder
 */
public class Base64 {

    private static final int MASK_6 = 0x3F;
    private static final int MASK_4 = 0xF;
    private static final int MASK_2 = 0x3;

    private static final String CHAR_TABLE          = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";

    /**
     * Encodes the given data to a base64 string.
     *
     * @param data a valid byte[], must not be null.
     * @return a valid <code>String</code> instance representing the base64 encoding of the given data.
     */
    @SuppressWarnings("AssignmentToForLoopParameter")
    public static String encode(final byte[] data) {
        final int len = data.length;
        final StringBuilder result = new StringBuilder((len / 3 + 1) * 4);
        int bte;
        int index;

        for (int i = 0; i < len; i++) {

            bte = data[i];

            // First 6 bits
            index = bte >> 2 & MASK_6;
            result.append(CHAR_TABLE.charAt(index));

            // Last 2 bits plus 4 from next byte
            index = bte << 4 & MASK_6;

            if (++i < len) {
                bte = data[i];
                index |= bte >> 4 & MASK_4;
            }
            result.append(CHAR_TABLE.charAt(index));

            // 4 + 2 from next
            if (i < len) {
                index = bte << 2 & MASK_6;
                if (++i < len) {
                    bte = data[i];
                    index |= bte >> 6 & MASK_2;
                }
                result.append(CHAR_TABLE.charAt(index));
            } else {
                i++;
                result.append(CHAR_TABLE.charAt(64));
            }
            if (i < len) {
                index = bte & MASK_6;
                result.append(CHAR_TABLE.charAt(index));
            } else {
                result.append(CHAR_TABLE.charAt(64));
            }
        }

        return result.toString();
    }

    private Base64() {
    }

}
