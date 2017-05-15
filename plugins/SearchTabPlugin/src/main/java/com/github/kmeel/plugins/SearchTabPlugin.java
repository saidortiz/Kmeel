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
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.spi.GlobalTab;
import com.github.kmeel.api.spi.listeners.BookmarkListener;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.custom.CustomTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Marten4n6
 *         This plugin adds the search tab
 */
@Slf4j
public class SearchTabPlugin extends Plugin {

    public SearchTabPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class SearchPluginTab implements GlobalTab, BookmarkListener {

        private static MessagePane messagePane;

        @Override
        public Tab getGlobalTab(KmeelAPI kmeelAPI) {
            Tab tab = new Tab();
            BorderPane layout = new BorderPane();
            messagePane = new MessagePane(kmeelAPI);
            SearchController searchController = new SearchController(kmeelAPI, messagePane);

            // Tab
            ImageView searchIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/images/search.png")));

            tab.setClosable(false);
            tab.setText("Search");
            tab.setGraphic(searchIcon);

            // Top HBox
            HBox hBox = new HBox();

            hBox.setSpacing(5);
            hBox.setPadding(new Insets(0, 5, 0, 0));
            hBox.setMaxWidth(Double.MAX_VALUE);

            // Search Field
            CustomTextField searchField = new CustomTextField();
            ImageView helpIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/images/question.png")));

            helpIcon.setCursor(Cursor.HAND);

            searchField.setMaxWidth(Double.MAX_VALUE);
            searchField.setRight(helpIcon);

            // Settings Label
            Label settingsLabel = new Label();
            ImageView settingsIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/images/gear.png")));

            settingsLabel.setCursor(Cursor.HAND);
            settingsLabel.setGraphic(settingsIcon);

            // Add
            hBox.getChildren().addAll(searchField, settingsLabel);

            HBox.setHgrow(searchField, Priority.ALWAYS);

            layout.setPadding(new Insets(5, 0, 0, 0));
            layout.setTop(hBox);
            layout.setCenter(messagePane.getPane());

            tab.setContent(layout);

            // Listeners
            searchField.setOnAction((event) -> {
                log.info("Searching for: " + searchField.getText());

                searchController.search(searchField.getText());
            });
            helpIcon.setOnMouseClicked((event) -> {
                Stage stage = new Stage();
                BorderPane borderPane = new BorderPane();
                Scene scene = new Scene(borderPane, 600, 400);
                WebView webView = new WebView();

                borderPane.setCenter(webView);

                stage.setScene(scene);
                stage.centerOnScreen();
                stage.show();

                try {
                    String querySyntax = new String(Files.readAllBytes(Paths.get(getClass().getClassLoader().getResource("querysyntax.html").toURI())));

                    Platform.runLater(() -> webView.getEngine().loadContent(querySyntax, "text/html"));
                } catch (IOException | URISyntaxException ex) {
                    log.error(ex.getMessage(), ex);
                }
            });
            settingsLabel.setOnMouseClicked((event) -> {
                TextInputDialog dialog = new TextInputDialog(kmeelAPI.settings().get("SearchLimit"));

                dialog.setHeaderText("Search Limit");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    kmeelAPI.settings().set("SearchLimit", result.get());
                    searchController.refresh();
                }
            });
            return tab;
        }

        @Override
        public void bookmarkAdded(ID id) {
            messagePane.updateFooter();
        }

        @Override
        public void bookmarkRemoved(ID id) {
            messagePane.updateFooter();

            // Bookmark removed in another tab, update table
            messagePane.getTable().getItems().stream()
                    .filter(row -> row.getId().equals(id))
                    .filter(row -> row.getBookmarked().get())
                    .forEach(row -> row.setBookmarked(false));
        }
    }
}