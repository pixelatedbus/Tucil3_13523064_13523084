package tucil.rhsolver.app;

import java.util.*;

public class Solver {
    private HashMap<String, Board> visitedStates;
    private PriorityQueue<Board> queue;
    private int visited;

    public Solver(){
        this.visitedStates = new HashMap<>();
        this.queue = new PriorityQueue<>(Comparator.comparingInt(Board::getHeuristicCost));
        this.visited = 0;
    }

    public void addVisited(Board visitedBoard){
        this.visitedStates.put(visitedBoard.getStateKey(), visitedBoard);
    }

    public int getVisited(){
        return this.visited;
    }

    public void addQueue(Board queuedBoard){
        this.queue.add(queuedBoard);
    }

    public Board GameSolver(Board parentBoard, String algorithm, String heuristicType){
        if (!parentBoard.isBoardValid()){
            return null;
        }
        int heuristic;
        if(algorithm.equals("GBFS")){
            heuristic = parentBoard.getHeuristicByType(heuristicType);
        } else if(algorithm.equals("UCS")){
            heuristic = parentBoard.getIteration();
        } else if (algorithm.equals("A*")){
            heuristic = parentBoard.getHeuristicByType(heuristicType) + parentBoard.getIteration();
        } else {
            return IDAStar(parentBoard, heuristicType);
        }
        parentBoard.setHeuristicCost(heuristic);
        addQueue(parentBoard);

        while (!queue.isEmpty()){
            Board currentBoard = this.queue.poll();
            currentBoard.printBoard();
            if(currentBoard.isGoalState()){
                addVisited(currentBoard);
                System.out.println("Visited: " + visited);
                System.out.println("Heuristic: " + heuristicType);
                return currentBoard;
            }
            String currentKey = currentBoard.getStateKey();
            if(this.visitedStates.containsKey(currentKey)){
                if (this.visitedStates.get(currentKey).getHeuristicCost() <= currentBoard.getHeuristicCost() || algorithm.equals("GBFS")) {
                    continue;
                }
            }

            addVisited(currentBoard);
            visited++;

            for(Board next : currentBoard.generatePossibleBoards()){
                String key = next.getStateKey();
                if(!this.visitedStates.containsKey(key) || this.visitedStates.get(key).getHeuristicCost() > next.getHeuristicCost()){
                    int childHeuristic;
                    if(algorithm.equals("GBFS")){
                        childHeuristic = next.getHeuristicByType(heuristicType);
                    } else if(algorithm.equals("UCS")){
                        childHeuristic = next.getIteration();
                        System.out.println(childHeuristic);
                    } else {
                        childHeuristic = next.getHeuristicByType(heuristicType) + next.getIteration();
                    }
                    next.setHeuristicCost(childHeuristic);
                    addQueue(next);
                }
            }
        }

        return null;
    }

    public List<Board> getResultInOrder(Board goalBoard) {
        List<Board> path = new ArrayList<>();


        Board currentBoard = goalBoard;

        Stack<Board> reversePath = new Stack<>();

        while (currentBoard != null) {
            reversePath.push(currentBoard);

            if (currentBoard.getParentState().equals("")) {
                break;
            }

            String parentKey = currentBoard.getParentState();
            currentBoard = visitedStates.get(parentKey);

        }

        while (!reversePath.isEmpty()) {
            path.add(reversePath.pop());
        }

        return path;
    }

        public Board IDAStar(Board parentBoard, String heuristicType) {
        // Initial threshold is just the heuristic value of the start state
        int threshold = parentBoard.getHeuristicByType(heuristicType);

        while (true) {
            // Reset for each new threshold iteration
            visitedStates.clear();
            visited = 0;

            Stack<Board> stack = new Stack<>();
            stack.push(parentBoard);

            // Track minimum f-value that exceeds threshold
            int nextThreshold = Integer.MAX_VALUE;

            while (!stack.isEmpty()) {
                Board currentBoard = stack.pop();

                // Check for goal state
                if (currentBoard.isGoalState()) {
                    System.out.println("Visited: " + visited);
                    System.out.println("Heuristic: " + heuristicType);
                    return currentBoard;
                }

                // Process current board
                String currentKey = currentBoard.getStateKey();
                if (visitedStates.containsKey(currentKey)) {
                    Board storedIteration = visitedStates.get(currentKey);
                    if (storedIteration.getIteration() <= currentBoard.getIteration()) {
                        continue;
                    }
                }

                // Mark as visited
                visited++;
                addVisited(currentBoard);

                // Get all possible next boards
                List<Board> successors = new ArrayList<>(currentBoard.generatePossibleBoards());

                // Process in reverse order (to maintain DFS order when using stack)
                Collections.reverse(successors);

                for (Board next : successors) {
                    // Calculate f-value: g + h
                    int g = next.getIteration(); // Path cost so far
                    int h = next.getHeuristicByType(heuristicType); // Heuristic estimate
                    int f = g + h; // Total estimated cost

                    // Set the heuristic cost for board (f-value)
                    next.setHeuristicCost(f);

                    if (f <= threshold) {
                        // If within threshold, add to stack for exploration
                        stack.push(next);
                    } else {
                        // If exceeds threshold, track for next iteration
                        nextThreshold = Math.min(nextThreshold, f);
                    }
                }
            }

            // If no solution found within threshold and no higher f-values found
            if (nextThreshold == Integer.MAX_VALUE) {
                return null; // No solution exists
            }

            // Update threshold for next iteration
            threshold = nextThreshold;
        }
    }
}
