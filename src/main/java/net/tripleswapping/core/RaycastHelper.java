package net.tripleswapping.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.tripleswapping.config.TripleSwappingConfig;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RaycastHelper {

    public record TargetResult(Entity entity, double distance, Vec3d hitPos) {
    }

    /**
     * Resolves the effective extended reach distance (in blocks) for the current player and reach weapon.
     * Inspects Minecraft 1.21.11's {@link AttackRangeComponent} on the reach slot item when enabled.
     */
    public static double getEffectiveReachDistance(ClientPlayerEntity player, TripleSwappingConfig config) {
        double configuredReach = config.reachDistance;
        if (!config.useItemAttackRangeComponent || player == null) {
            return configuredReach;
        }

        int reachSlot = TripleSwapManager.getInstance().resolveReachSlot(player, config);
        if (reachSlot >= 0 && reachSlot < 9) {
            ItemStack reachStack = player.getInventory().getStack(reachSlot);
            AttackRangeComponent attackRange = reachStack.get(DataComponentTypes.ATTACK_RANGE);
            if (attackRange != null) {
                return Math.max(configuredReach, attackRange.getEffectiveMaxRange(player));
            }
        }
        return configuredReach;
    }

    /**
     * Resolves the effective hitbox expansion margin for 1.21.11 Spear / AttackRangeComponent raycasting.
     * Vanilla 1.21.11 Spears define {@code hitbox_margin = 0.125f}.
     */
    public static double getEffectiveHitboxMargin(ClientPlayerEntity player, TripleSwappingConfig config) {
        double configuredMargin = config.hitboxMargin;
        if (!config.useItemAttackRangeComponent || player == null) {
            return configuredMargin;
        }

        int reachSlot = TripleSwapManager.getInstance().resolveReachSlot(player, config);
        if (reachSlot >= 0 && reachSlot < 9) {
            ItemStack reachStack = player.getInventory().getStack(reachSlot);
            AttackRangeComponent attackRange = reachStack.get(DataComponentTypes.ATTACK_RANGE);
            if (attackRange != null) {
                return Math.max(configuredMargin, attackRange.hitboxMargin());
            }
        }
        return configuredMargin;
    }

    /**
     * Evaluates entity raycasting out to the resolved 1.21.11 extended reach (4.5m survival / 6.5m creative Spear reach)
     * incorporating the 1.21.11 {@code hitboxMargin} (+0.125m) and verifying line-of-sight against solid block colliders.
     * Allows acquiring targets across the full 0.0 -> maxReach range even when inside a Spear's 0–2.0m close-range zone.
     */
    public static TargetResult findTargetInExtendedReach(MinecraftClient client, double maxReach) {
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null || client.world == null || client.player == null) {
            return null;
        }

        TripleSwappingConfig config = TripleSwappingConfig.getInstance();
        double extraMargin = getEffectiveHitboxMargin(client.player, config);

        float tickDelta = client.getRenderTickCounter().getTickProgress(true);
        Vec3d cameraPos = cameraEntity.getCameraPosVec(tickDelta);
        Vec3d lookVec = cameraEntity.getRotationVec(tickDelta);
        Vec3d endPos = cameraPos.add(lookVec.x * maxReach, lookVec.y * maxReach, lookVec.z * maxReach);

        // Clip maximum reach against intervening solid blocks first
        BlockHitResult blockHit = client.world.raycast(new RaycastContext(
                cameraPos,
                endPos,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                cameraEntity
        ));

        double closestDistSq = maxReach * maxReach;
        if (blockHit != null && blockHit.getType() != HitResult.Type.MISS) {
            closestDistSq = cameraPos.squaredDistanceTo(blockHit.getPos());
        }

        Box searchBox = cameraEntity.getBoundingBox()
                .stretch(lookVec.multiply(maxReach))
                .expand(1.0 + extraMargin);

        Predicate<Entity> targetPredicate = entity -> !entity.isSpectator()
                && entity.canHit()
                && entity != client.player
                && entity != client.player.getVehicle();

        List<Entity> candidates = client.world.getOtherEntities(cameraEntity, searchBox, targetPredicate);

        Entity closestEntity = null;
        Vec3d closestHitPos = null;

        for (Entity candidate : candidates) {
            Box expandedBox = candidate.getBoundingBox().expand(candidate.getTargetingMargin() + extraMargin);
            if (expandedBox.contains(cameraPos)) {
                closestEntity = candidate;
                closestHitPos = cameraPos;
                closestDistSq = 0.0;
                break;
            }

            Optional<Vec3d> hitOptional = expandedBox.raycast(cameraPos, endPos);
            if (hitOptional.isPresent()) {
                Vec3d hitVec = hitOptional.get();
                double distSq = cameraPos.squaredDistanceTo(hitVec);
                if (distSq < closestDistSq) {
                    closestDistSq = distSq;
                    closestEntity = candidate;
                    closestHitPos = hitVec;
                }
            }
        }

        if (closestEntity == null || closestHitPos == null) {
            return null;
        }

        return new TargetResult(closestEntity, Math.sqrt(closestDistSq), closestHitPos);
    }

    /**
     * Checks whether an entity currently falls outside normal melee reach.
     */
    public static boolean isOutOfNormalRange(MinecraftClient client, Entity target) {
        if (target == null || client.getCameraEntity() == null) {
            return false;
        }

        TripleSwappingConfig config = TripleSwappingConfig.getInstance();
        float tickDelta = client.getRenderTickCounter().getTickProgress(true);
        Vec3d cameraPos = client.getCameraEntity().getCameraPosVec(tickDelta);

        double distSq = target.getBoundingBox().squaredMagnitude(cameraPos);
        double normalReachSq = config.normalReach * config.normalReach;

        if (distSq > normalReachSq) {
            return true;
        }

        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hit = (EntityHitResult) client.crosshairTarget;
            if (hit.getEntity() == target) {
                return cameraPos.distanceTo(hit.getPos()) > config.normalReach;
            }
        }

        return cameraPos.distanceTo(target.getBoundingBox().getCenter()) > config.normalReach + 0.5;
    }

    /**
     * Evaluates whether the target is a valid attackable living entity or combat target.
     */
    public static boolean isAttackableTarget(Entity entity) {
        return entity != null && entity.isAlive() && (entity instanceof LivingEntity || entity.canHit());
    }
}
