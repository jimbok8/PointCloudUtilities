package cn.jimmiez.pcu.common.graph;


import cn.jimmiez.pcu.model.Pair;

import java.util.*;

public class ShortestPath {

    /**
     * dijkstra algorithm
     * @param graph abstract graph
     * @param rootIndex index of root vertex
     * @return list of pairs, for each pair, the key is the path, the value is length of shortest path
     */
    public static Map<Integer, Pair<List<Integer>, Double>> dijkstra(GraphStatic graph, int rootIndex) {
        Map<Integer, Pair<List<Integer>, Double>> result = new HashMap<>();
        if (rootIndex < 0 || rootIndex >= graph.vertices().size()) {
            throw new IllegalArgumentException("Invalid root index");
        }
        Set<Integer> sSet = new HashSet<>();
        Set<Integer> sExcluded = new HashSet<>();
        Map<Integer, Integer> prev = new HashMap<>();

        for (int i : graph.vertices()) {
            sExcluded.add(i);
            result.put(i, new Pair<List<Integer>, Double>(new Vector<Integer>(), Double.POSITIVE_INFINITY));
        }
        result.get(rootIndex).setValue(0d);

        while (sSet.size() < graph.vertices().size()) {
            int nearestVertexIndex = -1;
            double shortestPathLen = Double.POSITIVE_INFINITY;
            for (int index : sExcluded) {
                if (result.get(index).getValue() < shortestPathLen) {
                    shortestPathLen = result.get(index).getValue();
                    nearestVertexIndex = index;
                }
            }
            sSet.add(nearestVertexIndex);
            sExcluded.remove(nearestVertexIndex);
            if (prev.get(nearestVertexIndex) != null) {
                int prevNodeIndex = prev.get(nearestVertexIndex);
                result.get(nearestVertexIndex).getKey().addAll(result.get(prevNodeIndex).getKey());
            }
            result.get(nearestVertexIndex).getKey().add(nearestVertexIndex);
            for (int index : sExcluded) {
                double weightSum = shortestPathLen + graph.edgeWeight(nearestVertexIndex, index);
                if (weightSum < result.get(index).getValue()) {
//                    result.get(index).getKey().add(nearestVertexIndex);
                    prev.put(index, nearestVertexIndex);
                    result.get(index).setValue(weightSum);
                }
            }
        }

        return result;
    }
}
