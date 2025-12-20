function initializeCoreMod() {
    return {
        'embeddium-chunk': {
            'target': {
                'type': 'CLASS',
                'name': 'me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer'
            },
            'transformer': function(classNode) {
                var Opcodes = Java.type("org.objectweb.asm.Opcodes");
                var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
                var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
                var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
                var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
                var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
                var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
                var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
                var LdcInsnNode = Java.type("org.objectweb.asm.tree.LdcInsnNode");
                var TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");

                var asmapi = Java.type("net.minecraftforge.coremod.api.ASMAPI");
                asmapi.log("INFO", "Patching Embeddium DefaultChunkRenderer");

                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
                    if (method.name === "render") {
                        var instructions = method.instructions;
                        for (var insn = instructions.getFirst(); insn != null; insn = insn.getNext()) {
                            // Find the first assignment to useBlockFaceCulling (ISTORE) after Embeddium.options().performance.useBlockFaceCulling
                            // This is a simplification; you may need to adjust the matching depending on your mappings and code.
                            if (insn.getOpcode() === Opcodes.ISTORE) {
                                // Insert our OR logic after value is on stack, before ISTORE
                                // Stack: [original boolean]
                                var inject = new InsnList();

                                // -- inject SolomonUtil.isUpsideDown(SolomonClientUtil.MC.player)
                                // Get: SolomonClientUtil.MC
								inject.add(new FieldInsnNode(
								    Opcodes.GETSTATIC,
								    "com/min01/solomonlib/util/SolomonClientUtil",
								    "MC",
								    "Lnet/minecraft/client/Minecraft;"
								));
								inject.add(new FieldInsnNode(
								    Opcodes.GETFIELD,
								    "net/minecraft/client/Minecraft",
								    asmapi.mapField("f_91074_"),
								    "Lnet/minecraft/client/player/LocalPlayer;"
								));
                                // Call: MirroredCityUtil.isUpsideDown(player)
                                inject.add(new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    "com/min01/solomonlib/util/SolomonUtil",
                                    "isUpsideDown",
                                    "(Lnet/minecraft/world/entity/Entity;)Z", // Update descriptor if needed
                                    false
                                ));
								inject.add(new InsnNode(Opcodes.ICONST_1));
								inject.add(new InsnNode(Opcodes.IXOR)); // Negate
								inject.add(new InsnNode(Opcodes.IAND)); // AND with original

                                // Insert before ISTORE (so stack: [old, ourNew] -> IOR -> ISTORE)
                                instructions.insertBefore(insn, inject);

                                asmapi.log("INFO", "Injected logic for useBlockFaceCulling at " + insn);
                                break;
                            }
                        }
                    }
                }

                return classNode;
            }
        }
    };
}