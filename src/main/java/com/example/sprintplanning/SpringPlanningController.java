package com.example.sprintplanning;

import com.example.sprintplanning.model.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

public class SpringPlanningController {

    private final SpringPlanning view;

    public SpringPlanningController(SpringPlanning view) {
        this.view = view;

        // Event bindings
        view.weekListView.setOnMouseClicked(this::onWeekSelected);

        view.btnNew.setOnAction(this::onNewTask);
        view.btnDelete.setOnAction(this::onDeleteTask);
        view.btnIndent.setOnAction(this::onIndentTask);
        view.btnOutdent.setOnAction(this::onOutdentTask);
        view.btnImportant.setOnAction(this::onToggleImportant);
        view.btnUrgent.setOnAction(this::onToggleUrgent);
        view.btnDone.setOnAction(this::onToggleDone);

        view.taskTreeView.setOnEditCommit(this::onTaskRename);
    }

    // Controller method stubs for Use Cases

    public void onWeekSelected(MouseEvent event) {
        System.out.println("loading of week plan");
    }

    public void onNewTask(ActionEvent event) {
        System.out.println("creation of a new task");
    }

    public void onDeleteTask(ActionEvent event) {
        System.out.println("deletion of selected task(s)");
    }

    public void onIndentTask(ActionEvent event) {
        System.out.println("indenting of selected task");
    }

    public void onOutdentTask(ActionEvent event) {
        System.out.println("outdenting of selected task");
    }

    public void onToggleImportant(ActionEvent event) {
        System.out.println("toggling important flag");
    }

    public void onToggleUrgent(ActionEvent event) {
        System.out.println("toggling urgent flag");
    }

    public void onToggleDone(ActionEvent event) {
        System.out.println("toggling done flag");
    }

    public void onTaskRename(TreeView.EditEvent<Task> event) {
        System.out.println("renaming of task on edit commit");
    }

}
