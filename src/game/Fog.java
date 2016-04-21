package game;

import engine.AbstractEntity;
import engine.Core;
import graphics.Window3D;
import graphics.data.Shader;
import util.Color4;
import util.Util;

public class Fog extends AbstractEntity {

    private static final Shader FOG = new Shader("default.vert", "fog.frag");

    public Fog(Color4 color, double density, double fade) {
        Window3D.background = color;
        //GLSL is mean - you have to enable a shader to edit it
        Shader.pushShader(FOG);
        FOG.setVec3("fogColor", color.toFloatBuffer3());
        FOG.setFloat("density", density);
        FOG.setFloat("fade", fade);
        Shader.popShader();
    }

    @Override
    public void create() {
        add(Core.renderLayer(-1).onEvent(() -> Shader.pushShader(FOG)),
                Core.renderLayer(1).onEvent(() -> Shader.popShader()));
    }

    public static void setMinTexColor(double... vals) {
        FOG.setVec4("min", Util.floatBuffer(vals));
    }
}
