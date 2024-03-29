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

import palgacodebooktoxml.gui.resourcemanagement.ResourceManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.StageStyle;

/**
 * The About Window
 */
class AboutWindow {
    private static final ResourceManager resourceManager = new ResourceManager();
    private static Dialog dialog;
    private static final double prefWidth = 675;
    private static final double prefHeigth = 400;

    /**
     * shows the about window
     */
    static void showAbout(){
        if (dialog==null) {
            createDialog();
        }
        dialog.showAndWait();
    }

    /**
     * creates the about window
     */
    private static void createDialog(){
        dialog = new Dialog();
        dialog.getDialogPane().setPrefSize(prefWidth, prefHeigth);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle("About...");

        dialog.getDialogPane().getStylesheets().add(resourceManager.getResourceStyleSheet("style.css"));
        dialog.getDialogPane().getStyleClass().add("fillBackground");

        // add an ok button
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        // generate the panes
        Node topPane = setupTopPane();
        Node centerPane = setupCenterPane();
        Node bottomPane = setupBottomPane();

        // add them to the borderpane
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topPane);
        borderPane.setCenter(centerPane);
        borderPane.setBottom(bottomPane);

        // set the content of the dialog
        dialog.getDialogPane().setContent(borderPane);
    }

    /**
     * creates the top pane of the window
     * @return the top pane
     */
    private static Node setupTopPane(){
        Label sceneTitle = new Label("About PALGA Protocol Codebook to XML...");
        sceneTitle.setId("title2");
        sceneTitle.setPadding(new Insets(5,0,5,10));
        return sceneTitle;
    }

    /**
     * creates the center pane of the window
     * @return the center pane
     */
    private static Node setupCenterPane(){
        TextArea textArea = new TextArea(StaticTexts.getAboutText());
        textArea.setEditable(false);
        textArea.setPrefWidth(prefWidth);

        HBox hBox = new HBox();
        hBox.getStyleClass().add("fillBackground");
        hBox.getChildren().addAll(textArea);
        hBox.setAlignment(Pos.CENTER);

        return hBox;
    }

    /**
     * creates the bottom pane of the window
     * @return the bottom pane
     */
    private static Node setupBottomPane(){
        double f=0.70;
        int height=(int) Math.floor(60*f);
        int width= (int) Math.floor(70*f);

        GridPane grid = new GridPane();
        grid.setHgap(55);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10,0,0,0));

//        grid.setGridLinesVisible(true);

        // first row
        ImageView vumcImv = getImageView(height,2*width,"vumc_white.png");
        grid.add(vumcImv,0,0,2,1);

        ImageView antonieImv = getImageView(height,2*width,"nki_white.png");
        grid.add(antonieImv,2,0,2,1);

//        ImageView palgaImv = getImageView(height,2*width-10,"palga_white.png");
//        grid.add(palgaImv,4,0,2,1);

        ImageView lygatureImv = getImageView(height,2*width,"lygature_white.png");
        grid.add(lygatureImv,4,0,2,1);

        // second row...
        ImageView healthriImv = getImageView(height,2*width,"healthri_white.png");
        grid.add(healthriImv,0,1,2,1);

        ImageView aumcImv = getImageView(height,5*width,"aumc_white.png");
        grid.add(aumcImv,2,1,5,1);

        ImageView bbmriImv = getImageView(height,2*width,"bbmri_white.png");
        grid.add(bbmriImv,6,1,2,1);

        // third row
        ImageView kwfImv = getImageView(height,2*width,"kwf_white.png");
        grid.add(kwfImv,0,2,2,1);

        ImageView nictizImv = getImageView(height,2*width,"nictiz_white.png");
        grid.add(nictizImv,2,2,2,1);

        ImageView mldsImv = getImageView(height,2*width-10,"mlds_white.png");
        grid.add(mldsImv,4,2,2,1);

        // give the grid an id and add styleclass
        grid.setId("icons");
        grid.getStyleClass().add("fillBackgroundWhite");
        return grid;
    }

    /**
     * creates an image view
     * @param height height
     * @param width  width
     * @param image  name of the image
     * @return the image view
     */
    private static ImageView getImageView(int height, int width, String image){
        ImageView imageView = new ImageView();
        imageView.setFitHeight(height);
        imageView.setFitWidth(width);
        imageView.setImage(resourceManager.getResourceImage(image));
        return imageView;
    }
}
