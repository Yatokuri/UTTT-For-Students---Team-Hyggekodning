package dk.easv.bll.bot;

import dk.easv.bll.field.Field;
import dk.easv.bll.field.IField;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HyggeBot implements IBot {

    final int maxTimeMs = 980;
    private Random random = new Random();
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

    long time;

    @Override
    public IMove doMove(IGameState state) {
        String hyggeBot = getHyggeBot(state);
        String opponentBot = getOpponentBot(state);
        List<IMove> winningMoves = findWinningMoves(state, hyggeBot);
        // Check if there are winning moves available
        if (!winningMoves.isEmpty()) {return winningMoves.get(random.nextInt(winningMoves.size()));}
        // If there are no winning moves, calculate the next move using your normal logic
        return calculateWinningMove(state, hyggeBot, opponentBot);
    }

    // Plays single games until it wins and returns the first move for that. If iterations reached with no clear win, just return random valid move
    private IMove calculateWinningMove(IGameState state, String hyggeBot, String opponentBot) {
        time = System.currentTimeMillis();
        Random rand = new Random();
        IMove bestMove = null;
            // Place move in the center if it's empty
        if (state.getField().getBoard()[4][4].equals(IField.EMPTY_FIELD) && state.getField().isEmpty()) { return new Move(4, 4); }
        while (System.currentTimeMillis() < time + maxTimeMs) { // check how much time has passed, stop if over maxTimeMs
            GameSimulator simulator = createSimulator(state);
            IGameState gs = simulator.getCurrentState();
            List<IMove> moves = gs.getField().getAvailableMoves();
            // Perform minimax with alpha-beta pruning
            int alpha = Integer.MIN_VALUE;
            int beta = Integer.MAX_VALUE;
            for (IMove move : moves) {
                if (System.currentTimeMillis() > time + maxTimeMs) {
                    if (bestMove != null) { return bestMove; }
                    return moves.get(rand.nextInt(moves.size()));
                }
                GameSimulator childSimulator = createSimulator(state);
                childSimulator.updateGame(move);
                int score = minimax(childSimulator, 0, false, alpha, beta, hyggeBot, opponentBot, state.getMoveNumber() % 2);
                if (score > alpha) {
                    alpha = score;
                    bestMove = move;
                }
            }
            if (bestMove != null) { return bestMove; }
        }
        // Just return random valid move if no clear winning move is found
        List<IMove> moves = state.getField().getAvailableMoves();
        return moves.get(rand.nextInt(moves.size()));
    }

    private int minimax(GameSimulator simulator, int depth, boolean maximizingPlayer, int alpha, int beta, String hyggeBot, String opponentBot, int currentPlayer) {
        if (depth == MAX_DEPTH || simulator.getGameOver() != GameOverState.Active) { return evaluate(simulator, hyggeBot, opponentBot); }
        List<IMove> moves = simulator.getCurrentState().getField().getAvailableMoves();
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (IMove move : moves) {
                if (System.currentTimeMillis() > time + maxTimeMs) { return maxEval; }
                GameSimulator childSimulator = createSimulator(simulator.getCurrentState());
                childSimulator.updateGame(move);
                int eval = minimax(childSimulator, depth + 1, false, alpha, beta, hyggeBot, opponentBot, currentPlayer);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) { break; }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (IMove move : moves) {
                if (System.currentTimeMillis() > time + maxTimeMs) { return minEval; }
                GameSimulator childSimulator = createSimulator(simulator.getCurrentState());
                childSimulator.updateGame(move);
                int eval = minimax(childSimulator, depth + 1, true, alpha, beta, hyggeBot, opponentBot, currentPlayer);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) { break; }
            }
            return minEval;
        }
    }

    private int evaluate(GameSimulator simulator, String hyggeBot, String opponentBot) {
        int score = 0;
        IGameState currentState = simulator.getCurrentState();
        String[][] board = currentState.getField().getBoard();
        String[][] macroboard = currentState.getField().getMacroboard();
        // Evaluate each microBoard
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) {
                score += evaluateMicroBoard(board, macroboard, i, j, hyggeBot, opponentBot);
            }
        }

        // Consider the status of the macroBoard
        score += evaluateMacroboard(macroboard, hyggeBot, opponentBot);

        // Prioritize preventing the opponent from winning the game
        score += evaluatePreventiveMoves(board, macroboard, hyggeBot, opponentBot);
        // Prioritize checking if HyggeBot can win on the next move
        if (isHyggeBotWinningNextMove(macroboard, hyggeBot)) {score += 200; } // Give a high bonus if HyggeBot can win on the next move
        // Prioritize checking if the opponent can win on the next move
        if (isOpponentWinningNextMove(macroboard, opponentBot)) { score -= 200;} // Give a high penalty if the opponent can win on the next move
        // Prioritize checking if the opponent can win the macroBoard on the next move
        if (isOpponentWinningMacroboardNextMove(macroboard, opponentBot)) { score -= 10000; } // Give a high penalty if the opponent can win the macroboard on the next move
        return score;
    }

    private static int evaluateMicroBoard(String[][] board, String[][] macroboard, int startX, int startY, String hyggeBot, String opponentBot) {
        int score = 0;
        // Check rows, columns, and diagonals
        for (int i = 0; i < 3; i++) {
            // Check rows
            if (board[startX + i][startY].equals(board[startX + i][startY + 1]) && board[startX + i][startY + 1].equals(board[startX + i][startY + 2])) {
                score += scoreForPlayer(board[startX + i][startY], hyggeBot, opponentBot);
            }
            // Check columns
            if (board[startX][startY + i].equals(board[startX + 1][startY + i]) && board[startX + 1][startY + i].equals(board[startX + 2][startY + i])) {
                score += scoreForPlayer(board[startX][startY + i], hyggeBot, opponentBot);
            }
        }
        // Check diagonals
        if (board[startX][startY].equals(board[startX + 1][startY + 1]) && board[startX + 1][startY + 1].equals(board[startX + 2][startY + 2])) {
            score += scoreForPlayer(board[startX][startY], hyggeBot, opponentBot);
        }
        if (board[startX + 2][startY].equals(board[startX + 1][startY + 1]) && board[startX + 1][startY + 1].equals(board[startX][startY + 2])) {
            score += scoreForPlayer(board[startX + 2][startY], hyggeBot, opponentBot);
        }

        // Prevent giving away free wins to opponent
        if (macroboard[startX / 3][startY / 3].equals(IField.AVAILABLE_FIELD)) {
            score -= 20; // Decrease score for microboards in available macroboards
        }
        return score;
    }

    private static int scoreForPlayer(String player, String hyggeBot, String opponentBot) {
        if (player.equals(hyggeBot)) { return 10; } // HyggeBot controls the microboard
        else if (player.equals(opponentBot)) { return -10; } // Opponent controls the microboard
        return 0; // The microboard is empty or tied
    }

    private static int evaluateMacroboard(String[][] macroboard, String hyggeBot, String opponentBot) {
        int score = 0;
        for (String[] row : macroboard) {
            for (String cell : row) {
                if (cell.equals(hyggeBot)) { score += 100; } // HyggeBot has control over the macroboard
                else if (cell.equals(opponentBot)) { score -= 100; } // Opponent has control over the macroboard
            }
        }
        return score;
    }

    private static int evaluatePreventiveMoves(String[][] board, String[][] macroboard, String hyggeBot, String opponentBot) {
        int preventiveScore = 0;
        // Consider blocking opponent's winning moves in microboards
        for (int i = 0; i < 9; i += 3) {
            for (int j = 0; j < 9; j += 3) {
                // Check rows
                if (board[i][j].equals(opponentBot) && board[i][j + 1].equals(opponentBot) && board[i][j + 2].equals(IField.EMPTY_FIELD)) {
                    preventiveScore += 100; // Give a high score for blocking opponent's winning move in the row
                }
                // Check columns
                if (board[i][j].equals(opponentBot) && board[i + 1][j].equals(opponentBot) && board[i + 2][j].equals(IField.EMPTY_FIELD)) {
                    preventiveScore += 100; // Give a high score for blocking opponent's winning move in the column
                }
                // Check diagonal
                if ((i == 0 && j == 0) || (i == 3 && j == 3) || (i == 6 && j == 6)) {
                    if (board[i][j].equals(opponentBot) && board[i + 1][j + 1].equals(opponentBot) && board[i + 2][j + 2].equals(IField.EMPTY_FIELD)) {
                        preventiveScore += 100; // Give a high score for blocking opponent's winning move in the diagonal
                    }
                }
                // Check anti-diagonal
                if ((i == 0 && j == 6) || (i == 3 && j == 3) || (i == 6 && j == 0)) {
                    if (board[i][j + 2].equals(opponentBot) && board[i + 1][j + 1].equals(opponentBot) && board[i + 2][j].equals(IField.EMPTY_FIELD)) {
                        preventiveScore += 100; // Give a high score for blocking opponent's winning move in the anti-diagonal
                    }
                }
            }
        }
        return preventiveScore;
    }

    private boolean isOpponentWinningNextMove(String[][] macroboard, String opponentBot) {
        // Check if the opponent is winning the game on the next move
        for (String[] row : macroboard) {
            for (String cell : row) { if (cell.equals(opponentBot)) { return true; }} // Opponent is winning the game
        }
        return false;
    }



    private boolean isHyggeBotWinningNextMove(String[][] macroboard, String hyggeBot) {
        // Check if HyggeBot is winning the game on the next move
        for (String[] row : macroboard) {
            for (String cell : row) { if (cell.equals(hyggeBot)) { return true; }} // HyggeBot is winning the game
        }
        return false;
    }

    private boolean isOpponentWinningMacroboardNextMove(String[][] macroboard, String opponentBot) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (macroboard[i][0].equals(opponentBot) && macroboard[i][1].equals(opponentBot) && macroboard[i][2].equals(opponentBot)) {
                return true;
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (macroboard[0][i].equals(opponentBot) && macroboard[1][i].equals(opponentBot) && macroboard[2][i].equals(opponentBot)) {
                return true;
            }
        }
        // Check diagonals
        if (macroboard[0][0].equals(opponentBot) && macroboard[1][1].equals(opponentBot) && macroboard[2][2].equals(opponentBot)) {
            return true;
        }
        if (macroboard[0][2].equals(opponentBot) && macroboard[1][1].equals(opponentBot) && macroboard[2][0].equals(opponentBot)) {
            return true;
        }
        return false;
    }

    private String getHyggeBot(IGameState state) {return state.getMoveNumber() % 2 == 0 ? "0" : "1";}

    private String getOpponentBot(IGameState state) {return getHyggeBot(state).equals("0") ? "1" : "0";}
    private static final int MAX_DEPTH = 5 ; // Adjust this depth according to your requirements

    private List<IMove> findWinningMoves(IGameState state, String player) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        List<IMove> winningMoves = new ArrayList<>();
        for (IMove move : availableMoves) { if (isWinningMove(state, move, player)) { winningMoves.add(move); }}
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
        for (int i = 0; i < board.length; i++) { clonedBoard[i] = board[i].clone(); }
        return clonedBoard;
    }

    /*
        The code below is a simulator for simulation of gameplay. This is needed for AI.
        It is put here to make the Bot independent of the GameManager and its subclasses/enums
        Now this class is only dependent on a few interfaces: IMove, IField, and IGameState
        You could say it is self-contained. The drawback is that if the game rules change, the simulator must be
        changed accordingly, making the code redundant.
     */
    @Override
    public String getBotName() {return BOT_NAME;}

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
        public int getX() { return x; }

        @Override
        public int getY() { return y; }

        @Override
        public String toString() { return "(" + x + "," + y + ")"; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Move move = (Move) o;
            return x == move.x && y == move.y;
        }

        @Override
        public int hashCode() { return Objects.hash(x, y); }
    }

    class GameSimulator {
        private final IGameState currentState;
        private int currentPlayer = 0; //player0 == 0 && player1 == 1
        private volatile GameOverState gameOver = GameOverState.Active;

        public void setGameOver(GameOverState state) { gameOver = state; }

        public GameOverState getGameOver() { return gameOver; }

        public void setCurrentPlayer(int player) { currentPlayer = player; }

        public IGameState getCurrentState() { return currentState; }

        public GameSimulator(IGameState currentState) { this.currentState = currentState; }

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
            if (currentState.getMoveNumber() % 2 == 0) { currentState.setRoundNumber(currentState.getRoundNumber() + 1); }
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

    public class GameState implements IGameState{
        IField field;
        int moveNumber;
        int roundNumber;
        int timePerMove = 1000; //1000ms default value, can be changes depending on game specifics.

        public GameState(){
            field = new Field();
            moveNumber=0;
            roundNumber=0;
        }

        public GameState(IGameState state) {
            field = new Field();
            field.setMacroboard(state.getField().getMacroboard());
            field.setBoard(state.getField().getBoard());

            moveNumber = state.getMoveNumber();
            roundNumber = state.getRoundNumber();
        }

        @Override
        public IField getField() { return field; }

        @Override
        public int getMoveNumber() { return moveNumber; }

        @Override
        public void setMoveNumber(int moveNumber) { this.moveNumber=moveNumber; }

        @Override
        public int getRoundNumber() { return roundNumber; }

        @Override
        public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

        @Override
        public int getTimePerMove() { return this.timePerMove; }

        @Override
        public void setTimePerMove(int milliSeconds) { this.timePerMove = milliSeconds;}
    }
}