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
            if(currentBoard.isGoalState()){
                addVisited(currentBoard);
                for (Board b : getResultInOrder(currentBoard)){
                    System.out.println(b);
                }
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
        int threshold = parentBoard.getHeuristicByType(heuristicType);

        while (true) {
            visitedStates.clear();

            Stack<Board> stack = new Stack<>();
            stack.push(parentBoard);

            int nextThreshold = Integer.MAX_VALUE;

            while (!stack.isEmpty()) {
                Board currentBoard = stack.pop();

                if (currentBoard.isGoalState()) {
                    List<Board> s = getResultInOrder(currentBoard);
                    for (Board b : s) {
                        System.out.println(b);
                    }
                    System.out.println("Visited: " + visited);
                    System.out.println("Heuristic: " + heuristicType);
                    return currentBoard;
                }

                String currentKey = currentBoard.getStateKey();
                if (visitedStates.containsKey(currentKey)) {
                    Board storedIteration = visitedStates.get(currentKey);
                    if (storedIteration.getIteration() <= currentBoard.getIteration()) {
                        continue;
                    }
                }

                visited++;
                addVisited(currentBoard);

                List<Board> successors = new ArrayList<>(currentBoard.generatePossibleBoards());

                Collections.reverse(successors);

                for (Board next : successors) {
                    int g = next.getIteration();
                    int h = next.getHeuristicByType(heuristicType);
                    int f = g + h;

                    next.setHeuristicCost(f);

                    if (f <= threshold) {
                        stack.push(next);
                    } else {
                        nextThreshold = Math.min(nextThreshold, f);
                    }
                }
            }

            if (nextThreshold == Integer.MAX_VALUE) {
                return null;
            }

            threshold = nextThreshold;
        }
    }
}
