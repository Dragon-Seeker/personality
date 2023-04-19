package io.blodhgarm.personality.mixin;

import io.blodhgarm.personality.utils.Constants;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//Used as
@Mixin(Formatting.class)
public class FormattingMixin {

    @Invoker("<init>")
    public static Formatting personality$invokeNew(String internalName, int ordinal, String name, char code, int colorIndex, @Nullable Integer colorValue) {
        throw new IllegalStateException("How did this mixin stub get called conc");
    }

    @Final
    @Shadow
    @Mutable
    private static Formatting[] field_1072;

    @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lnet/minecraft/util/Formatting;field_1072:[Lnet/minecraft/util/Formatting;", shift = At.Shift.AFTER, opcode = Opcodes.PUTSTATIC))
    private static void addCustomFormmatting(CallbackInfo ci) {
        var formattings = new Formatting[field_1072.length + 1];
        System.arraycopy(field_1072, 0, formattings, 0, field_1072.length);

        formattings[formattings.length - 1] = FormattingMixin.personality$invokeNew("CHARACTER_FORMATTING", Formatting.values().length, "CHARACTER_FORMATTING", 'Z',16, new Color(177 / 255f, 201 / 255f, 197 / 255f).rgb());
        Constants.CHARACTER_FORMATTING = formattings[formattings.length - 1];

        field_1072 = formattings;
    }
}
