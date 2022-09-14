package io.wispforest.personality.client.screens;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.badge.Badge;
import io.wispforest.owo.ui.component.TextureComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.Identifier;

import java.util.List;

public class OriginBadgeComponent extends TextureComponent {

    private final Badge badge;
    private final PowerType<?> type;

    protected OriginBadgeComponent(PowerType<?> type, Badge badge) {
        super(badge.spriteId(), 0, 0, 9, 9 ,9,9);

        this.badge = badge;
        this.type = type;
    }

    @Override
    public List<TooltipComponent> tooltip() {
        return badge.getTooltipComponents(type, this.parent().width(), AdditionalCreationComponent.time,  MinecraftClient.getInstance().textRenderer);
    }
}
