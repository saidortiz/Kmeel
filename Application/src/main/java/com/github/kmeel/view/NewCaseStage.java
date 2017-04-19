/*
 * This file is part of Kmeel.
 * Copyright (C) 2017  Marten4n6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.kmeel.view;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.model.Plugins;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import ro.fortsoft.pf4j.PluginDescriptor;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Marten4n6
 *         View for creating new cases
 */
public class NewCaseStage extends Stage {

    private @Getter KmeelAPI kmeelAPI;

    private TextField caseName;
    private TextField description;
    private TextField investigator;
    private ComboBox<String> hashType;
    private CheckBox hasSubFolders;
    private CheckBox open;

    private @Getter List<String> sources = new ArrayList<>();
    private @Getter List<String> disabledPlugins = new ArrayList<>();

    private @Setter ActionListener onCreateCase;

    public NewCaseStage() {
        setupStage();
    }

    private void setupStage() {
        kmeelAPI = new KmeelAPI(null);

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 525, 295);

        // Tab pane
        TabPane tabPane = new TabPane();

        tabPane.getTabs().addAll(getCaseTab(), getPluginsTab());

        // Add
        borderPane.setCenter(tabPane);
        this.setScene(scene);

        this.centerOnScreen();
        this.show();
    }

    private Tab getCaseTab() {
        Tab tab = new Tab();
        GridPane gridPane = new GridPane();

        // Tab
        tab.setContent(gridPane);
        tab.setText("Case");
        tab.setClosable(false);

        // Labels
        Label labelName = new Label("Name:");
        Label labelDescription = new Label("Description:");
        Label labelInvestigator = new Label("Investigator:");
        Label labelSource = new Label("Source:");
        Label labelHash = new Label("Hash:");

        // Fields
        caseName = new TextField();
        description = new TextField();
        investigator = new TextField();
        TextField fieldSource = new TextField();

        investigator.setText(System.getProperty("user.name"));
        fieldSource.setEditable(false);

        // Combo Boxes
        hashType = new ComboBox<>(FXCollections.observableArrayList("SHA-256", "MD5", "SHA1"));
        ComboBox<String> sourceType = new ComboBox<>(FXCollections.observableArrayList("File(s)", "Folder"));

        hashType.setValue("SHA-256");
        sourceType.setValue("File(s)");

        // Buttons
        Button browseSource = new Button();
        Button createCase = new Button();

        browseSource.setText(" ... ");
        createCase.setText("Create Case");

        createCase.setMaxWidth(Integer.MAX_VALUE);
        createCase.setMinHeight(40);

        // Check Boxes
        HBox hBox = new HBox();

        open = new CheckBox();
        hasSubFolders = new CheckBox();

        open.setText("Open");
        open.setSelected(true);

        hasSubFolders.setText("Subfolders");
        hasSubFolders.setSelected(true);

        hasSubFolders.setVisible(false);
        hasSubFolders.setManaged(false);

        hBox.setSpacing(10);
        hBox.getChildren().addAll(open, hasSubFolders);

        // Layout
        gridPane.setVgap(5);
        gridPane.setHgap(10);
        gridPane.setPadding(new Insets(5, 5, 5, 5));

        ColumnConstraints columnOneConstraints = new ColumnConstraints();
        ColumnConstraints columnTwoConstraints = new ColumnConstraints();

        columnOneConstraints.setMinWidth(85);
        columnTwoConstraints.setMinWidth(250);

        gridPane.getColumnConstraints().addAll(columnOneConstraints, columnTwoConstraints);

        // Add
        gridPane.add(labelName, 0, 0);
        gridPane.add(caseName, 1, 0);
        gridPane.add(labelDescription, 0, 1);
        gridPane.add(description, 1, 1);
        gridPane.add(labelInvestigator, 0, 2);
        gridPane.add(investigator, 1, 2);
        gridPane.add(labelSource, 0, 3);
        gridPane.add(fieldSource, 1, 3);
        gridPane.add(browseSource, 2, 3);
        gridPane.add(sourceType, 3, 3);
        gridPane.add(labelHash, 0, 4);
        gridPane.add(hashType, 1, 4);
        gridPane.add(createCase, 1, 5);
        gridPane.add(hBox, 1, 6);

        // Listeners
        browseSource.setOnAction((event) -> {
            if (sourceType.getSelectionModel().getSelectedItem().startsWith("File")) {
                FileChooser fileChooser = new FileChooser();
                List<String> supportedExtensions = new ArrayList<>();

                fileChooser.setTitle("Kmeel");

                kmeelAPI.messages().getSupportedFileExtensions().forEach(extension -> supportedExtensions.add("*." + extension));
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Supported", supportedExtensions));

                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

                if (selectedFiles != null) {
                    sources.clear();
                    selectedFiles.forEach(file -> sources.add(file.getPath()));

                    if (selectedFiles.size() > 1) {
                        fieldSource.setText(selectedFiles.size() + " selected files.");
                    } else {
                        fieldSource.setText(selectedFiles.get(0).getPath());
                    }
                }
            } else {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Kmeel");

                File selectedDirectory = directoryChooser.showDialog(null);

                if (selectedDirectory != null) {
                    sources.clear();
                    sources.add(selectedDirectory.getPath());

                    fieldSource.setText(selectedDirectory.getPath());
                }
            }
        });
        sourceType.setOnAction((event) -> {
            if (sourceType.getSelectionModel().getSelectedItem().startsWith("File")) {
                hasSubFolders.setVisible(false);
                hasSubFolders.setManaged(false);
            } else {
                hasSubFolders.setVisible(true);
                hasSubFolders.setManaged(true);
            }
        });
        createCase.setOnAction((event) -> {
            if (onCreateCase != null) onCreateCase.actionPerformed(null);
        });
        return tab;
    }

    private Tab getPluginsTab() {
        Tab tab = new Tab();
        BorderPane borderPane = new BorderPane();

        // Tab
        tab.setContent(borderPane);
        tab.setText("Plugins");
        tab.setClosable(false);

        // Table
        TableView<PluginDescriptor> table = new TableView<>();

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setEditable(true);

        TableColumn<PluginDescriptor, String> columnName = new TableColumn<>("Name");
        TableColumn<PluginDescriptor, String> columnVersion = new TableColumn<>("Version");
        TableColumn<PluginDescriptor, String> columnAuthor = new TableColumn<>("Author");
        TableColumn<PluginDescriptor, Boolean> columnEnabled = new TableColumn<>("Enabled");

        HashMap<String, SimpleBooleanProperty> enabledPlugins = new HashMap<>(); //pluginId as key

        kmeelAPI.plugins().getPluginManager().getStartedPlugins().forEach(plugin -> {
            SimpleBooleanProperty booleanProperty = new SimpleBooleanProperty(true);

            booleanProperty.addListener((event) -> {
                if (disabledPlugins.contains(plugin.getPluginId())) {
                    disabledPlugins.remove(plugin.getPluginId());
                } else {
                    disabledPlugins.add(plugin.getPluginId());
                }
            });
            enabledPlugins.put(plugin.getDescriptor().getPluginId(), booleanProperty);
        });

        columnName.setCellValueFactory(new PropertyValueFactory<>("pluginId"));
        columnVersion.setCellValueFactory(new PropertyValueFactory<>("version"));
        columnAuthor.setCellValueFactory(new PropertyValueFactory<>("provider"));
        columnEnabled.setCellValueFactory((event) -> enabledPlugins.get(event.getValue().getPluginId()));
        columnEnabled.setCellFactory(CheckBoxTableCell.forTableColumn(columnEnabled));

        table.getColumns().addAll(columnName, columnVersion, columnAuthor, columnEnabled);
        kmeelAPI.plugins().getPluginManager().getStartedPlugins().forEach(plugin -> {
            table.getItems().add(plugin.getDescriptor());
        });

        Platform.runLater(() -> {
            for (String pluginName : Plugins.getPluginsDisabledByDefault()) {
                enabledPlugins.get(pluginName).set(false);
            }
        });

        // Add
        borderPane.setCenter(table);

        return tab;
    }

    public String getName() {
        return caseName.getText();
    }

    public String getDescription() {
        return description.getText();
    }

    public String getInvestigator() {
        return investigator.getText();
    }

    public String getHashType() {
        return hashType.getSelectionModel().getSelectedItem();
    }

    public boolean isSubFoldersSelected() {
        return hasSubFolders.isSelected();
    }

    public boolean isOpenCaseSelected() {
        return open.isSelected();
    }
}