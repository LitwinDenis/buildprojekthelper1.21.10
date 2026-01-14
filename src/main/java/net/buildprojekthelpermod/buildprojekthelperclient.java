package net.buildprojekthelpermod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import net.buildprojekthelpermod.gui.ProjectHelperScreen;
import net.buildprojekthelpermod.data.ProjectManager;
import net.buildprojekthelpermod.client.BuildHudRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;



public class buildprojekthelperclient implements ClientModInitializer {

    public static KeyBinding OPEN_GUI_KEY;

    @Override
    public void onInitializeClient() {


        OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.buildprojekthelper.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                KeyBinding.Category.MISC
        ));


        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;

            while (OPEN_GUI_KEY.wasPressed()) {
                client.player.sendMessage(
                        Text.literal("§aBuildProjektHelper GUI is opening..."),
                        false
                );

                client.setScreen(new ProjectHelperScreen());

            }

        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            System.out.println("Welt betreten -> Lade Projekte...");
            ProjectManager.getInstance().load();
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            System.out.println("Welt verlassen -> Speichere Projekte...");
            ProjectManager.getInstance().save();

        });

        MinecraftClient client = MinecraftClient.getInstance();
        HudRenderCallback.EVENT.register(new BuildHudRenderer(client));
    }
}
