package net.buildprojekthelpermod.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public class BlockGridWidget implements Element, Drawable, Selectable {

    private final MinecraftClient client;
    private final int x, y, width, height;
    private final int slotSize = 20;
    private int scrollOffset = 0;
    private boolean focused = false;
    private final List<Block> blocks = new ArrayList<>();
    private final List<Block> allBlocks = new ArrayList<>();
    private ItemStack hoveredStack = ItemStack.EMPTY;
    private Consumer<Block> onBlockClicked;

    // FEHLER BEHOBEN: Die unvollständige Zeile 'private Click =' wurde entfernt.

    @Override
    public net.minecraft.client.gui.Selectable.SelectionType getType() {
        return net.minecraft.client.gui.Selectable.SelectionType.NONE;
    }

    public BlockGridWidget(MinecraftClient client, int x, int y, int width, int height, Consumer<Block> onBlockClicked) {
        this.client = client;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.onBlockClicked = onBlockClicked;

        for (Block block : Registries.BLOCK) {
            if (block.asItem() != Items.AIR) {
                allBlocks.add(block);
            }
        }
        blocks.addAll(allBlocks);
    }

    public void applyFilter(String query) {
        blocks.clear();

        if (query.isEmpty()) {
            blocks.addAll(allBlocks);
            return;
        }
        String lowerCaseQuery = query.toLowerCase();

        List<Block> filteredList = allBlocks.stream()
                .filter(block -> {
                    String blockName = Text.translatable(block.getTranslationKey()).getString().toLowerCase();
                    return blockName.contains(lowerCaseQuery);
                })
                .collect(Collectors.toList());
        blocks.addAll(filteredList);
        scrollOffset = 0;
    }
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width &&
                mouseY >= this.y && mouseY < this.y + this.height;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        int columns = width / slotSize;
        int startIndex = scrollOffset * columns;
        int rows = height / slotSize;

        hoveredStack = ItemStack.EMPTY;

        for (int i = 0; i < rows * columns && startIndex + i < blocks.size(); i++) {
            int col = i % columns;
            int row = i / columns;
            int slotX = x + col * slotSize;
            int slotY = y + row * slotSize;

            // Verhindern von IndexOutOfBoundsException
            if (startIndex + i >= blocks.size()) break;

            Block block = blocks.get(startIndex + i);
            ItemStack stack = block.asItem() instanceof BlockItem ? new ItemStack(block) : ItemStack.EMPTY;

            if (!stack.isEmpty()) {
                context.drawItem(stack, slotX, slotY);
            }

            if (mouseX >= slotX && mouseX < slotX + slotSize && mouseY >= slotY && mouseY < slotY + slotSize) {
                context.fill(slotX, slotY, slotX + slotSize, slotY + slotSize, 0x80FFFFFF);
                hoveredStack = stack;
            }
        }

        if (!hoveredStack.isEmpty()) {
            context.drawItemTooltip(client.textRenderer, hoveredStack, mouseX, mouseY);
        }
    }

    public void scroll(int amount) {
        int columns = width / slotSize;
        if (columns == 0) return; // Division durch Null verhindern

        int maxRows = (int) Math.ceil((double) blocks.size() / columns) - height / slotSize;

        scrollOffset -= amount;

        if (scrollOffset < 0) scrollOffset = 0;
        if (maxRows < 0) maxRows = 0;
        if (scrollOffset > maxRows) scrollOffset = maxRows;
    }

    // ============================================
    // KORRIGIERTE SIGNATUREN FÜR 1.21.10
    // ============================================

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        // Daten aus dem Objekt holen (achten Sie auf .getX() vs .x())
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // 1. Prüfen, ob der Klick innerhalb des Widgets liegt
        if (mouseX < this.x || mouseX >= this.x + width || mouseY < this.y || mouseY >= this.y + height) {
            return false;
        }

        // 2. Nur Linksklick
        if (button != 0) return false;

        // 3. Index berechnen
        int columns = (slotSize > 0) ? width / slotSize : 1;
        int startIndex = scrollOffset * columns;

        int col = (int)((mouseX - this.x) / slotSize);
        int row = (int)((mouseY - this.y) / slotSize);
        int blockIndex = startIndex + row * columns + col;

        // 4. Block finden und Callback auslösen
        if (blockIndex >= 0 && blockIndex < blocks.size()) {
            Block clickedBlock = blocks.get(blockIndex);

            if (clickedBlock != null) {
                if (this.onBlockClicked != null) {
                    this.onBlockClicked.accept(clickedBlock);
                    return true;
                }
            }
        }
        return false;
    }


    // --- Implementierung der Element Methoden (Standard Signaturen) ---

    @Override
    public void mouseMoved(double mouseX, double mouseY) {}

    @Override
    public boolean mouseReleased(Click click) {
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // 1. Prüfen: Ist die Maus überhaupt über diesem Widget?
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        // 2. Anzahl der Spalten berechnen
        int columns = (this.width / slotSize);
        if (columns < 1) columns = 1;

        // 3. WICHTIG: Die Gesamtzahl der Items nehmen, die GERADE sichtbar sind
        // (Nutze hier deine gefilterte Liste, z.B. visibleBlocks oder filteredBlocks)
        int totalItems = blocks.size();

        // 4. Berechnen, wie viele Zeilen wir insgesamt haben
        // (Math.ceil rundet auf, damit auch unvollständige Zeilen zählen)
        int totalRows = (int) Math.ceil((double) totalItems / columns);

        // 5. Wie viele Zeilen passen gleichzeitig auf den Bildschirm?
        int visibleRows = this.height / slotSize;

        // 6. Das Maximum berechnen (Total Zeilen - Sichtbare Zeilen)
        int maxScroll = Math.max(0, totalRows - visibleRows);

        // 7. Den Scroll-Wert ändern
        // verticalAmount ist +1 (hoch) oder -1 (runter). Wir kehren es um oder passen es an.
        if (verticalAmount > 0) {
            scrollOffset--; // Hoch scrollen
        } else if (verticalAmount < 0) {
            scrollOffset++; // Runter scrollen
        }

        // 8. Begrenzen (Clamping): Nicht unter 0 und nicht über das Maximum
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        return true; // Event konsumiert
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return false;
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        return false;
    }

    @Override
    public boolean charTyped(CharInput input) {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {}
}