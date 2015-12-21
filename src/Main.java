/**
 * @fileoverview This file contains the integration tests for the Ortc Api
 * @author ORTC team members (ortc@ibt.pt) 
 */

import ibt.ortc.api.*;
import ibt.ortc.extensibility.*;

import java.awt.*;
import java.util.*;

public class Main {

    private static final String defaultServerUrl = "http://ortc-developers.realtime.co/server/2.1";
    private static final boolean defaultIsBalancer = true;
    private static final String defaultApplicationKey = "YOUR_APPLICATION_KEY";
    private static final String defaultPrivateKey = "YOUR_PRIVATE_KEY";
    private static final String defaultAuthenticationToken = "RealtimeDemo";
    private static final boolean defaultNeedsAuthentication = false;
    private static String serverUrl;
    private static boolean isBalancer;
    private static String applicationKey;
    private static String authenticationToken;
    private static Proxy proxy;

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Welcome to Realtime Demo");

        readConnectionInfo();

        try {
            connectOrtcClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readInput() {
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    private static Integer readCommandIndex() {
        Scanner in = new Scanner(System.in);
        return in.nextInt();
    }

    private static void readConnectionInfo() {
        System.out.print("Insert the server URL or press ENTER to use default (" + defaultServerUrl + "):");

        serverUrl = readInput();

        if (Strings.isNullOrEmpty(serverUrl)) {
            serverUrl = defaultServerUrl;
            System.out.println("Using default server URL: " + serverUrl);
        }

        System.out.print("Is it a balancer? Press y/n or ENTER to use default (" + (defaultIsBalancer ? "yes" : "no") + "):");

        String readIsBalancer = readInput();

        if (Strings.isNullOrEmpty(readIsBalancer)) {
            isBalancer = defaultIsBalancer;
            System.out.println("Using default is balancer: " + (isBalancer ? "yes" : "no"));
        }
        else {
            isBalancer = readIsBalancer.equals("y");

            System.out.println((isBalancer ? "Yes" : "No"));
        }

        System.out.print("Insert the application key or press ENTER to use default (" + defaultApplicationKey + "):");

        applicationKey = readInput();

        if (Strings.isNullOrEmpty(applicationKey)) {
            applicationKey = defaultApplicationKey;
            System.out.println("Using default application key: " + applicationKey);
        }

        System.out.print("Insert the authentication token or press ENTER to use default (" + defaultAuthenticationToken + "):");

        authenticationToken = readInput();

        if (Strings.isNullOrEmpty(authenticationToken)) {
            authenticationToken = defaultAuthenticationToken;
            System.out.println("Using default authentication token: " + authenticationToken);
        }
    }

    private static void interfaceMenu() {
        System.out.println("========== Ortc Integration Test Menu ==========");
        System.out.println("");
        System.out.println(" Commands List:");
        System.out.println("");
        System.out.println(" 0 - Connect");
        System.out.println(" 1 - Subscribe to channel");
        System.out.println(" 2 - Unsubscribe from channel");
        System.out.println(" 3 - Send message to channel");
        System.out.println(" 4 - Disconnect ");
        System.out.println(" 5 - Presence ");
        System.out.println(" 6 - Enable Presence ");
        System.out.println(" 7 - Disable Presence ");
        System.out.println("");
        System.out.println("========== Ortc Integration Test Menu ==========");
    }

    private static void readMenuCommand(OrtcClient client) {
        System.out.println("Enter command 0/1/2/3/4:");

        int command = readCommandIndex();

        Scanner in = new Scanner(System.in);

        String channel = "";
        String message = "";

        switch (command) {
            case 0:
                System.out.println("Connecting...");
                client.connect(applicationKey, authenticationToken);
                break;
            case 1:
                System.out.println("channel:");
                channel = in.nextLine();
                System.out.println("Subscribing to " + channel + "...");
                subscribeChannel(client, channel);
                break;
            case 2:
                System.out.println("channel:");
                channel = in.nextLine();
                System.out.println("Unsubscribing from " + channel + "...");
                client.unsubscribe(channel);
                break;
            case 3:
                System.out.println("channel:");
                channel = in.nextLine();
                System.out.println("message:");
                message = in.nextLine();
                System.out.println("Sending " + message + " to " + channel + "...");
                client.send(channel, message);
                readMenuCommand(client);
                break;
            case 4:
                System.out.println("Disconnecting...");
                client.disconnect();
                break;
            case 5:
                System.out.println("channel:");
                channel = in.nextLine();
                getPresence(channel);
                readMenuCommand(client);
                break;
            case 6:
                System.out.println("channel:");
                channel = in.nextLine();
                System.out.println("metadata:");
                Boolean metadata = Boolean.parseBoolean(in.nextLine());
                enablePresence(channel,metadata);
                readMenuCommand(client);
                break;
            case 7:
                System.out.println("channel:");
                channel = in.nextLine();
                disablePresence(channel);
                readMenuCommand(client);
                break;
            default:
                System.out.println("Invalid command");
                readMenuCommand(client);
                break;
        }
    }

    private static void getPresence(String channel){
        Ortc.presence(serverUrl, isBalancer, applicationKey, authenticationToken, channel, proxy, new OnPresence() {

            @Override
            public void run(Exception error, Presence presence) {
                if(error != null){
                    System.out.println(error.getMessage());
                }else{
                    System.out.println("Subscriptions - " + presence.getSubscriptions());

                    Iterator<?> metadataIterator = presence.getMetadata().entrySet().iterator();
                    while(metadataIterator.hasNext()){
                        @SuppressWarnings("unchecked")
                        Map.Entry<String, Long> entry = (Map.Entry<String, Long>) metadataIterator.next();
                        System.out.println(entry.getKey() + " - " + entry.getValue());
                    }
                }
            }
        });
    }

    private static void enablePresence(String channel,Boolean metadata){
        Ortc.enablePresence(serverUrl, isBalancer, applicationKey, defaultPrivateKey, channel, metadata, proxy, new OnEnablePresence() {

            @Override
            public void run(Exception error, String result) {
                if(error != null){
                    System.out.println(error.getMessage());
                }else{
                    System.out.println(result);

                }
            }
        });
    }

    private static void disablePresence(String channel){
        Ortc.disablePresence(serverUrl, isBalancer, applicationKey, defaultPrivateKey, channel, proxy, new OnDisablePresence() {

            @Override
            public void run(Exception error, String result) {
                if(error != null){
                    System.out.println(error.getMessage());
                }else{
                    System.out.println(result);
                }
            }
        });
    }

    private static void subscribeChannel(OrtcClient client, String channel) {
        client.subscribe(channel, true, new OnMessage() {
            @Override
            public void run(OrtcClient sender, String channel, String message) {
                System.out.println(String.format("Message received on channel %s: '%s'", channel, message));

                if ("unsubscribe".equals(message)) {
                    ((OrtcClient) sender).unsubscribe(channel);
                } else if ("disconnect".equals(message)) {
                    ((OrtcClient) sender).disconnect();
                }
            }
        });
    }

    private static void connectOrtcClient() throws Exception {
        if (defaultNeedsAuthentication) {
            System.out.println("Authenticating...");

            HashMap<String, LinkedList<ChannelPermissions>> permissions = new HashMap<String, LinkedList<ChannelPermissions>>();

            LinkedList<ChannelPermissions> yellowPermissions = new LinkedList<ChannelPermissions>();
            yellowPermissions.add(ChannelPermissions.Write);
            yellowPermissions.add(ChannelPermissions.Presence);

            LinkedList<ChannelPermissions> testPermissions = new LinkedList<ChannelPermissions>();
            testPermissions .add(ChannelPermissions.Read);
            testPermissions .add(ChannelPermissions.Presence);


            permissions.put("yellow:*", yellowPermissions);
            permissions.put("test:*", testPermissions);

            if (Ortc.saveAuthentication(serverUrl, isBalancer, authenticationToken, false, applicationKey, 14000, defaultPrivateKey, permissions, proxy)) {
                System.out.println("Authentication successful");
            }
            else {
                System.out.println("Unable to authenticate");
            }
        }

        Ortc api = new Ortc();

        OrtcFactory factory = api.loadOrtcFactory("IbtRealtimeSJ");

        final OrtcClient client = factory.createClient();

        if (System.getProperty("http.proxyHost") != null) {
            System.out.println("Main.connectOrtcClient: using proxy");
            proxy = new Proxy(System.getProperty("http.proxyHost"), Integer.parseInt(System.getProperty("http.proxyPort")), System.getProperty("http.proxyUser"), System.getProperty("http.proxyPassword"));
            client.setProxy(proxy);
        }
        client.setHeartbeatActive(false);

        if (isBalancer) {
            client.setClusterUrl(serverUrl);
        } else {
            client.setUrl(serverUrl);
        }

        System.out.println(String.format("Connecting to server %s", serverUrl));

        client.onConnected = new OnConnected() {
            @Override
            public void run(OrtcClient sender) {
                System.out.println(String.format("Connected to %s", client.getUrl()));
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        interfaceMenu();
                        readMenuCommand(client);
                    }
                });
            }
        };

        client.onException = new OnException() {
            @Override
            public void run(OrtcClient send, Exception ex) {
                System.out.println(String.format("Error: '%s'", ex.toString()));
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        readMenuCommand(client);
                    }
                });
                //readMenuCommand(client);
            }
        };

        client.onDisconnected = new OnDisconnected() {
            @Override
            public void run(OrtcClient sender) {
                System.out.println("Disconnected");
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        readMenuCommand(client);
                    }
                });
                //readMenuCommand(client);
            }
        };

        client.onReconnected = new OnReconnected() {
            @Override
            public void run(OrtcClient sender) {
                System.out.println(String.format("Reconnected to %s", client.getUrl()));
            }
        };

        client.onReconnecting = new OnReconnecting() {
            @Override
            public void run(OrtcClient sender) {
                System.out.println(String.format("Reconnecting to %s", client.getUrl()));
            }
        };

        client.onSubscribed = new OnSubscribed() {
            @Override
            public void run(OrtcClient sender, String channel) {
                System.out.println(String.format("Subscribed to channel %s (Receive the message 'unsubscribe' to stop receiving and enter commands)", channel));

            }
        };

        client.onUnsubscribed = new OnUnsubscribed() {
            @Override
            public void run(OrtcClient sender, String channel) {
                System.out.println(String.format("Unsubscribed from channel %s", channel));
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        readMenuCommand(client);
                    }
                });
            }
        };

        System.out.println("Connecting...");
        client.setConnectionMetadata("JavaApp");
        client.setHeartbeatActive(true);
        client.connect(applicationKey, authenticationToken);
    }
}
