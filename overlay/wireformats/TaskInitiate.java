package CS555.overlay.wireformats;

import java.io.*;

public class TaskInitiate implements Event {
    private int messageType;
    private int numberOfRounds;

    public TaskInitiate(int numberOfRounds) {
        this.messageType = Protocol.TASK_INITIATE.getValue();
        this.numberOfRounds = numberOfRounds;
    }

    public TaskInitiate(byte[] incomingByteArray) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingByteArray);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);

        int messageType = dataInputStream.readInt();

        int numberOfRounds = dataInputStream.readInt();

        dataInputStream.close();
        baInputStream.close();

        this.numberOfRounds = numberOfRounds;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public int getNumberOfRounds() {
        return this.numberOfRounds;
    }

    @Override
    public byte[] getbytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dataOutputStream.writeInt(Protocol.TASK_INITIATE.getValue());

        dataOutputStream.writeInt(numberOfRounds);

        dataOutputStream.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }
}
