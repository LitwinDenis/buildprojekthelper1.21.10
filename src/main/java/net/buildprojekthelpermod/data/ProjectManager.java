package net.buildprojekthelpermod.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import java.io.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("build_projects.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ProjectManager INSTANCE;


    public static ProjectManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectManager();
        }
        return INSTANCE;
    }

    private List<BuildProject> projects = new ArrayList<>();
    private int currentProjectIndex = 0;

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }


    private File getSaveFile() {
        MinecraftClient client = MinecraftClient.getInstance();
        File configDir = new File(client.runDirectory, "config/buildprojekthelper"); // Ordner für deine Mod
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        String fileName = "default_projects.json"; // Fallback (z.B. Hauptmenü)


        if (client.getCurrentServerEntry() != null) {
            String ip = client.getCurrentServerEntry().address;
            fileName = "server_" + sanitizeFileName(ip) + ".json";
        }

        else if (client.isInSingleplayer()) {
            IntegratedServer server = client.getServer();
            if (server != null) {
                String levelName = server.getSaveProperties().getLevelName();
                fileName = "local_" + sanitizeFileName(levelName) + ".json";
            }
        }

        return new File(configDir, fileName);
    }

    private ProjectManager() {
        load();

        if (projects.isEmpty()) {
            projects.add(new BuildProject("My Project"));
            save();
        }
    }

    public void save() {
        File file = getSaveFile();
    }

    public void load() {
        File file = getSaveFile();

        this.projects.clear();
        this.currentProjectIndex = 0;

        if (!file.exists()) return;
    }

    public BuildProject getCurrentProject() {
        if (projects.isEmpty()) return null;
        if (currentProjectIndex >= projects.size()) currentProjectIndex = 0;
        return projects.get(currentProjectIndex);
    }

    public String getPreviousProjectName() {
        if (projects.isEmpty()) return "";
        int prevIndex = currentProjectIndex - 1;
        if (prevIndex < 0) prevIndex = projects.size() - 1;
        return projects.get(prevIndex).getName();
    }

    public String getNextProjectName() {
        if (projects.isEmpty()) return "";
        int nextIndex = currentProjectIndex + 1;
        if (nextIndex >= projects.size()) nextIndex = 0;
        return projects.get(nextIndex).getName();
    }

    public void createNewProject(String name) {
        BuildProject newProj = new BuildProject(name);
        projects.add(newProj);
        currentProjectIndex = projects.size() - 1;
        save();
    }


    public void nextProject() {
        if (projects.isEmpty()) return;
        currentProjectIndex++;
        if (currentProjectIndex >= projects.size()) currentProjectIndex = 0;
    }

    public void previousProject() {
        if (projects.isEmpty()) return;
        currentProjectIndex--;
        if (currentProjectIndex < 0) currentProjectIndex = projects.size() - 1;
    }
    public String getProjectStatus() {
        if (projects.isEmpty()) return "0/0";
        return (currentProjectIndex + 1) + "/" + projects.size();
    }
    public void deleteCurrentProject(){
        if (projects.isEmpty() || currentProjectIndex < 0 || currentProjectIndex >= projects.size()){
            return;
        }

        projects.remove(currentProjectIndex);

        if (projects.isEmpty()) {
            currentProjectIndex = 0;
        }else if (currentProjectIndex >= projects.size()) {
            currentProjectIndex = projects.size() -1;
        }
        save();
    }
}


