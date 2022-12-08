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

import palgacodebooktoxml.artdecor.ArtDecorDataset;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import palgacodebooktoxml.settings.RunParameters;
import palgacodebooktoxml.settings.Statics;
import palgacodebooktoxml.utils.ExcelUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class for the Excel Codebook
 */
class Codebook {
    private static final Logger logger = LogManager.getLogger(Codebook.class.getName());
    private static final SimpleDateFormat parseFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");
    private Date effectiveDateAsDate;
    private String effectiveDate;

    private String datasetVersionLabel="";
    private final Map<String, CodebookLanguageParameters> codebookLanguageParametersMap = new HashMap<>();

    private final RunParameters runParameters;
    private final Map<String, Concept> conceptMap = new LinkedHashMap<>();
    private List<String> headerList;


    private Codebook(RunParameters runParameters){
        this.runParameters = runParameters;
    }

    /**
     * reads an Excel codebook and turns it into a Codebook object
     * @param path          the Excel codebook file
     * @param runParameters the runparameters
     * @return the newly created codebook
     * @throws IOException
     * @throws InvalidFormatException
     */
    static Codebook readExcel(Path path, RunParameters runParameters) throws IOException, InvalidFormatException {
        Codebook codebook = new Codebook(runParameters);
        try (Workbook workbook = WorkbookFactory.create(path.toFile())) {
            parseInfoSheet(codebook, workbook, runParameters);
            parseMainSheet(codebook, workbook);
        }
        return codebook;
    }

    /**
     * parse the info sheet of the Excel file and add the necessary information to the codebook
     * @param codebook      the codebook
     * @param workbook      the excel codebook
     * @param runParameters the runparamters
     */
    private static void parseInfoSheet(Codebook codebook, Workbook workbook, RunParameters runParameters){
        Sheet sheet = workbook.getSheet("Info");
        if(sheet==null) throw new RuntimeException("Info sheet missing...");

        // create a map for the variables and their values from the info sheet
        Map<String, String> valueMap = createValueMap(sheet);
        codebook.datasetVersionLabel = valueMap.get("version");
        codebook.setEffectiveDate(valueMap);

        // check which language were selected in the runparameters
        // each language should have a description and name in the specified language in the info sheet
        // add these to the codebook
        Set<String> languageList = runParameters.getLanguages();
        for(String language:languageList) {
            String datasetDescription = valueMap.get("DatasetDescription_"+language);
            String datasetName  = valueMap.get("DatasetDescription_"+language);
            codebook.addLanguageSetting(language, datasetDescription, datasetName);
        }
    }

    /**
     * The info sheet contains information in the following form:
     * col1 col2
     * key  value
     * e.g.
     * Version	                33
     * DatasetName_nl	        PALGA colonbiopt protocol versie 33
     * DatasetDescription_nl	Versie 33 van het PALGA colonbiopt protocol
     * This is turned into a map.
     * @param sheet the info sheet
     * @return a map with the variables in the info sheet and their values
     */
    private static Map<String, String> createValueMap(Sheet sheet){
        Map<String, String> valueMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        int lastRowNr = sheet.getLastRowNum();
        for(int i=0; i<=lastRowNr; i++){
            Row row = sheet.getRow(i);
            // if the row exists, add the information in the row to our excelCodebook
            if(row!=null) {
                String key = ExcelUtils.getCellValue(row, 0);
                String value = ExcelUtils.getCellValue(row, 1);
                valueMap.put(key, value);
            }
        }
        return valueMap;
    }

    /**
     * parse the main sheet
     * @param codebook the codebook
     * @param workbook the Excel codebook
     */
    private static void parseMainSheet(Codebook codebook, Workbook workbook){
        // head to the main sheet of the codebook
        Sheet sheet = workbook.getSheet("Codebook");

        // first row contains the header.
        Row row = sheet.getRow(0);
        codebook.addHeader(row);

        // iterate over the rest of the rows
        int lastRowNr = sheet.getLastRowNum();
        for(int i=1; i<=lastRowNr; i++){
            row = sheet.getRow(i);
            // if the row exists, add the information in the row to our excelCodebook
            if (row != null && !ExcelUtils.isEmptyRow(row)) {
                codebook.addData(workbook, row);
            }
        }
    }

    /**
     * set this codebook's effectiveDate.
     * @param valueMap the parameter map
     */
    private void setEffectiveDate(Map<String, String> valueMap){
        if(valueMap.containsKey("effectiveDate")) {
            try {
                effectiveDateAsDate = parseFormat.parse(valueMap.get("effectivedate"));
                effectiveDate = outFormat.format(effectiveDateAsDate);

            } catch (ParseException e) {
                logger.log(Level.ERROR, "codebook version: {}; Severe Error: The effective date is not in the correct format {}", datasetVersionLabel, valueMap.get("effectivedate"));
                try{
                    effectiveDateAsDate = parseFormat.parse("1900-01-01");
                    effectiveDate = outFormat.format(effectiveDateAsDate);
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
        }
        else{
            logger.log(Level.WARN, "codebook version: {}; Warning: The Effectivedate is not available in the INFO sheet (yyyy-mm-dd). Setting it to today... ", datasetVersionLabel);
            effectiveDateAsDate = new Date();
            effectiveDate = outFormat.format(effectiveDateAsDate);
        }
    }

    /**
     * returns a collection with this codebook's concepts
     * @return a collection with this codebook's concepts
     */
    Collection<Concept> getAllConcepts(){
        return conceptMap.values();
    }

    /**
     * Store a language's specific settings (dataset description and dataset name)
     * @param language           the language
     * @param datasetDescription the description of the dataset in that language
     * @param datasetName        the name of the dataset in that language
     */
    private void addLanguageSetting(String language, String datasetDescription, String datasetName){
        CodebookLanguageParameters codebookLanguageParameters = new CodebookLanguageParameters(datasetDescription, datasetName);
        codebookLanguageParametersMap.put(language, codebookLanguageParameters);
    }

    /**
     * store the header of the Excel's codebook
     * @param row row containing the header
     */
    private void addHeader(Row row){
        headerList = ExcelUtils.getRowAsList(row);
    }

    /**
     * validation whether whether there are issues with the values provided for a concept
     * @param id               the concept's id
     * @param codesystem       the concept's codesystem
     * @param code             the concept's code
     * @param description_code the concept's code description
     * @return true/false
     */
    private boolean isValidEntry(String id, String codesystem, String code, String description_code){
        boolean isValid=true;
        if(conceptMap.containsKey(id)){
            logger.log(Level.ERROR, "codebook version: {}; Concept: The identifier in the codebook must be unique {}", datasetVersionLabel, id);
            isValid = false;
        }
        if(Statics.mayBeTypo(codesystem)){
            logger.log(Level.WARN, "codebook version: {}; Concept: Codesystem found: {} for {}. Did you mean {}?", datasetVersionLabel, codesystem, id, Statics.getTypoValue(codesystem));
        }
        if(code.equalsIgnoreCase("")){
            logger.log(Level.ERROR, "codebook version: {}; Concept: Mandatory code missing for concept {}", datasetVersionLabel, id);
            isValid = false;
        }
        if(codesystem.equalsIgnoreCase("")){
            logger.log(Level.ERROR, "codebook version: {}; Concept: Mandatory codesystem missing for concept {}", datasetVersionLabel, id);
            isValid = false;
        }
        if(description_code.equalsIgnoreCase("")){
            logger.log(Level.ERROR, "codebook version: {}; Concept: Mandatory code description missing for concept {}", datasetVersionLabel, id);
            isValid = false;
        }
        return isValid;
    }

    /**
     * create a codebook item for the row
     * @param workbook the Excel codebook
     * @param row      the row we're looking at
     */
    private void addData(Workbook workbook, Row row){
        String id = ExcelUtils.getValue(row, "id", headerList);
        String codesystem = ExcelUtils.getValue(row, "codesystem", headerList);
        String code = ExcelUtils.getValue(row, "code", headerList);
        String description_code = ExcelUtils.getValue(row, "description_code", headerList);
        String codelist_ref = ExcelUtils.getValue(row, "codelist_ref", headerList);
        String properties =  ExcelUtils.getValue(row, "properties", headerList);
        String parent = ExcelUtils.getValue(row, "parent", headerList);
        String data_type = ExcelUtils.getValue(row, "data_type", headerList);

        // If the concept itself is invalid, we basically stop for this entry. This also implies that any errors made
        // in the concept's codelist will not be shown until the concept itself is fixed.
        if(isValidEntry(id, codesystem, code, description_code)) {
            Concept concept = new Concept(id, codesystem, code, description_code, properties, codelist_ref, parent, data_type, effectiveDate, datasetVersionLabel, runParameters.getStatusCode());

            // get the description in the available languages
            Set<String> languages = runParameters.getLanguages();
            for (String language : languages) {
                String languageDescription = ExcelUtils.getValue(row, "description_" + language, headerList);
                concept.addLanguageConcept(language, languageDescription);
            }

            conceptMap.put(id, concept);

            // if the codebook item has a codelist add it as well
            if (!codelist_ref.equalsIgnoreCase("")) {
                addCodeList(workbook, concept, codelist_ref);
            }
        }
    }

    /**
     * add a codelist to the concept
     * @param workbook     the Excel codebook
     * @param concept      the concept
     * @param codelist_ref the codelist reference we're looking for
     */
    private void addCodeList(Workbook workbook, Concept concept, String codelist_ref) {
        try {
            Sheet sheet = workbook.getSheet(codelist_ref);

            // retrieve the header of the sheet
            Row row = sheet.getRow(0);
            List<String> codelistHeaderList = ExcelUtils.getRowAsList(row);

            // parse the remaining rows
            int lastRowNr = sheet.getLastRowNum();
            for (int i = 1; i <= lastRowNr; i++) {
                row = sheet.getRow(i);
                // if the row exists, add the information in the row to our excelCodebook
                if (row != null && !ExcelUtils.isEmptyRow(row)) {
                    concept.addCodeListEntry(row, codelistHeaderList, runParameters.getLanguages(), codelist_ref);
//                    addCodeListEntry(concept, row, codelistHeaderList);
                }
            }
        } catch (NullPointerException e){
            logger.log(Level.ERROR, "codebook version: {}; Severe Error: Issue adding codelist, ref = {}", datasetVersionLabel, codelist_ref);
        }
    }

    /**
     * returns the version number
     * @return the version number
     */
//    int getDatasetVersionLabel() {
//        try {
//            return Integer.parseInt(datasetVersionLabel);
//        } catch (Exception e){
//            logger.log(Level.ERROR, "Only integer values are supported as version number at the moment.");
//            return 0;
//        }
//    }
    double getDatasetVersionLabel() {
        try {
            return Double.parseDouble(datasetVersionLabel);
        } catch (Exception e){
            logger.log(Level.ERROR, "Only numbers are supported as version labels");
            return 0;
        }
    }

    /**
     * creates the ArtDecor Dataset for this Codebook and adds the language parameters
     * @param artdecorDatasetId identifier for the ART-DECOR dataset
     * @return the newly created ArtDecorDataset
     */
    ArtDecorDataset createArtDecorDataset(String artdecorDatasetId){
        ArtDecorDataset artDecorDataset = new ArtDecorDataset(artdecorDatasetId, effectiveDate, getDatasetVersionLabel(), runParameters.getStatusCode());
        for(Map.Entry<String, CodebookLanguageParameters> entrySet:codebookLanguageParametersMap.entrySet()) {
            CodebookLanguageParameters codebookLanguageParameters = entrySet.getValue();
            artDecorDataset.addLanguageParameter(entrySet.getKey(), codebookLanguageParameters.datasetName, codebookLanguageParameters.datasetDescription);
        }
        return artDecorDataset;
    }

    /**
     * returns a Date representation of the effectiveDate String
     * @return date representation of the effective date
     */
    Date getEffectiveDateAsDate() {
        return effectiveDateAsDate;
    }

    private class CodebookLanguageParameters{
        private String datasetDescription="";
        private String datasetName="";

        CodebookLanguageParameters(String datasetDescription, String datasetName){
            this.datasetDescription = datasetDescription;
            this.datasetName = datasetName;
        }
    }

}
