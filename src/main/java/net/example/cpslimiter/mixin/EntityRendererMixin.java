package net.example.cpslimiter.mixin;

import net.example.cpslimiter.CpsNetworking;
import net.example.cpslimiter.access.PlayerStateAccessor;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import java.util.UUID;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<S extends EntityRenderState> {

    /**
     * Этот миксин перехватывает переменную 'text' (который является ником игрока)
     * ПЕРЕД тем как игра его нарисует. Если CPS найден, мы приклеиваем его слева.
     */
    @ModifyVariable(method = "renderLabelIfPresent", at = @At("HEAD"), argsOnly = true)
    private Text prependCpsToName(Text original, S state) {
        // 1. ПРОВЕРКА НАСТРОЙКИ: Если отображение выключено клавишей K — возвращаем ник без изменений
        if (!CpsNetworking.showCpsLocally) {
            return original;
        }

        // 2. ПРОВЕРКА ТИПА: Нас интересуют только игроки
        if (state instanceof PlayerEntityRenderState playerState) {
            
            // 3. ПОЛУЧАЕМ UUID: Используем интерфейс PlayerStateAccessor, чтобы не было краша
            UUID uuid = ((PlayerStateAccessor) playerState).cpslimiter$getUuid();
            
            if (uuid != null) {
                // 4. ПОЛУЧАЕМ CPS: Ищем данные в нашей мапе из сетевого модуля
                Integer cps = CpsNetworking.MOD_USERS_CPS.get(uuid);
                
                // Если данные есть, склеиваем: "[10 CPS] " + "Никнейм"
                if (cps != null) {
                    return Text.literal("[" + cps + " CPS] ")
                            .formatted(Formatting.GREEN) // Делаем префикс зеленым
                            .append(original);           // Приклеиваем оригинальный ник (с его цветами)
                }
            }
        }

        // Если это не игрок или данных нет — возвращаем обычный текст
        return original;
    }
}