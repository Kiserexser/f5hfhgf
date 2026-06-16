package com.example.speed.mixin;

import com.example.speed.SpeedMod;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (key == 54 && action == 1) { // правый Shift
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen instanceof SpeedMod.ModMenuScreen) {
                client.currentScreen.close();
            } else {
                client.setScreen(new SpeedMod.ModMenuScreen());
            }
            ci.cancel();
        }
    }
}
