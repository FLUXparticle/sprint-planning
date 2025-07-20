package com.example.sprintplanning.model;

public class Task {

    private String text;

    public Task(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

}
