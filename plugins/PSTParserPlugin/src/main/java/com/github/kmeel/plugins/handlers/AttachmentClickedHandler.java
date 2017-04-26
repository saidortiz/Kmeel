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

package com.github.kmeel.plugins.handlers;

import com.github.kmeel.api.KmeelAPI;
import com.github.kmeel.api.model.objects.AttachmentRow;
import com.github.kmeel.api.utils.OSUtils;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.model.PSTModel;
import com.pff.PSTObject;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * @author Marten4n6
 *         Creates and opens the clicked attachment
 */
@Slf4j
public class AttachmentClickedHandler {

    private KmeelAPI kmeelAPI;

    public AttachmentClickedHandler(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
    }

    public void handle(MessagePane messagePane) {
        PSTObject pstObject = PSTModel.getInstance().getFromID(kmeelAPI, messagePane.getTable().getSelectionModel().getSelectedItem().getId());

        if (pstObject != null && messagePane.getAttachmentsTable().getSelectionModel().getSelectedItem() != null) {
            Platform.runLater(() -> {
                Optional<ButtonType> confirmOpen = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to open this attachment?", ButtonType.YES, ButtonType.NO).showAndWait();
                if (confirmOpen.isPresent() && confirmOpen.get() == ButtonType.NO) return;

                AttachmentRow attachmentRow = messagePane.getAttachmentsTable().getSelectionModel().getSelectedItem();

                InputStream inputStream = PSTModel.getInstance().getAttachmentFromID().get(attachmentRow.getID());
                Path outputFile = Paths.get(OSUtils.getTempPath() + attachmentRow.getAttachmentName());

                try {
                    Files.copy(inputStream, outputFile, StandardCopyOption.REPLACE_EXISTING);
                    outputFile.toFile().deleteOnExit();
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                }

                SwingUtilities.invokeLater(() -> {
                    try {
                        Desktop.getDesktop().open(outputFile.toFile());
                    } catch (IOException ex) {
                        log.error(ex.getMessage());
                        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to open file: \n" + outputFile.toString(), ButtonType.CLOSE).showAndWait());
                    }
                });
            });
        }
    }
}
