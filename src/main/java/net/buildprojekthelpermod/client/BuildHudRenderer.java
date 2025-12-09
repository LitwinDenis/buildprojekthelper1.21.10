package net.buildprojekthelpermod.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.buildprojekthelpermod.data.BuildProject;
import net.buildprojekthelpermod.data.ProjectManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;


import org.lwjgl.opengl.GL11;

import java.util.Map;

public class BuildHudRenderer implements HudRenderCallback {

    private final ProjectManager projectManager;
    private final MinecraftClient client;
    private final int SLOT_SIZE = 20;


    public BuildHudRenderer(MinecraftClient client) {
        this.projectManager = ProjectManager.getInstance();
        this.client = client;
    }

    private int countItemsInInventory(Block block) {
        if (client == null || client.player == null) return 0;

        int count = 0;

        net.minecraft.item.Item targetItem = block.asItem();

        System.out.println("DEBUG: Searching for item: " + net.minecraft.registry.Registries.ITEM.getId(targetItem));

        final int MAIN_INVENTORY_SIZE = 36;

        for (int i = 0; i < MAIN_INVENTORY_SIZE; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);

            if (!stack.isEmpty()) {
                // Direkter Item-Vergleich
                if (stack.getItem() == targetItem) {
                    count += stack.getCount();
                    System.out.println("DEBUG: Found " + stack.getCount() + " at slot " + i);
                }
            }
        }

        System.out.println("DEBUG: Total count for " + block.getName().getString() + ": " + count);

        return count;
    }

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        BuildProject currentProject = projectManager.getCurrentProject();

        if (currentProject == null || client.player == null) return;

        Map<Block, Integer> requiredBlocksMap = currentProject.getBlockMap();

        int startX = 20;
        int startY = 20;
        int yOffset = 0;
        final int MAX_DISPLAY = 5;
        final int SLOT_SIZE = 20;

        int index = 0;
        for (Map.Entry<Block, Integer> entry : requiredBlocksMap.entrySet()) {
            if (index >= MAX_DISPLAY) break;

            Block block = entry.getKey();
            int required = entry.getValue();

            if (required == 0) continue;

            int currentHeldCount = countItemsInInventory(block);
            ItemStack stack = new ItemStack(block);

            String fullDisplayString = currentHeldCount + "/" + required;

            //FF am anfang der hex damit nicht durchsichtig
            int textColor;
            if (currentHeldCount >= required) {
                textColor = 0xFF00FF00; // Grün
            } else if (currentHeldCount == 0) {
                textColor = 0xFFFF4444; // Rot
            } else {
                textColor = 0xFFFFFFFF; // Weiß
            }

            drawContext.drawItem(stack, startX, startY + yOffset);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            drawContext.drawText(
                    client.textRenderer,
                    net.minecraft.text.Text.literal(fullDisplayString),
                    startX + 22,
                    startY + yOffset + 6,
                    textColor,
                    true // Shadow
            );
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            yOffset += SLOT_SIZE;
            index++;
        }
    }
}



