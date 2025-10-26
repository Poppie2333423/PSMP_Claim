package de.popcornsmp.claim;

import de.popcornsmp.claim.menu.MenuHandler;
import de.popcornsmp.claim.menu.MenuListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class PopcornSMPPlugin extends JavaPlugin {
    private ChunkManager chunkManager;
    private MenuHandler menuHandler;

    @Override
    public void onEnable() {
        ClaimStorage storage = new ClaimStorage(this);
        chunkManager = new ChunkManager(storage);
        ChunkHighlighter highlighter = new ChunkHighlighter(this);
        menuHandler = new MenuHandler(this, chunkManager, highlighter);

        PluginCommand command = getCommand("chunk");
        if (command != null) {
            ChunkCommand chunkCommand = new ChunkCommand(chunkManager, menuHandler);
            command.setExecutor(chunkCommand);
            command.setTabCompleter(chunkCommand);
        } else {
            getLogger().severe("Der /chunk Befehl konnte nicht registriert werden.");
        }

        getServer().getPluginManager().registerEvents(new MenuListener(this, menuHandler, chunkManager), this);
        getServer().getPluginManager().registerEvents(new ChunkProtectionListener(chunkManager), this);

        getLogger().info("PopcornSMP Claim Plugin aktiviert.");
    }

    @Override
    public void onDisable() {
        if (chunkManager != null) {
            chunkManager.save();
        }
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public MenuHandler getMenuHandler() {
        return menuHandler;
    }
}
