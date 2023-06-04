package io.blodhgarm.personality.client;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.client.gui.components.DiscoveryProgressComponent;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.blodhgarm.personality.misc.pond.owo.AnimationExtension;
import io.blodhgarm.personality.packets.RevealCharacterPackets;
import io.blodhgarm.personality.utils.LookingUtils;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class ClientCharacterTick implements ClientTickEvents.EndWorldTick {

    public static ClientCharacterTick INSTANCE = new ClientCharacterTick();

    public int baseRange = 64;

    public String lastPlayerUUID = "";

    public int timeLookedAt = 0;
    public boolean sentPacket = false;

    public int delayBeforeReset = -1;

    public boolean disableDiscovery = false;

    @Override
    public void onEndTick(ClientWorld world) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        Character clientCharacter = ClientCharacters.INSTANCE.getClientCharacter();

        String currentPlayerUUID = "";

        boolean bl = clientCharacter != null
                && !PersonalityMod.hasEffect(player, PersonalityTags.StatusEffects.REVEAL_BLINDING_EFFECTS)
                && !disableDiscovery
                && PersonalityMod.CONFIG.autoDiscovery();

        if(bl) {
            for (Entity entity : world.getOtherEntities(player, Box.of(player.getPos(), baseRange, baseRange, baseRange))) {
                boolean bl2 = !(entity instanceof PlayerEntity otherPlayer)
                        || PersonalityMod.hasEffect(otherPlayer, PersonalityTags.StatusEffects.OBSCURING_EFFECTS)
                        || clientCharacter.doseKnowCharacter(otherPlayer)
                        || otherPlayer.getInventory().armor.get(3).isIn(PersonalityTags.Items.OBSCURES_IDENTITY)
                        || !LookingUtils.isPlayerStaring(player, otherPlayer, baseRange);

                if (bl2) continue;

                if (!lastPlayerUUID.equals(entity.getUuidAsString())) timeLookedAt = 0;

                timeLookedAt++;

                currentPlayerUUID = entity.getUuidAsString();

                break;
            }
        }

        if(delayBeforeReset > -1) {
            delayBeforeReset -= 1;
        }

        if (currentPlayerUUID.isEmpty() && timeLookedAt != 0) {
            if (delayBeforeReset == -2){
                delayBeforeReset = 40;
            }
        } else {
            delayBeforeReset = -2;

            lastPlayerUUID = currentPlayerUUID;
        }

        if(delayBeforeReset > 0) return;

        boolean componentInHud = Hud.hasComponent(PersonalityMod.id("reveal_layout"));

        if(componentInHud) {
            var property = ((FlowLayout) Hud.getComponent(PersonalityMod.id("reveal_layout")))
                    .childById(DiscoveryProgressComponent.class, "progress_bar")
                    .alphaProperty();

            if(property.animation() == null){
                ((AnimationExtension<?>) property.animate(1000, Easing.LINEAR, DiscoveryProgressComponent.AnimatableObject.ofFloat(0.0f))
                        .forwards()).setOnCompletionEvent(animation -> {
                    if (animation.direction() != Animation.Direction.FORWARDS) return;

                    Hud.remove(PersonalityMod.id("reveal_layout"));

                    timeLookedAt = 0;

                    lastPlayerUUID = "";
                });
            }

            if(currentPlayerUUID.isEmpty()) {
                if(property.animation().direction() != Animation.Direction.FORWARDS) property.animation().forwards();
            } else if(property.animation().direction() != Animation.Direction.BACKWARDS) {
                property.animation().backwards();
            }

        } else if(!currentPlayerUUID.isEmpty() && timeLookedAt > 0) {
            //Hud.remove(PersonalityMod.id("reveal_progress"));

            Hud.add(PersonalityMod.id("reveal_layout"), () -> {
                return Containers.verticalFlow(Sizing.content(), Sizing.content())
                        .child(new DiscoveryProgressComponent()
                                .sizing(Sizing.fixed(30), Sizing.fixed(6))
                                .id("progress_bar")
                        )
                        .positioning(Positioning.relative(50, 59));
            });
        }

        if(timeLookedAt > 50){
            if(!sentPacket) {
                Networking.sendC2S(new RevealCharacterPackets.RevealByLooking(lastPlayerUUID));

                sentPacket = true;
            }

            if((timeLookedAt - 50) % 600f == 599f) sentPacket = false;
        } else if (sentPacket) {
            sentPacket = false;
        }
    }

}
