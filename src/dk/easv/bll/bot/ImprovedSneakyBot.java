package dk.easv.bll.bot;

import dk.easv.bll.field.IField;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ImprovedSneakyBot implements IBot { // Improve it so it can play as player 2, currently it cannot figure it out

    final int moveTimeMs = 1000;
    private final String BOT_NAME = getClass().getSimpleName();

    private GameSimulator createSimulator(IGameState state) {
        GameSimulator simulator = new GameSimulator(new GameState());
        simulator.setGameOver(GameOverState.Active);
        simulator.setCurrentPlayer(state.getMoveNumber() % 2);
        simulator.getCurrentState().setRoundNumber(state.getRoundNumber());
        simulator.getCurrentState().setMoveNumber(state.getMoveNumber());
        simulator.getCurrentState().getField().setBoard(state.getField().getBoard());
        simulator.getCurrentState().getField().setMacroboard(state.getField().getMacroboard());
        return simulator;
    }

    @Override
    public IMove doMove(IGameState state) {
        return calculateWinningMove(state, moveTimeMs);
    }
    // Plays single games until it wins and returns the first move for that. If iterations reached with no clear win, just return random valid move
    private IMove calculateWinningMove(IGameState state, int maxTimeMs) {
        long time = System.currentTimeMillis();
        Random rand = new Random();
        while (System.currentTimeMillis() < time + maxTimeMs) { // check how much time has passed, stop if over maxTimeMs
            GameSimulator simulator = createSimulator(state);
            IGameState gs = simulator.getCurrentState();
            List<IMove> moves = gs.getField().getAvailableMoves();

            // Perform minimax with alpha-beta pruning
            IMove bestMove = null;
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;
            for (IMove move : moves) {
                GameSimulator childSimulator = createSimulator(state);
                childSimulator.updateGame(move);
                int score = minimax(childSimulator, 0, false, alpha, beta);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                }
            }
            if (bestMove != null) {
                return bestMove;
            }
        }
        // Just return random valid move if no clear winning move is found
        List<IMove> moves = state.getField().getAvailableMoves();
        return moves.get(rand.nextInt(moves.size()));
    }

    private int minimax(GameSimulator simulator, int depth, boolean maximizingPlayer, int alpha, int beta) {
        if (depth == MAX_DEPTH || simulator.getGameOver() != GameOverState.Active) {
            // Return evaluation score
            return evaluate(simulator);
        }

        List<IMove> moves = simulator.getCurrentState().getField().getAvailableMoves();
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (IMove move : moves) {
                GameSimulator childSimulator = createSimulator(simulator.getCurrentState());
                childSimulator.updateGame(move);
                int eval = minimax(childSimulator, depth + 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (IMove move : moves) {
                GameSimulator childSimulator = createSimulator(simulator.getCurrentState());
                childSimulator.updateGame(move);
                int eval = minimax(childSimulator, depth + 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private int evaluate(GameSimulator simulator) {
        int score = 0;
        String[][] board = simulator.getCurrentState().getField().getBoard();
        String[][] macroBoard = simulator.getCurrentState().getField().getMacroboard();
        String hyggeBot = getHyggeBot(simulator.getCurrentState());
        String opponentBot = getOpponentBot(simulator.getCurrentState());

        // Evaluate microBoards
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) {
                score += evaluateMicroBoard(board, i, j, simulator);
            }
        }

        // Evaluate macroBoard
        score += evaluateMacroBoard(macroBoard, hyggeBot, opponentBot, simulator);

        return score;
    }

    private int evaluateMacroBoard(String[][] macroBoard, String hyggeBot, String opponentBot, GameSimulator simulator) {
        int score = 0;
        GameOverState gameOverState = simulator.getGameOver();
        String lastWinner = null; // Figure out how to find the individual board winner and insert here

        // Check if HyggeBot has won the macroBoard
        if (gameOverState == GameOverState.Win && lastWinner.equals(hyggeBot)) {
            score += 100; // Winning the macroBoard is highly rewarded
        }

        // Check if Opponent has won the macroBoard
        if (gameOverState == GameOverState.Win && lastWinner.equals(opponentBot)) {
            score -= 100; // Losing the macroBoard is heavily penalized
        }

        return score;
    }

    private int evaluateMicroBoard(String[][] board, int startX, int startY, GameSimulator simulator) {
        int score = 0;
        String hyggeBot = getHyggeBot(simulator.getCurrentState());
        String opponentBot = getOpponentBot(simulator.getCurrentState());

        // Evaluate rows, columns, and diagonals
        for (int i = startX; i < startX + 3; i++) {
            for (int j = startY; j < startY + 3; j++) {
                if (board[i][j].equals(hyggeBot)) {
                    score += 10; // HyggeBot's pieces contribute positively
                } else if (board[i][j].equals(opponentBot)) {
                    score -= 10; // Opponent's pieces contribute negatively
                }
            }
        }
        return score;
    }

    private String getHyggeBot(IGameState state) {return state.getMoveNumber() % 2 == 0 ? "0" : "1";}

    private String getOpponentBot(IGameState state) {return getHyggeBot(state).equals("0") ? "1" : "0";}
    private static final int MAX_DEPTH = 5; // Adjust this depth according to your requirements

    /*
        The code below is a simulator for simulation of gameplay. This is needed for AI.

        It is put here to make the Bot independent of the GameManager and its subclasses/enums

        Now this class is only dependent on a few interfaces: IMove, IField, and IGameState

        You could say it is self-contained. The drawback is that if the game rules change, the simulator must be
        changed accordingly, making the code redundant.

     */


    @Override
    public String getBotName() {
        return BOT_NAME;
    }

    public enum GameOverState {
        Active,
        Win,
        Tie
    }

    public class Move implements IMove {
        int x = 0;
        int y = 0;

        public Move(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return x == move.x && y == move.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    class GameSimulator {
        private final IGameState currentState;
        private int currentPlayer = 0; //player0 == 0 && player1 == 1
        private volatile GameOverState gameOver = GameOverState.Active;

        public void setGameOver(GameOverState state) {
            gameOver = state;
        }

        public GameOverState getGameOver() {
            return gameOver;
        }

        public void setCurrentPlayer(int player) {
            currentPlayer = player;
        }

        public IGameState getCurrentState() {
            return currentState;
        }

        public GameSimulator(IGameState currentState) {
            this.currentState = currentState;
        }

        public Boolean updateGame(IMove move) {
            if (!verifyMoveLegality(move))
                return false;

            updateBoard(move);
            currentPlayer = (currentPlayer + 1) % 2;

            return true;
        }

        private Boolean verifyMoveLegality(IMove move) {
            IField field = currentState.getField();
            boolean isValid = field.isInActiveMicroboard(move.getX(), move.getY());

            if (isValid && (move.getX() < 0 || 9 <= move.getX())) isValid = false;
            if (isValid && (move.getY() < 0 || 9 <= move.getY())) isValid = false;

            if (isValid && !field.getBoard()[move.getX()][move.getY()].equals(IField.EMPTY_FIELD))
                isValid = false;

            return isValid;
        }

        private void updateBoard(IMove move) {
            String[][] board = currentState.getField().getBoard();
            board[move.getX()][move.getY()] = currentPlayer + "";
            currentState.setMoveNumber(currentState.getMoveNumber() + 1);
            if (currentState.getMoveNumber() % 2 == 0) {
                currentState.setRoundNumber(currentState.getRoundNumber() + 1);
            }
            checkAndUpdateIfWin(move);
            updateMacroboard(move);

        }

        private void checkAndUpdateIfWin(IMove move) {
            String[][] macroBoard = currentState.getField().getMacroboard();
            int macroX = move.getX() / 3;
            int macroY = move.getY() / 3;

            if (macroBoard[macroX][macroY].equals(IField.EMPTY_FIELD) ||
                    macroBoard[macroX][macroY].equals(IField.AVAILABLE_FIELD)) {

                String[][] board = getCurrentState().getField().getBoard();

                if (isWin(board, move, "" + currentPlayer))
                    macroBoard[macroX][macroY] = currentPlayer + "";
                else if (isTie(board, move))
                    macroBoard[macroX][macroY] = "TIE";

                //Check macro win
                if (isWin(macroBoard, new Move(macroX, macroY), "" + currentPlayer))
                    gameOver = GameOverState.Win;
                else if (isTie(macroBoard, new Move(macroX, macroY)))
                    gameOver = GameOverState.Tie;
            }

        }

        private boolean isTie(String[][] board, IMove move) {
            int localX = move.getX() % 3;
            int localY = move.getY() % 3;
            int startX = move.getX() - (localX);
            int startY = move.getY() - (localY);

            for (int i = startX; i < startX + 3; i++) {
                for (int k = startY; k < startY + 3; k++) {
                    if (board[i][k].equals(IField.AVAILABLE_FIELD) ||
                            board[i][k].equals(IField.EMPTY_FIELD))
                        return false;
                }
            }
            return true;
        }


        public boolean isWin(String[][] board, IMove move, String currentPlayer) {
            int localX = move.getX() % 3;
            int localY = move.getY() % 3;
            int startX = move.getX() - (localX);
            int startY = move.getY() - (localY);

            //check col
            for (int i = startY; i < startY + 3; i++) {
                if (!board[move.getX()][i].equals(currentPlayer))
                    break;
                if (i == startY + 3 - 1) return true;
            }

            //check row
            for (int i = startX; i < startX + 3; i++) {
                if (!board[i][move.getY()].equals(currentPlayer))
                    break;
                if (i == startX + 3 - 1) return true;
            }

            //check diagonal
            if (localX == localY) {
                //we're on a diagonal
                int y = startY;
                for (int i = startX; i < startX + 3; i++) {
                    if (!board[i][y++].equals(currentPlayer))
                        break;
                    if (i == startX + 3 - 1) return true;
                }
            }

            //check anti diagonal
            if (localX + localY == 3 - 1) {
                int less = 0;
                for (int i = startX; i < startX + 3; i++) {
                    if (!board[i][(startY + 2) - less++].equals(currentPlayer))
                        break;
                    if (i == startX + 3 - 1) return true;
                }
            }
            return false;
        }

        private void updateMacroboard(IMove move) {
            String[][] macroBoard = currentState.getField().getMacroboard();
            for (int i = 0; i < macroBoard.length; i++)
                for (int k = 0; k < macroBoard[i].length; k++) {
                    if (macroBoard[i][k].equals(IField.AVAILABLE_FIELD))
                        macroBoard[i][k] = IField.EMPTY_FIELD;
                }

            int xTrans = move.getX() % 3;
            int yTrans = move.getY() % 3;

            if (macroBoard[xTrans][yTrans].equals(IField.EMPTY_FIELD))
                macroBoard[xTrans][yTrans] = IField.AVAILABLE_FIELD;
            else {
                // Field is already won, set all fields not won to avail.
                for (int i = 0; i < macroBoard.length; i++)
                    for (int k = 0; k < macroBoard[i].length; k++) {
                        if (macroBoard[i][k].equals(IField.EMPTY_FIELD))
                            macroBoard[i][k] = IField.AVAILABLE_FIELD;
                    }
            }
        }
    }
}
