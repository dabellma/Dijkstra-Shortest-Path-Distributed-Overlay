package CS555.overlay.dijkstra;

public class DjikstraVertex {
    private String key;
    private int weight;

    DjikstraVertex(String key, int weight) {
        this.key = key;
        this.weight = weight;
    }

    public String getKey() {
        return this.key;
    }
    public int getWeight() {
        return this.weight;
    }

}
