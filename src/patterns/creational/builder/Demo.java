package patterns.creational.builder;

public class Demo {
    public static void main(String[] args) {
        // Minimal: required name only, defaults fill the rest.
        LoggerConfig basic = LoggerConfig.named("app").build();
        System.out.println(basic);

        // Full fluent chain — reads like the config it describes.
        LoggerConfig audit = LoggerConfig.named("audit")
                .level(LoggerConfig.Level.WARN)
                .console(false)
                .file("/var/log/audit.log", 50)
                .json()
                .build();
        System.out.println(audit);

        // Failure path: cross-field rule caught at build time, not at use time.
        try {
            LoggerConfig.named("broken").console(false).build();
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
        try {
            LoggerConfig.named("broken").file("/var/log/x.log", 0).build();
        } catch (IllegalArgumentException e) {
            System.out.println("rejected: " + e.getMessage());
        }
    }
}
