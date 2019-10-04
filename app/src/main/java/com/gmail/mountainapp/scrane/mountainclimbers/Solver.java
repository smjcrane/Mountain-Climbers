package com.gmail.mountainapp.scrane.mountainclimbers;

import android.content.Context;
import android.os.SystemClock;
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
import java.util.SortedSet;


public class Solver {

    private Mountain mountain;
    private Graph graph;
    int numClimbers;

    public Solver(Mountain mountain, int numClimbers){
        this.mountain = mountain;
        this.numClimbers = numClimbers;
        if (true){//numClimbers < 5){
            this.graph = makeGraph();
        } else {
            this.graph = new Graph(new ArrayList<Vertex>(), new ArrayList<Pair<Vertex, Vertex>>());
            Log.d("SOLVE", "Too many climbers");
        }
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

            int minMoves = solver.solve(climberPositions).size();
            return minMoves;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<Move> solve(int[] initialCoords) {
        long millisStart = SystemClock.elapsedRealtime();
        Vertex start = new Vertex(initialCoords);
        List<Vertex> path = graph.getBreadthFirstPathFrom(start);
        if (path == null){
            return null;
        }
        List<Move> moves = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++){
            moves.add(new Move(path.get(i), path.get(i + 1)));
        }
        long millisEnd = SystemClock.elapsedRealtime();
        Log.d("SOLVE", "Solving took " + (millisEnd - millisStart) + " millis");
        return moves;
    }

    private Graph makeGraph(){
        long start = SystemClock.elapsedRealtime();
        List<Vertex> vertices = new ArrayList<>();

        for (int x1 : new HashSet<>(Arrays.asList(mountain.getTurningPoints()))){
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
                    Log.d("VERTEX", v.toString());
                }
            }
        }
        List<Pair<Vertex, Vertex>> edges = new ArrayList<>();
        for (Vertex v : vertices){
            for (Vertex w : vertices) {
                if (v.goesTo(w)){
                    edges.add(new Pair<>(v, w));
                }
            }
        }
        Graph graph = new Graph(vertices, edges);
        long end = SystemClock.elapsedRealtime();
        Log.d("SOLVE", "Making the graph took " + (end - start) + " millis");
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
        MountainClimber.Direction[] directions = new MountainClimber.Direction[numClimbers];
        int[] distanceLeft;
        int[] distanceRight;
        Integer[] turningPoints;

        public Vertex(int[] coords){
            this.coords = coords;
            turningPoints = mountain.getTurningPoints();
            distanceLeft = new int[numClimbers];
            distanceRight = new int[numClimbers];
            for (int i = 0; i < numClimbers; i++){
                int j = 0;
                int l = 0;
                int r = 0;
                while (r <= coords[i] && j < turningPoints.length - 1){
                    j++;
                    r = turningPoints[j];
                }
                if (!Arrays.asList(turningPoints).contains(coords[i]) && coords[i] > 0){
                    l = turningPoints[j - 1];
                } else if (coords[i] > 0){
                    l = turningPoints[j - 2];
                }
                if (coords[i] == 0){
                    l = 0;
                } else if (coords[i] == mountain.getWidth()){
                    r = mountain.getWidth();
                }
                distanceLeft[i] = coords[i] - l;
                distanceRight[i] = r - coords[i];
            }
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

        public boolean goesTo(Vertex w) {
            if (this.equals(w)){
                return false;
            }
            int diff = Math.abs(mountain.getHeightAt(coords[0]) -  mountain.getHeightAt(w.coords[0]));
            for (int i = 0; i < coords.length; i ++){
                for (int j = i + 1; j < coords.length; j++){
                    if (coords[i] == coords[j] && w.coords[i] != w.coords[j]){
                        return false;
                    }
                }
                if (Math.abs(coords[i] - w.coords[i]) != diff){
                    return false;
                }
            }
            return true;
        }

        private Vertex getNeighbourAt(int i) {
            int[] neighbour_coords = coords.clone();
            int distanceWeCanGo = mountain.getWidth();
            for (int c = 0; c < numClimbers; c++) {
                directions[c] = ((i >> c) % 2 == 0) ? MountainClimber.Direction.LEFT : MountainClimber.Direction.RIGHT;
                if (directions[c] == MountainClimber.Direction.LEFT) {
                    distanceWeCanGo = Math.min(distanceWeCanGo, distanceLeft[c]);
                } else {
                    distanceWeCanGo = Math.min(distanceWeCanGo, distanceRight[c]);
                }
                for (int d = 0; d < c; d++) {
                    if (coords[d] == coords[c] && directions[d] != directions[d]) {
                        return null;
                    }
                }
                if (distanceWeCanGo == 0) {
                    return null;
                }
            }
            //Log.d("SOLVE", "Can I go in the directions " + new Move(directions).toString());
            for (int c = 0; c < numClimbers; c++) {
                if (directions[c] == MountainClimber.Direction.LEFT) {
                    neighbour_coords[c] -= distanceWeCanGo;
                } else {
                    neighbour_coords[c] += distanceWeCanGo;
                }
            }
            if (goesTo(new Vertex(neighbour_coords))) {
                //Log.d("SOLVE", "Yes I can");
                return (new Vertex(neighbour_coords.clone()));
            }
            //Log.d("SOLVE", "No I can't");
            return null;
        }

        public List<Vertex> getNeighbours(){
            //Log.d("SOLVE", "I am a vertex at " + toString() +
             //       " and I can go " + new Vertex(distanceLeft).toString() + " to the left and " +
            //        new Vertex(distanceRight).toString() + " to the right");
            List<Vertex> neighbours = new ArrayList<>();
            for (int i = 0; i < Math.pow(2, numClimbers); i++){
                Vertex neighbour = getNeighbourAt(i);
                if (neighbour != null){
                    neighbours.add(neighbour);
                }
            }
            //Log.d("SOLVE", "Found " + neighbours.size() + " neighbours");
            return neighbours;
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
            if (!vertices.contains(startVertex) && numClimbers < 5){
                Log.d("SOLVE", "starting position does not exist");
                return null;
            }
            List<Vertex> exploring = new ArrayList<>();
            Map<Vertex, Vertex> parents = new HashMap<>();
            exploring.add(startVertex);
            while (exploring.size() > 0){
                Vertex v = exploring.get(0);
                List<Vertex> neighbours;
                if (false){//numClimbers >= 5){
                    neighbours = v.getNeighbours();
                } else {
                    neighbours = new ArrayList<>();
                    for (Pair<Vertex, Vertex> edge : edges) {
                        if (v.equals(edge.first)) {
                            neighbours.add(edge.second);
                        }
                    }
                }
                for (Vertex neighbour : neighbours){
                    //Log.d("SOLVE", "Exploring the neighbour at " + neighbour.toString());
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
                        } else {
                            Log.d("SOLVE", "Already explored that one");
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
