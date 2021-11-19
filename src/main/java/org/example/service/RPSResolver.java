package org.example.service;

import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class RPSResolver {

    public static final int LENGTH = Move.values().length;

    private final int[][] markovChain;
    private final Random rand = new Random();
    private Deque<Move> movesHistory;
    private Map<String, Integer> statistics;


    public RPSResolver() {
        movesHistory = new LinkedList<>();
        statistics = new HashMap<>();
        markovChain = new int[LENGTH][LENGTH];
        Arrays.stream(markovChain).forEach(arr ->  Arrays.fill(arr, 0)); // todo I guess it's not necessary
    }

    public Response play(String moveStr) {
        Move move = Move.valueOf(moveStr.toUpperCase()); // todo check if it's parsable
        Move lastMove = movesHistory.peekLast();
        Move aiMove = nextMove(lastMove);

        if (lastMove != null) {
            ++markovChain[lastMove.ordinal()][move.ordinal()];
        }
        movesHistory.addLast(move);

        return makeDecision(move, aiMove);
    }

    private Response makeDecision(Move move, Move aiMove) {
        if (move == aiMove) {
            String tieKey = Decision.TIE.toString();
            statistics.put(tieKey, statistics.getOrDefault(tieKey, 0) + 1);
            return new Response(move.toString(), aiMove.toString(), Decision.TIE, statistics);
        } else if (move.losesTo == aiMove) {
            String defeatKey = Decision.DEFEAT.toString();
            statistics.put(defeatKey, statistics.getOrDefault(defeatKey, 0) + 1);
            return new Response(move.toString(), aiMove.toString(), Decision.DEFEAT, statistics); // ai won
        } else {
            String victoryKey = Decision.VICTORY.toString();
            statistics.put(victoryKey, statistics.getOrDefault(victoryKey, 0) + 1);
            return new Response(move.toString(), aiMove.toString(), Decision.VICTORY, statistics); // ai lost
        }
    }

    private Move nextMove(Move last) {
        if (movesHistory.size() < 1) {
            return Move.values()[rand.nextInt(Move.values().length)];
        }
        int maxJ = 0; // todo make it random
        int ordinal = last.ordinal();
        for (int j = 0; j < LENGTH; j++) {
            if (markovChain[ordinal][j] > markovChain[ordinal][maxJ]) {
                maxJ = j;
            }
        }
        return Move.values()[maxJ].losesTo;
    }

    // for tests
    public void resolve(String prevMoves) {
        final int[][] markovChain = new int[LENGTH][LENGTH];
    }

    public enum Move {
        ROCK("R"),
        SCISSORS("S"),
        PAPER("P");

        public String code;
        public Move losesTo;

        Move(String code) {
            this.code = code;
        }

        static {
            SCISSORS.losesTo = ROCK;
            ROCK.losesTo = PAPER;
            PAPER.losesTo = SCISSORS;
        }
    }

    public static class Response {
        String playerMove;
        String aiMove;
        Decision result;
        Map <String, Integer> statistics;


        public Response(String playerMove, String aiMove, Decision result, Map<String, Integer> statistics) {
            this.playerMove = playerMove;
            this.aiMove = aiMove;
            this.result = result;
            this.statistics = statistics;
        }
    }

    enum Decision {
        TIE, VICTORY, DEFEAT;
    }
}
