package com.min01.solomonlib.coremod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.min01.solomonlib.coremod.transformer.EyePositionTransformer;
import com.min01.solomonlib.coremod.transformer.FallingBlockGravityTransformer;
import com.min01.solomonlib.coremod.transformer.GravityStrengthTransformer;
import com.min01.solomonlib.coremod.transformer.RangedAttackTransformer;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import net.minecraftforge.coremod.api.ASMAPI;

public class SolomonTransformationService implements ITransformationService
{
    private static final Logger LOGGER = LogManager.getLogger("SolomonLib/Coremod");

    private static final byte[] GRAVITY_METHOD = "getGravity".getBytes();
    private static final byte[] RANGED_METHOD = "performRangedAttack".getBytes();
    private static final byte[] EYE_SRG_X = "m_20185_".getBytes();
    private static final byte[] EYE_SRG_Y = "m_20186_".getBytes();
    private static final byte[] EYE_SRG_Z = "m_20189_".getBytes();

    private static final String GRAVITY_DESC = "()F";
    private static final String RANGED_DESC = "(Lnet/minecraft/world/entity/LivingEntity;F)V";

    private Path gameDir;

    @Override
    public String name()
    {
        return "solomonlib";
    }

    @Override
    public void initialize(IEnvironment environment)
    {
        this.gameDir = environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Path.of("."));
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) throws IncompatibleEnvironmentException
    {

    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<ITransformer> transformers()
    {
        long start = System.currentTimeMillis();

        Set<String> gravityTargets  = this.loadCache("gravity");
        Set<String> rangedTargets = this.loadCache("ranged");
        Set<String> eyeTargets = this.loadCache("eye");

        if(gravityTargets == null || rangedTargets == null || eyeTargets == null)
        {
            gravityTargets = new HashSet<>();
            rangedTargets = new HashSet<>();
            eyeTargets = new HashSet<>();
            this.scanAllJars(gravityTargets, rangedTargets, eyeTargets);
            this.saveCache("gravity", gravityTargets);
            this.saveCache("ranged", rangedTargets);
            this.saveCache("eye", eyeTargets);
        }

        long elapsed = System.currentTimeMillis() - start;
        LOGGER.info("[SolomonLib/Coremod] gravity: {}, ranged: {}, eye: {} classes ({}ms)", gravityTargets.size(), rangedTargets.size(), eyeTargets.size(), elapsed);

        return List.of(
            new EyePositionTransformer(eyeTargets),
            new RangedAttackTransformer(rangedTargets),
            new GravityStrengthTransformer(gravityTargets),
            new FallingBlockGravityTransformer());
    }

    private void scanAllJars(Set<String> gravityOut, Set<String> rangedOut, Set<String> eyeOut)
    {
        Path modsDir = this.gameDir.resolve("mods");
        if(!Files.exists(modsDir))
            return;

        List<Path> jars;
        try(Stream<Path> s = Files.walk(modsDir))
        {
            jars = s.filter(p -> p.toString().endsWith(".jar")).collect(Collectors.toList());
        }
        catch(IOException e)
        {
            return;
        }

        jars.parallelStream().forEach(jar ->
        {
            List<String> gravity = new ArrayList<>();
            List<String> ranged  = new ArrayList<>();
            List<String> eye     = new ArrayList<>();
            this.scanJar(jar, gravity, ranged, eye);
            synchronized(gravityOut) { gravityOut.addAll(gravity); }
            synchronized(rangedOut)  { rangedOut.addAll(ranged); }
            synchronized(eyeOut)     { eyeOut.addAll(eye); }
        });
    }

    private void scanJar(Path jarPath, List<String> gravityOut, List<String> rangedOut, List<String> eyeOut)
    {
        try(ZipFile zip = new ZipFile(jarPath.toFile()))
        {
            for(ZipEntry entry : Collections.list(zip.entries()))
            {
                String name = entry.getName();
                if(!name.endsWith(".class") || name.startsWith("META-INF/"))
                    continue;

                try(InputStream is = zip.getInputStream(entry))
                {
                    byte[] bytes = is.readAllBytes();
                    String className = name.replace('/', '.').replace(".class", "");

                    if(containsBytes(bytes, GRAVITY_METHOD) && classDeclaresMethod(bytes, GRAVITY_DESC, "getGravity", "getGravity"))
                        gravityOut.add(className);

                    if(containsBytes(bytes, RANGED_METHOD) && classDeclaresMethod(bytes, RANGED_DESC, ASMAPI.mapMethod("m_6504_"), "performRangedAttack"))
                        rangedOut.add(className);

                    if((containsBytes(bytes, EYE_SRG_X) || containsBytes(bytes, EYE_SRG_Y) || containsBytes(bytes, EYE_SRG_Z)) && classCallsCoordinateMethods(bytes))
                        eyeOut.add(className);
                }
                catch(IOException ignored)
                {

                }
            }
        }
        catch(IOException ignored)
        {

        }
    }

    private static boolean containsBytes(byte[] data, byte[] pattern)
    {
        if(pattern.length > data.length)
            return false;
        int[] skip = new int[256];
        Arrays.fill(skip, pattern.length);
        for(int i = 0; i < pattern.length - 1; i++)
            skip[pattern[i] & 0xFF] = pattern.length - 1 - i;

        int i = pattern.length - 1;
        while(i < data.length)
        {
            int j = pattern.length - 1;
            int k = i;
            while(j >= 0 && data[k] == pattern[j])
            {
                k--;
                j--;
            }
            if(j < 0)
                return true;
            i += skip[data[i] & 0xFF];
        }
        return false;
    }

    private static boolean classDeclaresMethod(byte[] bytes, String desc, String srgName, String deobfName)
    {
        boolean[] found = {false};
        try
        {
            ClassReader cr = new ClassReader(bytes);
            cr.accept(new ClassVisitor(Opcodes.ASM9)
            {
                @Override
                public MethodVisitor visitMethod(int access, String name, String mDesc, String sig, String[] ex)
                {
                    if(found[0])
                        return null;
                    if(desc.equals(mDesc) && (srgName.equals(name) || deobfName.equals(name)))
                        found[0] = true;
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        catch(Exception ignored)
        {

        }
        return found[0];
    }

    private static boolean classCallsCoordinateMethods(byte[] bytes)
    {
        boolean[] found = {false};
        String mappedX = ASMAPI.mapMethod("m_20185_");
        String mappedY = ASMAPI.mapMethod("m_20186_");
        String mappedZ = ASMAPI.mapMethod("m_20189_");
        try
        {
            ClassReader cr = new ClassReader(bytes);
            cr.accept(new ClassVisitor(Opcodes.ASM9)
            {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String sig, String[] ex)
                {
                    if(found[0])
                        return null;
                    return new MethodVisitor(Opcodes.ASM9)
                    {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String mName, String mDesc, boolean itf)
                        {
                            if(found[0])
                                return;
                            if(opcode != Opcodes.INVOKEVIRTUAL)
                                return;
                            if(!"()D".equals(mDesc))
                                return;
                            if(mappedX.equals(mName) || mappedY.equals(mName) || mappedZ.equals(mName))
                                found[0] = true;
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
        catch(Exception ignored)
        {

        }
        return found[0];
    }

    private Path getCacheFile(String type)
    {
        return this.gameDir.resolve(".cache").resolve("solomonlib_" + type + "_scan.cache");
    }

    private Set<String> loadCache(String type)
    {
        Path cacheFile = this.getCacheFile(type);
        if(!Files.exists(cacheFile))
            return null;
        try
        {
            List<String> lines = Files.readAllLines(cacheFile);
            if(lines.isEmpty())
                return null;

            String cachedSignature = lines.get(0);
            String currentSignature = this.computeJarsSignature();
            if(!cachedSignature.equals(currentSignature))
            {
                LOGGER.info("[SolomonLib/Coremod] Detected jar change, Rescanning");
                return null;
            }

            Set<String> targets = new HashSet<>(lines.subList(1, lines.size()));
            LOGGER.info("[SolomonLib/Coremod] Cache [{}] load finished, total: {}", type, targets.size());
            return targets;
        }
        catch(IOException e)
        {
            return null;
        }
    }

    private void saveCache(String type, Set<String> targets)
    {
        try
        {
            Path cacheFile = this.getCacheFile(type);
            Files.createDirectories(cacheFile.getParent());
            List<String> lines = new ArrayList<>();
            lines.add(this.computeJarsSignature());
            lines.addAll(targets);
            Files.write(cacheFile, lines);
        }
        catch(IOException e)
        {
            LOGGER.warn("[SolomonLib/Coremod] Failed to save cache [{}]: {}", type, e.getMessage());
        }
    }

    private String computeJarsSignature()
    {
        Path modsDir = this.gameDir.resolve("mods");
        try(Stream<Path> s = Files.walk(modsDir))
        {
            return s.filter(p -> p.toString().endsWith(".jar")).sorted().map(p ->
            {
                try
                {
                    return p.getFileName() + "|" + Files.getLastModifiedTime(p).toMillis();
                }
                catch(IOException e)
                {
                    return p.getFileName().toString();
                }
            }).collect(Collectors.joining(","));
        }
        catch(IOException e)
        {
            return "";
        }
    }
}