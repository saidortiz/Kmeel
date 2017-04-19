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

import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import com.github.kmeel.api.utils.OSUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * @author Marten4n6
 *         This class handles indexing
 *         (Required before using the Searcher)
 */
@Slf4j
public class Indexer {

    private String caseName;
    private IndexWriter indexWriter;

    public Indexer(String caseName) {
        this.caseName = caseName;
    }

    public @Synchronized IndexWriter getIndexWriter() {
        if (indexWriter == null) {
            try {
                Directory indexDirectory = FSDirectory.open(new File(OSUtils.getIndexPath(caseName)).toPath());
                IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
                indexWriter = new IndexWriter(indexDirectory, config);
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return indexWriter;
    }

    public void close() {
        if (indexWriter != null) {
            try {
                indexWriter.commit();
                indexWriter.close();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
}
