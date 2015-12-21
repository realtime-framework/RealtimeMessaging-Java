/*
 *  Copyright (C) 2011 Roderick Baier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ibt.ortc.plugins.websocket;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;

public class WebSocketHandshake {
    private String key1 = null;
    private String key2 = null;
    private byte[] key3 = null;
    private byte[] expectedServerResponse = null;

    private URI url = null;
    private String origin = null;
    // CAUSE: Not used

    public WebSocketHandshake(URI url, String protocol) {
        this.url = url;
        // CAUSE: Not used
        generateKeys();
    }

    public byte[] getHandshake() throws UnsupportedEncodingException {
        String path = url.getPath();
        String host = url.getHost();
        // CAUSE: Prefer String.format to +
        origin = String.format("http://%s", host);

        // CAUSE: Prefer String.format to +
        String handshake = String.format("GET %s HTTP/1.1%nHost: %s%nConnection: Upgrade%nConnection: keep-alive%nSec-WebSocket-Key2: %s%n", path, host, key2);
        //String handshake = String.format("CONNECT ortc-developers2-euwest1-S0001.realtime.co:443 HTTP/1.1%nHost: %s%nConnection: Upgrade%nSec-WebSocket-Key2: %s%n", host, host, key2);
        
        // CAUSE: Prefer String.format to +
        handshake += String.format("Upgrade: WebSocket%nSec-WebSocket-Key1: %s%nOrigin: %s%n%n", key1, origin);

        // CAUSE: Reliance on default encoding
        byte[] handshakeBytes = new byte[handshake.getBytes("UTF-8").length + 8];

        // CAUSE: Reliance on default encoding
        System.arraycopy(handshake.getBytes("UTF-8"), 0, handshakeBytes, 0, handshake.getBytes("UTF-8").length);
        //System.arraycopy(key3, 0, handshakeBytes, handshake.getBytes().length, 8);

        return handshakeBytes;
    }

    public void verifyServerResponse(byte[] bytes) throws WebSocketException {
        if (!Arrays.equals(bytes, expectedServerResponse)) {
            throw new WebSocketException("not a WebSocket Server");
        }
    }

    public void verifyServerStatusLine(String statusLine) throws WebSocketException {
        int statusCode = Integer.valueOf(statusLine.substring(9, 12));

        if (statusCode == 407) {
            throw new WebSocketException("connection failed: proxy authentication not supported");
        } else if (statusCode == 404) {
            throw new WebSocketException("connection failed: 404 not found");
        } else if (statusCode != 101) {
            // CAUSE: Prefer String.format to +
            throw new WebSocketException(String.format("connection failed: unknown status code %s", statusCode));
        }
    }

    public void verifyServerHandshakeHeaders(HashMap<String, String> headers) throws WebSocketException {
        if (!headers.get("Upgrade").equals("WebSocket")) {
            throw new WebSocketException("connection failed: missing header field in server handshake: Upgrade");
        } else if (!headers.get("Connection").equals("Upgrade")) {
            throw new WebSocketException("connection failed: missing header field in server handshake: Connection");
        } else if (!headers.get("Sec-WebSocket-Origin").equals(origin)) {
            throw new WebSocketException("connection failed: missing header field in server handshake: Sec-WebSocket-Origin");
        }
        // TODO see 4.1. step 41
        // else if
        // (!headers.get("Sec-WebSocket-Location").equals(url.toASCIIString()))
        // {
        // System.out.println("location: " + url.toASCIIString());
        // }
        // else if protocol
    }

    private void generateKeys() {
        int spaces1 = rand(1, 12);
        int spaces2 = rand(1, 12);

        int max1 = Integer.MAX_VALUE / spaces1;
        int max2 = Integer.MAX_VALUE / spaces2;

        int number1 = rand(0, max1);
        int number2 = rand(0, max2);

        int product1 = number1 * spaces1;
        int product2 = number2 * spaces2;

        key1 = Integer.toString(product1);
        key2 = Integer.toString(product2);

        key1 = insertRandomCharacters(key1);
        key2 = insertRandomCharacters(key2);

        key1 = insertSpaces(key1, spaces1);
        key2 = insertSpaces(key2, spaces2);

        key3 = createRandomBytes();

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(number1);
        byte[] number1Array = buffer.array();
        buffer = ByteBuffer.allocate(4);
        buffer.putInt(number2);
        byte[] number2Array = buffer.array();

        byte[] challenge = new byte[16];
        System.arraycopy(number1Array, 0, challenge, 0, 4);
        System.arraycopy(number2Array, 0, challenge, 4, 4);
        System.arraycopy(key3, 0, challenge, 8, 8);

        expectedServerResponse = md5(challenge);
    }

    private String insertRandomCharacters(String key) {
        int count = rand(1, 12);
        // CAUSE: Assignment to method parameter
        String lKey = key;

        char[] randomChars = new char[count];
        int randCount = 0;
        while (randCount < count) {
            int rand = (int) (Math.random() * 0x7e + 0x21);
            if (((0x21 < rand) && (rand < 0x2f)) || ((0x3a < rand) && (rand < 0x7e))) {
                randomChars[randCount] = (char) rand;
                randCount += 1;
            }
        }

        for (int i = 0; i < count; i++) {
            int split = rand(0, lKey.length());
            String part1 = lKey.substring(0, split);
            String part2 = lKey.substring(split);
            lKey = part1 + randomChars[i] + part2;
        }

        return lKey;
    }

    private String insertSpaces(String key, int spaces) {
        // CAUSE: Assignment to method parameter
        String lKey = key;
        for (int i = 0; i < spaces; i++) {
            int split = rand(1, lKey.length() - 1);
            String part1 = lKey.substring(0, split);
            String part2 = lKey.substring(split);
            // CAUSE: Prefer String.format to +
            lKey = String.format("%s %s", part1, part2);
        }

        return lKey;
    }

    private byte[] createRandomBytes() {
        byte[] bytes = new byte[8];

        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) rand(0, 255);
        }

        return bytes;
    }

    private byte[] md5(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            // CAUSE: Thrown exception is hidden
            //e.printStackTrace();
            // TODO: Consider returning a zero length array rather than null?
            return new byte[0];
        }
    }

    private int rand(int min, int max) {
        int rand = (int) (Math.random() * max + min);
        return rand;
    }
}
