package com.example.sprintplanning;

import com.example.sprintplanning.model.*;
import de.jensd.fx.glyphs.fontawesome.*;
import jakarta.xml.bind.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;

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
        view.btnMoveUp.setOnAction(this::onMoveUp);
        view.btnMoveDown.setOnAction(this::onMoveDown);
        view.btnImportant.setOnAction(this::onToggleImportant);
        view.btnUrgent.setOnAction(this::onToggleUrgent);
        view.btnDone.setOnAction(this::onToggleDone);
        view.btnOptional.setOnAction(this::onToggleOptional);
        view.btnObsolete.setOnAction(this::onToggleObsolete);



        view.taskTreeView.setOnEditCommit(this::onTaskRename);
        view.taskTreeView.setEditable(true);
        view.taskTreeView.setCellFactory(tv -> {
            TextFieldTreeCell<Task> cell = new TextFieldTreeCell<>(new TaskStringConverter());

            cell.itemProperty().addListener((obs, oldTask, newTask) -> {
                if (newTask != null) {
                    cell.setStyle(getStyle(newTask));
                } else {
                    cell.setStyle("");
                }
            });

            return cell;
        });

        view.taskTreeView.setOnKeyPressed(event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String fullPathText = buildFullTaskText(selected);
                    Clipboard clipboard = Clipboard.getSystemClipboard();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(fullPathText);
                    clipboard.setContent(content);
                    event.consume(); // Verhindert Standardverhalten
                }
            }
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
        } else if (newTask.isImportant()) {
            style.append("-fx-text-fill: blue;");
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

        // Bestimme neuen Auswahlknoten
        int index = parentItem.getChildren().indexOf(selected);
        int totalSiblings = parentItem.getChildren().size();

        // Entferne aus Model
        parentTask.getChildren().remove(selectedTask);

        // Entferne aus TreeView
        parentItem.getChildren().remove(selected);

        // Auswahl auf den nächsten oder vorherigen Eintrag oder Parent setzen
        TreeItem<Task> newSelected = null;
        if (index < totalSiblings - 1) {
            newSelected = parentItem.getChildren().get(index);
        } else if (index > 0) {
            newSelected = parentItem.getChildren().get(index - 1);
        } else {
            newSelected = parentItem;
        }

        view.taskTreeView.getSelectionModel().select(newSelected);

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

        // Schritt 1: Kinder des selektierten Knotens sichern
        List<TreeItem<Task>> childrenToDetach = new ArrayList<>(selected.getChildren());
        List<Task> childrenModel = new ArrayList<>(selected.getValue().getChildren());

        // Schritt 2: Entferne Kinder aus dem selektierten Knoten
        selected.getChildren().clear();
        selected.getValue().getChildren().clear();

        // Schritt 3: Entferne selektierten Knoten aus Parent
        parent.getChildren().remove(selected);
        parent.getValue().getChildren().remove(selected.getValue());

        // Schritt 4: Hänge selektierten Knoten als Kind an vorheriges Geschwister
        prevSibling.getChildren().add(selected);
        prevTask.getChildren().add(selected.getValue());
        prevSibling.setExpanded(true);

        // Schritt 5: Hänge die ehemaligen Kinder des selektierten Knotens nun **nach** dem selektierten Knoten beim neuen Parent ein
        int insertIndex = prevTask.getChildren().indexOf(selected.getValue()) + 1;
        prevTask.getChildren().addAll(insertIndex, childrenModel);
        prevSibling.getChildren().addAll(insertIndex, childrenToDetach);

        // Fokus zurück auf verschobenen Knoten
        view.taskTreeView.getSelectionModel().select(selected);
        save();
    }

    public void onOutdentTask(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TreeItem<Task> parent = selected.getParent();
        if (parent == null || parent.getParent() == null) return;

        TreeItem<Task> grandParent = parent.getParent();
        Task selectedTask = selected.getValue();
        Task parentTask = parent.getValue();

        int index = parent.getChildren().indexOf(selected);

        // 1. Sammle alle nachfolgenden Geschwister (die unter dem verschobenen Knoten stehen)
        List<TreeItem<Task>> trailingSiblings = new ArrayList<>();
        List<Task> trailingTasks = new ArrayList<>();

        for (int i = index + 1; i < parent.getChildren().size(); i++) {
            TreeItem<Task> item = parent.getChildren().get(i);
            trailingSiblings.add(item);
            trailingTasks.add(item.getValue());
        }

        // 2. Entferne die nachfolgenden aus der alten Ebene
        parent.getChildren().removeAll(trailingSiblings);
        parentTask.getChildren().removeAll(trailingTasks);

        // 3. Entferne den selektierten Knoten aus der alten Ebene
        parent.getChildren().remove(selected);
        parentTask.getChildren().remove(selectedTask);

        // 4. Füge den selektierten Knoten beim Grandparent ein (hinter Parent)
        int parentIndex = grandParent.getChildren().indexOf(parent);
        grandParent.getChildren().add(parentIndex + 1, selected);
        grandParent.getValue().getChildren().add(parentIndex + 1, selectedTask);

        // 5. Hänge die "Nachfolger" als Kinder unter den selektierten Knoten
        selected.getChildren().addAll(trailingSiblings);
        selectedTask.getChildren().addAll(trailingTasks);
        selected.setExpanded(true);

        view.taskTreeView.getSelectionModel().select(selected);
        save();
    }

    public void onMoveUp(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getParent() == null) return;

        TreeItem<Task> parent = selected.getParent();
        List<TreeItem<Task>> siblings = parent.getChildren();
        List<Task> siblingTasks = parent.getValue().getChildren();

        int index = siblings.indexOf(selected);
        if (index <= 0) return; // Bereits ganz oben oder nicht gefunden

        // Swap in TreeView
        siblings.remove(index);
        siblings.add(index - 1, selected);

        // Swap im Model
        Task task = selected.getValue();
        siblingTasks.remove(index);
        siblingTasks.add(index - 1, task);

        view.taskTreeView.getSelectionModel().select(selected);
        save();
    }

    public void onMoveDown(ActionEvent event) {
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getParent() == null) return;

        TreeItem<Task> parent = selected.getParent();
        List<TreeItem<Task>> siblings = parent.getChildren();
        List<Task> siblingTasks = parent.getValue().getChildren();

        int index = siblings.indexOf(selected);
        if (index < 0 || index >= siblings.size() - 1) return; // Bereits ganz unten oder nicht gefunden

        // Swap in TreeView
        siblings.remove(index);
        siblings.add(index + 1, selected);

        // Swap im Model
        Task task = selected.getValue();
        siblingTasks.remove(index);
        siblingTasks.add(index + 1, task);

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
        TreeItem<Task> selected = view.taskTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Task task = selected.getValue();
            task.setDone(!task.isDone());
            refreshTreeItem(selected);
            save();
        }
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

    private TreeItem<Task> createTreeItem(Task task) {
        TreeItem<Task> item = new TreeItem<>(task);

        updateCheckbox(item);

        // Setze initial den Expand-Status entsprechend dem Model
        item.setExpanded(task.isOpen());

/*
        item.selectedProperty().addListener((obs, oldVal, newVal) -> {
            task.setDone(newVal);
            save();
        });
*/

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
        Task task = item.getValue();
        item.setValue(null);
        item.setValue(task);

        updateCheckbox(item);
    }

    private static void updateCheckbox(TreeItem<Task> item) {
        Task task = item.getValue();
        FontAwesomeIconView icon = new FontAwesomeIconView(task.isDone() ? FontAwesomeIcon.CHECK_SQUARE_ALT : FontAwesomeIcon.SQUARE_ALT);
        item.setGraphic(icon);
    }

    private void save() {
        try {
            model.saveWeekPlan();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private String buildFullTaskText(TreeItem<Task> item) {
        List<String> parts = new ArrayList<>();

        TreeItem<Task> current = item;
        while (current != null && current.getParent() != null && current.getParent().getParent() != null) {
            Task task = current.getValue();
            if (task != null) {
                parts.add(task.getText());
            }
            current = current.getParent();
        }

        Collections.reverse(parts);
        return String.join(" - ", parts);
    }

}
