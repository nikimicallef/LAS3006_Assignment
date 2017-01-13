package server;

import clientdisconnector.InactivityChannelMonitorMbean;
import clientdisconnector.InactivityChannelMonitorThreading;
import properties.GlobalProperties;
import resources.*;

import javax.management.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.stream.Collectors;

public class Server implements ServerMbean {
    private Selector serverSelector = null;
    private ServerSocketChannel serverSocketChannel = null;

    private Map<String, List<SelectionKey>> listOfSubscribers = new HashMap<>();
    private final List<Pair<SelectionKey, ServerCustomMessage>> messagesToSend = new ArrayList<>();

    private final InactivityChannelMonitorThreading inactivityChannelMonitorThreading;

    private int clientsConnected = 0;
    private int totalMessagesDelivered = 0;
    private Map<String, Integer> noOfMessagesDeliveredPerTopic = new HashMap<>();
    private Map<String, Integer> noOfMessagesDeliveredToEachClient = new HashMap<>();
    private Map<String, Integer> noOfMessagesPublishedByEachClient = new HashMap<>();
    private Map<SelectionKey, String> mapSelectionKeyWithClientId = new HashMap<>();

    @Override
    public int getClientsConnected() {
        return clientsConnected;
    }

    @Override
    public Set<String> getActivePath() {
        return listOfSubscribers.keySet();
    }

    @Override
    public int getTotalMessagesDelivered() {
        return totalMessagesDelivered;
    }

    @Override
    public Map<String, Integer> getNoOfMessagesDeliveredPerTopic() {
        return noOfMessagesDeliveredPerTopic;
    }

    @Override
    public Map<String, Integer> getNoOfMessagesDeliveredToEachClient() {
        return noOfMessagesDeliveredToEachClient;
    }

    @Override
    public Map<String, Integer> noOfMessagesPublishedByEachClient() {
        return noOfMessagesPublishedByEachClient;
    }

    public Server() {
        init();

        inactivityChannelMonitorThreading = new InactivityChannelMonitorThreading(new InactivityChannelMonitorMbean());
    }

    private void init() {
        if (serverSelector != null) {
            return;
        }
        if (serverSocketChannel != null) {
            return;
        }

        try {
            serverSelector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(GlobalProperties.address, GlobalProperties.port));
            serverSocketChannel.register(serverSelector, serverSocketChannel.validOps());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectionManager() throws IOException, ClassNotFoundException {
        while (true) {
            disconnectInactiveChannels();
            if (serverSelector.select() > 0) {
                final Iterator<SelectionKey> keyIterator = serverSelector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    final SelectionKey selectionKey = keyIterator.next();
                    keyIterator.remove();
                    if (!selectionKey.isValid()) {
                        System.out.println("Selection key is not valid. " + selectionKey.toString());
                    } else if (selectionKey.isAcceptable()) {
                        acceptConnection(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        read(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        write(selectionKey);
                    }
                }
            }
        }
    }

    private void disconnectInactiveChannels() {
        final List<SelectionKey> keysNotInvalidated = new ArrayList<>();
        synchronized (inactivityChannelMonitorThreading.getInactivityChannelMonitor().getKeysToInvalidate()) {
            inactivityChannelMonitorThreading.getInactivityChannelMonitor().getKeysToInvalidate().forEach(selectionKey -> {
                try {
                    System.out.println("Closing inactive channel " + selectionKey.channel());
                    closeChannel(selectionKey, false);
                    synchronized (inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime()) {
                        inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime().remove(selectionKey);
                    }
                } catch (IOException e) {
                    System.out.println("Could not close " + selectionKey.channel());
                    keysNotInvalidated.add(selectionKey);
                }
            });
        }

        synchronized (inactivityChannelMonitorThreading.getInactivityChannelMonitor().getKeysToInvalidate()) {
            inactivityChannelMonitorThreading.getInactivityChannelMonitor().setKeysToInvalidate(new ArrayList<>(keysNotInvalidated));
        }
    }

    private void acceptConnection(final SelectionKey selectionKey) throws IOException {
        final ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        final SocketChannel clientSocketChannel = serverSocketChannel.accept();
        clientSocketChannel.configureBlocking(false);
        final SelectionKey newSelectionKey = clientSocketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);

        synchronized (inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime()) {
            inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime().putIfAbsent(newSelectionKey, System.currentTimeMillis());
        }
    }

    private void write(final SelectionKey selectionKey) throws IOException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        final List<Pair<SelectionKey, ServerCustomMessage>> messagesToRemove = new ArrayList<>();

        messagesToSend.stream().filter(messageToSend -> messageToSend.getFirst() == selectionKey).forEach(messageToSend -> {
            System.out.println("Writing message " + messageToSend.getSecond().getMessage() + " to selection key " + messageToSend.getFirst().channel().toString());
            final byte[] serializedMessage;
            try {
                serializedMessage = GlobalProperties.serializeMessage(messageToSend.getSecond());
                messagesToRemove.add(messageToSend);
                socketChannel.write(ByteBuffer.wrap(serializedMessage));
                totalMessagesDelivered++;
                noOfMessagesDeliveredToEachClient.put(mapSelectionKeyWithClientId.get(selectionKey), noOfMessagesDeliveredToEachClient.get(mapSelectionKeyWithClientId.get(selectionKey)) + 1);
            } catch (IOException e) {
                System.out.println("Failed to write.");
                e.printStackTrace();
            }
        });

        messagesToRemove.forEach(messagesToSend::remove);

        selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void read(final SelectionKey selectionKey) throws IOException, ClassNotFoundException {
        final SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(1024);

        try {
            socketChannel.read(buffer);
            ClientCustomMessage deserializedClientMessage = (ClientCustomMessage) GlobalProperties.deserializeMessage(buffer.array());

            noOfMessagesDeliveredToEachClient.putIfAbsent(deserializedClientMessage.getClientId(), 0);
            noOfMessagesPublishedByEachClient().put(deserializedClientMessage.getClientId(), noOfMessagesDeliveredToEachClient.get(deserializedClientMessage.getClientId() + 1));

            if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PUBLISH) {
                System.out.println("Data read: " + deserializedClientMessage.getMessage());

                prepareAckMessage(ServerMessageKey.PUBACK, selectionKey);

                final List<String> validPaths = listOfSubscribers.keySet().stream().filter(path -> PathParsing.pathsMatch(deserializedClientMessage.getPath(), path)).collect(Collectors.toList());

                validPaths.forEach(path -> {
                    final List<SelectionKey> selectionKeys = listOfSubscribers.get(path);
                    selectionKeys.forEach(selectionKey1 -> {
                        preparePublishMessage(selectionKey1, deserializedClientMessage.getMessage());
                        noOfMessagesDeliveredPerTopic.put(path, noOfMessagesDeliveredPerTopic.get(path) + 1);
                    });
                });
            } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PUBREC) {
                System.out.println("Pubrec received for " + deserializedClientMessage.getClientMessageKey().toString());
            } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.SUBSCRIBE) {
                System.out.println("Subscribe to Path: " + deserializedClientMessage.getPath());

                final boolean pathValid = PathParsing.pathChecker(deserializedClientMessage.getPath());

                if (pathValid) {
                    listOfSubscribers.putIfAbsent(deserializedClientMessage.getPath(), new ArrayList<>());
                    if (!listOfSubscribers.get(deserializedClientMessage.getPath()).contains(selectionKey)) {
                        listOfSubscribers.get(deserializedClientMessage.getPath()).add(selectionKey);
                    }

                    noOfMessagesDeliveredPerTopic.putIfAbsent(deserializedClientMessage.getPath(), 0);
                } else {
                    System.out.println("Path " + deserializedClientMessage.getPath() + " is not valid.");
                }

                prepareAckMessage(ServerMessageKey.SUBACK, selectionKey);
            } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.CONNECT) {
                System.out.println("Connect message received from client with id " + deserializedClientMessage.getClientId());

                clientsConnected++;
                noOfMessagesDeliveredToEachClient.put(deserializedClientMessage.getClientId(), 0);
                mapSelectionKeyWithClientId.put(selectionKey, deserializedClientMessage.getClientId());

                prepareAckMessage(ServerMessageKey.CONNACK, selectionKey);
            } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.UNSUBSCRIBE) {
                boolean unsubscribeSuccessful = false;
                if (listOfSubscribers.get(deserializedClientMessage.getPath()) != null) {
                    if (listOfSubscribers.get(deserializedClientMessage.getPath()).remove(selectionKey)) {
                        unsubscribeSuccessful = true;
                        System.out.println(selectionKey.channel() + " has unsubscribed from " + deserializedClientMessage.getPath());
                    }
                } else {
                    System.out.println("No subscribers on path " + deserializedClientMessage.getPath());
                }

                prepareAckMessage(ServerMessageKey.UNSUBACK, selectionKey, unsubscribeSuccessful);
            } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.PINGREQ) {
                System.out.println("Pingreq received from " + selectionKey.channel());

                synchronized (inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime()) {
                    inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime().put(selectionKey, System.currentTimeMillis());
                }

                prepareAckMessage(ServerMessageKey.PINGRESP, selectionKey);
            } else if (deserializedClientMessage.getClientMessageKey() == ClientMessageKey.DISCONNECT) {
                System.out.println("Disconnect received.");

                listOfSubscribers.keySet().forEach(path -> listOfSubscribers.get(path).remove(selectionKey));

                closeChannel(selectionKey, true);
            }
        } catch (final IOException e) {
            System.out.println("Failed to read from " + socketChannel);
        }
    }

    private void closeChannel(final SelectionKey selectionKey, final boolean automated) throws IOException {
        listOfSubscribers.keySet().forEach(path -> listOfSubscribers.get(path).remove(selectionKey));

        if (automated) {
            synchronized (inactivityChannelMonitorThreading.getInactivityChannelMonitor().getKeysToInvalidate()) {
                inactivityChannelMonitorThreading.getInactivityChannelMonitor().getKeysToInvalidate().remove(selectionKey);
            }

            synchronized (inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime()) {
                inactivityChannelMonitorThreading.getInactivityChannelMonitor().getLastSelectionKeyActivityTime().remove(selectionKey);
            }
        }

        clientsConnected--;

        selectionKey.channel().close();
        ((SocketChannel) selectionKey.channel()).socket().close();
        selectionKey.cancel();
    }

    private void prepareAckMessage(final ServerMessageKey serverMessageKey, final SelectionKey selectionKey, final boolean... status) {
        if (serverMessageKey == ServerMessageKey.UNSUBACK) {
            messagesToSend.add(new Pair<>(selectionKey, new ServerCustomMessage(serverMessageKey, "Ack of type " + serverMessageKey, status[0])));
        } else {
            messagesToSend.add(new Pair<>(selectionKey, new ServerCustomMessage(serverMessageKey, "Ack of type " + serverMessageKey)));
        }

        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    private void preparePublishMessage(final SelectionKey selectionKey, final String message) {
        messagesToSend.add(new Pair<>(selectionKey, new ServerCustomMessage(ServerMessageKey.PUBLISH, message)));

        selectionKey.interestOps(SelectionKey.OP_WRITE);
    }

    public ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("AssignmentMonitoring:type=ServerMonitoring");
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final Server server = new Server();

        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(server.inactivityChannelMonitorThreading.getInactivityChannelMonitor(), server.inactivityChannelMonitorThreading.getInactivityChannelMonitor().getObjectName());
        mbs.registerMBean(server, server.getObjectName());

        server.connectionManager();
    }
}
