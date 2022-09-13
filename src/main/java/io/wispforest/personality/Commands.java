package io.wispforest.personality;

import com.mojang.brigadier.context.CommandContext;
import io.wispforest.personality.storage.Character;
import io.wispforest.personality.storage.CharacterManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import java.util.Locale;

import static com.mojang.brigadier.arguments.FloatArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.LongArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.server.command.CommandManager.*;

public class Commands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("personality")
                .then(literal("create")
                    .then(argument("name", word() )
                        .then(argument("gender", string() )
                            .then(argument("description", string() )
                                .then(argument("heightOffset", floatArg(-0.5F, 0.5F) )
                                    .then(argument("age", integer(17, 60) )
                                        .executes(Commands::create) ))))))
                .then(literal("get")
                    .then(argument("timeOffset", longArg())
                        .executes(Commands::get)))
            ));
    }

    private static int create(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();

            String name = getString(context, "name");
            String description = getString(context, "description");
            float heightOffset = getFloat(context, "heightOffset");
            int age = getInteger(context, "age");
            int activityOffset = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));

            String g = getString(context, "gender");
            Character.Gender gender = g.equalsIgnoreCase("male") ? Character.Gender.MALE
                    : g.equalsIgnoreCase("female") ? Character.Gender.FEMALE
                    : Character.Gender.NONBINARY;

            Character c = new Character(name, gender, description, heightOffset, age, activityOffset);

            CharacterManager.playerIDToCharacterID.put(player.getUuidAsString(), c.uuid);
            CharacterManager.saveCharacter(c);
            CharacterManager.saveCharacterReference();

            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static int get(CommandContext<ServerCommandSource> context) {
        try {
            ServerPlayerEntity player = context.getSource().getPlayer();
            Character c = CharacterManager.readCharacter(player);
            long timeOffset = getLong(context, "timeOffset");

            player.sendMessage(Text.literal(
                    "\n§nCharacter: " + c.name + "§r\n"
                            + "\n§lUUID§r: " + c.uuid
                            + "\n§lGender§r: " + c.gender.toString()
                            + "\n§lDescription§r: " + c.description
                            + "\n§lAge§r: " + c.getAge(timeOffset)
                            + "\n§lStage§r: " + c.getStage(timeOffset)
                            + "\n§lPlaytime§r: " + c.getPlaytime()
                            + "\n§lHeightOffset§r: " + c.heightOffset + "\n"
            ));
            return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

}
