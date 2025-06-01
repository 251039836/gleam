package gleam.console;

import java.util.Scanner;

/**
 * 控制台工具类<br>
 * 仅用于开发过程的调试
 * 
 * @author hdh
 *
 */
public class ConsoleUtil {

    /**
     * 启动控制台输入监听
     */
    public static void startConsoleListener() {
        Thread consoleThread = new Thread(() -> {
            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    String cmd = sc.next();
                    String response = ConsoleManager.getInstance().handlerConsoleCmd(cmd);
                    System.out.println("exec cmd result : " + response);
                }
            } finally {
            }
        });
        consoleThread.setName("os_console_thread");
        consoleThread.setDaemon(true);
        consoleThread.start();
    }
}
