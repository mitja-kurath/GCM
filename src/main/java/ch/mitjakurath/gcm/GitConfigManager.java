package ch.mitjakurath.gcm;

import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "gcm",
        mixinStandardHelpOptions = true,
        version = "gcm 1.0",
        description = "Manages multiple Git configurations."
)
public class GitConfigManager implements Callable<Integer> {

    private final ConfigManager configManager = new ConfigManager();

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GitConfigManager()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.println("Use 'gcm --help' to see available commands.");
        return 0;
    }

    @CommandLine.Command(name = "add-profile", description = "Adds a new Git profile.")
    public int addProfile(
            @CommandLine.Parameters(index = "0", description = "Name for the profile (e.g., school, work).") String name,
            @CommandLine.Option(names = {"-n", "--name"}, required = true, description = "Git user name.") String userName,
            @CommandLine.Option(names = {"-e", "--email"}, required = true, description = "Git user email.") String userEmail
    ) {
        try {
            AppConfig config = configManager.loadConfig();
            Optional<AppConfig.Profile> existing = config.getProfiles().stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst();

            if (existing.isPresent()) {
                System.err.println("Error: Profile '" + name + "' already exists.");
                return 1;
            }

            AppConfig.Profile newProfile = new AppConfig.Profile(name, userName, userEmail);
            config.getProfiles().add(newProfile);
            configManager.saveConfig(config);
            System.out.println("Profile '" + name + "' added successfully.");
            return 0;
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
            return 1;
        }
    }

    @CommandLine.Command(name = "remove-profile", description = "Removes a Git profile and its associated rules.")
    public int removeProfile(
            @CommandLine.Parameters(index = "0", description = "Name of the profile to remove.") String name
    ) {
        try {
            AppConfig config = configManager.loadConfig();
            boolean removedProfile = config.getProfiles().removeIf(p -> p.getName().equals(name));

            if (!removedProfile) {
                System.err.println("Error: Profile '" + name + "' not found.");
                return 1;
            }

            boolean removedRules = config.getRules().removeIf(r -> r.getProfileName().equals(name));

            configManager.saveConfig(config);
            System.out.println("Profile '" + name + "' removed successfully.");
            if (removedRules) {
                System.out.println("Associated rules were also removed.");
            }
            System.out.println("Remember to run 'git-cfg-mgr apply-config' and clean up your ~/.gitconfig manually if needed.");
            return 0;
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
            return 1;
        }
    }

    @CommandLine.Command(name = "list-profiles", description = "Lists all stored Git profiles.")
    public int listProfiles() {
        try {
            AppConfig config = configManager.loadConfig();
            List<AppConfig.Profile> profiles = config.getProfiles();
            if (profiles.isEmpty()) {
                System.out.println("No profiles found.");
            } else {
                System.out.println("Profiles:");
                profiles.forEach(p ->
                        System.out.println(
                                "- " + p.getName() + ": " + p.getUserName() + " <" +
                                        p.getUserEmail() + ">"
                        )
                );
            }
            return 0;
        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
            return 1;
        }
    }

    @CommandLine.Command(name = "add-rule", description = "Adds a rule linking a directory to a profile.")
    public int addRule(
            @CommandLine.Parameters(index = "0", description = "Name of the profile for this rule.") String profileName,
            @CommandLine.Parameters(
                    index = "1",
                    description = "Directory path (use ~ for home, e.g., ~/work/projects)."
            ) String directoryPath
    ) {
        try {
            AppConfig config = configManager.loadConfig();
            boolean profileExists = config.getProfiles().stream()
                    .anyMatch(p -> p.getName().equals(profileName));

            if (!profileExists) {
                System.err.println(
                        "Error: Profile '" + profileName + "' not found. Add it first."
                );
                return 1;
            }

            boolean ruleExists = config.getRules().stream()
                    .anyMatch(r ->
                            r.getProfileName().equals(profileName) &&
                                    r.getDirectoryPath().equals(directoryPath)
                    );
            if (ruleExists) {
                System.out.println(
                        "Rule for profile '" + profileName + "' and path '" +
                                directoryPath + "' already exists."
                );
                return 0;
            }

            AppConfig.Rule newRule = new AppConfig.Rule(profileName, directoryPath);
            config.getRules().add(newRule);
            configManager.saveConfig(config);
            System.out.println(
                    "Rule added: Use profile '" + profileName + "' for directory '" +
                            directoryPath + "'"
            );
            System.out.println(
                    "Remember to run 'git-cfg-mgr apply-config' afterwards."
            );
            return 0;
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
            return 1;
        }
    }

    @CommandLine.Command(name = "remove-rule", description = "Removes a specific rule.")
    public int removeRule(
            @CommandLine.Parameters(index = "0", description = "Name of the profile in the rule.") String profileName,
            @CommandLine.Parameters(
                    index = "1",
                    description = "Directory path in the rule (must match exactly)."
            ) String directoryPath
    ) {
        try {
            AppConfig config = configManager.loadConfig();
            boolean removed = config.getRules().removeIf(r ->
                    r.getProfileName().equals(profileName) &&
                            r.getDirectoryPath().equals(directoryPath)
            );

            if (!removed) {
                System.err.println(
                        "Error: Rule for profile '" + profileName + "' and path '" +
                                directoryPath + "' not found."
                );
                return 1;
            }

            configManager.saveConfig(config);
            System.out.println(
                    "Rule removed: Profile '" + profileName + "' for directory '" +
                            directoryPath + "'"
            );
            System.out.println("Remember to run 'git-cfg-mgr apply-config' and clean up your ~/.gitconfig manually if needed.");
            return 0;
        } catch (IOException e) {
            System.err.println("Error saving config: " + e.getMessage());
            return 1;
        }
    }
}