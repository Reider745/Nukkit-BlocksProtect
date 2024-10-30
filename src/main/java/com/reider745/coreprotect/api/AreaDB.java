package com.reider745.coreprotect.api;

import com.mefrreex.jooq.database.IDatabase;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.concurrent.CompletableFuture;

public class AreaDB {
    public static final short SIZE = 256;
    private static final Field<byte[]> POS = DSL.field("pos", byte[].class);
    private static final Field<byte[]> USE = DSL.field("use", byte[].class);

    private final Table<Record> table;
    private final CompletableFuture<Connection> connection;

    public AreaDB(IDatabase database, Table<Record> table){
        this.table = table;
        this.connection = database.getConnection();

        connection.thenAccept(connection -> {
            DSL.using(connection)
                    .createTableIfNotExists(table)
                    .column("pos", SQLDataType.BINARY(3))
                    .column("use", SQLDataType.VARBINARY)
                    .execute();
        }).join();
    }

    protected byte[] getInteractionOrCreate(DSLContext context, byte[] pos){
        final SelectConditionStep<Record> select = context.selectFrom(table).where(DSL.field("pos").eq(pos));
        final Result<Record> result = select.fetch();

        if(result.isEmpty()){
            final byte[] EMPTY = new byte[] {0, 0, 0, 0};
            context.insertInto(table)
                    .set(POS, pos)
                    .set(USE, EMPTY)
                    .execute();
            return EMPTY;
        }

        return result.get(0).get(DSL.field("use", byte[].class));
    }

    protected void setInteractions(DSLContext context, byte[] pos, byte[] bytes){
        context.update(table)
                .set(USE, bytes)
                .where(DSL.field("pos").eq(pos))
                .execute();
    }

    protected void addInteraction(Connection connection, byte x, byte y, byte z, byte type, String player){
        final DSLContext context = DSL.using(connection);

        final byte[] pos = new byte[]{x, y, z};
        final byte[] bytes = getInteractionOrCreate(context, pos);
        int length = bytes.length;

        final ByteBuffer buffer = ByteBuffer.allocate(length + 8 + 1 + 1 + player.length());
        buffer.put(bytes);

        int size = buffer.getInt(0);
        buffer.putInt(0, size + 1);

        buffer.putLong(length, System.currentTimeMillis());
        length += 8;
        buffer.put(length, type);
        length += 1;
        buffer.put(length, (byte) (player.length() + Byte.MIN_VALUE));
        length += 1;

        for(byte symbol : player.getBytes()){
            buffer.put(length, symbol);
            length += 1;
        }

        setInteractions(context, pos, buffer.array());
    }

    public void addInteractionAsync(byte x, byte y, byte z, byte type, String player) {
        connection.thenAcceptAsync(connection -> addInteraction(connection, x, y, z, type, player));
    }

    public void addInteraction(byte x, byte y, byte z, byte type, String player) {
        connection.thenAccept(connection -> addInteraction(connection, x, y, z, type, player));
    }

    protected BlockInteractionPlayerInfo[] getInteractions(Connection connection, byte x, byte y, byte z){
        final ByteBuffer buffer = ByteBuffer.wrap(getInteractionOrCreate(DSL.using(connection), new byte[]{x, y, z}));
        final BlockInteractionPlayerInfo[] list = new BlockInteractionPlayerInfo[buffer.getInt()];

        for(int i = 0;i < list.length;i++){
            list[i] = new BlockInteractionPlayerInfo(buffer);
        }

        return list;
    }

    public BlockInteractionPlayerInfo[] getInteractionsAsync(byte x, byte y, byte z){
        return connection.thenApplyAsync(connection -> getInteractions(connection, x, y, z)).join();
    }

    public BlockInteractionPlayerInfo[] getInteractions(byte x, byte y, byte z){
        return connection.thenApply(connection -> getInteractions(connection, x, y, z)).join();
    }

    public void unload() {

    }
}
