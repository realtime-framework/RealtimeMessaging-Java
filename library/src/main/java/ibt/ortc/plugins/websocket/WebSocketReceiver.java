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

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebSocketReceiver
        extends Thread
{
    private DataInputStream input = null;
    private WebSocketConnection websocket = null;
    private WebSocketEventHandler eventHandler = null;

    private volatile boolean stop = false;

    public WebSocketReceiver(DataInputStream input, WebSocketConnection websocket)
    {
        this.input = input;
        this.websocket = websocket;
        this.eventHandler = websocket.getEventHandler();
    }

    @Override
    public void run()
    {
        boolean frameStart = false;
        // CAUSE: Instantiating collection without specified initial capacity
        List<Byte> messageBytes = new ArrayList<Byte>(10);

        while (!stop) {
            try {
                int b = input.readUnsignedByte();
                if (b == 0x00) {
                    frameStart = true;
                }
                else if (b == 0xff && frameStart == true) {
                    frameStart = false;
                    Byte[] message = messageBytes.toArray(new Byte[messageBytes.size()]);
                    eventHandler.onMessage(new WebSocketMessage(message));
                    messageBytes.clear();
                }
                else if (b == -1) {
                    frameStart = false;
                    messageBytes.clear();
                    handleError();
                }
                else if (frameStart == true){
                    messageBytes.add((byte)b);
                }
            }
            catch (IOException ioe) {
                frameStart = false;
                messageBytes.clear();
                handleError();
            }
        }
    }


    public void stopit()
    {
        stop = true;
    }


    public boolean isRunning()
    {
        return !stop;
    }


    private void handleError()
    {
        stopit();
        websocket.handleReceiverError();
    }
}
