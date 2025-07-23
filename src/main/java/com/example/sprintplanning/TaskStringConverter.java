package com.example.sprintplanning;

import com.example.sprintplanning.model.*;
import javafx.util.*;

class TaskStringConverter extends StringConverter<Task> {

    @Override
    public String toString(Task task) {
        String string = task == null ? "" : task.getText();
//        System.out.println("toString(" + string + ") = " + System.identityHashCode(task));
        return string;
    }

    @Override
    public Task fromString(String string) {
        Task task = new Task(string);
//        System.out.println("fromString(" + string + ") = " + System.identityHashCode(task));
        return task;
    }

}
