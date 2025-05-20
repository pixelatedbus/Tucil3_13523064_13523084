package tucil.rhsolver.app;

import java.util.*;

public class Board {
    private int row;
    private int col;
    private int iteration;
    private HashMap<Character, Piece> pieces;
    private Coords goal;
    private char[][] matrix;
    private String parentState;
    private String latestMove;
    private int heuristicCost;

    public Board(int row, int col){
        this.row = row;
        this.col = col;
        this.iteration = 0;
        this.pieces = new HashMap<>();
        this.matrix = new char[row][col];
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++){
                matrix[i][j] = '.';
            }
        }
        this.goal = new Coords(-1, -1);
        this.parentState = "";
        this.latestMove = "";
    }

    public Board(Board board){
        this.row = board.row;
        this.col = board.col;
        this.iteration = board.iteration;
        this.pieces = new HashMap<>();
        for (var entry : board.pieces.entrySet()) {
            this.pieces.put(entry.getKey(), new Piece(entry.getValue())); // pastikan Piece punya copy-constructor
        }
        this.goal = new Coords(board.goal.getX(), board.goal.getY());
        this.matrix = new char[row][col];
        for (int i = 0; i < row; i++){
            System.arraycopy(board.matrix[i], 0, matrix[i], 0, col);
        }
    }

    public void setGoal(Coords goal){
        this.goal = goal;
    }

    public Coords getGoal(){
        return goal;
    }

    public HashMap<Character, Piece> getPieces(){
        return pieces;
    }

    public void addPiece(Piece piece){
        pieces.put(piece.getId(), piece);
    }

    public Piece getPlayer(){
        return pieces.get('P');
    }

    public void setParentState(String parentState){
        this.parentState = parentState;
    }
    public String getParentState(){
        return parentState;
    }

    public void setLatestMove(String latestMove){
        this.latestMove = latestMove;
    }
    public String getLatestMove(){
        return latestMove;
    }

    public void setHeuristicCost(int heuristicCost) {
        this.heuristicCost = heuristicCost;
    }

    public int getHeuristicCost() {
        return heuristicCost;
    }

    public int getIteration(){
        return iteration;
    }

    public String getStateKey(){
        StringBuilder state = new StringBuilder();
        for (Piece piece : pieces.values()){
            state.append(piece.getId()).append(":");
            for (Coords coord : piece.getPosition()){
                state.append(coord.getX()).append(":").append(coord.getY()).append(";");
            }
        }
        return state.toString();
    }

    public void printBoard(){
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++){
                System.out.print(matrix[i][j]);
            }
            System.out.println();
        }
    }

    public void updateBoard(){
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++){
                matrix[i][j] = '.';
            }
        }
        for (Piece piece : pieces.values()){
            for (Coords coord : piece.getPosition()){
                matrix[coord.getX()][coord.getY()] = piece.getId();
            }
        }
    }

    public void removePiece(char id){
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++){
                if (matrix[i][j] == id){
                    matrix[i][j] = '.';
                }
            }
        }
    }

    public boolean isValidMove(Character id, boolean forward){
        Piece piece = pieces.get(id);
        if (piece == null) return false;
        int mult = forward ? 1 : -1;

        if (piece.isHorizontal()){
            for (Coords coord : piece.getPosition()){
                int newY = coord.getY() + mult;
                if (newY < 0 || newY >= col || (matrix[coord.getX()][newY] != '.' && matrix[coord.getX()][newY] != id)){
                    return false;
                }
            }
        } else {
            for (Coords coord : piece.getPosition()){
                int newX = coord.getX() + mult;
                if (newX < 0 || newX >= row || (matrix[newX][coord.getY()] != '.' && matrix[newX][coord.getY()] != id)){
                    return false;
                }
            }
        }
        return true;
    }

    public List<Board> generatePossibleBoards(){
        List<Board> possibleBoards = new ArrayList<>();
        HashMap<Character, Piece> tempPieces = new HashMap<>(this.pieces);
        String stateKey = getStateKey();
        for (Piece piece : tempPieces.values()){
            if (isValidMove(piece.getId(), true)){
                Board newBoard = new Board(this);
                newBoard.iteration++;
                newBoard.updateBoard();
                Piece newPiece = newBoard.pieces.get(piece.getId());
                newPiece.move(true);
                newBoard.updateBoard();
                possibleBoards.add(newBoard);
                newBoard.setParentState(stateKey);
                if (newPiece.isHorizontal()) {
                    newBoard.setLatestMove("Move " + newPiece.getId() + " right");
                } else {
                    newBoard.setLatestMove("Move " + newPiece.getId() + " down");
                }
                Board newNewBoard = new Board(newBoard);
                Piece newNewPiece = newNewBoard.pieces.get(piece.getId());
                while (newNewBoard.isValidMove(newNewPiece.getId(), true)){
                    newNewBoard.updateBoard();
                    newNewPiece.move(true);
                    newNewBoard.updateBoard();
                    possibleBoards.add(newNewBoard);
                    newNewBoard.setParentState(stateKey);
                    if (newNewPiece.isHorizontal()) {
                        newNewBoard.setLatestMove("Move " + newNewPiece.getId() + " right");
                    } else {
                        newNewBoard.setLatestMove("Move " + newNewPiece.getId() + " down");
                    }
                    newNewBoard = new Board(newNewBoard);
                    newNewPiece = newNewBoard.pieces.get(piece.getId());
                }
            }
            if (isValidMove(piece.getId(), false)){
                Board newBoard = new Board(this);
                newBoard.iteration++;
                newBoard.updateBoard();
                Piece newPiece = newBoard.pieces.get(piece.getId());
                newPiece.move(false);
                newBoard.updateBoard();
                possibleBoards.add(newBoard);
                newBoard.setParentState(stateKey);
                if (newPiece.isHorizontal()) {
                    newBoard.setLatestMove("Move " + newPiece.getId() + " left");
                } else {
                    newBoard.setLatestMove("Move " + newPiece.getId() + " up");
                }
                Board newNewBoard = new Board(newBoard);
                Piece newNewPiece = newNewBoard.pieces.get(piece.getId());
                while (newNewBoard.isValidMove(newNewPiece.getId(), false)){
                    newNewBoard.updateBoard();
                    newNewPiece.move(false);
                    newNewBoard.updateBoard();
                    possibleBoards.add(newNewBoard);
                    newNewBoard.setParentState(stateKey);
                    if (newNewPiece.isHorizontal()) {
                        newNewBoard.setLatestMove("Move " + newNewPiece.getId() + " left");
                    } else {
                        newNewBoard.setLatestMove("Move " + newNewPiece.getId() + " up");
                    }
                    newNewBoard = new Board(newNewBoard);
                    newNewPiece = newNewBoard.pieces.get(piece.getId());
                }
            }
        }
        return possibleBoards;
    }

    public boolean isGoalState(){
        Piece player = getPlayer();
        return player.isIntersecting(goal);
    }

    public char[][] getMatrix(){
        return matrix;
    }

    public List<Coords> stepsToGoal(){
        List<Coords> steps = new ArrayList<>();
        Coords playerFirst = new Coords(getPlayer().getPosition().getFirst());
        Coords goal = getGoal();
        // find all the steps to goal
        while (!playerFirst.isIntersecting(goal)){
            if (getPlayer().isHorizontal()){
                if (playerFirst.getY() < goal.getY()){
                    steps.add(new Coords(playerFirst.getX(), playerFirst.getY() + 1));
                    playerFirst.addY(1);
                } else {
                    steps.add(new Coords(playerFirst.getX(), playerFirst.getY() - 1));
                    playerFirst.addY(-1);
                }
            } else {
                if (playerFirst.getX() < goal.getX()){
                    steps.add(new Coords(playerFirst.getX() + 1, playerFirst.getY()));
                    playerFirst.addX(1);
                } else {
                    steps.add(new Coords(playerFirst.getX() - 1, playerFirst.getY()));
                    playerFirst.addX(-1);
                }
            }
        }
        return steps;
    }

    public List<Piece> getAllBlocking(){
        List<Coords> blocking = stepsToGoal();
        List<Piece> blockingPieces = new ArrayList<>();
        updateBoard();
        for (Coords coord : blocking){
            // ignore if the coord is out of bounds
            if (coord.getX() < 0 || coord.getX() >= row || coord.getY() < 0 || coord.getY() >= col){
                continue;
            }
            // ignore if the coord is the same as the player
            if (matrix[coord.getX()][coord.getY()] == 'P'){
                continue;
            }
            if(matrix[coord.getX()][coord.getY()] != '.'){
                char id = matrix[coord.getX()][coord.getY()];
                Piece blockingPiece = new Piece(pieces.get(id));
                if (!blockingPieces.contains(blockingPiece)){
                    blockingPieces.add(blockingPiece);
                }
            }
        }
        return blockingPieces;
    }

    public boolean canMove(Piece piece){
        Character self = piece.getId();
        // check if the piece can move forward or backward
        boolean canMoveForward = isValidMove(self, true);
        boolean canMoveBackward = isValidMove(self, false);
        return canMoveForward || canMoveBackward;
    }

    public List<Character> getPiecesBlockingPiece(Piece piece){
        // get all the pieces that are blocking the given piece
        List<Character> blockingPieces = new ArrayList<>();
        if (canMove(piece)){
            return blockingPieces;
        }
        if (piece.isHorizontal()){
            for (Coords coord : piece.getPosition()){
                int newY = coord.getY() + 1;
                if (newY < col && matrix[coord.getX()][newY] != '.'){
                    char id = matrix[coord.getX()][newY];
                    if (id != piece.getId()){
                        blockingPieces.add(id);
                    }
                }
                newY = coord.getY() - 1;
                if (newY >= 0 && matrix[coord.getX()][newY] != '.'){
                    char id = matrix[coord.getX()][newY];
                    if (id != piece.getId()){
                        blockingPieces.add(id);
                    }
                }
            }
        } else {
            for (Coords coord : piece.getPosition()){
                int newX = coord.getX() + 1;
                if (newX < row && matrix[newX][coord.getY()] != '.'){
                    char id = matrix[newX][coord.getY()];
                    if (id != piece.getId()){
                        blockingPieces.add(id);
                    }
                }
                newX = coord.getX() - 1;
                if (newX >= 0 && matrix[newX][coord.getY()] != '.'){
                    char id = matrix[newX][coord.getY()];
                    if (id != piece.getId()){
                        blockingPieces.add(id);
                    }
                }
            }
        }
        return blockingPieces;
    }

    public List<Character> getPiecesBlockingPiece(Character id){
        Piece piece = pieces.get(id);
        if (piece == null) return new ArrayList<>();
        return getPiecesBlockingPiece(piece);
    }

    public int heuristicByBlockCountAndDistance(){
        List<Piece> blockingPieces = getAllBlocking();
        int count = 0;
        for (Piece piece : blockingPieces){
            if (piece.getId() != 'P'){
                count++;
            }
        }
        Coords playerFirst = new Coords(getPlayer().getPosition().getFirst());
        return count + (int) playerFirst.distanceTo(goal);
    }

    public int heuristicByRecursiveBlock(){
        // for each blocking piece, see if it can move, if it cant, what is the blocking piece that blocks it
        List<Piece> initialBlocking = getAllBlocking(); // find pieces that block the player
        Set<Piece> visitedPieces = new HashSet<>();
        Stack<Piece> blockingPieces = new Stack<>();
        for (Piece piece : initialBlocking){
            if (piece.getId() != 'P'){
                blockingPieces.push(piece);
            }
        }
        int count = 0;
        while (!blockingPieces.isEmpty()){
            Piece piece = blockingPieces.pop();
            if (visitedPieces.contains(piece)){
                continue;
            }
            count++;
            visitedPieces.add(piece);
            List<Character> blocking = getPiecesBlockingPiece(piece);
            for (Character id : blocking){
                Piece blockingPiece = pieces.get(id);
                if (blockingPiece != null && !visitedPieces.contains(blockingPiece)){
                    blockingPieces.push(blockingPiece);
                }
            }
        }
        Coords player = getPlayer().getPosition().getFirst();
        Coords goal = getGoal();
        return 10*count + (int) player.distanceTo(goal);
    }

    public int maxDepth(Piece piece, Set<Piece> visited){
        // find the max depth of the blocking pieces
        if (visited.contains(piece)){
            return 0;
        }
        visited.add(piece);
        int depth = 0;
        for (Character id : getPiecesBlockingPiece(piece)) {
            Piece child = pieces.get(id);
            if (child != null) {
                depth = Math.max(depth, maxDepth(child, visited));
            }
        }
        return depth + 1;
    }

    public int heuristicByMaxDepth(){
        List<Piece> initialBlocking = getAllBlocking(); // find pieces that block the player
        Set<Piece> visitedPieces = new HashSet<>();
        int maxDepth = 0;
        for (Piece piece : initialBlocking){
            if (piece.getId() != 'P'){
                System.out.println("Blocking Piece: " + piece.getId());
                maxDepth = Math.max(maxDepth, maxDepth(piece, visitedPieces));
            }
        }
        return 10*maxDepth + (int) getPlayer().getPosition().getFirst().distanceTo(goal);
    }

    public int getHeuristicByType(String type){
        if (type.equals("BlockCountDistance")){
            return heuristicByBlockCountAndDistance();
        } else if (type.equals("Recursive")){
            return heuristicByRecursiveBlock();
        } else if (type.equals("Max Depth")){
            return heuristicByMaxDepth();
        }
        return 0;
    }

    public void setMatrix(char[][] matrix){
        this.matrix = matrix;
    }

    public void parsePieces(){
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++){
                if(this.matrix[i][j] == '.'){
                    continue;
                }

                char c = this.matrix[i][j];
                Coords coord = new Coords(i, j);
                if (!pieces.containsKey(c)) {
                    Piece piece = new Piece(c);
                    piece.addCoord(coord);
                    addPiece(piece);
                } else {
                    getPieces().get(c).addCoord(coord);
                }
            }
        }
    }

    public boolean isBoardValid(){
        Coords goal = getGoal();
        Piece player = getPlayer();
        if (goal.getX() < 0 || goal.getX() >= row || goal.getY() < 0 || goal.getY() >= col){
            return false;
        }
        if (player.getPosition().isEmpty()){
            return false;
        }
        // check if the player can reach the goal by orientation
        if (player.isHorizontal()){
            return goal.getX() == player.getPosition().getFirst().getX();
        } else {
            return goal.getY() == player.getPosition().getFirst().getY();
        }
    }


//    public int getDependencyDepth(){
//        list<Piece> blockingPieces = getAllBlocking();
//        int depth = 0;
//        return depth;
//    }

//    public Character[] getAllBlocking(){
//
//    }

//    public int dependencyCount(){
//        // count the number of pieces tha
//    }
}
