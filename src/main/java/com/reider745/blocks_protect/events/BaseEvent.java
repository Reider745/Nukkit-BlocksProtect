package com.reider745.blocks_protect.events;

import cn.nukkit.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.level.Position;
import com.reider745.blocks_protect.MainBlocksProtect;
import com.reider745.blocks_protect.api.LevelDB;

class BaseEvent implements Listener {
    protected final MainBlocksProtect main;

    public BaseEvent(MainBlocksProtect main){
        this.main = main;
    }

    public void message(LevelDB levelDB, Player player, Position position) {
        if(!main.message(levelDB, player, position)){
            player.sendMessage("§4Empty list");
        }
    }
}
