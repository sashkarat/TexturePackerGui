package org.skr.texturepackergui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

/**
 * Created by rat on 25.05.14.
 */
public class TexturePackerProject {



    private String inputDirectory;
    private String outputDirectory;
    private String packFileName;


    TexturePacker.Settings settings = new TexturePacker.Settings();


    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public TexturePacker.Settings getSettings() {
        return settings;
    }

    public void setSettings(TexturePacker.Settings settings) {
        this.settings = settings;
    }

    public String getPackFileName() {
        return packFileName;
    }

    public void setPackFileName(String packFileName) {
        this.packFileName = packFileName;
    }
}
