public final class OperatingMode {
    private OperatingMode() {
    } // avoid instantiation

    public static boolean isDomain() {
        return Boolean.parseBoolean(System.getProperty("domain", "false"));
    }
}
