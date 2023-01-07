package io.blodhgarm.personality.mixin.client.owo;

import io.blodhgarm.personality.client.PersonalityClient;
import io.wispforest.owo.ui.core.Positioning;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Positioning.class)
public interface PositioningMixin {

    @Invoker("<init>")
    public static Positioning personality$invokeNewPosition(int x, int y, Positioning.Type type){
        throw new IllegalStateException("How did this mixin stub get called conc");
    }

    @Mixin(Positioning.Type.class)
    public static abstract class TypeMixin {

        @Invoker("<init>")
        public static Positioning.Type personality$invokeNew(String internalName, int ordinal) {
            throw new IllegalStateException("How did this mixin stub get called conc");
        }

        @Final @Shadow @Mutable private static Positioning.Type[] $VALUES;

        @Inject(method = "<clinit>", at = @At(value = "FIELD", target = "Lio/wispforest/owo/ui/core/Positioning$Type;$VALUES:[Lio/wispforest/owo/ui/core/Positioning$Type;", shift = At.Shift.AFTER, opcode = Opcodes.PUTSTATIC))
        private static void addNullDyeColorValue(CallbackInfo ci) {
            var positionTypes = new Positioning.Type[$VALUES.length + 1];
            System.arraycopy($VALUES, 0, positionTypes, 0, $VALUES.length);

            positionTypes[positionTypes.length - 1] = PositioningMixin.TypeMixin.personality$invokeNew("relative_without_child", Positioning.Type.values().length);
            PersonalityClient.RELATIVE_WITHOUT_CHILD = positionTypes[positionTypes.length - 1];

            $VALUES = positionTypes;
        }
    }
}
