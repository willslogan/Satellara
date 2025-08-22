package com.satellaratech.satellara.user.controller;

import com.satellaratech.satellara.user.model.SatelliteGroup;
import com.satellaratech.satellara.user.model.User;
import com.satellaratech.satellara.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Optional<User> getUser() {
        return userService.getCurrentUser();
    }

    @PostMapping
    public User createUser() {
        return userService.save();
    }

    @PutMapping("/favorites")
    public User addFavorites(@RequestBody SatelliteGroup satelliteGroup) {
        System.out.println("This called and" + satelliteGroup);
        return userService.addGroupToUser(satelliteGroup);
    }

    @DeleteMapping("/favorites")
    public User deleteFromFavorites(String satelliteGroup) {
        return userService.removeGroupFromUser(satelliteGroup);
    }

    @PutMapping("/favorites/{groupName}/{noradId}")
    public SatelliteGroup updateFavorites(@PathVariable String groupName, @PathVariable int noradId) {
        return userService.addSatelliteToGroup(groupName, noradId);
    }

    @DeleteMapping("/favorites/{groupName}/{noradId}")
    public SatelliteGroup deleteFromFavorites(@PathVariable String groupName, @PathVariable int noradId) {
        return userService.removeSatelliteFromGroup(groupName, noradId);
    }
}
