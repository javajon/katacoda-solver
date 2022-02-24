package com.katacoda.solver.models;

import io.quarkus.runtime.util.StringUtil;
import picocli.CommandLine;

public enum Status {
    Undetermined('☐', "white"),
    Info('ⓘ', "green"),
    Pass('✅', "green"),
    Warning('⚠', "yellow"),
    Fixed((char)0xD83D, "blue"),
    Error('❌', "red");

    public static int indent = 1;
    private final char icon;
    private final String color;

    Status(char icon, String color) {
        this.icon = icon;
        this.color = color;
    }

    public String message(String message) {
        String indentSpaces = new String(new char[indent]).replace('\0', ' ');

        if (this == Status.Undetermined) {
            return String.format("%-10s%s%s", "", indentSpaces, message);
        }

        return CommandLine.Help.Ansi.AUTO.string(String.format("@|bold,%s %s %-8s%s%s|@", color, icon, name(), indentSpaces, message));
    }
}
