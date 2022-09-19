package io.blodhgarm.personality.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.blodhgarm.personality.server.config.PersonalityConfig;
import io.github.apace100.calio.mixin.DamageSourceAccessor;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.packets.SyncS2CPackets;
import io.blodhgarm.personality.Character;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PersonalityServer implements ModInitializer {

	public static final DamageSource DEATH_BY_OLD_AGE = DamageSourceAccessor.createDamageSource("oldAge");
	private static final UUID NO_STICK_SLOWNESS = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278F");

	public static MinecraftServer server;

	@Override
	public void onInitialize() {
		Commands.register();
		Networking.registerNetworking();

		ServerTickEvents.END_WORLD_TICK.register(PersonalityMod.id("tick"), PersonalityServer::onTick);
		ServerWorldEvents.LOAD.register(PersonalityMod.id("on_world_load"), PersonalityServer::onWorldLoad);
		ServerPlayConnectionEvents.JOIN.register(PersonalityMod.id("on_player_join"), PersonalityServer::onPlayerJoin);
	}

	public static ServerPlayerEntity getPlayer(String uuid) {
		return server.getPlayerManager().getPlayer(uuid);
	}

	public static void onWorldLoad(MinecraftServer server, ServerWorld world) {
		PersonalityServer.server = server;
		ServerCharacters.loadCharacterReference();
	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		List<String> characters = new ArrayList<>();

		for (Character c : ServerCharacters.characterIDToCharacter.values())
			characters.add(GSON.toJson(c));

		Networking.sendS2C(handler.player, new SyncS2CPackets.Initial(characters, ServerCharacters.playerIDToCharacterID));
	}

	public static void onTick(ServerWorld world) {
		for (ServerPlayerEntity player : world.getPlayers()) {
			Character c = ServerCharacters.getCharacter(player);
			if (c == null)
				return;

			if (c.getAge() >= c.getMaxAge()) {
				ServerCharacters.killCharacter(c);
				player.damage(DEATH_BY_OLD_AGE, Float.MAX_VALUE);
				continue;
			}

			PersonalityConfig.GradualValue config = PersonalityMod.CONFIG.NO_STICK_SLOWNESS;
			EntityAttributeInstance instance = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
			if (PersonalityMod.shouldGradualValue(config, c) && !player.getOffHandStack().isIn(PersonalityMod.WALKING_STICKS) && !player.getMainHandStack().isIn(PersonalityMod.WALKING_STICKS)) {
				if (instance.getModifier(NO_STICK_SLOWNESS) == null)
					instance.addTemporaryModifier(new EntityAttributeModifier(NO_STICK_SLOWNESS, "Old Person with No Stick", PersonalityMod.getGradualValue(config, c), EntityAttributeModifier.Operation.MULTIPLY_BASE));
			}
			else {
				instance.tryRemoveModifier(NO_STICK_SLOWNESS);
			}

		}

	}

}
