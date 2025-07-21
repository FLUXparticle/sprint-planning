package com.example.sprintplanning;

import com.example.sprintplanning.model.*;
import jakarta.xml.bind.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.util.*;

import java.io.*;
import java.util.*;

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
        view.btnOptional.setOnAction(this::onToggleOptional);
        view.btnObsolete.setOnAction(this::onToggleObsolete);

        view.taskTreeView.setOnEditCommit(this::onTaskRename);
        view.taskTreeView.setCellFactory(tv -> {
            CheckBoxTreeCell<Task> cell = new CheckBoxTreeCell<>(param -> {
                if (param instanceof CheckBoxTreeItem<Task> item) {
                    if (item.getValue() != null) {
                        return new SimpleBooleanProperty(item.getValue().isDone()) {
                            @Override
                            public void set(boolean newValue) {
                                super.set(newValue);
                                item.getValue().setDone(newValue);
                                refreshTreeItem(item);
                                save();
                            }
                        };
                    }
                }
                return null;
            }, new StringConverter<>() {
                @Override
                public String toString(TreeItem<Task> item) {
                    Task task = item.getValue();
                    return task == null ? "" : task.getText();
                }

                @Override
                public TreeItem<Task> fromString(String string) {
                    return createTreeItem(new Task(string));
                }
            });

            cell.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !cell.isEmpty()) {
                    view.taskTreeView.edit(cell.getTreeItem());
                }
            });

            cell.itemProperty().addListener((obs, oldTask, newTask) -> {
                if (newTask != null) {
                    StringBuilder label = new StringBuilder();
                    if (newTask.isImportant()) {
                        label.append("⭐ ");
                    }
                    label.append(newTask.getText());
                    cell.setText(label.toString());

                    String style = getStyle(newTask);
                    cell.setStyle(style);
                } else {
                    cell.setText(null);
                    cell.setStyle("");
                }
            });

            return cell;
        });

        loadWeekPlans();
    }

    private static String getStyle(Task newTask) {
        StringBuilder style = new StringBuilder();

        if (newTask.isUrgent()) {
            style.append("-fx-underline: true;");
        }

        if (newTask.isObsolete()) {
            style.append("-fx-text-fill: red;");
        } else if (newTask.isOptional()) {
            style.append("-fx-text-fill: grey;");
        } else {
            style.append("-fx-text-fill: black;");
        }

        return style.toString();
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

            List<Task> tasks = model.getTasks();

            TreeItem<Task> root = new TreeItem<>(new Task("Root", tasks));
            root.setExpanded(true);

            for (Task task : tasks) {
                root.getChildren().add(createTreeItem(task));
            }

            view.taskTreeView.setRoot(root);
            view.taskTreeView.setShowRoot(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNewTask(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();

        TreeItem<Task> parent;
        if (selected == null) {
            parent = view.taskTreeView.getRoot();
        } else {
            parent = selected;
        }

        Task newTask = new Task("Neue Aufgabe");
        TreeItem<Task> newItem = createTreeItem(newTask);

        parent.getValue().getChildren().add(newTask);   // Model ergänzen
        parent.getChildren().add(newItem);              // TreeView ergänzen
        parent.setExpanded(true);

        view.taskTreeView.getSelectionModel().select(newItem);

        save();
    }

    public void onDeleteTask(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();

        if (selected == null || selected.getParent() == null) {
            // Kein Eintrag oder Root ausgewählt
            return;
        }

        TreeItem<Task> parentItem = selected.getParent();
        Task parentTask = parentItem.getValue();
        Task selectedTask = selected.getValue();

        // Entferne aus Model
        parentTask.getChildren().remove(selectedTask);

        // Entferne aus TreeView
        parentItem.getChildren().remove(selected);

        // Auswahl zurück auf Parent setzen
        view.taskTreeView.getSelectionModel().select(parentItem);

        save();
    }

    public void onIndentTask(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TreeItem<Task> parent = selected.getParent();
        if (parent == null) return;

        int index = parent.getChildren().indexOf(selected);
        if (index <= 0) return; // Kein vorheriges Geschwister

        TreeItem<Task> prevSibling = parent.getChildren().get(index - 1);
        Task prevTask = prevSibling.getValue();
        Task currentTask = selected.getValue();

        // Aus der aktuellen Ebene entfernen
        parent.getChildren().remove(selected);
        parent.getValue().getChildren().remove(currentTask);

        // In die Children des vorherigen Knotens verschieben
        prevSibling.getChildren().add(selected);
        prevTask.getChildren().add(currentTask);
        prevSibling.setExpanded(true);

        view.taskTreeView.getSelectionModel().select(selected);

        save();
    }

    public void onOutdentTask(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TreeItem<Task> parent = selected.getParent();
        if (parent == null || parent.getParent() == null) return;

        TreeItem<Task> grandParent = parent.getParent();
        Task currentTask = selected.getValue();

        // Index von parent in seiner Ebene bestimmen
        int index = grandParent.getChildren().indexOf(parent);

        // Entfernen aus alter Struktur
        parent.getChildren().remove(selected);
        parent.getValue().getChildren().remove(currentTask);

        // Einfügen direkt hinter den parent
        grandParent.getChildren().add(index + 1, selected);
        grandParent.getValue().getChildren().add(index + 1, currentTask);

        view.taskTreeView.getSelectionModel().select(selected);

        save();
    }

    public void onToggleImportant(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Task task = selected.getValue();
            task.setImportant(!task.isImportant());
            refreshTreeItem(selected);
            save();
        }
    }

    public void onToggleUrgent(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Task task = selected.getValue();
            task.setUrgent(!task.isUrgent());
            refreshTreeItem(selected);
            save();
        }
    }

    public void onToggleDone(ActionEvent event) {
        System.out.println("toggling done flag");
    }

    public void onTaskRename(TreeView.EditEvent<Task> event) {
        Task task = event.getTreeItem().getValue();
        if (task != null) {
            task.setText(event.getNewValue().getText());
            refreshTreeItem(event.getTreeItem());
            save();
        }
    }

    public void onToggleOptional(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Task task = selected.getValue();
            task.setOptional(!task.isOptional());
            refreshTreeItem(selected);
            save();
        }
    }

    public void onToggleObsolete(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Task task = selected.getValue();
            task.setObsolete(!task.isObsolete());
            task.setDone(false); // Wenn obsolet, nicht mehr als "done" markieren
            refreshTreeItem(selected);
            save();
        }
    }

    private CheckBoxTreeItem<Task> createTreeItem(Task task) {
        CheckBoxTreeItem<Task> item = new CheckBoxTreeItem<>(task);
        item.setSelected(task.isDone());

        // Setze initial den Expand-Status entsprechend dem Model
        item.setExpanded(task.isOpen());

        item.selectedProperty().addListener((obs, oldVal, newVal) -> {
            task.setDone(newVal);
            save();
        });

        // Wenn der Nutzer ein- oder ausklappt, übertrage das ins Model
        item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
            task.setOpen(isNowExpanded);
            save();
        });

        // Rekursiv Kind-Knoten anlegen
        for (Task child : task.getChildren()) {
            item.getChildren().add(createTreeItem(child));
        }

        return item;
    }

    private void refreshTreeItem(TreeItem<Task> item) {
        // Workaround zum Aktualisieren der Darstellung (da TreeCell nicht automatisch reagiert)
        Task t = item.getValue();
        item.setValue(null);
        item.setValue(t);
    }

    private void save() {
        try {
            model.saveWeekPlan();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
