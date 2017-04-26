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

import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.model.objects.Message;
import com.github.kmeel.api.model.objects.MessageAttachment;
import com.github.kmeel.api.view.MessagePane;
import com.github.kmeel.plugins.model.EMLModel;

import java.util.List;

/**
 * @author Marten4n6
 */
public class MessageSelectionHandler {

    private EMLModel emlModel;

    public MessageSelectionHandler(EMLModel emlModel) {
        this.emlModel = emlModel;
    }

    public void handle(MessagePane messagePane, ID id) {
        Message message = emlModel.getMessage(id);

        emlModel.getAttachmentFromID().clear();

        if (message != null) {
            List<MessageAttachment> attachments = message.getAttachments();

            messagePane.setBodyText(message.getBody().replaceAll("\n", "<br/>"));
            messagePane.setHeadersText(message.getHeaders().replaceAll("\n", "<br/>"));
            messagePane.setAttachmentTabAmount(attachments.size());

            attachments.forEach(attachment -> {
                emlModel.getAttachmentFromID().put(attachment.getRow().getID(), attachment.getInputStream());

                messagePane.getAttachmentsTable().getItems().add(attachment.getRow());
            });
        }
    }
}
