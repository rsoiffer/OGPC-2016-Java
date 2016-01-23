
import engine.Core;
import engine.Input;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.TextureAttachment;
import graphics.data.PostProcessEffect;
import graphics.data.Shader;
import static util.Color4.BLACK;
import static util.Color4.gray;
import util.Vec2;

public class ShaderTest {

    public static void main(String[] args) {
        Core.init();
        Window2D.background = BLACK;

        new PostProcessEffect(1, new Framebuffer(new TextureAttachment()),
                new Shader("default.vert", "invert.frag")).toggleOn(Input.whenMouse(0, true));
//        Shader hdr1 = new Shader("void main(){gl_Position = ftransform();}",
//                "void main(){if (gl_Color.r > 1) gl_FragColor = vec4(1);"
//                + "else gl_FragColor = gl_Color;}");
//
        Framebuffer fb1 = new Framebuffer(new TextureAttachment());
        Core.render.onEvent(() -> {
            Framebuffer.pushFramebuffer(fb1);
            Graphics2D.fillRect(new Vec2(-300), new Vec2(200), gray(.25));
            Graphics2D.fillRect(new Vec2(-100), new Vec2(200), gray(.5));
            Graphics2D.fillRect(new Vec2(100), new Vec2(200), gray(.75));
            Framebuffer.popFramebuffer();
            fb1.render();
        });

        Core.run();
    }
}
