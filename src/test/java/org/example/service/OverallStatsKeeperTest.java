package org.example.service;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OverallStatsKeeperTest {

    private OverallStatsKeeper statsKeeper;

    @Before
    public void setUp() {
        statsKeeper = new OverallStatsKeeper();
        statsKeeper.count("1111", new RPSResolver.Response("ROCK", "PAPER",
                RPSResolver.Decision.DEFEAT,
                new RPSResolver.BaseStats(15, 20, 20)));
        statsKeeper.count("2222", new RPSResolver.Response("SCISSORS", "ROCK",
                RPSResolver.Decision.DEFEAT,
                new RPSResolver.BaseStats(9, 100, 50)));
        statsKeeper.count("3333", new RPSResolver.Response("PAPER", "PAPER",
                RPSResolver.Decision.TIE,
                new RPSResolver.BaseStats(15, 1, 2)));
        statsKeeper.count("4444", new RPSResolver.Response("ROCK", "PAPER",
                RPSResolver.Decision.DEFEAT,
                new RPSResolver.BaseStats(10, 2, 2)));
        statsKeeper.count("5555", new RPSResolver.Response("ROCK", "PAPER",
                RPSResolver.Decision.DEFEAT,
                new RPSResolver.BaseStats(15, 2000, 20)));
    }

    @Test
    public void getUserPlaceTest() {
        assertEquals(0.6, statsKeeper.getUserPlace("1111"), 0.00000001);
        assertEquals(0.4, statsKeeper.getUserPlace("2222"), 0.00000001);
        assertEquals(1.0, statsKeeper.getUserPlace("3333"), 0.00000001);
        assertEquals(0.8, statsKeeper.getUserPlace("4444"), 0.00000001);
        assertEquals(0.2, statsKeeper.getUserPlace("5555"), 0.00000001);

    }
}