package CS555.overlay.wireformats;

public enum Protocol {
    REGISTER_REQUEST(0),
    DEREGISTER_REQUEST(1),
    REGISTER_RESPONSE(2),
    DEREGISTER_RESPONSE(3),
    MESSAGING_NODES_LIST(4),
    MESSAGE(5),
    TASK_INITIATE(6),
    LINK_WEIGHTS(7),
    TASK_COMPLETE(8),
    PULL_TRAFFIC_SUMMARY(9),
    TRAFFIC_SUMMARY(10);

    private final int value;

    Protocol(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
