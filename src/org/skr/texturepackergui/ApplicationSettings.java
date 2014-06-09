package org.skr.texturepackergui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Created by rat on 25.05.14.
 */
public class ApplicationSettings {

    private static Preferences pr = null;

    private static final String keyProjectFile = "keyProjectFile";

    private static String projectFile;



    private static void checkSettings() {
        if ( pr == null ) {
            pr = Gdx.app.getPreferences("TexturePackerGui");
        }
    }

    public static void saveSettings() {
        checkSettings();
        pr.putString(keyProjectFile, projectFile);

        pr.flush();
        Gdx.app.log("ApplicationSettings", "Saved");
    }

    public static void loadSettings() {
        checkSettings();
        projectFile = pr.getString(keyProjectFile, "noname");

        Gdx.app.log("ApplicationSettings", "Loaded");

    }


    public static String getProjectFile() {
        return projectFile;
    }

    public static void setProjectFile(String projectFile) {
        ApplicationSettings.projectFile = projectFile;
    }
}
