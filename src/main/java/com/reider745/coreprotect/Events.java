package com.reider745.coreprotect;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Position;
import cn.nukkit.math.BlockFace;
import com.reider745.coreprotect.api.LevelDB;
import com.reider745.coreprotect.api.PlayerInteractionType;

import java.util.concurrent.ConcurrentHashMap;

public class Events implements Listener {
    private final ConcurrentHashMap<Player, Integer> blockPreUse = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Player, Integer> blockPlaceUse = new ConcurrentHashMap<>();
    private final MainCoreProtect main;

    public Events(MainCoreProtect main){
        this.main = main;
    }

    public void message(LevelDB levelDB, Player player, Position position) {
        if(!main.message(levelDB, player, position)){
            player.sendMessage("§4Empty list");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreakBlock(BlockBreakEvent event){
        final Player player = event.getPlayer();
        final LevelDB levelDB = main.getLevel(player.level);

        if(!event.isCancelled()) {
            if (main.isLedgerEnabled(player)) {
                message(levelDB, player, event.getBlock());
                event.setCancelled();
            } else {
                levelDB.addInteractionAsync(event.getBlock(), PlayerInteractionType.BREAK, player, 0, 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractBefore(PlayerInteractEvent event){
        final Player player = event.getPlayer();

        if(main.isLedgerEnabled(player)){
            message(main.getLevel(player.level), player, event.getBlock());
            event.setCancelled();
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

        if(!main.isLedgerEnabled(player) && !event.isCancelled()) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event){
        final Player player = event.getPlayer();
        final Block block = event.getBlockReplace();

        if(!event.isCancelled() && !main.isLedgerEnabled(player) && block != null){
            main.getLevel(player.level)
                    .addInteraction(block, PlayerInteractionType.PLACE, player, event.getBlockReplace().getId(), event.getBlock().getId());
        }
    }
}
