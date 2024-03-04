package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyTunesITry implements IBot {
    private static final String BOTNAME = "MyTunesITry";
    private Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> winningMoves = findWinningMoves(state);

        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }

        List<IMove> availableMoves = state.getField().getAvailableMoves();
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }

        return null;
    }

    private List<IMove> findWinningMoves(IGameState state) {
        String player = getPlayer(state);
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        List<IMove> winningMoves = new ArrayList<>();

        for (IMove move : availableMoves) {
            if (isWinningMove(state, move, player)) {
                winningMoves.add(move);
            }
        }

        return winningMoves;
    }

    private String getPlayer(IGameState state) {
        return state.getMoveNumber() % 2 == 0 ? "0" : "1";
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

    private String[][] cloneBoard(String[][] board) {
        String[][] clonedBoard = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            clonedBoard[i] = board[i].clone();
        }
        return clonedBoard;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
