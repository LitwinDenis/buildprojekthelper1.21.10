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
import net.buildprojekthelpermod.client.BuildHudRenderer; // Der neue Renderer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;


public class buildprojekthelperclient implements ClientModInitializer {

    public static KeyBinding OPEN_GUI_KEY;

    @Override
    public void onInitializeClient() {

        // Keybind registrieren
        OPEN_GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.buildprojekthelper.open_gui",   // Übersetzungsschlüssel
                InputUtil.Type.KEYSYM,              // Eingabetyp
                GLFW.GLFW_KEY_P,                    // Taste
                KeyBinding.Category.MISC            // Kategorie (kein String mehr!)
        ));

        // Event für Keybind
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;

            while (OPEN_GUI_KEY.wasPressed()) {
                client.player.sendMessage(
                        Text.literal("§aBuildProjektHelper GUI wird geöffnet..."),
                        false
                );

                client.setScreen(new ProjectHelperScreen());

                // client.setScreen(new DeinGuiScreen());
            }
        });

        MinecraftClient client = MinecraftClient.getInstance();
        HudRenderCallback.EVENT.register(new BuildHudRenderer(client));
    }
}
