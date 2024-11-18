package com.reider745.coreprotect.api.description.parser;

import com.reider745.coreprotect.api.description.BaseBlockInfo;

import java.nio.ByteBuffer;

public class BaseParserBlockInfo {
    public int length(String player){
        // 1(byte) - PlayerInteractionType
        // 8(long) - time
        // 1(byte) - string length
        // player.length - string bytes
        return 1 + 8 + 1 + player.length();
    }

    public int encode(ByteBuffer buffer, int length, byte type, String player, short beforeBlock, short afterBlock) {
        buffer.put(length, type);
        length += 1;

        buffer.putLong(length, System.currentTimeMillis());
        length += 8;

        buffer.put(length, (byte) (player.length() + Byte.MIN_VALUE));
        length += 1;

        for(byte symbol : player.getBytes()){
            buffer.put(length, symbol);
            length += 1;
        }

        return length;
    }

    public BaseBlockInfo decode(ByteBuffer buffer, byte type) {
        return new BaseBlockInfo(buffer, type);
    }
}
