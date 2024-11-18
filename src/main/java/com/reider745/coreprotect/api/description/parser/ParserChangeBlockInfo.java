package com.reider745.coreprotect.api.description.parser;

import com.reider745.coreprotect.api.description.BaseBlockInfo;
import com.reider745.coreprotect.api.description.ChangeBlockInfo;

import java.nio.ByteBuffer;

public class ParserChangeBlockInfo extends BaseParserBlockInfo {
    @Override
    public int length(String player) {
        // 2(short) - beforeBlock
        // 2(short) - afterBlock
        return super.length(player) + 2 + 2;
    }

    @Override
    public int encode(ByteBuffer buffer, int length, byte type, String player, short beforeBlock, short afterBlock) {
        length = super.encode(buffer, length, type, player, beforeBlock, afterBlock);
        buffer.putShort(length, beforeBlock);
        length += 2;
        buffer.putShort(length, afterBlock);
        length += 2;
        return length;
    }

    @Override
    public BaseBlockInfo decode(ByteBuffer buffer, byte type) {
        return new ChangeBlockInfo(buffer, type);
    }
}
