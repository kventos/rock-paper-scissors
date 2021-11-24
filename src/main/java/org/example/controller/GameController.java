package org.example.controller;

import com.google.gson.Gson;
import org.example.service.OverallStatsKeeper;
import org.example.service.RPSResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GameController {

    private RPSResolver rpsResolver;
    private OverallStatsKeeper statsKeeper;

    @Autowired
    public GameController(RPSResolver rpsResolver, OverallStatsKeeper statsKeeper) {
        this.rpsResolver = rpsResolver;
        this.statsKeeper = statsKeeper;
    }

    private final Gson gson = new Gson();

    @GetMapping("/play/{move}")
    public String play(@PathVariable String move, HttpServletRequest request) {
        request.getSession().setMaxInactiveInterval(7 * 24 * 60 * 60);

        RPSResolver.Response res = rpsResolver.play(move);
        statsKeeper.count(request.getSession().getId(), res);

        return gson.toJson(res);
    }

    @GetMapping("/stop")
    public String stop(HttpServletRequest request) {
        RPSResolver.DetailedStats ds = rpsResolver.stop();
        ds.setBetterThanOtherUsersPercentage(statsKeeper.getUserPlace(request.getSession().getId()));
        return gson.toJson(ds);
    }
}
