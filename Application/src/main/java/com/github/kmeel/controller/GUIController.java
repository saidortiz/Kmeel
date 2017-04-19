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

package com.github.kmeel.controller;

import com.github.kmeel.api.model.Version;
import com.github.kmeel.api.spi.GlobalTab;
import com.github.kmeel.model.FileParser;
import com.github.kmeel.view.HomeTab;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Marten4n6
 *         This class controls the UI
 */
@Slf4j
public class GUIController extends Application {

    private TabPane tabPane;
    private HomeTab homeTab;

    @Override
    public void start(Stage stage) throws Exception {
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 900, 600);
        tabPane = new TabPane();

        homeTab = new HomeTab();
        new HomeController(homeTab);

        tabPane.getTabs().add(homeTab.getTab());
        borderPane.setCenter(tabPane);

        stage.setTitle("Kmeel - " + Version.getVersion());
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        // Listeners
        handleListeners();
        stage.setOnCloseRequest((WindowEvent event) -> {
            log.info("Shutting down, goodbye.");

            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Handle handlers of the home tab and ParseController
     */
    private void handleListeners() {
        homeTab.setOnNewCase((event) -> {
            Platform.runLater(() -> tabPane.getTabs().removeIf(tab -> !tab.getText().equals("Home")));
        });
        homeTab.setOnOpenCase((event) -> {
            Platform.runLater(() -> tabPane.getTabs().removeIf(tab -> !tab.getText().equals("Home")));
        });
        homeTab.setOnRemoveCase((event) -> {
            Platform.runLater(() -> tabPane.getTabs().removeIf(tab -> !tab.getText().equals("Home")));
        });
        FileParser.getInstance().setFinishedListener((kmeelAPI, loadingView) -> {
            loadingView.close();
            kmeelAPI.indexer().close();

            Platform.runLater(() -> {
                kmeelAPI.plugins().getPluginManager().getExtensions(GlobalTab.class).forEach(globalTab -> {
                    Tab tab = globalTab.getGlobalTab(kmeelAPI);

                    switch (tab.getText()) {
                        case "Bookmarks":
                            tabPane.getTabs().add(getIndexAfterTab("Search", tabPane), tab);
                            break;
                        case "Tree":
                            tabPane.getTabs().add(getIndexAfterTab("Home", tabPane), tab);
                            break;
                        default:
                            tabPane.getTabs().add(tab);
                            break;
                    }
                });

                tabPane.getSelectionModel().select(1);
            });
        });
    }

    private int getIndexAfterTab(String tabName, TabPane tabPane) {
        int tabIndex = 1;

        for (Tab tabLoop : tabPane.getTabs()) {
            if (tabLoop.getText().equals(tabName)) {
                break;
            } else {
                tabIndex++;
            }
        }
        return tabIndex;
    }
}
