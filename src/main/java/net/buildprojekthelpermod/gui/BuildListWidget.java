package net.buildprojekthelpermod.gui;

import net.buildprojekthelpermod.data.ProjectManager;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.Click; // Deine Custom Click Klasse oder Library

import java.util.Map;

public class BuildListWidget implements Element, Drawable, Selectable {

    private final MinecraftClient client;
    private final int x, y, width, height;
    private final Map<Block, Integer> requiredBlocks;
    private final QuadConsumer<Block, Integer, Integer, Integer> onBlockDoubleClicked;

    private final int slotSize = 20;
    private int scrollOffset = 0;

    // Doppelklick Tracking
    private long lastClickTime = 0;
    private Block lastClickedBlock = null;

    @FunctionalInterface
    public interface QuadConsumer<T1, T2, T3, T4> {
        void accept(T1 t1, T2 t2, T3 t3, T4 t4);
    }

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

    // --- RENDER METHODE ---
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
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

            // Pinned Status prüfen (Grüner Hintergrund)
            boolean isPinned = false;
            if (ProjectManager.getInstance().getCurrentProject() != null) {
                isPinned = ProjectManager.getInstance().getCurrentProject().isPinned(block);
            }

            if (isPinned) {
                // Hintergrund Transparent Grün
                context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0x6000FF00);

                // Grüner Rand (Manuell gezeichnet)
                int borderColor = 0xFF00FF00;
                context.fill(slotX, slotY, slotX + slotSize, slotY + 1, borderColor); // Oben
                context.fill(slotX, slotY + slotSize - 1, slotX + slotSize, slotY + slotSize, borderColor); // Unten
                context.fill(slotX, slotY, slotX + 1, slotY + slotSize, borderColor); // Links
                context.fill(slotX + slotSize - 1, slotY, slotX + slotSize, slotY + slotSize, borderColor); // Rechts
            } else {
                // Standard Hintergrund Dunkel
                context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0x55000000);
            }

            // Item und Text rendern
            context.drawItem(stack, slotX + 2, slotY + 2);
            context.drawStackOverlay(client.textRenderer, stack, slotX + 2, slotY + 2);

            index++;
        }
    }

    // --- WICHTIG: DIE SUCHE NACH DEM BLOCK ---
    public Block getBlockAt(double mouseX, double mouseY) {
        // Debugging: Wo klicke ich hin?
        // System.out.println("Suche Block bei Maus: " + mouseX + "/" + mouseY + " in Widget Bereich: " + x + "," + y);

        if (mouseX < this.x || mouseX > this.x + this.width ||
                mouseY < this.y || mouseY > this.y + this.height) {
            return null;
        }

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

            // HITBOX CHECK
            // Wenn Maus innerhalb dieses Slots ist
            if (mouseX >= slotX && mouseX < slotX + slotSize &&
                    mouseY >= slotY && mouseY < slotY + slotSize) {

                System.out.println("DEBUG: Block gefunden: " + entry.getKey().getName().getString());
                return entry.getKey();
            } else {
                // Optional: Zeige an, wo wir gesucht haben, wenn es NICHT passt
                // System.out.println("Check Slot: " + slotX + "/" + slotY + " - Passt nicht.");
            }

            index++;
        }

        System.out.println("DEBUG: Klick im Widget, aber kein Slot getroffen (Leerer Bereich?)");
        return null;
    }

    // --- MOUSE CLICKED (Für Linksklick / Doppelklick) ---
    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // Wenn Rechtsklick (Button 1), ignorieren wir das HIER und lassen den Screen das machen
        if (!isMouseOver(mouseX, mouseY) || button != 0) return false;

        // Logik für Linksklick (Doppelklick zum Editieren)
        int columns = (slotSize > 0) ? this.width / slotSize : 1;
        int relativeX = (int)(mouseX - this.x);
        int relativeY = (int)(mouseY - this.y);

        int col = relativeX / slotSize;
        int row = relativeY / slotSize;

        int clickedSlotIndex = (row * columns) + col;
        int startIndex = scrollOffset * columns;
        int actualIndex = startIndex + clickedSlotIndex;

        int currentIndex = 0;
        Block targetBlock = null;
        for (Map.Entry<Block, Integer> entry : requiredBlocks.entrySet()) {
            if (currentIndex == actualIndex) {
                targetBlock = entry.getKey();
                break;
            }
            currentIndex++;
        }

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

    // Platzhalter
    public void addEnteries(Map<Block, Integer> map){}
    public void refresh(){}

    @Override public SelectionType getType() { return SelectionType.NONE; }
    @Override public boolean mouseReleased(Click click) { return false; }
    @Override public boolean mouseDragged(Click click, double deltaX, double deltaY) { return false; }
    @Override public void appendNarrations(NarrationMessageBuilder builder) {}
    @Override public void setFocused(boolean focused) {}
    @Override public boolean isFocused() { return false; }
    @Override public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}