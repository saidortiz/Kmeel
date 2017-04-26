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

import com.google.gson.Gson;
import lombok.Cleanup;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import com.github.kmeel.api.model.objects.Case;
import com.github.kmeel.api.utils.OSUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * @author Marten4n6
 *         This class handles cases
 */
@Slf4j
public class Cases {

    private static Gson gson = new Gson();
    private static @Getter @Setter Case currentCase;

    private Cases() {}

    /**
     * Stores the case object in JSON.
     */
    public static void storeCaseObject(@NonNull Case caseObject) {
        new File(OSUtils.getCasePath(caseObject.getName())).mkdir();

        Path jsonPath = Paths.get(OSUtils.getCasePath(caseObject.getName()) + "Case.json");

        if (!jsonPath.toFile().exists()) {
            try {
                Files.createFile(jsonPath);

                Files.write(
                        jsonPath,
                        gson.toJson(caseObject).getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
        }
    }

    /**
     * @return The case object from the case name otherwise null.
     * @throws NullPointerException if the caseName is null.
     */
    public static Case get(@NonNull String caseName) {
        Path jsonPath = Paths.get(OSUtils.getCasePath(caseName) + "Case.json");

        try {
            return gson.fromJson(Files.readAllLines(jsonPath).get(0), Case.class);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * @return A list of case names.
     */
    public static ArrayList<String> getNames() {
        ArrayList<String> caseNames = new ArrayList<>();

        try {
            Files.list(Paths.get(OSUtils.getCasesPath())).forEach(path -> {
                caseNames.add(path.toFile().getName());
            });
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        return caseNames;
    }
}