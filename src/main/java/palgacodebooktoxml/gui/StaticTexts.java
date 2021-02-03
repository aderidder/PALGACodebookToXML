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

class StaticTexts {

    /**
     * returns the welcome text
     * @return the welcome text
     */
    static String getWelcomeText(){
        return "Welcome to the Excel Codebook to Art-Decor XML converter!\n\n" + getHelpText();
    }

    /**
     * returns the help text
     * @return the help text
     */
    static String getHelpText(){
        return  "Press Run to start the Wizard. The fields contain mouseovers with examples. Please\n" +
                "be aware that some values need to be provided by Nictiz (the project id, project prefix)\n" +
                "Output is written to the directory that contains your datafile.\n\n";
    }

    /**
     * returns the about text
     * @return the about text
     */
    static String getAboutText(){
        return  "PALGA Protocol Codebook to XML was designed and created by:" +
                "\n\tSander de Ridder (NKI 2017; VUmc 2018/2019/2020)\n"+
                "\n\tJeroen Belien (VUmc)\n" +
                "Testers & Consultants:" +
                "\n\tMaarten Ligtvoet (Nictiz)" +
                "\n\tAlexander Henket (Nictiz)" +
                "\n\tElze de Groot (Nictiz)" +
                "This project was sponsored by MLDS project OPSLAG and KWF project TraIT2Health-RI (WP: Registry-in-a-Box)\n\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------------------------\n\n"+
                "Copyright 2017 NKI / AvL; 2018/2019/2020 VUmc\n" +
                "\n" +
                "PALGA Protocol Codebook to XML is free software: you can redistribute it and/or modify\n" +
                "it under the terms of the GNU General Public License as published by\n" +
                "the Free Software Foundation, either version 3 of the License, or\n" +
                "(at your option) any later version.\n" +
                "\n" +
                "PALGA Protocol Codebook to XML is distributed in the hope that it will be useful,\n" +
                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the\n" +
                "GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License\n" +
                "along with PALGA Protocol Codebook to XML. If not, see <http://www.gnu.org/licenses/>\n";
    }
}
