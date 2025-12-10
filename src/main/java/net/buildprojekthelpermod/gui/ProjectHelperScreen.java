package net.buildprojekthelpermod.gui;

import net.buildprojekthelpermod.data.BuildProject;
import net.buildprojekthelpermod.data.ProjectManager;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.Click;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ProjectHelperScreen extends Screen {

    private final ProjectManager projectManager;

    private BlockGridWidget blockGrid;
    private BuildListWidget buildListWidget;
    private TextFieldWidget searchField;
    private TextFieldWidget amountField;

    // Neues Projekt UI
    private TextFieldWidget newProjectNameField;
    private TextFieldWidget currentProjectNameField;
    private boolean isCreatingProject = false;

    private Block blockToEdit;
    private ItemStack draggedStack = ItemStack.EMPTY;
    private boolean isDragging = false;
    private Block draggedBlock = null;

    private Map<Block, Integer> requiredBlocksMap = new HashMap<>();

    public ProjectHelperScreen() {
        super(Text.literal("Build Projekt Helper"));
        this.projectManager = ProjectManager.getInstance();
        loadCurrentProjectData();
    }

    private void loadCurrentProjectData() {
        BuildProject current = projectManager.getCurrentProject();
        requiredBlocksMap.clear();
        if (current != null) {
            requiredBlocksMap.putAll(current.getBlockMap());
        }
    }

    private void saveCurrentProjectData() {
        BuildProject current = projectManager.getCurrentProject();
        if (current != null) {
            current.setBlockMap(this.requiredBlocksMap);
            projectManager.save();
        }
    }

    private void reloadScreenData() {
        this.clearChildren();
        loadCurrentProjectData();
        this.init();
    }

    @Override
    protected void init() {
        int margin = 10;
        int topBarHeight = 30;
        int leftWidth = this.width / 3;
        int leftHeight = this.height - (margin * 3 + 20 + topBarHeight);
        int centerX = this.width / 2;

        BuildProject curProj = projectManager.getCurrentProject();

        // --- BUTTONS ---
        String prevName = projectManager.getPreviousProjectName();
        String nextName = projectManager.getNextProjectName();

        if (prevName.length() > 8) prevName = prevName.substring(0, 8) + "..";
        if (nextName.length() > 8) nextName = nextName.substring(0, 8) + "..";

        // Button "<"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> {
            saveCurrentProjectData();
            projectManager.previousProject();
            reloadScreenData();
        }).dimensions(centerX - 140, 5, 40, 20).build());

        // Button ">"
        this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> {
            saveCurrentProjectData();
            projectManager.nextProject();
            reloadScreenData();
        }).dimensions(centerX + 100, 5, 40, 20).build());

        // Button "+"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            isCreatingProject = true;

            currentProjectNameField.setVisible(false);
            newProjectNameField.setVisible(true);
            newProjectNameField.setText("");
            newProjectNameField.setEditable(true);
            newProjectNameField.setFocused(true);

            this.setFocused(newProjectNameField);
        }).dimensions(centerX + 145, 5, 20, 20).build());

        // Button "X"
        ButtonWidget deleteButton = ButtonWidget.builder(Text.literal("X"), b -> {
            if (isShiftPressed()) {
                projectManager.deleteCurrentProject();
                reloadScreenData();
            } else {
                if (this.client != null && this.client.player != null) {
                    this.client.player.sendMessage(Text.literal("§cHold shift!"), true);
                }
            }
        }).dimensions(centerX + 170, 5, 20, 20).build();

        if (curProj == null) {
            deleteButton.active = false;
        }
        this.addDrawableChild(deleteButton);

        BuildProject current = projectManager.getCurrentProject();
        String status = projectManager.getProjectStatus();
        String title = (current != null) ? current.getName() + " (" + status + ")" : "No Project";

        currentProjectNameField = new TextFieldWidget(
                textRenderer,
                centerX - 90,
                5,
                180,
                20,
                Text.literal("Project name")
        );
        currentProjectNameField.setText(title);
        currentProjectNameField.setMaxLength(30);
        currentProjectNameField.setEditable(false);
        this.addDrawableChild(currentProjectNameField);

        newProjectNameField = new TextFieldWidget(textRenderer, centerX - 50, 5, 100, 20, Text.literal("Name"));
        newProjectNameField.setVisible(false);
        this.addDrawableChild(newProjectNameField);

        // --- GRID ---
        blockGrid = new BlockGridWidget(
                this.client,
                margin,
                margin + 30 + topBarHeight,
                leftWidth,
                leftHeight,
                this::startBlockDrag
        );
        this.addDrawableChild(blockGrid);

        // --- SEARCH ---
        searchField = new TextFieldWidget(
                this.client.textRenderer,
                margin,
                margin + topBarHeight,
                leftWidth,
                20,
                Text.literal("Search")
        );
        searchField.setChangedListener(this::onSearchTextChanges);
        this.addDrawableChild(searchField);

        // --- LIST ---
        int rightPanelX = margin + leftWidth + margin;
        int rightPanelY = margin + topBarHeight;
        int rightPanelWidth = this.width - rightPanelX - margin;
        int rightPanelHeight = this.height - margin * 2 - topBarHeight;

        buildListWidget = new BuildListWidget(
                this.client,
                rightPanelX,
                rightPanelY,
                rightPanelWidth,
                rightPanelHeight,
                requiredBlocksMap,
                this::openAmountInput
        );
        this.addDrawableChild(buildListWidget);

        // --- AMOUNT ---
        amountField = new TextFieldWidget(
                this.client.textRenderer,
                0, 0, 50, 20, Text.literal("Anzahl")
        );
        amountField.setVisible(false);
        amountField.setMaxLength(5);
        amountField.setChangedListener(this::onAmountFieldChange);
        this.addDrawableChild(amountField);
    }

    @Override
    public void close() {
        saveCurrentProjectData();
        super.close();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (button == 1) {
            System.out.println("Rechtsklick erkannt bei: " + mouseX + ", " + mouseY);
        }


        if (button == 1){
            if (buildListWidget.isMouseOver(mouseX, mouseY)) {
                Block targetBlock = buildListWidget.getBlockAt(mouseX, mouseY);

                if (targetBlock != null && projectManager.getCurrentProject() != null) {
                    projectManager.getCurrentProject().togglePin(targetBlock);
                    client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    saveCurrentProjectData();
                    return true;
                }
            }
        }
        if (super.mouseClicked(click, doubled)) {
            return true;
        }

        if (amountField.isVisible() && !amountField.isMouseOver(mouseX, mouseY)) {
            amountField.setVisible(false);
            saveCurrentProjectData();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (isDragging && button == 0) {
            if (buildListWidget.isMouseOver(mouseX, mouseY)) {
                if (draggedBlock != null) {
                    requiredBlocksMap.putIfAbsent(draggedBlock, 1);
                    saveCurrentProjectData();

                    int currentAmount = requiredBlocksMap.get(draggedBlock);
                    openAmountInput(draggedBlock, (int)mouseX, (int)mouseY, currentAmount);
                }
            }
            this.isDragging = false;
            this.draggedBlock = null;
            this.draggedStack = ItemStack.EMPTY;
            return true;
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        int button = click.button();
        if (isDragging && button == 0) {
            boolean isPressed = GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            if (!isPressed) {
                return this.mouseReleased(click);
            }
        }
        if (isDragging) return true;
        return super.mouseDragged(click, offsetX, offsetY);
    }
    private boolean isShiftPressed() {
        long handle = this.client.getWindow().getHandle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();

        if (isCreatingProject && newProjectNameField.isVisible()) {
            if (newProjectNameField.keyPressed(input)) return true;

            // ENTER gedrückt
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                String name = newProjectNameField.getText();
                if (!name.isEmpty()) {
                    saveCurrentProjectData();
                    projectManager.createNewProject(name);

                    // Reset
                    isCreatingProject = false;
                    newProjectNameField.setVisible(false);

                    // WICHTIG: Das alte Feld wieder zeigen!
                    currentProjectNameField.setVisible(true);

                    reloadScreenData();
                }
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                isCreatingProject = false;
                newProjectNameField.setVisible(false);
                return true;
            }
            return false;
        }

        if (amountField.isVisible() && amountField.keyPressed(input)) return true;
        if (searchField.keyPressed(input)) return true;

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (amountField.isVisible()) {
                amountField.setVisible(false);
                amountField.setFocused(false);
                this.setFocused(null);
                saveCurrentProjectData();
                return true;
            }
        }


        //Entferne der Blöcke aus der Liste
        if (keyCode == GLFW.GLFW_KEY_Q) {
            // Mausposition holen und skalieren
            double mouseX = client.mouse.getX() * ((double)this.width / client.getWindow().getFramebufferWidth());
            double mouseY = client.mouse.getY() * ((double)this.height / client.getWindow().getFramebufferHeight());


            Block targetBlock = buildListWidget.getBlockAt(mouseX, mouseY);

            if (targetBlock != null) {
                // Löschen!
                requiredBlocksMap.remove(targetBlock);


                client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.master(net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP, 1.0F));

                // Speichern und Ansicht aktualisieren
                saveCurrentProjectData();
                this.init();

                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {


        if (isCreatingProject && newProjectNameField.isVisible()) {
            return newProjectNameField.charTyped(input);
        }
        if (amountField.isVisible() && amountField.charTyped(input)) return true;
        if (searchField.charTyped(input)) return true;
        return super.charTyped(input);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);

        if (!isCreatingProject) {
            BuildProject current = projectManager.getCurrentProject();

            // Status sicher abrufen
            String status = projectManager.getProjectStatus();
            String title = (current != null) ? current.getName() : "Kein Projekt";
            context.drawCenteredTextWithShadow(textRenderer, title + " (" + status + ")", this.width / 2, 10, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, "Neuer Projektname:", this.width / 2, -5, 0xFFAAAA);
        }

        super.render(context, mouseX, mouseY, tickDelta);

        if (isDragging && !draggedStack.isEmpty()) {
            context.drawItem(draggedStack, mouseX - 8, mouseY - 8);
        }
    }

    private void startBlockDrag(Block block) {
        this.draggedBlock = block;
        this.isDragging = true;
        this.draggedStack = new ItemStack(block);
    }

    private void openAmountInput(Block block, int x, int y, int amount) {
        this.blockToEdit = block;
        amountField.setX(x - 20);
        amountField.setY(y - 10);
        amountField.setVisible(true);
        amountField.setEditable(true);
        amountField.setText(String.valueOf(amount));
        amountField.setFocused(true);
        this.setFocused(amountField);
        amountField.setSelectionStart(0);
        amountField.setSelectionEnd(amountField.getText().length());
    }

    private void onAmountFieldChange(String txt) {
        if (blockToEdit == null) return;
        if (txt.isEmpty()) return;

        try {
            int value = Integer.parseInt(txt);
            requiredBlocksMap.put(blockToEdit, value);
            saveCurrentProjectData();
        } catch (NumberFormatException e) {
        }
    }

    private void onSearchTextChanges(String query) {
        if (blockGrid != null) blockGrid.applyFilter(query);
    }
    @Override public boolean shouldCloseOnEsc() { return !isCreatingProject; }
}