package net.buildprojekthelpermod.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProjectManager {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("build_projects.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 1. Die einzige Instanz (Singleton)
    private static ProjectManager INSTANCE;

    // Diese Methode gibt IMMER dasselbe Objekt zurück
    public static ProjectManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ProjectManager();
        }
        return INSTANCE;
    }

    private List<BuildProject> projects = new ArrayList<>();
    private int currentProjectIndex = 0;

    // 2. WICHTIG: Der Konstruktor muss PRIVATE sein!
    // So kann niemand von außen "new ProjectManager()" aufrufen.
    private ProjectManager() {
        load();
        // Falls leer, Standard-Projekt anlegen
        if (projects.isEmpty()) {
            projects.add(new BuildProject("Mein Projekt"));
            save();
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(projects, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (!Files.exists(CONFIG_PATH)) return;

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            projects = GSON.fromJson(reader, new TypeToken<List<BuildProject>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        // Automatisch zum neuen Projekt wechseln
        currentProjectIndex = projects.size() - 1;
        save();
    }

    // 3. Hier war der Fehler: Methode nur EINMAL definieren
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


