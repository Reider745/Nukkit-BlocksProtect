package com.reider745.blocks_protect.events;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.BlockPlaceEvent;
import com.reider745.blocks_protect.MainBlocksProtect;
import com.reider745.blocks_protect.api.PlayerInteractionType;

public class PlaceEvent extends BaseEvent {
    public PlaceEvent(MainBlocksProtect main) {
        super(main);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event){
        final Player player = event.getPlayer();
        final Block block = event.getBlockReplace();

        if(!event.isCancelled() && !main.isInspectEnabled(player) && block != null){
            main.getLevel(player.level)
                    .addInteraction(block, PlayerInteractionType.PLACE, player, event.getBlockReplace().getId(), event.getBlock().getId());
        }
    }
}
