package hospital.logging;

import jade.core.Agent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoggerSMA {

    private static final long startTime = System.currentTimeMillis();
    private static int currentTick = 0;

    private static final Queue<String> buffer = new ConcurrentLinkedQueue<>();
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    // 🎨 ANSI Colors
    private static final String RESET   = "\u001B[0m";
    private static final String GREEN   = "\u001B[32m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String RED     = "\u001B[31m";
    private static final String CYAN    = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String BOLD_RED = "\u001B[1;31m"; // Vermelho intenso

    // ========================
    // Tick
    // ========================
    public static synchronized void setTick(int tick) {
        currentTick = tick;
    }

    public static synchronized void flushTick() {
        System.out.println("\n🕒 === Tick " + currentTick + " ===");
        while (!buffer.isEmpty()) {
            System.out.println(buffer.poll());
        }
    }

    // ========================
    // Níveis de log
    // ========================
    public static void info(Agent agent, String msg, Object... args) {
        log(agent, "INFO", GREEN, msg, args);
    }

    public static void warn(Agent agent, String msg, Object... args) {
        log(agent, "WARN", YELLOW, msg, args);
    }

    public static void error(Agent agent, String msg, Object... args) {
        log(agent, "ERROR", RED, msg, args);
    }

    public static void event(Agent agent, String msg, Object... args) {
        log(agent, "EVENT", MAGENTA, msg, args);
    }

    // ========================
    // Core do log
    // ========================
    private static void log(Agent agent, String level, String color, String msg, Object... args) {
        String time = timeFormat.format(new Date());
        String formatted = String.format(msg, args);

        // 🔥 Se a mensagem contiver morte, use vermelho intenso
        if (formatted.contains("MORTE") || formatted.toLowerCase().contains("morreu")) {
            color = BOLD_RED;
        }

        String line = String.format(
                "%s[%s][%s][Tick %d][%s|%s] %s%s",
                color,
                time,
                level,
                currentTick,
                agent != null ? agent.getLocalName() : "System",
                agent != null ? agent.getClass().getSimpleName() : "NoAgent",
                formatted,
                RESET
        );

        buffer.add(line);
    }

    // ========================
    // Mensagens diretas do sistema (sem buffer)
    // ========================
    public static void system(String msg, Object... args) {
        String time = timeFormat.format(new Date());
        String formatted = String.format(msg, args);
        System.out.printf("%s[System][%s] %s%s%n", CYAN, time, formatted, RESET);
    }
}
