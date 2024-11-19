package com.reider745.blocks_protect.events;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.ItemStick;
import cn.nukkit.math.BlockFace;
import com.reider745.blocks_protect.MainBlocksProtect;
import com.reider745.blocks_protect.api.LevelDB;
import com.reider745.blocks_protect.api.PlayerInteractionType;

import java.util.concurrent.ConcurrentHashMap;

public class InteractEvent extends BaseEvent {
    private final ConcurrentHashMap<Player, Integer> blockPreUse = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, Integer> blockPlaceUse = new ConcurrentHashMap<>();

    public InteractEvent(MainBlocksProtect main) {
        super(main);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractBefore(PlayerInteractEvent event){
        final Player player = event.getPlayer();

        if(main.isInspectEnabled(player)){
            if(event.getItem() instanceof ItemStick) {
                message(main.getLevel(player.level), player, event.getBlock());
                event.setCancelled();
            }
        }else{
            final Block block = event.getBlock();
            if(block != null) {
                blockPreUse.put(player, block.getId());

                final BlockFace face = event.getFace();
                if (face != null) {
                    blockPlaceUse.put(player, block.getSide(face).getId());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteractAfter(PlayerInteractEvent event){
        final Player player = event.getPlayer();

        if(!main.isInspectEnabled(player) && !event.isCancelled()) {
            final LevelDB levelDB = main.getLevel(player.level);
            Block block = event.getBlock();
            if(block != null){
                block = player.level.getBlock(block);

                int beforeId = blockPreUse.remove(player);
                if (block != null && block.getId() != beforeId) {
                    levelDB.addInteractionAsync(block, PlayerInteractionType.CHANGE, player, beforeId, block.getId());
                }

                final BlockFace face = event.getFace();
                if(face != null){
                    block = event.getBlock().getSide(face);
                    if (block != null && block.getId() != blockPlaceUse.remove(player)) {
                        levelDB.addInteractionAsync(block, PlayerInteractionType.CHANGE, player, beforeId, block.getId());
                    }
                }
            }
        }
    }
}
