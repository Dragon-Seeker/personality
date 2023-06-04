package io.blodhgarm.personality.mixin.client;

import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.client.ClientCharacters;
import io.blodhgarm.personality.misc.pond.CharacterToPlayerLink;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Mixin(ClientWorld.ClientEntityHandler.class)
    public abstract static class ClientEntityHandler {

        @Inject(method = "startTracking(Lnet/minecraft/entity/Entity;)V", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 0, shift = At.Shift.BY, by = 2))
        private void personality$updateBasedOnClientsKnownCharacters(Entity entity, CallbackInfo ci){
            if (!(entity instanceof AbstractClientPlayerEntity player)) return;

            Character characterOther = ClientCharacters.INSTANCE.getCharacter(player);

            if(characterOther == null) return;

//            ((CharacterToPlayerLink<AbstractClientPlayerEntity>) (player))
//                    .setCharacter(ClientCharacters.INSTANCE.getKnownCharacter(player));

            //TODO: MAYBE HOOK INTO EVENTS TO APPLY ADDONS ON THE CLIENT WHEN NEW PLAYERS ENTER THE CLIENTS WORLD AND VISE VERSA
            // Use net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents
            ClientCharacters.INSTANCE.applyAddons(player);

            //ClientCharacters.INSTANCE.setKnownCharacters(new PlayerAccess(player), characterOther.getUUID());
        }
    }
}
