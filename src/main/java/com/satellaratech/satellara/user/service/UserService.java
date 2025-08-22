package com.satellaratech.satellara.user.service;

import com.satellaratech.satellara.exception.ErrorType;
import com.satellaratech.satellara.exception.SatelliteGroupException;
import com.satellaratech.satellara.exception.UserException;
import com.satellaratech.satellara.user.model.SatelliteGroup;
import com.satellaratech.satellara.user.model.User;
import com.satellaratech.satellara.user.repository.SatelliteGroupRepository;
import com.satellaratech.satellara.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final SatelliteGroupRepository satelliteGroupRepository;

    public UserService(UserRepository userRepository, SatelliteGroupRepository satelliteGroupRepository) {
        this.userRepository = userRepository;
        this.satelliteGroupRepository = satelliteGroupRepository;
    }

    public User save() {
        String userId = getCurrentUserId();
        if(userId.isEmpty()) {
            throw new UserException("User not Found", ErrorType.NOT_FOUND);
        }

        User user = new User();
        user.setUserId(userId);
        user.setGroups(new ArrayList<>());

        return userRepository.save(user);
    }

    public Optional<User> getCurrentUser() {
        String userId = getCurrentUserId();
        if (userId.isBlank())
            return Optional.empty();
        return userRepository.findById(getCurrentUserId());
    }

    @Transactional
    public User addGroupToUser( SatelliteGroup group) {
        User user = getCurrentUser().orElseThrow(() -> new UserException("User not found", ErrorType.NOT_FOUND));
        user.addGroup(group);

        return user;
    }

    @Transactional
    public User removeGroupFromUser(String groupName) {
        User user = getCurrentUser().orElseThrow(() -> new UserException("User not found", ErrorType.NOT_FOUND));
        SatelliteGroup group = satelliteGroupRepository.findByUser_UserIdAndGroupName(user.getUserId(), groupName).orElseThrow(() -> new SatelliteGroupException("Group not found", ErrorType.NOT_FOUND));

        user.removeGroup(group);

        return user;
    }

    @Transactional
    public List<SatelliteGroup> getGroups() {
        User user = getCurrentUser().orElseThrow(() -> new UserException("User not found", ErrorType.NOT_FOUND));
        return user.getGroups();
    }

    @Transactional
    public SatelliteGroup addSatelliteToGroup(String groupName, int noradId) {
        User user = getCurrentUser().orElseThrow(() -> new UserException("User not found", ErrorType.NOT_FOUND));
        SatelliteGroup group = satelliteGroupRepository.findByUser_UserIdAndGroupName(user.getUserId(), groupName).orElseThrow(() -> new SatelliteGroupException("Group not found", ErrorType.NOT_FOUND));
        group.getNoradIds().add(noradId);

        return group;
    }

    @Transactional
    public SatelliteGroup removeSatelliteFromGroup(String groupName, int noradId) {
        User user = getCurrentUser().orElseThrow(() -> new UserException("User not found", ErrorType.NOT_FOUND));
        SatelliteGroup group = satelliteGroupRepository.findByUser_UserIdAndGroupName(user.getUserId(), groupName).orElseThrow(() -> new SatelliteGroupException("Group not found", ErrorType.NOT_FOUND));
        group.getNoradIds().remove(Integer.valueOf(noradId));

        return group;
    }

    public String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return "";
        }

        if (!auth.isAuthenticated()) {
            return "";
        }

        return auth.getName();
    }
}
