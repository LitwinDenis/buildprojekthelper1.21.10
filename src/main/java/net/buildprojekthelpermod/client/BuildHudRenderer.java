package net.buildprojekthelpermod.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.buildprojekthelpermod.data.BuildProject;
import net.buildprojekthelpermod.data.ProjectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.option.GameOptions;

import java.util.Map;
import java.util.Set;

public class BuildHudRenderer implements HudRenderCallback {

    private final ProjectManager projectManager;
    private final MinecraftClient client;
    // SLOT_SIZE wird unten lokal definiert, kann hier weg oder bleiben

    public BuildHudRenderer(MinecraftClient client) {
        this.projectManager = ProjectManager.getInstance();
        this.client = client;
    }

    private int countItemsInInventory(Block block) {
        if (client == null || client.player == null) return 0;

        int count = 0;
        net.minecraft.item.Item targetItem = block.asItem();
        final int MAIN_INVENTORY_SIZE = 36;

        for (int i = 0; i < MAIN_INVENTORY_SIZE; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);

            if (!stack.isEmpty()) {
                if (stack.getItem() == targetItem) {
                    count += stack.getCount();
                }
            }
        }
        return count;
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        try {
            if (client.player == null) return;

            BuildProject currentProject = projectManager.getCurrentProject();
            if (currentProject == null) return;

            // Hier würde es crashen, wenn wir den Fix in BuildProject nicht hätten
            Set<Block> pinnedBlocks = currentProject.getPinnedBlocks();

            if (pinnedBlocks == null || pinnedBlocks.isEmpty()) return;

            Map<Block, Integer> allRequirements = currentProject.getBlockMap();
            if (allRequirements == null) return;

            int startX = 20;
            int startY = 20;
            int yOffset = 0;
            final int SLOT_SIZE = 20;

            for (Block block : pinnedBlocks) {
                if (block == null) continue;

                int required = allRequirements.getOrDefault(block, 0);
                int currentHeldCount = countItemsInInventory(block);
                ItemStack stack = new ItemStack(block);

                String fullDisplayString = currentHeldCount + "/" + required;

                int textColor;
                if (currentHeldCount >= required) {
                    textColor = 0xFF00FF00;
                } else if (currentHeldCount == 0) {
                    textColor = 0xFFFF4444;
                } else {
                    textColor = 0xFFFFFFFF;
                }

                drawContext.drawItem(stack, startX, startY + yOffset);

                GL11.glDisable(GL11.GL_DEPTH_TEST);
                drawContext.drawText(
                        client.textRenderer,
                        Text.literal(fullDisplayString),
                        startX + 22,
                        startY + yOffset + 6,
                        textColor,
                        true
                );
                GL11.glEnable(GL11.GL_DEPTH_TEST);

                yOffset += SLOT_SIZE;
            }
        } catch (Exception e) {
            // Falls DOCH ein Fehler passiert: Fehler drucken, aber NICHT abstürzen!
            System.out.println("HUD Error: " + e.getMessage());
            // e.printStackTrace(); // Kannst du einkommentieren für mehr Details
        }
    }
}