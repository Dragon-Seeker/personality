package io.wispforest.personality.initializers;

import io.github.apace100.calio.mixin.DamageSourceAccessor;
import io.wispforest.personality.Commands;
import io.wispforest.personality.PersonalityMod;
import io.wispforest.personality.PersonalityNetworking;
import io.wispforest.personality.config.Config;
import io.wispforest.personality.storage.Character;
import io.wispforest.personality.storage.CharacterManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class ServerSideInitializer implements ModInitializer {

	public static final DamageSource DEATH_BY_OLD_AGE = DamageSourceAccessor.createDamageSource("oldAge");

	public static MinecraftServer server;

	@Override
	public void onInitialize() {
		Commands.register();
		CharacterManager.loadCharacterReference();
		PersonalityNetworking.registerNetworking();

		ServerLifecycleEvents.SERVER_STARTED.register(PersonalityMod.id("on_startup"), ServerSideInitializer::onStart);
		ServerTickEvents.END_WORLD_TICK.register(PersonalityMod.id("tick"), ServerSideInitializer::onTick);
	}

	public static void onStart(MinecraftServer server) {
		ServerSideInitializer.server = server;
	}

	public static void onTick(ServerWorld world) {
		for (ServerPlayerEntity player : world.getPlayers()) {
			Character c = CharacterManager.getCharacter(player);
			if (c == null)
				return;

			if (c.getAge() >= c.getMaxAge()) {
				CharacterManager.deleteCharacter(c.getUUID());
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

}
