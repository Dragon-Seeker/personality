package io.blodhgarm.personality.client.gui.components.grid;

import io.blodhgarm.personality.client.gui.components.CustomButtonComponent;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MultiToggleButton extends CustomButtonComponent {

    private final List<ToggleVariantInfo> variantMap = new ArrayList<>();

    public int currentVariantIndex = 0;

    public List<MultiToggleButton> linkedButtons = new ArrayList<>();

    protected MultiToggleButton() {
        super(Text.empty(), (c) -> ((MultiToggleButton) c).toggledNextVariant());
    }

    public static MultiToggleButton of(List<ToggleVariantInfo> variantMap){
        return new MultiToggleButton().configure(button -> {
            button.variantMap.addAll(variantMap);

            button.toggleGivenVariant(button.currentVariantIndex, false);
        });
    }

    public MultiToggleButton linkButton(MultiToggleButton button){
        this.linkedButtons.add(button);

        return this;
    }

    public MultiToggleButton linkButton(List<MultiToggleButton> buttons){
        this.linkedButtons.addAll(buttons);

        return this;
    }

    public void toggledNextVariant(){
        this.toggleGivenVariant(currentVariantIndex + 1, true);
    }

    public void toggleGivenVariant(int index, boolean shouldApplyToggleFunc){
        this.currentVariantIndex = (index >= variantMap.size()) ? 0 : index;

        ToggleVariantInfo info = this.variantMap.get(currentVariantIndex);

        this.setMessage(Text.of(info.message()));
        this.tooltip(Text.of(info.tooltip()));

        this.linkedButtons.forEach(button -> button.toggleGivenVariant(0, false));

        if(shouldApplyToggleFunc) info.onToggle().accept(this);

        System.out.println("Current Button Index: " + currentVariantIndex);
    }

    public record ToggleVariantInfo(String message, String tooltip, Consumer<ButtonComponent> onToggle){}

}
