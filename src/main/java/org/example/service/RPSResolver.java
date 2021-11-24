package org.example.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


@Component
@SessionScope
public class RPSResolver implements Serializable {
    private static final long serialVersionUID = -8077201717643372583L;
    private static final Log LOG = LogFactory.getLog(RPSResolver.class);

    private static final int LENGTH = Move.values().length;

    private final int[][] markovChain;
    private final Random rand = new Random();
    private LinkedList<Move> movesHistory;
    private BaseStats statistics;


    public RPSResolver() {
        movesHistory = new LinkedList<>();
        statistics = new BaseStats();
        markovChain = new int[LENGTH * LENGTH][LENGTH];
    }


    public Response play(String moveStr) {
        try {
            Move move = Move.valueOf(moveStr.toUpperCase());
            Move lastMove = movesHistory.peekLast();
            Move preLastMove = movesHistory.size() > 1 ? movesHistory.get(movesHistory.size() - 2) : null;
            Move aiMove = nextMove(lastMove, preLastMove);

            updateState(move, lastMove, preLastMove);

            return makeDecision(move, aiMove);
        } catch (Exception ex) {
            String message = "Playing failed. Your move: " + moveStr;
            LOG.error(message, ex);
            return Response.error(message);
        }
    }

    public DetailedStats stop() {
        DetailedStats ds = new DetailedStats(statistics);
        if (!movesHistory.isEmpty()) {
            Map<String, Integer> reducedMovesHistory = movesHistory.stream()
                    .collect(Collectors.toMap(Enum::toString, m -> 1, Integer::sum));
            reducedMovesHistory.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .ifPresent(m -> ds.setFavouriteMove(m.getKey()));
            Move favouriteMove = Move.valueOf(ds.getFavouriteMove());
            ds.setPercentageOfFavouriteMove(
                    reducedMovesHistory.getOrDefault(favouriteMove.toString(), 0) /
                            (double) movesHistory.size());
            ds.setWinRate(statistics.winRate());
            ds.setMovesHistory(movesHistory.stream()
                    .map(Enum::toString)
                    .collect(Collectors.toList()));
        }
        reset();
        return ds;
    }

    void resetState() {
        Arrays.stream(markovChain).forEach(arr ->  Arrays.fill(arr, 0));
        reset();
    }

    void reset() {
        statistics.reset();
        movesHistory.clear();
    }

    private Move nextMove(Move last, Move preLast) {
        int randomChoice = rand.nextInt(Move.values().length);
        if (movesHistory.size() < 2) {
            return Move.values()[randomChoice];
        }
        int maxJ = randomChoice;
        int row = LENGTH * preLast.ordinal() + last.ordinal();
        for (int j = 0; j < LENGTH; j++) {
            if (markovChain[row][j] > markovChain[row][maxJ]) {
                maxJ = j;
            }
        }
        return Move.values()[maxJ].losesTo;
    }

    private void updateState(Move move, Move lastMove, Move preLastMove) {
        if (lastMove != null && preLastMove != null) {
            ++markovChain[LENGTH * preLastMove.ordinal() + lastMove.ordinal()][move.ordinal()];
        }
        movesHistory.addLast(move);
    }

    private Response makeDecision(Move move, Move aiMove) {
        if (move == aiMove) {
            statistics.setTies(statistics.getTies() + 1);
            return new Response(move.toString(), aiMove.toString(), Decision.TIE, statistics);
        } else if (move.losesTo == aiMove) {
            statistics.setDefeats(statistics.getDefeats() + 1);
            return new Response(move.toString(), aiMove.toString(), Decision.DEFEAT, statistics); // ai won
        } else {
            statistics.setVictories(statistics.getVictories() + 1);
            return new Response(move.toString(), aiMove.toString(), Decision.VICTORY, statistics); // ai lost
        }
    }

    public enum Move {
        ROCK,
        SCISSORS,
        PAPER;

        public Move losesTo;

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
        BaseStats statistics;

        public Response(String error) {
            this.aiMove = error;
        }

        public Response(String playerMove, String aiMove, Decision result, BaseStats statistics) {
            this.playerMove = playerMove;
            this.aiMove = aiMove;
            this.result = result;
            this.statistics = statistics;
        }

        public static Response error(String error) {
            return new Response(error);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Response response = (Response) o;

            if (!Objects.equals(playerMove, response.playerMove))
                return false;
            if (!Objects.equals(aiMove, response.aiMove)) return false;
            if (result != response.result) return false;
            return Objects.equals(statistics, response.statistics);
        }
    }

    enum Decision {
        TIE, VICTORY, DEFEAT
    }

    static class BaseStats implements Serializable {
        private static final long serialVersionUID = -1499436174537734732L;

        private int victories;
        private int defeats;
        private int ties;

        public BaseStats() {}

        public BaseStats(int victories, int defeats, int ties) {
            this.victories = victories;
            this.defeats = defeats;
            this.ties = ties;
        }

        void reset() {
            victories = 0;
            defeats = 0;
            ties = 0;
        }

        double winRate() {
            return victories / (double) sum();
        }

        int sum() {
            return victories + defeats + ties;
        }

        public int getVictories() {
            return victories;
        }

        public void setVictories(int victories) {
            this.victories = victories;
        }

        public int getDefeats() {
            return defeats;
        }

        public void setDefeats(int defeats) {
            this.defeats = defeats;
        }

        public int getTies() {
            return ties;
        }

        public void setTies(int ties) {
            this.ties = ties;
        }
    }

    public static class DetailedStats extends BaseStats {
        private String favouriteMove;
        private double percentageOfFavouriteMove;
        private double winRate;
        private List<String> movesHistory;
        private double betterThanOtherUsersPercentage;

        public DetailedStats(BaseStats baseStats) {
            super(baseStats.getVictories(), baseStats.getDefeats(), baseStats.getTies());
        }

        public String getFavouriteMove() {
            return favouriteMove;
        }

        public void setFavouriteMove(String favouriteMove) {
            this.favouriteMove = favouriteMove;
        }

        public double getPercentageOfFavouriteMove() {
            return percentageOfFavouriteMove;
        }

        public void setPercentageOfFavouriteMove(double percentageOfFavouriteMove) {
            this.percentageOfFavouriteMove = percentageOfFavouriteMove;
        }

        public double getWinRate() {
            return winRate;
        }

        public void setWinRate(double winRate) {
            this.winRate = winRate;
        }

        public List<String> getMovesHistory() {
            return movesHistory;
        }

        public void setMovesHistory(List<String> movesHistory) {
            this.movesHistory = movesHistory;
        }

        public double getBetterThanOtherUsersPercentage() {
            return betterThanOtherUsersPercentage;
        }

        public void setBetterThanOtherUsersPercentage(double betterThanOtherUsersPercentage) {
            this.betterThanOtherUsersPercentage = betterThanOtherUsersPercentage;
        }
    }
}
