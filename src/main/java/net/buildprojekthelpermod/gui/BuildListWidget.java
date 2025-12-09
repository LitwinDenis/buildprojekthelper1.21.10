package net.buildprojekthelpermod.gui;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.item.ItemStack;





import java.util.Map;

public class BuildListWidget implements Element, Drawable, Selectable {

    private final int slotSize = 20;
    private int scrollOffset = 0;

    @Override
    public net.minecraft.client.gui.Selectable.SelectionType getType() {
        return net.minecraft.client.gui.Selectable.SelectionType.NONE;
    }

    @FunctionalInterface
    public interface QuadConsumer<T1, T2, T3, T4> {
        void accept(T1 t1, T2 t2, T3 t3, T4 t4);
    }

    private final MinecraftClient client;
    private final int x, y, width, height;
    private final QuadConsumer<Block, Integer, Integer, Integer> onBlockDoubleClicked;
    private final Map<Block, Integer> requiredBlocks;

    private long lastClickTime = 0;
    private Block lastClickedBlock = null;

    public BuildListWidget(
            MinecraftClient client,
            int x, int y, int width, int height,
            Map<Block, Integer> requiredBlocks,
            QuadConsumer<Block, Integer, Integer, Integer> onBlockDoubleClicked
    ) {
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.requiredBlocks = requiredBlocks;
        this.onBlockDoubleClicked = onBlockDoubleClicked;
    }

    // Fehlende Methoden (leer lassen, um Fehler zu vermeiden)
    public void addEnteries(Map<Block, Integer> map){}
    public void refresh(){}

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        // Raster-Darstellung wie ein Inventar
        int columns = (slotSize > 0) ? this.width / slotSize : 1;
        int index = 0;
        int startIndex = scrollOffset * columns;

        for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {
            if (index < startIndex) {
                index++;
                continue;
            }

            int displayIndex = index - startIndex;
            int col = displayIndex % columns;
            int row = displayIndex / columns;

            int slotX = this.x + col * slotSize;
            int slotY = this.y + row * slotSize;

            if (slotY + slotSize > this.y + this.height) break;

            Block block = entry.getKey();
            int requiredAmount = entry.getValue();
            ItemStack stack = new ItemStack(block, requiredAmount);

            // Slot Hintergrund
            context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0x55000000);
            // Item
            context.drawItem(stack, slotX + 2, slotY + 2);
            // Menge als Text
            context.drawStackOverlay(client.textRenderer, stack, slotX + 2, slotY + 2);

            index++;
        }
    }
    // Neue Methode: Gibt den Block an der Mausposition zurück (oder null)
    public Block getBlockAt(double mouseX, double mouseY) {
        // 1. Ist die Maus überhaupt über der Liste?
        if (!isMouseOver(mouseX, mouseY)) return null;

        // 2. Raster-Berechnung (dieselbe wie in mouseClicked)
        int columns = (slotSize > 0) ? this.width / slotSize : 1;

        // Relative Position im Widget
        int relativeX = (int)(mouseX - this.x);
        int relativeY = (int)(mouseY - this.y);

        int col = relativeX / slotSize;
        int row = relativeY / slotSize;

        // Index berechnen
        int clickedSlotIndex = (row * columns) + col;
        int startIndex = scrollOffset * columns;
        int actualIndex = startIndex + clickedSlotIndex;

        // 3. Den Block an diesem Index finden
        int currentIndex = 0;
        for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {
            if (currentIndex == actualIndex) {
                return entry.getKey(); // Gefunden!
            }
            currentIndex++;
        }

        return null; // Nichts gefunden (leerer Slot)
    }

    // ============================================
    // FIXED: mouseClicked mit Click-Objekt
    // ============================================

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (!isMouseOver(mouseX, mouseY) || button != 0) return false;

        int columns = (slotSize > 0) ? this.width / slotSize : 1;
        int relativeX = (int)(mouseX - this.x);
        int relativeY = (int)(mouseY - this.y);

        int col = relativeX / slotSize;
        int row = relativeY / slotSize;

        int clickedSlotIndex = (row * columns) + col;
        int startIndex = scrollOffset * columns;
        int actualIndex = startIndex + clickedSlotIndex;

        // Finde den Block an diesem Index
        int currentIndex = 0;
        Block targetBlock = null;
        for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {
            if (currentIndex == actualIndex) {
                targetBlock = entry.getKey();
                break;
            }
            currentIndex++;
        }

        // Doppelklick-Logik
        if (targetBlock != null) {
            long now = System.currentTimeMillis();
            if (targetBlock == lastClickedBlock && now - lastClickTime < 250) {
                int amount = requiredBlocks.get(targetBlock);
                int targetX = this.x + col * slotSize;
                int targetY = this.y + row * slotSize;

                onBlockDoubleClicked.accept(targetBlock, targetX, targetY, amount);

                lastClickTime = 0;
                lastClickedBlock = null;
                return true;
            }
            lastClickTime = now;
            lastClickedBlock = targetBlock;
            return true;
        }

        return false;
    }


    /* Element Interface Methoden (müssen Click nutzen) */

    @Override
    public boolean mouseReleased(Click click) {
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        return false;
    }

    @Override public void appendNarrations(NarrationMessageBuilder builder) {}
    @Override public void setFocused(boolean focused) {}
    @Override public boolean isFocused() { return false; }
    @Override public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}