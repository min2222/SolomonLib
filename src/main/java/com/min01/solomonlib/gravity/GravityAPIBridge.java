package com.min01.solomonlib.gravity;

import java.lang.reflect.Method;

public final class GravityAPIBridge
{
    private static volatile boolean initialised = false;
    private static Class<?> API_CLASS;

    private static Method M_EYE_X;
    private static Method M_EYE_Y;
    private static Method M_EYE_Z;

    private static Method M_RANGED_BODY_X;
    private static Method M_RANGED_BODY_Y;
    private static Method M_RANGED_BODY_Z;
    private static Method M_RANGED_EYE_X;
    private static Method M_RANGED_EYE_Y;
    private static Method M_RANGED_EYE_Z;
    private static Method M_RANGED_SQRT;

    private static Method M_ADD_WITH_GRAVITY;
    private static Method M_SCALE;
    private static Method M_SCALE_F;
    private static Method M_DELTA_MOVEMENT;

    private static void init()
    {
        if (initialised) return;
        synchronized (GravityAPIBridge.class)
        {
            if (initialised) return;
            try
            {
                API_CLASS = Class.forName("com.min01.solomonlib.gravity.GravityAPI");

                Class<?> cEntity  = Class.forName("net.minecraft.world.entity.Entity");
                Class<?> cLiving  = Class.forName("net.minecraft.world.entity.LivingEntity");
                Class<?> cVec3    = Class.forName("net.minecraft.world.phys.Vec3");

                M_EYE_X = API_CLASS.getMethod("eyeX",  cEntity);
                M_EYE_Y = API_CLASS.getMethod("eyeY",  cEntity);
                M_EYE_Z = API_CLASS.getMethod("eyeZ",  cEntity);

                M_RANGED_BODY_X  = API_CLASS.getMethod("rangedBodyTargetX", cLiving);
                M_RANGED_BODY_Y  = API_CLASS.getMethod("rangedBodyTargetY", cLiving, double.class);
                M_RANGED_BODY_Z  = API_CLASS.getMethod("rangedBodyTargetZ", cLiving);
                M_RANGED_EYE_X   = API_CLASS.getMethod("rangedEyeTargetX",  cLiving);
                M_RANGED_EYE_Y   = API_CLASS.getMethod("rangedEyeTargetY",  cLiving);
                M_RANGED_EYE_Z   = API_CLASS.getMethod("rangedEyeTargetZ",  cLiving);
                M_RANGED_SQRT    = API_CLASS.getMethod("rangedSqrt", double.class, cLiving);

                M_ADD_WITH_GRAVITY = API_CLASS.getMethod("addWithGravity",
                    cVec3, double.class, double.class, double.class, cEntity);
                M_SCALE   = API_CLASS.getMethod("scale",  double.class, cEntity);
                M_SCALE_F = API_CLASS.getMethod("scaleF", float.class,  cEntity);
                M_DELTA_MOVEMENT = API_CLASS.getMethod("deltaMovement", cLiving);

                initialised = true;
            }
            catch (Exception e)
            {
                throw new RuntimeException("[SolomonLib] GravityAPIBridge init failed", e);
            }
        }
    }
    
    private static double invokeD(Method m, Object... args)
    {
        try { return (double) m.invoke(null, args); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static float invokeF(Method m, Object... args)
    {
        try { return (float) m.invoke(null, args); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    private static Object invokeO(Method m, Object... args)
    {
        try { return m.invoke(null, args); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    public static double eyeX(Object entity)
    {
        init();
        return invokeD(M_EYE_X, entity);
    }

    public static double eyeY(Object entity)
    {
        init();
        return invokeD(M_EYE_Y, entity);
    }

    public static double eyeZ(Object entity)
    {
        init();
        return invokeD(M_EYE_Z, entity);
    }

    public static double rangedBodyTargetX(Object target)
    {
        init();
        return invokeD(M_RANGED_BODY_X, target);
    }

    public static double rangedBodyTargetY(Object target, double heightScale)
    {
        init();
        return invokeD(M_RANGED_BODY_Y, target, heightScale);
    }

    public static double rangedBodyTargetZ(Object target)
    {
        init();
        return invokeD(M_RANGED_BODY_Z, target);
    }

    public static double rangedEyeTargetX(Object target)
    {
        init();
        return invokeD(M_RANGED_EYE_X, target);
    }

    public static double rangedEyeTargetY(Object target)
    {
        init();
        return invokeD(M_RANGED_EYE_Y, target);
    }

    public static double rangedEyeTargetZ(Object target)
    {
        init();
        return invokeD(M_RANGED_EYE_Z, target);
    }

    public static double rangedSqrt(double value, Object target)
    {
        init();
        return invokeD(M_RANGED_SQRT, value, target);
    }

    public static Object addWithGravity(Object vec, double x, double y, double z, Object entity)
    {
        init();
        return invokeO(M_ADD_WITH_GRAVITY, vec, x, y, z, entity);
    }

    public static double scale(double constant, Object entity)
    {
        init();
        return invokeD(M_SCALE, constant, entity);
    }

    public static float scaleF(float value, Object entity)
    {
        init();
        return invokeF(M_SCALE_F, value, entity);
    }
    
    public static Object deltaMovement(Object target)
    {
        init();
        return invokeO(M_DELTA_MOVEMENT, target);
    }
}
