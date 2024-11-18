package com.reider745.coreprotect;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.SimpleCommandMap;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.level.LevelLoadEvent;
import cn.nukkit.event.level.LevelUnloadEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import com.reider745.coreprotect.api.LevelDB;
import com.reider745.coreprotect.api.PlayerInteractionType;
import com.reider745.coreprotect.api.description.BaseBlockInfo;
import com.reider745.coreprotect.api.description.parser.BaseParserBlockInfo;
import com.reider745.coreprotect.api.description.parser.ParserChangeBlockInfo;
import com.reider745.coreprotect.commands.LedgerBlockListCommand;
import com.reider745.coreprotect.commands.LedgerCommand;
import com.reider745.coreprotect.commands.LedgerRadiusCommand;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class MainCoreProtect extends PluginBase {
    private final ConcurrentHashMap<Integer, LevelDB> levels = new ConcurrentHashMap<>();
    private final String bdDirectory;
    private final ConcurrentHashMap<Player, Boolean> ledgerEnabled = new ConcurrentHashMap<>();

    public MainCoreProtect(){
        final File dir = new File("coreprotect");
        dir.mkdir();
        bdDirectory = dir.getAbsolutePath();
    }

    @Override
    public void onEnable() {
        Server.getInstance().getPluginManager().registerEvents(new Events(this), this);

        final SimpleCommandMap map = Server.getInstance().getCommandMap();

        map.register(getName(), new LedgerCommand("l", this));
        map.register(getName(), new LedgerCommand("ledger", this));
        map.register(getName(), new LedgerRadiusCommand("lr", this));
        map.register(getName(), new LedgerBlockListCommand("test", this));
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

    public boolean setChangedLedger(Player player) {
        final boolean current = ledgerEnabled.getOrDefault(player, false);
        ledgerEnabled.put(player, !current);
        return !current;
    }

    public boolean isLedgerEnabled(Player player) {
        return ledgerEnabled.getOrDefault(player, false);
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
