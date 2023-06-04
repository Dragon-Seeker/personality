package io.blodhgarm.personality.api.character;

import io.blodhgarm.personality.api.utils.PlayerAccess;
import io.blodhgarm.personality.server.ServerCharacters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;

public class ServerCharacter extends Character {

    protected transient int startingPlaytime = 0;

    public ServerCharacter(String playerUUID, String name, String gender, String description, String biography, int ageOffset) {
        super(playerUUID, name, gender, description, biography, ageOffset);
    }

    @Override
    public void beforeEvent(String event) {
        super.beforeEvent(event);

        if(event.equals("disassociate")) {
            PlayerAccess<ServerPlayerEntity> playerAccess = ServerCharacters.INSTANCE.getPlayer(this.playerUUID);

            if(!playerAccess.playerValid()) return;

            updateCurrentPlaytime();

            ServerCharacters.INSTANCE.pushToSaveQueue(this, true);
        }
    }

    public int getStartingPlaytime(){
        return this.startingPlaytime;
    }

    public void setStartingPlaytime(int startingPlaytime){
        this.startingPlaytime = startingPlaytime;
    }

    public void updateCurrentPlaytime() {
        PlayerAccess<ServerPlayerEntity> playerAccess = ServerCharacters.INSTANCE.getPlayerFromCharacter(getUUID());

        if(playerAccess.isEmpty()) return;

        StatHandler handler = playerAccess.playerValid()
                ? ServerCharacters.INSTANCE.getStats(playerAccess.player())
                : ServerCharacters.INSTANCE.getStats(playerAccess.getProfile());

        currentPlaytime =  (handler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) - getStartingPlaytime());
    }
}
