package com.reider745.coreprotect.api.description;

import com.reider745.coreprotect.api.PlayerInteractionType;
import com.reider745.coreprotect.api.description.parser.BaseParserBlockInfo;
import com.reider745.coreprotect.api.description.parser.ParserChangeBlockInfo;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class BaseBlockInfo {
    public final long time;
    public final PlayerInteractionType type;
    public String name = "";

    public BaseBlockInfo(ByteBuffer buffer, byte type) {
        this.type = PlayerInteractionType.values()[type];

        time = buffer.getLong();

        final int size = ((short) buffer.get()) - Byte.MIN_VALUE;
        for(int i = 0;i < size;i++){
            name += (char) buffer.get();
        }
    }

    public String toMessage(){
        final Duration duration = Duration.between(Instant.ofEpochMilli(time), Instant.ofEpochMilli(System.currentTimeMillis()));
        String message = duration.toHours() + " ч, " + duration.toMinutesPart() + " м," + duration.toSecondsPart() + " с.";
        return "type="+type.name()+", player="+name+"("+message+")";
    }

    @Override
    public String toString() {
        return "BaseBlockInfo{" +
                "time=" + time +
                ", type=" + type +
                ", name='" + name + '\'' +
                '}';
    }

    private static final BaseParserBlockInfo[] PARSERS = new BaseParserBlockInfo[PlayerInteractionType.MAX_VALUE.ordinal()];

    public static void registerParser(PlayerInteractionType type, BaseParserBlockInfo info) {
        PARSERS[type.ordinal()] = info;
    }

    public static BaseParserBlockInfo getParserForType(byte type) {
        return PARSERS[type];
    }

    static {
        BaseBlockInfo.registerParser(PlayerInteractionType.BREAK, new BaseParserBlockInfo());
        BaseBlockInfo.registerParser(PlayerInteractionType.CHANGE, new ParserChangeBlockInfo());
        BaseBlockInfo.registerParser(PlayerInteractionType.PLACE, new ParserChangeBlockInfo());
    }
}
