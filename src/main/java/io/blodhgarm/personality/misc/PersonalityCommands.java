package io.blodhgarm.personality.misc;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import io.blodhgarm.personality.api.Character;
import io.blodhgarm.personality.Networking;
import io.blodhgarm.personality.api.CharacterManager;
import io.blodhgarm.personality.client.screens.CharacterScreenMode;
import io.blodhgarm.personality.packets.OpenPersonalityScreenS2CPacket;
import io.blodhgarm.personality.impl.ServerCharacters;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.slf4j.Logger;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.mojang.brigadier.arguments.FloatArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.LongArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.command.argument.EntityArgumentType.*;
import static net.minecraft.server.command.CommandManager.*;

public class PersonalityCommands {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(buildCommand("personality"));
            dispatcher.register(buildCommand("p"));
        });
    }

    public static LiteralArgumentBuilder<ServerCommandSource> buildCommand(String base) {
        return literal(base)
            .then(literal("create")
                .then(argument("name", word() )
                    .then(argument("gender", word() ).suggests( (c,b) -> suggestions(b, "male", "female", "nonbinary"))
                        .then(argument("description", string() )
                            .then(argument("biography", string() )
                                .then(argument("heightOffset", floatArg(-0.5F, 0.5F) )
                                    .then(argument("age", integer(17, 60) )
                                        .executes(PersonalityCommands::create) ))))))
            )

            .then(literal("get")
                .executes(c -> get(c, getCharacterFromSelf(c)))
                .then(literal("self")
                    .executes(c -> get(c, getCharacterFromSelf(c))))
                .then(literal("player")
                    .then(argument("player", player())
                        .executes(c -> get(c, getCharacterFromPlayer(c)))))
                .then(literal("uuid")
                    .then(argument("uuid", greedyString())
                        .executes(c -> get(c, getCharacterFromUUID(c)))))
            )

            .then(literal("set")
                .then(setters(literal("self"), PersonalityCommands::getCharacterFromSelf ))
                .then(literal("player").then(setters(argument("player", player()), PersonalityCommands::getCharacterFromPlayer )))
                .then(literal("uuid").then(setters(argument("uuid", string()), PersonalityCommands::getCharacterFromUUID)))
            )

            .then(literal("associate")
                .then(argument("uuid", string())
                    .executes(c -> associate(c, null))
                    .then(argument("player", player())
                        .executes(c -> associate(c, getPlayer(c,"player")))))
            )

            .then(literal("kill")
                .executes(c -> deleteCharacter(c, true))
                    .then(argument("players", players())
                        .executes(c -> deleteCharacterByPlayer(c, true)))
                    .then(argument("uuid", string())
                        .executes(c -> deleteCharacterByUUID(c, true)))
            )

            .then(literal("delete")
                .executes(c -> deleteCharacter(c, false))
                .then(argument("players", players())
                    .executes(c -> deleteCharacterByPlayer(c, false)))
                .then(argument("uuid", string())
                    .executes(c -> deleteCharacterByUUID(c, false)))
            )

            .then(literal("reveal")
                .then(literal("near")
                    .executes(c -> revealRange(c, 7) ))
                .then(literal("far")
                    .executes(c -> revealRange(c, 15) ))
                .then(literal("range")
                    .then(argument("range", integer(0))
                        .executes(c -> revealRange(c, getInteger(c,"range") ))))
                .then(literal("players")
                    .then(argument("players", players())
                        .executes(PersonalityCommands::revealPerson)))
            )

            .then(literal("characters")
                .then(literal("list")
                    .executes(PersonalityCommands::listKnownCharacters))
                .then(literal("add")
                    .then(argument("players", players())
                        .executes(PersonalityCommands::addKnownCharacterByPlayer))
                    .then(argument("uuid", string())
                        .executes(PersonalityCommands::addKnownCharacterByUUID)))
                .then(literal("remove")
                    .then(argument("players", players())
                        .executes(PersonalityCommands::removeKnownCharacterByPlayer))
                    .then(argument("uuid", string())
                        .executes(PersonalityCommands::removeKnownCharacterByUUID)))
                .then(literal("list-all")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(1))
                        .executes(PersonalityCommands::listAllCharacters)
                )

            )

            .then(literal("screen")
                .then(literal("creation")
                    .executes(PersonalityCommands::openCreationScreen)
                )
                .then(literal("view")
                    .then(literal("self")
                        .executes(PersonalityCommands::openViewScreenSelf)
                    )
                    .then(argument("uuid", string())
                        .executes(PersonalityCommands::openViewScreen)
                    )
                    .then(argument("players", players())
                        .executes(PersonalityCommands::openViewScreen)
                    )
                )
            );
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
//                .then(literal("heightOffset").then(argument("heightOffset", integer(-25, 25))
//                        .executes(c -> setProperty(c, () -> { character.apply(c).setHeightOffset(getInteger(c, "heightOffset")); return msg(c, "Height Offset Set"); }))))
                .then(literal("age").then(argument("age", integer(17))
                        .executes(c -> setProperty(c, () -> { character.apply(c).setAge(getInteger(c, "age")); return msg(c, "Age Set"); }))))
                .then(literal("playtime").then(argument("playtime",  longArg())
                        .executes(c -> setProperty(c, () -> { boolean success = character.apply(c).setPlaytime(getInteger(c, "playtime")); return msg(c, success ? "Playtime Set" : "Couldn't set Playtime, player not online"); }))));
    }

    private static int create(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();

            String name = getString(context, "name");
            String gender = getString(context, "gender");
            String description = getString(context, "description");
            String biography = getString(context, "biography");
            //float heightOffset = getFloat(context, "heightOffset");
            int age = getInteger(context, "age");
            int activityOffset = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

            Character c = new Character(name, gender, description, biography, age, activityOffset);

            ServerCharacters.INSTANCE.playerToCharacterReferences().put(player.getUuidAsString(), c.getUUID());
            ServerCharacters.INSTANCE.saveCharacter(c);
            ServerCharacters.INSTANCE.saveCharacterReference();

            return msg(context, "Character Created");
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int get(CommandContext<ServerCommandSource> context, Character c) {
        if (c == null)
            return msg(context, "§cYou don't have a Character");
        try {
            context.getSource().sendFeedback(Text.literal("\n§nCharacter: " + c.getInfo() + "\n"), false);
            return 1;
        } catch(Exception e){
            e.printStackTrace();
            return errorMsg(context);
        }
    }

    private static int setProperty(CommandContext<ServerCommandSource> context, Supplier<Integer> code) {
        try {
            int out = code.get();
            ServerCharacters.INSTANCE.saveCharacter(ServerCharacters.INSTANCE.getCharacter(context.getSource().getPlayer()));
            return out;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    private static int associate(CommandContext<ServerCommandSource> context, PlayerEntity player) {
        ServerCharacters.INSTANCE.associateCharacterToPlayer(getString(context, "uuid"), player.getUuidAsString());

        return msg(context, "Character associated");
    }

    private static int revealRange(CommandContext<ServerCommandSource> context, int range) {
        try {
            ServerCharacters.INSTANCE.revealToPlayersInRange(context.getSource().getPlayer(), range);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return msg(context, "Identity Revealed");
    }

    private static int revealPerson(CommandContext<ServerCommandSource> context) {
        return 1;
    }

    private static int listKnownCharacters(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            MutableText text = Text.literal("\n§nKnown Characters§r:\n\n");

            Character c = ServerCharacters.INSTANCE.getCharacter(player);

            if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

            for (String uuid : c.knowCharacters) {
                Character pc = ServerCharacters.INSTANCE.getCharacter(uuid);

                if(pc != null) {
                    text.append(Text.literal(pc.getName() + "\n").setStyle(Style.EMPTY.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§n" + pc.getInfo())))));
                } else {
                    LOGGER.error("A known Character of [{}] wasn't found by the character manager: [UUID: {}]", player, uuid);
                }
            }

            player.sendMessage(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static int listAllCharacters(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            MutableText text = Text.literal("\n§nAll Characters§r:\n\n");

            if(ServerCharacters.INSTANCE.characterLookupMap().values().isEmpty()) {
                return msg(context, "§cThere are no Characters bound to this world.");
            }

            ServerCharacters.INSTANCE.characterLookupMap().values().forEach(character -> {
                text.append(Text.literal(character.getName() + "\n").setStyle(Style.EMPTY.withHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§n" + character.getInfo())))));
            });

            player.sendMessage(text);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static int addKnownCharacterByPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Character c = ServerCharacters.INSTANCE.getCharacter(player);

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        for (ServerPlayerEntity p : getPlayers(context, "players")) {
            Character pCharacter = ServerCharacters.INSTANCE.getCharacter(p);

            if(pCharacter != null) {
                c.knowCharacters.add(pCharacter.getUUID());
            } else {
                LOGGER.error("Could not add a known Character to [{}] as it wasn't found by the character manager: [Player: {}]", player, p);
            }
        }

        ServerCharacters.INSTANCE.saveCharacter(c);
        return msg(context, "Character(s) Added");
    }

    private static int addKnownCharacterByUUID(CommandContext<ServerCommandSource> context) {
        Character c = ServerCharacters.INSTANCE.getCharacter(context.getSource().getPlayer());

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        c.knowCharacters.add(getString(context, "uuid"));
        ServerCharacters.INSTANCE.saveCharacter(c);

        return msg(context, "Character Added");
    }

    private static int removeKnownCharacterByPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Character c = ServerCharacters.INSTANCE.getCharacter(player);

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        for (ServerPlayerEntity p : getPlayers(context, "players")) {
            Character pCharacter = ServerCharacters.INSTANCE.getCharacter(p);

            if(pCharacter != null) {
                c.knowCharacters.remove(pCharacter.getUUID());
            } else {
                LOGGER.error("Could not remove a known Character of [{}] as it wasn't found by the character manager: [Player: {}]", player, p);
            }
        }

        ServerCharacters.INSTANCE.saveCharacter(c);
        return msg(context, "Character(s) Removed");
    }

    private static int removeKnownCharacterByUUID(CommandContext<ServerCommandSource> context) {
        Character c = ServerCharacters.INSTANCE.getCharacter(context.getSource().getPlayer());

        if(c == null)
            return errorNoCharacterMsg(context, context.getSource().getPlayer());

        c.knowCharacters.remove( getString(context, "uuid") );
        ServerCharacters.INSTANCE.saveCharacter(c);

        return msg(context, "Character Removed");
    }

    private static int deleteCharacter(CommandContext<ServerCommandSource> context, boolean onlyKill) {
        Character c = ServerCharacters.INSTANCE.getCharacter(context.getSource().getPlayer());
        if(c == null)
            return errorNoCharacterMsg(context, context.getSource().getPlayer());

        if (onlyKill)
            ServerCharacters.INSTANCE.killCharacter(c);
        else
            ServerCharacters.INSTANCE.deleteCharacter(c);

        return msg(context, "Character(s) Deleted");
    }

    private static int deleteCharacterByPlayer(CommandContext<ServerCommandSource> context, boolean onlyKill) throws CommandSyntaxException {
        for (ServerPlayerEntity p : getPlayers(context, "players")) {
           Character pCharacter = ServerCharacters.INSTANCE.getCharacter(p);

            if(pCharacter != null) {
                if (onlyKill)
                    ServerCharacters.INSTANCE.killCharacter(pCharacter);
                else
                    ServerCharacters.INSTANCE.deleteCharacter(pCharacter);
            } else {
                LOGGER.error("There was a attempt to delete a players character but the CharacterManager could not find anything: [Player: {}]", p);
            }
        }

        return msg(context, "Character(s) Deleted");
    }

    private static int deleteCharacterByUUID(CommandContext<ServerCommandSource> context, boolean onlyKill) {
        if (onlyKill)
            ServerCharacters.INSTANCE.killCharacter(getString(context, "uuid"));
        else
            ServerCharacters.INSTANCE.deleteCharacter(getString(context, "uuid"));
        return msg(context, "Character Deleted");
    }

    private static int openCreationScreen(CommandContext<ServerCommandSource> context) {
        PlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            Networking.CHANNEL.serverHandle(player).send(new OpenPersonalityScreenS2CPacket(CharacterScreenMode.CREATION, "personality$packet_target"));

            return msg(context, "Opening Screen");
        }

        return msg(context, "");
    }

    private static int openViewScreenSelf(CommandContext<ServerCommandSource> context){
        PlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            Networking.CHANNEL.serverHandle(player).send(new OpenPersonalityScreenS2CPacket(CharacterScreenMode.VIEWING, "personality$packet_target"));

            return msg(context, "Opening Screen");
        }

        return msg(context, "");
    }

    private static int openViewScreen(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        PlayerEntity player = context.getSource().getPlayer();

        if (player == null) msg(context, "");

        Character character = null;

        try {
            String uuid = getString(context, "uuid");

            character = CharacterManager.getManger(player).getCharacter(uuid);
        } catch (IllegalArgumentException ignore) {}

        if(character != null) {
            try {
                ServerPlayerEntity targetPlayer = getPlayer(context, "player");

                character = CharacterManager.getManger(player).getCharacter(targetPlayer);
            } catch (IllegalArgumentException ignore) {}
        }

        if(character != null) {
            Networking.CHANNEL.serverHandle(player).send(new OpenPersonalityScreenS2CPacket(CharacterScreenMode.VIEWING, character.getUUID()));

            return msg(context, "Opening Screen");
        }

        return msg(context, "");
    }

    //Helpers:

    private static CompletableFuture<Suggestions> suggestions(SuggestionsBuilder builder, String... suggestions) {
        for (String suggestion : suggestions)
            builder.suggest(suggestion);
        return builder.buildFuture();
    }

    private static Character getCharacterFromSelf(CommandContext<ServerCommandSource> context) {
        return ServerCharacters.INSTANCE.getCharacter(context.getSource().getPlayer());
    }

    private static Character getCharacterFromUUID(CommandContext<ServerCommandSource> context) {
        try {
            return ServerCharacters.INSTANCE.getCharacter(getString(context, "uuid"));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Character getCharacterFromPlayer(CommandContext<ServerCommandSource> context) {
        try {
            return ServerCharacters.INSTANCE.getCharacter(getPlayer(context, "player"));
        } catch (CommandSyntaxException | NoSuchElementException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int msg(CommandContext<ServerCommandSource> context, String msg) {
        context.getSource().sendFeedback(Text.literal("§2WJR: §a" + msg), false);
        return 1;
    }

    private static int errorMsg(CommandContext<ServerCommandSource> context){
        return msg(context, "§cSomething went Wrong.");
    }

    private static int errorNoCharacterMsg(CommandContext<ServerCommandSource> context, ServerPlayerEntity player){
        return msg(context, "§cThe current Player could not be found within the CharacterManager: [Player: " + context.getSource().getPlayer().toString()  + "] ");
    }


}
