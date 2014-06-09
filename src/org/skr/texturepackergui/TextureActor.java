package org.skr.texturepackergui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by rat on 28.05.14.
 */
public class TextureActor extends Actor {

    TextureRegion region;
    ShapeRenderer renderer;
    Animation animation;

    float stateTime;


    public TextureActor() {

        renderer = new ShapeRenderer();

    }

    @Override
    public void act(float delta) {

        stateTime += delta;

        if ( animation != null ) {
            region = animation.getKeyFrame(stateTime, true);
        }


        super.act(delta);
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
    }

    public void setAnimation( Animation animation ) {
        this.animation = animation;
    }



    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

        if ( region == null )
            return;

        int x = - region.getRegionWidth() / 2;
        int y = - region.getRegionHeight() / 2;

        batch.draw(region, x, y, region.getRegionWidth(), region.getRegionHeight());

        batch.end();

        renderer.begin(ShapeRenderer.ShapeType.Line);

        renderer.setProjectionMatrix( batch.getProjectionMatrix() );
        renderer.setTransformMatrix( batch.getTransformMatrix() );
        renderer.translate(getX(), getY(), 0);

        renderer.setColor(1, 0, 0, 1);

        renderer.rect( x-1, y-1, region.getRegionWidth()+2, region.getRegionHeight()+2 );

        renderer.end();

        batch.begin();

    }
}
