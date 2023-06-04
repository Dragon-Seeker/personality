package io.blodhgarm.personality.misc.pond;

import com.mojang.authlib.GameProfile;
import net.minecraft.stat.ServerStatHandler;

public interface OfflineStatExtension {

    ServerStatHandler loadStatHandler(GameProfile profile);
}
