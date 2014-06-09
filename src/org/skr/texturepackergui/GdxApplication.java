package org.skr.texturepackergui;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.File;
import java.util.Queue;

/**
 * Created by rat on 25.05.14.
 */
public class GdxApplication implements ApplicationListener, InputProcessor {

    Stage stage;
    OrthographicCamera camera;

    TextureAtlas atlas;
    TextureAtlas.TextureAtlasData atlasData;

    TextureActor actor;

    String atlasPackFile = "";

    int currentPageIndex = -1;
    Array<String> regionNames = new Array<String>();

    TextureRegion currentRegion;
      public interface AtlasLoadListener {
      public void atlasLoaded();
    }

    AtlasLoadListener atlasLoadListener;


    @Override
    public void create() {


        ScreenViewport sv = new ScreenViewport();
        stage = new Stage(sv);
        camera = (OrthographicCamera) stage.getCamera();

        actor = new TextureActor();

        stage.addActor( actor );


        actor.setPosition( 0, 0 );

        if ( !atlasPackFile.isEmpty() ) {
            updateAtlas(atlasPackFile);
        }

        Gdx.input.setInputProcessor( this );

        Gdx.app.log("GdxApplication.create", "done");
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, false);
        camera.position.set(0, 0, 0);
    }

    @Override
    public void render() {

        processInputState();

        float delta = Gdx.graphics.getDeltaTime();

        stage.act( delta );

        Gdx.gl20.glClearColor(0.8f, 0.8f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        stage.draw();

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        stage.dispose();

        releaseRegion();
        if ( atlas != null )
            atlas.dispose();
    }


    void releaseRegion() {
        if ( currentRegion == null )
            return;

        actor.setRegion( null );
        actor.setAnimation( null );

        currentRegion.getTexture().dispose();
        currentRegion = null;
    }


    public void setAtlasLoadListener(AtlasLoadListener atlasLoadListener) {
        this.atlasLoadListener = atlasLoadListener;
    }

    public void updateAtlas(String packPathFileName) {

        File f = new File( packPathFileName );

        if ( !f.exists() ) {
            Gdx.app.log("GdxApplication.updateAtlas", " Pack file: " + packPathFileName + " does not exist");
            return;
        }

        atlasPackFile = packPathFileName;

        if ( stage == null )
            return;

        FileHandle fh = Gdx.files.internal( atlasPackFile );

        atlasData = new TextureAtlas.TextureAtlasData(fh, fh.parent(), false);
        atlas = new TextureAtlas( atlasData );


        regionNames.clear();

        for (TextureAtlas.TextureAtlasData.Region r :  atlasData.getRegions()  ) {
            if ( regionNames.indexOf(r.name, false) == -1)
                regionNames.add(r.name);
        }

        regionNames.sort();

        Gdx.app.log( "GdxApplication.updateAtlas", "Done. Pages: " + atlasData.getPages().size );



        setCurrentPage( currentPageIndex );
        if ( atlasLoadListener != null ) {
            atlasLoadListener.atlasLoaded();
        }
    }

    public Array<String> getRegionNames() {
        return regionNames;
    }

    public int getPagesCount() {
        if ( atlasData == null )
            return 0;
        return atlasData.getPages().size;
    }


    public void setCurrentPage( int index ) {

        if (index < 0)
            index = 0;
        if (index >= atlasData.getPages().size) {
            index = atlasData.getPages().size - 1;
        }

        releaseRegion();

        currentPageIndex = index;

        if ( currentPageIndex < 0 ) {
            return;
        }

        Gdx.app.log("GdxApplication.setCurrentPage", "index: " + currentPageIndex);

        TextureAtlas.TextureAtlasData.Page page = atlasData.getPages().get( index );

        Texture texture = new Texture(page.textureFile);

        currentRegion = new TextureRegion(texture);

        actor.setAnimation( null );
        actor.setRegion( currentRegion );

    }


    void showRegion(String name) {
        if (atlas == null)
            return;
        Array<TextureAtlas.AtlasRegion> tr = atlas.findRegions(name);

        actor.setAnimation( new Animation(0.25f, tr));

    }

    private void processInputState() {

    };



    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }


    float downPosX = 0;
    float downPosY = 0;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        downPosX = screenX;
        downPosY = screenY;

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {

        float offsetX = screenX - downPosX;
        float offsetY = screenY - downPosY;

        downPosX = screenX;
        downPosY = screenY;

        camera.translate( - offsetX * camera.zoom, offsetY * camera.zoom, 0);

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {

        if ( amount > 0)
            camera.zoom *= 2;
        else if (amount < 0)
            camera.zoom /= 2;
        return true;
    }

}
