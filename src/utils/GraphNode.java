package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Nodes of the graph, with neighbours
 */
public class GraphNode {
    /**
     * Coordinates of the node
     */
    private int x;
    private int y;
    /**
     * Orientation of the node (True = Horizontal, False = Vertical)
     */
    private boolean isHorizontal;
    /**
     * List of neighbours and the weights to get to them.
     */
    private HashMap<GraphNode, Float> neighbours;

    /**
     * Constructor for node
     * @param x - x coordinate
     * @param y - y coordinate
     * @param isHorizontal - orientation
     */
    public GraphNode(int x, int y, boolean isHorizontal){
        this.x = x;
        this.y = y;
        this.isHorizontal = isHorizontal;
        neighbours = new HashMap<>();
    }

    /**
     * Gets the x coordinate
     * @return x coordinate as int
     */
    public int getX(){
        return x;
    }

    /**
     * Gets the y coordinate
     * @return y coordinate as int
     */
    public int getY(){
        return y;
    }

    /**
     * Gets the orientation
     * @return True if Horizontal, False if Vertical
     */
    public boolean isHorizontal(){
        return isHorizontal;
    }

    /**
     * Adds a neighbour to the list with associated weight, with input validation
     * @param graphNode - Node to add
     * @param weight - Weight of edge
     * @return True if successful
     */
    public boolean addNeighbour(GraphNode graphNode, Float weight){
        if(graphNode.x == this.x && graphNode.y == this.y && graphNode.isHorizontal == this.isHorizontal) return false;
        if(getEuclideanDistanceTo(graphNode) != 1) return false;
        neighbours.put(graphNode, weight);
        return true;
    }


    public Float getNeighbourWeight(GraphNode graphNode){
        return neighbours.get(graphNode);
    }

    /**
     * Gets the list of neighbours
     * @return Pairs of Nodes and the weights to traverse to them
     */
    public Set<Map.Entry<GraphNode, Float>> getNeighbours(){
        return neighbours.entrySet();
    }

    public boolean isNeighbour(GraphNode graphNode){
        return neighbours.containsKey(graphNode);
    }

    /**
     * Calculates the euclidean distance to a node
     * @param graphNode - Node to compute distance to
     * @return Straight line distance to node as a double
     */
    public double getEuclideanDistanceTo(GraphNode graphNode){
        return Math.sqrt((this.x - graphNode.x)*(this.x - graphNode.x) + (this.y - graphNode.y) * (this.y - graphNode.y));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return x == graphNode.x &&
                y == graphNode.y &&
                isHorizontal == graphNode.isHorizontal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, isHorizontal);
    }
}