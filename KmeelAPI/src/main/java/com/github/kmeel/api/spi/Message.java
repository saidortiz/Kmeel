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

package com.github.kmeel.api.spi;

import com.github.kmeel.api.model.objects.MessageAttachment;
import com.github.kmeel.api.model.objects.MessageRow;

import java.util.List;

/**
 * @author Marten4n6
 */
public interface Message {

    /**
     * @return MessageRow that can be added to the MessagePane.
     */
    MessageRow getRow();

    /**
     * @return The body of this message.
     */
    String getBody();

    /**
     * @return The headers of this message.
     */
    String getHeaders();

    /**
     * @return A list of attachment of this message.
     */
    List<MessageAttachment> getAttachments();
}
