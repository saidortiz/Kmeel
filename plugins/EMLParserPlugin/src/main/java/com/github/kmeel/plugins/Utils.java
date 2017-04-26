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

import com.github.kmeel.api.model.objects.ID;
import lombok.extern.slf4j.Slf4j;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Marten4n6
 */
@Slf4j
public class Utils {

    /**
     * @return A human readable byte size
     */
    public static String humanReadableByteCount(int bytes) {
        boolean si = false;
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * @return A Base64 ID of the MimeMessage
     */
    public static ID getID(MimeMessage mimeMessage) {
        try {
            String unencodedID = mimeMessage.getMessageID();
            return new ID(Base64.getEncoder().encodeToString(unencodedID.getBytes(StandardCharsets.UTF_8)));
        } catch (MessagingException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}
