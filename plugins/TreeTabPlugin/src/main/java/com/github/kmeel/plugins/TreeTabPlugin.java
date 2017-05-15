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
import com.github.kmeel.api.spi.PluginableFileTree;
import com.github.kmeel.api.spi.listeners.BookmarkListener;
import com.github.kmeel.api.spi.listeners.TagListener;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.checktreeview.CheckTreeView;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

import java.util.ArrayList;

/**
 * @author Marten4n6
 *         This plugin adds the file tree tab
 */
public class TreeTabPlugin extends Plugin {

    public TreeTabPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class TreeTab implements GlobalTab, PluginableFileTree, BookmarkListener, TagListener {

        private static ObservableList<CheckBoxTreeItem<Object>> treeItems = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
        private static MessagePane messagePane;

        @Override
        public Tab getGlobalTab(KmeelAPI kmeelAPI) {
            Tab tab = new Tab();
            BorderPane borderPane = new BorderPane();
            messagePane = new MessagePane(kmeelAPI);

            // Tab
            ImageView homeIcon = new ImageView(new Image(this.getClass().getResourceAsStream("/images/tree.png")));

            tab.setText("Tree");
            tab.setClosable(false);
            tab.setGraphic(homeIcon);
            tab.setContent(borderPane);

            // File Tree
            CheckBoxTreeItem<Object> rootTreeItem = new CheckBoxTreeItem<>("File(s)");
            CheckTreeView<Object> treeView = new CheckTreeView<>(rootTreeItem);

            rootTreeItem.getChildren().setAll(treeItems);
            rootTreeItem.setExpanded(true);

            // Split Pane
            SplitPane splitPane = new SplitPane();

            splitPane.setDividerPositions(0.18); // Between 0.0 and 1.0
            splitPane.setOrientation(Orientation.HORIZONTAL);
            splitPane.getItems().addAll(treeView, messagePane.getPane());

            // Add
            borderPane.setCenter(splitPane);

            treeView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TreeItem<Object>> observable, TreeItem<Object> oldValue, TreeItem<Object> newValue) -> {
                Platform.runLater(() -> {
                    messagePane.getTable().setCursor(Cursor.WAIT);
                    messagePane.getTable().getItems().clear();
                    messagePane.setFooter(null);
                    messagePane.setBodyText("");
                    messagePane.setHeadersText("");
                    messagePane.setAttachmentTabAmount(0);
                });

                ArrayList<TreeItem<Object>> treeItems = new ArrayList<>(treeView.getCheckModel().getCheckedItems());

                if (treeView.getSelectionModel().getSelectedItem() != null && !treeItems.contains(treeView.getSelectionModel().getSelectedItem())) {
                    // Selected is not checked, add to the list
                    treeItems.add(treeView.getSelectionModel().getSelectedItem());
                }

                treeItems.forEach(item -> {
                    kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).forEach(plugin -> {
                        plugin.treeSelectionUpdate((CheckBoxTreeItem<Object>)item, false, messagePane);
                    });
                });
            });
            treeView.getCheckModel().getCheckedItems().addListener((ListChangeListener.Change<? extends TreeItem<Object>> change) -> {
                Platform.runLater(() -> {
                    messagePane.setFooter(null);
                    messagePane.setBodyText("");
                    messagePane.setHeadersText("");
                    messagePane.setAttachmentTabAmount(0);
                });

                while (change.next()) {
                    if (change.wasAdded()) {
                        change.getAddedSubList().forEach(treeItem -> {
                            TreeItem selectedItem = treeView.getSelectionModel().getSelectedItem();

                            if (selectedItem == null) {
                                kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).forEach(plugin -> {
                                    plugin.treeSelectionUpdate((CheckBoxTreeItem<Object>) treeItem, false, messagePane);
                                });
                            } else if (!selectedItem.equals(treeItem)) {
                                kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).forEach(plugin -> {
                                    plugin.treeSelectionUpdate((CheckBoxTreeItem<Object>) treeItem, false, messagePane);
                                });
                            }
                        });
                    } else {
                        change.getRemoved().forEach(treeItem -> {
                            kmeelAPI.plugins().getPluginManager().getExtensions(PluginableFileTree.class).forEach(plugin -> {
                                plugin.treeSelectionUpdate((CheckBoxTreeItem<Object>) treeItem, true, messagePane);
                            });
                        });
                    }
                }
            });
            return tab;
        }

        @Override
        public void addTreeItem(CheckBoxTreeItem treeItem) {
            Platform.runLater(() -> treeItems.add(treeItem));
        }

        @Override
        public void treeSelectionUpdate(CheckBoxTreeItem<Object> updatedItem, boolean removed, MessagePane messagePane) {}

        @Override
        public void bookmarkAdded(ID id) {
            messagePane.updateFooter();

            messagePane.getTable().getItems().stream()
                    .filter(row -> row.getId().equals(id))
                    .filter(row -> !row.getBookmarked().get())
                    .forEach(row -> row.setBookmarked(true));
        }

        @Override
        public void bookmarkRemoved(ID id) {
            messagePane.updateFooter();

            messagePane.getTable().getItems().stream()
                    .filter(row -> row.getId().equals(id))
                    .filter(row -> row.getBookmarked().get())
                    .forEach(row -> row.setBookmarked(false));
        }

        @Override
        public void tagChanged(ID id, String newTag) {
            messagePane.getTable().getItems().stream()
                    .filter(row -> row.getId().equals(id))
                    .filter(row -> !row.getTag().get().equals(newTag))
                    .forEach(row -> row.setTag(newTag));
        }

        @Override
        public void tagRemoved(ID id) {
            messagePane.getTable().getItems().stream()
                    .filter(row -> row.getId().equals(id))
                    .filter(row -> !row.getTag().get().isEmpty())
                    .forEach(row -> row.setTag(""));
        }
    }
}
