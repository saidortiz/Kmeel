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
import com.github.kmeel.api.model.objects.ID;
import com.github.kmeel.api.view.LoadingView;
import ro.fortsoft.pf4j.ExtensionPoint;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

/**
 * @author Marten4n6
 *         Pluginable interface for parsing files.
 */
public interface Parser extends ExtensionPoint {

    /**
     * @return The name of this parser.
     */
    String getName();

    /**
     * @return A set of file extensions this parser accepts.
     */
    Set<String> getAcceptedExtensions();

    /**
     * Called before parsing starts
     *
     * @param kmeelAPI    API to interact with the current case.
     * @param loadingView Loading view for adding tasks.
     */
    void setup(KmeelAPI kmeelAPI, LoadingView loadingView);

    /**
     * Parses the file.
     *
     * @param file             The file to parse.
     * @param totalFiles       The amount of files this parser accepts.
     * @param finishedListener This listener should be called EVERY TIME the method is finished.
     */
    void parseFile(File file, int totalFiles, ActionListener finishedListener);

    /**
     * @return The Message from the ID.
     * @see Message
     */
    Message getMessage(ID id);
}
