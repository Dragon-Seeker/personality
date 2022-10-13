package io.blodhgarm.personality.api.client;

import io.blodhgarm.personality.api.addons.BaseAddon;
import io.blodhgarm.personality.client.screens.AddonObservable;
import io.blodhgarm.personality.client.screens.PersonalityCreationScreen;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;

import java.util.Map;

public abstract class PersonalityScreenAddon {

    private final String addonId;

    private PersonalityCreationScreen originScreen = null;

    protected BaseParentComponent rootBranchComponent;

    public PersonalityScreenAddon(String addonId){
        this.addonId = addonId;
    }

    public String addonId(){
        return addonId;
    }

    public final FlowLayout createMainFlowlayout(boolean darkMode){
        return (FlowLayout) Containers.verticalFlow(Sizing.content(), Sizing.content())
                .child(this.build(darkMode))
                .id(this.addonId());
    }

    public final PersonalityScreenAddon linkAddon(PersonalityCreationScreen screen){
        this.originScreen = screen;

        return this;
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
    public Component addBranchComponent(AddonObservable addonObservable, BaseParentComponent rootComponent){
        this.rootBranchComponent = rootComponent;

        return Containers.verticalFlow(Sizing.content(), Sizing.content());
    }

    /**
     * Used to update the given branch component
     */
    public abstract void branchUpdate();

    //TODO: IMPLEMENT THIS
    public abstract Map<String, BaseAddon<?>> saveAddonData(BaseParentComponent rootComponent);
}
