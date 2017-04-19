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

/**
 * @author Marten4n6
 *         This class stores information about the node
 */
public class TreeObject {

    private @Getter String title;
    private @Getter String folderID;

    public TreeObject(String title, PSTFolder pstFolder) {
        this.title = title;
        this.folderID = new PSTFolderID(pstFolder).getId();
    }

    @Override
    public String toString() {
        return title;
    }
}