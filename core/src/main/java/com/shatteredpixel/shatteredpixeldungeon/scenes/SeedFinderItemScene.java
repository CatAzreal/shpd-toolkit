package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Clipboard;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SeedFinder;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSeedfinderLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;

import java.util.*;
public class SeedFinderItemScene extends PixelScene {

    private static final int CELL_W = 60;
    private static final int CELL_H = 15;
    private static final int GAP    = 2;
    private static final int MARG   = 2;

    @Override
    public void create() {

        super.create();

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);
        add(new ColorBlock(w, h, 0x88000000));

        ScrollPane pane = new ScrollPane(new Component());
        add(pane);
        Component content = pane.content();

        List<Item> items = collectUniqueItems();
        int cols = Math.max(2, (w - 2 * MARG + GAP) / (CELL_W + GAP));
        int col = 0, row = 0;

        for (Item it : items) {
            StyledButton btn = makeItemButton(it);
            btn.setRect(col * (CELL_W + GAP), row * (CELL_H + GAP), CELL_W, CELL_H);
            content.add(btn);

            if (++col == cols) { col = 0; ++row; }
        }

        int rows = (items.size() + cols - 1) / cols;
        content.setSize(cols * (CELL_W + GAP) - GAP,
                rows * (CELL_H + GAP) - GAP);

        pane.setRect(MARG, MARG, w - 2 * MARG, h - 2 * MARG);
        pane.scrollTo(0, 0);

        ExitButton exit = new ExitButton();
        exit.setPos(w - exit.width(), 0);
        add(exit);

    }

    @Override
    protected void onBackPressed() {
        ShatteredPixelDungeon.switchScene(TitleScene.class);
    }

    private static StyledButton makeItemButton(final Item item) {

        String label = item.trueName();

        StyledButton btn = new StyledButton(Chrome.Type.GREY_BUTTON_TR, label, 5) {
            @Override protected void onClick() {

                String seedTxt = new SeedFinder()
                        .find_seed(item.title().toLowerCase());

                Clipboard cb = Gdx.app.getClipboard();
                cb.setContents(seedTxt);

                long seed = DungeonSeed.convertFromText(seedTxt);
                String[] log = new SeedFinder().logSeedItemsSeededRun(seed);

                ShatteredPixelDungeon.scene().addToFront(
                        new WndSeedfinderLog(Icons.get(Icons.BACKPACK),
                                "Found seed " + DungeonSeed.convertToCode(seed),
                                log));

            }
        };
        btn.hotArea.blockLevel = PointerArea.NEVER_BLOCK;

        btn.multiline = false;
        btn.icon(new ItemSprite(item.image()));
        return btn;
    }

    private static List<Item> collectUniqueItems() {
        HashSet<Class<?>> seen = new HashSet<Class<?>>();
        ArrayList<Item>   list = new ArrayList<Item>();

        for (Generator.Category c : Generator.Category.values()) {
            for (int i = 0; i < 32; ++i) {
                Item it = Generator.random(c);
                if (it != null && seen.add(it.getClass())) list.add(it);
            }
        }

        Collections.sort(list, new Comparator<Item>() {
            public int compare(Item a, Item b) {
                return a.name().compareTo(b.name());
            }
        });
        return list;
    }
}
