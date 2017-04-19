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

package com.github.kmeel.api.model;

import com.github.kmeel.api.spi.Message;
import com.github.kmeel.api.spi.Parser;
import com.github.kmeel.api.model.objects.ID;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marten4n6
 */
public class Messages {

    private Plugins plugins;

    public Messages(Plugins plugins) {
        this.plugins = plugins;
    }

    public Message getMessage(ID id) {
        List<Parser> parsers = plugins.getPluginManager().getExtensions(Parser.class);

        for (Parser parser : parsers) {
            Message message = parser.getMessage(id);

            if (message != null) return message;
        }
        return null;
    }

    public Set<String> getSupportedFileExtensions() {
        Set<String> extensions = new HashSet<>();

        for (Parser parser : plugins.getPluginManager().getExtensions(Parser.class)) {
            parser.getAcceptedExtensions().forEach(extensions::add);
        }
        return extensions;
    }
}
