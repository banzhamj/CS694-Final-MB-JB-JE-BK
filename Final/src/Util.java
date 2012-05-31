public class Util {
    public static void DebugPrint(DbgSub subsys, Object object) {
        if ( DebugConfig.DebugLevels[subsys.ordinal()] ) {
            System.out.println(subsys + ": " + object);
        }
    }
}