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

import com.github.kmeel.api.KmeelAPI;
import javafx.scene.control.Tab;
import com.github.kmeel.api.view.MessagePane;
import ro.fortsoft.pf4j.ExtensionPoint;

/**
 * @author Marten4n6
 */
public interface MessageTab extends ExtensionPoint {

    /**
     * @param kmeelAPI    API to interact with the current case.
     * @param messagePane The MessagePane this tab will be added to.
     * @return A tab that gets added to each MessagePane.
     */
    Tab getMessageTab(KmeelAPI kmeelAPI, MessagePane messagePane);
}
