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

package com.github.kmeel.api.view;

import com.github.kmeel.api.KmeelAPI;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import lombok.Getter;
import com.github.kmeel.api.model.Cases;

import java.awt.event.ActionListener;

/**
 * @author Marten4n6
 *         Shows the view for adding tags
 */
public class TagStage extends Stage {

    private ActionListener onAddTag;

    private KmeelAPI kmeelAPI;
    private @Getter String tagName;

    private ComboBox<String> comboBox;
    private TextField textField;
    private Button button;

    public TagStage(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;

        setupStage();
        setupListeners();
    }

    private void setupStage() {
        GridPane gridPane = new GridPane();
        Scene scene = new Scene(gridPane, 370, 100);

        // Stage
        this.setTitle("Kmeel");
        this.setResizable(false);

        // Grid Pane
        gridPane.setPadding(new Insets(5, 5, 5, 5));
        gridPane.setHgap(5);

        // Combo Box
        comboBox = new ComboBox<>();

        comboBox.setMinWidth(50);

        if (!kmeelAPI.tags().getExistingTags().isEmpty()) {
            kmeelAPI.tags().getExistingTags().forEach(tagName -> {
                comboBox.getItems().add(tagName);
            });
        } else {
            comboBox.setVisible(false);
            comboBox.setManaged(false);
        }

        // Text Field
        textField = new TextField();

        GridPane.setHgrow(textField, Priority.ALWAYS);

        // Button
        button = new Button("Add Tag");

        // Add
        gridPane.add(comboBox, 0, 0);
        gridPane.add(textField, 1, 0);
        gridPane.add(button, 2, 0);

        this.setScene(scene);
        this.centerOnScreen();
        this.show();
    }

    private void setupListeners() {
        comboBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> arg0, String oldValue, String newValue) -> {
            textField.setText(newValue);
        });
        button.setOnAction((event) -> {
            tagName = textField.getText();
            if (onAddTag != null) onAddTag.actionPerformed(null);
        });
    }

    public void setOnAddTag(ActionListener listener) {
        onAddTag = listener;
    }
}
