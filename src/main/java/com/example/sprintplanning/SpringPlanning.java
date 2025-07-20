package com.example.sprintplanning;

import com.example.sprintplanning.model.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;

public class SpringPlanning extends Application {

    // UI-Controls
    ListView<String> weekListView;
    TreeView<Task> taskTreeView;
    Button btnNew, btnDelete, btnIndent, btnOutdent, btnImportant, btnUrgent, btnDone;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Instantiate Controls
        weekListView   = new ListView<>();
        taskTreeView   = new TreeView<>();
        btnNew         = new Button("Neu");
        btnDelete      = new Button("Löschen");
        btnIndent      = new Button("Einrücken");
        btnOutdent     = new Button("Ausrücken");
        btnImportant   = new Button("⭐");
        btnUrgent      = new Button("🔻");
        btnDone        = new Button("✔");

        // 2. Configure Controls
        weekListView.setPrefWidth(150);
        taskTreeView.setShowRoot(false);
        taskTreeView.setEditable(true);

        // TODO: setCellFactory auf TreeView, um Inline-Editing etc. zu ermöglichen

        // 3. Layout: Toolbar
        HBox toolbar = new HBox(5, btnNew, btnDelete, btnIndent, btnOutdent, btnImportant, btnUrgent, btnDone);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-background-color: #EEE;");

        // 4. Layout: Center with toolbar + TreeView
        VBox centerBox = new VBox(5, toolbar, taskTreeView);
        centerBox.setPadding(new Insets(10));

        // 5. Root Layout: BorderPane
        BorderPane root = new BorderPane();
        root.setLeft(weekListView);
        root.setCenter(centerBox);

        BorderPane.setMargin(weekListView, new Insets(10));
        BorderPane.setMargin(centerBox, new Insets(10));

        // 6. Scene & Stage
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        SpringPlanningModel model = new SpringPlanningModel();
        new SpringPlanningController(this, model);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Sprint Planning");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
