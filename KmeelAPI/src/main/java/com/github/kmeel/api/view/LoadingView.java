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

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.github.kmeel.api.view.taskprogress.TaskProgressView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marten4n6
 */
public class LoadingView {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Stage stage;
    private TaskProgressView taskProgressView;

    public void show() {
        stage = new Stage();
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 500, 364);

        // Stage
        stage.setTitle("Kmeel");

        // Scene
        Label labelInformation = new Label("Waiting for parsers to finish...");
        taskProgressView = new TaskProgressView();

        labelInformation.setPadding(new Insets(0, 0, 5, 0));
        borderPane.setPadding(new Insets(5, 5, 5, 5));

        // Add
        borderPane.setTop(labelInformation);
        borderPane.setCenter(taskProgressView);

        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Starts the task and adds it to the view
     */
    public void addTask(Task task) {
        taskProgressView.getTasks().add(task);
        executorService.submit(task);
    }

    /**
     * Stops all tasks and closes the loading view
     */
    public void close() {
        executorService.shutdown();
        Platform.runLater(() -> stage.close());
    }
}
