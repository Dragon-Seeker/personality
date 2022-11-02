package io.blodhgarm.personality.compat.pehkui.client;

import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.api.addon.client.PersonalityScreenAddon;
import io.blodhgarm.personality.api.client.AddonObservable;
import io.blodhgarm.personality.compat.pehkui.ScaleAddon;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PehkuiScaleDisplayAddon extends PersonalityScreenAddon {

    public PehkuiScaleDisplayAddon() {
        super(new Identifier("pehkui", "scale_selection_addon"));
    }

    @Override
    public boolean hasSideScreenComponent() {return false;}

    @Override
    public FlowLayout build(boolean darkMode) {return Containers.verticalFlow(Sizing.content(), Sizing.content()); }

    @Override
    public Component buildBranchComponent(AddonObservable addonObservable, BaseParentComponent rootBranchComponent) {
        return Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(
                        Components.label(Text.of("Height: "))
                )
                .child(
                        Components.discreteSlider(Sizing.fixed(108), -0.5f, 0.5f) //128
                                .decimalPlaces(2)
                                .snap(true)
                                .setFromDiscreteValue(0.0)
                                .message(s -> {
                                    if(!s.startsWith("-") && !s.equals("0.00")){
                                        s = "+" + s;
                                    }
                                    return Text.literal(s);
                                })
                                .id("height_slider")
                )
                .verticalAlignment(VerticalAlignment.CENTER)
                .margins(Insets.bottom(8));
    }

    @Override
    public void branchUpdate() {}

    @Override
    public Map<Identifier, BaseAddon> getAddonData() {
        Map<Identifier, BaseAddon> addonData = new HashMap<>();

        float heightOffset = (float) getRootComponent().childById(DiscreteSliderComponent.class, "height_slider").discreteValue();

        addonData.put(new Identifier("pehkui", "height_modifier"), new ScaleAddon(heightOffset));

        return addonData;
    }

    @Override
    public boolean isDataEmpty(BaseParentComponent rootComponent) {
        return false;
    }
}
