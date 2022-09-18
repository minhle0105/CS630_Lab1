import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;

public class ReadInput {
    static class Node implements Comparable<Node> {
        int x;
        int y;
        double elevation;
        Node previousNode;
        List<Edge> neighbors;

        double fScore;
        double gScore;
        double hScore;

        public Node(int x, int y, double elevation) {
            this.x = x;
            this.y = y;
            this.elevation = elevation;
            this.previousNode = null;
            this.neighbors = new ArrayList<>();
            this.fScore = Double.MAX_VALUE;
            this.gScore = Double.MAX_VALUE;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.fScore, o.fScore);
        }

        public void addNeighbor(double weight, Node node) {
            Edge edge = new Edge(weight, node);
            neighbors.add(edge);
        }

        public double calculateHeuristic(Node target) {
            double d1 = Math.abs(this.x - target.x) + Math.abs(this.y - target.y);
            double d2 = Math.abs(this.elevation - target.elevation);
            return Math.sqrt((d1 * d1) + (d2 * d2));
        }
    }

    static class Edge {
        double weight;
        Node node;

        public Edge(double weight, Node node) {
            this.weight = weight;
            this.node = node;
        }
    }

    public static boolean findingPath(Node start, Node dest) {
        Set<Node> visited = new HashSet<>();
        PriorityQueue<Node> queue = new PriorityQueue<>();
        start.fScore = start.gScore + start.calculateHeuristic(dest);
        queue.add(start);
        while (!queue.isEmpty()) {
            Node thisNode = queue.peek();
            if (thisNode == dest) {
                return true;
            }
            for (Edge edge : thisNode.neighbors) {
                Node nextNode = edge.node;
                double totalWeight = thisNode.gScore + edge.weight;
                if (!queue.contains(nextNode) && !visited.contains(nextNode)) {
                    nextNode.previousNode = thisNode;
                    nextNode.gScore = totalWeight;
                    nextNode.fScore = nextNode.gScore + nextNode.calculateHeuristic(dest);
                    queue.add(nextNode);
                }
                else {
                    if (totalWeight < nextNode.gScore) {
                        nextNode.previousNode = thisNode;
                        nextNode.gScore = totalWeight;
                        nextNode.fScore = nextNode.gScore + nextNode.calculateHeuristic(dest);
                        if (visited.contains(nextNode)) {
                            visited.remove(nextNode);
                            queue.add(nextNode);
                        }
                    }
                }
            }

            queue.remove(thisNode);
            visited.add(thisNode);
        }
        return false;
    }

    public static List<Node> printPath(Node end) {
        List<Node> paths = new ArrayList<>();
        Node curr = end;
        while (curr.previousNode != null) {
            paths.add(curr);
            curr = curr.previousNode;
        }
        paths.add(curr);
        for (int i = paths.size() - 1; i >= 0; i--) {
            System.out.println(paths.get(i).x + " " + paths.get(i).y);
        }
        return paths;
    }

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(new File("input.txt"));
        BufferedImage image = ImageIO.read(new File("img.png"));
//        int color = image.getRGB(300, 400);
//        int blue = color & 0xff;
//        int green = (color & 0xff00) >> 8;
//        int red = (color & 0xff0000) >> 16;
//        System.out.println(image.getWidth());
//        System.out.println(image.getHeight());
//        System.out.println("(" + red + ", " + green + ", " + blue + ")");
        double[][] elevations = new double[500][395];
        int row = 0;
        while (sc.hasNextLine()) {
            String[] next = sc.nextLine().trim().split("   ");
            double[] thisRow = new double[395];
            for (int i = 0; i < 395; i++) {
                thisRow[i] = Double.parseDouble(next[i]);
            }
            elevations[row] = thisRow;
            row++;
        }
        Node[][] nodes = new Node[500][395];
        for (int i = 0; i < elevations.length; i++) {
            for (int j = 0; j < elevations[i].length; j++) {
                Node node = new Node(i, j, elevations[i][j]);
                nodes[i][j] = node;
            }
        }
        int[] dR = {1, -1, 0, 0};
        int[] dC = {0, 0, 1, -1};
        for (int i = 0; i < elevations.length; i++) {
            for (int j = 0; j < elevations[i].length; j++) {
                Node thisNode = nodes[i][j];
                for (int k = 0; k < 4; k++) {
                    int nextX = i + dR[k];
                    int nextY = j + dC[k];
                    boolean isInBound = nextX >= 0 && nextX < 500 && nextY >= 0 && nextY < 395;
                    if (isInBound) {
                        thisNode.addNeighbor(elevations[nextX][nextY], nodes[nextX][nextY]);
                    }
                }
            }
        }
        int[] start = {150, 200};
        int[] end = {340, 350};
        Node startNode = nodes[start[0]][start[1]];
        Node endNode = nodes[end[0]][end[1]];
        findingPath(startNode, endNode);
        List<Node> path = printPath(endNode);
        Color color = new Color(255, 192, 0); // Color white
        int rgb = color.getRGB();
        for (Node node : path) {
            image.setRGB(node.x, node.y, rgb);
        }
        ImageIO.write(image, "png", new File("output.png"));
        sc.close();
    }
}
