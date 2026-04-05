/**
 * Gravity Strength Patch Coremod
 *
 * For each listed entity class, multiplies the gravity constant in the physics
 * method by GravityAPI.getGravityStrength(entity) to support variable gravity
 * strength per entity.
 *
 * Three patterns:
 *   A1 — Vec3.add(x, y, z) in tick(): replaces INVOKEVIRTUAL with INVOKESTATIC
 *         addWithGravity(vec, x, y, z, entity) so only the y component is scaled.
 *   A2 — LDC double constant: inserts ALOAD_0 + scale(D, Entity)D after the LDC.
 *   A3 — getGravity()F return: inserts ALOAD_0 + scaleF(F, Entity)F before FRETURN.
 */
function initializeCoreMod() {
    var Opcodes        = Java.type("org.objectweb.asm.Opcodes");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var VarInsnNode    = Java.type("org.objectweb.asm.tree.VarInsnNode");
    var InsnList       = Java.type("org.objectweb.asm.tree.InsnList");
    var asmapi         = Java.type("net.minecraftforge.coremod.api.ASMAPI");

    var HELPER_CLASS = "com/min01/solomonlib/gravity/GravityAPI";
    var ENTITY_TICK  = asmapi.mapMethod("m_8119_"); // Entity.tick()

    // -----------------------------------------------------------------------
    // Pattern A1 — Vec3.add(x, y, z) in tick()
    //
    // The Mixin equivalent is @ModifyArg(index=1) which modifies the y argument.
    // In ASM: before the INVOKEVIRTUAL Vec3.add(DDD)Vec3, the stack is
    //   [..., vec, x, y, z]
    // We insert ALOAD_0 (entity=this) to get:
    //   [..., vec, x, y, z, entity]
    // then switch to INVOKESTATIC addWithGravity(Vec3,D,D,D,Entity)Vec3
    // which multiplies y by getGravityStrength before calling vec.add.
    // -----------------------------------------------------------------------
    function patchVecAdd(classNode, label) {
        var methods = classNode.methods;
        var patched = false;
        for (var i = 0; i < methods.size(); i++) {
            var m = methods.get(i);
            if (m.name === ENTITY_TICK && m.desc === "()V") {
                var list = m.instructions;
                for (var j = 0; j < list.size(); j++) {
                    var insn = list.get(j);
                    if (insn.getOpcode
                        && insn.getOpcode() === Opcodes.INVOKEVIRTUAL
                        && insn.owner === "net/minecraft/world/phys/Vec3"
                        && insn.name  === "add"
                        && insn.desc  === "(DDD)Lnet/minecraft/world/phys/Vec3;") {
                        list.insertBefore(insn, new VarInsnNode(Opcodes.ALOAD, 0));
                        list.set(insn, new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HELPER_CLASS,
                            "addWithGravity",
                            "(Lnet/minecraft/world/phys/Vec3;DDDLnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/phys/Vec3;",
                            false
                        ));
                        patched = true;
                        break;
                    }
                }
                break;
            }
        }
        asmapi.log(patched ? "INFO" : "WARN",
            "[GravityStrengthPatch] A1 " + (patched ? "OK" : "FAIL") + " " + label);
        return classNode;
    }

    // -----------------------------------------------------------------------
    // Pattern A2 — LDC double constant
    //
    // Scans all methods for the specific double value (avoids needing SRG name
    // for private methods like Boat.floatBoat). After the LDC insn, inserts:
    //   ALOAD_0; INVOKESTATIC scale(D, Entity)D
    // so the constant is multiplied by getGravityStrength(this).
    // -----------------------------------------------------------------------
    function patchLdcConstant(classNode, targetValue, label) {
        var methods = classNode.methods;
        var patched = false;
        for (var i = 0; i < methods.size(); i++) {
            var m = methods.get(i);
            var list = m.instructions;
            for (var j = 0; j < list.size(); j++) {
                var insn = list.get(j);
                if (insn.getOpcode
                    && insn.getOpcode() === Opcodes.LDC
                    && insn.cst !== null
                    && Math.abs(Number(insn.cst) - targetValue) < 1e-15) {
                    var after = new InsnList();
                    after.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    after.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER_CLASS,
                        "scale",
                        "(DLnet/minecraft/world/entity/Entity;)D",
                        false
                    ));
                    list.insert(insn, after);
                    patched = true;
                    break;
                }
            }
            if (patched) break;
        }
        asmapi.log(patched ? "INFO" : "WARN",
            "[GravityStrengthPatch] A2 " + (patched ? "OK" : "FAIL") + " " + label);
        return classNode;
    }

    // -----------------------------------------------------------------------
    // Pattern A3 — getGravity()F FRETURN
    //
    // Collects all FRETURN nodes first, then for each inserts before it:
    //   ALOAD_0; INVOKESTATIC scaleF(F, Entity)F
    // so the return value is multiplied by getGravityStrength(this).
    // -----------------------------------------------------------------------
    function patchGetGravity(classNode, label) {
        var methods = classNode.methods;
        var patched = false;
        for (var i = 0; i < methods.size(); i++) {
            var m = methods.get(i);
            if (m.name === "getGravity" && m.desc === "()F") {
                var list = m.instructions;
                // Collect all FRETURN nodes before modifying the list
                var freturns = [];
                for (var j = 0; j < list.size(); j++) {
                    var insn = list.get(j);
                    if (insn.getOpcode && insn.getOpcode() === Opcodes.FRETURN) {
                        freturns.push(insn);
                    }
                }
                for (var k = 0; k < freturns.length; k++) {
                    var before = new InsnList();
                    before.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    before.add(new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        HELPER_CLASS,
                        "scaleF",
                        "(FLnet/minecraft/world/entity/Entity;)F",
                        false
                    ));
                    list.insertBefore(freturns[k], before);
                    patched = true;
                }
                break;
            }
        }
        asmapi.log(patched ? "INFO" : "WARN",
            "[GravityStrengthPatch] A3 " + (patched ? "OK" : "FAIL") + " " + label);
        return classNode;
    }

    return {

        // ---- Pattern A1: Vec3.add(x,y,z) in tick() ----

        'gravity-strength-experience-orb': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.ExperienceOrb' },
            'transformer': function(classNode) { return patchVecAdd(classNode, "ExperienceOrb"); }
        },

        'gravity-strength-llama-spit': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.projectile.LlamaSpit' },
            'transformer': function(classNode) { return patchVecAdd(classNode, "LlamaSpit"); }
        },

        'gravity-strength-primed-tnt': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.item.PrimedTnt' },
            'transformer': function(classNode) { return patchVecAdd(classNode, "PrimedTnt"); }
        },

        'gravity-strength-abstract-minecart': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.vehicle.AbstractMinecart' },
            'transformer': function(classNode) { return patchVecAdd(classNode, "AbstractMinecart"); }
        },

        // ---- Pattern A2: LDC double constant ----

        'gravity-strength-fishing-hook': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.projectile.FishingHook' },
            'transformer': function(classNode) { return patchLdcConstant(classNode, -0.03, "FishingHook"); }
        },

        'gravity-strength-item-entity': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.item.ItemEntity' },
            'transformer': function(classNode) { return patchLdcConstant(classNode, -0.04, "ItemEntity"); }
        },

        'gravity-strength-boat': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.vehicle.Boat' },
            'transformer': function(classNode) { return patchLdcConstant(classNode, -0.03999999910593033, "Boat"); }
        },

        // ---- Pattern A3: getGravity()F ----

        'gravity-strength-thrown-exp-bottle': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.projectile.ThrownExperienceBottle' },
            'transformer': function(classNode) { return patchGetGravity(classNode, "ThrownExperienceBottle"); }
        },

        'gravity-strength-thrown-potion': {
            'target': { 'type': 'CLASS', 'name': 'net.minecraft.world.entity.projectile.ThrownPotion' },
            'transformer': function(classNode) { return patchGetGravity(classNode, "ThrownPotion"); }
        }

    };
}
