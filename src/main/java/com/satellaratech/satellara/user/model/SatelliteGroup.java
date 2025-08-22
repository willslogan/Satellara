package com.satellaratech.satellara.user.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class SatelliteGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String groupName;


    @ElementCollection
    private List<Integer> noradIds;

    private int r;
    private int g;
    private int b;
    private int a;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    public SatelliteGroup() {
    }

    public SatelliteGroup(long id, String groupName, List<Integer> noradIds, int r, int g, int b, int a, User user) {
        this.id = id;
        this.groupName = groupName;
        this.noradIds = noradIds;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<Integer> getNoradIds() {
        return noradIds;
    }

    public void setNoradIds(List<Integer> noradIds) {
        this.noradIds = noradIds;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String toString() {
        return groupName + " " + noradIds + " " + r + " " + g + " " + b + " " + a + " " + user;
    }
}
