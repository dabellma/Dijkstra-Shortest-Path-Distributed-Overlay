package CS555.overlay.node;

import CS555.overlay.dijkstra.ShortestPath;
import CS555.overlay.transport.TCPChannel;
import CS555.overlay.transport.TCPSenderThread;
import CS555.overlay.transport.TCPServerThread;
import CS555.overlay.util.StatisticsCollectorAndDisplay;
import CS555.overlay.wireformats.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingNode implements Node {

    private Set<LinkInfo> linkWeights = new HashSet<>();
    private Set<String> allOtherMessagingNodes = new HashSet<>();
    private TCPChannel registryTcpChannel;

    private Map<String, TCPChannel> tcpChannels = new ConcurrentHashMap<>();

    //"to" node as key, path as value
    private Map<String, List<String>> routingCache = new ConcurrentHashMap<>();

    private String ipAddress;
    private int serverSocketPortNumber;
    private String registryIPAddress;
    private int registryPortNumber;


    public MessagingNode(String ipAddress, int serverSocketPortNumber) {
        this.ipAddress = ipAddress;
        this.serverSocketPortNumber = serverSocketPortNumber;
    }

    public MessagingNode(String ipAddress, int serverSocketPortNumber, String registryIPAddress, int registryPortNumber) {
        this.ipAddress = ipAddress;
        this.serverSocketPortNumber = serverSocketPortNumber;
        this.registryIPAddress = registryIPAddress;
        this.registryPortNumber = registryPortNumber;
    }

    private StatisticsCollectorAndDisplay statisticsCollectorAndDisplay = new StatisticsCollectorAndDisplay();

    public String getIpAddress() {
        return this.ipAddress;
    }

    public int getServerSocketPortNumber() {
        return this.serverSocketPortNumber;
    }

    public static void main(String[] args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        if (args.length == 2) {
            System.out.println();

            try {
                //create a new server socket on messaging node
                ServerSocket serverSocket = new ServerSocket(0);

                MessagingNode messagingNode = new MessagingNode(InetAddress.getLocalHost().getHostAddress(), serverSocket.getLocalPort(), args[0], Integer.parseInt(args[1]));
                System.out.println("Starting up messaging node on ip address and port number: " + InetAddress.getLocalHost().getHostAddress() + ":" + serverSocket.getLocalPort());

                Thread messagingNodeServerThread = new Thread(new TCPServerThread(serverSocket, messagingNode));
                messagingNodeServerThread.start();

                //let the registry know about this new messaging node
                System.out.println();
                messagingNode.registerWithRegistry();

                while (true) {
                    String input;
                    input = reader.readLine();

                    messagingNode.processInput(input);
                }

            } catch (IOException | InterruptedException e) {
                System.out.println("Encountered an issue while setting up MessagingNode");
                System.exit(1);
            }

        } else {
            System.out.println("Please enter exactly two arguments for the messaging node call. Exiting.");
            System.exit(1);
        }

    }

    private void processInput(String input) throws IOException, InterruptedException {
        switch (input.toUpperCase()) {
            case "EXIT-OVERLAY":

                if (tcpChannels.size() != 0) {
                    System.out.println("Cannot exit after the overlay has been set up");
                    break;
                }

                DeregisterRequest deregisterRequest = new DeregisterRequest(this.getIpAddress(), this.getServerSocketPortNumber());
                registryTcpChannel.getTcpSenderThread().sendData(deregisterRequest.getbytes());
                break;

            case "LIST-CONNECTIONS":
                for (String connection : tcpChannels.keySet()) {
                    System.out.println(connection);
                }
                break;

            case "LIST-WEIGHTS":
                if (linkWeights.size() == 0) {
                    System.out.println("Please have the overlay send out the link weights before trying to list them from a messaging node.");
                    break;
                }

                System.out.println("Listing weights with duplicates");
                for (LinkInfo edge : linkWeights) {
                    System.out.println(edge.getIpAddressA() + ":" + edge.getPortNumberA() + " " + edge.getIpAddressB() + ":" + edge.getPortNumberB() + " " + edge.getWeight());
                }

                break;

            case "PRINT-SHORTEST-PATH":

                if (linkWeights.size() == 0) {
                    System.out.println("Please send link weights to each messaging node first before trying to print shortest path.");
                    break;
                }

                System.out.println("Printing shortest paths...");

                for (Map.Entry<String, List<String>> messagingNodeToPath : routingCache.entrySet()) {
                    System.out.println("The shortest path from " + this + " to " + messagingNodeToPath.getKey() + ":");
                    List<String> shortestPath = routingCache.get(messagingNodeToPath.getKey());
                    List<MessagingNode> messagingNodesPath = convertDjikstraStringPathToMessagingNodePath(shortestPath);
                    printShortestPath(messagingNodesPath);
                    System.out.println();
                }

            break;

            default:
                System.out.println("Unknown command. Re-entering wait period.");
                break;
        }

    }

    private void printShortestPath(List<MessagingNode> messagingNodesPath) {
        for (int i = 0; i < (messagingNodesPath.size() - 1); i++) {
            int weight = getWeightBetweenMessagingNodesFromLinkWeights(messagingNodesPath.get(i), messagingNodesPath.get(i + 1));
            System.out.print(messagingNodesPath.get(i).toString() + "--" + weight + "--" + messagingNodesPath.get(i + 1) + "   ");
        }
        System.out.println();
    }

    private int getWeightBetweenMessagingNodesFromLinkWeights(MessagingNode firstMessagingNode, MessagingNode secondMessagingNode) {
        for (LinkInfo linkInfo : linkWeights) {
            if (linkInfo.getIpAddressA().equals(firstMessagingNode.getIpAddress())
            && (linkInfo.getPortNumberA() == firstMessagingNode.getServerSocketPortNumber())
            && linkInfo.getIpAddressB().equals(secondMessagingNode.getIpAddress())
            && (linkInfo.getPortNumberB() == secondMessagingNode.getServerSocketPortNumber())) {
                return linkInfo.getWeight();
            }
        }
        return -1;
    }

    @Override
    public void onEvent(Event event, TCPChannel tcpChannel) throws IOException, InterruptedException {
        if (event instanceof DeregisterResponse) {

            DeregisterResponse deregisterResponse = (DeregisterResponse) event;
            if (deregisterResponse.getSuccessOrFailure() == 1) {
                System.out.println("Received successful deregister response from the registry. Terminating this messaging node.");

                Thread.sleep(2000);

                registryTcpChannel.getTcpReceiverThread().getDataInputStream().close();
                registryTcpChannel.getTcpSenderThread().getDataOutputStream().close();
                registryTcpChannel.getSocket().close();

                System.exit(1);
            } else {
                System.out.println("Did not receive a successful deregister response from the registry.");
            }

        } else if (event instanceof LinkWeights) {

            //populate link weights and allOtherMessagingNodes set
            LinkWeights linkWeights = (LinkWeights) event;
            this.linkWeights.addAll(linkWeights.getLinkWeights());

            for (LinkInfo linkInfo : this.linkWeights) {
                String messagingNode1 = new MessagingNode(linkInfo.getIpAddressA(), linkInfo.getPortNumberA()).toString();
                String messagingNode2 = new MessagingNode(linkInfo.getIpAddressB(), linkInfo.getPortNumberB()).toString();
                this.allOtherMessagingNodes.add(messagingNode1);
                this.allOtherMessagingNodes.add(messagingNode2);

            }

            //remove this one from the other node set, so it's truly "other" nodes and doesn't include this messaging node
            this.allOtherMessagingNodes.remove(this.toString());


            //calculate and cache shortest paths for this messaging node
            ShortestPath shortestPath = new ShortestPath();
            for (String toMessagingNode : allOtherMessagingNodes) {
                List<String> path = shortestPath.getDijsktraPath(this.toString(), toMessagingNode, this.linkWeights);
                routingCache.put(toMessagingNode, path);
            }

            System.out.println();
            System.out.println("Link weights received and processed. Routing caches created. Ready to send messages.");

        } else if (event instanceof Message) {
            Message message = (Message) event;

            int thisMessagingNodesPosition = getThisMessagingNodesPositionInPath(message.getPath());

            //if this is the last node in the path, receive the message and increment receive numbers
            if (thisMessagingNodesPosition == (message.getPath().size() - 1)) {
                statisticsCollectorAndDisplay.getNumReceived().getAndIncrement();
                statisticsCollectorAndDisplay.getSumReceived().getAndAdd(message.getPayload());
            }
            else {
                //if this is not the last node in the path, relay it and increment relay number
                //note this does not increment sent or received
                MessagingNode nextMessagingNodeInPath = message.getPath().get(thisMessagingNodesPosition + 1);

                //use socket to the next node in the path
                TCPChannel tcpChannelToNextNodeInPath = tcpChannels.get(nextMessagingNodeInPath.toString());
                tcpChannelToNextNodeInPath.getTcpSenderThread().sendData(message.getbytes());

                statisticsCollectorAndDisplay.getNumRelayed().getAndIncrement();
            }

        } else if (event instanceof MessagingNodesList) {
            MessagingNodesList messagingNodesList = (MessagingNodesList) event;

            for (MessagingNode otherMessagingNode : messagingNodesList.getMessagingNodesList()) {

                Socket socketToMessagingNodeToConnectWith = new Socket(otherMessagingNode.getIpAddress(), otherMessagingNode.getServerSocketPortNumber());
                TCPChannel tcpChannelOtherMessagingNode = new TCPChannel(socketToMessagingNodeToConnectWith, this);

                Thread receivingThread = new Thread(tcpChannelOtherMessagingNode.getTcpReceiverThread());
                receivingThread.start();

                Thread sendingThread = new Thread(tcpChannelOtherMessagingNode.getTcpSenderThread());
                sendingThread.start();

                RegisterRequest registerRequest = new RegisterRequest(this.getIpAddress(), this.getServerSocketPortNumber());
                tcpChannelOtherMessagingNode.getTcpSenderThread().sendData(registerRequest.getbytes());

                //store the new link to the other node... outgoing
                tcpChannels.put(otherMessagingNode.toString(), tcpChannelOtherMessagingNode);
            }

            System.out.println("Initiated " + messagingNodesList.getMessagingNodesList().size() + " connections... summed across all nodes, this should be N nodes * overlay connections / 2");

            //wait for all the connections to be made on all threads before accessing the tcp channels
            Thread.sleep(3000);
            System.out.println("All connections established. Number of connections: " + tcpChannels.size());

        } else if (event instanceof RegisterRequest) {

            //store the new link to the other node... incoming
            RegisterRequest registerRequest = (RegisterRequest) event;
            tcpChannels.put(registerRequest.getIpAddress() + ":" + registerRequest.getPortNumber(), tcpChannel);

            //and respond
            RegisterResponse registerResponse = new RegisterResponse((byte) 1, "Response from other messaging node: registration request to " + this.getIpAddress() + ":" + this.getServerSocketPortNumber() + " successful.");
            tcpChannel.getTcpSenderThread().sendData(registerResponse.getbytes());

        } else if (event instanceof RegisterResponse) {
            RegisterResponse registerResponse = (RegisterResponse) event;
            System.out.println(registerResponse.getAdditionalInfo());

        }  else if (event instanceof TaskInitiate) {
            TaskInitiate taskInitiate = (TaskInitiate) event;

            //check if this messaging node has a node list of other connected messaging nodes
            if (tcpChannels.size() == 0) {
                System.out.println("Please send a messaging node list from the registry before trying to send messages.");

            } else if (linkWeights.size() == 0) {
                System.out.println("Please have the overlay send link weights out to the nodes before trying to send messages.");

            } else {
                System.out.println("Received task initiate message, number of rounds is: " + taskInitiate.getNumberOfRounds());
                for (int i = 0; i < taskInitiate.getNumberOfRounds(); i++) {
                    String randomMessagingNode = getRandomMessagingNode();

                    //get cached shortest path, calculated and cached earlier when the overlay sent the linked weights to each messaging node
                    List<String> shortestPath = routingCache.get(randomMessagingNode);
                    List<MessagingNode> messagingNodesPath = convertDjikstraStringPathToMessagingNodePath(shortestPath);

                    int payload = generateRandomMaxOrMinPayload();
                    Message message = new Message(payload, messagingNodesPath);

                    //get socket to a registered node
                    MessagingNode nextNodeInPath = messagingNodesPath.get(1);
                    TCPChannel tcpChannelToNextNode = tcpChannels.get(nextNodeInPath.toString());

                    //hard code sending a message 5 times, for each round, to a random node
                    for (int j = 0; j < 5; j++) {
                        tcpChannelToNextNode.getTcpSenderThread().sendData(message.getbytes());

                        //increment sent numbers
                        statisticsCollectorAndDisplay.getNumSent().getAndIncrement();
                        statisticsCollectorAndDisplay.getSumSent().getAndAdd(payload);
                    }

                }

                //after all rounds are done, send a task complete message to the registry
                TaskComplete taskComplete = new TaskComplete(this.getIpAddress(), this.getServerSocketPortNumber());
                registryTcpChannel.getTcpSenderThread().sendData(taskComplete.getbytes());
            }

        } else if (event instanceof TaskSummaryRequest) {
            //once the registry receives a request for a traffic summary, send a traffic summary response back to the registry

            System.out.println("Sending traffic summary message to the registry");

            TaskSummaryResponse taskSummaryResponse = new TaskSummaryResponse(InetAddress.getLocalHost().getHostAddress(), serverSocketPortNumber,
                    statisticsCollectorAndDisplay.getNumSent().get(), statisticsCollectorAndDisplay.getSumSent().get(), statisticsCollectorAndDisplay.getNumReceived().get(),
                    statisticsCollectorAndDisplay.getSumReceived().get(), statisticsCollectorAndDisplay.getNumRelayed().get());

            registryTcpChannel.getTcpSenderThread().sendData(taskSummaryResponse.getbytes());

            //reset the counts for sent, relayed, received, sumsent, sumreceived
            statisticsCollectorAndDisplay.getNumSent().set(0);
            statisticsCollectorAndDisplay.getSumSent().set(0);
            statisticsCollectorAndDisplay.getNumReceived().set(0);
            statisticsCollectorAndDisplay.getSumReceived().set(0);
            statisticsCollectorAndDisplay.getNumRelayed().set(0);

        } else {
            System.out.println("Received unknown event.");
        }
    }

    private int generateRandomMaxOrMinPayload() {
        Random random = new Random();

        int payload = random.nextInt(Integer.MAX_VALUE);
        int posOrNeg = random.nextInt(2);

        payload = posOrNeg == 0 ? payload : -payload;

        return payload;
    }

    private int getThisMessagingNodesPositionInPath(List<MessagingNode> path) {
        int positionIndex = 0;
        for (int i = 0; i < path.size(); i++) {
            if (path.get(i).getIpAddress().equals(this.getIpAddress()) && path.get(i).getServerSocketPortNumber() == this.getServerSocketPortNumber()) {
                break;
            }
            positionIndex++;
        }
        return positionIndex;
    }

    //this code assumes a 129.82.44.136:47032 string like that used in the algorithm
    private List<MessagingNode> convertDjikstraStringPathToMessagingNodePath(List<String> path) {
        List<MessagingNode> messagingNodesPath = new ArrayList<>();

        for (String messagingNode : path) {
            String[] split = messagingNode.split(":");
            messagingNodesPath.add(new MessagingNode(split[0], Integer.parseInt(split[1])));
        }

        return messagingNodesPath;
    }

    private void registerWithRegistry() throws IOException, InterruptedException {

        Socket socketToHost = new Socket(this.registryIPAddress, this.registryPortNumber);
        TCPChannel tcpChannel = new TCPChannel(socketToHost, this);

        Thread receivingThread = new Thread(tcpChannel.getTcpReceiverThread());
        receivingThread.start();

        Thread sendingThread = new Thread(tcpChannel.getTcpSenderThread());
        sendingThread.start();

        registryTcpChannel = tcpChannel;

        System.out.println("Sending Register message from messaging node to Registry");

        RegisterRequest registerRequest = new RegisterRequest(this.getIpAddress(), this.getServerSocketPortNumber());
        registryTcpChannel.getTcpSenderThread().sendData(registerRequest.getbytes());
    }

    public String getRandomMessagingNode() {

        List<String> messagingNodeList = new ArrayList<>(allOtherMessagingNodes);
        int randomIndex = new Random().nextInt(messagingNodeList.size());

        return messagingNodeList.get(randomIndex);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        MessagingNode messagingNodeObject = (MessagingNode) object;
        return serverSocketPortNumber == messagingNodeObject.serverSocketPortNumber && Objects.equals(ipAddress, messagingNodeObject.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress, serverSocketPortNumber);
    }

    @Override
    public String toString() {
        return ipAddress + ":" + serverSocketPortNumber;
    }
}
