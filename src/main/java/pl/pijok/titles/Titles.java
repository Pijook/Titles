package pl.pijok.titles;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import pl.pijok.titles.commands.TitleCommand;
import pl.pijok.titles.essentials.Debug;
import pl.pijok.titles.listeners.JoinListener;
import pl.pijok.titles.listeners.QuitListener;
import pl.pijok.titles.owner.OwnerController;
import pl.pijok.titles.title.TitleController;

public class Titles extends JavaPlugin {

    private static Titles instance;
    private static TitleController titleController;
    private static OwnerController ownerController;
    private static Economy econ = null;

    @Override
    public void onEnable() {
        instance = this;

        titleController = new TitleController();
        ownerController = new OwnerController();

        if (!setupEconomy() ) {
            Debug.log(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        getServer().getPluginManager().registerEvents(new QuitListener(), this);

        getCommand("sbtitle").setExecutor(new TitleCommand());

        loadStuff();

    }

    @Override
    public void onDisable() {

    }

    private void loadStuff() {

        Debug.log("&aLoading Titles v1.0 by Pijok_");

        Debug.log("&7Loading titles...");
        titleController.load();

        Debug.log("&7Loading gui...");
        titleController.loadGui();

        Debug.log("&aEverything loaded! Starting!");

    }

    public static Titles getInstance() {
        return instance;
    }

    public static TitleController getTitleController(){
        return titleController;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEcon(){
        return econ;
    }

    public static OwnerController getOwnerController() {
        return ownerController;
    }

}
