package io.wispforest.personality.server;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.logging.LogUtils;
import io.wispforest.personality.Character;
import io.wispforest.personality.Networking;
import io.wispforest.personality.packets.OpenCharacterCreationScreenPacket;
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

public class Commands {

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
                            .then(argument("heightOffset", floatArg(-0.5F, 0.5F) )
                                .then(argument("age", integer(17, 60) )
                                    .executes(Commands::create) ))))))

            .then(literal("get")
                .executes(c -> get(c, getCharacterFromSelf(c)))
                .then(literal("self")
                    .executes(c -> get(c, getCharacterFromSelf(c))))
                .then(literal("player")
                    .then(argument("player", player())
                        .executes(c -> get(c, getCharacterFromPlayer(c)))))
                .then(literal("uuid")
                    .then(argument("uuid", greedyString())
                        .executes(c -> get(c, getCharacterFromUUID(c))))))

            .then(literal("set")
                .then(setters(literal("self"), Commands::getCharacterFromSelf ))
                .then(literal("player").then(setters(argument("player", player()), Commands::getCharacterFromPlayer )))
                .then(literal("uuid").then(setters(argument("uuid", string()), Commands::getCharacterFromUUID))))

            .then(literal("delete")
                .executes(Commands::deleteCharacter)
                .then(argument("players", players())
                    .executes(Commands::deleteCharacterByPlayer))
                .then(argument("uuid", greedyString())
                    .executes(Commands::deleteCharacterByUUID)))

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
                        .executes(Commands::revealPerson))))

            .then(literal("characters")
                .then(literal("list")
                    .executes(Commands::listKnownCharacters))
                .then(literal("add")
                    .then(argument("players", players())
                        .executes(Commands::addKnownCharacterByPlayer))
                    .then(argument("uuid", string())
                        .executes(Commands::addKnownCharacterByUUID)))
                .then(literal("remove")
                    .then(argument("players", players())
                        .executes(Commands::removeKnownCharacterByPlayer))
                    .then(argument("uuid", string())
                        .executes(Commands::removeKnownCharacterByUUID))))

            .then(literal("screen")
                .then(literal("creation")
                    .executes(Commands::openCreationScreen)))
            ;
    }

    private static ArgumentBuilder<ServerCommandSource,?> setters(ArgumentBuilder<ServerCommandSource,?> builder, Function<CommandContext<ServerCommandSource>, Character> character) {
        return builder.then(literal("name").then(argument("name", word())
                        .executes(c -> setProperty(c, () -> { character.apply(c).setName(getString(c, "name")); return msg(c, "Name Set"); }))))
                .then(literal("gender").then(argument("gender", word()).suggests( (c,b) -> suggestions(b, "male", "female", "nonbinary"))
                        .executes(c -> setProperty(c, () -> { character.apply(c).setGender(getString(c, "gender")); return msg(c, "Gender Set"); }))))
                .then(literal("description").then(argument("description", greedyString())
                        .executes(c -> setProperty(c, () -> { character.apply(c).setDescription(getString(c, "description")); return msg(c, "Description Set"); }))))
                .then(literal("heightOffset").then(argument("heightOffset",  floatArg(-0.5F, 0.5F))
                        .executes(c -> setProperty(c, () -> { character.apply(c).setHeightOffset(getFloat(c, "heightOffset")); return msg(c, "Height Offset Set"); }))))
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
            float heightOffset = getFloat(context, "heightOffset");
            int age = getInteger(context, "age");
            int activityOffset = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

            Character c = new Character(name, gender, description, heightOffset, age, activityOffset);

            ServerCharacters.playerIDToCharacterID.put(player.getUuidAsString(), c.getUUID());
            ServerCharacters.saveCharacter(c);
            ServerCharacters.saveCharacterReference();

            return msg(context, "Character Created");
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int get(CommandContext<ServerCommandSource> context, Character c) {
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
            ServerCharacters.saveCharacter(ServerCharacters.getCharacter(context.getSource().getPlayer()));
            return out;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    //TODO: Implement Reveal
    private static int revealRange(CommandContext<ServerCommandSource> context, int range) {
        return 1;
    }

    private static int revealPerson(CommandContext<ServerCommandSource> context) {
        return 1;
    }

    private static int listKnownCharacters(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            MutableText text = Text.literal("\n§nKnown Characters§r:\n\n");

            Character c = ServerCharacters.getCharacter(player);

            if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

            for (String uuid : c.knowCharacters) {
                Character pCharacter = ServerCharacters.getCharacter(uuid);

                if(pCharacter != null) {
                    text.append(Text.literal(c.getName() + "\n").setStyle(Style.EMPTY.withHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("§n" + c.getInfo())))));
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

    private static int addKnownCharacterByPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Character c = ServerCharacters.getCharacter(player);

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        for (ServerPlayerEntity p : getPlayers(context, "players")) {
            Character pCharacter = ServerCharacters.getCharacter(p);

            if(pCharacter != null) {
                c.knowCharacters.add(pCharacter.getUUID());
            } else {
                LOGGER.error("Could not add a known Character to [{}] as it wasn't found by the character manager: [Player: {}]", player, p);
            }
        }

        ServerCharacters.saveCharacter(c);
        return msg(context, "Character(s) Added");
    }

    private static int addKnownCharacterByUUID(CommandContext<ServerCommandSource> context) {
        Character c = ServerCharacters.getCharacter(context.getSource().getPlayer());

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        c.knowCharacters.add(getString(context, "uuid"));
        ServerCharacters.saveCharacter(c);

        return msg(context, "Character Added");
    }

    private static int removeKnownCharacterByPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Character c = ServerCharacters.getCharacter(player);

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        for (ServerPlayerEntity p : getPlayers(context, "players")) {
            Character pCharacter = ServerCharacters.getCharacter(p);

            if(pCharacter != null) {
                c.knowCharacters.remove(pCharacter.getUUID());
            } else {
                LOGGER.error("Could not remove a known Character of [{}] as it wasn't found by the character manager: [Player: {}]", player, p);
            }
        }

        ServerCharacters.saveCharacter(c);
        return msg(context, "Character(s) Removed");
    }

    private static int removeKnownCharacterByUUID(CommandContext<ServerCommandSource> context) {
        Character c = ServerCharacters.getCharacter(context.getSource().getPlayer());

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        c.knowCharacters.remove( getString(context, "uuid") );
        ServerCharacters.saveCharacter(c);

        return msg(context, "Character Removed");
    }

    private static int deleteCharacter(CommandContext<ServerCommandSource> context) {
        Character c = ServerCharacters.getCharacter(context.getSource().getPlayer());

        if(c == null) return errorNoCharacterMsg(context, context.getSource().getPlayer());

        ServerCharacters.deleteCharacter(c);

        return msg(context, "Character(s) Deleted");
    }

    private static int deleteCharacterByPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        for (ServerPlayerEntity p : getPlayers(context, "players")) {
           Character pCharacter = ServerCharacters.getCharacter(p);

            if(pCharacter != null) {
                ServerCharacters.deleteCharacter(pCharacter);
            } else {
                LOGGER.error("There was a attempt to delete a players character but the CharacterManager could not find anything: [Player: {}]", p);
            }
        }

        return msg(context, "Character(s) Deleted");
    }

    private static int deleteCharacterByUUID(CommandContext<ServerCommandSource> context) {
        ServerCharacters.deleteCharacter(getString(context, "uuid"));
        return msg(context, "Character Deleted");
    }

    private static int openCreationScreen(CommandContext<ServerCommandSource> context) {
        PlayerEntity player = context.getSource().getPlayer();
        if (player != null) {
            Networking.CHANNEL.serverHandle(player).send(new OpenCharacterCreationScreenPacket());

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
        return ServerCharacters.getCharacter(context.getSource().getPlayer());
    }

    private static Character getCharacterFromUUID(CommandContext<ServerCommandSource> context) {
        return ServerCharacters.getCharacter(getString(context, "uuid"));
    }

    private static Character getCharacterFromPlayer(CommandContext<ServerCommandSource> context) {
        try {
            return ServerCharacters.getCharacter(getPlayer(context, "player"));
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
