package com.example.sprintplanning.model;

import jakarta.xml.bind.*;

import java.io.*;

public class SpringPlanningModel {

    public static void main(String[] args) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Tasks.class);

        File file = new File("planning/2025-07-14.xml");
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Tasks tasks = (Tasks) unmarshaller.unmarshal(file);

        for (Task task : tasks.getTasks()) {
            printTask(task, 0);
        }
    }

    private static void printTask(Task task, int indent) {
        System.out.println("  ".repeat(indent) + task.getText());
        for (Task sub : task.getChildren()) {
            printTask(sub, indent + 1);
        }
    }

}
