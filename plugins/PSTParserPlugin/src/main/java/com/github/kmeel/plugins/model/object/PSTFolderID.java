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

import com.pff.PSTFolder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Marten4n6
 *         Holds an ID for the PSTFolder which is indexed.
 *         Used to get all messages from a specific PSTFolder just by searching for the ID.
 */
@ToString @Slf4j
public class PSTFolderID {

    private @Getter String id;

    public PSTFolderID(PSTFolder pstFolder) {
        if (pstFolder == null) return;

        String unencodedID = pstFolder.getDisplayName() + pstFolder.getDescriptorNodeId();
        id = Base64.getEncoder().encodeToString((unencodedID.getBytes(StandardCharsets.UTF_8)));
    }
}
