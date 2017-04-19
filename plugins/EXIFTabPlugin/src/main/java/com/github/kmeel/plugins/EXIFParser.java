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

import com.thebuzzmedia.exiftool.ExifTool;
import com.thebuzzmedia.exiftool.ExifToolBuilder;
import com.thebuzzmedia.exiftool.Tag;
import com.thebuzzmedia.exiftool.core.StandardTag;
import com.thebuzzmedia.exiftool.exceptions.UnsupportedFeatureException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Marten4n6
 *         Parses EXIF data using https://github.com/mjeanroy/exiftool
 */
@Slf4j
class EXIFParser {

    private ExifTool exifTool;

    EXIFParser() {
        try {
            exifTool = new ExifToolBuilder().enableStayOpen().build();
        } catch (UnsupportedFeatureException ex) {
            log.warn("Falling back to simple ExifTool instance.");
            log.warn("enableStayOpen failed since version is too old.");
            exifTool = new ExifToolBuilder().build();
        }
    }

    Map<Tag, String> parse(File image) {
        try {
            return exifTool.getImageMeta(image, Arrays.asList(StandardTag.values()));
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return null;
        }
    }
}