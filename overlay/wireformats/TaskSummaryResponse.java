package CS555.overlay.wireformats;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TaskSummaryResponse implements Event {

    private int messageType;
    private String ipAddress;
    private int portNumber;

    private int numSent;
    private long sumSent;
    private int numReceived;
    private long sumReceived;
    private int numRelayed;

    public TaskSummaryResponse(String ipAddress, int portNumber, int numSent, long sumSent, int numReceived, long sumReceived, int numRelayed) {
        this.messageType = Protocol.TRAFFIC_SUMMARY.getValue();
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.numSent = numSent;
        this.sumSent = sumSent;
        this.numReceived = numReceived;
        this.sumReceived = sumReceived;
        this.numRelayed = numRelayed;
    }

    public TaskSummaryResponse(byte[] incomingByteArray) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingByteArray);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);

        int messageType = dataInputStream.readInt();

        int ipAddressLength = dataInputStream.readInt();
        byte[] ipAddressBytes = new byte[ipAddressLength];
        dataInputStream.readFully(ipAddressBytes);
        String ipAddress = new String(ipAddressBytes, StandardCharsets.UTF_8);

        int portNumber = dataInputStream.readInt();

        int numSent = dataInputStream.readInt();
        long sumSent = dataInputStream.readLong();
        int numReceived = dataInputStream.readInt();
        long sumReceived = dataInputStream.readLong();
        int numRelayed = dataInputStream.readInt();

        dataInputStream.close();
        baInputStream.close();

        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.numSent = numSent;
        this.sumSent = sumSent;
        this.numReceived = numReceived;
        this.sumReceived = sumReceived;
        this.numRelayed = numRelayed;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }
    public int getPortNumber() {
        return this.portNumber;
    }
    public int getNumSent() {
        return this.numSent;
    }
    public long getSumSent() {
        return this.sumSent;
    }
    public int getNumReceived() {
        return this.numReceived;
    }
    public long getSumReceived() {
        return this.sumReceived;
    }
    public int getNumRelayed() {
        return this.numRelayed;
    }

    @Override
    public byte[] getbytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //for this project, I'm testing the protocol to send the type
        //first, so the receiver thread can process the event
        dataOutputStream.writeInt(Protocol.TRAFFIC_SUMMARY.getValue());

        byte[] ipAddressBytes = ipAddress.getBytes(StandardCharsets.UTF_8);
        int byteStringLength = ipAddressBytes.length;
        dataOutputStream.writeInt(byteStringLength);
        dataOutputStream.write(ipAddressBytes);

        dataOutputStream.writeInt(portNumber);

        dataOutputStream.writeInt(numSent);
        dataOutputStream.writeLong(sumSent);
        dataOutputStream.writeInt(numReceived);
        dataOutputStream.writeLong(sumReceived);
        dataOutputStream.writeInt(numRelayed);

        dataOutputStream.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }
}
