package CS555.overlay.wireformats;

import java.io.*;

public class TaskSummaryRequest implements Event {

    private int messageType;

    public TaskSummaryRequest() {
        this.messageType = Protocol.PULL_TRAFFIC_SUMMARY.getValue();
    }

    public TaskSummaryRequest(byte[] incomingByteArray) throws IOException {

        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingByteArray);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);

        int messageType = dataInputStream.readInt();

        dataInputStream.close();
        baInputStream.close();
    }

    public int getMessageType() {
        return this.messageType;
    }

    @Override
    public byte[] getbytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dataOutputStream.writeInt(Protocol.PULL_TRAFFIC_SUMMARY.getValue());

        dataOutputStream.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }
}
