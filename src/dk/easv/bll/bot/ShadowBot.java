package dk.easv.bll.bot;

import dk.easv.bll.bot.IBot;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ShadowBot implements IBot {
    private static final String BOTNAME = "ShadowBot";

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> winMoves = getWinningMoves(state);
        if (!winMoves.isEmpty())
            return winMoves.get(0);

        // If there are no winning moves, try to choose a corner
        List<IMove> corners = getCorners(state);
        if (!corners.isEmpty()) {
            Collections.shuffle(corners); // Shuffle the list of corners
            return corners.get(0); // Return the first (randomized) corner
        }

        // If no corners are available, make a random move
        List<IMove> avail = state.getField().getAvailableMoves();
        return avail.get(0); // Fallback to the first available move
    }

    // Get available corner moves
    private List<IMove> getCorners(IGameState state) {
        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> corners = new ArrayList<>();
        for (IMove move : avail) {
            if (isCorner(move))
                corners.add(move);
        }

        // Shuffle the list of corners
        Collections.shuffle(corners);

        return corners;
    }


    // Check if a move is a corner move
    private boolean isCorner(IMove move) {
        int x = move.getX();
        int y = move.getY();
        return (x == 0 || x == 2) && (y == 0 || y == 2);
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
        if (state.getMoveNumber() % 2 == 0)
            player = "0";

        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> winningMoves = new ArrayList<>();
        for (IMove move : avail) {
            if (isWinningMove(state, move, player))
                winningMoves.add(move);
        }
        return winningMoves;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
