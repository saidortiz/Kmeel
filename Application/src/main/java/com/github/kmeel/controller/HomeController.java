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
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Marten4n6
 *         This class controls the Home tab
 */
@Slf4j
public class HomeController {

    private HomeTab homeTab;

    private KmeelAPI kmeelAPI;
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
                // Create Case
                if (Cases.getNames().contains(newCaseStage.getName())) {
                    new Alert(Alert.AlertType.ERROR, "A case with this name already exists.", ButtonType.CLOSE).showAndWait();
                } else if (newCaseStage.getName().trim().isEmpty() || newCaseStage.getName().contains("'")) {
                    new Alert(Alert.AlertType.ERROR, "Invalid case name.", ButtonType.CLOSE).showAndWait();
                } else if (newCaseStage.getInvestigator().trim().isEmpty() || newCaseStage.getInvestigator().contains("'")) {
                    new Alert(Alert.AlertType.ERROR, "Invalid investigator name.", ButtonType.CLOSE).showAndWait();
                } else if (newCaseStage.getSources().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Invalid data source.", ButtonType.CLOSE).showAndWait();
                } else {
                    kmeelAPI = newCaseStage.getKmeelAPI();

                    List<String> sources = newCaseStage.getSources();
                    long sourceSize = getSourceSize(kmeelAPI, sources, newCaseStage.isSubFoldersSelected());

                    Case caseObject = new Case(
                            newCaseStage.getName(),
                            newCaseStage.getDescription(),
                            newCaseStage.getInvestigator(),
                            humanReadableByteCount(sourceSize),
                            newCaseStage.getHashType(),
                            sources);

                    if (new File(sources.get(0)).isDirectory()) caseObject.setParseSubFolders(newCaseStage.isSubFoldersSelected());

                    kmeelAPI.setCase(caseObject);
                    Cases.setCurrentCase(caseObject);
                    Cases.createCase(caseObject);

                    kmeelAPI.settings().set("SearchLimit", "0");
                    kmeelAPI.settings().set("DateFormat", "EEE, d MMM yyyy HH:mm:ss");
                    kmeelAPI.settings().set("DisabledPlugins", new Gson().toJson(newCaseStage.getDisabledPlugins()));

                    // Disable plugins
                    if (!newCaseStage.getDisabledPlugins().isEmpty()) {
                        int disabledPlugins = newCaseStage.getDisabledPlugins().size();

                        newCaseStage.getDisabledPlugins().forEach(pluginName -> kmeelAPI.plugins().getPluginManager().unloadPlugin(pluginName));

                        switch (disabledPlugins) {
                            case 1:
                                log.info("Disabled " + disabledPlugins + " plugin.");
                                break;
                            default:
                                log.info("Disabled " + disabledPlugins + " plugins.");
                                break;
                        }
                    }

                    newCaseStage.close();

                    if (newCaseStage.isOpenCaseSelected()) {
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
                            fileParser.parseDirectory(new File(caseObject.getSources().get(0)), caseObject.getParseSubFolders());
                        } else {
                            fileParser.parseFiles(caseObject.getSources());
                        }
                    }

                    Platform.runLater(() -> homeTab.getTable().getItems().add(caseObject));
                }
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

                ArrayList<String> disabledPlugins = new Gson().fromJson(kmeelAPI.settings().get("DisabledPlugins"), ArrayList.class);
                if (!disabledPlugins.isEmpty()) {
                    disabledPlugins.forEach(pluginName -> kmeelAPI.plugins().getPluginManager().unloadPlugin(pluginName));

                    if (disabledPlugins.size() > 1) {
                        log.info("Disabled " + disabledPlugins.size() + " plugins.");
                    } else {
                        log.info("Disabled " + disabledPlugins.size() + " plugin.");
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
                    fileParser.parseDirectory(new File(caseObject.getSources().get(0)), caseObject.getParseSubFolders());
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

                Cases.remove(caseName);

                Platform.runLater(() -> {
                    homeTab.getTable().getItems().remove(homeTab.getTable().getSelectionModel().getSelectedItem());
                });

                log.info("Case \"" + caseName + "\" removed.");
            } catch (NullPointerException ex) {
                new Alert(Alert.AlertType.ERROR, "No case selected.", ButtonType.CLOSE).showAndWait();
            }
        });
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

    public void setOnFinishedParsing(FinishedListener listener) {
        onFinishedParsing = listener;
    }
}
