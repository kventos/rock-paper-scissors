package org.example.controller;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.service.OverallStatsKeeper;
import org.example.service.RPSResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class GameController {
    private static final Log LOG = LogFactory.getLog(GameController.class);

    private RPSResolver rpsResolver;
    private OverallStatsKeeper statsKeeper;

    @Autowired
    public GameController(RPSResolver rpsResolver, OverallStatsKeeper statsKeeper) {
        this.rpsResolver = rpsResolver;
        this.statsKeeper = statsKeeper;
    }

    private final Gson gson = new Gson();

    @PostMapping("/play/{move}")
    public String play(@PathVariable String move, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            request.getSession().setMaxInactiveInterval(7 * 24 * 60 * 60);

            RPSResolver.Response res = rpsResolver.play(move);
            statsKeeper.count(request.getSession().getId(), res);

            return gson.toJson(res);
        } catch (Exception ex) {
            String message = "Playing failed. Your move: " + move;
            LOG.error(message, ex);
            response.sendError(400, message);
            return message;
        }
    }

    @PutMapping("/stop")
    public String stop(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            RPSResolver.DetailedStats ds = rpsResolver.stop();
            ds.setBetterThanOtherUsersPercentage(statsKeeper.getUserPlace(request.getSession().getId()));
            return gson.toJson(ds);
        } catch (Exception ex) {
            String msg = "Failed to stop the game";
            LOG.error(msg, ex);
            response.sendError(500, msg);
            return msg;
        }
    }
}
