package io.blodhgarm.personality.client;

import com.mojang.logging.LogUtils;
import dev.kosmx.playerAnim.api.layered.AnimationContainer;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddonRegistry;
import io.blodhgarm.personality.api.core.BaseRegistry;
import io.blodhgarm.personality.compat.origins.client.gui.OriginSelectionDisplayAddon;
import io.blodhgarm.personality.compat.pehkui.client.PehkuiScaleDisplayAddon;
import io.blodhgarm.personality.compat.trinkets.TrinketsGlasses;
import io.blodhgarm.personality.mixin.client.owo.PositioningMixin;
import io.wispforest.owo.ui.core.Positioning;
import ladysnake.satin.api.event.ShaderEffectRenderCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class PersonalityClient implements ClientModInitializer {

    public static Positioning.Type RELATIVE_WITHOUT_CHILD;

    private static Logger LOGGER = LogUtils.getLogger();

    private static final Identifier CANE_WALK_ANIMATION_ID = PersonalityMod.id("cane_walk_test");
    private static final Identifier CANE_RUN_ANIMATION_ID = PersonalityMod.id("cane_run_test");

    private static KeyframeAnimation CANE_WALK_ANIMATION = null;//PlayerAnimationRegistry.getAnimation(CANE_WALK_ANIMATION_ID);
    private static KeyframeAnimation CANE_RUN_ANIMATION = null;//PlayerAnimationRegistry.getAnimation(CANE_RUN_ANIMATION_ID);

    private static boolean UNABLE_TO_LOAD_WALK_ANIMATION = false;
    private static boolean UNABLE_TO_LOAD_RUN_ANIMATION = false;

    @Override
    public void onInitializeClient() {
        Networking.registerNetworkingClient();
		ShaderEffectRenderCallback.EVENT.register(new BlurryVisionShaderEffect());

		KeyBindings.init();
        ClientTickEvents.END_WORLD_TICK.register(KeyBindings::processKeybindings);

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientCharacters.INSTANCE.onPlayDisconnect(handler, client);

            for (BaseRegistry value : BaseRegistry.REGISTRIES.values()) value.clearRegistry();
        });

        if(FabricLoader.getInstance().isModLoaded("trinkets")){
            TrinketsGlasses.clientInit();
        }

        if(FabricLoader.getInstance().isModLoaded("origins")){
            PersonalityScreenAddonRegistry.registerScreenAddon(new Identifier("origins", "origin_selection_addon"), OriginSelectionDisplayAddon::new);
        }

        if(FabricLoader.getInstance().isModLoaded("pehkui")){
            PersonalityScreenAddonRegistry.registerScreenAddon(new Identifier("pehkui", "scale_selection_addon"), PehkuiScaleDisplayAddon::new);
        }

        GeoItemRenderer.registerItemRenderer(PersonalityMod.CANE, new GeoItemRenderer<>(new WalkingStickModel()));

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CANE_WALK_ANIMATION_ID, 21, player -> new AnimationContainer<>());

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(CANE_RUN_ANIMATION_ID, 22, player -> new AnimationContainer<>());

        ClientTickEvents.START_WORLD_TICK.register(world -> {
            world.getPlayers().forEach(player -> {
                boolean caneInHand = (player.getOffHandStack().isOf(PersonalityMod.CANE) || player.getMainHandStack().isOf(PersonalityMod.CANE));

                PlayerAnimationAccess.PlayerAssociatedAnimationData data = PlayerAnimationAccess.getPlayerAssociatedData(player);

                if(!UNABLE_TO_LOAD_WALK_ANIMATION){
                    AnimationContainer<IAnimation> walkContainer = (AnimationContainer<IAnimation>) data.get(CANE_WALK_ANIMATION_ID);

                    try {
                        boolean isAnim1NotNull = walkContainer.getAnim() != null;

                        if(caneInHand && !player.isSprinting() && player.forwardSpeed > 0.0){
                            if(isAnim1NotNull) return;

                            if(CANE_WALK_ANIMATION == null){
                                KeyframeAnimation.AnimationBuilder builder = PlayerAnimationRegistry.getAnimation(CANE_WALK_ANIMATION_ID).mutableCopy();

                                builder.isLooped = true;

                                CANE_WALK_ANIMATION = builder.build();
                            }

                            walkContainer.setAnim(new KeyframeAnimationPlayer(CANE_WALK_ANIMATION, 3, true));
                        } else if(isAnim1NotNull) {
                            walkContainer.setAnim(null);
                        }
                    } catch (IllegalArgumentException e){
                        LOGGER.error("Unable to load the Cane Walk Animation, meaning animation will not play!");

                        UNABLE_TO_LOAD_WALK_ANIMATION = true;
                    }
                }

                if(!UNABLE_TO_LOAD_RUN_ANIMATION){
                    AnimationContainer<IAnimation> runContainer = (AnimationContainer<IAnimation>) data.get(CANE_RUN_ANIMATION_ID);

                    try{
                        boolean isAnim2NotNull = runContainer.getAnim() != null;

                        if(caneInHand && player.isSprinting() && player.forwardSpeed > 0.0){
                            if(isAnim2NotNull) return;

                            if(CANE_RUN_ANIMATION == null) {
                                KeyframeAnimation.AnimationBuilder builder = PlayerAnimationRegistry.getAnimation(CANE_RUN_ANIMATION_ID).mutableCopy();

                                builder.isLooped = true;

                                CANE_RUN_ANIMATION = builder.build();
                            }

                            runContainer.setAnim(new KeyframeAnimationPlayer(CANE_RUN_ANIMATION, 0, true));
                        } else if(isAnim2NotNull) {
                            runContainer.setAnim(null);
                        }
                    } catch (IllegalArgumentException e){
                        LOGGER.error("Unable to load the Cane Run Animation, meaning animation will not play!");

                        UNABLE_TO_LOAD_RUN_ANIMATION = true;
                    }
                }
            });
        });
    }


    public static Positioning customRelative(int xPercent, int yPercent){
        return PositioningMixin.personality$invokeNewPosition(xPercent, yPercent, RELATIVE_WITHOUT_CHILD);
    }
}
