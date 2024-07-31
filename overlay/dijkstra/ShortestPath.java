package CS555.overlay.dijkstra;

import CS555.overlay.node.MessagingNode;
import CS555.overlay.wireformats.LinkInfo;

import java.util.*;

public class ShortestPath {

    //This assumes the input link infos are already bidirectional
    //This takes in a string for the start and end messaging nodes
    //example 129.82.44.136:47032
    //which will be used throughout the algorithm
    //Also, messagingNode.toString() returns this format also (129.82.44.136:47032)
    //it's mainly used here as a key for the graph

    //this implementation of djikstra's algorithm guarantees no cycles (no messaging node will receive the same packet more than once)
    //because of the "already visited" collection

    public List<String> getDijsktraPath(String start, String finish, Set<LinkInfo> linkInfos) {
        Map<String, List<LinkInfo>> initialGraph = new HashMap<>();
        Map<String, Integer> distances = new HashMap<>();

        PriorityQueue<DjikstraVertex> minHeap = new PriorityQueue<>(Comparator.comparingInt(DjikstraVertex::getWeight));

        Set<String> alreadyVisited = new HashSet<>();
        Map<String, String> predecessors = new HashMap<>();

        List<String> path = new ArrayList<>();

        //create initial graph from all the link weights
        for (LinkInfo linkInfo : linkInfos) {
            initialGraph.computeIfAbsent(new MessagingNode(linkInfo.getIpAddressA(), linkInfo.getPortNumberA()).toString(), key -> new ArrayList<>()).add(linkInfo);
        }

        //initialization of start node distance to 0, other nodes' distances to max value, heap to start node with weight of 0
        for (String messagingNode : initialGraph.keySet()) {
            if (messagingNode.equals(start)) {
                distances.put(messagingNode, 0);
                minHeap.add(new DjikstraVertex(messagingNode, 0));
            } else {
                distances.put(messagingNode, Integer.MAX_VALUE);
            }
        }

        while (!minHeap.isEmpty()) {
            DjikstraVertex currentDjikstraVertex = minHeap.poll();
            alreadyVisited.add(currentDjikstraVertex.getKey());

            if (currentDjikstraVertex.getKey().equals(finish)) {
                break;
            }

            int currentDistance = distances.get(currentDjikstraVertex.getKey());

            //from initial graph get all the neighbors... which are really just the "B"/opposite part of the link info object I created and am using in this function
            for (LinkInfo neighbor : initialGraph.get(currentDjikstraVertex.getKey())) {
                String neighborKey = currentDjikstraVertex.getKey().equals(new MessagingNode(neighbor.getIpAddressA(), neighbor.getPortNumberA()).toString()) ? new MessagingNode(neighbor.getIpAddressB(), neighbor.getPortNumberB()).toString() : new MessagingNode(neighbor.getIpAddressA(), neighbor.getPortNumberA()).toString();
                if (!alreadyVisited.contains(neighborKey)) {
                    int newDistance = currentDistance + neighbor.getWeight();
                    if (newDistance < distances.get(neighborKey)) {
                        distances.put(neighborKey, newDistance);
                        predecessors.put(neighborKey, currentDjikstraVertex.getKey());
                        minHeap.add(new DjikstraVertex(neighborKey, newDistance));
                    }
                }
            }
        }

        if (predecessors.containsKey(finish)) {
            for (String node = finish; node != null; node = predecessors.get(node)) {
                path.add(node);
            }
            Collections.reverse(path);
        }

        return path;
    }
}
