package org.example.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RPSResolverTest {

    @Autowired
    RPSResolver resolver;

    public static final int ROUNDS = 1000;

    @Before
    public void before() {
        resolver.resetState();
    }

    @Test
    public void playAgainstRandomTest() {
        Random rand = new Random();
        RPSResolver.Response response = null;

        for (int i = 0; i < ROUNDS; i++) {
            RPSResolver.Move pseudoRandomMove = RPSResolver.Move.values()[rand.nextInt(3)];

            response = resolver.play(pseudoRandomMove.toString());
        }

        int ties = response.statistics.getTies();
        int victories = response.statistics.getVictories();
        int defeats = response.statistics.getDefeats();

        double threshold = ROUNDS * 0.2;
        assertTrue(ties > threshold);
        assertTrue(victories > threshold);
        assertTrue(defeats > threshold);
    }

    @Test
    public void oneMoveTest() {
        RPSResolver.Response response = resolver.play(RPSResolver.Move.SCISSORS.toString());
        RPSResolver.BaseStats statistics = response.statistics;

        assertEquals(RPSResolver.Move.SCISSORS.toString(), response.playerMove);
        assertEquals(1, statistics.getTies() + statistics.getDefeats() + statistics.getVictories());
    }

    @Test
    public void sequenceMovesTest() {
        RPSResolver.Response response = null;
        for (int i = 0; i < ROUNDS; i++) {
            response = resolver.play(RPSResolver.Move.PAPER.toString());
        }

        int defeats = response.statistics.getDefeats();

        assertTrue(defeats > ROUNDS - 4); // ai should win at least 997 times
    }

    @Test
    public void simpleMovesTest() {
        String[] moves = new String[] {"paper", "paper", "scissors", "paper", "rock", "scissors", "scissors", "rock", "paper"};
        RPSResolver.Response response = null;

        for (String move : moves) {
            response = resolver.play(move);
        }

        assertTrue(response.statistics.getDefeats() > 0); // ai wins
    }

    @Test
    public void invalidInputTest() {
        String move = "asdf";
        RPSResolver.Response response = resolver.play(move);
        assertEquals(RPSResolver.Response.error("Playing failed. Your move: asdf"), response);
    }

    @Test
    public void stopGameTest() {
        String[] moves = new String[] {"PAPER", "PAPER", "PAPER", "PAPER", "PAPER", "PAPER", "SCISSORS", "ROCK", "PAPER"};

        for (String move : moves) {
            resolver.play(move);
        }
        RPSResolver.DetailedStats stats = resolver.stop();

        assertEquals(moves.length, stats.sum());
        assertEquals(Arrays.asList(moves), stats.getMovesHistory());
        assertEquals("PAPER", stats.getFavouriteMove());
        assertEquals(7 / 9.0, stats.getPercentageOfFavouriteMove(), 0.0000001);
        assertEquals(0.0, stats.getBetterThanOtherUsersPercentage(), 0.0000001);
        assertTrue(Double.compare(stats.getWinRate(), 1 / 3.0) <= 0); // win rate of player
    }

    @Test
    public void stopGameOneStateTest() {
        String[] moves = new String[] {"ROCK", "ROCK", "ROCK", "ROCK", "ROCK", "ROCK"};

        for (String move : moves) {
            resolver.play(move);
        }
        RPSResolver.DetailedStats stats = resolver.stop();

        assertEquals(moves.length, stats.sum());
        assertEquals(Arrays.asList(moves), stats.getMovesHistory());
        assertEquals("ROCK", stats.getFavouriteMove());
        assertEquals(1.0, stats.getPercentageOfFavouriteMove(), 0.0000001);
        assertEquals(0.0, stats.getBetterThanOtherUsersPercentage(), 0.0000001);
        assertTrue(Double.compare(stats.getWinRate(), 1 / 3.0) <= 0); // win rate of player
    }

    @Test
    public void stopGameEqualStatesTest() {
        String[] moves = new String[] {"ROCK", "ROCK", "PAPER", "PAPER", "SCISSORS", "SCISSORS"};

        for (String move : moves) {
            resolver.play(move);
        }
        RPSResolver.DetailedStats stats = resolver.stop();

        assertEquals(moves.length, stats.sum());
        assertEquals(Arrays.asList(moves), stats.getMovesHistory());
        assertEquals("ROCK", stats.getFavouriteMove());
        assertEquals(1 / 3.0, stats.getPercentageOfFavouriteMove(), 0.0000001);
        assertEquals(0.0, stats.getBetterThanOtherUsersPercentage(), 0.0000001);
    }
}