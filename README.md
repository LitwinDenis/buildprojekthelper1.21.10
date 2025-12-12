
# 🏗️ Build Project Helper Mod

**Build Project Helper** is a client-side Fabric mod for Minecraft designed to help survival builders keep track of their building projects and required materials. Plan your builds, create material lists, and pin important blocks directly to your HUD\!

 

## ✨ Features

  * **📂 Project Management:** Manage multiple building projects simultaneously (e.g., "Castle", "Warehouse", "Harbor").
  * **📝 Material Lists:** Define which blocks you need and set the target amount.
  * **📌 HUD Overlay (Pinning System):**
      * **Right-click** any block in your project list to "pin" it.
      * Pinned blocks are displayed permanently on your HUD (top-left corner).
      * **Live Tracking:** The HUD shows your current inventory count vs. the required amount in real-time.
      * **Color Feedback:**
          * 🔴 Red: You have none of this item.
          * ⚪ White: You are collecting, but haven't reached the target yet.
          * 🟢 Green: You have enough materials\!
  * **💾 Smart Saving:**
      * Project data is saved separateley per **World** (Singleplayer) or per **Server IP** (Multiplayer).
      * No more mixed-up lists when switching between servers or singleplayer worlds\!
  * **🖱️ Easy to use:** Intuitive GUI with search functionality and visual grid selection.

## 🚀 Installation

1.  Install [Fabric Loader](https://fabricmc.net/).
2.  Download the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) and place it in your `.minecraft/mods` folder.
3.  Download the `.jar` file of this mod and place it in the `mods` folder as well.
4.  Launch Minecraft\!

## 🎮 Controls & Usage

### 1\. Open the GUI

Press the configured key (Default: `P` - *please check your keybind settings*) to open the Project Manager.

### 2\. Create a Project

  * Click the **`+`** button in the top right corner.
  * Enter a name for your project and press `Enter`.

### 3\. Add Blocks

  * Use the search bar or scroll through the block grid on the left.
  * Click on a block to set the required amount.

### 4\. Using the HUD (Pinning) 📌

  * **Right-click** on any block in your active project list (right panel).
  * The block slot will turn **green**, indicating it is pinned.
  * Close the GUI (`ESC`).
  * The block is now tracked live on your screen\!

### 5\. Delete a Project

  * Select the project you want to remove.
  * Click the **`X`** button next to the `+` button.
  * *(Safety feature: Hold `SHIFT` while clicking to confirm deletion).*

## 🛠️ Technical Details

Project data is stored as JSON files located in:
`.minecraft/config/buildprojekthelper/`

Files are automatically named based on the world name or server address:

  * `local_MySurvivalWorld.json`
  * `server_play_example_com.json`

## 📸 Screenshots

*(Screenshots will be add soon)*

## 📜 License

This project is licensed under the MIT License. Feel free to fork and build\!
