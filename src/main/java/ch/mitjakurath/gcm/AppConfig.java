package ch.mitjakurath.gcm;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppConfig {

    private List<Profile> profiles;
    private List<Rule> rules;

    public AppConfig() {
        this.profiles = new ArrayList<>();
        this.rules = new ArrayList<>();
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public static class Profile {
        private String name;
        private String userName;
        private String userEmail;

        @JsonCreator
        public Profile(
                @JsonProperty("name") String name,
                @JsonProperty("userName") String userName,
                @JsonProperty("userEmail") String userEmail
        ) {
            this.name = name;
            this.userName = userName;
            this.userEmail = userEmail;
        }

        public String getName() {
            return name;
        }

        public String getUserName() {
            return userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        @Override
        public String toString() {
            return "Profile{" +
                    "name='" + name + '\'' +
                    ", userName='" + userName + '\'' +
                    ", userEmail='" + userEmail + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Profile profile = (Profile) o;
            return Objects.equals(name, profile.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    public static class Rule {
        private String profileName;
        private String directoryPath;

        @JsonCreator
        public Rule(
                @JsonProperty("profileName") String profileName,
                @JsonProperty("directoryPath") String directoryPath
        ) {
            this.profileName = profileName;
            this.directoryPath = directoryPath;
        }

        public String getProfileName() {
            return profileName;
        }

        public String getDirectoryPath() {
            return directoryPath;
        }

        @Override
        public String toString() {
            return "Rule{" +
                    "profileName='" + profileName + '\'' +
                    ", directoryPath='" + directoryPath + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Rule rule = (Rule) o;
            return Objects.equals(profileName, rule.profileName) &&
                    Objects.equals(directoryPath, rule.directoryPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(profileName, directoryPath);
        }
    }
}
