package CS555.overlay.wireformats;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class EventFactory {
    private static final EventFactory instance = new EventFactory();

    private EventFactory() {}

    public static EventFactory getInstance() {
        return instance;
    }

    public Event createEvent(byte[] incomingMessage) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingMessage);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);

        //protocol that I always send what type of message it is to then process it
        int messageType = dataInputStream.readInt();

        if (messageType == Protocol.REGISTER_REQUEST.getValue()) {
            RegisterRequest registerRequest = new RegisterRequest(incomingMessage);
            return registerRequest;
        } else if (messageType == Protocol.DEREGISTER_REQUEST.getValue()) {

            DeregisterRequest deregisterRequest = new DeregisterRequest(incomingMessage);
            return deregisterRequest;
        } else if (messageType == Protocol.REGISTER_RESPONSE.getValue()) {
            RegisterResponse registerResponse = new RegisterResponse(incomingMessage);
            return registerResponse;
        } else if (messageType == Protocol.DEREGISTER_RESPONSE.getValue()) {
            DeregisterResponse deregisterResponse = new DeregisterResponse(incomingMessage);
            return deregisterResponse;
        } else if (messageType == Protocol.MESSAGING_NODES_LIST.getValue()) {
            MessagingNodesList messagingNodesList = new MessagingNodesList(incomingMessage);
            return messagingNodesList;
        } else if (messageType == Protocol.MESSAGE.getValue()) {
            Message message = new Message(incomingMessage);
            return message;
        } else if (messageType == Protocol.TASK_INITIATE.getValue()) {
            TaskInitiate taskInitiate = new TaskInitiate(incomingMessage);
            return taskInitiate;
        } else if (messageType == Protocol.LINK_WEIGHTS.getValue()) {
            LinkWeights linkWeights = new LinkWeights(incomingMessage);
            return linkWeights;
        } else if (messageType == Protocol.TASK_COMPLETE.getValue()) {
            TaskComplete taskComplete = new TaskComplete(incomingMessage);
            return taskComplete;

        } else if (messageType == Protocol.PULL_TRAFFIC_SUMMARY.getValue()) {
            TaskSummaryRequest taskSummaryRequest = new TaskSummaryRequest();
            return taskSummaryRequest;
        } else if (messageType == Protocol.TRAFFIC_SUMMARY.getValue()) {
            TaskSummaryResponse taskSummaryResponse = new TaskSummaryResponse(incomingMessage);
            return taskSummaryResponse;
        } else {
            System.out.println("Unrecognized event in event factory");

        }

        return null;
    }

}
