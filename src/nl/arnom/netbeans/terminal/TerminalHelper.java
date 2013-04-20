/*
 * Copyright (c) 2013, Arno Moonen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package nl.arnom.netbeans.terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.openide.util.NbPreferences;

/**
 * This class contains most of the important logic used by this
 * module.
 *
 * @author Arno Moonen <info@arnom.nl>
 */
public class TerminalHelper {

    private static OperatingSystem systemOs;

    /**
     * Find out which operating system is being used.
     *
     * @return Current operating system
     */
    public static OperatingSystem getOperatingSystem() {
        if (systemOs != null) {
            return systemOs;
        }

        String name = System.getProperty("os.name");

        // Windows
        if (name.contains("Windows")) {
            systemOs = OperatingSystem.WINDOWS;
            return systemOs;
        }

        // Mac OS X
        if (name.contains("Mac OS")) {
            systemOs = OperatingSystem.MAC_OSX;
            return systemOs;
        }

        // Linux
        if (name.contains("Linux")) {
            systemOs = OperatingSystem.LINUX;
            return systemOs;
        }

        // Other
        systemOs = OperatingSystem.UNKNOWN;
        return systemOs;
    }

    /**
     * Get the default "open a terminal window" command for the given
     * operating system.
     *
     * @param system
     * @return "Open a terminal window" command
     */
    public static String getDefaultCommand(OperatingSystem system) {

        // Windows
        if (system == OperatingSystem.WINDOWS) {
            return "cmd /K \"cd /d \\\"{folder}\\\"\"";
        }

        // Mac OS X
        if (system == OperatingSystem.MAC_OSX) {
            return "/usr/bin/open -a Terminal";
        }

        return "";
    }

    /**
     * Get the default "open a terminal window" command for the
     * current operating system.
     *
     * @return "Open a terminal window" command
     */
    public static String getDefaultCommand() {
        return getDefaultCommand(getOperatingSystem());
    }

    /**
     * Get the command configured by the user or the default one if
     * the user hasn't configured one yet.
     *
     * @return "Open a terminal window" command
     */
    public static String getConfiguredCommand() {
        return NbPreferences.forModule(TerminalHelper.class).get("terminalCommand", TerminalHelper.getDefaultCommand());
    }

    /**
     * Set the command that should be used to open a terminal window.
     *
     * @param command "Open a terminal window" command
     */
    public static void setConfiguredCommand(String command) {
        command = command.trim();
        String defaultCommand = TerminalHelper.getDefaultCommand();

        if (command.isEmpty() || command.equals(defaultCommand)) {
            // It's the default, so we don't have to save anything.
            NbPreferences.forModule(TerminalHelper.class).remove("terminalCommand");
        } else {
            NbPreferences.forModule(TerminalHelper.class).put("terminalCommand", command);
        }
    }

    /**
     * Open a new terminal that starts in the given folder
     *
     * @param folder Folder to start in
     * @throws RuntimeException Thrown when the command is empty.
     * @throws IOException Thrown when something goes wrong when
     * executing the command.
     */
    public static void openTerminal(String folder) throws RuntimeException, IOException {
        if (folder.isEmpty()) {
            // Nothing to do
            return;
        }

        // Command
        String command = getConfiguredCommand();
        if (command.isEmpty()) {
            throw new RuntimeException("No command defined for opening a new terminal window");
        }

        // Escape whitespace
        OperatingSystem os = getOperatingSystem();
        if (os == OperatingSystem.MAC_OSX || os == OperatingSystem.LINUX) {
            folder = folder.replaceAll(" ", "\\\\ ");
        } else if (os == OperatingSystem.WINDOWS) {
            folder = folder.replaceAll(" ", "^ ");
        }

        if (command.contains("{folder}")) {
            // Inject path to folder
            command = command.replaceAll("\\{folder}", folder);
        } else {
            // Append folder
            command += ' ' + folder;
        }

        // Execute command
        Runtime.getRuntime().exec(command);
    }
}