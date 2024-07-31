package CS555.overlay.node;

import CS555.overlay.transport.TCPChannel;
import CS555.overlay.wireformats.Event;

import java.io.IOException;

public interface Node {
    public void onEvent(Event event, TCPChannel tcpChannel) throws IOException, InterruptedException;

}
