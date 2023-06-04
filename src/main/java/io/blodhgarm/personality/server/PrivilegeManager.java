package io.blodhgarm.personality.server;

import io.blodhgarm.personality.PersonalityMod;
import io.blodhgarm.personality.api.character.CharacterManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A Simple Utility class for handling privilege for certain operations within Personality
 */
public class PrivilegeManager {

    private static final Map<String, PrivilegeLevel> actionMap = new HashMap<>();

    public static void init(){

        actionMap.put("list-all", PrivilegeLevel.MODERATOR);

        actionMap.put("get_player", PrivilegeLevel.MODERATOR);
        actionMap.put("get_uuid", PrivilegeLevel.MODERATOR);

        actionMap.put("reveal_players", PrivilegeLevel.MODERATOR);

        actionMap.put("screen_view_player", PrivilegeLevel.MODERATOR);
        actionMap.put("screen_view_uuid", PrivilegeLevel.MODERATOR);

        //-------------------------------------------------------------

        actionMap.put("set", PrivilegeLevel.ADMIN);

        actionMap.put("known_add", PrivilegeLevel.ADMIN);
        actionMap.put("known_remove", PrivilegeLevel.ADMIN);

        actionMap.put("associate", PrivilegeLevel.ADMIN);
        actionMap.put("disassociate", PrivilegeLevel.ADMIN);

        actionMap.put("create", PrivilegeLevel.ADMIN);

        actionMap.put("revive", PrivilegeLevel.ADMIN);
        actionMap.put("kill", PrivilegeLevel.ADMIN);
        actionMap.put("delete", PrivilegeLevel.ADMIN);

        actionMap.put("screen_creation", PrivilegeLevel.ADMIN);
        actionMap.put("screen_creation_targeted", PrivilegeLevel.ADMIN);

        actionMap.put("screen_edit_player", PrivilegeLevel.ADMIN);
        actionMap.put("screen_edit_uuid", PrivilegeLevel.ADMIN);

        actionMap.put("screen_kill_player", PrivilegeLevel.ADMIN);
    }

    public static PrivilegeLevel getLevel(String action){
        return actionMap.getOrDefault(action, PrivilegeLevel.NONE);
    }

    public static Predicate<ServerCommandSource> privilegeCheck(String action){
        return source -> source.getPlayer() != null
                && getLevel(action).test(source.getPlayer());
    }

    public enum PrivilegeLevel implements Predicate<PlayerEntity> {
        ADMIN(p -> p.hasPermissionLevel(3) || PersonalityMod.CONFIG.administrationList().contains(p.getGameProfile().toString())),
        MODERATOR(p -> ADMIN.test(p) || PersonalityMod.CONFIG.moderationList().contains(p.getGameProfile().toString())),
        NONE(e -> true);

        public final Predicate<PlayerEntity> authorizationCheck;

        PrivilegeLevel(Predicate<PlayerEntity> check){
            this.authorizationCheck = check;
        }

        @Override
        public boolean test(PlayerEntity playerEntity) {
            return this.authorizationCheck.test(playerEntity);
        }
    }
}
