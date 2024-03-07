package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ShadowBot implements IBot {
    private static final String BOTNAME = "ShadowBot";
    private final Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> winMoves = getWinningMoves(state);
        if (!winMoves.isEmpty()) {
            return winMoves.get(0);
        }

        // Block opponent's winning moves
        List<IMove> blockMoves = getBlockingMoves(state);
        if (!blockMoves.isEmpty()) {
            return blockMoves.get(0);
        }

        // If there are no winning moves or blocking moves, try to choose corners close to each other
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

        // If no corners are available, make a random move
        List<IMove> avail = state.getField().getAvailableMoves();
        return avail.get(random.nextInt(avail.size())); // Return a random move
    }

    // Get available corner moves
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
                if (isOuterCorner(move1) && isOuterCorner(move2) && manhattanDistance(move1, move2) <= 2) { // Check if both moves are outer corners
                    closeCorners.add(move1);
                    closeCorners.add(move2);
                }
            }
        }

        return closeCorners;
    }

    // Check if a move is an outer corner move
    private boolean isOuterCorner(IMove move) {
        int x = move.getX();
        int y = move.getY();
        return (x == 0 && y == 0) || // Top-left corner
                (x == 0 && y == 2) || // Top-right corner
                (x == 2 && y == 0) || // Bottom-left corner
                (x == 2 && y == 2);   // Bottom-right corner
    }

    // Calculate Manhattan distance between two moves
    private int manhattanDistance(IMove move1, IMove move2) {
        return Math.abs(move1.getX() - move2.getX()) + Math.abs(move1.getY() - move2.getY());
    }

    // Check if a move is winning
    private boolean isWinningMove(IGameState state, IMove move, String player) {
        String[][] board = Arrays.stream(state.getField().getBoard()).map(String[]::clone).toArray(String[][]::new);

        board[move.getX()][move.getY()] = player;

        int startX = move.getX() - (move.getX() % 3);
        int startY = move.getY() - (move.getY() % 3);

        // Check rows, columns, and diagonals for a win
        return (board[startX][move.getY()].equals(player) && board[startX + 1][move.getY()].equals(player) && board[startX + 2][move.getY()].equals(player)) ||
                (board[move.getX()][startY].equals(player) && board[move.getX()][startY + 1].equals(player) && board[move.getX()][startY + 2].equals(player)) ||
                (board[startX][startY].equals(player) && board[startX + 1][startY + 1].equals(player) && board[startX + 2][startY + 2].equals(player)) ||
                (board[startX][startY + 2].equals(player) && board[startX + 1][startY + 1].equals(player) && board[startX + 2][startY].equals(player));
    }

    // Compile a list of all available winning moves
    private List<IMove> getWinningMoves(IGameState state) {
        String player = "1";
        if (state.getMoveNumber() % 2 == 0) {
            player = "0";
        }

        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> winningMoves = new ArrayList<>();
        for (IMove move : avail) {
            if (isWinningMove(state, move, player)) {
                winningMoves.add(move);
            }
        }
        return winningMoves;
    }

    // Check if a move is opponent's winning
    private boolean isOpponentWinningMove(IGameState state, IMove move, String opponent) {
        String player = "1";
        if (opponent.equals("1")) {
            player = "0";
        }
        return isWinningMove(state, move, player);
    }

    // Get available moves to block opponent's winning moves
    private List<IMove> getBlockingMoves(IGameState state) {
        String opponent = "1";
        if (state.getMoveNumber() % 2 == 0) {
            opponent = "0";
        }

        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> blockingMoves = new ArrayList<>();
        for (IMove move : avail) {
            if (isOpponentWinningMove(state, move, opponent)) {
                blockingMoves.add(move);
            }
        }
        return blockingMoves;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}

