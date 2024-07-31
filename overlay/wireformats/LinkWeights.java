package CS555.overlay.wireformats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class LinkWeights implements Event {

    private int messageType;
    private int numberOfLinks;
    private Set<LinkInfo> linkWeights;

    public LinkWeights(int numberOfLinks, Set<LinkInfo> linkWeights) {
        this.messageType = Protocol.LINK_WEIGHTS.getValue();
        this.numberOfLinks = numberOfLinks;
        this.linkWeights = linkWeights;
    }

    public LinkWeights(byte[] incomingByteArray) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(incomingByteArray);
        DataInputStream dataInputStream = new DataInputStream(baInputStream);
        Set<LinkInfo> linkWeights = new HashSet<>();

        int messageType = dataInputStream.readInt();

        int numberOfLinks = dataInputStream.readInt();

        for (int i = 0; i < numberOfLinks; i++) {

            int ipAddressASize = dataInputStream.readInt();
            byte[] ipAddressABytes = new byte[ipAddressASize];
            dataInputStream.readFully(ipAddressABytes);
            String ipAddressA = new String(ipAddressABytes, StandardCharsets.UTF_8);

            int ipAddressBSize = dataInputStream.readInt();
            byte[] ipAddressBBytes = new byte[ipAddressBSize];
            dataInputStream.readFully(ipAddressBBytes);
            String ipAddressB = new String(ipAddressBBytes, StandardCharsets.UTF_8);

            int portNumberA = dataInputStream.readInt();
            int portNumberB = dataInputStream.readInt();
            int weight = dataInputStream.readInt();

            linkWeights.add(new LinkInfo(ipAddressA, ipAddressB, portNumberA, portNumberB, weight));
        }

        dataInputStream.close();
        baInputStream.close();

        this.numberOfLinks = numberOfLinks;
        this.linkWeights = linkWeights;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public int getNumberOfLinks() {
        return this.numberOfLinks;
    }

    public Set<LinkInfo> getLinkWeights() {
        return this.linkWeights;
    }

    @Override
    public byte[] getbytes() throws IOException {
        byte[] marshalledBytes;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream =
                new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dataOutputStream.writeInt(Protocol.LINK_WEIGHTS.getValue());

        dataOutputStream.writeInt(numberOfLinks);

        for (LinkInfo linkInfo : linkWeights) {
            byte[] ipAddressABytes = linkInfo.getIpAddressA().getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeInt(ipAddressABytes.length);
            dataOutputStream.write(ipAddressABytes);

            byte[] ipAddressBBytes = linkInfo.getIpAddressB().getBytes(StandardCharsets.UTF_8);
            dataOutputStream.writeInt(ipAddressBBytes.length);
            dataOutputStream.write(ipAddressBBytes);

            dataOutputStream.writeInt(linkInfo.getPortNumberA());

            dataOutputStream.writeInt(linkInfo.getPortNumberB());

            dataOutputStream.writeInt(linkInfo.getWeight());
        }


        dataOutputStream.flush();
        marshalledBytes = baOutputStream.toByteArray();
        baOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }
}
