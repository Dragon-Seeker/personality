package io.blodhgarm.personality.server;

import io.blodhgarm.personality.api.character.CharacterManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class PrivilegeManager {

    private static final Map<String, PrivilegeLevel> actionMap = new HashMap<>();

    public static void init(){

        actionMap.put("list-all", PrivilegeLevel.MODERATOR);

        actionMap.put("get_player", PrivilegeLevel.MODERATOR);
        actionMap.put("get_uuid", PrivilegeLevel.MODERATOR);

        actionMap.put("reveal_players", PrivilegeLevel.MODERATOR);

        actionMap.put("screen_view_player", PrivilegeLevel.MODERATOR);
        actionMap.put("screen_view_uuid", PrivilegeLevel.MODERATOR);

        actionMap.put("screen_edit_player", PrivilegeLevel.MODERATOR);
        actionMap.put("screen_edit_uuid", PrivilegeLevel.MODERATOR);

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
    }

    public static PrivilegeLevel getLevel(String action){
        return actionMap.getOrDefault(action, PrivilegeLevel.NONE);
    }

    public static Predicate<ServerCommandSource> privilegeCheck(String action){
        return source -> source.getPlayer() != null
                && getLevel(action).test(source.getPlayer());
    }

    public enum PrivilegeLevel implements Predicate<ServerPlayerEntity> {
        ADMIN(CharacterManager::hasAdministrationPermissions),
        MODERATOR(CharacterManager::hasModerationPermissions),
        NONE(e -> true);

        public final Predicate<ServerPlayerEntity> authorizationCheck;

        PrivilegeLevel(Predicate<ServerPlayerEntity> check){
            this.authorizationCheck = check;
        }

        @Override
        public boolean test(ServerPlayerEntity serverPlayerEntity) {
            return this.authorizationCheck.test(serverPlayerEntity);
        }
    }
}
