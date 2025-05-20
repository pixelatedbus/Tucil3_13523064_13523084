package tucil.rhsolver.app;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tooltip;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.util.Duration;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application {
    private StackPane previewPane;
    private GridPane previewGrid;
    private Label fileLabel;
    private GridPane boardView;
    private Board boardToSolve;
    private final Map<Character, Color> colorMap = new HashMap<>();
    private char[][] manualBoard;
    private int manualRows = 6, manualCols = 6;
    private FlowPane piecesPalette;
    private Map<Character, Boolean> pieceOnBoardMap = new HashMap<>();
    private int[] finishPosition = null;
    private static final char FINISH_MARKER = '!';
    private final Map<Character, StackPane> piecePreviews = new HashMap<>();
    private char draggedPiece = 'P';
    private int draggedPieceSize = 2;
    private boolean draggedPieceHorizontal = true;
    private char dragSourcePiece = '.';
    private int[] dragSourcePosition = null;
    private boolean isDraggingFromBoard = false;

    private static final int POOL_HORIZONTAL_2 = 0;
    private static final int POOL_VERTICAL_2 = 1;
    private static final int POOL_HORIZONTAL_3 = 2;
    private static final int POOL_VERTICAL_3 = 3;
    private static final int POOL_HORIZONTAL_4 = 4;
    private static final int POOL_VERTICAL_4 = 5;
    private static final int POOL_HORIZONTAL_5 = 6;
    private static final int POOL_VERTICAL_5 = 7;
    private static final int POOL_HORIZONTAL_6 = 8;
    private static final int POOL_VERTICAL_6 = 9;
    private int currentPool = POOL_HORIZONTAL_2;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        colorMap.put('P', Color.RED);
        colorMap.put(FINISH_MARKER, Color.GREEN);

        for (char c = 'A'; c <= 'Z'; c++) {
            pieceOnBoardMap.put(c, false);
        }

        primaryStage.setTitle("Rush Hour Solver");
        primaryStage.setResizable(true);

        ScrollPane scrollRoot = new ScrollPane();
        scrollRoot.setFitToWidth(true);
        scrollRoot.setFitToHeight(true);
        scrollRoot.setPannable(true);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f5f5f5;");

        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 10;");

        HBox modeSelector = new HBox(10);
        modeSelector.setStyle("-fx-background-color: #eaeaea; -fx-padding: 8; -fx-border-radius: 5;");
        ToggleGroup inputModeGroup = new ToggleGroup();
        RadioButton fileMode = new RadioButton("Load From File");
        RadioButton manualMode = new RadioButton("Manual Board Editor");
        fileMode.setToggleGroup(inputModeGroup);
        manualMode.setToggleGroup(inputModeGroup);
        fileMode.setSelected(true);
        fileMode.setStyle("-fx-font-weight: bold;");
        manualMode.setStyle("-fx-font-weight: bold;");
        modeSelector.getChildren().addAll(fileMode, manualMode);

        Button loadButton = new Button("Load Board File");
        loadButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        fileLabel = new Label("No file loaded.");
        fileLabel.setStyle("-fx-font-size: 12;");

        TextField rowField = new TextField("6");
        TextField colField = new TextField("6");
        rowField.setPrefWidth(40);
        colField.setPrefWidth(40);
        rowField.setStyle("-fx-font-size: 12;");
        colField.setStyle("-fx-font-size: 12;");
        Button applySizeButton = new Button("Apply Size");
        applySizeButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox sizeEditor = new HBox(5, new Label("Rows:"), rowField, new Label("Cols:"), colField, applySizeButton);
        sizeEditor.setVisible(false);
        sizeEditor.setStyle("-fx-background-color: #eaeaea; -fx-padding: 8; -fx-border-radius: 5;");

        // Pool toggle buttons
        HBox poolControls = new HBox(10);
        poolControls.setAlignment(Pos.CENTER_LEFT);
        poolControls.setPadding(new Insets(5));
        poolControls.setVisible(false);
        poolControls.setStyle("-fx-background-color: #eaeaea; -fx-padding: 8; -fx-border-radius: 5;");

        Label poolLabel = new Label("Piece Pool:");
        poolLabel.setStyle("-fx-font-weight: bold;");

        ToggleGroup poolToggleGroup = new ToggleGroup();

        ToggleButton pool2H = new ToggleButton("2-Size H");
        pool2H.setToggleGroup(poolToggleGroup);
        pool2H.setSelected(true);
        pool2H.setUserData(POOL_HORIZONTAL_2);

        ToggleButton pool2V = new ToggleButton("2-Size V");
        pool2V.setToggleGroup(poolToggleGroup);
        pool2V.setUserData(POOL_VERTICAL_2);

        ToggleButton pool3H = new ToggleButton("3-Size H");
        pool3H.setToggleGroup(poolToggleGroup);
        pool3H.setUserData(POOL_HORIZONTAL_3);

        ToggleButton pool3V = new ToggleButton("3-Size V");
        pool3V.setToggleGroup(poolToggleGroup);
        pool3V.setUserData(POOL_VERTICAL_3);

        ToggleButton pool4H = new ToggleButton("4-Size H");
        pool4H.setToggleGroup(poolToggleGroup);
        pool4H.setUserData(POOL_HORIZONTAL_4);

        ToggleButton pool4V = new ToggleButton("4-Size V");
        pool4V.setToggleGroup(poolToggleGroup);
        pool4V.setUserData(POOL_VERTICAL_4);

        ToggleButton pool5H = new ToggleButton("5-Size H");
        pool5H.setToggleGroup(poolToggleGroup);
        pool5H.setUserData(POOL_HORIZONTAL_5);

        ToggleButton pool5V = new ToggleButton("5-Size V");
        pool5V.setToggleGroup(poolToggleGroup);
        pool5V.setUserData(POOL_VERTICAL_5);

        ToggleButton pool6H = new ToggleButton("6-Size H");
        pool6H.setToggleGroup(poolToggleGroup);
        pool6H.setUserData(POOL_HORIZONTAL_6);

        ToggleButton pool6V = new ToggleButton("6-Size V");
        pool6V.setToggleGroup(poolToggleGroup);
        pool6V.setUserData(POOL_VERTICAL_6);

        poolControls.getChildren().addAll(poolLabel, pool2H, pool2V, pool3H, pool3V, pool4H, pool4V, pool5H, pool5V, pool6H, pool6V);

        pool2H.setOnAction(e -> {
            currentPool = POOL_HORIZONTAL_2;
            updatePiecesPalette();
        });

        pool2V.setOnAction(e -> {
            currentPool = POOL_VERTICAL_2;
            updatePiecesPalette();
        });

        pool3H.setOnAction(e -> {
            currentPool = POOL_HORIZONTAL_3;
            updatePiecesPalette();
        });

        pool3V.setOnAction(e -> {
            currentPool = POOL_VERTICAL_3;
            updatePiecesPalette();
        });

        pool4H.setOnAction(e -> {
            currentPool = POOL_HORIZONTAL_4;
            updatePiecesPalette();
        });

        pool4V.setOnAction(e -> {
            currentPool = POOL_VERTICAL_4;
            updatePiecesPalette();
        });

        pool5H.setOnAction(e -> {
            currentPool = POOL_HORIZONTAL_5;
            updatePiecesPalette();
        });

        pool5V.setOnAction(e -> {
            currentPool = POOL_VERTICAL_5;
            updatePiecesPalette();
        });

        pool6H.setOnAction(e -> {
            currentPool = POOL_HORIZONTAL_6;
            updatePiecesPalette();
        });

        pool6V.setOnAction(e -> {
            currentPool = POOL_VERTICAL_6;
            updatePiecesPalette();
        });

        Button finishMarkerBtn = new Button("Place Finish Marker");
        finishMarkerBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        finishMarkerBtn.setTooltip(new Tooltip("Drag finish marker to the board"));
        finishMarkerBtn.setVisible(false);

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        resetButton.setOnAction(e -> {
            manualBoard = new char[manualRows][manualCols];
            for (int i = 0; i < manualRows; i++) for (int j = 0; j < manualCols; j++) manualBoard[i][j] = '.';
            finishPosition = null;
            for (char c : pieceOnBoardMap.keySet()) {
                pieceOnBoardMap.put(c, false);
            }
            renderManualEditor();
        });
        resetButton.setVisible(false);


        piecesPalette = new FlowPane(10, 10);
        piecesPalette.setPadding(new Insets(10));
        piecesPalette.setAlignment(Pos.CENTER_LEFT);
        piecesPalette.setPrefHeight(120);
        piecesPalette.setVisible(false);
        piecesPalette.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        loadButton.setOnAction(e -> loadBoardFile(primaryStage));
        applySizeButton.setOnAction(e -> {
            manualRows = Integer.parseInt(rowField.getText());
            manualCols = Integer.parseInt(colField.getText());
            manualBoard = new char[manualRows][manualCols];
            for (int i = 0; i < manualRows; i++) for (int j = 0; j < manualCols; j++) manualBoard[i][j] = '.';
            finishPosition = null;
            for (char c : pieceOnBoardMap.keySet()) {
                pieceOnBoardMap.put(c, false);
            }
            renderManualEditor();
        });

        fileMode.setOnAction(e -> {
            loadButton.setVisible(true);
            fileLabel.setVisible(true);
            sizeEditor.setVisible(false);
            finishMarkerBtn.setVisible(false);
            resetButton.setVisible(false);
            poolControls.setVisible(false);
            piecesPalette.setVisible(false);
            renderBoard(boardToSolve);
        });

        manualMode.setOnAction(e -> {
            loadButton.setVisible(false);
            fileLabel.setVisible(false);
            sizeEditor.setVisible(true);
            finishMarkerBtn.setVisible(true);
            resetButton.setVisible(true);
            poolControls.setVisible(true);
            piecesPalette.setVisible(true);
            if (manualBoard == null) {
                manualBoard = new char[manualRows][manualCols];
                for (int i = 0; i < manualRows; i++) for (int j = 0; j < manualCols; j++) manualBoard[i][j] = '.';
            }
            updatePiecesPalette();
            renderManualEditor();
        });

        HBox editorSection = new HBox(20);
        editorSection.setAlignment(Pos.CENTER_LEFT);

        VBox leftPanel = new VBox(10, finishMarkerBtn, resetButton, piecesPalette);
        leftPanel.setPadding(new Insets(10));

        topSection.getChildren().addAll(modeSelector, loadButton, fileLabel, sizeEditor, poolControls, leftPanel);

        boardView = new GridPane();
        boardView.setHgap(2);
        boardView.setVgap(2);
        boardView.setAlignment(Pos.CENTER);
        boardView.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-padding: 10;");

        HBox bottomSection = new HBox(10);
        bottomSection.setPadding(new Insets(10));
        bottomSection.setAlignment(Pos.CENTER_LEFT);
        bottomSection.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");

        ComboBox<String> algoSelector = new ComboBox<>();
        algoSelector.getItems().addAll("GBFS", "UCS", "A*", "IDA*", "SA");
        algoSelector.setValue("GBFS");
        algoSelector.setStyle("-fx-font-size: 12;");

        ComboBox<String> heuristicSelector = new ComboBox<>();
        heuristicSelector.getItems().addAll( "Recursive", "Max Depth", "BlockCount");
        heuristicSelector.setValue("Recursive");
        heuristicSelector.setStyle("-fx-font-size: 12;");

        Button solveButton = new Button("Solve");
        solveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        Label algoStatus = new Label("Algorithm: GBFS");
        Label heuristicStatus = new Label("Heuristic: Recursive");
        algoStatus.setStyle("-fx-font-size: 12;");
        heuristicStatus.setStyle("-fx-font-size: 12;");

        algoSelector.setOnAction(e -> algoStatus.setText("Algorithm: " + algoSelector.getValue()));
        heuristicSelector.setOnAction(e -> heuristicStatus.setText("Heuristic: " + heuristicSelector.getValue()));

        solveButton.setOnAction(e -> {
            String selectedAlgo = algoSelector.getValue();
            algoStatus.setText("Solving with " + selectedAlgo + "...");
            heuristicStatus.setText("Heuristic: " + heuristicSelector.getValue());

            String selectedHeuristic = heuristicSelector.getValue();
            if (selectedHeuristic.equals("BlockCountDistance")) {
                heuristicStatus.setText("Heuristic: BlockCountDistance");
            } else if (selectedHeuristic.equals("Recursive")) {
                heuristicStatus.setText("Heuristic: Recursive");
            } else {
                heuristicStatus.setText("Heuristic: Max Depth");
            }

            Board toSolve;
            if (manualMode.isSelected()) {
                toSolve = new Board(manualRows, manualCols);
                toSolve.setMatrix(manualBoard);
                toSolve.parsePieces();
                if (finishPosition != null) {
                    Coords finishCoord = new Coords(finishPosition[0], finishPosition[1]);
                    toSolve.setGoal(finishCoord);
                }
                toSolve.updateBoard();
            } else {
                if (this.boardToSolve == null) {
                    algoStatus.setText("Please load a board first.");
                    return;
                }
                toSolve = this.boardToSolve;
            }

            Instant startTime = Instant.now();

            Solver solver = new Solver();
            Board goalBoard = solver.GameSolver(toSolve, selectedAlgo, selectedHeuristic);

            java.time.Duration solvingTime = java.time.Duration.between(startTime, Instant.now());
            long millis = solvingTime.toMillis();
            String timeString;

            if (millis < 1000) {
                timeString = millis + " milliseconds";
            } else {
                double seconds = millis / 1000.0;
                timeString = String.format("%.2f seconds", seconds);
            }

            if (goalBoard == null) {
                algoStatus.setText("No solution found.");
                return;
            }

            List<Board> steps = solver.getResultInOrder(goalBoard);
            Timeline timeline = new Timeline();
            int delayMillis = 300;
            for (int i = 0; i < steps.size(); i++) {
                Board boardStep = steps.get(i);
                KeyFrame keyFrame = new KeyFrame(Duration.millis(i * delayMillis), event -> renderBoard(boardStep));
                timeline.getKeyFrames().add(keyFrame);
            }
            timeline.setOnFinished(ev -> {
                String resultText = String.format("Solved in %d visited nodes using %s (Time: %s)",
                        solver.getVisited(), selectedAlgo, timeString);
                algoStatus.setText(resultText);
            });
            timeline.play();
        });

        bottomSection.getChildren().addAll(algoSelector, heuristicSelector, solveButton, algoStatus, heuristicStatus);

        root.setTop(topSection);
        root.setCenter(boardView);
        root.setBottom(bottomSection);

        scrollRoot.setContent(root);

        Scene scene = new Scene(scrollRoot, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        createDraggableFinishMarker(finishMarkerBtn);
    }

    private void updatePiecesPalette() {
        piecesPalette.getChildren().clear();
        piecePreviews.clear();

        // int size = (currentPool == POOL_HORIZONTAL_2 || currentPool == POOL_VERTICAL_2) ? 2 : 3;
        // boolean isHorizontal = (currentPool == POOL_HORIZONTAL_2 || currentPool == POOL_HORIZONTAL_3);
        // currentpool can go from 0 to 9, 0 and 1 are 2-size, 2 and 3 are 3-size, 4 and 5 are 4-size, 6 and 7 are 5-size, and 8 and 9 are 6-size
        // orientation horizontal is even, vertical is odd
        int size = (currentPool / 2) + 2;
        boolean isHorizontal = (currentPool % 2 == 0);

        for (char c = 'A'; c <= 'Z'; c++) {
            createDraggablePiece(piecesPalette, c, size, isHorizontal);
        }

        updatePiecePaletteAvailability();
    }

    private void updatePiecePaletteAvailability() {
        for (char c = 'A'; c <= 'Z'; c++) {
            StackPane pieceContainer = piecePreviews.get(c);
            if (pieceContainer != null) {
                boolean isOnBoard = pieceOnBoardMap.getOrDefault(c, false);

                pieceContainer.setOpacity(isOnBoard ? 0.4 : 1.0);
                pieceContainer.setDisable(isOnBoard);

                if (isOnBoard) {
                    for (int i = 0; i < pieceContainer.getChildren().size(); i++) {
                        if (pieceContainer.getChildren().get(i) instanceof GridPane) {
                            GridPane grid = (GridPane) pieceContainer.getChildren().get(i);
                            for (int j = 0; j < grid.getChildren().size(); j++) {
                                if (grid.getChildren().get(j) instanceof Rectangle) {
                                    Rectangle rect = (Rectangle) grid.getChildren().get(j);
                                    rect.setOpacity(1.0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadBoardFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Rush Hour Board File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            fileLabel.setText("Loaded: " + selectedFile.getName());
            this.boardToSolve = IO.readInput(selectedFile.getAbsolutePath());
            assert boardToSolve != null;
            this.boardToSolve.updateBoard();
            renderBoard(this.boardToSolve);
        } else {
            fileLabel.setText("File loading cancelled.");
        }
    }

    private void renderBoard(Board board) {
        if (board == null) return;
        boardView.getChildren().clear();
        char[][] matrix = board.getMatrix();
        int rows = matrix.length;
        int cols = matrix[0].length;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = matrix[i][j];
                Rectangle cell = new Rectangle(40, 40);
                cell.setStroke(Color.BLACK);
                if (c == '.') {
                    cell.setFill(Color.LIGHTGRAY);
                } else {
                    colorMap.putIfAbsent(c, Color.color(Math.random(), Math.random(), Math.random()));
                    cell.setFill(colorMap.get(c));
                }

                StackPane cellPane = new StackPane(cell);

                if (finishPosition != null && finishPosition[0] == i && finishPosition[1] == j) {
                    Text finishText = new Text("F");
                    finishText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    cellPane.getChildren().add(finishText);
                }

                boardView.add(cellPane, j, i);
            }
        }
    }

    private void renderManualEditor() {
        boardView.getChildren().clear();

        for (char c = 'A'; c <= 'Z'; c++) {
            pieceOnBoardMap.put(c, false);
        }

        for (int i = 0; i < manualRows; i++) {
            for (int j = 0; j < manualCols; j++) {
                char piece = manualBoard[i][j];
                if (piece != '.' && piece != FINISH_MARKER) {
                    pieceOnBoardMap.put(piece, true);
                }
            }
        }

        updatePiecePaletteAvailability();

        for (int i = 0; i < manualRows; i++) {
            for (int j = 0; j < manualCols; j++) {
                Rectangle cell = new Rectangle(40, 40);
                cell.setStroke(Color.BLACK);
                char c = manualBoard[i][j];

                if (c == '.') {
                    cell.setFill(Color.LIGHTGRAY);
                } else {
                    colorMap.putIfAbsent(c, Color.color(Math.random(), Math.random(), Math.random()));
                    cell.setFill(colorMap.get(c));
                }

                StackPane cellPane = new StackPane(cell);

                if (finishPosition != null && finishPosition[0] == i && finishPosition[1] == j) {
                    Text finishText = new Text("F");
                    finishText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    cellPane.getChildren().add(finishText);
                }

                final int fi = i, fj = j;

                cell.setOnMouseClicked(event -> {
                    if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                        removePieceAtPosition(fi, fj);
                    }
                });

                setupBoardCellDragHandlers(cellPane, fi, fj);

                if (c != '.' && c != FINISH_MARKER) {
                    setupBoardPieceDragSource(cellPane, fi, fj, c);
                }

                boardView.add(cellPane, j, i);
            }
        }
    }

    private void createDraggablePiece(FlowPane palette, char pieceChar, int size, boolean isHorizontal) {
        GridPane piecePreview = new GridPane();
        piecePreview.setHgap(1);
        piecePreview.setVgap(1);

        Color pieceColor = colorMap.computeIfAbsent(pieceChar,
                k -> Color.color(Math.random(), Math.random(), Math.random()));

        int rows = isHorizontal ? 1 : size;
        int cols = isHorizontal ? size : 1;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Rectangle cell = new Rectangle(30, 30);
                cell.setFill(pieceColor);
                if(pieceChar == 'P'){
                    cell.setStroke(Color.BLACK);
                    cell.setStrokeWidth(2.0);
                } else {
                    cell.setStroke(pieceColor.darker());
                }
                piecePreview.add(cell, j, i);
            }
        }

        StackPane container = new StackPane(piecePreview);
        container.setStyle("-fx-padding: 5; -fx-background-color: white; -fx-border-color: #ccc;");


        container.setUserData(new PieceData(pieceChar, size, isHorizontal));

        container.setOnDragDetected(e -> {
            if (pieceOnBoardMap.getOrDefault(pieceChar, false)) {
                return;
            }

            PieceData data = (PieceData) container.getUserData();
            draggedPiece = data.piece;
            draggedPieceSize = data.size;
            draggedPieceHorizontal = data.isHorizontal;

            Dragboard db = container.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(draggedPiece));
            db.setContent(content);
            db.setDragView(container.snapshot(null, null));
            e.consume();
        });

        palette.getChildren().add(container);
        piecePreviews.put(pieceChar, container);
    }

    private void createDraggableFinishMarker(Button finishMarkerBtn) {
        finishMarkerBtn.setOnDragDetected(e -> {
            draggedPiece = FINISH_MARKER;
            draggedPieceSize = 1;
            draggedPieceHorizontal = true;

            Dragboard db = finishMarkerBtn.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(FINISH_MARKER));
            db.setContent(content);


            StackPane finishMarker = new StackPane();
            Rectangle bg = new Rectangle(30, 30);
            bg.setFill(Color.GREEN);
            bg.setStroke(Color.BLACK);
            Text text = new Text("!");
            text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            finishMarker.getChildren().addAll(bg, text);

            db.setDragView(finishMarker.snapshot(null, null));
            e.consume();
        });
    }

    private void setupBoardCellDragHandlers(StackPane cellPane, int row, int col) {
        cellPane.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.ANY); // Accept both COPY and MOVE
            }
            event.consume();
        });

        cellPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                if (isDraggingFromBoard) {
                    success = movePieceOnBoard(row, col);
                } else {
                    // Handle placing new pieces from palette
                    success = placePieceAtPosition(row, col, draggedPiece, draggedPieceSize, draggedPieceHorizontal);
                }
                event.setDropCompleted(success);
            } else {
                event.setDropCompleted(false);
            }

            isDraggingFromBoard = false;
            dragSourcePiece = '.';
            dragSourcePosition = null;

            event.consume();
        });
    }

    private void setupBoardPieceDragSource(StackPane cellPane, int row, int col, char pieceChar) {
        cellPane.setOnDragDetected(e -> {
            boolean isHorizontal = false;
            int pieceSize = 1;

            int horizontalSize = 1;
            for (int j = col + 1; j < manualCols; j++) {
                if (manualBoard[row][j] == pieceChar) {
                    horizontalSize++;
                } else {
                    break;
                }
            }
            for (int j = col - 1; j >= 0; j--) {
                if (manualBoard[row][j] == pieceChar) {
                    horizontalSize++;
                } else {
                    break;
                }
            }

            int verticalSize = 1;
            for (int i = row + 1; i < manualRows; i++) {
                if (manualBoard[i][col] == pieceChar) {
                    verticalSize++;
                } else {
                    break;
                }
            }
            for (int i = row - 1; i >= 0; i--) {
                if (manualBoard[i][col] == pieceChar) {
                    verticalSize++;
                } else {
                    break;
                }
            }

            if (horizontalSize > verticalSize) {
                isHorizontal = true;
                pieceSize = horizontalSize;
            } else {
                isHorizontal = false;
                pieceSize = verticalSize;
            }

            int startRow = row;
            int startCol = col;
            if (isHorizontal) {
                while (startCol > 0 && manualBoard[row][startCol - 1] == pieceChar) {
                    startCol--;
                }
            } else {
                while (startRow > 0 && manualBoard[startRow - 1][col] == pieceChar) {
                    startRow--;
                }
            }

            draggedPiece = pieceChar;
            draggedPieceSize = pieceSize;
            draggedPieceHorizontal = isHorizontal;
            dragSourcePiece = pieceChar;
            dragSourcePosition = new int[]{startRow, startCol};
            isDraggingFromBoard = true;

            Dragboard db = cellPane.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(pieceChar));
            db.setContent(content);

            GridPane piecePreview = new GridPane();
            piecePreview.setHgap(1);
            piecePreview.setVgap(1);

            Color pieceColor = colorMap.get(pieceChar);
            int previewRows = isHorizontal ? 1 : pieceSize;
            int previewCols = isHorizontal ? pieceSize : 1;

            for (int i = 0; i < previewRows; i++) {
                for (int j = 0; j < previewCols; j++) {
                    Rectangle cell = new Rectangle(30, 30);
                    cell.setFill(pieceColor);
                    cell.setStroke(pieceColor.darker());
                    piecePreview.add(cell, j, i);
                }
            }

            StackPane previewContainer = new StackPane(piecePreview);
            db.setDragView(previewContainer.snapshot(null, null));

            e.consume();
        });
    }

    private boolean movePieceOnBoard(int targetRow, int targetCol) {
        if (dragSourcePosition == null || dragSourcePiece == '.') {
            return false;
        }

        int sourceRow = dragSourcePosition[0];
        int sourceCol = dragSourcePosition[1];
        char pieceToMove = dragSourcePiece;

        if (sourceRow == targetRow && sourceCol == targetCol) {
            return true;
        }

        for (int i = 0; i < manualRows; i++) {
            for (int j = 0; j < manualCols; j++) {
                if (manualBoard[i][j] == pieceToMove) {
                    manualBoard[i][j] = '.';
                }
            }
        }


        boolean canPlace = true;
        int pieceRows = draggedPieceHorizontal ? 1 : draggedPieceSize;
        int pieceCols = draggedPieceHorizontal ? draggedPieceSize : 1;

        if (targetRow + pieceRows > manualRows || targetCol + pieceCols > manualCols) {
            placePieceAtPosition(sourceRow, sourceCol, pieceToMove, draggedPieceSize, draggedPieceHorizontal);
            return false;
        }

        for (int i = 0; i < pieceRows; i++) {
            for (int j = 0; j < pieceCols; j++) {
                if (targetRow + i >= manualRows || targetCol + j >= manualCols ||
                        (manualBoard[targetRow + i][targetCol + j] != '.' &&
                                manualBoard[targetRow + i][targetCol + j] != pieceToMove)) {
                    canPlace = false;
                    break;
                }
            }
            if (!canPlace) break;
        }

        if (canPlace) {
            placePieceAtPosition(targetRow, targetCol, pieceToMove, draggedPieceSize, draggedPieceHorizontal);
            return true;
        } else {
            placePieceAtPosition(sourceRow, sourceCol, pieceToMove, draggedPieceSize, draggedPieceHorizontal);
            return false;
        }
    }

    private boolean placePieceAtPosition(int row, int col, char piece, int size, boolean horizontal) {
        if (piece == FINISH_MARKER) {
            finishPosition = new int[]{row, col};
            renderManualEditor();
            return true;
        }

        if (pieceOnBoardMap.getOrDefault(piece, false)) {
            for (int i = 0; i < manualRows; i++) {
                for (int j = 0; j < manualCols; j++) {
                    if (manualBoard[i][j] == piece) {
                        manualBoard[i][j] = '.';
                    }
                }
            }
            pieceOnBoardMap.put(piece, false);
        }

        int pieceRows = horizontal ? 1 : size;
        int pieceCols = horizontal ? size : 1;

        if (row + pieceRows > manualRows || col + pieceCols > manualCols) {
            System.out.println("Piece doesn't fit at this position");
            return false;
        }

        for (int i = 0; i < pieceRows; i++) {
            for (int j = 0; j < pieceCols; j++) {
                if (manualBoard[row + i][col + j] != '.' && manualBoard[row + i][col + j] != piece) {
                    System.out.println("Space already occupied by another piece");
                    return false;
                }
            }
        }

        for (int i = 0; i < pieceRows; i++) {
            for (int j = 0; j < pieceCols; j++) {
                manualBoard[row + i][col + j] = piece;
            }
        }

        pieceOnBoardMap.put(piece, true);
        renderManualEditor();
        return true;
    }

    private void removePieceAtPosition(int row, int col) {
        char pieceToRemove = manualBoard[row][col];

        if (pieceToRemove == FINISH_MARKER || (finishPosition != null && finishPosition[0] == row && finishPosition[1] == col)) {
            finishPosition = null;
            renderManualEditor();
            return;
        }

        if (pieceToRemove == '.') {
            return;
        }

        pieceOnBoardMap.put(pieceToRemove, false);

        boolean[][] visited = new boolean[manualRows][manualCols];
        removeConnectedPieces(row, col, pieceToRemove, visited);

        renderManualEditor();
    }

    private void removeConnectedPieces(int row, int col, char pieceChar, boolean[][] visited) {
        if (row < 0 || row >= manualRows || col < 0 || col >= manualCols) {
            return;
        }

        if (visited[row][col] || manualBoard[row][col] != pieceChar) {
            return;
        }

        visited[row][col] = true;
        manualBoard[row][col] = '.';

        removeConnectedPieces(row + 1, col, pieceChar, visited);
        removeConnectedPieces(row - 1, col, pieceChar, visited);
        removeConnectedPieces(row, col + 1, pieceChar, visited);
        removeConnectedPieces(row, col - 1, pieceChar, visited);
    }

    private static class PieceData {
        public final char piece;
        public final int size;
        public final boolean isHorizontal;

        public PieceData(char piece, int size, boolean isHorizontal) {
            this.piece = piece;
            this.size = size;
            this.isHorizontal = isHorizontal;
        }
    }
}

//Compile
//javac --module-path "C:\javafx-sdk-24.0.1\lib" --add-modules javafx.controls,javafx.fxml -d bin src\*.java
//Run
//java --module-path "C:\javafx-sdk-24.0.1\lib" --add-modules javafx.controls,javafx.fxml -cp bin src.Main