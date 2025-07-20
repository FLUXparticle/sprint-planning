package com.example.sprintplanning.model;

import jakarta.xml.bind.annotation.*;

import java.util.*;

@XmlRootElement(name = "tasks")
public class Tasks {

    @XmlElement(name = "task")
    private List<Task> tasks = new ArrayList<>();

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

}
