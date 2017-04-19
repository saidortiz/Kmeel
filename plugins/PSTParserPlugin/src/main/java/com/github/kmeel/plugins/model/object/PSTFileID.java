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

package com.github.kmeel.plugins.model.object;

import com.pff.PSTException;
import com.pff.PSTFile;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Marten4n6
 *         Used by the PSTModel to get the original PSTFile object back
 */
@ToString @Slf4j
public class PSTFileID {

    private @Getter String id;

    public PSTFileID(PSTFile pstFile) {
        try {
            String unencodedID = pstFile.getMessageStore().getTagRecordKeyAsUUID().toString() + pstFile.getRootFolder().getDescriptorNodeId();
            id = Base64.getEncoder().encodeToString(unencodedID.getBytes(StandardCharsets.UTF_8));
        } catch (IOException | PSTException ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public PSTFileID(String id) {
        this.id = id;
    }
}
