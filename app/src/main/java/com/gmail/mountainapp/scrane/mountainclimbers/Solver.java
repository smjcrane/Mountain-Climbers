package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Solver {

    private Mountain mountain;
    private Graph graph;
    int numClimbers;

    public Solver(Mountain mountain, int numClimbers){
        this.mountain = mountain;
        this.numClimbers = numClimbers;
        this.graph = makeGraph();

        Log.d("SOLVE", "Going to solve " + mountain.toString());
    }

    public static int solveFromResourceID(Context context, int resourceID) {
        try {
            InputStream stream = context.getResources().openRawResource(resourceID);

            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String[] heightStrings = br.readLine().split(" ");
            String[] climberString = br.readLine().split(" ");

            int[] heights = new int[heightStrings.length];
            int[] climberPositions = new int[climberString.length];
            for (int i = 0; i < heightStrings.length; i++) {
                heights[i] = Integer.parseInt(heightStrings[i]);
            }

            Mountain mountain = new Mountain(heights);

            for (int i = 0; i < climberString.length; i++) {
                climberPositions[i] = Integer.parseInt(climberString[i]);
            }

            Solver solver = new Solver(mountain, climberString.length);

            return solver.solve(climberPositions).size();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
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
                boolean good = false;
                for (int i = 0; i < numClimbers; i++){
                    if (Arrays.asList(mountain.getTurningPoints()).contains(v.coords[i])){
                        good = true;
                    }
                }
                if (vertices.contains(v)){
                    good = false;
                }
                if (good){
                    vertices.add(v);
                }
            }
        }
        List<Pair<Vertex, Vertex>> edges = new ArrayList<>();
        for (Vertex v : vertices){
            for (Vertex w : vertices) {
                if (v.numberOfDistinctCoords() >= w.numberOfDistinctCoords()){
                    int diff = Math.abs(mountain.getHeightAt(v.coords[0]) -  mountain.getHeightAt(w.coords[0]));
                    boolean reachable = true;
                    for (int i = 0; i < v.coords.length; i ++){

                        if (Math.abs(v.coords[i] - w.coords[i]) != diff){
                            reachable = false;
                        }
                    }
                    if (reachable){
                        edges.add(new Pair<>(v, w));
                    }
                }
            }
        }
        Graph graph = new Graph(vertices, edges);
        Log.d("SOLVE", "Graph has " + vertices.size() + " vertices and " + edges.size() + "edges");
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
            boolean good = true;
            int[] coords = new int[numClimbers];
            for (int c = 0; c < numClimbers; c++){
                coords[c] = possibleXs.get((i % (int) Math.pow(n, c + 1)) / (int) Math.pow(n, c));
                if (c > 0 && coords[c] < coords[c - 1]){
                    good = false;
                }
            }
            if (good){
                Vertex v = new Vertex(coords);
                vertices.add(v);
            }
        }

        return vertices;
    }

    private class Vertex {
        private int[] coords;

        public Vertex(int[] coords){
            this.coords = coords;
        }

        public int numberOfDistinctCoords(){
            Set mySet = new HashSet<>(Arrays.asList(coords));
            return mySet.size();
        }

        public boolean isGoal(){
            for (int i = 1; i < coords.length; i++){
                if (coords[i]!= coords[0]){
                    return false;
                }
            }
            return true;
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
                Log.d("SOLVE", "starting position does not exist");
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
            Log.d("SOLVE", "Couldn't find a solution");
            return null;
        }
    }

    public static class Move {
        private MountainClimber.Direction[] directions;

        public Move(Vertex start, Vertex end){
            directions = new MountainClimber.Direction[start.coords.length];
            for (int i = 0; i < start.coords.length; i++){
                directions[i] = (start.coords[i] < end.coords[i]) ?
                        MountainClimber.Direction.RIGHT : MountainClimber.Direction.LEFT;
            }
        }

        public Move(MountainClimber.Direction[] directions){
            this.directions = directions;
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
