package CS555.overlay.wireformats;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RegisterRequest implements Event {

    private int messageType;
    private String ipAddress;
    private int portNumber;

    public RegisterRequest(String ipAddress, int portNumber) {
        this.messageType = Protocol.REGISTER_REQUEST.getValue();
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

    public RegisterRequest(byte[] incomingByteArray) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingByteArray);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);

        int messageType = dataInputStream.readInt();

        //deserialize ip address
        int ipAddressLength = dataInputStream.readInt();
        byte[] ipAddressBytes = new byte[ipAddressLength];
        dataInputStream.readFully(ipAddressBytes);
        String ipAddress = new String(ipAddressBytes, StandardCharsets.UTF_8);

        //deserialize port number
        int portNumber = dataInputStream.readInt();

        dataInputStream.close();
        baInputStream.close();

        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public int getPortNumber() {
        return this.portNumber;
    }


    @Override
    public byte[] getbytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dataOutputStream.writeInt(Protocol.REGISTER_REQUEST.getValue());

        //serialize ip address
        byte[] hostNameBytes = ipAddress.getBytes(StandardCharsets.UTF_8);
        int byteStringLength = hostNameBytes.length;
        dataOutputStream.writeInt(byteStringLength);
        dataOutputStream.write(hostNameBytes);

        //serializes port number
        dataOutputStream.writeInt(portNumber);

        dataOutputStream.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }
}
