package dk.easv.bll.bot;

import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;

public class HyggeBot1000MiniMaxBetaPruning implements IBot {
        private final String BOT_NAME = getClass().getSimpleName();


        public IMove doMove(IGameState state) {
            String player = getHyggeBot(state);
            List<IMove> availableMoves = state.getField().getAvailableMoves();
            IMove bestMove = null;
            int bestScore = Integer.MIN_VALUE;
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;

            for (IMove move : availableMoves) {
                String[][] clonedBoard = cloneBoard(state.getField().getBoard());
                int x = move.getX();
                int y = move.getY();
                clonedBoard[x][y] = player;
                int score = minimax(clonedBoard, state, 0, false, alpha, beta);
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) {
                    break;
                }
            }
            return bestMove;
        }

        private int minimax(String[][] board, IGameState state, int depth, boolean isMaximizingPlayer, int alpha, int beta) {
            String player = isMaximizingPlayer ? getHyggeBot(state) : getOpponentBot(state);

            if (isWinningMove(state, board, player)) {
                return isMaximizingPlayer ? 1 : -1;
            } else if (state.getField().isFull()) {
                return 0;
            }
            int bestScore = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j].isEmpty()) {
                        board[i][j] = player;
                        int score = minimax(board, state, depth + 1, !isMaximizingPlayer, alpha, beta);
                        board[i][j] = "";
                        if (isMaximizingPlayer) {
                            bestScore = Math.max(bestScore, score);
                            alpha = Math.max(alpha, bestScore);
                        } else {
                            bestScore = Math.min(bestScore, score);
                            beta = Math.min(beta, bestScore);
                        }
                        if (beta <= alpha) {
                            break;
                        }
                    }
                }
            }
            return bestScore;
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
        private String getHyggeBot(IGameState state) {
            return state.getMoveNumber() % 2 == 0 ? "0" : "1";
        }

        private String getOpponentBot(IGameState state) {
            return getHyggeBot(state).equals("0") ? "1" : "0";
        }

        private String[][] cloneBoard(String[][] board) {
            String[][] clonedBoard = new String[board.length][board[0].length];
            for (int i = 0; i < board.length; i++) {
                clonedBoard[i] = board[i].clone();
            }
            return clonedBoard;
        }

        public String getBotName() {
            return BOT_NAME;
        }
    }