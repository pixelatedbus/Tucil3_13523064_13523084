package tucil.rhsolver.app;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IO {
    public static Board readInput(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String[] firstLine = br.readLine().split(" ");
            int N = Integer.parseInt(firstLine[0]);
            int M = Integer.parseInt(firstLine[1]);
            int pieceCount = Integer.parseInt(br.readLine().trim());

            Board board = new Board(N, M);

            List<String> allLines = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                allLines.add(line);
            }

            int matrixStartRow = 0;

            if (allLines.size() > N) {
                String firstDataLine = allLines.get(0).trim();
                if (firstDataLine.length() == 1 && firstDataLine.charAt(0) == 'K') {
                    matrixStartRow = 1;
                    int kCol = allLines.get(0).indexOf('K');
                    board.setGoal(new Coords(-1, kCol));
                }
            }

            Coords kPosition = null;

            for (int i = matrixStartRow; i < Math.min(N + matrixStartRow, allLines.size()); i++) {
                int matrixRow = i - matrixStartRow;

                String currentLine = allLines.get(i);

                boolean hasLeadingK = false;
                int firstNonSpaceIndex = -1;

                for (int j = 0; j < currentLine.length(); j++) {
                    if (currentLine.charAt(j) != ' ') {
                        firstNonSpaceIndex = j;
                        break;
                    }
                }

                if (firstNonSpaceIndex != -1 && currentLine.charAt(firstNonSpaceIndex) == 'K') {
                    kPosition = new Coords(matrixRow, -1);
                    hasLeadingK = true;
                }

                int matrixCol = 0;
                for (int j = 0; j < currentLine.length() && matrixCol < M; j++) {
                    char c = currentLine.charAt(j);

                    if (c == ' ') continue;

                    if (c == 'K' && matrixCol >= M) {
                        kPosition = new Coords(matrixRow, M);
                        continue;
                    }

                    if (c == 'K' && hasLeadingK && j == firstNonSpaceIndex) {
                        continue;
                    }

                    if (c == '.') {
                        matrixCol++;
                        continue;
                    }

                    if (c == 'K' && matrixCol < M) {
                        kPosition = new Coords(matrixRow, matrixCol);
                        matrixCol++;
                        continue;
                    }

                    Coords coord = new Coords(matrixRow, matrixCol);
                    if (!board.getPieces().containsKey(c)) {
                        Piece piece = new Piece(c);
                        piece.addCoord(coord);
                        board.addPiece(piece);
                    } else {
                        board.getPieces().get(c).addCoord(coord);
                    }

                    matrixCol++;
                }

                if (currentLine.length() > matrixCol) {
                    for (int j = matrixCol; j < currentLine.length(); j++) {
                        if (currentLine.charAt(j) == 'K') {
                            kPosition = new Coords(matrixRow, M);
                            break;
                        }
                    }
                }
            }

            if (kPosition == null && matrixStartRow + N < allLines.size()) {
                for (int i = matrixStartRow + N; i < allLines.size(); i++) {
                    String belowLine = allLines.get(i);
                    if (belowLine.contains("K")) {
                        int kCol = belowLine.indexOf('K');
                        kPosition = new Coords(N, kCol);
                    }
                }
            }

            if (kPosition != null) {
                board.setGoal(kPosition);
            }

            // normalize goal position
            if (board.getGoal().getX() == -1) {
                board.getGoal().setX(0);
            }
            if (board.getGoal().getY() == -1) {
                board.getGoal().setY(0);
            }
            if(board.getGoal().getY() > M-1){
                board.getGoal().setY(M-1);
            }
            if(board.getGoal().getX() > N-1){
                board.getGoal().setX(N);
            }
            return board;
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }

    public static void saveResult(List<Board> result){

    }
}