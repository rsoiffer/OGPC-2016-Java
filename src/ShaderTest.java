
import engine.Core;
import engine.Input;
import graphics.Graphics2D;
import graphics.Window2D;
import graphics.data.Framebuffer;
import graphics.data.Framebuffer.HDRTextureAttachment;
import graphics.data.Framebuffer.TextureAttachment;
import graphics.data.Shader;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import util.Color4;
import static util.Color4.BLACK;
import static util.Color4.TRANSPARENT;
import util.Vec2;

public class ShaderTest {

    public static void main(String[] args) {
        Core.init();
        Window2D.background = BLACK;
        //Show the fps
        Core.render.bufferCount(Core.interval(1)).forEach(i -> Display.setTitle("FPS: " + i));

//        new PostProcessEffect(1, new Framebuffer(new HDRTextureAttachment()),
//                new Shader("default.vert", "bloom.frag")).toggleOn(Input.whenMouse(0, true));
//
        Framebuffer f1 = new Framebuffer(new HDRTextureAttachment());
        Framebuffer f2 = new Framebuffer(new HDRTextureAttachment());
        Framebuffer f3 = new Framebuffer(new HDRTextureAttachment());
        Framebuffer f4 = new Framebuffer(new TextureAttachment());

        Shader onlyHDR = new Shader("default.vert", "onlyHDR.frag");
        Shader blur = new Shader("default.vert", "kawase.frag");

        Core.render.onEvent(() -> {
            f1.clear(TRANSPARENT);
            f2.clear(TRANSPARENT);
            f3.clear(TRANSPARENT);
            //Basic render
            Vec2 start = new Vec2(0, -300);
            Vec2 end = Input.getMouse().subtract(start).withLength(500).add(start);
            f1.with(() -> {
                Graphics2D.drawWideLine(start, end, new Color4(2.5, 1, 3), 5);
                Graphics2D.drawWideLine(start, start.interpolate(end, .85), Color4.gray(.5), 5);
            });

            /*
            Nice colors:
            Red: 3 1 1
            Green: 1 2.5 1
            Blue: 1 2 3
            Purple: 2.5 1 3
             */
            //Isolate hdr areas
            f2.with(() -> onlyHDR.with(f1::render));

            //Kawase blur hdr areas
            blur.with(() -> {
                blur.setInt("size", 0);
                f3.clear(TRANSPARENT);
                f3.with(f2::render);

                blur.setInt("size", 1);
                f2.clear(TRANSPARENT);
                f2.with(f3::render);

                blur.setInt("size", 2);
                f3.clear(TRANSPARENT);
                f3.with(f2::render);

                blur.setInt("size", 2);
                f2.clear(TRANSPARENT);
                f2.with(f3::render);

                blur.setInt("size", 3);
                f3.clear(TRANSPARENT);
                f3.with(f2::render);
            });

            //Save hdr areas
            f4.with(() -> {
                glBlendFunc(GL_SRC_ALPHA, GL_ONE);
                f3.render();
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                Graphics2D.fillRect(new Vec2(0), new Vec2(1), BLACK.withA(.1));
            });

            //Blur hdr
//            for (int i = 0; i < 5; i++) {
//                blur.setBoolean("horizontal", true);
//                f3.clear(TRANSPARENT);
//                f3.with(() -> blur.with(() -> f2.render()));
//
//                blur.setBoolean("horizontal", false);
//                f2.clear(TRANSPARENT);
//                f2.with(() -> blur.with(() -> f3.render()));
//            }
            //Render both to screen
            f1.render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            f3.render();
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        });

        Core.run();
    }
}
