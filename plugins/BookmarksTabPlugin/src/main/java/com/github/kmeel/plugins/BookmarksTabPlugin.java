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
import com.github.kmeel.api.spi.listeners.TagListener;
import com.github.kmeel.api.view.MessagePane;
import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * @author Marten4n6
 *         This plugin adds the bookmarks tab
 */
public class BookmarksTabPlugin extends Plugin {

    public BookmarksTabPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class BookmarksTab implements GlobalTab, BookmarkListener, TagListener {

        private static MessagePane messagePane;
        private static KmeelAPI kmeelAPI;

        @Override
        public Tab getGlobalTab(KmeelAPI api) {
            Tab tab = new Tab();
            GridPane gridPane = new GridPane();

            kmeelAPI = api;
            messagePane = new MessagePane(kmeelAPI);

            // Tab
            ImageView bookmarkIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/images/bookmark.png")));

            tab.setClosable(false);
            tab.setText("Bookmarks");
            tab.setGraphic(bookmarkIcon);
            tab.setContent(gridPane);

            // Panel
            GridPane.setHgrow(messagePane.getPane(), Priority.ALWAYS);
            GridPane.setVgrow(messagePane.getPane(), Priority.ALWAYS);
            messagePane.getPane().setMaxWidth(Double.MAX_VALUE);
            messagePane.getPane().setMaxHeight(Double.MAX_VALUE);

            // Add
            gridPane.add(messagePane.getPane(), 0, 1);

            kmeelAPI.bookmarks().get().forEach(id -> {
                Platform.runLater(() -> {
                    messagePane.getTable().getItems().add(kmeelAPI.messages().getMessage(id).getRow());
                });
            });
            return tab;
        }

        @Override
        public void bookmarkAdded(ID id) {
            messagePane.updateFooter();

            Platform.runLater(() -> {
                messagePane.getTable().getItems().add(kmeelAPI.messages().getMessage(id).getRow());
            });
        }

        @Override
        public void bookmarkRemoved(ID id) {
            messagePane.updateFooter();

            Platform.runLater(() -> {
                messagePane.getTable().getItems().removeIf(row -> row.getId().equals(id));
            });
        }

        @Override
        public void tagChanged(ID id, String newTag) {
            Platform.runLater(() -> {
                messagePane.getTable().getItems().forEach(row -> {
                    if (row.getId().equals(id) && !row.getTag().get().equals(newTag)) {
                        row.setTag(newTag);
                    }
                });
            });
        }

        @Override
        public void tagRemoved(ID id) {
            Platform.runLater(() -> {
                messagePane.getTable().getItems().forEach(row -> {
                    if (row.getId().equals(id)) row.setTag("");
                });
            });
        }
    }
}
