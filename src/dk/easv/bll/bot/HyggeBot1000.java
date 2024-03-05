package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HyggeBot1000 implements IBot {
    private final String BOT_NAME = getClass().getSimpleName();
    private final Random random = new Random();

    public IMove doMove(IGameState state) {
        List<IMove> winningMoves = findWinningMoves(state, getHyggeBot(state));
        List<IMove> opponentWinningMoves = findWinningMoves(state, getOpponentBot(state));

        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }

        if (!opponentWinningMoves.isEmpty()) {
            return opponentWinningMoves.getFirst();
        }

        List<IMove> closeCorners = getCloseCorners(state);
        if (!closeCorners.isEmpty()) {
            Collections.shuffle(closeCorners); // Shuffle the list of close corners
            return closeCorners.get(0); // Return the first (randomized) close corner
        }

        // If no close corners are available, try to choose any corner
        List<IMove> corners = getCorners(state);
        if (!corners.isEmpty()) {
            Collections.shuffle(corners); // Shuffle the list of corners
            return corners.get(0); // Return the first (randomized) corner
        }

        List<IMove> availableMoves = state.getField().getAvailableMoves(); // Use simulation instead of randomness to make the rest of the moves and peraps replace te above corner method as well
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }

        return null;
    }

    private List<IMove> findWinningMoves(IGameState state, String player) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        List<IMove> winningMoves = new ArrayList<>();

        for (IMove move : availableMoves) {
            if (isWinningMove(state, move, player)) {
                winningMoves.add(move);
            }
        }
        return winningMoves;
    }

    private boolean isWinningMove(IGameState state, IMove move, String player) {
        String[][] clonedBoard = cloneBoard(state.getField().getBoard());
        int x = move.getX();
        int y = move.getY();
        clonedBoard[x][y] = player;

        int startX = x - (x % 3);
        int startY = y - (y % 3);

        return (clonedBoard[startX][y].equals(player) && clonedBoard[startX][y].equals(clonedBoard[startX + 1][y]) &&
                clonedBoard[startX + 1][y].equals(clonedBoard[startX + 2][y])) || // Check column
                (clonedBoard[x][startY].equals(player) && clonedBoard[x][startY].equals(clonedBoard[x][startY + 1]) &&
                        clonedBoard[x][startY + 1].equals(clonedBoard[x][startY + 2])) || // Check row
                (clonedBoard[startX][startY].equals(player) && clonedBoard[startX][startY].equals(clonedBoard[startX + 1][startY + 1]) &&
                        clonedBoard[startX + 1][startY + 1].equals(clonedBoard[startX + 2][startY + 2])) || // Check diagonal
                (clonedBoard[startX][startY + 2].equals(player) && clonedBoard[startX][startY + 2].equals(clonedBoard[startX + 1][startY + 1]) &&
                        clonedBoard[startX + 1][startY + 1].equals(clonedBoard[startX + 2][startY])); // Check anti-diagonal
    }

    private List<IMove> getCorners(IGameState state) {
        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> corners = new ArrayList<>();
        for (IMove move : avail) {
            if (isCorner(move)) {
                corners.add(move);
            }
        }

        return corners;
    }

    // Check if a move is a corner move
    private boolean isCorner(IMove move) {
        int x = move.getX();
        int y = move.getY();
        return (x == 0 || x == 2) && (y == 0 || y == 2);
    }

    // Get available corner moves that are close to each other
    private List<IMove> getCloseCorners(IGameState state) {
        List<IMove> avail = getCorners(state);
        List<IMove> closeCorners = new ArrayList<>();

        // Calculate Manhattan distance between each pair of corners
        for (int i = 0; i < avail.size(); i++) {
            IMove move1 = avail.get(i);
            for (int j = i + 1; j < avail.size(); j++) {
                IMove move2 = avail.get(j);
                if (manhattanDistance(move1, move2) <= 2) { // Define the threshold for closeness
                    closeCorners.add(move1);
                    closeCorners.add(move2);
                }
            }
        }

        return closeCorners;
    }

    // Calculate Manhattan distance between two moves
    private int manhattanDistance(IMove move1, IMove move2) {
        return Math.abs(move1.getX() - move2.getX()) + Math.abs(move1.getY() - move2.getY());
    }
    private String[][] cloneBoard(String[][] board) {
        String[][] clonedBoard = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            clonedBoard[i] = board[i].clone();
        }
        return clonedBoard;
    }
    private String getHyggeBot(IGameState state) {return state.getMoveNumber() % 2 == 0 ? "0" : "1";}

    private String getOpponentBot(IGameState state) {return getHyggeBot(state).equals("0") ? "1" : "0";}

    public String getBotName() {return BOT_NAME;}
}
