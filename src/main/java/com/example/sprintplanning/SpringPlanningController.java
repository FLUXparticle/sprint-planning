package com.example.sprintplanning;

import com.example.sprintplanning.model.*;
import jakarta.xml.bind.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.input.*;

import java.io.*;

public class SpringPlanningController {

    private static final File FOLDER = new File("planning");

    private final SpringPlanning view;

    private final SpringPlanningModel model;

    public SpringPlanningController(SpringPlanning view, SpringPlanningModel model) {
        this.view = view;
        this.model = model;

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

        loadWeekPlans();
    }

    public void loadWeekPlans() {
        if (FOLDER.exists() && FOLDER.isDirectory()) {
            File[] files = FOLDER.listFiles((dir, name) -> name.endsWith(".xml"));
            if (files != null) {
                view.weekListView.getItems().clear();
                for (File file : files) {
                    view.weekListView.getItems().add(file.getName());
                }
            }
        }
    }

    // Controller methods for Use Cases

    public void onWeekSelected(MouseEvent event) {
        String selectedFile = view.weekListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) return;

        File file = new File(FOLDER, selectedFile);
        try {
            model.loadWeekPlan(file.getPath());

            TreeItem<Task> root = new TreeItem<>(new Task("Root"));
            root.setExpanded(true);

            for (Task task : model.getTasks()) {
                root.getChildren().add(createTreeItem(task));
            }

            view.taskTreeView.setRoot(root);
            view.taskTreeView.setShowRoot(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private TreeItem<Task> createTreeItem(Task task) {
        TreeItem<Task> item = new TreeItem<>(task);

        // Setze initial den Expand-Status entsprechend dem Model
        item.setExpanded(task.isOpen());

        // Wenn der Nutzer ein- oder ausklappt, übertrage das ins Model
        item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
            task.setOpen(isNowExpanded);
            try {
                model.saveWeekPlan();
            } catch (JAXBException e) {
                // TODO throw new RuntimeException(e);
                e.printStackTrace();
            }
        });

        // Rekursiv Kind-Knoten anlegen
        for (Task child : task.getChildren()) {
            item.getChildren().add(createTreeItem(child));
        }
        return item;
    }

}
