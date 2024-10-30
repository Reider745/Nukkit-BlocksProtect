package com.reider745.coreprotect;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Level;
import cn.nukkit.plugin.PluginBase;
import com.reider745.coreprotect.api.BlockInteractionPlayerInfo;
import com.reider745.coreprotect.api.LevelDB;
import com.reider745.coreprotect.api.PlayerInteractionType;
import com.reider745.coreprotect.commands.LedgerCommand;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class MainCoreProtect extends PluginBase implements Listener {
    private final ConcurrentHashMap<Integer, LevelDB> levels = new ConcurrentHashMap<>();
    private final String bdDirectory;
    private final ConcurrentHashMap<Player, Boolean> ledgerEnabled = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Block> blockPreUse = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Block> blockPlaceUse = new ConcurrentHashMap<>();

    public MainCoreProtect(){
        final File dir = new File("coreprotect");
        dir.mkdir();
        bdDirectory = dir.getAbsolutePath();
    }

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(this, this);

        final SimpleCommandMap map = Server.getInstance().getCommandMap();
        map.register(getName(), new LedgerCommand("l", this));
        map.register(getName(), new LedgerCommand("ledger", this));
    }

    @Override
    public void onDisable() {
        for(LevelDB level : levels.values())
            level.unload();
        ledgerEnabled.clear();
    }

    @EventHandler
    public void onLoadLevel(LevelLoadEvent event){
        final Level level = event.getLevel();
        levels.put(level.getId(), new LevelDB(new File(bdDirectory+"/"+level.getName()+".bd")));
    }

    @EventHandler
    public void onUnloadLevel(LevelUnloadEvent event){
        LevelDB levelDB = levels.remove(event.getLevel().getId());
        levelDB.unload();
    }

    public LevelDB getLevel(Level level){
        LevelDB levelDB = levels.get(level.getId());
        if(levelDB == null){
            levelDB = new LevelDB(new File(bdDirectory+"/"+level.getName()+".bd"));
            levels.put(level.getId(), levelDB);
        }
        return levelDB;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakBlock(BlockBreakEvent event){
        final Player player = event.getPlayer();
        final LevelDB levelDB = getLevel(player.level);

        if(ledgerEnabled.getOrDefault(player, false)){
            player.sendMessage("=====LIST=====");
            for(BlockInteractionPlayerInfo info : levelDB.getInteractions(event.getBlock()))
                player.sendMessage(info.toMessage());
            event.setCancelled();
        } else {
            levelDB.addInteractionAsync(event.getBlock(), PlayerInteractionType.BREAK, player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractHigh(PlayerInteractEvent event){
        final Player player = event.getPlayer();
        final LevelDB levelDB = getLevel(player.level);

        if(ledgerEnabled.getOrDefault(player, false)){
            player.sendMessage("=====LIST=====");
            for(BlockInteractionPlayerInfo info : levelDB.getInteractions(event.getBlock()))
                player.sendMessage(info.toMessage());
            event.setCancelled();
        }else{
            blockPreUse.put(player.getDisplayName(), event.getBlock());
            blockPlaceUse.put(player.getDisplayName(), event.getBlock().getSide(event.getFace()));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractLowest(PlayerInteractEvent event){
        final Player player = event.getPlayer();
        final LevelDB levelDB = getLevel(player.level);

        if(!player.level.getBlock(event.getBlock()).equalsBlock(blockPreUse.remove(player.getDisplayName()))){
            levelDB.addInteractionAsync(event.getBlock(), PlayerInteractionType.CHANGE, player);
        }

        if(!player.level.getBlock(event.getBlock()).equalsBlock(blockPlaceUse.remove(player.getDisplayName()))){
            levelDB.addInteractionAsync(event.getBlock(), PlayerInteractionType.CHANGE, player);
        }
    }

    public boolean setChangedLedger(Player player) {
        final boolean current = ledgerEnabled.getOrDefault(player, false);
        ledgerEnabled.put(player, !current);
        return !current;
    }
}
