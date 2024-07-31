package CS555.overlay.wireformats;

import CS555.overlay.node.MessagingNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Message implements Event {

    private int messageType;
    private int payload;
    private List<MessagingNode> path;

    public Message(int payload, List<MessagingNode> path) {
        this.messageType = Protocol.MESSAGE.getValue();
        this.payload = payload;
        this.path = path;
    }

    public Message(byte[] incomingByteArray) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingByteArray);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);
        List<MessagingNode> path = new ArrayList<>();

        int messageType = dataInputStream.readInt();

        int payload = dataInputStream.readInt();

        int pathSize = dataInputStream.readInt();

        for (int i = 0; i < pathSize; i++) {

            int ipAddressSize = dataInputStream.readInt();
            byte[] ipAddressBytes = new byte[ipAddressSize];
            dataInputStream.readFully(ipAddressBytes);
            String ipAddress = new String(ipAddressBytes, StandardCharsets.UTF_8);

            int portNumber = dataInputStream.readInt();

            path.add(new MessagingNode(ipAddress, portNumber));
        }

        dataInputStream.close();
        baInputStream.close();

        this.payload = payload;
        this.path = path;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public int getPayload() {
        return this.payload;
    }

    public List<MessagingNode> getPath() {
        return this.path;
    }

    @Override
    public byte[] getbytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dataOutputStream.writeInt(Protocol.MESSAGE.getValue());

        dataOutputStream.writeInt(payload);

        //send size of the path, but don't keep it in the message object
        dataOutputStream.writeInt(path.size());

        for (MessagingNode messagingNode : path) {

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
