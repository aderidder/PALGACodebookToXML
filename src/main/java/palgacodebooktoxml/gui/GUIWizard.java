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

package palgacodebooktoxml.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.controlsfx.control.CheckComboBox;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.tools.ValueExtractor;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import palgacodebooktoxml.settings.RunParameters;
import palgacodebooktoxml.settings.Statics;

//import javax.xml.soap.Text;
import java.io.File;
import java.util.*;
import java.util.function.Predicate;


/**
 * class for the GUI GUIWizard
 */
class GUIWizard {
    private static final Logger logger = LogManager.getLogger(GUIWizard.class.getName());

    private static final int wizardWidth = 600;
    private static final int wizardHeight = 300;
    private RunParameters runParameters;
    private boolean canRun = false;

    private WizardFlow wizardFlow;

    /**
     * retrieve the value of something from the wizard settings map
     * @param wizardSettings    map with the wizardsettings
     * @param setting           the setting for which we want the value
     * @return the string value of the setting
     */
    private static String getStringSetting(Map<String, Object> wizardSettings, String setting){
        if(wizardSettings.containsKey(setting)){
            return (String) wizardSettings.get((setting));
        }
        return "";
    }

    /**
     * add a tooltip to an item
     * @param control  the item to which to add the tooltip
     * @param helpText the text to show
     */
    private static void addTooltip(Control control, String helpText){
        Tooltip tooltip = new Tooltip(helpText);
        control.setTooltip(tooltip);
    }


    /**
     * Constructor
     */
    GUIWizard(){
    }

    /**
     * create the wizard
     * parameters from the previous run are used to set some values, such as the filenames
     * @param oldParameters    the parameters used in the previous run
     * @return true/false, whether the wizard was successfully completed
     */
    boolean startWizard(RunParameters oldParameters) {
        canRun = false;
        // create the pages
        Wizard wizard = new Wizard();
        WizardPane summaryPage = createSummaryPage();
        wizardFlow = new WizardFlow(summaryPage);

        WizardPane page1 = createPage1(oldParameters);
        wizardFlow.addPage(page1);

        wizard.setFlow(wizardFlow);

        // show wizard and wait for response
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                canRun = true;
            }
            else if(result == ButtonType.CANCEL) {
                logger.log(Level.INFO, "Cancel button pressed...");
            }
        });
        return canRun;
    }

    /**
     * returns the runparameters which were created during the wizard
     * @return the runparameters
     */
    RunParameters getRunParameters(){
        return runParameters;
    }

    /**
     * set the initial browsing directory to the previous directory if possible
     * @param textField    textfield may already contain a directory or file
     * @return the File to which to set the initial directory
     */
    private File getInitialDirectory(TextField textField) {
        String curContent = textField.getText();
        if (!curContent.equalsIgnoreCase("")) {
            File file = new File(curContent);
            if (file.isDirectory()) {
                return file;
            } else {
                return file.getParentFile();
            }
        }
        return null;
    }

    /**
     * create a directorychooser and set the textfield to the selected value
     * @param textField    the textfield which may contain a previous value and will contain the selected value
     */
    private void browseDir(TextField textField){
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(getInitialDirectory(textField));
            directoryChooser.setTitle("Select directory");
            File selectedDirectory = directoryChooser.showDialog(null);
            if (selectedDirectory != null) {
                textField.setText(selectedDirectory.getCanonicalPath()+File.separator);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * creates a row with a label, a textfield and a button to browse for a file
     * @param gridPane the pane to which to add the elements
     * @param id       the id to use for the textField
     * @param label    the text to use for the label
     * @param oldVal   the old value of the textField
     * @param row      the row number, used by the gridPane
     * @return the generated textField
     */
    private TextField createBrowseDirRow(GridPane gridPane, String id, String label, String oldVal, int row){
        // add label and textfield
        gridPane.add(new Label(label), 0, row);
        TextField textField = createTextField(id, oldVal);
        gridPane.add(textField, 1, row);

        // add browse button
        addBrowseButton(gridPane, id, row, event -> browseDir(textField));
        return textField;
    }

    /**
     * create a button with functionality
     * @param gridPane      the gridpane to which the items will be added
     * @param id            base id of the new items
     * @param row           row number
     * @param eventHandler  eventhandler which will be called when the button is clicked
     */
    private void addBrowseButton(GridPane gridPane, String id, int row, EventHandler eventHandler){
        Button browseButton = new Button("Browse");
        browseButton.setId(id+"Button");
        browseButton.setOnAction(eventHandler);
        gridPane.add(browseButton, 2, row);
    }

    /**
     * create the first wizard page
     * @param oldParameters    previous run parameters
     * @return the wizard page
     */
    private WizardPane createPage1(RunParameters oldParameters){
        return new WizardPane() {

            private TextField codebookDirectory;
            private TextField projecIdTextField;
            private TextField projecPrefixTextField;
            private TextField nrLanguagesSelectedTextField;
            private ComboBox <String> experimentalComboBox;
            private ComboBox <String> statusCodeComboBox;
            private final ComboBox <String> defaultLanguageComboBox = createComboBox("defaultLanguage", FXCollections.observableArrayList());
            private final TextArea authorsArea;
            private final TextArea copyrightArea;
            private CheckComboBox<String> languageCheckComboBox;

            private final ValidationSupport validationSupport = new ValidationSupport();

            private final Map<String, WizardPane> languagePageMap = new HashMap<>();

            private Predicate isNumberPredicate = o -> Integer.valueOf(nrLanguagesSelectedTextField.getText())>0;

            {
                this.setId("A");
                this.getStylesheets().clear();
                this.setPrefWidth(wizardWidth);
                this.setPrefHeight(wizardHeight);
                this.setHeaderText("Page 1");
                createLanguagesCheckComboBox();
                authorsArea = createTextArea("authorsArea", "Each line should contain: userid;email address;name");
                copyrightArea = createTextArea("copyrightArea", "Each line should contain: institute;year(s);author/contributor/reviewer");

//                copyright by="NKI and VUmc " years="2016 2017 2018 2019" type="author"
                createContent();
            }

            /**
             * create the content of this page
             */
            private void createContent(){
                // create the gridpane and add the textfields, buttons and labels
                int rowNum = 0;
                GridPane gridPane = createGridPane();
                codebookDirectory = createBrowseDirRow(gridPane, "codebookDirectory", "Codebook directory:", oldParameters.getCodebookDirectory(), rowNum);

                projecIdTextField = createTextField("projectId", oldParameters.getProjectId());
                gridPane.add(new Label("Project Id:"),0,++rowNum);
                gridPane.add(projecIdTextField,1,rowNum);
                addTooltip(projecIdTextField, "Provided by Art-Decor. E.g. 2.16.9999.1.113883.2.4.3.11.9999.902");

                projecPrefixTextField = createTextField("projectPrefix", oldParameters.getProjectPrefix());
                gridPane.add(new Label("Project Prefix:"),0,++rowNum);
                gridPane.add(projecPrefixTextField,1,rowNum);
                addTooltip(projecPrefixTextField, "Provided by Art-Decor. E.g. cce-");

                rowNum = addLanguageRow(gridPane, rowNum);

                experimentalComboBox = createComboBox("experimental", FXCollections.observableArrayList("true", "false"));
                gridPane.add(new Label("Experimental:"),0,++rowNum);
                gridPane.add(experimentalComboBox,1,rowNum);
                experimentalComboBox.setValue(oldParameters.getExperimental());

                statusCodeComboBox = createComboBox("statusCode", FXCollections.observableArrayList("draft", "final"));
                gridPane.add(new Label("Status code:"),0,++rowNum);
                gridPane.add(statusCodeComboBox,1,rowNum);
                statusCodeComboBox.setValue(oldParameters.getStatusCode());

                // create and add the authors text area
                gridPane.add(new Label("Authors:"),0,++rowNum);
                gridPane.add(authorsArea,1,rowNum);
                authorsArea.setText(oldParameters.getAuthorString());

                // create and add the authors text area
                gridPane.add(new Label("Copyright/Contributors:"),0,++rowNum);
                gridPane.add(copyrightArea,1,rowNum);
                copyrightArea.setText(oldParameters.getCopyrightString());


                // create and add the clearbutton
                gridPane.add(createClearButton(), 1, ++rowNum);

                // set the content
                this.setContent(gridPane);

                addValidation();
            }

            /**
             * add validations
             */
            private void addValidation(){
                // add validation
                validationSupport.initInitialDecoration();

                // value extractor to allow the controlsfx to recognise the CheckComboBox
                ValueExtractor.addValueExtractor((c) -> {
                    return c instanceof CheckComboBox;
                }, (checkComboBox) -> {
                    return ((CheckComboBox) checkComboBox).getCheckModel().getCheckedItems();
                });


                // workaround for bug https://bitbucket.org/controlsfx/controlsfx/issues/539/multiple-dialog-fields-with-validation
                Platform.runLater(() -> {
                    validationSupport.registerValidator(codebookDirectory, Validator.createEmptyValidator("Codebook file required"));
                    validationSupport.registerValidator(projecIdTextField, Validator.createEmptyValidator("Project Id required"));
                    validationSupport.registerValidator(projecPrefixTextField, Validator.createEmptyValidator("Project prefix required"));
                    validationSupport.registerValidator(authorsArea, true, authorValidator());
                    validationSupport.registerValidator(copyrightArea, true, copyrightValidator());
                    validationSupport.registerValidator(nrLanguagesSelectedTextField, Validator.createPredicateValidator(isNumberPredicate, "Select at least one language"));

                });

            }

            /**
             * add the row which allows the user to select languages
             * @param gridPane the gridPane
             * @param row      the row number
             * @return the new row number
             */
            private int addLanguageRow(GridPane gridPane, int row){
                gridPane.add(new Label("Select languages"), 0, ++row);
                GridPane smallPane = createGridPane();
                smallPane.add(languageCheckComboBox, 0, 0);
                smallPane.add(new Label("#selected:"), 1, 0);
                nrLanguagesSelectedTextField = createTextField("nrLanguagesSelected", "0");
                GridPane.setHgrow(nrLanguagesSelectedTextField, Priority.NEVER);
                nrLanguagesSelectedTextField.setPrefWidth(30);
                nrLanguagesSelectedTextField.setEditable(false);
                smallPane.add(nrLanguagesSelectedTextField, 2, 0);

                smallPane.add(new Label("Default Language"), 3, 0);
                smallPane.add(defaultLanguageComboBox, 4, 0);

                gridPane.add(smallPane, 1, row);

                Set<String> languages = oldParameters.getLanguages();
                for(String language:languages) {
                    languageCheckComboBox.getCheckModel().check(language);
                }

                defaultLanguageComboBox.getSelectionModel().select(oldParameters.getDefaultLanguage());
                return row;
            }

            /**
             * add event listener to the languageCheckComboBox which updates the nrLanguageTextfield as well as calls the setupLanguagePages
             */
            private void addTargetListEventListener(){
                ListChangeListener<String> listChangeListener = c -> {
                    nrLanguagesSelectedTextField.setText(String.valueOf(c.getList().size()));

                    // find the currently selected default language
                    String selectedDefaultLanguage = defaultLanguageComboBox.getSelectionModel().getSelectedItem();
                    // update the default languages combobox
                    ObservableList<String> checkedItems = languageCheckComboBox.getCheckModel().getCheckedItems();
                    defaultLanguageComboBox.setItems(FXCollections.observableArrayList(checkedItems));

                    // restore the previously selected default language if possible
                    if(defaultLanguageComboBox.getItems().contains(selectedDefaultLanguage)){
                        defaultLanguageComboBox.getSelectionModel().select(selectedDefaultLanguage);
                    }
                    else{
                        defaultLanguageComboBox.getSelectionModel().selectFirst();
                    }

                    setupLanguagePages();
                };

                // add the listener to the checkItems
                languageCheckComboBox.getCheckModel().getCheckedItems().addListener(listChangeListener);

            }

            /**
             * adds a checkcombobox which allows a user to select languages
             */
            private void createLanguagesCheckComboBox(){
                ObservableList<String> languages = FXCollections.observableArrayList(Statics.getLanguages());
                languageCheckComboBox = new CheckComboBox<>(languages);
                languageCheckComboBox.setId("languages");

                addTargetListEventListener();
            }

            /**
             * create the authors area
             */
            private TextArea createTextArea(String id, String tooptipText){
                TextArea textArea = new TextArea();
                textArea.setId(id);
                textArea.setPrefSize(375, 75);
                textArea.setWrapText(true);
                addTooltip(textArea, tooptipText);
                return textArea;
            }

            /**
             * create a clear button
             * @return the clear button
             */
            private Button createClearButton(){
                Button clearButton = new Button("Clear all");
                clearButton.setId("clearAll");
                clearButton.setOnAction(event -> {
                    codebookDirectory.clear();
                    projecIdTextField.clear();
                    projecPrefixTextField.clear();
                    languageCheckComboBox.getCheckModel().clearChecks();
                    authorsArea.clear();
                });
                return clearButton;
            }

            /**
             * create a validator for the authors area
             * the validator expects 3 texts separated by ; symbols per line
             * @return a validator for the authors area
             */
            private Validator<String> authorValidator(){
                return (control, value) -> {
                    boolean condition=false;
                    // multiple lines possible
                    String [] splitString = value.split("\\n");
                    // each line has to validate
                    for(String line:splitString){
                        if(line.split(";",-1).length!=3 || line.startsWith(";") || line.endsWith(";")){
                            // if there is an issue with the line
                            condition = true;
                            break;
                        }
                    }
                    return ValidationResult.fromMessageIf(control, "the author line is not correct", Severity.ERROR, condition);
                };
            }

            private Validator<String> copyrightValidator(){
                return (control, value) -> {
                    boolean condition=false;
                    // multiple lines possible
                    String [] splitString = value.split("\\n");
                    // each line has to validate
                    for(String line:splitString){
                        // validate whether the line has the expected number of columns (based on ;)
                        if(line.split(";",-1).length!=3 || line.startsWith(";") || line.endsWith(";")){
                            // if there is an issue with the line
                            condition = true;
                            break;
                        }
                        // validate whether the line's third argument is in the list
                        String [] splitLine = line.split(";");
                        String contributorType = splitLine[2].trim().toLowerCase();
                        List<String> optionList = Arrays.asList("author", "contributor", "reviewer");
                        if(!optionList.contains(contributorType)){
                            condition = true;
                            break;
                        }
                    }
                    return ValidationResult.fromMessageIf(control, "the author line is not correct", Severity.ERROR, condition);
                };
            }

            /**
             * adds/removes language pages from the wizard flow
             */
            private void setupLanguagePages(){
                List<String> selected = languageCheckComboBox.getCheckModel().getCheckedItems();
                // user iterator to loop the hashmap values and delete (otherwise concurrent exception will occur)
                for(Iterator<String>it=languagePageMap.keySet().iterator();it.hasNext();){
                    String existingPage = it.next();
                    if(!selected.contains(existingPage)){
                        wizardFlow.removeLanguagePage(languagePageMap.get(existingPage));
                        it.remove();
                    }
                }
                for(String language:selected){
                    if(!languagePageMap.containsKey(language)){
                        WizardPane wizardPane = createLanguagePage(language, oldParameters);
                        languagePageMap.put(language, wizardPane);
                        wizardFlow.addPage(wizardPane);
                    }
                }
            }

            /**
             * things to do when we enter the page
             * @param wizard    the wizard
             */
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizard.invalidProperty().unbind();
                wizard.invalidProperty().bind(validationSupport.invalidProperty());
            }

            /**
             * things to do when we leave the page
             * @param wizard    the wizard
             */
            @Override
            public void onExitingPage(Wizard wizard){

            }
        };
    }

    /**
     * create a language page
     * @param language      the language
     * @param oldParameters previous parameters
     * @return the wizard pane that was created
     */
    private WizardPane createLanguagePage(String language, RunParameters oldParameters){
        return new WizardPane() {
            private TextField projectDescription;
            private TextField projectName;

            private final ValidationSupport validationSupport = new ValidationSupport();

            {
                setId(language);
                this.getStylesheets().clear();
                this.setPrefWidth(wizardWidth);
                this.setPrefHeight(wizardHeight);
                this.setHeaderText("Language settings for "+language);
                createContent();
            }

            /**
             * create the content for the page
             */
            private void createContent(){
                int row = 0;
                GridPane gridPane = createGridPane();

                projectName = createTextField("projectName"+language, oldParameters.getProjectName(language));
                gridPane.add(new Label("Project Name in "+language), 0, ++row);
                gridPane.add(projectName, 1, row);
                addTooltip(projectName, "Name of the project in "+language);

                projectDescription = createTextField("projectDescription"+language, oldParameters.getProjectDescription(language));
                gridPane.add(new Label("Project Description in "+language), 0, ++row);
                gridPane.add(projectDescription, 1, row);
                addTooltip(projectDescription, "Description of the project in "+language);

                Button clearButton = new Button("Clear all");
                clearButton.setId("clearAll");
                gridPane.add(clearButton, 1, ++row);

                clearButton.setOnAction(event -> {
                    projectName.clear();
                    projectDescription.clear();
                });

                // set the content
                this.setContent(gridPane);

                // add validation
                validationSupport.initInitialDecoration();

                validationSupport.registerValidator(projectName, Validator.createEmptyValidator("Project name in the language is required"));
                validationSupport.registerValidator(projectDescription, Validator.createEmptyValidator("Project description in the language is required"));
            }

            /**
             * things to do when we enter the page
             * @param wizard the wizard
             */
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizard.invalidProperty().unbind();
                wizard.invalidProperty().bind(validationSupport.invalidProperty());
            }

            /**
             * things to do when we leave the page
             * @param wizard the wizard
             */
            @Override
            public void onExitingPage(Wizard wizard) {

            }
        };

    }

    /**
     * create the summary page
     * @return the summary page
     */
    private WizardPane createSummaryPage(){
        return new WizardPane(){
            {
                // zet the id to Z ensuring the summary page will be the last page and other pages will be inserted before this
                setId("Z");
                this.getStylesheets().clear();
                this.setPrefWidth(wizardWidth);
                this.setPrefHeight(wizardHeight);
                this.setHeaderText("Summary");

            }

            /**
             * create the summary text
             */
            private void generateContentText(){
                String content = "An art-decoc xml file will be created for the codebooks found in "+runParameters.getCodebookDirectory()+" \n";
                content += "The output will be written to: "+runParameters.getOutputFile();
                this.setContentText(content);
            }

            /**
             * collect the necessary information for the runparameters
             * @param wizard the wizard
             */
            private void createRunParameters(Wizard wizard){
                String codebookDirectory = getStringSetting(wizard.getSettings(), "codebookDirectory");
                String projectId = getStringSetting(wizard.getSettings(), "projectId");
                String projectPrefix = getStringSetting(wizard.getSettings(), "projectPrefix");
                String experimental = getStringSetting(wizard.getSettings(), "experimental");
                String authorString = getStringSetting(wizard.getSettings(), "authorsArea");
                String copyrightString = getStringSetting(wizard.getSettings(), "copyrightArea");
                String statusCode =  getStringSetting(wizard.getSettings(), "statusCode");
                runParameters = new RunParameters(codebookDirectory, projectId, projectPrefix, experimental, authorString, copyrightString, statusCode);
                addLanguageParameters(wizard, runParameters);
            }

            /**
             * adds the Project name and Project language as specified in the frontend for each language to the
             * runparameters
             * @param wizard        the wizard
             * @param runParameters the runparameters
             */
            private void addLanguageParameters(Wizard wizard, RunParameters runParameters){
                List<String> languages = (List<String>) wizard.getSettings().get("languages");
                for(String language:languages){
                    String languageName = getStringSetting(wizard.getSettings(), "projectName"+language);
                    String languageDescription = getStringSetting(wizard.getSettings(), "projectDescription"+language);
                    runParameters.addLanguageSettings(language, languageDescription, languageName);
                }
                runParameters.setDefaultLanguage(getStringSetting(wizard.getSettings(), "defaultLanguage"));
            }

            /**
             * things to do when we enter the page
             * @param wizard the wizard
             */
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizard.invalidProperty().unbind();
                createRunParameters(wizard);
                generateContentText();
            }
        };
    }


    /**
     * creates grid pane
     * @return grid pane
     */
    private GridPane createGridPane(){
        GridPane pageGrid = new GridPane();
        pageGrid.setVgap(10);
        pageGrid.setHgap(10);

        return pageGrid;
    }

    /**
     * creates standard textfield
     * @param id          id for the textfield
     * @param initialText contents for the textfield
     * @return the new textfield
     */
    private TextField createTextField(String id, String initialText) {
        TextField textField = new TextField();
        textField.setId(id);
        textField.setText(initialText);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }

    /**
     * creates standard combobox
     * @param id id for the textfield
     * @return the new combobox
     */
    private ComboBox<String> createComboBox(String id, ObservableList <String> itemList) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId(id);
        comboBox.setItems(itemList);
        GridPane.setHgrow(comboBox, Priority.ALWAYS);
        return comboBox;
    }


    /**
     * create a different type of wizard flow that will allow us to dynamically add pages
     * the idea is that if different/more/fewer languages are selected, the user can add the
     * necessary information on newly generated pages
     */
    static class WizardFlow implements Wizard.Flow {
        private final List<WizardPane> pageList = new ArrayList<>();

        /**
         * create a new wizardflow. Add a page
         * @param wizardPane
         */
        WizardFlow(WizardPane wizardPane){
            pageList.add(wizardPane);
        }

        /**
         * add a new page if the page doesn't exist yet
         * @param wizardPane the page to add
         */
        void addPage(WizardPane wizardPane){
            if(!pageList.contains(wizardPane)){
                for (int i = 0; i < pageList.size(); i++) {
                    // check where the page should be inserted based on the page of the id
                    // this way we basically make the pages alphabetically for the languages
                    if (wizardPane.getId().compareToIgnoreCase(pageList.get(i).getId()) < 0) {
                        pageList.add(i, wizardPane);
                        break;
                    }
                }
            }
        }

        /**
         * remove a language page. This ensures the page will no longer show up in the wizard if it
         * it is not selected by the user
         * @param wizardPane the page to remove
         */
        void removeLanguagePage(WizardPane wizardPane){
            if(pageList.contains(wizardPane)){
                pageList.remove(wizardPane);
            }
        }

        /**
         * Find the WizardPane to advance to
         * @param currentPage current WizardPane
         * @return WizardPane to advace to
         */
        @Override
        public Optional<WizardPane> advance(WizardPane currentPage) {
            int pageIndex = this.pageList.indexOf(currentPage);
            ++pageIndex;
            return Optional.ofNullable(this.pageList.get(pageIndex));
        }

        /**
         * checks whether it is possible to advance
         * @param currentPage current WizardPane
         * @return true/false
         */
        @Override
        public boolean canAdvance(WizardPane currentPage) {
            int pageIndex = this.pageList.indexOf(currentPage);
            return this.pageList.size() - 1 > pageIndex;
        }
    }
}