package CS555.overlay.transport;

import CS555.overlay.node.MessagingNode;
import CS555.overlay.node.Node;
import CS555.overlay.node.Registry;
import CS555.overlay.wireformats.Event;
import CS555.overlay.wireformats.EventFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class TCPReceiverThread implements Runnable {

    private Socket socket;
    private DataInputStream dataInputStream;
    private Node node;
    private TCPChannel tcpChannel;

    //when this is created from the server node,
    //the socket will be the node that called the registry/server in the first case,
    //and the node will be the registry/server in the first place

    public TCPReceiverThread(Socket socket, Node node, TCPChannel tcpChannel) throws IOException {
        this.socket = socket;
        this.node = node;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.tcpChannel = tcpChannel;

    }

    public DataInputStream getDataInputStream() {
        return this.dataInputStream;
    }

    @Override
    public void run() {
        while (socket != null) {
            try {

                //readInt is a blocking call
                //for this project, the protocol is to receive the length first
                int msgLength = dataInputStream.readInt();
                byte[] incomingMessage = new byte[msgLength];
                dataInputStream.readFully(incomingMessage, 0, msgLength);

                EventFactory eventFactory = EventFactory.getInstance();

                Event event = eventFactory.createEvent(incomingMessage);
                node.onEvent(event, tcpChannel);

            } catch (IOException | InterruptedException exception) {

                if (node instanceof Registry) {
                    Registry registryInstance = (Registry) node;
                    String messagingNodeToRemove = null;
                    for (Map.Entry<String, TCPChannel> messagingNode : registryInstance.getTCPChannels().entrySet()) {
                        if (messagingNode.getValue().equals(tcpChannel)) {
                            messagingNodeToRemove = messagingNode.getKey();
                        }
                    }

                    if (messagingNodeToRemove != null) {
                        System.out.println("Removing " + messagingNodeToRemove);
                        registryInstance.getTCPChannels().remove(messagingNodeToRemove);
                    }
                }
                break;
            }
        }
    }
}
