package net.buildprojekthelpermod.data;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;

public class BuildProject {
    private String name;

    private Map<String, Integer> requiredBlocks = new HashMap<>();
    private Set<String> pinnedBlockIds = new HashSet<>();

    public BuildProject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public Map<Block, Integer> getBlockMap() {
        Map<Block, Integer> map = new HashMap<>();
        for (Map.Entry<String, Integer> entry : requiredBlocks.entrySet()) {
            Block block = Registries.BLOCK.get(Identifier.tryParse(entry.getKey()));
            map.put(block, entry.getValue());
        }
        return map;
    }


    public void setBlockMap(Map<Block, Integer> map) {
        this.requiredBlocks.clear();
        for (Map.Entry<Block, Integer> entry : map.entrySet()) {
            String id = Registries.BLOCK.getId(entry.getKey()).toString();
            this.requiredBlocks.put(id, entry.getValue());
        }
    }

    public void addBlock(Block block, int amount) {
        String id = Registries.BLOCK.getId(block).toString();
        this.requiredBlocks.put(id, amount);
    }
    public void togglePin(Block block) {
        if (pinnedBlockIds == null) pinnedBlockIds = new HashSet<>();

        String id = Registries.BLOCK.getId(block).toString();
        if (pinnedBlockIds.contains(id)) {
            pinnedBlockIds.remove(id);
        } else {
            pinnedBlockIds.add(id);
        }
    }
    public boolean isPinned(Block block) {
        if (pinnedBlockIds == null) return false;

        String id = Registries.BLOCK.getId(block).toString();
        return pinnedBlockIds.contains(id);
    }
    public Set<Block> getPinnedBlocks() {
        if (pinnedBlockIds == null){
            pinnedBlockIds = new HashSet<>();
        }
        Set<Block> blocks = new HashSet<>();
        for (String id : pinnedBlockIds) {
            if (id == null) continue;

            Identifier identifier = Identifier.tryParse(id);
            if (identifier == null) continue;

            Block block = Registries.BLOCK.get(Identifier.tryParse(id));
            if (block != null) {
                blocks.add(block);
            }
        }
        return blocks;
    }

}