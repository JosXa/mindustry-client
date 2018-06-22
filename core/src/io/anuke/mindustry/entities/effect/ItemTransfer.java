package io.anuke.mindustry.entities.effect;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.graphics.Palette;
import io.anuke.mindustry.net.In;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.entities.impl.TimedEntity;
import io.anuke.ucore.entities.trait.DrawTrait;
import io.anuke.ucore.entities.trait.PosTrait;
import io.anuke.ucore.function.Callable;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.graphics.Fill;
import io.anuke.ucore.graphics.Lines;
import io.anuke.ucore.util.Mathf;

import static io.anuke.mindustry.Vars.effectGroup;

public class ItemTransfer extends TimedEntity implements DrawTrait{
    private Vector2 from = new Vector2();
    private Vector2 current = new Vector2();
    private Vector2 tovec = new Vector2();
    private Item item;
    private float seed;
    private PosTrait to;
    private Callable done;

    @Remote(in = In.entities, called = Loc.server, unreliable = true)
    public static void transferAmmo(Item item, float x, float y, Unit to){
        if(to == null) return;
        to.addAmmo(item);
        create(item, x, y, to, () -> {});
    }

    @Remote(in = In.entities, called = Loc.server, unreliable = true)
    public static void transferItemEffect(Item item, float x, float y, Unit to){
        create(item, x, y, to, () -> {});
    }

    @Remote(in = In.entities, called = Loc.server)
    public static void transferItemTo(Item item, int amount, float x, float y, Tile tile){
        for (int i = 0; i < Mathf.clamp(amount/3, 1, 8); i++) {
            Timers.run(i*3, () -> create(item, x, y, tile, () -> {}));
        }
        tile.entity.items.addItem(item, amount);
    }

    public static void create(Item item, float fromx, float fromy, PosTrait to, Callable done){
        ItemTransfer tr = Pools.obtain(ItemTransfer.class);
        tr.item = item;
        tr.from.set(fromx, fromy);
        tr.to = to;
        tr.done = done;
        tr.seed = Mathf.range(1f);
        tr.add();
    }

    public ItemTransfer(){}

    @Override
    public float lifetime() {
        return 60;
    }

    @Override
    public void reset() {
        super.reset();
        item = null;
        to = null;
        done = null;
        from.setZero();
        current.setZero();
        tovec.setZero();
    }

    @Override
    public void removed() {
        done.run();
        Pools.free(this);
    }

    @Override
    public void update() {
        super.update();
        current.set(from).interpolate(tovec.set(to.getX(), to.getY()), fin(), Interpolation.pow3);
        current.add(tovec.set(to.getX(), to.getY()).sub(from).nor().rotate90(1).scl(seed * fslope() * 10f));
        set(current.x, current.y);
    }

    @Override
    public void draw() {
        float length = fslope()*6f;
        float angle = current.set(x, y).sub(from).angle();
        Draw.color(Palette.accent);
        Lines.stroke(fslope()*2f);

        Lines.circle(x, y, fslope()*2f);
        Lines.lineAngleCenter(x, y, angle, length);
        Lines.lineAngle(x, y, angle, fout()*6f);

        Draw.color(item.color);
        Fill.circle(x, y, fslope()*1.5f);

        Draw.reset();
    }

    @Override
    public EntityGroup targetGroup() {
        return effectGroup;
    }
}
