package ch.mitjakurath.gcm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitConfigApplier {

    private final ConfigManager configManager;
    private static final String PROFILES_DIR_NAME = "profiles";

    public GitConfigApplier(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public void apply(AppConfig config) throws IOException {
        Path profilesDir = configManager.getConfigDir().resolve(PROFILES_DIR_NAME);
        Files.createDirectories(profilesDir);

        System.out.println("Applying Git configuration based on rules...");
        System.out.println("--------------------------------------------------");

        System.out.println("Creating/Updating profile config files in: " + profilesDir);
        for (AppConfig.Profile profile : config.getProfiles()) {
            Path profileConfigFile = profilesDir.resolve(profile.getName() + ".gitconfig");
            try (BufferedWriter writer = Files.newBufferedWriter(profileConfigFile)) {
                writer.write("[user]");
                writer.newLine();
                writer.write("\tname = " + profile.getUserName());
                writer.newLine();
                writer.write("\temail = " + profile.getUserEmail());
                writer.newLine();
            }
            System.out.println("- Written: " + profileConfigFile);
        }

        System.out.println("\n--------------------------------------------------");
        System.out.println("Generated `includeIf` commands for your global .gitconfig:");
        System.out.println("--------------------------------------------------");
        System.out.println("NOTE: You need to run these commands manually.");
        System.out.println("This tool avoids modifying your ~/.gitconfig directly to prevent conflicts.");
        System.out.println("If you change rules, you might need to manually clean up old entries in ~/.gitconfig.");
        System.out.println("--------------------------------------------------");

        Path homeDir = Paths.get(System.getProperty("user.home"));
        String profilesDirPathString = homeDir.relativize(profilesDir).toString().replace("\\", "/");

        for (AppConfig.Rule rule : config.getRules()) {
            AppConfig.Profile profile = config.getProfiles().stream()
                    .filter(p -> p.getName().equals(rule.getProfileName()))
                    .findFirst()
                    .orElse(null);

            if (profile != null) {
                Path ruleDirPath = Paths.get(rule.getDirectoryPath().replaceFirst("^~", System.getProperty("user.home")));
                String ruleDirPathString = homeDir.relativize(ruleDirPath).toString().replace("\\", "/");

                String gitdirCondition = ruleDirPathString + "/";

                String profileIncludePath = Paths.get(profilesDirPathString, profile.getName() + ".gitconfig")
                        .toString().replace("\\", "/");

                System.out.println("git config --global --add includeIf.\"gitdir:" + gitdirCondition + "\".path ~/" + profileIncludePath);
            } else {
                System.err.println("Warning: Rule found for unknown profile: " + rule.getProfileName());
            }
        }
        System.out.println("\n--------------------------------------------------");
        System.out.println("Please run the commands printed above to update your ~/.gitconfig.");
    }
}