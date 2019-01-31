package cn.jimmiez.pcu.model;


import javax.vecmath.Point3d;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jimmiez.pcu.common.graph.EntityGraph;
import cn.jimmiez.pcu.common.graph.UndirectedGraph;
import cn.jimmiez.pcu.io.ply.PlyPropertyType;
import cn.jimmiez.pcu.io.ply.ReadFromPly;
import cn.jimmiez.pcu.io.ply.WriteListToPly;
import cn.jimmiez.pcu.io.ply.WriteScalarToPly;
import cn.jimmiez.pcu.util.Pair;

/**
 * Use a undirected acyclic graph for representing a curve skeleton
 */
public class Skeleton extends EntityGraph<Point3d> {

    public Skeleton() {
        super(false);
    }

//    @WriteScalarToPly(element = "nodes", properties = {"x", "y", "z"}, type = PlyPropertyType.DOUBLE)
//    public List<double[]> nodes() {
//        List<double[]> result = new ArrayList<>();
//        for (Point3d p : values.values()) {
//            result.add(new double[] {p.x, p.y, p.z});
//        }
//        return result;
//    }
//
//    @WriteListToPly(element = "edges", property = "node_index")
//    public List<int[]> edges() {
//        List<int[]> result = new ArrayList<>();
//        for (Integer id : values.keySet()) {
//            Collection<Integer> edges = adjacentVertices(id);
//            Map<Integer, Double> edge = edges.get(vi);
//            int[] edgesVi = new int[edge.size()];
//            int cnt = 0;
//            for (int vj : edge.keySet()) {
//                edgesVi[cnt ++] = vj;
//            }
//            result.add(edgesVi);
//        }
//        return result;
//
//    }

}
