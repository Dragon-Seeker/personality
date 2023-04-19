package io.blodhgarm.personality.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.character.BaseCharacter;
import io.blodhgarm.personality.api.character.Character;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.reveal.KnownCharacter;
import io.blodhgarm.personality.client.gui.CharacterViewMode;
import io.blodhgarm.personality.api.reveal.InfoRevealLevel;
import io.blodhgarm.personality.client.gui.GenderSelection;
import io.blodhgarm.personality.packets.OpenPersonalityScreenS2CPacket;
import io.blodhgarm.personality.server.PrivilegeManager;
import io.blodhgarm.personality.server.ServerCharacters;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.LongArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.*;

public class PersonalityCommands {

    private static final String
            CHARACTER_UUID_KEY = "character_uuid",
            PLAYER_UUID_KEY = "player_uuid",
            PLAYER_KEY = "player",
            PLAYERS_KEY = "players",
            TARGET_PLAYER_KEY = "target_player";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final SuggestionProvider<ServerCommandSource> REVEAL_LEVEL_SUGGESTION =
            (c, b) -> CommandSource.suggestMatching(Arrays.stream(InfoRevealLevel.values()).map(InfoRevealLevel::name), b);

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(buildCommand("personality"));
            dispatcher.register(buildCommand("p"));

            dispatcher.register(buildCharacterCommands("cm", dispatcher));
            dispatcher.register(buildCharacterCommands("characterManager", dispatcher));
        });
    }


    public static LiteralArgumentBuilder<ServerCommandSource> buildCharacterCommands(String base, CommandDispatcher<ServerCommandSource> dispatcher){
        return literal(base)

            .then(literal("list-all").requires(PrivilegeManager.privilegeCheck("list-all")) //MODERATION_CHECK
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                .executes(PersonalityCommands::listAllCharacters)
            )

            .then(literal("create").requires(PrivilegeManager.privilegeCheck("create"))
                .then(argument("name", word() )
                    .then(argument("gender", word() ).suggests((c, b) -> CommandSource.suggestMatching(Arrays.stream(GenderSelection.valuesWithoutOther()).map(GenderSelection::name), b))
                        .then(argument("description", string() )
                            .then(argument("biography", string() )
                                .then(argument("age", integer(17, 60) )
                                    .executes(PersonalityCommands::create) )))))
            )

            .then(literal("associate").requires(PrivilegeManager.privilegeCheck("associate"))
                .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                    .then(argument(PLAYER_KEY, player())
                        .executes(c -> associate(c, getPlayer(c, PLAYER_KEY)))))
            )

            .then(literal("disassociate").requires(PrivilegeManager.privilegeCheck("disassociate"))
                    .then(argument(PLAYER_KEY, player())
                            .executes(c -> disassociate(c, getPlayer(c, PLAYER_KEY).getUuidAsString(), true))
                    )
                    .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                            .executes(c -> disassociate(c, getUUID(c, CHARACTER_UUID_KEY), false))
                    )
            )

            .then(literal("get")
                .then(literal("self")
                    .executes(c -> get(c, getCharacter(c, 0))))
                .then(literal("player").requires(PrivilegeManager.privilegeCheck("get_player"))
                    .then(argument(PLAYER_KEY, player())
                        .executes(c -> get(c, getCharacter(c, 1)))))
                .then(literal("uuid").requires(PrivilegeManager.privilegeCheck("get_uuid"))
                    .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                        .executes(c -> get(c, getCharacter(c, 2)))))
            )

            .then(literal("set").requires(PrivilegeManager.privilegeCheck("set"))
                .then(setters(literal("self"), c -> PersonalityCommands.getCharacter(c, 0) ))
                .then(literal("player")
                    .then(setters(argument(PLAYER_KEY, player()), c -> PersonalityCommands.getCharacter(c, 1))))
                .then(literal("uuid")
                    .then(setters(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid()), c -> PersonalityCommands.getCharacter(c, 2))))
            )

            .then(literal("known")
                .then(literal("list")
                    .executes(PersonalityCommands::listKnownCharacters))
                .then(
                    literal("add").requires(PrivilegeManager.privilegeCheck("known_add"))
                        .then(
                            buildKnowledgeCommand(
                                argument("reveal_level", string()).suggests(REVEAL_LEVEL_SUGGESTION),
                                PersonalityCommands::addKnownCharacter
                            )
                        )
                )
                .then(
                    buildKnowledgeCommand(
                            literal("remove").requires(PrivilegeManager.privilegeCheck("known_remove")),
                            PersonalityCommands::removeKnownCharacter
                    )
                )
            )

            .then(literal("revive").requires(PrivilegeManager.privilegeCheck("revive"))
//                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
//                    .executes(c -> reviveCharacter(c, 0))
//                        .then(argument(PLAYERS_KEY, players())
//                                .executes(c -> reviveCharacter(c, 3)))
                    .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                            .executes(c -> reviveCharacter(c, 2)))
            )

            .then(literal("delete").requires(PrivilegeManager.privilegeCheck("delete"))
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                .executes(c -> deleteCharacter(c, 0))
                .then(argument(PLAYERS_KEY, players())
                        .executes(c -> deleteCharacter(c, 3)))
                .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                        .executes(c -> deleteCharacter(c, 2)))
            )

            .then(literal("kill").requires(PrivilegeManager.privilegeCheck("kill"))
//                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                .executes(c -> killCharacter(c, 0))
                .then(argument(PLAYERS_KEY, players())
                        .executes(c -> killCharacter(c, 3)))
                .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                        .executes(c -> killCharacter(c, 2)))
            );
    }

    public static ArgumentBuilder<ServerCommandSource, ?> buildKnowledgeCommand(ArgumentBuilder<ServerCommandSource, ?> base, BiFunction<CommandContext<ServerCommandSource>, Boolean, Integer> func){
        return base
                .then(argument(PLAYERS_KEY, players())
                        .then(argument(PLAYERS_KEY, players())
                                .executes(c -> func.apply(c, true)))
                        .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                                .executes(c -> func.apply(c, true)))
                        .executes(c -> func.apply(c, true))
                ).then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                        .then(argument(PLAYERS_KEY, players())
                                .executes(c -> func.apply(c, false)))
                        .then(argument(CHARACTER_UUID_KEY, UuidArgumentType.uuid())
                                .executes(c -> func.apply(c, false)))
                        .executes(c -> func.apply(c, false))
                );
    }

    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(String base) {
        return literal(base)
            .then(literal("reveal")
                .then(literal("small").executes(c -> revealRange(c, 3)))
                .then(literal("medium").executes(c -> revealRange(c, 7)))
                .then(literal("large").executes(c -> revealRange(c, 15)))
                .then(literal("range")
                    .then(argument("range", integer(0))
                        .executes(c -> revealRange(c, getInteger(c,"range")))))
                .then(literal("players").requires(PrivilegeManager.privilegeCheck("reveal_players"))
                    .then(argument(PLAYERS_KEY, players())
                        .executes(c -> revealRange(c, -1))))
            )
            .then(literal("screen")
                .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                .then(
                    buildScreenCommand(literal("creation").requires(PrivilegeManager.privilegeCheck("screen_creation")), CharacterViewMode.CREATION, "creation")
                )
                .then(
                    buildScreenCommand(literal("view"), CharacterViewMode.VIEWING, "view")
                )
                .then(
                    buildScreenCommand(literal("edit"), CharacterViewMode.EDITING, "edit")
                )
            );
    }

    private static LiteralArgumentBuilder<ServerCommandSource> buildScreenCommand(LiteralArgumentBuilder<ServerCommandSource> node, CharacterViewMode mode, String type){
        node
            .then(literal("self")
                    .executes(c -> PersonalityCommands.openCharacterScreen(c, 0, mode, false))
            );

        if(mode.importFromCharacter()){
            node
                .then(argument(PLAYER_KEY, player()).requires(PrivilegeManager.privilegeCheck("screen_" + type + "_player"))
                        .executes(c -> PersonalityCommands.openCharacterScreen(c, 1, mode, false))
                )
                .then(argument(CHARACTER_UUID_KEY, string()).requires(PrivilegeManager.privilegeCheck("screen_" + type + "_uuid"))
                        .executes(c -> PersonalityCommands.openCharacterScreen(c, 2, mode, false))
                );
        } else {
            node
                .then(argument(TARGET_PLAYER_KEY, player()).requires(PrivilegeManager.privilegeCheck("screen_" + type + "_targeted"))
                        .executes(c -> PersonalityCommands.openCharacterScreen(c, -1, mode, true))
                );
        }

        return node;
    }

    private static ArgumentBuilder<ServerCommandSource,?> setters(ArgumentBuilder<ServerCommandSource,?> builder, Function<CommandContext<ServerCommandSource>, Character> character) {
        return builder.then(literal("name").then(argument("name", word())
                    .executes(c -> setProperty(c, () -> { character.apply(c).setName(getString(c, "name")); return msg(c, "Name Set"); }))))
            .then(literal("gender").then(argument("gender", word()).suggests( (c,b) -> suggestions(b, "male", "female", "nonbinary"))
                    .executes(c -> setProperty(c, () -> { character.apply(c).setGender(getString(c, "gender")); return msg(c, "Gender Set"); }))))
            .then(literal("description").then(argument("description", greedyString())
                    .executes(c -> setProperty(c, () -> { character.apply(c).setDescription(getString(c, "description")); return msg(c, "Description Set"); }))))
            .then(literal("biography").then(argument("biography", greedyString())
                    .executes(c -> setProperty(c, () -> { character.apply(c).setBiography(getString(c, "biography")); return msg(c, "Biography Set"); }))))
            .then(literal("age").then(argument("age", integer(17))
                    .executes(c -> setProperty(c, () -> { character.apply(c).setAge(getInteger(c, "age")); return msg(c, "Age Set"); }))))
            .then(literal("playtime").then(argument("playtime",  longArg())
                    .executes(c -> setProperty(c, () -> { boolean success = character.apply(c).setPlaytime(getInteger(c, "playtime")); return msg(c, success ? "Playtime Set" : "Couldn't set Playtime, player not online"); }))));
    }

    private static int create(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if(player == null) return requiresPlayerOperatorMsg(context);

            Character c = new Character(
                    player.getUuidAsString(),
                    getString(context, "name"),
                    getString(context, "gender"),
                    getString(context, "description"),
                    getString(context, "biography"),
                    getInteger(context, "age"),
                    player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME))
            );

            ServerCharacters.INSTANCE.playerToCharacterReferences().put(player.getUuidAsString(), c.getUUID());
            ServerCharacters.INSTANCE.saveCharacter(c);
            ServerCharacters.INSTANCE.saveCharacterReference();

            return msg(context, "Character Created");
        } catch (Exception e) { e.printStackTrace(); }

        return 0;
    }

    private static int get(CommandContext<ServerCommandSource> context, Character c) {
        if (c == null) return msg(context, "§cYou don't have a Character");

        context.getSource().sendFeedback(Text.literal("\n§nCharacter: " + c.getInfo() + "\n"), false);

        return 1;
    }

    private static int setProperty(CommandContext<ServerCommandSource> context, Supplier<Integer> code) {
        try {
            int out = code.get();
            ServerCharacters.INSTANCE.saveCharacter(ServerCharacters.INSTANCE.getCharacter(context.getSource().getPlayer()));
            return out;
        } catch (Exception e) { e.printStackTrace(); }

        return errorMsg(context);
    }

    private static int associate(CommandContext<ServerCommandSource> context, PlayerEntity player) {
        String msg = ServerCharacters.INSTANCE.associateCharacterToPlayer(getUUID(context, CHARACTER_UUID_KEY), player.getUuidAsString())
                ? "Player associated to selected character!"
                : "§cUnable to locate the selected character!";

        return msg(context, msg);
    }

    private static int disassociate(CommandContext<ServerCommandSource> context, String UUID, boolean isPlayer) {
        String playerUUID = ServerCharacters.INSTANCE.dissociateUUID(UUID, isPlayer);

        String targetType = (isPlayer ? "Player" : "Character");

        String returnMsg = playerUUID == null
                ? "§cTargeted " + targetType + " was not found to be Associated to anything"
                : targetType + " disassociated!";

        return msg(context, returnMsg);
    }

    private static int revealRange(CommandContext<ServerCommandSource> context, int range) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        if(player == null) return requiresPlayerOperatorMsg(context);

        try {
            if(range != -1) {
                ServerCharacters.INSTANCE.revealCharacterInfo(player, range, InfoRevealLevel.GENERAL);
            } else {
                ServerCharacters.INSTANCE.revealCharacterInfo(player, getPlayers(context, PLAYERS_KEY), InfoRevealLevel.GENERAL);
            }

            return msg(context, "Identity Revealed");
        } catch (Exception e) { e.printStackTrace(); }

        return errorMsg(context);
    }

    private static int listKnownCharacters(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if(player == null) return requiresPlayerOperatorMsg(context);

            Character c = ServerCharacters.INSTANCE.getCharacter(player);

            if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

            MutableText text = Text.literal("\n§nKnown Characters§r:\n\n");

            for (Map.Entry<String, KnownCharacter> entry : c.getKnownCharacters().entrySet()) {
                String characterUUID = entry.getKey();

                BaseCharacter pc = entry.getValue();

                if(pc != null) {
                    text.append(Text.literal(pc.getName() + "\n").setStyle(Style.EMPTY.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§n" + pc.getInfo())))));
                } else {
                    LOGGER.error("A known Character of [{}] wasn't found by the character manager: [UUID: {}]", player, characterUUID);
                }
            }

            player.sendMessage(text);

            return 1;
        }
        catch (Exception e) { e.printStackTrace(); }

        return errorMsg(context);
    }

    private static int listAllCharacters(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();

            if(player == null) return requiresPlayerOperatorMsg(context);

            MutableText text = Text.literal("\n§nAll Characters§r:\n\n");

            if(ServerCharacters.INSTANCE.characterLookupMap().values().isEmpty()) {
                return msg(context, "§cThere are no Characters bound to this world.");
            }

            ServerCharacters.INSTANCE.characterLookupMap().values().forEach(character -> {
                text.append(Text.literal(character.getName() + "\n").setStyle(Style.EMPTY.withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§n" + character.getInfo())))));
            });

            player.sendMessage(text);

            return 1;
        }
        catch (Exception e) { e.printStackTrace(); }

        return errorMsg(context);
    }

    //TODO: CREATE COMMAND FOR MODIFYING OTHER CHARACTERS
    private static int addKnownCharacter(CommandContext<ServerCommandSource> context, boolean removeViaPlayers) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        InfoRevealLevel level = getRevealLevel(context);

        if(level == null) return errorMsg(context);

        Character c = ServerCharacters.INSTANCE.getCharacter(player);

        if(c == null) return errorNoCharacterMsg(context, player);

        if(removeViaPlayers){
            try {
                for (ServerPlayerEntity p : getPlayers(context, PLAYERS_KEY)) {
                    Character pCharacter = ServerCharacters.INSTANCE.getCharacter(p);

                    if(pCharacter != null) {
                        KnownCharacter wrappedPCharacter = c.getKnownCharacters().get(pCharacter.getUUID());

                        if(wrappedPCharacter == null) wrappedPCharacter = new KnownCharacter(c.getUUID(), pCharacter.getUUID());

                        wrappedPCharacter.updateInfoLevel(level);

                        c.getKnownCharacters().put(pCharacter.getUUID(), wrappedPCharacter);
                    } else {
                        LOGGER.error("Could not add a known Character to [{}] as it wasn't found by the character manager: [Player: {}]", player, p);
                    }
                }
            } catch (CommandSyntaxException e){
                e.printStackTrace();

                return errorMsg(context);
            }
        } else {
            String characterUUID = getUUID(context, CHARACTER_UUID_KEY);

            KnownCharacter wrappedCharacter = new KnownCharacter(c.getUUID(), characterUUID);

            wrappedCharacter.updateInfoLevel(level);

            c.getKnownCharacters().put(characterUUID, wrappedCharacter);
        }

        ServerCharacters.INSTANCE.saveCharacter(c);

        return msg(context, "Character(s) Added");
    }

    private static int removeKnownCharacter(CommandContext<ServerCommandSource> context, boolean removeViaPlayers) {
        ServerPlayerEntity player = context.getSource().getPlayer();

        Character c = ServerCharacters.INSTANCE.getCharacter(player);

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        if(removeViaPlayers) {
            try {
                for (ServerPlayerEntity p : getPlayers(context, PLAYERS_KEY)) {
                    Character pCharacter = ServerCharacters.INSTANCE.getCharacter(p);

                    if (pCharacter != null) {
                        c.getKnownCharacters().remove(pCharacter.getUUID());
                    } else {
                        LOGGER.error("Could not remove a known Character of [{}] as it wasn't found by the character manager: [Player: {}]", player, p);
                    }
                }
            } catch (CommandSyntaxException e){
                e.printStackTrace();

                return errorMsg(context);
            }
        } else {
            String characterUUID = getString(context, "character_uuid");

            c.getKnownCharacters().put(characterUUID, new KnownCharacter(c.getUUID(), characterUUID));
        }

        ServerCharacters.INSTANCE.saveCharacter(c);

        return msg(context, "Character(s) Removed");
    }

    //---

    public static int reviveCharacter(CommandContext<ServerCommandSource> context, int characterSelectionType){
        return performActionOnCharacter(context, characterSelectionType, "revive");
    }

    public static int killCharacter(CommandContext<ServerCommandSource> context, int characterSelectionType){
        return performActionOnCharacter(context, characterSelectionType, "kill");
    }

    private static int deleteCharacter(CommandContext<ServerCommandSource> context, int characterSelectionType) {
        return performActionOnCharacter(context, characterSelectionType, "delete");
    }

    public static int performActionOnCharacter(CommandContext<ServerCommandSource> context, int characterSelectionType, String action){
        List<Character> characters = new ArrayList<>();

        if(characterSelectionType != 4) {
            characters.add(getCharacter(context, characterSelectionType));
        } else {
            try {
                characters.addAll(getPlayers(context, PLAYERS_KEY).stream()
                        .map(ServerCharacters.INSTANCE::getCharacter)
                        .toList()
                );
            } catch (CommandSyntaxException e){ e.printStackTrace(); }
        }

        ServerCharacters.ReturnInformation info = ServerCharacters.INSTANCE.attemptActionOn(
                characters.stream()
                        .filter(Objects::nonNull)
                        .map(Character::getUUID)
                        .toList(),
                action,
                context.getSource().getPlayer()
        );

        return info.success() ? msg(context, info.returnMessage()) : errorMsg(context, info.returnMessage());
    }

    //----------------------

    private static int openCharacterScreen(CommandContext<ServerCommandSource> context, int characterSelectionType, CharacterViewMode mode, boolean gatherPlayer){
        PlayerEntity player;

        if(gatherPlayer) {
            try {
                player = getPlayer(context, "target_player");
            } catch (CommandSyntaxException e){
                return msg(context, "Could not find the target person to open the screen for!");
            }
        } else {
            player = context.getSource().getPlayer();
        }

        if (player == null) msg(context, "Command requires a target player to open the screen for!");

        Character character = getCharacter(context, characterSelectionType);

        if(character == null && mode.importFromCharacter()) return msg(context, "Could not locate the Character though the given selection method");

        Networking.CHANNEL.serverHandle(player).send(new OpenPersonalityScreenS2CPacket(mode, character == null ? "" : character.getUUID()));

        return msg(context, "Opening Screen");
    }

    //Helpers:

    private static String getUUID(CommandContext<ServerCommandSource> context, String name){
        return UuidArgumentType.getUuid(context, name).toString();
    }


    private static CompletableFuture<Suggestions> suggestions(SuggestionsBuilder builder, String... suggestions) {
        for (String suggestion : suggestions) builder.suggest(suggestion);

        return builder.buildFuture();
    }

    @Nullable
    private static Character getCharacter(CommandContext<ServerCommandSource> context, int characterSelectionType) {
        try {
            return switch (characterSelectionType) {
                case 0 -> ServerCharacters.INSTANCE.getCharacter(context.getSource().getPlayer());
                case 1 -> ServerCharacters.INSTANCE.getCharacter(getPlayer(context, PLAYER_KEY));
                case 2 -> ServerCharacters.INSTANCE.getCharacter(getUUID(context, CHARACTER_UUID_KEY));
                default -> null;
            };
        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

    public static InfoRevealLevel getRevealLevel(CommandContext<ServerCommandSource> context){
        try {
            return InfoRevealLevel.valueOf(getString(context, "reveal_level"));
        } catch (Exception e){ e.printStackTrace(); }

        return null;
    }

    public static CharacterViewMode getScreenMode(CommandContext<ServerCommandSource> context){
        try {
            return CharacterViewMode.valueOf(getString(context, "screen_mode"));
        } catch (Exception e){ e.printStackTrace(); }

        return null;
    }


    private static int msg(CommandContext<ServerCommandSource> context, String msg) {
        context.getSource().sendFeedback(Text.literal("§2WJR: §a" + msg), false);
        return 1;
    }

    private static int requiresPlayerOperatorMsg(CommandContext<ServerCommandSource> context){
        return msg(context, "§cCommand can only be called by a Player!.");
    }

    private static int errorMsg(CommandContext<ServerCommandSource> context){
        return errorMsg(context, "Something went Wrong.");
    }

    private static int errorMsg(CommandContext<ServerCommandSource> context, String message){
        return msg(context, "§c" + message);
    }

    private static int errorNoCharacterMsg(CommandContext<ServerCommandSource> context, ServerPlayerEntity player){
        return msg(context, "§cThe current Player could not be found within the CharacterManager: [Player: " + context.getSource().getPlayer().toString()  + "] ");
    }

    private static int errorNoCharactersMsg(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> players){
        return msg(context, "§cThe given Players could not be found within the CharacterManager: [Players: " + players.toString()  + "] ");
    }

}
