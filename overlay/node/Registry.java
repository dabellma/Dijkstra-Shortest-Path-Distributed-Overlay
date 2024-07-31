package CS555.overlay.node;

import CS555.overlay.transport.TCPChannel;
import CS555.overlay.transport.TCPServerThread;
import CS555.overlay.util.OverlayCreator;
import CS555.overlay.wireformats.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Registry implements Node {

    private AtomicInteger taskCompleteMessagesReceived = new AtomicInteger(0);

    private List<TaskSummaryResponse> taskSummaryResponses = new ArrayList<>();
    private Map<String, TCPChannel> tcpChannels = new ConcurrentHashMap<>();

    public Map<String, TCPChannel> getTCPChannels() {
        return this.tcpChannels;
    }

    //I put both directions, even though links are bidirectional, in this set
    //for example, if A->B 7, I also put B->A 7
    private Set<LinkInfo> linkWeights = new HashSet<>();

    public static void main(String[] args) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        if (args.length == 1) {
            try {
                int portNumber = Integer.parseInt(args[0]);

                System.out.println("Starting up registry on ip address and port number: " + InetAddress.getLocalHost().getHostAddress() + " " + portNumber);
                System.out.println();

                Registry registry = new Registry();

                //Create the socket to accept incoming messages from messenger nodes
                ServerSocket ourServerSocket = new ServerSocket(Integer.parseInt(args[0]));

                //Spawn a thread to receive connections to the Registry node
                //while at the same time being able to run as a foreground process
                Thread registryTCPServerThread = new Thread(new TCPServerThread(ourServerSocket, registry));
                registryTCPServerThread.start();

                while (true) {
                    String input;
                    input = reader.readLine();

                    registry.processInput(input);
                }

            } catch (IOException | InterruptedException exception) {
                System.out.println("Encountered an issue while setting up main Registry");
                System.exit(1);
            }

        } else {
            System.out.println("Please enter exactly one argument. Exiting.");
            System.exit(1);
        }
    }

    private void processInput(String input) throws IOException, InterruptedException {
        String[] tokens = input.split("\\s+");
        switch (tokens[0].toUpperCase()) {

            case "LIST-MESSAGING-NODES":
                for (String connection : tcpChannels.keySet()) {
                    System.out.println(connection);
                }
                break;

            case "LIST-WEIGHTS":
                System.out.println("Listing weights... there are duplicates, noting bidirectionality, stored in the link weights collection for ease, but only one connection exists between each pair of connected nodes.");
                for (LinkInfo edge : linkWeights) {
                    System.out.println(edge.getIpAddressA() + ":" + edge.getPortNumberA() + " " + edge.getIpAddressB() + ":" + edge.getPortNumberB() + " " + edge.getWeight());
                }
                break;

            case "SETUP-OVERLAY":

                if (tokens.length != 2) {
                    System.out.println("Please add a number of connections as a second argument when setting up the overlay.");
                    break;
                }

                if (tcpChannels.size() < 10) {
                    System.out.println("Need at least 10 messaging nodes, please register more messaging nodes.");
                    break;
                }

                System.out.println("Setting up overlay");
                int numberOfConnections = Integer.parseInt(tokens[1]);
                if (numberOfConnections < 4) {
                    System.out.println("Need at least 4 connections on each node, setting to the default value of 4 connections instead of the user inputted number");
                    numberOfConnections = 4;
                }

                if (overlayChecksFail(numberOfConnections)) {
                    break;
                }

                //Note: the overlay creates a map which lists "other" messaging nodes to connect to, for each key in the map
                //this is done to ensure a messaging node doesn't get itself as a neighbor
                OverlayCreator overlayCreator = new OverlayCreator();
                Map<Integer, Set<Integer>> overlay = overlayCreator.createDesiredLinksPerVertex(tcpChannels.size(), numberOfConnections);
                Set<String> containedLinks = new HashSet<>();

                for (int i=0; i < tcpChannels.size(); i++) {

                    Set<Integer> indicesOfConnectionsToMake = overlay.get(i);
                    String fromMessagingNode = getMessagingNodeFromSet(i);
                    Set<MessagingNode> messagingNodeListToThisMessagingNode = new HashSet<>();

                        for (Integer indexOfConnectionToMake : indicesOfConnectionsToMake) {
                            String toMessagingNode = getMessagingNodeFromSet(indexOfConnectionToMake);

                            if (!containedLinks.contains(fromMessagingNode+"-"+toMessagingNode) && !containedLinks.contains(toMessagingNode+"-"+fromMessagingNode)) {
                                messagingNodeListToThisMessagingNode.add(messagingNodeDestring(toMessagingNode));
                                containedLinks.add(fromMessagingNode+"-"+toMessagingNode);
                            }
                        }

                    //create socket to a registered node
                    TCPChannel tcpChannel = tcpChannels.get(fromMessagingNode);

                    //send a messaging node list of all other messaging nodes, to this messaging node
                    MessagingNodesList realMessagingNodesList = new MessagingNodesList(messagingNodeListToThisMessagingNode.size(), messagingNodeListToThisMessagingNode);
                    tcpChannel.getTcpSenderThread().sendData(realMessagingNodesList.getbytes());

                }

                //create and store link weights
                Random random = new Random();
                Map<String, Integer> commonWeights = new HashMap<>();

                for (Map.Entry<Integer, Set<Integer>> eachNode : overlay.entrySet()) {
                    MessagingNode nodeA = messagingNodeDestring(getMessagingNodeFromSet(eachNode.getKey()));

                    //for each of the nodes this node should connect to
                    for (Integer toNode : eachNode.getValue()) {
                        MessagingNode nodeB = messagingNodeDestring(getMessagingNodeFromSet(toNode));

                        Integer weight;
                        String weightKey = getWeightKey(nodeA.getIpAddress(), nodeB.getIpAddress(), nodeA.getServerSocketPortNumber(), nodeB.getServerSocketPortNumber());

                        if (commonWeights.containsKey(weightKey)) {
                            weight = commonWeights.get(weightKey);
                        } else {
                            weight = random.nextInt(10) + 1;
                            commonWeights.put(weightKey, weight);
                        }
                        linkWeights.add(new LinkInfo(nodeA.getIpAddress(), nodeB.getIpAddress(), nodeA.getServerSocketPortNumber(), nodeB.getServerSocketPortNumber(), weight));
                    }
                }
                break;

            case "SEND-OVERLAY-LINK-WEIGHTS":

                if (linkWeights.size() == 0) {
                    System.out.println("Please create overlay first to populate link weights.");
                    break;
                }

                int numberOfLinks = linkWeights.size();

                LinkWeights linkWeights = new LinkWeights(numberOfLinks, this.linkWeights);

                for (String messagingNode : tcpChannels.keySet()) {

                    //get socket to a registered node
                    TCPChannel tcpChannel = tcpChannels.get(messagingNode);
                    tcpChannel.getTcpSenderThread().sendData(linkWeights.getbytes());
                }

                break;

            case "START":

                if (this.linkWeights.size() == 0) {
                    System.out.println("Please create overlay first to populate link weights.");
                    break;
                }

                if (tokens.length != 2) {
                    System.out.println("Please add a number of rounds when starting the message transferring between messaging nodes.");
                    break;
                }

                int numberOfRounds = Integer.parseInt(tokens[1]);
                System.out.println("Sending task initiate command to messaging nodes... number of rounds is: " + numberOfRounds);
                for (String messagingNode : tcpChannels.keySet()) {

                    TaskInitiate taskInitiate = new TaskInitiate(numberOfRounds);

                    //get socket to a registered node
                    TCPChannel tcpChannel = tcpChannels.get(messagingNode);
                    tcpChannel.getTcpSenderThread().sendData(taskInitiate.getbytes());
                }

                break;

            default:
                System.out.println("Unknown command. Re-entering wait period.");
                break;
        }
    }

    @Override
    public void onEvent(Event event, TCPChannel tcpChannel) throws IOException, InterruptedException {
        if (event instanceof DeregisterRequest) {
            DeregisterRequest deregisterRequest = (DeregisterRequest) event;

            if (!deregisterRequest.getIpAddress().equals(tcpChannel.getSocket().getInetAddress().getHostAddress())) {
                System.out.println("Did not pass checks to deregister this messaging node because there's a mismatch between the deregister address and the address of the request");

                //send a response that the messaging node can not safely exit and terminate the process
                DeregisterResponse deregisterResponse = new DeregisterResponse((byte) 0);
                tcpChannel.getTcpSenderThread().sendData(deregisterResponse.getbytes());
            } else if (!tcpChannels.containsKey(new MessagingNode(deregisterRequest.getIpAddress(), deregisterRequest.getPortNumber()).toString())) {
                System.out.println("Did not pass checks to deregister this messaging node because the deregister address doesn't exist in the registry");

                //send a response that the messaging node can not safely exit and terminate the process
                DeregisterResponse deregisterResponse = new DeregisterResponse((byte) 0);
                tcpChannel.getTcpSenderThread().sendData(deregisterResponse.getbytes());
            } else {
                //if there's a successful deregistration
                System.out.println("Deregistering a messaging node with the registry");
                String messagingNodeToRemove = new MessagingNode(deregisterRequest.getIpAddress(), deregisterRequest.getPortNumber()).toString();

                tcpChannels.remove(messagingNodeToRemove);

                //send a response that the messaging node can safely exit and terminate the process
                DeregisterResponse deregisterResponse = new DeregisterResponse((byte) 1);
                tcpChannel.getTcpSenderThread().sendData(deregisterResponse.getbytes());

                Thread.sleep(2000);

                tcpChannel.getTcpReceiverThread().getDataInputStream().close();
                tcpChannel.getTcpSenderThread().getDataOutputStream().close();
                tcpChannel.getSocket().close();
            }

        } else if (event instanceof RegisterRequest) {
            RegisterRequest registerRequest = (RegisterRequest) event;

            //if there's an unsuccessful registration
            if (!registerRequest.getIpAddress().equals(tcpChannel.getSocket().getInetAddress().getHostAddress())) {

                System.out.println("Did not pass checks to register this messaging node because there's a mismatch between the registration address and the address of the request" );

                //send a response from registry to messaging node
                //saying the messaging node did not register successfully
                RegisterResponse registerResponse = new RegisterResponse((byte) 0, "Registration request unsuccessful because there's a mismatch between the registration address and the address of the request");
                tcpChannel.getTcpSenderThread().sendData(registerResponse.getbytes());

            } else if (tcpChannels.containsKey(new MessagingNode(registerRequest.getIpAddress(), registerRequest.getPortNumber()).toString())) {
                System.out.println("Did not pass checks to register this messaging node because this node already exists in the registry" );

                //send a response from registry to messaging node
                //saying the messaging node did not register successfully
                RegisterResponse registerResponse = new RegisterResponse((byte) 0, "Registration request unsuccessful because this node already exists in the registry");
                tcpChannel.getTcpSenderThread().sendData(registerResponse.getbytes());
            } else {
                //if there's a successful registration
                String messagingNode = new MessagingNode(registerRequest.getIpAddress(), registerRequest.getPortNumber()).toString();
                tcpChannels.put(messagingNode, tcpChannel);


                //to simulate failure before the deregister response gets back to the messaging node
                //sleep, kill the other node, and ensure the killed node doesn't get any response
                //Thread.sleep(8000);


                //send a response from registry to messaging node saying the messaging node registered successfully
                RegisterResponse registerResponse = new RegisterResponse((byte) 1, "Registration request to registry was successful. The number of messaging nodes currently constituting the overlay is " + tcpChannels.size());
                tcpChannel.getTcpSenderThread().sendData(registerResponse.getbytes());
            }

        } else if (event instanceof TaskComplete) {
            TaskComplete taskComplete = (TaskComplete) event;
            taskCompleteMessagesReceived.getAndIncrement();

            if (taskCompleteMessagesReceived.get() == tcpChannels.size()) {
                Thread.sleep(15000);

                for (String messagingNode : tcpChannels.keySet()) {

                    TaskSummaryRequest taskSummaryRequest = new TaskSummaryRequest();

                    TCPChannel messagingNodeTCPChannel = tcpChannels.get(messagingNode);
                    messagingNodeTCPChannel.getTcpSenderThread().sendData(taskSummaryRequest.getbytes());
                }

                //reset it so the registry can "start" again if desired
                taskCompleteMessagesReceived.set(0);
            }

        } else if (event instanceof TaskSummaryResponse) {
            TaskSummaryResponse taskSummaryResponse = (TaskSummaryResponse) event;
            System.out.println("Received traffic summary response");
            addTaskSummaryResponse(taskSummaryResponse);

        } else {
            System.out.println("Received unknown event");
        }
    }

    private String getMessagingNodeFromSet(int numberOfItemInMap) {
        if (numberOfItemInMap > tcpChannels.size()){
            throw new RuntimeException();
        }
        int number = 0; //start with a number of 0 for the first item in the set
        for (String messagingNode : tcpChannels.keySet()) {
            if (number == numberOfItemInMap) {
                return messagingNode;
            }
            number++;
        }
        return null;
    }

    private boolean overlayChecksFail(int numberOfConnections) {
        if (tcpChannels.size() <= numberOfConnections) {
            System.out.println("Need at least 1 more vertex than the number of edges... For example, for 8 nodes the max is 7 connections each");
            return true;
        }

        if ((tcpChannels.size() * numberOfConnections) % 2 == 1) {
            System.out.println("Num vertices times num edges must be even for this to work");
            return true;
        }
        return false;
    }

    private String getWeightKey(String ipAddressA, String ipAddressB, int portNumberA, int portNumberB) {
        if ((ipAddressA+portNumberA).compareTo(ipAddressB+portNumberB) < 0) {
            return ipAddressA+portNumberA+ipAddressB+portNumberB;
        } else {
            return ipAddressB+portNumberB+ipAddressA+portNumberA;
        }
    }

    private synchronized void addTaskSummaryResponse(TaskSummaryResponse taskSummaryResponse) {
        taskSummaryResponses.add(taskSummaryResponse);

        System.out.println();
        //if received traffic summaries from all the nodes
        if (taskSummaryResponses.size() == tcpChannels.size()) {

            String topRowFormat = "%-20s | %-20s | %-20s | %-20s | %-20s | %-20s%n";
            String format = "%-20s | %-20s | %-20s | %-20s | %-20s | %-20s%n";
            String sumFormat = "%-20s | %-20s | %-20s | %-20s | %-20s%n";

            int totalNumSent = 0;
            int totalNumReceived = 0;
            long totalSumSent = 0;
            long totalSumReceived = 0;

            System.out.printf(topRowFormat, "", "Num messages sent", "Num messages received", "Sum sent messages", "Sum received messages", "Num messages relayed");

            for (TaskSummaryResponse tsr : taskSummaryResponses) {
                System.out.printf(format, tsr.getIpAddress()  + ":" +  tsr.getPortNumber(), tsr.getNumSent(), tsr.getNumReceived(), tsr.getSumSent(), tsr.getSumReceived(), tsr.getNumRelayed());
                totalNumSent += tsr.getNumSent();
                totalNumReceived += tsr.getNumReceived();
                totalSumSent += tsr.getSumSent();
                totalSumReceived += tsr.getSumReceived();

            }

            System.out.println();
            System.out.printf(sumFormat, "Sum", totalNumSent, totalNumReceived, totalSumSent, totalSumReceived);

            //reset so the registry can "start" again if desired
            taskSummaryResponses.clear();

        }
    }

    private MessagingNode messagingNodeDestring(String firstValue) {
        String[] ipAddressAndPort = firstValue.split(":");
        String ipAddress = ipAddressAndPort[0];
        int portNumber = Integer.parseInt(ipAddressAndPort[1]);
        return new MessagingNode(ipAddress, portNumber);
    }

}
