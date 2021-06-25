package pl.pijok.titles.title;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.mattstudios.mfgui.gui.components.ItemBuilder;
import me.mattstudios.mfgui.gui.guis.Gui;
import me.mattstudios.mfgui.gui.guis.GuiItem;
import me.mattstudios.mfgui.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import pl.pijok.titles.Titles;
import pl.pijok.titles.essentials.ChatUtils;
import pl.pijok.titles.essentials.ConfigUtils;
import pl.pijok.titles.essentials.Debug;
import pl.pijok.titles.owner.Owner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TitleController {

    private HashMap<String, Title> availableTitles;
    private Gui mainGui;

    public void load(){
        availableTitles = new HashMap<>();

        YamlConfiguration configuration = ConfigUtils.load("titles.yml", Titles.getInstance());

        for(String titleName : configuration.getConfigurationSection("titles").getKeys(false)){
            Title title = new Title(
                    titleName,
                    configuration.getString("titles." + titleName + ".prefix"),
                    configuration.getDouble("titles." + titleName + ".price")
            );
            availableTitles.put(titleName, title);
        }
    }

    public void loadGui(){

        mainGui = new Gui(3, "Tytuly");

        mainGui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        mainGui.setItem(11, ItemBuilder.from(Material.PAPER).setName(ChatUtils.fixColor("&a&lTwoje tytuly")).asGuiItem(event -> {

            openTitleSelect((Player) event.getWhoClicked());

        }));

        mainGui.setItem(15, ItemBuilder.from(Material.BOOK).setName(ChatUtils.fixColor("&e&lKup nowe tytuly")).asGuiItem(event -> {

            openShop((Player) event.getWhoClicked());

        }));

        GuiItem fillItem = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName("").asGuiItem();

        mainGui.getFiller().fill(fillItem);

    }

    public void addPlayerTitle(String nickname, String titleName){
        Title title = availableTitles.get(titleName);
        Owner owner = Titles.getOwnerController().getOwner(nickname);
        if(!owner.getUnlockedTitles().contains(title)){
            owner.getUnlockedTitles().add(title);
        }
        else{
            Debug.log("&cPlayer already has title " + titleName + "!");
        }
    }

    public void removePlayerTitle(String nickname, String titleName){
        Title title = availableTitles.get(titleName);
        Owner owner = Titles.getOwnerController().getOwner(nickname);
        if(owner.getUnlockedTitles().contains(title)){
            owner.getUnlockedTitles().remove(title);
        }
        else{
            Debug.log("&cPlayer doesn't have title " + titleName + "!");
        }
    }

    public boolean canPlayerBuyTitle(Player player, String titleName){
        Title title = availableTitles.get(titleName);
        return Titles.getEcon().getBalance(player) >= title.getPrice();
    }

    public void setPlayerPrefix(String nickname, String titleName){
        Title title = availableTitles.get(titleName);

        Owner owner = Titles.getOwnerController().getOwner(nickname);

        String command = "";
        String prefixCommand;
        String prefix = PlaceholderAPI.setBracketPlaceholders(Bukkit.getPlayer("" + nickname), "{luckperms_prefix}");
        String suffixFromPrefix = prefix.substring(prefix.lastIndexOf(" ") + 1 );
        if(!owner.getCurrentTitle().equalsIgnoreCase("none")){
//            prefixCommand = "suffix.10." + availableTitles.get(owner.getCurrentTitle()).getPrefix();
//            command = "lp user " + nickname + " permission unset " + prefixCommand + " server=skyblocknew";
            command = "lp user " + nickname + " meta removesuffix 10 * skyblocknew";

            Debug.log("[Unset command] " + command);
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        prefixCommand = "\"suffix.10." + availableTitles.get(titleName).getPrefix() + suffixFromPrefix + "\"";
        command = "lp user " + nickname + " permission set " + prefixCommand + " server=skyblocknew";
        Debug.log("[Set command] " + command);
        //.getServer().getConsoleSender().sendMessage(command);
        owner.setCurrentTitle(titleName);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void checkTitle(Player player){

        Debug.log("&aSprawdzam tytul gracza " + player.getName());

        Owner owner = Titles.getOwnerController().getOwner(player.getName());
        Title title = availableTitles.get(owner.getCurrentTitle());

        if(!owner.getCurrentTitle().equalsIgnoreCase("none")){



            String prefix = PlaceholderAPI.setBracketPlaceholders(player, "{luckperms_prefix}");
            String colorFromPrefix = prefix.substring(prefix.lastIndexOf(" ") + 1 );

            String suffix = PlaceholderAPI.setBracketPlaceholders(player, "{luckperms_suffix}");
            String colorFromSuffix = suffix.substring(suffix.lastIndexOf(" ") + 1 );

            if( !colorFromPrefix.equalsIgnoreCase(colorFromSuffix) ){
                Debug.log("PrefixColor: " + colorFromPrefix + "PrefixTest");
                Debug.log("SuffixColor: " + colorFromSuffix + "SuffixTest");

                Debug.log("&aSuffix nie jest prawidlowy... Poprawiam");

                String clearSuffixCommand = "lp user " + player.getName() + " meta removesuffix 10 * skyblocknew";

                Debug.log("[Unset command] " + clearSuffixCommand);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), clearSuffixCommand);

                String prefixCommand = "\"suffix.10." + availableTitles.get(title.getName()).getPrefix() + colorFromPrefix + "\"";
                String command = "lp user " + player.getName() + " permission set " + prefixCommand + " server=skyblocknew";
                Debug.log("[Set command] " + command);
                //.getServer().getConsoleSender().sendMessage(command);
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);


            }
            else{
                Debug.log("&aSuffix jest prawidlowy");
            }


        }


    }

    public Title getTitle(String titleName){
        return availableTitles.get(titleName);
    }

    public void openMainGui(Player player){
        mainGui.open(player);
    }

    private void openShop(Player player){
        Owner owner = Titles.getOwnerController().getOwner(player.getName());

        ArrayList<Title> notUnlockedTitles = new ArrayList<>();

        for(String titleName : availableTitles.keySet()){
            Title title = availableTitles.get(titleName);
            if(!owner.getUnlockedTitles().contains(title)){
                notUnlockedTitles.add(title);
            }
        }

        PaginatedGui paginatedGui = new PaginatedGui(6, "Kup nowe tytuly!");

        paginatedGui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        int[] lockedSlots = new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44, 45,46,47,48,49,50,51,52,53};

        GuiItem fillItem = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName("").asGuiItem();

        for(int a : lockedSlots){
            paginatedGui.setItem(a, fillItem);
        }

        HeadDatabaseAPI api = new HeadDatabaseAPI();
        paginatedGui.setItem(50, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE).setName(ChatUtils.fixColor("&a&lNastepna strona")).asGuiItem(event -> {

            if( api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum())) == null ){
                paginatedGui.setItem(49, ItemBuilder.from(Material.WRITABLE_BOOK).setName(ChatUtils.fixColor("&5&lStrona " + paginatedGui.getCurrentPageNum())).asGuiItem());
            }
            else {
                paginatedGui.setItem(49, ItemBuilder.from(api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum()))).setName(ChatUtils.fixColor("&5&lStrona " + paginatedGui.getCurrentPageNum())).asGuiItem());
            }
            paginatedGui.next();
        }));

        if( api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum())) == null ){
            paginatedGui.setItem(49, ItemBuilder.from(Material.WRITABLE_BOOK).setName(ChatUtils.fixColor("&5&lStrona " + paginatedGui.getCurrentPageNum())).asGuiItem());
        }
        else {
            paginatedGui.setItem(49, ItemBuilder.from(api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum()))).setName(ChatUtils.fixColor("&5&lStrona " + paginatedGui.getCurrentPageNum())).asGuiItem());
        }



        paginatedGui.setItem(48, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE).setName(ChatUtils.fixColor("&c&lPoprzednia strona")).asGuiItem(event -> {

            if( api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum())) == null ){
                paginatedGui.setItem(49, ItemBuilder.from(Material.WRITABLE_BOOK).setName(ChatUtils.fixColor("&5&lStrona " + paginatedGui.getCurrentPageNum())).asGuiItem());
            }
            else {
                paginatedGui.setItem(49, ItemBuilder.from(api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum()))).setName(ChatUtils.fixColor("&5&lStrona " + paginatedGui.getCurrentPageNum())).asGuiItem());
            }
            paginatedGui.previous();
        }));

        for(Title title : notUnlockedTitles){
            paginatedGui.addItem(ItemBuilder.from(Material.BOOK).setName(ChatUtils.fixColor(title.getPrefix())).setLore("", ChatUtils.fixColor("&7Cena: &e" + title.getPrice())).asGuiItem(event -> {

                Player target = (Player) event.getWhoClicked();

                if(!canPlayerBuyTitle(target, title.getName())){
                    ChatUtils.sendMessage(target, "&cNie stac cie na ten tytul!");
                    return;
                }

                addPlayerTitle(target.getName(), title.getName());
                ChatUtils.sendMessage(target, "&aOdblokowano tytul " + title.getPrefix());
                Titles.getEcon().withdrawPlayer(target, title.getPrice());
                paginatedGui.close(target);
            }));
        }

        paginatedGui.open(player);
    }

    private void openTitleSelect(Player player){

        PaginatedGui paginatedGui = new PaginatedGui(6, "Kup nowe tytuly!");

        paginatedGui.setDefaultClickAction(event -> {
            event.setCancelled(true);
        });

        int[] lockedSlots = new int[]{0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};

        GuiItem fillItem = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).setName("").asGuiItem();

        for(int a : lockedSlots){
            paginatedGui.setItem(a, fillItem);
        }

        HeadDatabaseAPI api = new HeadDatabaseAPI();
        paginatedGui.setItem(50, ItemBuilder.from(Material.LIME_STAINED_GLASS_PANE).setName(ChatUtils.fixColor("&a&lNastepna strona")).asGuiItem(event -> {
            String pageName = "&5&lStrona " + paginatedGui.getNextPageNum();

            if( api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum())) == null ){
                paginatedGui.setItem(49, ItemBuilder.from(Material.WRITABLE_BOOK).setName(ChatUtils.fixColor("" + pageName)).asGuiItem());
            }
            else {
                paginatedGui.setItem(49, ItemBuilder.from(api.getItemHead("" + (44061 - paginatedGui.getNextPageNum()))).setName(ChatUtils.fixColor("" + pageName)).asGuiItem());
            }
            paginatedGui.next();
        }));


        String pgName = "&5&lStrona " + paginatedGui.getNextPageNum();
        if( api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum())) == null ){
            paginatedGui.setItem(49, ItemBuilder.from(Material.WRITABLE_BOOK).setName(ChatUtils.fixColor("" + pgName)).asGuiItem());
        }
        else {
            paginatedGui.setItem(49, ItemBuilder.from(api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum()))).setName(ChatUtils.fixColor("" + pgName)).asGuiItem());
        }



        paginatedGui.setItem(48, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE).setName(ChatUtils.fixColor("&c&lPoprzednia strona")).asGuiItem(event -> {
            String pageName = "&5&lStrona " + paginatedGui.getPrevPageNum();

            if( api.getItemHead("" + (44061 - paginatedGui.getCurrentPageNum())) == null ){
                paginatedGui.setItem(49, ItemBuilder.from(Material.WRITABLE_BOOK).setName(ChatUtils.fixColor("" + pageName)).asGuiItem());
            }
            else {
                paginatedGui.setItem(49, ItemBuilder.from(api.getItemHead("" + (44061 - paginatedGui.getPrevPageNum()))).setName(ChatUtils.fixColor("" + pageName)).asGuiItem());
            }
            paginatedGui.previous();
        }));

        Owner owner = Titles.getOwnerController().getOwner(player.getName());

        for(Title title : owner.getUnlockedTitles()){
            paginatedGui.addItem(ItemBuilder.from(Material.PAPER).setName(ChatUtils.fixColor(title.getPrefix())).setLore("", ChatUtils.fixColor("&eWcisnij aby ustawic ten prefix")).asGuiItem(event -> {
                setPlayerPrefix(event.getWhoClicked().getName(), title.getName());
                ChatUtils.sendMessage(event.getWhoClicked(), "&aZmieniono prefix!");
                paginatedGui.close(event.getWhoClicked());
            }));
        }

        paginatedGui.open(player);

    }
}
