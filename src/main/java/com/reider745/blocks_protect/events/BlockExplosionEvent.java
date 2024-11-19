package com.reider745.blocks_protect.events;

import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.BlockExplodeEvent;
import com.reider745.blocks_protect.MainBlocksProtect;
import com.reider745.blocks_protect.api.LevelDB;
import com.reider745.blocks_protect.api.PlayerInteractionType;

import java.util.List;

public class BlockExplosionEvent extends BaseEvent {
    public BlockExplosionEvent(MainBlocksProtect main) {
        super(main);
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        if (!event.isCancelled()) {
            final List<Block> blocks = event.getBlockList();
            final LevelDB levelDB = main.getLevel(event.getPosition().level);

            for (Block block : blocks) {
                levelDB.addInteractionAsync(block, PlayerInteractionType.EXPLOSION_BLOCK, null, block.getId(), 0);
            }
        }
    }
}
