package org.example.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class RPSResolverTest {

    @Autowired
    RPSResolver resolver;

    @Test
    public void playAgainstRandomTest() {
        Random rand = new Random();
        RPSResolver.Response response = null;

        int rounds = 1000;
        for (int i = 0; i < rounds; i++) {
            RPSResolver.Move pseudoRandomMove = RPSResolver.Move.values()[rand.nextInt(3)];

            response = resolver.play(pseudoRandomMove.toString());
        }

        int ties = response.statistics.get(RPSResolver.Decision.TIE.toString());
        int victories = response.statistics.get(RPSResolver.Decision.VICTORY.toString());
        int defeats = response.statistics.get(RPSResolver.Decision.DEFEAT.toString());

        double ninetyPercentThreshold = rounds * 0.3;
        assertTrue(ties > ninetyPercentThreshold);
        assertTrue(victories > ninetyPercentThreshold);
        assertTrue(defeats > ninetyPercentThreshold);
    }

    @Test
    public void playAgainstHumanHistory() {

    }
}