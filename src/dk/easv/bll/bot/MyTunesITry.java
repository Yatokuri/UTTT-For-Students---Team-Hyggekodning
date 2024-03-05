package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyTunesITry implements IBot {
    private static final String BOT_NAME = "MyTunesITry";
    private Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        List<IMove> winningMoves = findWinningMoves(state, getHyggeBot(state));
        List<IMove> opponentWinningMoves = findWinningMoves(state, getOpponentBot(state));

        // First, try to win the game if possible
        if (!winningMoves.isEmpty()) {
            return winningMoves.get(random.nextInt(winningMoves.size()));
        }

        // Then, block the opponent's winning moves
        if (!opponentWinningMoves.isEmpty()) {
            return opponentWinningMoves.get(0); // Choose the first blocking move
        }

        // Next, prioritize winning a microboard
        List<IMove> winningMicroboardMoves = findWinningMicroboardMoves(state, getHyggeBot(state));
        if (!winningMicroboardMoves.isEmpty()) {
            return winningMicroboardMoves.get(random.nextInt(winningMicroboardMoves.size()));
        }

        // If none of the above, choose a random available move that does not lead to a won/tied microboard
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        List<IMove> safeMoves = filterSafeMoves(state, availableMoves);
        if (!safeMoves.isEmpty()) {
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        // If no safe moves, choose any available move
        if (!availableMoves.isEmpty()) {
            return availableMoves.get(random.nextInt(availableMoves.size()));
        }

        return null; // No moves available
    }



    private boolean willLeadToWin(IGameState state, IMove move, String player) {
        String opponent = getOpponentBot(state);

        // Simulate the move
        String[][] clonedBoard = cloneBoard(state.getField().getBoard());
        int x = move.getX();
        int y = move.getY();
        clonedBoard[x][y] = player;

        // Check if the opponent can win in the next move after this simulated move
        return isWinningMove(state, move, opponent);
    }


    private List<IMove> filterSafeMoves(IGameState state, List<IMove> moves) {
        List<IMove> safeMoves = new ArrayList<>();
        String player = getHyggeBot(state);
        String opponent = getOpponentBot(state);

        // Check each available move
        for (IMove move : moves) {
            // Prepare the board for checking if the opponent can win in the next move after this move
            if (!willLeadToWin(state, move, player)) {
                safeMoves.add(move); // If opponent cannot win, consider this move safe
            }
        }

        return safeMoves;
    }



    private List<IMove> findWinningMicroboardMoves(IGameState state, String player) {
        // Implement your findWinningMicroboardMoves method here
        return new ArrayList<>(); // Placeholder, replace with actual implementation
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
