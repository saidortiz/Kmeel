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

package com.github.kmeel.api.custom;

import javafx.scene.control.ContextMenu;
import com.github.kmeel.api.view.MessagePane;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author Marten4n6
 *         Menu items to add to each context menu
 */
public interface DefaultContextMenuItems extends ExtensionPoint {

    /**
     * Adds a menu item to the context menu
     */
    void addMenuItems(MessagePane messagePane, ContextMenu contextMenu);
}
