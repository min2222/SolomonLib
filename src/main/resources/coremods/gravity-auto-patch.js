/**
 * Gravity Auto-Patch Coremod
 *
 * For each listed method, replaces the first INVOKEVIRTUAL call to Entity.getX()D / getZ()D
 * with INVOKESTATIC GravityAPI.eyeX / eyeZ so that mobs and AI goals use
 * the correct eye-position when the target entity has non-default gravity.
 *
 * This eliminates the need for per-mob @Redirect mixin files for this pattern.
 */
function initializeCoreMod() {
    var Opcodes      = Java.type("org.objectweb.asm.Opcodes");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var asmapi       = Java.type("net.minecraftforge.coremod.api.ASMAPI");

    var HELPER_CLASS = "com/min01/solomonlib/gravity/GravityAPI";
    var HELPER_DESC  = "(Lnet/minecraft/world/entity/Entity;)D";

    // SRG → runtime method names
    var GET_X   = asmapi.mapMethod("m_20185_");   // Entity.getX()
    var GET_Z   = asmapi.mapMethod("m_20189_");   // Entity.getZ()
    var TICK    = asmapi.mapMethod("m_8127_");    // Goal.tick()
    var AI_STEP = asmapi.mapMethod("m_21334_");   // LivingEntity.aiStep()
    var EXPLODE = asmapi.mapMethod("m_46529_");   // Explosion.explode()

    /**
     * In the given method, replace the first (ordinal 0) INVOKEVIRTUAL to getX()D
     * and the first INVOKEVIRTUAL to getZ()D with INVOKESTATIC equivalents.
     * @returns true if at least one replacement was made
     */
    function patchXZ(methodNode, className) {
        var patchedX = false;
        var patchedZ = false;
        var list = methodNode.instructions;
        for (var i = 0; i < list.size(); i++) {
            var insn = list.get(i);
            if (insn.getOpcode && insn.getOpcode() === Opcodes.INVOKEVIRTUAL && insn.desc === "()D") {
                if (!patchedX && insn.name === GET_X) {
                    list.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, "eyeX", HELPER_DESC, false));
                    patchedX = true;
                } else if (!patchedZ && insn.name === GET_Z) {
                    list.set(insn, new MethodInsnNode(Opcodes.INVOKESTATIC, HELPER_CLASS, "eyeZ", HELPER_DESC, false));
                    patchedZ = true;
                }
                if (patchedX && patchedZ) break;
            }
        }
        if (!patchedX || !patchedZ) {
            asmapi.log("WARN", "[GravityAutoPatch] patchXZ incomplete for " + className + "." + methodNode.name + methodNode.desc
                + " (x=" + patchedX + ", z=" + patchedZ + ")");
        }
        return patchedX || patchedZ;
    }

    /**
     * Build a class transformer that finds methods matching the given specs and patches getX/getZ.
     * Each spec: { name: String|null, desc: String }
     *   - name=null means match by descriptor only
     */
    function makeTransformer(specs, label) {
        return function(classNode) {
            asmapi.log("INFO", "[GravityAutoPatch] Transforming " + label);
            var methods = classNode.methods;
            for (var i = 0; i < methods.size(); i++) {
                var m = methods.get(i);
                for (var j = 0; j < specs.length; j++) {
                    var spec = specs[j];
                    var nameOk = (spec.name === null || m.name === spec.name);
                    if (nameOk && m.desc === spec.desc) {
                        var ok = patchXZ(m, label);
                        asmapi.log(ok ? "INFO" : "WARN",
                            "[GravityAutoPatch] " + (ok ? "OK" : "FAIL") + " " + label + "." + m.name + m.desc);
                    }
                }
            }
            return classNode;
        };
    }

    return {

        // BegGoal.tick() — targets Player.getX/Z
        'gravity-beg-goal': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.ai.goal.BegGoal'
            },
            'transformer': makeTransformer(
                [{ name: TICK, desc: "()V" }],
                "BegGoal"
            )
        },

        // EnderMan.isLookingAtMe(Player) and teleportTowards(Entity)
        'gravity-enderman': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.monster.EnderMan'
            },
            'transformer': makeTransformer(
                [
                    { name: null, desc: "(Lnet/minecraft/world/entity/player/Player;)Z" },
                    { name: null, desc: "(Lnet/minecraft/world/entity/Entity;)Z" }
                ],
                "EnderMan"
            )
        },

        // LookAtPlayerGoal.tick() — targets Entity.getX/Z
        'gravity-look-at-player-goal': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.ai.goal.LookAtPlayerGoal'
            },
            'transformer': makeTransformer(
                [{ name: TICK, desc: "()V" }],
                "LookAtPlayerGoal"
            )
        },

        // LookControl.setLookAt(Entity) and setLookAt(Entity, float, float)
        'gravity-look-control': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.ai.control.LookControl'
            },
            'transformer': makeTransformer(
                [
                    { name: null, desc: "(Lnet/minecraft/world/entity/Entity;)V" },
                    { name: null, desc: "(Lnet/minecraft/world/entity/Entity;FF)V" }
                ],
                "LookControl"
            )
        },

        // Mob.lookAt(Entity, float, float)
        'gravity-mob-look-at': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.Mob'
            },
            'transformer': makeTransformer(
                [{ name: null, desc: "(Lnet/minecraft/world/entity/Entity;FF)V" }],
                "Mob"
            )
        },

        // WitherBoss.aiStep() — the simple getX/Z redirect (performRangedAttack is kept in WitherBossMixin)
        'gravity-wither-boss': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.entity.boss.wither.WitherBoss'
            },
            'transformer': makeTransformer(
                [{ name: AI_STEP, desc: "()V" }],
                "WitherBoss"
            )
        },

        // Explosion.explode() — the getX/Z calls for eye-based position
        'gravity-explosion': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.Explosion'
            },
            'transformer': makeTransformer(
                [{ name: EXPLODE, desc: "()V" }],
                "Explosion"
            )
        },

        // ClientPacketListener.handleGameEvent — thunder bolt entity position
        'gravity-client-packet-listener': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.multiplayer.ClientPacketListener'
            },
            'transformer': makeTransformer(
                [{ name: null, desc: "(Lnet/minecraft/network/protocol/game/ClientboundGameEventPacket;)V" }],
                "ClientPacketListener"
            )
        }
    };
}
