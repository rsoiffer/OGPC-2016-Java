package invisibleman;

import engine.Core;
import engine.Input;
import engine.Signal;
import graphics.Window3D;
import static graphics.Window3D.*;
import static invisibleman.MessageType.*;
import map.CubeMap;
import static map.CubeMap.WORLD_SIZE;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.input.Keyboard.*;
import util.Mutable;
import util.RegisteredEntity;
import util.Vec3;

public class InvisibleMan extends RegisteredEntity {

    @Override
    protected void createInner() {
        //Create the player's variables
        Signal<Vec3> position = Premade3D.makePosition(this);
        Signal<Vec3> prevPos = Premade3D.makePrevPosition(this);
        Signal<Vec3> velocity = Premade3D.makeVelocity(this);
        Signal<Double> invincible = addChild(new Signal(5.), "invincible");

        position.set(WORLD_SIZE.multiply(.5));

        //Give the player basic first-person controls
        Premade3D.makeMouseLook(this, 2, -1.5, 1.5);
        Premade3D.makeWASDMovement(this, 5);
        Premade3D.makeGravity(this, new Vec3(0, 0, -10));

        //Flying cheat
        Signal<Boolean> fly = Input.whenKey(KEY_TAB, true).reduce(false, b -> !b);
        add(fly,
                Input.whileKey(KEY_W, true).filter(fly).forEach(dt -> position.edit((fly.get() ? facing.toVec3() : forwards()).multiply(20 * dt)::add)),
                Input.whileKey(KEY_S, true).filter(fly).forEach(dt -> position.edit((fly.get() ? facing.toVec3() : forwards()).multiply(-20 * dt)::add)),
                Input.whileKey(KEY_A, true).filter(fly).forEach(dt -> position.edit(facing.toVec3().cross(UP).withLength(-20 * dt)::add)),
                Input.whileKey(KEY_D, true).filter(fly).forEach(dt -> position.edit(facing.toVec3().cross(UP).withLength(20 * dt)::add)),
                Input.whileKey(KEY_SPACE, true).filter(fly).forEach(dt -> position.edit(UP.multiply(20 * dt)::add)),
                Input.whileKey(KEY_LSHIFT, true).filter(fly).forEach(dt -> position.edit(UP.multiply(-20 * dt)::add)),
                Core.update.filter(fly).forEach(dt -> velocity.set(new Vec3(0))));

        //Make the camera automatically follow the player
        position.doForEach(v -> Window3D.pos = v.add(new Vec3(0, 0, .8)));

        //Make the player collide with the floor
        Premade3D.makeCollisions(this, new Vec3(.3, .3, .9));
        Signal<Boolean> onGround = addChild(Core.update.map(() -> velocity.get().z <= 0 && CubeMap.isSolid(position.get().add(new Vec3(0, 0, -.01)), new Vec3(.3, .3, .9))));

        //Force the player to stay inside the room
        position.filter(p -> !p.containedBy(new Vec3(0), WORLD_SIZE)).forEach(p -> {
            position.set(p.clamp(new Vec3(0), WORLD_SIZE.subtract(new Vec3(.0001))));
        });

        //Make the player slowly lose invincibility
        add(Core.update.forEach(dt -> invincible.edit(d -> d - dt)));

        //Throwing snowballs
        add(Input.whenMouse(0, true).limit(.5).onEvent(() -> {
            Vec3 pos = position.get().add(new Vec3(0, 0, .8));
            Vec3 vel = Window3D.facing.toVec3().withLength(30);

            Client.sendMessage(SNOWBALL, pos, vel);

            BallAttack b = new BallAttack();
            b.create();
            b.get("position", Vec3.class).set(pos);
            b.get("velocity", Vec3.class).set(vel);
        }));

        //Jumping
        add(Input.whileKey(Keyboard.KEY_SPACE, true).filter(onGround).onEvent(() -> {
            velocity.edit(v -> v.withZ(4.75));
        }));

        //Creating footsteps
        //Whether you should place a left footstep or a right footstep
        Mutable<Boolean> isLeft = new Mutable(true);
        //Every .2 seconds, do the following:
        add(Core.update.filter(onGround).limit(.2).combineEventStreams(onGround.distinct()).filter(invincible.map(d -> d < 0)).onEvent(() -> {
            //Create the footstep
            Footstep f = new Footstep();
            f.create();
            double opacity = Input.keySignal(KEY_LSHIFT).get() ? .5 : 1;
            //Set the footstep's position
            f.set(position.get().add(new Vec3(0, 0, -.88)), Window3D.facing.t, isLeft.o, opacity);

            //Walking leaves lighter steps
            if (Input.keySignal(KEY_LSHIFT).get()) {
                f.get("opacity", Double.class).set(.5);
            }

            //Make the next footstep switch from left to right or vice-versa
            isLeft.o = !isLeft.o;

            //Send a message to the server with the footstep
            Client.sendMessage(FOOTSTEP, position.get().add(new Vec3(0, 0, -.88)), Window3D.facing.t, isLeft.o, opacity);
        }));

        add(Core.update.filter(onGround.map(b -> !b)).limit(.1).onEvent(() -> {
            Smoke s = new Smoke();
            s.create();
            s.get("position", Vec3.class).set(position.get().add(Vec3.randomShell(.2)));
            if (Input.keySignal(KEY_LSHIFT).get()) {
                s.get("opacity", Double.class).set(.5);
            }
            Client.sendMessage(SMOKE, s.get("position", Vec3.class).get(), s.get("opacity", Double.class).get());
        }));

        add(Core.renderLayer(.4).onEvent(() -> {
            //drawShadow(position.get(), .3, 100);
        }));
    }

//    private void drawShadow(Vec3 position, double size, int detail) {
//        Fog.setMinTexColor(1, 1, 1, 1);
//        glDisable(GL_TEXTURE_2D);
//        BLACK.withA(.4).glColor();
//
//        List<Double> angles = new ArrayList();
//        Util.repeat(detail, i -> angles.add(2 * Math.PI * i / detail));
//        angles.add(2 * Math.PI - .0001);
//        double dx = Math.min(position.x % 1, 1 - position.x % 1);
//        if (dx < size) {
//            angles.add(Math.PI / 2 - Math.asin(dx / size));
//            angles.add(3 * Math.PI / 2 + Math.asin(dx / size));
//        }
//        double dy = Math.min(position.y % 1, 1 - position.y % 1);
//        if (dy < size) {
//            angles.add(Math.asin(dy / size));
//            angles.add(-Math.asin(dy / size));
//        }
//
//        if (dx < size && dy < size) {
//            CubeMap.rayCastStream(position.add(new Vec2(size).toVec3()), new Vec3(0, 0, -1)).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
//                glBegin(GL_TRIANGLE_FAN);
//                {
//                    Vec3 pos = new Vec3(cd.x - .0001, cd.y - .0001, cd.z + 1.0001);
//                    double dir;
//                    if (pos.subtract(position).toVec2().lengthSquared() < size * size) {
//                        pos.glVertex();
//                        dir = Math.PI * 5 / 4;
//                    } else {
//                        position.toVec2().perComponent(pos.toVec2(), Math::max).toVec3().withZ(pos.z).glVertex();
//                        dir = pos.subtract(position).toVec2().direction();
//                    }
//                    angles.stream().map(x -> (x + 4 * Math.PI - dir) % (2 * Math.PI)).sorted().forEach(angle -> {
//                        Vec3 vert = position.add(new Vec2(size, 0).rotate(angle + dir).toVec3()).withZ(pos.z);
//                        if (pos.quadrantXY(vert) == 1) {
//                            vert.glVertex();
//                        }
//                    });
//                }
//                glEnd();
//            });
//        }
//        if (dy < size) {
//            CubeMap.rayCastStream(position.add(new Vec2(-size, size).toVec3()), new Vec3(0, 0, -1)).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
//                glBegin(GL_TRIANGLE_FAN);
//                {
//                    Vec3 pos = new Vec3(cd.x + 1, cd.y, cd.z + 1.0001);
//                    double dir;
//                    if (pos.subtract(position).toVec2().lengthSquared() < size * size) {
//                        pos.glVertex();
//                        dir = Math.PI * 7 / 4;
//                    } else {
//                        new Vec3(Math.min(position.x, pos.x), Math.max(position.y, pos.y), pos.z).glVertex();
//                        dir = pos.subtract(position).toVec2().direction();
//                    }
//                    angles.stream().map(x -> (x + 4 * Math.PI - dir) % (2 * Math.PI)).sorted().forEach(angle -> {
//                        Vec3 vert = position.add(new Vec2(size, 0).rotate(angle + dir).toVec3()).withZ(pos.z);
//                        if (pos.quadrantXY(vert) == 2) {
//                            vert.glVertex();
//                        }
//                    });
//                }
//                glEnd();
//            });
//        }
//        CubeMap.rayCastStream(position.add(new Vec2(-size).toVec3()), new Vec3(0, 0, -1)).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
//            glBegin(GL_TRIANGLE_FAN);
//            {
//                Vec3 pos = new Vec3(cd.x + 1, cd.y + 1, cd.z + 1.0001);
//                double dir;
//                if (pos.subtract(position).toVec2().lengthSquared() < size * size) {
//                    pos.glVertex();
//                    dir = Math.PI / 4;
//                } else {
//                    position.toVec2().perComponent(pos.toVec2(), Math::min).toVec3().withZ(pos.z).glVertex();
//                    dir = pos.subtract(position).toVec2().direction();
//                }
//                angles.stream().map(x -> (x + 4 * Math.PI - dir) % (2 * Math.PI)).sorted().forEach(angle -> {
//                    Vec3 vert = position.add(new Vec2(size, 0).rotate(angle + dir).toVec3()).withZ(pos.z);
//                    if (pos.quadrantXY(vert) == 3) {
//                        vert.glVertex();
//                    }
//                });
//            }
//            glEnd();
//        });
//        if (dx < size) {
//            CubeMap.rayCastStream(position.add(new Vec2(size, -size).toVec3()), new Vec3(0, 0, -1)).filter(cd -> cd.c != null).findFirst().ifPresent(cd -> {
//                glBegin(GL_TRIANGLE_FAN);
//                {
//                    Vec3 pos = new Vec3(cd.x, cd.y + 1, cd.z + 1.0001);
//                    double dir;
//                    if (pos.subtract(position).toVec2().lengthSquared() < size * size) {
//                        pos.glVertex();
//                        dir = Math.PI * 3 / 4;
//                    } else {
//                        new Vec3(Math.max(position.x, pos.x), Math.min(position.y, pos.y), pos.z).glVertex();
//                        dir = pos.subtract(position).toVec2().direction();
//                    }
//                    angles.stream().map(x -> (x + 4 * Math.PI - dir) % (2 * Math.PI)).sorted().forEach(angle -> {
//                        Vec3 vert = position.add(new Vec2(size, 0).rotate(angle + dir).toVec3()).withZ(pos.z);
//                        if (pos.quadrantXY(vert) == 4) {
//                            vert.glVertex();
//                        }
//                    });
//                }
//                glEnd();
//            });
//        }
//
//        Fog.setMinTexColor(0, 0, 0, 0);
//    }
}
