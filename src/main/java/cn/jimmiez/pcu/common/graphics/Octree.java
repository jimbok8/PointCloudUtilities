package cn.jimmiez.pcu.common.graphics;


import javafx.util.Pair;

import javax.vecmath.Point3d;
import java.util.*;
import static cn.jimmiez.pcu.util.PcuVectorUtil.*;
import static java.lang.Math.*;

/**
 * An octree can be used to build spatial index for point cloud.
 */
public class Octree {

    /**
     * the root node of the octree,
     * the size of root node is the bounding box of point cloud
     **/
    OctreeNode root = null;

    private List<Point3d> points = null;

    /**
     * used to find an octree node by its index
     * the index of an octree node is generated according to its spatial position
     */
    protected Map<Long, OctreeNode> octreeIndices = new HashMap<>();

    /** the depth of this tree **/
    private int depth = 1;

    /**
     * the max depth of this tree,
     * theoretically it shall be less than 21, because every three bits in a Long-type
     * octree-node index is used to locate its position of siblings.
     **/
    private static final int MAX_DEPTH = 10;

    /**
     * build spatial index for point cloud
     * note that the length double array in List points must be 3
     * @param points the point cloud
     */
    public void buildIndex(List<Point3d> points) {
        this.depth = (int) (Math.ceil((Math.log((points.size() + 1) / 64) / Math.log(8))) + 1);
        this.depth = min(this.depth, MAX_DEPTH);
        this.depth = max(this.depth, 1);

        this.octreeIndices.clear();

        this.points = points;
        this.root = new OctreeNode();

        if (points.size() < 1) {
            System.err.println("Warning: input for buildIndex() is empty list.");
            return;
        }
        this.root.minX = points.get(0).x;
        this.root.maxX = points.get(0).x;
        this.root.minY = points.get(0).y;
        this.root.maxY = points.get(0).y;
        this.root.minZ = points.get(0).z;
        this.root.maxZ = points.get(0).z;
        for (Point3d xyz: points) {
            this.root.minX = min(xyz.x, this.root.minX);
            this.root.maxX = max(xyz.x, this.root.maxX);
            this.root.minY = min(xyz.y, this.root.minY);
            this.root.maxY = max(xyz.y, this.root.maxY);
            this.root.minZ = min(xyz.z, this.root.minZ);
            this.root.maxZ = max(xyz.z, this.root.maxZ);
        }

        createOctree(1, this.root);

        for (int i = 0; i < points.size(); i ++) {
            Point3d xyz = points.get(i);
            this.root.addPoint(xyz, i);
        }
    }

    private void createOctree(int currentDepth, OctreeNode currentNode) {
        if (currentDepth == this.depth) {
            currentNode.indices = new ArrayList<>();
            if (currentNode == this.root) this.octreeIndices.put(this.root.index, this.root);
            return;
        }
        currentNode.children = new OctreeNode[8];
        int cnt = 0;
        for (int i : new int[]{-1, 1}) {
            for (int j : new int[]{-1, 1}) {
                for (int k : new int[]{-1, 1}) {
                    long index = (long) (((i + 1) * 2 + (j + 1) * 1 + (k + 1) / 2));
//                    index <<= (currentDepth - 1) * 3;
                    index |= (currentNode.index << 3);
                    OctreeNode node = new OctreeNode();
                    currentNode.children[cnt] = node;
                    node.index = index;
                    node.minX = currentNode.minX + (i + 1) / 2 * (currentNode.maxX - currentNode.minX) / 2;
                    node.maxX = currentNode.maxX + (i - 1) / 2 * (currentNode.maxX - currentNode.minX) / 2;
                    node.minY = currentNode.minY + (j + 1) / 2 * (currentNode.maxY - currentNode.minY) / 2;
                    node.maxY = currentNode.maxY + (j - 1) / 2 * (currentNode.maxY - currentNode.minY) / 2;
                    node.minZ = currentNode.minZ + (k + 1) / 2 * (currentNode.maxZ - currentNode.minZ) / 2;
                    node.maxZ = currentNode.maxZ + (k - 1) / 2 * (currentNode.maxZ - currentNode.minZ) / 2;
                    if (currentDepth + 1 == this.depth) this.octreeIndices.put(index, node);
                    createOctree(currentDepth + 1, node);
                    cnt += 1;
                }
            }

        }
    }

    /**
     * If you want to acquire the k-nearest neighbors of a certain point p, call this function,
     * octree will decrease the time cost
     * @param k the number of nearest neighbors
     * @param index the index of point p
     * @return the indices of nearest neighbors
     */
    public int[] searchNearestNeighbors(int k, int index) {
        if (points == null) throw new IllegalStateException("Octree.buildIndex() must be called before searchNearestNeighbors.");
        if (k >= this.points.size()) throw new IllegalArgumentException("number of nearest neighbors is larger than data size");

        List<Pair<Integer, Double>> nearest = new ArrayList<>();

        Point3d point = points.get(index);
        long leafNode = locateOctreeNode(this.root, point);
        List<Long> adjacentLeaves = obtainAdjacent26Indices(leafNode);
        List<Long> candidateLeaves = new ArrayList<>();
//        System.out.println("adjacent leaves: " + adjacentLeaves.size());
        candidateLeaves.add(leafNode);
        candidateLeaves.addAll(adjacentLeaves);

        adjacentOctreeNodesIndices(candidateLeaves, k);
//        System.out.println("candidate leaves: " + candidateLeaves.size());

        for (long leafIndex : candidateLeaves) {
            int[] coords = index2Coordinates(leafIndex);
//            System.out.println("x: " + coords[0] + " y: " + coords[1] + " z: " + coords[2]);
            for (int pointIndex : this.octreeIndices.get(leafIndex).indices) {
                if (pointIndex == index) continue;
                Point3d p = this.points.get(pointIndex);
                double distance = p.distance(point);
                if (nearest.size() < k) {
                    nearest.add(new Pair<>(pointIndex, distance));
                    if (nearest.size() == k) Collections.sort(nearest, new Comparator<Pair<Integer, Double>>() {
                        @Override
                        public int compare(Pair<Integer, Double> o1, Pair<Integer, Double> o2) {
                            return o1.getValue() > o2.getValue() ? 1 : -1;
                        }
                    });
                } else if (distance < nearest.get(k - 1).getValue()){
                    int nearestIndex;
                    for (nearestIndex = k - 1; nearestIndex >= 1; nearestIndex --) {
                        if (distance > nearest.get(nearestIndex - 1).getValue()) break;
                    }
                    nearest.add(nearestIndex, new Pair<>(pointIndex, distance));
                    nearest.remove(k);
                }
            }
        }

        int[] indices = new int[k];
        for (int i = 0; i < k; i ++) {
            indices[i] = nearest.get(i).getKey();
//            System.out.println("distance: " + nearest.get(i).getValue());
        }
        return indices;
    }

    /**
     * find the index of octree node in which the target point is located
     * @param node the root octree node
     * @param point the target point
     * @return the index of leaf node
     */
    protected Long locateOctreeNode(OctreeNode node, Point3d point) {
        if (node.children == null) {
            if (node.contains(point)) {
                return node.index;
            } else {
                throw new IllegalStateException("Search a point exceeding octree bounds.");
            }
        } else {
            int xi = point.x < (node.maxX + node.minX) / 2 ? 0 : 1;
            int yj = point.y < (node.maxY + node.minY) / 2 ? 0 : 1;
            int zk = point.z < (node.maxZ + node.minZ) / 2 ? 0 : 1;
            int childIndex = xi * 4 + yj * 2 + zk * 1;
            return locateOctreeNode(node.children[childIndex], point);
        }
    }

    /**
     * when searching k nearest neighbors of one point in point cloud, the point
     * may be `closed to` the boundary of the octree leaf node, so we need to search
     * adjacent nodes as well.
     *
     *
     * @param leafIndices index of leaf node in which the point is located
     * @param k number of nearest neighbors
     */
    private void adjacentOctreeNodesIndices(List<Long> leafIndices, int k) {
        int num = 0;
        for (long leafIndex : leafIndices) {
            num += this.octreeIndices.get(leafIndex).indices.size();
        }
//        System.out.println("num: " + num);
        if (num > k) return;
        Set<Long> indicesSet = new HashSet<>(leafIndices);
        for (Long leafIndex : leafIndices) {
            List<Long> adjacent26 = obtainAdjacent26Indices(leafIndex);
//            System.out.println("adjacent: " + adjacent26.size());
            indicesSet.addAll(adjacent26);
        }
        leafIndices.clear();
        leafIndices.addAll(indicesSet);
        adjacentOctreeNodesIndices(leafIndices, k);
    }

    /**
     *
     * interpreter the index to three-dimensional Cartesian coordinates
     * high bits   ----   ---- low bits
     * root index  ----   ---- leaf index
     * eg. if index is (101 011 001 101)
     * the coord is [(1001), (0100), (1111)]
     * @param index index of an octree node
     * @return  3-d coordinate of octree node
     **/
    protected int[] index2Coordinates(Long index) {
        int []coord = new int[3];//x y z
        coord[0] = 0; coord[1] = 0; coord[2] = 0;
        /** from left to right **/
        for (int i = depth; i > 1; i --) {
            Long temp = (index >> (i - 2) * 3) & 7L;
            coord[0] += ((temp >> 2) & 1L) << (i - 2);
            coord[1] += ((temp >> 1) & 1L) << (i - 2);
            coord[2] += ((temp >> 0) & 1L) << (i - 2);
        }
        return coord;
    }

    /**
     *
     * root index  ----   ---- leaf index
     * if index is (101 011 001 101)_2
     * the coord is [(1001), (0100), (1111)]
     * @param coord 3-d coordinate
     * @return index of an octree node
     **/
    protected Long coordinates2Index(int []coord) {
        Long index = 0L;
        for (int i = this.depth; i > 1; i --) {
            index |= (((coord[0] >> (i - 2)) & 1) << 2);
            index |= (((coord[1] >> (i - 2)) & 1) << 1);
            index |= (((coord[2] >> (i - 2)) & 1) << 0);
            index <<= 3;
        }
        index >>= 3;
        return index;
    }

    protected Vector<Long> obtainAdjacent26Indices(Long index) {
        Vector<Long> result = new Vector<>();
        int []coord = index2Coordinates(index);
        for (int i : new int[] {-1, 0, 1}) {
            for (int j : new int[] {-1, 0, 1}) {
                for (int k : new int[] {-1, 0, 1}) {
                    if (i == 0 && j == 0 && k == 0) continue;
                    if (coord[0] == 0 && i < 0) continue;
                    if (coord[1] == 0 && j < 0) continue;
                    if (coord[2] == 0 && k < 0) continue;
                    if (coord[0] == Math.pow(2, this.depth - 1) - 1 && i > 0) continue;
                    if (coord[1] == Math.pow(2, this.depth - 1) - 1 && j > 0) continue;
                    if (coord[2] == Math.pow(2, this.depth - 1) - 1 && k > 0) continue;

                    int []newCoord = new int [] {coord[0] + i, coord[1] + j, coord[2] + k};
                    if (isValidCoordinates(newCoord))
                        result.add(coordinates2Index(newCoord));
                }
            }
        }

        return result;
    }

    private boolean isValidCoordinates(int []coord) {
        if (coord.length < 3) return false;
        return coord[0] >= 0 && coord[0] < (int) pow(2, this.depth - 1)
                && coord[1] >= 0 && coord[1] < (int) pow(2, this.depth - 1)
                && coord[2] >= 0 && coord[2] < (int) pow(2, this.depth - 1);
    }

    public class OctreeNode {

        /**
         * default value is root index
         * the index is generated in createOctree()
         **/
        Long index = 0L;

        /**
         * an octree node holds the indices of 3d points in the List
         * in a non-leaf node, field indices is null
         **/
        List<Integer> indices = null;

        /** in a non-leaf node, field indices is null **/
        OctreeNode[] children = null;

        /** following fields define a bounding box **/
        double minX = Double.NaN;
        double maxX = Double.NaN;
        double minY = Double.NaN;
        double maxY = Double.NaN;
        double minZ = Double.NaN;
        double maxZ = Double.NaN;

        void addPoint(Point3d point, int index) {
            if (children == null) {
                if (contains(point)) {
                    indices.add(index);
                }
            } else {
                int xi = point.x < (maxX + minX) / 2 ? 0 : 1;
                int yj = point.y < (maxY + minY) / 2 ? 0 : 1;
                int zk = point.z < (maxZ + minZ) / 2 ? 0 : 1;
                int childIndex = xi * 4 + yj * 2 + zk * 1;
                children[childIndex].addPoint(point, index);
            }
        }

        boolean contains(Point3d point) {
            return point.x >= minX && point.x <= maxX &&
                    point.y >= minY && point.y <= maxY &&
                    point.z >= minZ && point.z <= maxZ;
        }

        public List<Integer> getIndices() {
            return indices;
        }
    }

    public int getDepth() {
        return depth;
    }

}