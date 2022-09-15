package io.wispforest.personality.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.apace100.calio.mixin.DamageSourceAccessor;
import io.wispforest.personality.PersonalityMod;
import io.wispforest.personality.Networking;
import io.wispforest.personality.packets.SyncS2CPackets;
import io.wispforest.personality.server.config.Config;
import io.wispforest.personality.Character;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class PersonalityServer implements ModInitializer {

	public static final DamageSource DEATH_BY_OLD_AGE = DamageSourceAccessor.createDamageSource("oldAge");

	public static MinecraftServer server;

	@Override
	public void onInitialize() {
		Commands.register();
		Networking.registerNetworking();

		ServerTickEvents.END_WORLD_TICK.register(PersonalityMod.id("tick"), PersonalityServer::onTick);
		ServerWorldEvents.LOAD.register(PersonalityMod.id("on_world_load"), PersonalityServer::onWorldLoad);
		ServerPlayConnectionEvents.JOIN.register(PersonalityMod.id("on_player_join"), PersonalityServer::onPlayerJoin);
	}

	public static void onWorldLoad(MinecraftServer server, ServerWorld world) {
		PersonalityServer.server = server;
		ServerCharacters.loadCharacterReference();
	}

	public static void onTick(ServerWorld world) {
		for (ServerPlayerEntity player : world.getPlayers()) {
			Character c = ServerCharacters.getCharacter(player);
			if (c == null)
				return;

			if (c.getAge() >= c.getMaxAge()) {
				ServerCharacters.deleteCharacter(c.getUUID());
				player.damage(DEATH_BY_OLD_AGE, Float.MAX_VALUE);
				continue;
			}

			if (Config.OLD_PERSON_SLOWNESS_WITHOUT_STICK > 0) {
				if (c.getStage() == Character.Stage.OLD)
					if (player.getOffHandStack().getItem() != Items.STICK && player.getMainHandStack().getItem() != Items.STICK)
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 5, Config.OLD_PERSON_SLOWNESS_WITHOUT_STICK -1, true, false, true));
			}

		}

	}

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static void onPlayerJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		List<String> characters = new ArrayList<>();

		for (Character c : ServerCharacters.characterIDToCharacter.values())
			characters.add(GSON.toJson(c));

		Networking.sendS2C(handler.player, new SyncS2CPackets.Initial(characters, ServerCharacters.playerIDToCharacterID));
	}

}
