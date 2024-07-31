package CS555.overlay.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OverlayCreator {

    public OverlayCreator() {
    }

    //Note: this function ensures there's no partitions by the way the algorithm is designed
    public Map<Integer, Set<Integer>> createDesiredLinksPerVertex(int numVertices, int numDesiredEdges) {

        if (numVertices <= numDesiredEdges) {
            System.out.println("Need at least 1 more vertex than the number of edges... For example, for 8 nodes the max is 7 connections each");
            return Map.of();
        }

        if ((numVertices * numDesiredEdges) % 2 == 1) {
            System.out.println("Num vertices times num edges must be even for this to work");
            return Map.of();
        }

        //if passing gate checks, create initial map
        Map<Integer, Set<Integer>> overlayEdgesForEachVertex = new HashMap<>();
        for (int vertexNumber = 0; vertexNumber < numVertices; vertexNumber++) {
            overlayEdgesForEachVertex.put(vertexNumber, new HashSet<>());
        }

        //get number of neighbors needed to connect to on either side from each vertex
        int numberOfNeighbors = numDesiredEdges / 2;

        //loop through all vertices
        for (int vertex = 0; vertex < numVertices; vertex++) {
            //loop through the number of neighbors to add by each vertex
            for (int neighbor = 1; neighbor <= numberOfNeighbors; neighbor++) {
                int neighborOne = (vertex + neighbor) % numVertices;
                int neighborTwo = (vertex - neighbor + numVertices) % numVertices;

                overlayEdgesForEachVertex.get(vertex).add(neighborOne);
                overlayEdgesForEachVertex.get(vertex).add(neighborTwo);
            }
        }

        //if numDesiredEdges is odd, need the opposites from a circular graph as well
        if ((numDesiredEdges % 2) != 0) {
            for (int vertex = 0; vertex < numVertices; vertex++) {
                int oppositeNode = (vertex + numVertices / 2) % numVertices;
                overlayEdgesForEachVertex.get(vertex).add(oppositeNode);
            }
        }

        return overlayEdgesForEachVertex;
    }
}
