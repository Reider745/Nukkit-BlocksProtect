package com.reider745.coreprotect.api.description;

import cn.nukkit.block.Block;

import java.nio.ByteBuffer;

public class ChangeBlockInfo extends BaseBlockInfo {
    public short beforeBlock, afterBlock;

    public ChangeBlockInfo(ByteBuffer buffer, byte type) {
        super(buffer, type);

        beforeBlock = buffer.getShort();
        afterBlock = buffer.getShort();
    }

    protected String getName(short id) {
        final Block block = Block.get(id);
        if(block != null) return block.getName();
        return ""+id;
    }

    @Override
    public String toMessage() {
        return getName(beforeBlock) + " -> " + getName(afterBlock) + ", " + super.toMessage();
    }

    @Override
    public String toString() {
        return "ChangeBlockInfo{" +
                "afterBlock=" + afterBlock +
                ", time=" + time +
                ", type=" + type +
                ", name='" + name + '\'' +
                ", beforeBlock=" + beforeBlock +
                '}';
    }
}
