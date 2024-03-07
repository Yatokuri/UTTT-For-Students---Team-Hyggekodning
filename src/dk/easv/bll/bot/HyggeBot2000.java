package dk.easv.bll.bot;

import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HyggeBot2000 implements IBot {
    private final String BOT_NAME = getClass().getSimpleName();
    private final Random random = new Random();


    private IGameState simulateMove(IGameState state, IMove move, String player) {
        IGameState newState = new GameState(state); // Create a copy of the current game state
        String[][] board = newState.getField().getBoard(); // Get the game board

        // Apply the move to the board
        int x = move.getX();
        int y = move.getY();
        board[x][y] = player;

        // Update move and round numbers
        newState.setMoveNumber(newState.getMoveNumber() + 1);
        newState.setRoundNumber(newState.getMoveNumber() / 2);

        return newState;
    }


    public IMove doMove(IGameState state) {
        String hyggeBot = getHyggeBot(state);
        String opponentBot = getOpponentBot(state);

        // Check if the opponent can win in the next round
        List<IMove> opponentWinningMoves = findWinningMoves(state, opponentBot);
        if (!opponentWinningMoves.isEmpty()) {
            return opponentWinningMoves.get(0); // Return the move that prevents the opponent from winning
        }

        // Check if the bot can win in this round
        List<IMove> winningMoves = findWinningMoves(state, hyggeBot);
        if (!winningMoves.isEmpty()) {
            for (IMove move : winningMoves) {
                // Simulate opponent's move after bot's winning move
                IGameState nextState = simulateMove(state, move, hyggeBot); // Pass hyggeBot as the player
                List<IMove> opponentNextWinningMoves = findWinningMoves(nextState, opponentBot);
                if (!opponentNextWinningMoves.isEmpty()) {
                    // If opponent can win after bot's winning move, skip this move
                    continue;
                }
                // Otherwise, return the winning move
                return move;
            }
        }

        // Check if any available moves could potentially lead the opponent to win the game
        List<IMove> safeMoves = new ArrayList<>();
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        for (IMove move : availableMoves) {
            IGameState nextState = simulateMove(state, move, hyggeBot); // Pass hyggeBot as the player
            List<IMove> opponentNextWinningMoves = findWinningMoves(nextState, opponentBot);
            if (opponentNextWinningMoves.isEmpty()) {
                // If opponent cannot win after this move, add it to safe moves
                safeMoves.add(move);
            }
        }

        if (!safeMoves.isEmpty()) {
            // If there are safe moves, randomly choose one of them
            return safeMoves.get(random.nextInt(safeMoves.size()));
        }

        // If no safe moves are available, return null
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
