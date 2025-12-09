package net.buildprojekthelpermod.data;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class BuildProject {
    private String name;

    private Map<String, Integer> requiredBlocks = new HashMap<>();

    public BuildProject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Wandelt Strings zurück in echte Blöcke für die GUI
    public Map<Block, Integer> getBlockMap() {
        Map<Block, Integer> map = new HashMap<>();
        for (Map.Entry<String, Integer> entry : requiredBlocks.entrySet()) {
            Block block = Registries.BLOCK.get(Identifier.tryParse(entry.getKey()));
            map.put(block, entry.getValue());
        }
        return map;
    }

    // Speichert echte Blöcke als Strings
    public void setBlockMap(Map<Block, Integer> map) {
        this.requiredBlocks.clear();
        for (Map.Entry<Block, Integer> entry : map.entrySet()) {
            String id = Registries.BLOCK.getId(entry.getKey()).toString();
            this.requiredBlocks.put(id, entry.getValue());
        }
    }

    // Hilfsmethode zum direkten Hinzufügen
    public void addBlock(Block block, int amount) {
        String id = Registries.BLOCK.getId(block).toString();
        this.requiredBlocks.put(id, amount);
    }
}