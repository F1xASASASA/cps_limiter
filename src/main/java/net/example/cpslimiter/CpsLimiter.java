package net.example.cpslimiter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CpsLimiter implements ClientModInitializer {
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        CpsNetworking.init();

        // Регистрация кнопки
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.cpslimiter.toggle", 
                InputUtil.Type.KEYSYM, 
                GLFW.GLFW_KEY_K, 
                "category.cpslimiter"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Обработка нажатия
            while (toggleKey.wasPressed()) {
                CpsNetworking.showCpsLocally = !CpsNetworking.showCpsLocally;
                
                // Чтобы при выключении текст СРАЗУ исчез, отправим пустое сообщение в Action Bar
                if (!CpsNetworking.showCpsLocally) {
                    client.player.sendMessage(Text.empty(), true);
                }
            }

            // ВЫВОД CPS (Action Bar)
            if (CpsNetworking.showCpsLocally) {
                int myCps = CpsNetworking.getCurrentCps();
                client.player.sendMessage(Text.literal("§7Ваш CPS: §a" + myCps), true);
            }
        });

        // Сетевые события
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                CpsNetworking.startPinging(client.player.getUuid());
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            CpsNetworking.stopPinging();
            CpsNetworking.MOD_USERS_CPS.clear();
        });
    }
}