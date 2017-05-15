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

import com.github.kmeel.api.model.Cases;
import com.github.kmeel.api.model.objects.Case;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Marten4n6
 *         View for the home tab
 */
public class HomeTab extends Tab{

    private ArrayList<ActionListener> onNewCase = new ArrayList<>();
    private ArrayList<ActionListener> onOpenCase = new ArrayList<>();
    private ArrayList<ActionListener> onRemoveCase = new ArrayList<>();

    private @Getter TableView<Case> table;

    public HomeTab() {
        setupTab();
    }

    private void setupTab() {
        BorderPane borderPane = new BorderPane();
        VBox vBox = new VBox();

        // Tab
        ImageView homeIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/images/home.png")));

        this.setClosable(false);
        this.setText("Home");
        this.setGraphic(homeIcon);

        // Layout
        borderPane.setPadding(new Insets(5, 5, 5, 5));

        vBox.setSpacing(10);
        vBox.setPadding(new Insets(0, 0, 0, 5));

        // Case Table
        table = new TableView<>();

        TableColumn<Case, String> columnName = new TableColumn<>("Name");
        TableColumn<Case, String> columnDescription = new TableColumn<>("Description");
        TableColumn<Case, String> columnInvestigator = new TableColumn<>("Investigator");
        TableColumn<Case, String> columnSource = new TableColumn<>("Source");
        TableColumn<Case, String> columnSize = new TableColumn<>("Size");

        columnName.setCellValueFactory(new PropertyValueFactory<>("name"));
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        columnInvestigator.setCellValueFactory(new PropertyValueFactory<>("investigator"));
        columnSource.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getSources().toString().replace("[", "").replace("]", "")));
        columnSize.setCellValueFactory(new PropertyValueFactory<>("size"));

        table.setPlaceholder(new Label("No cases added."));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().addAll(columnName, columnDescription, columnInvestigator, columnSource, columnSize);

        // Buttons
        Button newCase = new Button();
        Button openCase = new Button();
        Button removeCase = new Button();

        newCase.setText("New Case");
        openCase.setText("Open Case");
        removeCase.setText("Remove Case");

        newCase.setMaxWidth(Double.MAX_VALUE);
        newCase.setMinWidth(185);
        openCase.setMaxWidth(Double.MAX_VALUE);
        openCase.setMinWidth(185);
        removeCase.setMaxWidth(Double.MAX_VALUE);
        removeCase.setMinWidth(185);

        // Add
        borderPane.setCenter(table);

        vBox.getChildren().add(newCase);
        vBox.getChildren().add(openCase);
        vBox.getChildren().add(removeCase);
        borderPane.setRight(vBox);

        this.setContent(borderPane);

        // Add existing cases
        Cases.getNames().forEach(caseName -> {
            Platform.runLater(() -> table.getItems().add(Cases.get(caseName)));
        });

        // Listeners
        newCase.setOnAction((event) -> {
            onNewCase.forEach(listener -> listener.actionPerformed(null));
        });
        openCase.setOnAction((event) -> {
            if (!onOpenCase.isEmpty()) {
                Optional<ButtonType> confirmOpen = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to open this case?", ButtonType.YES, ButtonType.NO).showAndWait();
                if (confirmOpen.get() == ButtonType.NO) return;

                openCase.setDisable(true);
                onOpenCase.forEach(listener -> listener.actionPerformed(null));
            }
        });
        removeCase.setOnAction((event) -> {
            Optional<ButtonType> confirmOpen = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to remove this case?", ButtonType.YES, ButtonType.NO).showAndWait();
            if (confirmOpen.get() == ButtonType.NO) return;

            onRemoveCase.forEach(listener -> listener.actionPerformed(null));
        });
        table.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Case> observableValue, Case oldValue, Case newValue) -> {
            // Disable open case button if selected row is already open
            if (newValue != null && Cases.getCurrentCase() != null && Cases.getCurrentCase().getName().equals(newValue.getName())) {
                openCase.setDisable(true);
            } else {
                openCase.setDisable(false);
            }
        });
    }

    public void setOnNewCase(ActionListener listener) {
        onNewCase.add(listener);
    }

    public void setOnOpenCase(ActionListener listener) {
        onOpenCase.add(listener);
    }

    public void setOnRemoveCase(ActionListener listener) {
        onRemoveCase.add(listener);
    }
}
