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

import com.github.kmeel.api.model.objects.AttachmentRow;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.model.objects.Message;
import com.github.kmeel.api.utils.OSUtils;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.model.EMLModel;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * @author Marten4n6
 */
@Slf4j
public class AttachmentSelectionHandler {

    private EMLModel emlModel;

    public AttachmentSelectionHandler(EMLModel emlModel) {
        this.emlModel = emlModel;
    }

    public void handle(MessagePane messagePane) {
        AttachmentRow row = messagePane.getAttachmentsTable().getSelectionModel().getSelectedItem();
        ID attachmentID = row.getID();

        if (emlModel.getAttachmentFromID().get(attachmentID) != null) {
            Optional<ButtonType> confirmOpen = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to open this attachment?", ButtonType.YES, ButtonType.NO).showAndWait();

            if (confirmOpen.isPresent() && confirmOpen.get() == ButtonType.YES) {
                Path outputPath = Paths.get(OSUtils.getTempPath() + row.getAttachmentName());

                try {
                    Files.copy(emlModel.getAttachmentFromID().get(attachmentID), outputPath, StandardCopyOption.REPLACE_EXISTING);

                    SwingUtilities.invokeLater(() -> {
                        try {
                            Desktop.getDesktop().open(outputPath.toFile());
                        } catch (IOException ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    });
                } catch (IOException ex) {
                    log.error(ex.getMessage(), ex);
                    new Alert(Alert.AlertType.ERROR, "Failed to open attachment: " + ex.getMessage(), ButtonType.CLOSE).showAndWait();
                }
            }
        }
    }
}
