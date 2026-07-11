package com.min01.kaijuc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class KaijuCNative
{
    private static boolean LOADED;
    
    static
    {
        loadNative();
    }
    
    private static void loadNative()
    {
        if(LOADED)
        {
            return;
        }
        try
        {
            String platform = platformId();
            String fileName = libraryFileName();
            String resourcePath = "/natives/" + platform + "/" + fileName;
            try(InputStream in = KaijuCNative.class.getResourceAsStream(resourcePath))
            {
                if(in == null)
                {
                    throw new UnsatisfiedLinkError("Missing native: " + resourcePath);
                }
                Path dir = Files.createTempDirectory("kaijuc-");
                dir.toFile().deleteOnExit();
                Path lib = dir.resolve(fileName);
                Files.copy(in, lib, StandardCopyOption.REPLACE_EXISTING);
                lib.toFile().deleteOnExit();
                System.load(lib.toAbsolutePath().toString());
            }
            LOADED = true;
        }
        catch(IOException | UnsatisfiedLinkError e)
        {
            e.printStackTrace();
        }
    }
    
    private static String platformId()
    {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = normalizeArch(System.getProperty("os.arch").toLowerCase(Locale.ROOT));
        if(os.contains("win"))
        {
            return "windows-" + arch;
        }
        if(os.contains("linux"))
        {
            return "linux-" + arch;
        }
        if(os.contains("mac"))
        {
            return "macos-" + arch;
        }
        throw new UnsupportedOperationException("Unsupported OS: " + os);
    }
    
    private static String normalizeArch(String arch)
    {
        if(arch.equals("amd64") || arch.equals("x86_64"))
        {
            return "x86_64";
        }
        if(arch.equals("aarch64"))
        {
            return "aarch64";
        }
        return arch;
    }
    
    private static String libraryFileName()
    {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if(os.contains("win"))
        {
            return "kaijuc.dll";
        }
        if(os.contains("mac"))
        {
            return "libkaijuc.dylib";
        }
        return "libkaijuc.so";
    }
    
    public static native double raycast(double[] obbs, int count, double[] ray);
    public static native boolean intersects(double[] obbs, int count, double[] aabb);
    public static native double[] collide(double[] obbs, int count, double[] aabb, double[] delta);
}
