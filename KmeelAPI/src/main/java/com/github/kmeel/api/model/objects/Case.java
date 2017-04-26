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

package com.github.kmeel.api.model.objects;

import lombok.Data;

import java.util.List;

/**
 * @author Marten4n6
 *         This class stores information about the case.
 */
@Data
public class Case {

    private final String name, description, investigator, size;
    private final List<String> sources, disabledPlugins;
    private boolean indexed = false;
    private Boolean hasSubFolders = false;
}