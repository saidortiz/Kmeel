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

import com.github.kmeel.api.model.Version;
import com.github.kmeel.api.spi.GlobalTab;
import com.github.kmeel.controller.HomeController;
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
 *         Tab stage which is always shown.
 */
@Slf4j
public class TabStage extends Stage {

    private TabPane tabPane;

    private HomeTab homeTab;
    private HomeController homeController;

    public TabStage() {
        setupStage();
        setupListeners();
    }

    private void setupStage() {
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 900, 600);
        tabPane = new TabPane();

        homeTab = new HomeTab();
        homeController = new HomeController(homeTab);

        tabPane.getTabs().add(homeTab);
        borderPane.setCenter(tabPane);

        this.setTitle("Kmeel - " + Version.getVersion());
        this.setScene(scene);
        this.centerOnScreen();
        this.show();
    }

    /**
     * Handle handlers of the home tab and ParseController
     */
    private void setupListeners() {
        this.setOnCloseRequest((WindowEvent event) -> {
            log.info("Shutting down, goodbye.");

            Platform.exit();
            System.exit(0);
        });

        homeTab.setOnNewCase((event) -> {
            Platform.runLater(() -> tabPane.getTabs().removeIf(tab -> !tab.getText().equals("Home")));
        });
        homeTab.setOnOpenCase((event) -> {
            Platform.runLater(() -> tabPane.getTabs().removeIf(tab -> !tab.getText().equals("Home")));
        });
        homeTab.setOnRemoveCase((event) -> {
            Platform.runLater(() -> tabPane.getTabs().removeIf(tab -> !tab.getText().equals("Home")));
        });
        homeController.setOnFinishedParsing((kmeelAPI, loadingView) -> {
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
