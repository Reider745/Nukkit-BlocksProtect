package com.reider745.blocks_protect.events;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.item.ItemStick;
import com.reider745.blocks_protect.MainBlocksProtect;
import com.reider745.blocks_protect.api.LevelDB;
import com.reider745.blocks_protect.api.PlayerInteractionType;

public class BreakBlockEvent extends BaseEvent {
    public BreakBlockEvent(MainBlocksProtect main) {
        super(main);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreakBlock(BlockBreakEvent event){
        final Player player = event.getPlayer();
        final LevelDB levelDB = main.getLevel(player.level);

        if(!event.isCancelled()) {
            if (main.isInspectEnabled(player)) {
                if(event.getItem() instanceof ItemStick) {
                    message(levelDB, player, event.getBlock());
                    event.setCancelled();
                }
            } else {
                levelDB.addInteractionAsync(event.getBlock(), PlayerInteractionType.BREAK, player, 0, 0);
            }
        }
    }
}
