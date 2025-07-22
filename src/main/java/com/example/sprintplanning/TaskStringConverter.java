package com.example.sprintplanning;

import com.example.sprintplanning.model.*;
import javafx.util.*;

class TaskStringConverter extends StringConverter<Task> {

    @Override
    public String toString(Task task) {
//        System.out.println("toString: " + task);
        return task == null ? "" : task.getText();
    }

    @Override
    public Task fromString(String string) {
//        System.out.println("fromString: " + string);
        return new Task(string);
    }

}
