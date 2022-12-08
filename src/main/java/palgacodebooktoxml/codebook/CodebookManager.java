/*
 * Copyright 2017 NKI/AvL; VUmc 2018/2019/2020
 *
 * This file is part of PALGA Protocol Codebook to XML.
 *
 * PALGA Protocol Codebook to XML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PALGA Protocol Codebook to XML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PALGA Protocol Codebook to XML. If not, see <http://www.gnu.org/licenses/>
 */

package palgacodebooktoxml.codebook;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import palgacodebooktoxml.settings.RunParameters;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Codebook manager
 * builds the codebooks and provides access to them
 */
public class CodebookManager {
    private static final Logger logger = LogManager.getLogger(CodebookManager.class.getName());
//    private Map<Integer, Codebook> codebookMap = new TreeMap<>();
    private final Map<Double, Codebook> codebookMap = new TreeMap<>();

    private CodebookManager(){}

    /**
     * read the excel codebooks found in a directory (specifief in the runparameters)
     * @param runParameters    parameters used for this run
     * @return the codebookmanager which can be used to access the codebooks
     * @throws IOException
     * @throws InvalidFormatException
     */
    public static CodebookManager readCodebooks(RunParameters runParameters) throws IOException, InvalidFormatException {
        CodebookManager codebookManager = new CodebookManager();
        String codebookDirectory = runParameters.getCodebookDirectory();

        // read all files in the directory
        Path dir = FileSystems.getDefault().getPath(codebookDirectory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file: stream) {
                String fileName = file.getFileName().toString();
                // if the file is a proper excel file, create a codebook for it
                if(fileName.endsWith(".xlsx") && !(fileName.startsWith("~"))) {
                    logger.log(Level.INFO, "Reading codebook: {}", file.getFileName());
//                    System.out.println(file.getFileName());
                    Codebook codebook = Codebook.readExcel(file, runParameters);
                    codebookManager.addCodebook(codebook);
                }
            }
        }
        return codebookManager;
    }

    /**
     * store a codebook in the codebook map
     * @param codebook the codebook to store
     */
    private void addCodebook(Codebook codebook){
        codebookMap.put(codebook.getDatasetVersionLabel(), codebook);
    }

    /**
     * get which versions exist of a the codebook (ordered, smallest first)
     * @return set with all versions
     */
//    Set<Integer> getCodebookVersions(){
//        return codebookMap.keySet();
//    }
    Set<Double> getCodebookVersions(){
        return codebookMap.keySet();
    }

    /**
     * get the codebook of a version
     * @param version    the version for which to retrieve the codebook
     * @return the codebook
     */
//    Codebook getCodebook(Integer version){
    Codebook getCodebook(Double version){
        return codebookMap.get(version);
    }


}
