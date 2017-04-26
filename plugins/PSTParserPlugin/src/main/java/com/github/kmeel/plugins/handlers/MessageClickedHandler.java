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
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.model.objects.Message;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.api.view.objects.Footer;
import com.github.kmeel.plugins.Utils;
import com.github.kmeel.plugins.model.PSTModel;
import com.pff.*;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * @author Marten4n6
 *         Handles message clicked events, updates the body, headers and attachments
 */
@Slf4j
public class MessageClickedHandler {

    private KmeelAPI kmeelAPI;

    public MessageClickedHandler(KmeelAPI kmeelAPI) {
        this.kmeelAPI = kmeelAPI;
    }

    public void handle(MessagePane messagePane, ID id) {
        Message message = PSTModel.getInstance().getMessage(kmeelAPI, id);

        if (message != null) {
            messagePane.setBodyText(message.getBody());
            messagePane.setHeadersText(message.getHeaders());
            messagePane.setAttachmentTabAmount(message.getAttachments().size());

            message.getAttachments().forEach(attachment -> {
                PSTModel.getInstance().getAttachmentFromID().put(attachment.getRow().getID(), attachment.getInputStream());

                Platform.runLater(() -> {
                    messagePane.getAttachmentsTable().getItems().add(attachment.getRow());
                });
            });
        }

        messagePane.setFooter(new Footer(PSTModel.getInstance().getFolderPath(kmeelAPI, id), kmeelAPI, messagePane.getTable()));
    }
}
