package io.blodhgarm.personality.packets;

import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.reveal.InfoLevel;
import io.blodhgarm.personality.api.reveal.RevelInfoManager;
import io.blodhgarm.personality.client.gui.components.DiscoveryProgressComponent;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.blodhgarm.personality.server.ServerCharacters;
import io.blodhgarm.personality.utils.LookingUtils;
import io.wispforest.owo.network.ClientAccess;
import io.wispforest.owo.network.ServerAccess;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.hud.Hud;
import io.wispforest.owo.ui.hud.HudContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.UUID;

public class RevealCharacterPackets {
    public record RevealByInfoLevel(InfoLevel level, RevelInfoManager.RevealRange range, String uuid) {
        public static void revealInformationToPlayers(RevealByInfoLevel message, ServerAccess access){
            ServerCharacters manager = ServerCharacters.INSTANCE;

            Character sourceCharacter = manager.getCharacter(access.player());

            if(sourceCharacter == null) return;

            InfoLevel level = PersonalityMod.CONFIG.minimumRevealInfo();

            if(level.shouldUpdateLevel(message.level)) level = message.level;

            if(message.range == RevelInfoManager.RevealRange.DIRECTED){
                ServerPlayerEntity target = access.runtime().getPlayerManager().getPlayer(UUID.fromString(message.uuid));

                if(target == null) return;

                manager.revealCharacterInfo(access.player(), List.of(target), level);
            } else {
                manager.revealCharacterInfo(access.player(), message.range, level);
            }
        }
    }

    public record RevealByLooking(String soughtUUID){
        public static void doseRevealCharacter(RevealByLooking message, ServerAccess access){
            ServerPlayerEntity looker = access.player();
            Character lookerCharacter = ServerCharacters.INSTANCE.getCharacter(looker);

            if(lookerCharacter == null) return;

            ServerPlayerEntity sought = access.runtime().getPlayerManager().getPlayer(UUID.fromString(message.soughtUUID()));

            if(sought == null) return;

            Character soughtCharacter = (Character) sought.getCharacter(false);

            if(soughtCharacter == null) return;

            boolean bl = PersonalityMod.hasEffect(sought, PersonalityTags.StatusEffects.OBSCURING_EFFECTS)
                    || lookerCharacter.doseKnowCharacter(sought)
                    || sought.getInventory().armor.get(3).isIn(PersonalityTags.Items.OBSCURES_IDENTITY)
                    || !LookingUtils.isPlayerStaring(looker, sought, 64);

            if(bl) return;

            //TODO: Add packet or send something to change how this works idk

            ServerCharacters.INSTANCE.revealCharacterInfo(soughtCharacter, lookerCharacter, looker, PersonalityMod.CONFIG.minimumDiscoveryInfo(), (msg, action) -> {
                if(action.equals("New Character Introduced")){
                    Networking.sendS2C(looker, new RevealCharacterPackets.SuccessfulDiscovery());

                    return new ServerCharacters.ReturnInformation("You take note of the given person, maybe you can ask them for more information about them.", "Character Discovered", true);
                }

                return null;
            });

        }
    }

    public record SuccessfulDiscovery(){

        @Environment(EnvType.CLIENT)
        public static void onDiscovery(SuccessfulDiscovery message, ClientAccess access){
            boolean hasComponent = Hud.hasComponent(PersonalityMod.id("reveal_layout"));

            if(hasComponent){
                var color = ((FlowLayout) Hud.getComponent(PersonalityMod.id("reveal_layout")))
                        .childById(DiscoveryProgressComponent.class, "progress_bar")
                        .primaryColor;

                color.animate(250, Easing.LINEAR, new Color(0.1f, 0.55f, 0.25f))
                        .forwards();
            }
        }
    }


}
