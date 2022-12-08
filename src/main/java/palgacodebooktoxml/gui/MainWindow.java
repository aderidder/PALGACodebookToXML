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

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import palgacodebooktoxml.codebook.CodebookManager;
import palgacodebooktoxml.codebook.CodebookToArtDecorConvertor;
import palgacodebooktoxml.gui.resourcemanagement.ResourceManager;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import palgacodebooktoxml.settings.IdentifierManager;
import palgacodebooktoxml.settings.RunParameters;
import palgacodebooktoxml.utils.TextAreaAppender;

/**
 * Starting point for the software
 * Creates the main window
 *
 * Note to self: The XML uses only the descriptions (and the ontology code-things)
 * The Values column does still need to be present for the parser to work…
 */
public class MainWindow {
    private static final Logger logger = LogManager.getLogger(MainWindow.class.getName());
    private static final ResourceManager resourceManager = new ResourceManager();

    private static final int sceneWidth = 800;
    private static final int sceneHeight = 500;

    private TextArea logArea;

    private RunParameters runParameters=getDefaultParameters();

    private static final boolean debug = true;

    /**
     * Create the main GUI window
     * @param primaryStage the primary stage
     */
    public void createMainWindow(Stage primaryStage) {
        primaryStage.setTitle("PALGA Protocol Codebook to XML");

        // create the components
        Node topPane = setupTopPane();
        Node centerPane = setupCenterPane();
        Node bottomPane = setupBottomPane();

        // add them to a borderpane
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topPane);
        borderPane.setCenter(centerPane);
        borderPane.setBottom(bottomPane);

        // create the scene, set the scene
        Scene scene = new Scene(borderPane, sceneWidth, sceneHeight);
        primaryStage.setScene(scene);

        // create an icon
        primaryStage.getIcons().add(resourceManager.getResourceImage("icon.png"));
        // add the stylesheet
        scene.getStylesheets().add(resourceManager.getResourceStyleSheet("style.css"));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Creates the top pane
     * @return the top pane
     */
    private Node setupTopPane(){
        BorderPane borderPane = new BorderPane();

        // create first image for the left side
        ImageView healthriImv = new ImageView();
        healthriImv.setFitHeight(80);
        healthriImv.setFitWidth(200);
        healthriImv.setImage(resourceManager.getResourceImage("healthri_transp.png"));

        HBox leftHBox = new HBox();
        leftHBox.setAlignment(Pos.CENTER_LEFT);

        // create the title
        Label sceneTitle = new Label("PALGA Protocol");
        sceneTitle.setId("title");
        Label sceneTitle2 = new Label("Codebook to XML");
        sceneTitle2.setId("title");

        // add title, subtitle and edcComboBox to a vertical box and add the box to the Grid Pane
        VBox vBox = new VBox();
        vBox.getChildren().addAll(sceneTitle, sceneTitle2);
        vBox.setAlignment(Pos.CENTER);

        // create an imageview for RIB image
        ImageView ribImv = new ImageView();
        ribImv.setFitHeight(90);
        ribImv.setFitWidth(90);
        ribImv.setImage(resourceManager.getResourceImage("rib.png"));

        HBox rightHBox = new HBox();
        rightHBox.setPadding(new Insets(0,10,0,0));
        rightHBox.setAlignment(Pos.CENTER_RIGHT);

        // add items to the boxes and give them an equal preferred width for centering
        leftHBox.getChildren().addAll(healthriImv);
        rightHBox.getChildren().addAll(ribImv);
        leftHBox.setPrefWidth(215);
        rightHBox.setPrefWidth(215);

        // give the grid an id and add styleclass
        borderPane.setId("topPane");
        borderPane.getStyleClass().add("fillBackground");

        borderPane.setLeft(leftHBox);
        borderPane.setCenter(vBox);
        borderPane.setRight(rightHBox);
        return borderPane;
    }

    /**
     * Create center pane which contains the log area
     * @return the Node which will be added to the borderpane
     */
    private Node setupCenterPane(){
        // center pane currently contains only logarea, which we add to an hbox an return
        HBox hBox = new HBox();
        hBox.getStyleClass().add("fillBackground");
        createLogArea();
        hBox.getChildren().addAll(logArea);
        hBox.setAlignment(Pos.CENTER);
        return hBox;
    }

    /**
     * create the log area
     */
    private void createLogArea(){
        logArea = new TextArea();
        logArea.setPadding(new Insets(5, 5, 5, 5));
        logArea.setPrefWidth(sceneWidth);
        logArea.setEditable(false);
        logArea.setId("logArea");
        logArea.setText(StaticTexts.getWelcomeText());
        logArea.setWrapText(true);
        TextAreaAppender.setTextArea(logArea);
    }

    /**
     * Create the bottom pane, which contains buttons to e.g. run and exit the program
     *
     * @return the Node which will be added to the borderpane
     */
    private Node setupBottomPane(){
        HBox hBox = new HBox();
        hBox.getStyleClass().add("fillBackground");

        hBox.setPadding(new Insets(20, 12, 20, 12));
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.BASELINE_RIGHT);

        Button buttonClear = new Button("Clear");
        buttonClear.setPrefSize(100, 20);
        buttonClear.setOnAction(event -> logArea.setText(StaticTexts.getWelcomeText()));

        // add some buttons and tell what to do when the button is clicked
        Button buttonRun = new Button("Run");
        buttonRun.setPrefSize(100, 20);
        buttonRun.setOnAction(event -> startTask());

        Button buttonExit = new Button("Exit");
        buttonExit.setPrefSize(100, 20);
        buttonExit.setOnAction(event -> System.exit(0));

        // Give Help and About their own hbox, which we align to the right
        HBox rightBox = new HBox();
        rightBox.setAlignment(Pos.BASELINE_RIGHT);

        Hyperlink aboutHyperlink = new Hyperlink("About");
        aboutHyperlink.getStyleClass().add("hyperlink");
        aboutHyperlink.setOnAction(event -> AboutWindow.showAbout());

        Hyperlink helpHyperlink = new Hyperlink("Help");
        helpHyperlink.getStyleClass().add("hyperlink");
        helpHyperlink.setOnAction(event -> logArea.setText(StaticTexts.getHelpText()));

        // add to boxes
        rightBox.getChildren().addAll(helpHyperlink, aboutHyperlink);
        hBox.getChildren().addAll(buttonClear, buttonRun, buttonExit, rightBox);

        // give the right button a margin to push it to the center of the page
        HBox.setMargin(buttonExit, new Insets(0,150,0,0));

        return hBox;
    }

    /**
     * default parameters used the first time the wizard is started
     * @return the default parameters
     */
    private static RunParameters getDefaultParameters(){
        // 2.16.840.1.113883.2.4.3.11.60.904
        // s2nki
        // String codebookDirectory, String projectId, String projectPrefix, String experimental, String authorString, String statusCode)
        RunParameters runParameters;
        if (debug){
            runParameters = new RunParameters("","1.2.3.4", "test-","true","myusername;me@somewhere.nl;My Name","VUmc;2020;author","draft");
//            runParameters.setDefaultLanguage("nl");
        }
        else{
            runParameters = new RunParameters("","","","true","","","draft");
        }
        runParameters.addLanguageSettings("nl", "Nederlandse Project Omschrijving", "Nederlandse Project Naam");
        runParameters.addLanguageSettings("en", "English Project Description", "English Project Name");
        runParameters.setDefaultLanguage("nl");

        return runParameters;
    }

    /**
     * Called after the runbutton is clicked.
     * Starts a Thread to do the actual work.
     */
    private void startTask(){
        logArea.clear();
        GUIWizard GUIWizard = new GUIWizard();
        try {
            if(GUIWizard.startWizard(runParameters)) {
                runParameters = GUIWizard.getRunParameters();
                new Thread(new WorkTask()).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * does the work
     */
    private class WorkTask extends Task {
        WorkTask(){
        }

        /**
         * creates the captionoverwriter, generates the codebook items, creates the codebook,
         * saves the codebook and write the conflicting captions to a file
         * @return null
         */
        @Override
        public Void call() {
            try {

                logger.log(Level.INFO, "Reading codebooks...");
                // reset the identifier manager
                IdentifierManager.createIdentifierManager(runParameters);

                // create the codebookmanager, reading the codebooks in the directory
                CodebookManager codebookManager = CodebookManager.readCodebooks(runParameters);

                // transform the codebooks to the artdecor datatypes
                logger.log(Level.INFO, "Transforming codebooks...");
                CodebookToArtDecorConvertor codebookToArtDecorConvertor = new CodebookToArtDecorConvertor(codebookManager, runParameters);
                codebookToArtDecorConvertor.transformCodebooks();

                // write the xml file
                logger.log(Level.INFO, "Writing ArtDecor XML file...");
                codebookToArtDecorConvertor.writeOutput(runParameters.getOutputFile());

                logger.log(Level.INFO, "Finished!");
            } catch (Exception e){
                logger.log(Level.INFO, "A fatal error occurred:\n"+e.getMessage());
                e.printStackTrace();
            }
            return null;
        }
    }
}
