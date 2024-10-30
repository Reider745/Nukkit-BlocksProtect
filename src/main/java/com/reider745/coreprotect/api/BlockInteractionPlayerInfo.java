package com.reider745.coreprotect.api;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;

public class BlockInteractionPlayerInfo {
    public final long time;
    public final PlayerInteractionType type;
    public String name = "";

    BlockInteractionPlayerInfo(ByteBuffer buffer){
        time = buffer.getLong();
        type = PlayerInteractionType.values()[buffer.get()];
        final int size = ((short) buffer.get()) - Byte.MIN_VALUE;
        for(int i = 0;i < size;i++){
            name += (char) buffer.get();
        }
    }

    public String toMessage(){
        final Duration duration = Duration.between(Instant.ofEpochMilli(time), Instant.ofEpochMilli(System.currentTimeMillis()));
        String message = "Прошло " + duration.toHours() + " часов, " + duration.toMinutesPart() + " минут и " + duration.toSecondsPart() + " секунд.";
        return "type="+type.name()+", player="+name+"("+message+")";
    }

    @Override
    public String toString() {
        return "BlockInteractionPlayerInfo{" +
                "time=" + time +
                ", type=" + type.name() +
                ", name='" + name + '\'' +
                '}';
    }
}
