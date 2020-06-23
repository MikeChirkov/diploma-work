package ru.ertelecom.shellexecute;

import ru.ertelecom.logger.Logger;
import ru.ertelecom.logger.LoggerEventType;
import ru.ertelecom.logger.LoggerManager;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Работа с Shell. Не зависит от операционной системы.
 */
public class Shell {

    private static Logger logger = LoggerManager.getLogger(Shell.class);

    private final boolean isWindows;
    Process shellProcess;
    OutputStream stdin;
    OutputStreamWriter stdinw;
    BufferedWriter stdinbw;

    public Shell() {
        this(System.getProperty("user.home"));
    }

    public Shell(String directory) {
        isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe");
        } else {
            builder.command("sh");
        }

        builder.directory(new File(directory));

        try {
            shellProcess = builder.start();
        } catch (IOException e) {
            logger.log(LoggerEventType.ERROR,
                    "Не удалось запустить процесс.");
            e.printStackTrace();
        }

        stdin = shellProcess.getOutputStream();
        stdinw = new OutputStreamWriter(stdin);
        stdinbw = new BufferedWriter(stdinw);
    }


    public String readOutput() throws Exception {
        String output = "";

        InputStream is = this.shellProcess.getInputStream();
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("cp1251"));
        BufferedReader br = new BufferedReader(isr);

        String line;
        while ((line = br.readLine()) != null) {
            output += line + "\n";
        }

        return output;
    }

    public String executeCommand(String command) throws Exception {
        this.stdinbw.write(command);
        this.stdinbw.write("\n");

        this.stdinbw.write("exit;\n");
        this.stdinbw.flush();
        String output = readOutput();

        this.shellProcess.waitFor();
        return output;
    }

    public void exit() {
        shellProcess.destroy();
    }
}
