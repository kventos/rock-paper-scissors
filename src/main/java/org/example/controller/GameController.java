package org.example.controller;

import com.google.gson.Gson;
import org.example.service.RPSResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    private RPSResolver rpsResolver;

    @Autowired
    public GameController( RPSResolver rpsResolver) {
        this.rpsResolver = rpsResolver;
    }

    private final Gson gson = new Gson();

    @GetMapping("/play/{move}")
    public String play(@PathVariable String move) {
        RPSResolver.Response res = rpsResolver.play(move);
        return gson.toJson(res);
    }

}
