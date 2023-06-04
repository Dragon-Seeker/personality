package io.blodhgarm.personality.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class LookingUtils {

    public static boolean isPlayerStaring(PlayerEntity looker, PlayerEntity sought, double range) {
        Vec3d vec3d = looker.getRotationVec(1.0F).normalize();

        Vec3d vec3d2 = new Vec3d(sought.getX() - looker.getX(), (sought.getBlockY() + (sought.getHeight() / 2)) - looker.getEyeY(), sought.getZ() - looker.getZ());

        double d = vec3d2.length();

        vec3d2 = vec3d2.normalize();

        double e = vec3d.dotProduct(vec3d2);

        return e > 1.0 - (0.085 / d) && canSee(looker, sought, range);
    }

    public static boolean canSee(PlayerEntity looker, PlayerEntity sought, double range) {
        Vec3d min = looker.getCameraPosVec(1.0f);

        Vec3d lookVec = looker.getRotationVec(1.0f);

        Vec3d max = min.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);

        Box box = looker.getBoundingBox().stretch(lookVec.multiply(range)).expand(1.0, 1.0, 1.0);

        EntityHitResult entityHitResult = ProjectileUtil.raycast(looker, min, max, box, entityx -> !entityx.isSpectator() && entityx.canHit(), Math.pow(2, range));

        return entityHitResult != null && entityHitResult.getEntity() == sought;
    }
}
