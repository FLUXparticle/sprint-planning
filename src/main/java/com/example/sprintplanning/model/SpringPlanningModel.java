package com.example.sprintplanning.model;

import jakarta.xml.bind.*;

import java.io.*;
import java.util.*;

import static java.util.Collections.*;

public class SpringPlanningModel {

    private final Unmarshaller unmarshaller;

    private Tasks tasks;

    public SpringPlanningModel() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Tasks.class);
        unmarshaller = context.createUnmarshaller();
    }

    public void loadWeekPlan(String pathname) throws JAXBException {
        File file = new File(pathname);
        tasks = (Tasks) unmarshaller.unmarshal(file);
    }

    public static void main(String[] args) throws JAXBException {
        SpringPlanningModel model = new SpringPlanningModel();
        model.loadWeekPlan("planning/2025-07-21.xml");

        for (Task task : model.tasks.getTasks()) {
            printTask(task, 0);
        }
    }

    private static void printTask(Task task, int indent) {
        System.out.println("  ".repeat(indent) + task.getText());
        for (Task sub : task.getChildren()) {
            printTask(sub, indent + 1);
        }
    }

    public List<Task> getTasks() {
        return tasks != null ? tasks.getTasks() : emptyList();
    }

}
