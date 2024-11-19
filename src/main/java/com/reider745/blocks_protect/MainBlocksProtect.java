package com.reider745.blocks_protect;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import com.reider745.blocks_protect.api.LevelDB;
import com.reider745.blocks_protect.api.description.BaseBlockInfo;
import com.reider745.blocks_protect.commands.InspectCommand;
import com.reider745.blocks_protect.commands.InspectRadiusCommand;
import com.reider745.blocks_protect.events.*;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class MainBlocksProtect extends PluginBase {
    private final ConcurrentHashMap<Integer, LevelDB> levels = new ConcurrentHashMap<>();
    private final String bdDirectory;
    private final ConcurrentHashMap<Player, Boolean> inspectEnabled = new ConcurrentHashMap<>();

    public MainBlocksProtect(){
        final File dir = new File("blocks_protect");
        dir.mkdir();
        bdDirectory = dir.getAbsolutePath();
    }

    public void registerEvents(Listener listener) {
        final Server server = getServer();
        try {
            server.getPluginManager().registerEvents(listener, this);
        }catch (Throwable e) {
            server.getLogger().warning("Error loaded, " + listener.getClass());
        }
    }

    @Override
    public void onEnable() {
        registerEvents(new BreakBlockEvent(this));
        registerEvents(new InteractEvent(this));
        registerEvents(new PlaceEvent(this));
        registerEvents(new BlockExplosionEvent(this));
        registerEvents(new EntityExplosionEvent(this));

        final SimpleCommandMap map = Server.getInstance().getCommandMap();

        map.register(getName(), new InspectCommand("inspect", this));
        map.register(getName(), new InspectRadiusCommand("inspectr", this));
    }

    @Override
    public void onDisable() {
        for(LevelDB level : levels.values())
            level.unload();
        inspectEnabled.clear();
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

    public boolean setChangedInspect(Player player) {
        final boolean current = inspectEnabled.getOrDefault(player, false);
        inspectEnabled.put(player, !current);
        return !current;
    }

    public boolean isInspectEnabled(Player player) {
        return inspectEnabled.getOrDefault(player, false);
    }

    public boolean message(LevelDB levelDB, CommandSender sender, int x, int y, int z) {
        final BaseBlockInfo[] infos = levelDB.getInteractions(x, y, z);
        if(infos.length > 0) {
            sender.sendMessage("§e=====" + x + " " + y + " " + z + "=====");
            for (BaseBlockInfo info : infos)
                sender.sendMessage(info.toMessage());
            return true;
        }

        return false;
    }

    public boolean message(LevelDB levelDB, CommandSender sender, Position position) {
        return message(levelDB, sender, position.getFloorX(), position.getFloorY(), position.getFloorZ());
    }
}
