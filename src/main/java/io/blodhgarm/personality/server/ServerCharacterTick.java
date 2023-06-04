package io.blodhgarm.personality.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.api.character.ServerCharacter;
import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.misc.PersonalityTags;
import io.blodhgarm.personality.misc.config.ConfigHelper;
import io.blodhgarm.personality.misc.config.PersonalityConfig;
import io.blodhgarm.personality.misc.pond.DamageSourceExtended;
import io.blodhgarm.personality.packets.CharacterDeathPackets;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.wispforest.owo.Owo;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCharacterTick implements ServerTickEvents.EndWorldTick {

    public static final CustomDamageSource DEATH_BY_OLD_AGE = new CustomDamageSource("oldAge");
    public static final CustomDamageSource DEATH_BY_RUNNING = new CustomDamageSource("trippedByDeath");

    private static final UUID AGING_SLOWNESS_MODIFIER_UUID = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278F");

    private int currentPacketDelay = 0;

    private static final Map<String, DeathTracker> deathTrackerMap = new ConcurrentHashMap<>();

    private int checkForDeathScreen = 0;

    public static final Map<String, Boolean> hasDeathScreenOpen = new ConcurrentHashMap<>();

    @Override
    public void onEndTick(ServerWorld world) {
        List<Character> onlineCharacters = new ArrayList<>();

        for (ServerPlayerEntity player : world.getPlayers()) {
            Character c = ServerCharacters.INSTANCE.getCharacter(player);

            if (c == null) continue;

            onlineCharacters.add(c);

            ((ServerCharacter) c).updateCurrentPlaytime();

            String playerUUID = player.getUuidAsString();

            // Kill Too Old Characters
            if (c.getAge() >= c.getMaxAge() && !deathTrackerMap.containsKey(playerUUID) && !c.isDead()) {
                //TODO: Figure out way for handling Characters that don't have players that are online

                deathTrackerMap.put(playerUUID, new DeathTracker(playerUUID).resetTimer());

                continue;
            }

            // Apply Slowness to Old Characters without a stick
            PersonalityConfig.GradualValue config = PersonalityMod.CONFIG.agingSlowness;
            EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);

            boolean check = ConfigHelper.shouldApply(config, c)
                    && !player.getOffHandStack().isIn(PersonalityTags.Items.WALKING_STICKS)
                    && !player.getMainHandStack().isIn(PersonalityTags.Items.WALKING_STICKS);

            if (!check) {
                instance.removeModifier(AGING_SLOWNESS_MODIFIER_UUID);

                continue;
            }

            if (instance.getModifier(AGING_SLOWNESS_MODIFIER_UUID) != null) continue;

            instance.addTemporaryModifier(new EntityAttributeModifier(AGING_SLOWNESS_MODIFIER_UUID, "Old Person with No Stick", -ConfigHelper.apply(config, c), EntityAttributeModifier.Operation.MULTIPLY_BASE));
        }

        deathTrackerMap.forEach((s, deathTracker) -> {
            if(hasDeathScreenOpen.get(s)) return;

            deathTracker.reduceTimer();
        });

        checkForDeathScreen++;

        if(checkForDeathScreen > (31 * 20)){
            deathTrackerMap.forEach((s, deathTracker) -> {
                ServerPlayerEntity player = Owo.currentServer().getPlayerManager().getPlayer(s);

                if(player != null) {
                    Networking.sendS2C(Owo.currentServer().getPlayerManager().getPlayer(s), new CharacterDeathPackets.CheckDeathScreenOpen());
                } else {
                    hasDeathScreenOpen.put(s, false);
                }
            });
        }

        currentPacketDelay++;

        if(currentPacketDelay > (30 * 20)){
            if(!onlineCharacters.isEmpty()) {
                JsonArray jsonArray = new JsonArray();

                for (Character onlineCharacter : onlineCharacters) {
                    JsonObject jsonObject = new JsonObject();

                    jsonObject.addProperty("uuid", onlineCharacter.getUUID());
                    jsonObject.addProperty("currentPlaytime", onlineCharacter.getCurrentPlaytime());

                    jsonArray.add(jsonObject);
                }

                Networking.sendToAll(new SyncS2CPackets.SyncOnlinePlaytimes(jsonArray.toString()));
            }

            currentPacketDelay = 0;
        }
    }

    public static void killCharacter(String playerUUID, CustomDamageSource source){
        PlayerManager manager = Owo.currentServer().getPlayerManager();

        ServerPlayerEntity player = manager.getPlayer(playerUUID);

        Character c = ServerCharacters.INSTANCE.getCharacter(ServerCharacters.INSTANCE.getCharacterUUID(playerUUID));

        ServerCharacters.INSTANCE.killCharacter(c);

        if(player != null){
            if(PersonalityMod.CONFIG.killPlayerOnCharacterDeath()) {
                ((DamageSourceExtended) source).disableDeathMessage(true);

                player.damage(source, Float.MAX_VALUE);

                ((DamageSourceExtended) source).disableDeathMessage(false);
            }
        } else {
            source.withCustomMessage(" wasn't able to run from death forever, even when being offline!");
        }

        Networking.sendToAll(new CharacterDeathPackets.ReceivedDeathMessage(playerUUID, source.message, source == DEATH_BY_RUNNING));

        deathTrackerMap.remove(playerUUID);
    }

    public static class DeathTracker {

        private final String playerUUID;

        public int numberOfTries = 0;

        public int attemptTimer = 0;

        public DeathTracker(String playerUUID){
            this.playerUUID = playerUUID;
        }

        public void reduceTimer(){
            this.attemptTimer--;

            if(this.numberOfTries > 3){
                killCharacter(playerUUID, DEATH_BY_RUNNING);

                return;
            }

            if(this.attemptTimer < 0){
                this.numberOfTries++;

                this.resetTimer();
            }
        }

        public DeathTracker resetTimer(){
            this.attemptTimer = PersonalityMod.CONFIG.characterDeathWindow();

            PlayerAccess<ServerPlayerEntity> playerAccess = ServerCharacters.INSTANCE.getPlayer(this.playerUUID);

            if(playerAccess.playerValid()) {
                Networking.sendS2C(playerAccess.player(), new CharacterDeathPackets.OpenCharacterDeathScreen());
            }

            return this;
        }
    }

    public static class CustomDamageSource extends DamageSource {

        protected final String message;

        public CustomDamageSource(String name) {
            this(name, "");
        }

        public CustomDamageSource(String name, String message) {
            super(name);

            this.message = message;
        }

        public CustomDamageSource withCustomMessage(String message){
            if(message.isBlank()) return this;

            return new CustomDamageSource(name, message);
        }

        @Override
        public Text getDeathMessage(LivingEntity entity) {
            Text text = Text.of("");

            if(message.isBlank()){
                text = super.getDeathMessage(entity);
            }

            if(!Objects.equals(text, Text.empty()) && !message.isBlank()){
                text = entity.getDisplayName().copy()
                        .append(Text.of(" " + this.message));
            }

            return text;
        }
    }
}
