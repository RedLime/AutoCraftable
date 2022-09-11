package com.redlimerl.mc.autocraftable.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Shadow private MinecraftClient client;

    @Redirect(method = "onTitle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V"))
    public void onTitleMixin(InGameHud instance, Text message, boolean tinted) {
        instance.setOverlayMessage(message, tinted);
        if (message.getString().isEmpty()) {
            resetRecipeBook();
        }
    }

    @Inject(method = "onPlaySoundId", at = @At("TAIL"))
    public void onPlaySoundIdMixin(PlaySoundIdS2CPacket packet, CallbackInfo ci) {
        if (packet.getSoundId().toString().equals("autocraftable:reset_recipe_book")) {
            resetRecipeBook();
        }
    }

    private void resetRecipeBook() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ClientRecipeBook book = player.getRecipeBook();
            book.setGuiOpen(false);
            book.setFilteringCraftable(false);
            book.setBlastFurnaceFilteringCraftable(false);
            book.setBlastFurnaceGuiOpen(false);
            book.setFurnaceFilteringCraftable(false);
            book.setFurnaceGuiOpen(false);

            ClientPlayNetworkHandler networkHandler = this.client.getNetworkHandler();
            if (networkHandler != null)
                networkHandler.sendPacket(new RecipeBookDataC2SPacket(false, false, false, false, false, false));
        }
    }


}
