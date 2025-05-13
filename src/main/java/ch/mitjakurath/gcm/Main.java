package ch.mitjakurath.gcm;

import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new GitConfigManager()).execute(args);
        System.exit(exitCode);
    }
}
