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

package com.github.kmeel.plugins;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.spi.GlobalTab;
import com.github.kmeel.api.spi.Report;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.util.StringConverter;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Marten4n6
 *         This plugin adds the report tab
 */
public class ReportTabPlugin extends Plugin {

    public ReportTabPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class ReportTab implements GlobalTab {

        @Override
        public Tab getGlobalTab(KmeelAPI kmeelAPI) {
            Tab tab = new Tab();
            GridPane gridPane = new GridPane();

            // GridPane
            gridPane.setPadding(new Insets(5, 5, 5, 5));
            gridPane.setVgap(5);

            // Tab
            FontAwesomeIconView reportIcon = new FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT);

            tab.setClosable(false);
            tab.setText("Report");
            reportIcon.setSize("20");
            tab.setGraphic(reportIcon);
            tab.setContent(gridPane);

            // Labels
            Label labelNoBookmarks = new Label("Bookmarks are required to create a report.");
            Label labelReportType = new Label("Type:");
            Label labelReportName = new Label("Name:");
            Label labelReportOutput = new Label("Output: ");

            // Combo Box
            ComboBox<Report> comboBoxReportType = new ComboBox<>();

            comboBoxReportType.setConverter(new StringConverter<Report>() {
                @Override
                public String toString(Report report) {
                    if (report == null) {
                        return "";
                    } else {
                        return report.getReportFileFormat();
                    }
                }

                @Override
                public Report fromString(String s) {
                    for (Report report : comboBoxReportType.getItems()) {
                        if (report.getReportFileFormat().equals(s)) {
                            return report;
                        }
                    }
                    return null;
                }
            });
            comboBoxReportType.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(comboBoxReportType, Priority.ALWAYS);

            kmeelAPI.plugins().getPluginManager().getExtensions(Report.class).forEach(report -> {
                comboBoxReportType.getItems().add(report);
            });

            if (comboBoxReportType.getItems().isEmpty()) {
                comboBoxReportType.setValue(new Report() {
                    @Override
                    public String getReportFileFormat() {
                        return "Failed to find report plugins.";
                    }

                    @Override
                    public void createReport(KmeelAPI api, File outputFolder, String reportName, ActionListener finishedListener) {}
                });
            } else {
                comboBoxReportType.setValue(comboBoxReportType.getItems().get(0));
            }

            // Text Fields
            TextField textFieldReportName = new TextField();
            TextField textFieldReportOutput = new TextField();

            // Buttons
            Button buttonBrowseOutput = new Button(" ... ");
            Button buttonCreateReport = new Button("Create Report");

            buttonCreateReport.setPrefWidth(200);
            buttonCreateReport.setMinWidth(200);
            buttonCreateReport.setMinHeight(30);

            // Add
            HBox hBoxOutput = new HBox();

            hBoxOutput.getChildren().addAll(textFieldReportOutput, buttonBrowseOutput);
            hBoxOutput.setSpacing(5);

            textFieldReportOutput.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(textFieldReportOutput, Priority.ALWAYS);

            gridPane.add(labelNoBookmarks, 0, 0);
            gridPane.add(labelReportType, 0, 0);
            gridPane.add(comboBoxReportType, 1, 0);
            gridPane.add(labelReportName, 0, 1);
            gridPane.add(textFieldReportName, 1, 1);
            gridPane.add(labelReportOutput, 0, 2);
            gridPane.add(hBoxOutput, 1, 2);
            gridPane.add(buttonCreateReport, 1, 3);

            // Listeners
            tab.setOnSelectionChanged((event) -> {
                if (tab.isSelected()) {
                    if (kmeelAPI.bookmarks().get().isEmpty()) {
                        gridPane.getChildren().forEach(node -> {
                            node.setVisible(false);
                            node.setManaged(false);
                        });

                        labelNoBookmarks.setVisible(true);
                        labelNoBookmarks.setManaged(true);
                    } else {
                        gridPane.getChildren().forEach(node -> {
                            node.setVisible(true);
                            node.setManaged(true);
                        });

                        labelNoBookmarks.setVisible(false);
                        labelNoBookmarks.setManaged(false);
                    }
                }
            });
            buttonBrowseOutput.setOnAction((event) -> {
                DirectoryChooser directoryChooser = new DirectoryChooser();

                directoryChooser.setTitle("Kmeel");
                File file = directoryChooser.showDialog(tab.getTabPane().getScene().getWindow());

                if (file != null) {
                    textFieldReportOutput.setText(file.getPath());
                }
            });
            buttonCreateReport.setOnAction((event) -> {
                if (textFieldReportName.getText().trim().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Invalid report name.", ButtonType.CLOSE).showAndWait();
                } else if (textFieldReportOutput.getText().trim().isEmpty() || !new File(textFieldReportOutput.getText()).exists()) {
                    new Alert(Alert.AlertType.ERROR, "Invalid output path.", ButtonType.CLOSE).showAndWait();
                } else {
                    Platform.runLater(() -> tab.getTabPane().getScene().setCursor(Cursor.WAIT));

                    kmeelAPI.plugins().getPluginManager().getExtensions(Report.class)
                            .forEach(report -> {
                                ActionListener finishedListener = (eventWhatever) -> {
                                    Platform.runLater(() -> tab.getTabPane().getScene().setCursor(Cursor.DEFAULT));
                                };

                                report.createReport(kmeelAPI, new File(textFieldReportOutput.getText()), textFieldReportName.getText(), finishedListener);
                            });
                }
            });

            return tab;
        }
    }
}
