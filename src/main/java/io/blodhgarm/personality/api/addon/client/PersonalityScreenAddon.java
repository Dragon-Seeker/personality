package io.blodhgarm.personality.api.addon.client;

import io.blodhgarm.personality.api.addon.BaseAddon;
import io.blodhgarm.personality.client.screens.AddonObservable;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class PersonalityScreenAddon {

    private final Identifier addonId;

    private PersonalityCreationScreen originScreen = null;

    private BaseParentComponent rootBranchComponent = null;

    public PersonalityScreenAddon(Identifier addonId){
        this.addonId = addonId;
    }

    public Identifier addonId(){
        return addonId;
    }

    public final FlowLayout createMainFlowlayout(boolean darkMode){
        return (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(this.build(darkMode))
                .id(this.addonId().toString());
    }

    public final PersonalityScreenAddon linkAddon(PersonalityCreationScreen screen){
        this.originScreen = screen;

        return this;
    }

    public final Component addBranchComponent(AddonObservable addonObservable, BaseParentComponent rootComponent){
        this.rootBranchComponent = rootComponent;

        return buildBranchComponent(addonObservable, rootComponent);
    }

    public BaseParentComponent getRootComponent(){
        return rootBranchComponent;
    }

    public final void closeAddon(){
        this.originScreen.pushScreenAddon(this);
    }

    //-------------------------------------------------------------------------------

    public boolean requiresUserInput(){
        return false;
    }

    /**
     * Method used to build the main portion of the addon Screen
     */
    public abstract FlowLayout build(boolean darkMode);

    /**
     * Method used to add the component that will toggle the addons side screen
     */
    public abstract Component buildBranchComponent(AddonObservable addonObservable, BaseParentComponent rootBranchComponent);

    /**
     * Used to update the given branch component
     */
    public abstract void branchUpdate();

    //TODO: IMPLEMENT THIS
    public abstract Map<Identifier, BaseAddon> getAddonData();

    public abstract boolean isDataEmpty(BaseParentComponent rootComponent);
}
