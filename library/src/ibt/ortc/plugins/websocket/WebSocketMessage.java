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


public class WebSocketMessage
{
    private Byte[] message;


    public WebSocketMessage(final Byte[] message)
    {
        // CAUSE: May expose internal representation by incorporating reference to mutable object
        this.message = message.clone();
    }


    public String getText()
    {
        // CAUSE: Local variable hides a field
        byte[] lMessage = new byte[this.message.length];
        for (int i = 0; i < this.message.length; i++) {
            lMessage[i] = this.message[i];
        }
        try {
            return new String(lMessage, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return null;
        }
    }
}
