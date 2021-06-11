package pl.pijok.titles.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.pijok.titles.Titles;
import pl.pijok.titles.essentials.ChatUtils;
import pl.pijok.titles.essentials.Debug;
import pl.pijok.titles.owner.Owner;
import pl.pijok.titles.title.Title;

public class TitleCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender){
            Debug.log("&aSender is console!");
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("openmenu")){
                if(sender instanceof Player){
                    Player player = (Player) sender;
                    Titles.getTitleController().openMainGui(player);
                    return true;
                }
            }
        }

        if(args.length == 2){
            if(args[0].equalsIgnoreCase("showTitles")){
                Player target = Bukkit.getPlayer(args[1]);

                if(target == null || !target.isOnline()){
                    Debug.log("&cPlayer is offline!");
                    return true;
                }

                StringBuilder stringBuilder = new StringBuilder("&ePlayers titles: &7");

                Owner owner = Titles.getOwnerController().getOwner(target.getName());

                for(Title title : owner.getUnlockedTitles()){
                    stringBuilder.append(title.getName()).append(", ");
                }
                ChatUtils.sendMessage(sender, stringBuilder.toString());
                return true;
            }

        }

        if(args.length == 3){

            if(args[0].equalsIgnoreCase("unlockTitle")){

                Player target = Bukkit.getPlayer(args[1]);

                if(target == null || !target.isOnline()){
                    ChatUtils.sendMessage(sender, "&cPlayer is offline!");
                    return true;
                }

                Titles.getTitleController().addPlayerTitle(target.getName(), args[2]);
                ChatUtils.sendMessage(sender, "&aDone!");
                return true;
            }

            if(args[0].equalsIgnoreCase("removeTitle")){

                Player target = Bukkit.getPlayer(args[1]);

                if(target == null || !target.isOnline()){
                    ChatUtils.sendMessage(sender, "&cPlayer is offline!");
                    return true;
                }

                Titles.getTitleController().removePlayerTitle(target.getName(), args[2]);
                ChatUtils.sendMessage(sender, "&aDone!");
                return true;
            }

        }

        ChatUtils.sendMessage(sender,"&7/" + label + " unlockTitle <nickname> <title>");
        ChatUtils.sendMessage(sender,"&7/" + label + " removeTitle <nickname> <title>");
        ChatUtils.sendMessage(sender,"&7/" + label + " showTitles <nickname>");
        return true;
    }
}
