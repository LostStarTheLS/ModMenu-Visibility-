package net.tripleswapping.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class RaycastHelper {

    /**
     * Finds an entity in the player's crosshair up to a specified maximum reach distance.
     * Replicates the client-side target calculation that occurs when holding an extended reach weapon.
     */
    public static EntityHitResult raycastEntity(MinecraftClient client, double maxDistance) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return null;
        }

        float tickDelta = 1.0f;
        Vec3d cameraPos = player.getCameraPosVec(tickDelta);
        Vec3d rotationVec = player.getRotationVec(tickDelta);
        Vec3d reachVec = cameraPos.add(rotationVec.multiply(maxDistance));

        Box box = player.getBoundingBox()
                .stretch(rotationVec.multiply(maxDistance))
                .expand(1.0, 1.0, 1.0);

        double maxDistanceSq = maxDistance * maxDistance;

        // Perform raycast against entities
        EntityHitResult hitResult = ProjectileUtil.raycast(
                player,
                cameraPos,
                reachVec,
                box,
                entity -> !entity.isSpectator() && entity.canHit() && entity != player,
                maxDistanceSq
        );

        if (hitResult != null) {
            Entity target = hitResult.getEntity();
            // Check if line of sight is obstructed by blocks
            HitResult blockHit = client.world.raycast(new net.minecraft.world.RaycastContext(
                    cameraPos,
                    hitResult.getPos(),
                    net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE,
                    player
            ));

            if (blockHit.getType() != HitResult.Type.MISS) {
                double blockDistSq = cameraPos.squaredDistanceTo(blockHit.getPos());
                double entityDistSq = cameraPos.squaredDistanceTo(hitResult.getPos());
                if (blockDistSq < entityDistSq) {
                    return null; // Obstructed by a block
                }
            }

            return hitResult;
        }

        return null;
    }
}
