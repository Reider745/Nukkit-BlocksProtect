package com.reider745.coreprotect.api;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import com.mefrreex.jooq.database.SQLiteDatabase;
import org.jooq.impl.DSL;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class LevelDB {
    private ConcurrentHashMap<Long, AreaDB> ares = new ConcurrentHashMap<>();
    private final SQLiteDatabase database;

    public LevelDB(File dabaseFile){
        this.database = new SQLiteDatabase(dabaseFile);
    }

    private static Long hashPosition(int x, int z){
        return (((long)x) << 32) | (z & 0xffffffffL);
    }

    public AreaDB getAreaAt(int x, int z){
        final int areaPosX = x / AreaDB.SIZE;
        final int areaPosZ = z / AreaDB.SIZE;

        return ares.computeIfAbsent(hashPosition(areaPosX, areaPosZ), k -> new AreaDB(this.database, DSL.table(("area"+areaPosX+"_"+areaPosZ).replaceAll("-", "m"))));
    }

    public void addInteraction(int x, int y, int z, PlayerInteractionType type, String player){
        getAreaAt(x, z).addInteraction((byte) (x % AreaDB.SIZE), (byte) (y % AreaDB.SIZE), (byte) (z % AreaDB.SIZE), (byte) type.ordinal(), player);
    }

    public void addInteraction(Position position, PlayerInteractionType type, Player player){
        addInteraction((int) position.x, (int) position.y, (int) position.z, type, player.getDisplayName());
    }

    public BlockInteractionPlayerInfo[] getInteractions(int x, int y, int z){
        return getAreaAt(x, z).getInteractions((byte) (x % AreaDB.SIZE), (byte) (y % AreaDB.SIZE), (byte) (z % AreaDB.SIZE));
    }

    public BlockInteractionPlayerInfo[] getInteractions(Position position){
        return getInteractions((int) position.x, (int) position.y, (int) position.z);
    }

    public void addInteractionAsync(int x, int y, int z, PlayerInteractionType type, String player){
        getAreaAt(x, z).addInteractionAsync((byte) (x % AreaDB.SIZE), (byte) (y % AreaDB.SIZE), (byte) (z % AreaDB.SIZE), (byte) type.ordinal(), player);
    }

    public void addInteractionAsync(Position position, PlayerInteractionType type, Player player){
        addInteractionAsync((int) position.x, (int) position.y, (int) position.z, type, player.getDisplayName());
    }

    public BlockInteractionPlayerInfo[] getInteractionsAsync(int x, int y, int z){
        return getAreaAt(x, z).getInteractionsAsync((byte) (x % AreaDB.SIZE), (byte) (y % AreaDB.SIZE), (byte) (z % AreaDB.SIZE));
    }

    public BlockInteractionPlayerInfo[] getInteractionsAsync(Position position){
        return getInteractionsAsync((int) position.x, (int) position.y, (int) position.z);
    }

    public void unload() {
        for(AreaDB area : ares.values())
            area.unload();
        ares.clear();
    }
}
