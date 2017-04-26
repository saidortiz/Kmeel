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

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.model.Cases;
import com.github.kmeel.api.model.objects.Case;
import com.github.kmeel.api.spi.Parser;
import com.github.kmeel.api.utils.OSUtils;
import com.github.kmeel.api.view.LoadingView;
import com.github.kmeel.listeners.FinishedListener;
import com.github.kmeel.model.FileParser;
import com.github.kmeel.view.HomeTab;
import com.github.kmeel.view.NewCaseStage;
import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Marten4n6
 *         This class controls the Home tab
 */
@Slf4j
public class HomeController {

    private HomeTab homeTab;

    private @Getter KmeelAPI kmeelAPI;
    private FinishedListener onFinishedParsing;

    public HomeController(HomeTab homeTab) {
        this.homeTab = homeTab;

        handleListeners();
    }

    private void handleListeners() {
        homeTab.setOnNewCase((event) -> {
            NewCaseStage newCaseStage = new NewCaseStage();

            newCaseStage.show();

            newCaseStage.setOnCreateCase((event2) -> {
                kmeelAPI = newCaseStage.getKmeelAPI();

                // Disable plugins
                if (!kmeelAPI.getCaseObject().getDisabledPlugins().isEmpty()) {
                    int disabledPlugins = kmeelAPI.getCaseObject().getDisabledPlugins().size();

                    kmeelAPI.getCaseObject().getDisabledPlugins().forEach(pluginName -> {
                        kmeelAPI.plugins().getPluginManager().unloadPlugin(pluginName);
                    });

                    switch (disabledPlugins) {
                        case 1:
                            log.info("Disabled " + disabledPlugins + " plugin.");
                            break;
                        default:
                            log.info("Disabled " + disabledPlugins + " plugins.");
                            break;
                    }
                }

                Platform.runLater(newCaseStage::close);

                if (newCaseStage.isOpenCaseSelected()) {
                    Cases.setCurrentCase(kmeelAPI.getCaseObject());

                    LoadingView loadingView = new LoadingView();

                    kmeelAPI.plugins().getPluginManager().getExtensions(Parser.class).forEach(parser -> {
                        parser.setup(kmeelAPI, loadingView);
                    });
                    loadingView.show();

                    FileParser fileParser = new FileParser(kmeelAPI, loadingView);

                    fileParser.setFinishedListener((arg1, arg2) -> {
                        onFinishedParsing.finished(arg1, arg2);
                    });

                    if (new File(kmeelAPI.getCaseObject().getSources().get(0)).isDirectory()) {
                        fileParser.parseDirectory(new File(kmeelAPI.getCaseObject().getSources().get(0)), kmeelAPI.getCaseObject().getHasSubFolders());
                    } else {
                        fileParser.parseFiles(kmeelAPI.getCaseObject().getSources());
                    }
                }

                Platform.runLater(() -> homeTab.getTable().getItems().add(kmeelAPI.getCaseObject()));
            });
        });
        homeTab.setOnOpenCase((event) -> {
            try {
                Case caseObject = homeTab.getTable().getSelectionModel().getSelectedItem();

                kmeelAPI = new KmeelAPI(caseObject);

                Cases.setCurrentCase(caseObject);

                // Check if source exists
                if (new File(caseObject.getSources().get(0)).isDirectory() && !new File(caseObject.getSources().get(0)).exists()) {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
                        Label message = new Label("Failed to find source:\n" + caseObject.getSources().get(0));

                        message.setWrapText(true);
                        alert.getDialogPane().setContent(message);
                        alert.showAndWait();
                    });
                    return;
                } else {
                    for (String source : caseObject.getSources()) {
                        if (!new File(source).exists()) {
                            Platform.runLater(() -> {
                                Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
                                Label message = new Label("Failed to find source:\n" + caseObject.getSources().get(0));

                                message.setWrapText(true);
                                alert.getDialogPane().setContent(message);
                                alert.showAndWait();
                            });
                            return;
                        }
                    }
                }

                if (!caseObject.getDisabledPlugins().isEmpty()) {
                    caseObject.getDisabledPlugins().forEach(pluginName -> {
                        kmeelAPI.plugins().getPluginManager().unloadPlugin(pluginName);
                    });

                    if (caseObject.getDisabledPlugins().size() > 1) {
                        log.info("Disabled " + caseObject.getDisabledPlugins().size() + " plugins.");
                    } else {
                        log.info("Disabled " + caseObject.getDisabledPlugins().size() + " plugin.");
                    }
                }

                LoadingView loadingView = new LoadingView();

                kmeelAPI.plugins().getPluginManager().getExtensions(Parser.class).forEach(parser -> {
                    parser.setup(kmeelAPI, loadingView);
                });
                loadingView.show();

                FileParser fileParser = new FileParser(kmeelAPI, loadingView);

                fileParser.setFinishedListener((arg1, arg2) -> {
                    onFinishedParsing.finished(arg1, arg2);
                });

                if (new File(caseObject.getSources().get(0)).isDirectory()) {
                    fileParser.parseDirectory(new File(caseObject.getSources().get(0)), caseObject.getHasSubFolders());
                } else {
                    fileParser.parseFiles(caseObject.getSources());
                }
            } catch (NullPointerException ex) {
                new Alert(Alert.AlertType.ERROR, "No case selected.", ButtonType.CLOSE).showAndWait();
            }
        });
        homeTab.setOnRemoveCase((event) -> {
            try {
                String caseName = homeTab.getTable().getSelectionModel().getSelectedItem().getName();

                if (kmeelAPI != null && kmeelAPI.getCaseObject().getName().equals(caseName)) {
                    kmeelAPI.database().getDataSource().close();
                }

                try {
                    FileUtils.deleteDirectory(new File(OSUtils.getCasePath(caseName)));
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                }

                Platform.runLater(() -> {
                    homeTab.getTable().getItems().remove(homeTab.getTable().getSelectionModel().getSelectedItem());
                });

                log.info("Case \"" + caseName + "\" removed.");
            } catch (NullPointerException ex) {
                new Alert(Alert.AlertType.ERROR, "No case selected.", ButtonType.CLOSE).showAndWait();
            }
        });
    }

    public void setOnFinishedParsing(FinishedListener listener) {
        onFinishedParsing = listener;
    }
}
