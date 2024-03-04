package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ShadowBot implements IBot {
    private static final String BOTNAME = "ShadowBot";

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> winMoves = getWinningMoves(state);
        if (!winMoves.isEmpty())
            return winMoves.get(0);

        // If there are no winning moves, make a random move
        List<IMove> avail = state.getField().getAvailableMoves();
        Random random = new Random();
        return avail.get(random.nextInt(avail.size()));
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
