import java.awt.*;
import java.util.*;
import java.util.List;

class CustomNode {
    int[][] puzzle; // The puzzle configuration
    int g; // Cost from start to current node
    int h; // Heuristic cost (Manhattan distance)
    int f; // f = g + h
    int zeroRow, zeroCol; // Position of 0 in the puzzle
    String moves; // String to keep track of moves

    // Constructor
    public CustomNode(int[][] puzzle, int g, int h, int zeroRow, int zeroCol, String moves) {
        this.puzzle = puzzle;
        this.g = g;
        this.h = h;
        this.f = g + h;
        this.zeroRow = zeroRow;
        this.zeroCol = zeroCol;
        this.moves = moves;
    }
}

// This class has methods to compute the Manhattan distance heuristic, generate successor states, solve the problem, and more.
public class CustomPuzzleSolver {
    // Constants for colors and line thickness
    private static final Color backgroundColor = new Color(145, 234, 255);
    private static final Color boxColor = new Color(31, 160, 239);
    private static final double lineThickness = 0.02;

    private static int[][] goalStates; // The goal state of the puzzle

    // Method to calculate the Manhattan distance heuristic for a given puzzle configuration
    public static int calculateManhattanDistance(int[][] puzzle) {
        int distance = 0;
        int n = puzzle.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int value = puzzle[i][j];
                if (value != 0) {
                    int targetRow = (value - 1) / n; // Expected row
                    int targetCol = (value - 1) % n; // Expected column
                    distance += Math.abs(i - targetRow) + Math.abs(j - targetCol);
                }
            }
        }
        return distance;
    }

    // Method to check if a puzzle configuration is the goal state
    public static boolean isGoalState(int[][] puzzle) {
        int n = puzzle.length;
        int count = 1;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (puzzle[i][j] != count % (n * n)) {
                    return false;
                }
                count++;
            }
        }
        return true;
    }

    // Method to generate successor nodes for a given puzzle configuration
    public static List<CustomNode> generateSuccessors(CustomNode node) {
        List<CustomNode> successors = new ArrayList<>();
        int[][] moves = {{0, 1}, {0, -1}, {-1, 0}, {1, 0}}; // Right, Left, Up, Down
        char[] moveChars = {'R', 'L', 'U', 'D'};
        for (int i = 0; i < moves.length; i++) {
            int[] move = moves[i];
            int newRow = node.zeroRow + move[0];
            int newCol = node.zeroCol + move[1];
            if (newRow >= 0 && newRow < node.puzzle.length && newCol >= 0 && newCol < node.puzzle.length) {
                int[][] newPuzzle = deepCopy(node.puzzle);
                newPuzzle[node.zeroRow][node.zeroCol] = newPuzzle[newRow][newCol];
                newPuzzle[newRow][newCol] = 0;
                successors.add(new CustomNode(newPuzzle, node.g + 1, calculateManhattanDistance(newPuzzle), newRow, newCol, node.moves + moveChars[i]));
            }
        }
        return successors;
    }

    // Method to perform a deep copy of a 2D array
    public static int[][] deepCopy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    // Method to solve the puzzle using the A* algorithm
    public static String solvePuzzle(int[][] puzzle) {
        PriorityQueue<CustomNode> open = new PriorityQueue<>(Comparator.comparingInt(a -> a.f));
        Set<String> closed = new HashSet<>();
        setGoalStates(new int[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}});
        int zeroRow = -1, zeroCol = -1;
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                if (puzzle[i][j] == 0) {
                    zeroRow = i;
                    zeroCol = j;
                    break;
                }
            }
        }
        CustomNode startNode = new CustomNode(puzzle, 0, calculateManhattanDistance(puzzle), zeroRow, zeroCol, "");
        open.add(startNode);
        while (!open.isEmpty()) {
            CustomNode currentNode = open.poll();
            if (isGoalState(currentNode.puzzle)) {
                return currentNode.moves;
            }
            closed.add(Arrays.deepToString(currentNode.puzzle));
            List<CustomNode> successors = generateSuccessors(currentNode);
            for (CustomNode successor : successors) {
                if (!closed.contains(Arrays.deepToString(successor.puzzle))) {
                    open.add(successor);
                }
            }
        }
        return "No solution found.";
    }

    // Method to generate a random start state for the puzzle
    public static int[][] generateRandomStartState() {
        Random random = new Random();
        int[][] puzzle = new int[3][3];
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                puzzle[i][j] = numbers.get(index++);
            }
        }
        while (!isSolvable(puzzle)) {
            Collections.shuffle(numbers);
            index = 0;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    puzzle[i][j] = numbers.get(index++);
                }
            }
        }
        return puzzle;
    }

    // Method to check if the puzzle is solvable using inversions
    public static boolean isSolvable(int[][] puzzle) {
        int inversionCount = countInversions(puzzle);
        return inversionCount % 2 == 0; // Even inversions imply a solvable puzzle
    }

    // Method to count the number of inversions in the puzzle
    public static int countInversions(int[][] puzzle) {
        int[] flattened = flattenPuzzle(puzzle);
        int inversions = 0;
        for (int i = 0; i < flattened.length - 1; i++) {
            for (int j = i + 1; j < flattened.length; j++) {
                if (flattened[i] != 0 && flattened[j] != 0 && flattened[i] > flattened[j]) {
                    if (flattened[i] > flattened[j]) {
                        inversions++;
                    }
                }
            }
        }
        return inversions;
    }

    // Method to flatten the puzzle into a single-dimensional array
    public static int[] flattenPuzzle(int[][] puzzle) {
        int[] flattened = new int[puzzle.length * puzzle.length];
        int k = 0;
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                flattened[k++] = puzzle[i][j];
            }
        }
        return flattened;
    }

    // Method to draw the puzzle on the canvas
    public static void drawPuzzle(int[][] puzzle) {
        StdDraw.clear(backgroundColor);

        int n = puzzle.length;
        double tileSize = (double) 600 / n;
        double halfTileSize = tileSize / 2;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int tile = puzzle[i][j];
                if (tile != 0) {
                    double centerX = j * tileSize + halfTileSize;
                    double centerY = 600 - i * tileSize - halfTileSize;

                    StdDraw.setPenColor(boxColor);
                    StdDraw.setPenRadius(lineThickness);
                    StdDraw.square(centerX, centerY, halfTileSize);

                    StdDraw.setPenColor(backgroundColor);
                    StdDraw.filledSquare(centerX, centerY, halfTileSize);

                    StdDraw.setPenColor(Color.BLACK);
                    StdDraw.text(centerX, centerY, Integer.toString(tile));
                }
            }
        }

        StdDraw.setPenColor(boxColor);
        StdDraw.setPenRadius(lineThickness);
        StdDraw.square(2, 2, 1.5);
        StdDraw.setPenRadius();
        StdDraw.show();
    }

    // Main method to start the puzzle solver
    public static void main(String[] args) {
        int[][] startState = generateRandomStartState();
        int canvasSize = 600;
        StdDraw.setCanvasSize(canvasSize, canvasSize);
        StdDraw.setXscale(0, canvasSize);
        StdDraw.setYscale(0, canvasSize);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 24));

        String solution = solvePuzzle(startState);
        System.out.println("Start State:");
        printPuzzle(startState);
        System.out.println("Solution Moves: " + solution);

        int delay = 500;
        for (int i = 0; i < solution.length(); i++) {
            char move = solution.charAt(i);
            performMove(startState, move);
            drawPuzzle(startState);
            StdDraw.pause(delay);
        }
    }

    // Method to perform a move in the puzzle
    public static void performMove(int[][] puzzle, char move) {
        int zeroRow = -1, zeroCol = -1;
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                if (puzzle[i][j] == 0) {
                    zeroRow = i;
                    zeroCol = j;
                    break;
                }
            }
        }
        int newRow = zeroRow;
        int newCol = zeroCol;
        switch (move) {
            case 'R':
                newCol++;
                break;
            case 'L':
                newCol--;
                break;
            case 'U':
                newRow--;
                break;
            case 'D':
                newRow++;
                break;
        }
        puzzle[zeroRow][zeroCol] = puzzle[newRow][newCol];
        puzzle[newRow][newCol] = 0;
    }

    // Method to print the puzzle configuration
    public static void printPuzzle(int[][] puzzle) {
        for (int[] row : puzzle) {
            System.out.println(Arrays.toString(row));
        }
    }

    // Getter for the goal states
    public static int[][] getGoalStates() {
        return goalStates;
    }

    // Setter for the goal states
    public static void setGoalStates(int[][] goalStates) {
        CustomPuzzleSolver.goalStates = goalStates;
    }
}





