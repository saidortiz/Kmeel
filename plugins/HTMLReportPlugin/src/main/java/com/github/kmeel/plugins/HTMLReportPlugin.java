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
import com.github.kmeel.api.model.Cases;
import com.github.kmeel.api.model.objects.Case;
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.model.objects.MessageAttachment;
import com.github.kmeel.api.model.objects.MessageRow;
import com.github.kmeel.api.model.objects.Message;
import com.github.kmeel.api.spi.Report;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

/**
 * @author Marten4n6
 */
@Slf4j
public class HTMLReportPlugin extends Plugin {

    public HTMLReportPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class HTMLReport implements Report {

        @Override
        public String getReportFileFormat() {
            return "HTML";
        }

        @Override
        public void createReport(KmeelAPI kmeelAPI, File outputFolder, String reportName, ActionListener finishedListener) {
            StringBuilder html = new StringBuilder();
            File outputFile = new File(outputFolder.getPath() + File.separator + reportName + ".html");

            // Case Information
            Case caseObject = Cases.getCurrentCase();
            String[] keys = new String[]{"<b>Case Name:</b>", "<b>Description:</b>", "<b>Investigator:</b>", "<b>Source:</b>", "<b>Size:</b>"};
            String[] values = new String[]{caseObject.getName(), caseObject.getDescription(), caseObject.getInvestigator(), caseObject.getSources().toString(), caseObject.getSize()};

            for (int i = 0; i <= 4; i++) {
                html.append(keys[i]).append(" ").append(values[i]).append("<br/>");
            }
            html.append("<br/>");

            for (ID id : kmeelAPI.bookmarks().get()) {
                Message message = kmeelAPI.messages().getMessage(id);
                String tagName = kmeelAPI.tags().get(id);

                if (!tagName.isEmpty()) {
                    html.append("Tag: ").append(tagName).append("<br/>");
                }

                // Create attachments
                List<MessageAttachment> attachmentList = message.getAttachments();

                if (attachmentList != null) {
                    new File(outputFolder.getPath() + File.separator + "Attachments").mkdir();

                    html.append("Attachments:").append("<br/>");

                    for (MessageAttachment attachment : attachmentList) {
                        try {
                            MessageRow messageRow = message.getRow();
                            File attachmentDirectory = new File(outputFolder.getPath() + File.separator + "Attachments" + File.separator + messageRow.getSubject().replaceAll("[^a-zA-Z0-9.-]", "_"));
                            Path attachmentPath = Paths.get(attachmentDirectory.getPath() + File.separator + attachment.getAttachmentName().replaceAll("[^a-zA-Z0-9.-]", "_"));

                            attachmentDirectory.mkdir();
                            Files.copy(attachment.getInputStream(), attachmentPath, StandardCopyOption.REPLACE_EXISTING);

                            html.append(attachment.getAttachmentName()).append("<br/>");
                        } catch (IOException ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                    html.append("<br/>");
                }

                // Headers
                if (!message.getHeaders().isEmpty()) {
                    html.append("<b>Headers:</b>").append("<br/>");
                    html.append(message.getHeaders()).append("<br/>");
                }

                // Body
                if (!message.getBody().isEmpty()) {
                    html.append("<b>Message:</b>");
                    html.append(message.getBody());
                }

                html.append("<b>");
                for (int i = 0; i < 154; i++) html.append("-");
                html.append("</b>").append("<br/>");
            }

            finishedListener.actionPerformed(null);
            createAndOpen(outputFile, html.toString());
        }

        private void createAndOpen(File outputFile, String html) {
            if (outputFile.exists()) {
                Label message = new Label("File already exists, do you want to overwrite the file?");
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.YES, ButtonType.NO);

                message.setWrapText(true);
                alert.getDialogPane().setContent(message);

                Optional<ButtonType> result = alert.showAndWait();

                if (result.isPresent() && result.get() == ButtonType.YES) {
                    try {
                        Files.delete(Paths.get(outputFile.getPath()));
                    } catch (IOException ex) {
                        //
                    }
                }
            }

            try {
                Files.write(Paths.get(outputFile.getPath()), html.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
                Optional<ButtonType> confirmOpen = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to open the created report?", ButtonType.YES, ButtonType.NO).showAndWait();

                if (confirmOpen.isPresent() && confirmOpen.get() == ButtonType.YES) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            Desktop.getDesktop().open(outputFile);
                        } catch (IOException ex) {
                            log.error("Failed to open report: " + ex.getMessage());
                        }
                    });
                }
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }
}
