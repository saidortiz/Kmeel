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
import com.github.kmeel.api.model.Cases;
import com.github.kmeel.api.model.Plugins;
import com.github.kmeel.api.model.objects.Case;
import com.google.gson.Gson;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import ro.fortsoft.pf4j.PluginDescriptor;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Marten4n6
 *         View for creating new cases
 */
@Slf4j
public class NewCaseStage extends Stage {

    private @Getter KmeelAPI kmeelAPI;
    private @Getter Case caseObject;

    private CheckBox checkBoxOpen;
    private ArrayList<String> disabledPlugins = new ArrayList<>();

    private @Setter ActionListener onCreateCase;

    public NewCaseStage() {
        setupStage();
    }

    private void setupStage() {
        kmeelAPI = new KmeelAPI(null);

        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 525, 295);
        TabPane tabPane = new TabPane(getCaseTab(), getPluginsTab());

        borderPane.setCenter(tabPane);
        this.setScene(scene);
        this.centerOnScreen();
        this.show();
    }

    private Tab getCaseTab() {
        Tab tab = new Tab();
        GridPane gridPane = new GridPane();

        ArrayList<String> sources = new ArrayList<>();

        // Tab
        tab.setContent(gridPane);
        tab.setText("Case");
        tab.setClosable(false);

        // Labels
        Label labelName = new Label("Name:");
        Label labelDescription = new Label("Description:");
        Label labelInvestigator = new Label("Investigator:");
        Label labelSource = new Label("Source:");

        // Fields
        TextField fieldCaseName = new TextField();
        TextField fieldDescription = new TextField();
        TextField fieldInvestigator = new TextField();
        TextField fieldSource = new TextField();

        fieldInvestigator.setText(System.getProperty("user.name"));
        fieldSource.setEditable(false);

        // Combo Boxes
        ComboBox<String> sourceType = new ComboBox<>(FXCollections.observableArrayList("File(s)", "Folder"));

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

        checkBoxOpen = new CheckBox();
        CheckBox checkBoxHasSubFolders = new CheckBox();

        checkBoxOpen.setText("Open");
        checkBoxOpen.setSelected(true);

        checkBoxHasSubFolders.setText("Subfolders");
        checkBoxHasSubFolders.setSelected(true);

        checkBoxHasSubFolders.setVisible(false);
        checkBoxHasSubFolders.setManaged(false);

        hBox.setSpacing(10);
        hBox.getChildren().addAll(checkBoxOpen, checkBoxHasSubFolders);

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
        gridPane.add(fieldCaseName, 1, 0);
        gridPane.add(labelDescription, 0, 1);
        gridPane.add(fieldDescription, 1, 1);
        gridPane.add(labelInvestigator, 0, 2);
        gridPane.add(fieldInvestigator, 1, 2);
        gridPane.add(labelSource, 0, 3);
        gridPane.add(fieldSource, 1, 3);
        gridPane.add(browseSource, 2, 3);
        gridPane.add(sourceType, 3, 3);
        gridPane.add(createCase, 1, 5);
        gridPane.add(hBox, 1, 6);

        // Listeners
        browseSource.setOnAction((event) -> {
            if (sourceType.getSelectionModel().getSelectedItem().startsWith("File")) {
                FileChooser fileChooser = new FileChooser();
                List<String> supportedExtensions = new ArrayList<>();

                kmeelAPI.messages().getSupportedFileExtensions().forEach(extension -> supportedExtensions.add("*." + extension));

                fileChooser.setTitle("Kmeel");
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
                checkBoxHasSubFolders.setVisible(false);
                checkBoxHasSubFolders.setManaged(false);
            } else {
                checkBoxHasSubFolders.setVisible(true);
                checkBoxHasSubFolders.setManaged(true);
            }
        });
        createCase.setOnAction((event) -> {
            if (Cases.getNames().contains(fieldCaseName.getText())) {
                new Alert(Alert.AlertType.ERROR, "A case with this name already exists.", ButtonType.CLOSE).showAndWait();
            } else if (fieldCaseName.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Invalid case name.", ButtonType.CLOSE).showAndWait();
            } else if (fieldInvestigator.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Invalid investigator name.", ButtonType.CLOSE).showAndWait();
            } else if (sources.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Invalid source.", ButtonType.CLOSE).showAndWait();
            } else {
                caseObject = new Case(
                        fieldCaseName.getText(),
                        fieldDescription.getText(),
                        fieldInvestigator.getText(),
                        humanReadableByteCount(getSourceSize(kmeelAPI, sources, checkBoxHasSubFolders.isSelected())),
                        sources,
                        disabledPlugins
                );
                kmeelAPI.setCase(caseObject);

                if (new File(sources.get(0)).isDirectory()) {
                    caseObject.setHasSubFolders(checkBoxHasSubFolders.isSelected());
                }

                Cases.storeCaseObject(caseObject);

                // Default settings
                kmeelAPI.settings().set("SearchLimit", "0");
                kmeelAPI.settings().set("DateFormat", "EEE, d MMM yyyy HH:mm:ss");

                onCreateCase.actionPerformed(null);
            }
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

        HashMap<String, SimpleBooleanProperty> enabledPlugins = new HashMap<>(); // pluginId as key

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

    public boolean isOpenCaseSelected() {
        return checkBoxOpen.isSelected();
    }

    private long getSourceSize(KmeelAPI kmeelAPI, List<String> sources, boolean extractSubFolders) {
        final AtomicLong sourceSize = new AtomicLong(0);

        if (new File(sources.get(0)).isDirectory()) {
            try {
                Files.walkFileTree(Paths.get(sources.get(0)), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        if (kmeelAPI.messages().getSupportedFileExtensions().contains(FilenameUtils.getExtension(path.toString()).toLowerCase())) {
                            sourceSize.addAndGet(path.toFile().length());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                        if (!extractSubFolders && !path.toString().equals(sources.get(0))) {
                            return FileVisitResult.SKIP_SUBTREE;
                        } else {
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException ex) {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        } else {
            for (String source : sources) {
                sourceSize.addAndGet(new File(source).length());
            }
        }
        return sourceSize.get();
    }

    /**
     * @return A human readable byte size
     */
    private String humanReadableByteCount(long bytes) {
        boolean si = false;
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}