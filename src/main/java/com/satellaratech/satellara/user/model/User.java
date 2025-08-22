package com.satellaratech.satellara.user.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    private String userId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SatelliteGroup> groups = new ArrayList<>();

    public User() {
    }

    public User(String userId, List<SatelliteGroup> groups) {
        this.userId = userId;
        this.groups = groups;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String user_id) {
        this.userId = user_id;
    }

    public List<SatelliteGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<SatelliteGroup> groups) {
        this.groups = groups;
    }

    public void addGroup(SatelliteGroup group) {
        groups.add(group);
        group.setUser(this);
    }

    public void removeGroup(SatelliteGroup group) {
        groups.remove(group);
        group.setUser(null);
    }
}
