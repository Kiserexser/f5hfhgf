package com.example.speed;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class SpeedMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("speedmod");

    @Override
    public void onInitialize() {
        LOGGER.info("Speed Mod initialized!");
    }

    // ==================== ВНУТРЕННИЙ МИКСИН ====================

    @Mixin(ClientPlayerEntity.class)
    public static class MixinClientPlayerTick {
        @Inject(method = "tick", at = @At("TAIL"))
        private void onTick(CallbackInfo ci) {
            ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.player != player) return;

            // 1. Всегда прыгать (onInput)
            player.input.jumping = true;

            // 2. Расчёт движения (onTick)
            double grim = 0.03;
            if (player.isOnGround()) {
                grim *= 2.8500699;
            } else {
                grim *= 1.0200699;
            }

            float yaw = player.getYaw() + 90f; // аналог MovementUtility.getdir()
            double rad = Math.toRadians(yaw);
            double mx = grim * Math.cos(rad);
            double mz = grim * Math.sin(rad);

            player.setVelocity(player.getVelocity().add(mx, 0.0, mz));

            if (!player.isOnGround()) {
                player.setVelocity(player.getVelocity().add(0.0, -0.050699, 0.0));
            }

            // 3. Отправка пакетов (onPreMotion)
            if (mc.player != null && mc.player.networkHandler != null) {
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.StatusOnly(false));
                mc.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING)
                );
            }
        }
    }
}
