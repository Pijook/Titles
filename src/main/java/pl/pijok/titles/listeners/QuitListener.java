package pl.pijok.titles.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.pijok.titles.Titles;

public class QuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event){

        Player player = event.getPlayer();

        Titles.getOwnerController().savePlayer(player.getName());

    }
}
