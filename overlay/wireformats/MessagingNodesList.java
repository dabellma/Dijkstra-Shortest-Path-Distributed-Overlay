package CS555.overlay.wireformats;

import CS555.overlay.node.MessagingNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class MessagingNodesList implements Event {


    private int messageType;
    private int messagingNodesListSize;
    private Set<MessagingNode> messagingNodesList;

    public MessagingNodesList(int messagingNodesListSize, Set<MessagingNode> messagingNodesList) {
        this.messageType = Protocol.MESSAGING_NODES_LIST.getValue();
        this.messagingNodesListSize = messagingNodesListSize;
        this.messagingNodesList = messagingNodesList;
    }

    public MessagingNodesList(byte[] incomingByteArray) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingByteArray);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);
        Set<MessagingNode> messagingNodesList = new HashSet<>();

        int messageType = dataInputStream.readInt();

        int messagingNodesListSize = dataInputStream.readInt();

        for (int i = 0; i < messagingNodesListSize; i++) {

            int ipAddressSize = dataInputStream.readInt();
            byte[] ipAddressBytes = new byte[ipAddressSize];
            dataInputStream.readFully(ipAddressBytes);
            String ipAddressA = new String(ipAddressBytes, StandardCharsets.UTF_8);

            int portNumber = dataInputStream.readInt();

            messagingNodesList.add(new MessagingNode(ipAddressA, portNumber));
        }

        dataInputStream.close();
        baInputStream.close();

        this.messagingNodesListSize = messagingNodesListSize;
        this.messagingNodesList = messagingNodesList;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public int getMessagingNodesListSize() {
        return this.messagingNodesListSize;
    }

    public Set<MessagingNode> getMessagingNodesList() {
        return this.messagingNodesList;
    }


    @Override
    public byte[] getbytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dataOutputStream.writeInt(Protocol.MESSAGING_NODES_LIST.getValue());

        dataOutputStream.writeInt(messagingNodesListSize);

        for (MessagingNode messagingNode : messagingNodesList) {
            byte[] ipAddressBytes = messagingNode.getIpAddress().getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeInt(ipAddressBytes.length);
            dataOutputStream.write(ipAddressBytes);

            dataOutputStream.writeInt(messagingNode.getServerSocketPortNumber());

        }

        dataOutputStream.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }
}
