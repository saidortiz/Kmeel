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

package com.github.kmeel.api.custom;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.view.TagStage;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import lombok.extern.slf4j.Slf4j;
import com.github.kmeel.api.model.objects.MessageAttachment;
import com.github.kmeel.api.model.objects.MessageRow;
import com.github.kmeel.api.view.MessagePane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * @author Marten4n6
 *         Default context menu that gets added to each MessagePane
 */
@Slf4j
public class DefaultContextMenu extends ContextMenu {

    private MessagePane messagePane;
    private KmeelAPI kmeelAPI;

    private MenuItem bookmarksAddSelected;
    private MenuItem bookmarksRemoveSelected;
    private MenuItem tagChangeSelected;
    private MenuItem tagRemoveSelected;
    private MenuItem exportAttachments;

    public DefaultContextMenu(MessagePane messagePane, KmeelAPI kmeelAPI) {
        this.messagePane = messagePane;
        this.kmeelAPI = kmeelAPI;

        setupContextMenu();
        setupListeners();
    }

    private void setupContextMenu() {
        Menu menuBookmark = new Menu("Bookmark");
        Menu menuTag = new Menu("Tag");
        Menu menuExport = new Menu("Export");

        // Bookmark
        bookmarksAddSelected = new MenuItem("Add Selected");
        bookmarksRemoveSelected = new MenuItem("Remove Selected");

        // Tag
        tagChangeSelected = new MenuItem("Change Selected");
        tagRemoveSelected = new MenuItem("Remove Selected");

        // Export
        exportAttachments = new MenuItem("Export Attachments");

        // Add
        menuBookmark.getItems().addAll(bookmarksAddSelected, bookmarksRemoveSelected);
        menuTag.getItems().addAll(tagChangeSelected, tagRemoveSelected);
        menuExport.getItems().addAll(exportAttachments);

        this.getItems().addAll(menuBookmark, menuTag, menuExport);

        kmeelAPI.plugins().getPluginManager().getExtensions(DefaultContextMenuItems.class).forEach(plugin -> {
            plugin.addMenuItems(messagePane, this);
        });
    }

    private void setupListeners() {
        bookmarksAddSelected.setOnAction((event) -> {
            messagePane.getTable().getSelectionModel().getSelectedItems().forEach(row -> {
                Platform.runLater(() -> row.setBookmarked(true));
            });
        });
        bookmarksRemoveSelected.setOnAction((event) -> {
            messagePane.getTable().getSelectionModel().getSelectedItems().forEach(row -> {
                Platform.runLater(() -> row.setBookmarked(false));
            });
        });
        tagChangeSelected.setOnAction((event) -> {
            TagStage tagStage = new TagStage(kmeelAPI);

            tagStage.show();

            tagStage.setOnAddTag((addTagEvent) -> {
                String tagName = tagStage.getTagName();

                tagStage.close();

                messagePane.getTable().getSelectionModel().getSelectedItems().forEach(row -> {
                    Platform.runLater(() -> row.setTag(tagName));
                });
            });
        });
        tagRemoveSelected.setOnAction((event) -> {
            messagePane.getTable().getSelectionModel().getSelectedItems().forEach(row -> {
                Platform.runLater(() -> row.setTag(""));
            });
        });
        exportAttachments.setOnAction((event) -> {
            boolean hasAttachments = false;

            for (MessageRow row : messagePane.getTable().getSelectionModel().getSelectedItems()) {
                if (kmeelAPI.messages().getMessage(row.getId()).getAttachments() != null) {
                    hasAttachments = true;
                    break;
                }
            }

            if (hasAttachments) {
                exportAttachments();
            } else {
                new Alert(Alert.AlertType.ERROR, "Selected rows have no attachments.", ButtonType.CLOSE).showAndWait();
            }
        });
    }

    private void exportAttachments() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File outputDirectory = directoryChooser.showDialog(null);

        for (MessageRow row : messagePane.getTable().getSelectionModel().getSelectedItems()) {
            List<MessageAttachment> attachmentList = kmeelAPI.messages().getMessage(row.getId()).getAttachments();

            if (attachmentList != null && outputDirectory != null) {
                for (MessageAttachment attachment : attachmentList) {
                    try {
                        Files.copy(attachment.getInputStream(), Paths.get(outputDirectory.getPath() + File.separator + attachment.getAttachmentName()), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            }
        }

        if (outputDirectory != null) {
            Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Attachments exported successfully.", ButtonType.CLOSE).showAndWait());
        }
    }
}
