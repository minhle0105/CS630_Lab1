import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.List;

public class Lab1 {

    static Map<String, Double> terrainCode;
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

        public void adjustFScore(String terrainColorCode) {
            try {
                this.fScore -= terrainCode.get(terrainColorCode);
            }
            catch (Exception ignored) {

            }
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
            this.hScore = Math.sqrt((d1 * d1) + (d2 * d2));
            return this.hScore;
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
        visited.add(start);
        start.gScore = 0;
        dest.hScore = 0;
        while (!queue.isEmpty()) {
            Node thisNode = queue.remove();
            visited.remove(thisNode);
            if (thisNode == dest) {
                return true;
            }
            for (Edge edge : thisNode.neighbors) {
                Node neighbor = edge.node;
                double totalWeight = thisNode.gScore + edge.weight;
                if (totalWeight < neighbor.gScore) {
                    neighbor.previousNode = thisNode;
                    neighbor.gScore = totalWeight;
                    neighbor.fScore = neighbor.gScore + neighbor.calculateHeuristic(dest);
                    if (!visited.contains(neighbor)) {
                        queue.add(neighbor);
                        visited.add(neighbor);
                    }
                }
            }
        }
        return false;
    }

    public static List<Node> trackPath(Node end) {
        List<Node> paths = new ArrayList<>();
        Node curr = end;
        while (curr.previousNode != null) {
            paths.add(curr);
            curr = curr.previousNode;
        }
        paths.add(curr);
        return paths;
    }

    private static double calculateRealDistance(List<Node> nodes) {
        double res = 0.0;
        for (Node node : nodes) {
            Node prev = node.previousNode;
            for (Edge edge : node.neighbors) {
                if (edge.node == prev) {
                    res += edge.weight;
                    break;
                }
            }
        }
        return res;
    }

    private static String convertRGB(int color) {
        int blue = color & 0xff;
        int green = (color & 0xff00) >> 8;
        int red = (color & 0xff0000) >> 16;
        return red + "," + green + "," + blue + "";
    }

    public static void main(String[] args) throws Exception {
        final double LONGITUDE_PIXEL = 10.29;
        final double LATITUDE_PIXEL = 7.55;
        terrainCode = new HashMap<>();
        terrainCode.put("248,148,18", 0.0);
        terrainCode.put("255,192,0", 0.5);
        terrainCode.put("255,255,255", 0.0);
        terrainCode.put("2,208,60", 0.4);
        terrainCode.put("2,136,40", 0.3);
        terrainCode.put("5,73,24", 0.6);
        terrainCode.put("0,0,255", 0.8);
        terrainCode.put("71,51,3", 0.1);
        terrainCode.put("0,0,0", 0.2);
        terrainCode.put("205,0,101", 0.0);
        String terrainImage = args[0];
        String elevationFile = args[1];
        String pathFile = args[2];
        String outputImage = args[3];
        Scanner sc = new Scanner(new File(elevationFile));
        BufferedImage image = ImageIO.read(new File(terrainImage));
        int height = image.getHeight();
        int width = image.getWidth();
        double[][] elevations = new double[height][width];
        int row = 0;
        while (sc.hasNext()) {
            double[] thisRow = new double[width];
            for (int col = 0; col < width; col ++) {
                thisRow[col] = Double.parseDouble(sc.next());
            }
            for (int i = 0; i < 5; i++) {
                sc.next();
            }
            elevations[row] = thisRow;
            row++;
        }
        Node[][] nodes = new Node[height][width];
        for (int i = 0; i < elevations.length; i++) {
            for (int j = 0; j < elevations[i].length; j++) {
                Node node = new Node(i, j, elevations[i][j]);
                String terrainCode = convertRGB(image.getRGB(j, i));
                node.adjustFScore(terrainCode);
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
                    boolean isInBound = nextX >= 0 && nextX < height && nextY >= 0 && nextY < width;
                    if (isInBound) {
                        double pixelsInMeters = k == 2 | k == 3 ? LONGITUDE_PIXEL : LATITUDE_PIXEL;
                        thisNode.addNeighbor(Math.abs(thisNode.elevation - elevations[nextX][nextY]) + pixelsInMeters, nodes[nextX][nextY]);
                    }
                }
            }
        }
        int[] pathData = new int[4];
        int index = 0;
        Scanner readPath = new Scanner(new File(pathFile));
        while (readPath.hasNext()) {
            pathData[index] = Integer.parseInt(readPath.next());
            index++;
        }
        int[] start = {pathData[1], pathData[0]};
        int[] end = {pathData[3], pathData[2]};

        Node startNode = nodes[start[0]][start[1]];
        Node endNode = nodes[end[0]][end[1]];
        findingPath(startNode, endNode);
        List<Node> path = trackPath(endNode);
        double totalDistance = calculateRealDistance(path);
        Color color = new Color(255, 0, 0);
        int rgb = color.getRGB();
        for (Node node : path) {
            image.setRGB(node.y, node.x, rgb);
        }
        ImageIO.write(image, "png", new File(outputImage));
        FileWriter fileWriter = new FileWriter("distance.txt", false);
        fileWriter.write("Total Distance: " + totalDistance + " meters.\n");
        readPath.close();
        fileWriter.close();
        sc.close();
    }
}
