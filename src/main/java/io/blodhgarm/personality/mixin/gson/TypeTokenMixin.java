package io.blodhgarm.personality.mixin.gson;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import io.blodhgarm.personality.utils.gson.WrappedTypeToken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Mixin(TypeToken.class)
public class TypeTokenMixin<T> {

    @Inject(method = "getTypeTokenTypeArgument", at = @At(value = "INVOKE", target = "Ljava/lang/reflect/ParameterizedType;getRawType()Ljava/lang/reflect/Type;"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void personality$allowWrappedToken(CallbackInfoReturnable<Type> cir, Type superclass, ParameterizedType parameterized){
        if(parameterized.getRawType() == WrappedTypeToken.class){
            cir.setReturnValue($Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]));
        }
    }
}
