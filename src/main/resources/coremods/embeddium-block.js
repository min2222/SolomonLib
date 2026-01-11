function initializeCoreMod() {
    return {
        'embeddium-block': {
            'target': {
                'type': 'CLASS',
                'name': 'me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer'
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
                asmapi.log("INFO", "Patching Embeddium BlockRenderer");

                var methods = classNode.methods;
                for (var i = 0; i < methods.size(); i++) {
                    var method = methods.get(i);
					if (method.name === "writeGeometry") {
					    // Find the instruction after the Z assignment (original L11)
					    var targetIndex = -1;
					    for (var j = 0; j < method.instructions.size(); j++) {
					        var insn = method.instructions.get(j);
					        // Find the putfield for .z (last original coordinate assignment)
					        if (insn.getOpcode && insn.getOpcode() === Opcodes.PUTFIELD) {
					            if (insn instanceof FieldInsnNode && insn.name === "z") {
					                targetIndex = j + 1;
					                break;
					            }
					        }
					    }
					    if (targetIndex === -1) {
					        asmapi.log("WARN", "Couldn't find .z putfield; patch not applied!");
					        continue;
					    }

					    // Prepare our label nodes for branching
					    var labelIfNotUpsideDown = new LabelNode();
					    var labelEndPatch = new LabelNode();

					    // Build the injected instructions
					    var inject = new InsnList();

					    // Call SolomonUtil.isBlockUpsideDown(ctx.pos(), MC.level)
					    inject.add(new FieldInsnNode(
					        Opcodes.GETSTATIC,
					        "com/min01/solomonlib/util/SolomonClientUtil",
					        "MC",
					        "Lnet/minecraft/client/Minecraft;"
					    ));
					    inject.add(new FieldInsnNode(
					        Opcodes.GETFIELD,
					        "net/minecraft/client/Minecraft",
					        asmapi.mapField("f_91073_"),
					        "Lnet/minecraft/client/multiplayer/ClientLevel;"
					    ));
						inject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ctx
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
						    "pos",
						    "()Lnet/minecraft/core/BlockPos;",
						    false
						));
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKESTATIC,
					        "com/min01/solomonlib/util/SolomonUtil",
					        "isBlockUpsideDown",
					        "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
					        false
					    ));
					    inject.add(new JumpInsnNode(Opcodes.IFEQ, labelIfNotUpsideDown));

					    // If upside down:
					    // srcIndex = orientation.getVertexIndex(3 - dstIndex);
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 8)); // orientation
					    inject.add(new InsnNode(Opcodes.ICONST_3));
					    inject.add(new VarInsnNode(Opcodes.ILOAD, 11)); // dstIndex
					    inject.add(new InsnNode(Opcodes.ISUB));
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEVIRTUAL,
					        "me/jellysquid/mods/sodium/client/model/quad/properties/ModelQuadOrientation",
					        "getVertexIndex",
					        "(I)I",
					        false
					    ));
					    inject.add(new VarInsnNode(Opcodes.ISTORE, 12)); // srcIndex

					    // out.x = ctx.origin().x() + quad.getX(srcIndex) + (float) offset.x();
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 13)); // out
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ctx
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEVIRTUAL,
					        "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
					        "origin",
					        "()Lorg/joml/Vector3fc;",
					        false
					    ));
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEINTERFACE,
					        "org/joml/Vector3fc",
					        "x",
					        "()F",
					        true
					    ));
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 5)); // quad
					    inject.add(new VarInsnNode(Opcodes.ILOAD, 12)); // srcIndex
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEINTERFACE,
					        "me/jellysquid/mods/sodium/client/model/quad/BakedQuadView",
					        "getX",
					        "(I)F",
					        true
					    ));
					    inject.add(new InsnNode(Opcodes.FADD));
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 3)); // offset
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEVIRTUAL,
					        "net/minecraft/world/phys/Vec3",
					        asmapi.mapMethod("m_7096_"), // "x" getter, double
					        "()D",
					        false
					    ));
					    inject.add(new InsnNode(Opcodes.D2F));
					    inject.add(new InsnNode(Opcodes.FADD));
					    inject.add(new FieldInsnNode(
					        Opcodes.PUTFIELD,
					        "me/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex",
					        "x",
					        "F"
					    ));

					    // out.y = ctx.origin().y() + 1.0F - quad.getY(srcIndex) + (float) offset.y();
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 13)); // out
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ctx
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEVIRTUAL,
					        "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
					        "origin",
					        "()Lorg/joml/Vector3fc;",
					        false
					    ));
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEINTERFACE,
					        "org/joml/Vector3fc",
					        "y",
					        "()F",
					        true
					    ));
					    inject.add(new InsnNode(Opcodes.FCONST_1));
					    inject.add(new InsnNode(Opcodes.FADD));
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 5)); // quad
					    inject.add(new VarInsnNode(Opcodes.ILOAD, 12)); // srcIndex
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEINTERFACE,
					        "me/jellysquid/mods/sodium/client/model/quad/BakedQuadView",
					        "getY",
					        "(I)F",
					        true
					    ));
					    inject.add(new InsnNode(Opcodes.FSUB));
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 3)); // offset
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEVIRTUAL,
					        "net/minecraft/world/phys/Vec3",
					        asmapi.mapMethod("m_7098_"), // "y" getter, double
					        "()D",
					        false
					    ));
					    inject.add(new InsnNode(Opcodes.D2F));
					    inject.add(new InsnNode(Opcodes.FADD));
					    inject.add(new FieldInsnNode(
					        Opcodes.PUTFIELD,
					        "me/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex",
					        "y",
					        "F"
					    ));

					    // out.z = ctx.origin().z() + quad.getZ(srcIndex) + (float) offset.z();
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 13)); // out
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ctx
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEVIRTUAL,
					        "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
					        "origin",
					        "()Lorg/joml/Vector3fc;",
					        false
					    ));
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEINTERFACE,
					        "org/joml/Vector3fc",
					        "z",
					        "()F",
					        true
					    ));
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 5)); // quad
					    inject.add(new VarInsnNode(Opcodes.ILOAD, 12)); // srcIndex
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEINTERFACE,
					        "me/jellysquid/mods/sodium/client/model/quad/BakedQuadView",
					        "getZ",
					        "(I)F",
					        true
					    ));
					    inject.add(new InsnNode(Opcodes.FADD));
					    inject.add(new VarInsnNode(Opcodes.ALOAD, 3)); // offset
					    inject.add(new MethodInsnNode(
					        Opcodes.INVOKEVIRTUAL,
					        "net/minecraft/world/phys/Vec3",
					        asmapi.mapMethod("m_7094_"), // "z" getter, double
					        "()D",
					        false
					    ));
					    inject.add(new InsnNode(Opcodes.D2F));
					    inject.add(new InsnNode(Opcodes.FADD));
					    inject.add(new FieldInsnNode(
					        Opcodes.PUTFIELD,
					        "me/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex",
					        "z",
					        "F"
					    ));

					    // Jump past the original assignments
					    inject.add(new JumpInsnNode(Opcodes.GOTO, labelEndPatch));

					    // Not upside-down branch
					    inject.add(labelIfNotUpsideDown);
					    // (original assignments run here)

					    // End label
					    inject.add(labelEndPatch);

					    // Insert after the z assignment
					    method.instructions.insert(method.instructions.get(targetIndex - 1), inject);
					    asmapi.log("INFO", "Successfully patched writeGeometry for upside-down check.");
					}
					if (method.name === "isFaceVisible") {
						var instructions = method.instructions;
						var inject = new InsnList();
						var labelContinue = new LabelNode();
						var labelNotYAxis = new LabelNode();

						// ctx is arg1 (1), face is arg2 (2)

						// isBlockUpsideDown(ctx.pos(), SolomonClientUtil.MC.level)
						inject.add(new FieldInsnNode(
						    Opcodes.GETSTATIC,
						    "com/min01/solomonlib/util/SolomonClientUtil",
						    "MC",
						    "Lnet/minecraft/client/Minecraft;"
						));
						inject.add(new FieldInsnNode(
						    Opcodes.GETFIELD,
						    "net/minecraft/client/Minecraft",
						    asmapi.mapField("f_91073_"),
						    "Lnet/minecraft/client/multiplayer/ClientLevel;"
						));
						inject.add(new VarInsnNode(Opcodes.ALOAD, 1));
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
						    "pos",
						    "()Lnet/minecraft/core/BlockPos;",
						    false
						));
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKESTATIC,
						    "com/min01/solomonlib/util/SolomonUtil",
						    "isBlockUpsideDown",
						    "(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z",
						    false
						));
						inject.add(new JumpInsnNode(Opcodes.IFEQ, labelContinue));

						// if (face.getAxis() != Direction.Axis.Y) goto continue;
						inject.add(new VarInsnNode(Opcodes.ALOAD, 2)); // face
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "net/minecraft/core/Direction",
						    asmapi.mapMethod("m_122434_"),
						    "()Lnet/minecraft/core/Direction$Axis;",
						    false
						));
						inject.add(new FieldInsnNode(
						    Opcodes.GETSTATIC,
						    "net/minecraft/core/Direction$Axis",
						    "Y",
						    "Lnet/minecraft/core/Direction$Axis;"
						));
						inject.add(new JumpInsnNode(Opcodes.IF_ACMPNE, labelContinue));

						// return this.occlusionCache.shouldDrawSide(ctx.state(), ctx.localSlice(), ctx.pos(), face.getOpposite());
						inject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
						inject.add(new FieldInsnNode(
						    Opcodes.GETFIELD,
						    "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer",
						    "occlusionCache",
						    "Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockOcclusionCache;"
						));
						inject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ctx
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
						    "state",
						    "()Lnet/minecraft/world/level/block/state/BlockState;",
						    false
						));
						inject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ctx
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
						    "localSlice",
						    "()Lnet/minecraft/world/level/BlockAndTintGetter;",
						    false
						));
						inject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // ctx
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext",
						    "pos",
						    "()Lnet/minecraft/core/BlockPos;",
						    false
						));
						inject.add(new VarInsnNode(Opcodes.ALOAD, 2)); // face
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "net/minecraft/core/Direction",
						    asmapi.mapMethod("m_122424_"),
						    "()Lnet/minecraft/core/Direction;",
						    false
						));
						inject.add(new MethodInsnNode(
						    Opcodes.INVOKEVIRTUAL,
						    "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockOcclusionCache",
						    "shouldDrawSide",
						    "(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z",
						    false
						));
						inject.add(new InsnNode(Opcodes.IRETURN)); // return

						// continue:
						inject.add(labelContinue);

						// Insert at the beginning
						instructions.insert(inject);

						asmapi.log("INFO", "Patched isFaceVisible to check upside-down blocks.");
					}
                }

                return classNode;
            }
        }
    };
}