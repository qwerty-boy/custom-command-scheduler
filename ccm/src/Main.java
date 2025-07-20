
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        File file = new File("./ccm/src/tmp/commands.txt");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        FileOutputStream fileOutputStream = new FileOutputStream(new File("./ccm/src/tmp/sample-output.txt"));
        try (Scanner scanner = new Scanner(new FileReader(file))) {
            while (scanner.hasNext()) {
                String command = scanner.nextLine();
                if (command.startsWith("*")) {
                    String[] parts = command.split("\\*");
                    if (parts.length > 1) {
                        String[] timeToExecute = parts[1].split(" ");
                        byte[] dataBytes = command.split("&&")[1].replace("echo", "").getBytes();
                        Runnable recurringExecutable = () -> {
                            try {
                                fileOutputStream.write(dataBytes);
                                fileOutputStream.write("\n".getBytes());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        };
                        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(Integer.parseInt(timeToExecute[0].replace("/", "")));
                        scheduler.scheduleWithFixedDelay(() -> executorService.execute(recurringExecutable), 0, localDateTime.getMinute(), TimeUnit.SECONDS);
                    } else {
                        System.out.println("Invalid command format.");
                    }
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    String[] fixedTimePart = command.split("&&");
                    String[] cronExpressionParts = fixedTimePart[0].split(" ");
                    byte[] dataBytes = command.split("&&")[1].replace("echo", "").getBytes();
                    Runnable oneTimeExecutable = () -> {
                        try {
                            fileOutputStream.write(dataBytes);
                            fileOutputStream.write("\n".getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    };
                    String month = cronExpressionParts[3].length() > 1 ? cronExpressionParts[3] : "0" + cronExpressionParts[3];
                    String day = cronExpressionParts[2].length() > 1 ? cronExpressionParts[2] : "0" + cronExpressionParts[2];
                    LocalDateTime date = LocalDateTime.parse(cronExpressionParts[4] + "-" + month + "-" + day + " " + cronExpressionParts[1] + ":" + cronExpressionParts[0], formatter);
                    scheduler.schedule(() -> executorService.execute(oneTimeExecutable), date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
                }
            }
        } finally {
            scheduler.shutdown();
            executorService.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
