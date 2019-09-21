package com.example.mountainclimbers;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solver {

    private Mountain mountain;
    private Graph graph;
    int numClimbers;

    public Solver(Mountain mountain, int numClimbers){
        this.mountain = mountain;
        this.numClimbers = numClimbers;
        this.graph = makeGraph();
    }

    public List<Move> solve(int[] initialCoords) {
        Vertex start = new Vertex(initialCoords);
        List<Vertex> path = graph.getBreadthFirstPathFrom(start);
        if (path == null){
            return null;
        }
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++){
            moves.add(new Move(path.get(i), path.get(i + 1)));
        }
        return moves;
    }

    private Graph makeGraph(){
        List<Vertex> vertices = new ArrayList<>();

        for (int x1 : mountain.getTurningPoints()){
            for (Vertex v : getAllVerticesAtHeight(mountain.getHeightAt(x1))){
                boolean good = true;
                for (int i = 0; i < numClimbers - 1; i++){
                    if (v.coords[i] > v.coords[i + 1]){
                        good = false;
                    }
                }
                if (good){
                    vertices.add(v);
                }
            }
        }
        List<Pair<Vertex, Vertex>> edges = new ArrayList<>();
        for (Vertex v : vertices){
            for (Vertex w : vertices) {
                if (v.compareTo(w) != 0){
                    int diff = Math.abs(mountain.getHeightAt(v.coords[0]) -  mountain.getHeightAt(w.coords[0]));
                    boolean reachable = true;
                    for (int i = 0; i < v.coords.length; i ++){

                        if (Math.abs(v.coords[i] - w.coords[i]) != diff){
                            reachable = false;
                        }
                    }
                    if (reachable){
                        edges.add(new Pair<Vertex, Vertex>(v, w));
                    }
                }
            }
        }
        Graph graph = new Graph(vertices, edges);
        return graph;
    }

    private List<Integer> getAllX(int height){
        List xs = new ArrayList();
        for (int x = 0; x <= mountain.getWidth(); x++){
            if (mountain.getHeightAt(x) == height){
                xs.add(x);
            }
        }
        return xs;
    }

    private List<Vertex> getAllVerticesAtHeight(int height){
        List<Integer> possibleXs = getAllX(height);
        int n = possibleXs.size();

        List<Vertex> vertices = new ArrayList<>();

        for (int i = 0; i < (int) Math.pow(n, numClimbers); i++){
            int[] coords = new int[numClimbers];
            for (int c = 0; c < numClimbers; c++){
                coords[c] = possibleXs.get((i % (int) Math.pow(n, c + 1)) / (int) Math.pow(n, c));
            }
            Vertex v = new Vertex(coords);
            vertices.add(v);
        }

        return vertices;
    }

    private class Vertex implements Comparable<Vertex> {
        private int[] coords;

        public Vertex(int[] coords){
            this.coords = coords;
        }

        @Override
        public int compareTo(Vertex o) {
            for (int i = 0; i < coords.length; i++){
                if (coords[i] < o.coords[i]){
                    return -1;
                } else if (coords[i] > o.coords[i]){
                    return 1;
                }
            }
            return 0;
        }

        public boolean isGoal(){
            for (int i = 0; i < coords.length; i++){
                for (int j = i + 1; j < coords.length; j++){
                    if (coords[i] == coords[j]){
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean equals(Object other){
            return Arrays.equals(((Vertex) other).coords, coords);
        }

        public String toString(){
            String s = Integer.toString(coords[0]);
            for (int i = 1; i < coords.length; i++){
                s = s + ", " + coords[i];
            }
            return s;
        }
    }

    private class Graph{
        public List<Vertex> vertices;
        public List<Pair<Vertex, Vertex>> edges;

        public Graph(List<Vertex> vertices, List<Pair<Vertex, Vertex>> edges){
            this.vertices = vertices;
            this.edges = edges;
            for (int i = 0; i < vertices.size(); i ++){
            }
            for (int i = 0; i < edges.size(); i++){
            }
        }

        public List<Vertex> getBreadthFirstPathFrom(Vertex startVertex){
            if (!vertices.contains(startVertex)){
                return null;
            }
            List<Vertex> exploring = new ArrayList<>();
            Map<Vertex, Vertex> parents = new HashMap<>();
            exploring.add(startVertex);
            while (exploring.size() > 0){
                Vertex v = exploring.get(0);
                for (Pair<Vertex, Vertex> edge : edges){
                    Vertex neighbour = null;
                    if (v.equals(edge.first)){
                        neighbour = edge.second;
                    } else if (v.equals(edge.second)){
                        neighbour = edge.first;
                    }
                    if (neighbour != null && !neighbour.equals(startVertex)){
                        if (!parents.containsKey(neighbour)){
                            parents.put(neighbour, v);
                            if (neighbour.isGoal()){
                                List<Vertex> path = new ArrayList<>();
                                Vertex pos = neighbour;
                                while (pos != startVertex){
                                    path.add(pos);
                                    pos = parents.get(pos);
                                }
                                path.add(startVertex);
                                Collections.reverse(path);
                                return path;
                            }
                            if (!exploring.contains(neighbour)){
                                exploring.add(neighbour);
                            }
                        }
                    }
                }
                exploring.remove(v);
            }
            return null;
        }
    }

    public class Move {
        private MountainClimber.Direction[] directions;

        public Move(Vertex start, Vertex end){
            directions = new MountainClimber.Direction[start.coords.length];
            for (int i = 0; i < start.coords.length; i++){
                directions[i] = (start.coords[i] < end.coords[i]) ?
                        MountainClimber.Direction.RIGHT : MountainClimber.Direction.LEFT;
            }
        }

        public MountainClimber.Direction[] getDirections(){
            return directions;
        }

        public String toString(){
            String s = "";
            for (int i = 0; i < directions.length; i++){
                s = s + ((directions[i] == MountainClimber.Direction.RIGHT) ? "R" : "L");
            }
            return s;
        }
    }
}
