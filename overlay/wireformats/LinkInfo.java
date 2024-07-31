package CS555.overlay.wireformats;

public class LinkInfo {

    private String ipAddressA;
    private String ipAddressB;
    private int portNumberA;
    private int portNumberB;
    private int weight;

    public LinkInfo(String ipAddressA, String ipAddressB, int portNumberA, int portNumberB, int weight) {
        this.ipAddressA = ipAddressA;
        this.ipAddressB = ipAddressB;
        this.portNumberA = portNumberA;
        this.portNumberB = portNumberB;
        this.weight = weight;
    }

    public String getIpAddressA() {
        return this.ipAddressA;
    }
    public String getIpAddressB() {
        return this.ipAddressB;
    }
    public int getPortNumberA() {
        return this.portNumberA;
    }
    public int getPortNumberB() {
        return this.portNumberB;
    }
    public int getWeight() {
        return this.weight;
    }


}
