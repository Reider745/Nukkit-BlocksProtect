package com.reider745.blocks_protect.api;

import com.mefrreex.jooq.database.IDatabase;
import com.reider745.blocks_protect.api.description.BaseBlockInfo;
import com.reider745.blocks_protect.api.description.parser.BaseParserBlockInfo;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
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

    protected void addInteraction(Connection connection, byte x, byte y, byte z, byte type, String player, short beforeBlock, short afterBlock){
        final DSLContext context = DSL.using(connection);

        final byte[] pos = new byte[]{x, y, z};
        final byte[] bytes = getInteractionOrCreate(context, pos);
        final BaseParserBlockInfo parser = BaseBlockInfo.getParserForType(type);
        final int length = bytes.length;

        final ByteBuffer buffer = ByteBuffer.allocate(length + parser.length(player));
        buffer.put(bytes);
        buffer.putInt(0, buffer.getInt(0) + 1);

        parser.encode(buffer, length, type, player, beforeBlock, afterBlock);

        setInteractions(context, pos, buffer.array());
    }

    public void addInteractionAsync(byte x, byte y, byte z, byte type, String player, short beforeBlock, short afterBlock) {
        connection.thenAcceptAsync(connection -> addInteraction(connection, x, y, z, type, player, beforeBlock, afterBlock));
    }

    public void addInteraction(byte x, byte y, byte z, byte type, String player, short beforeBlock, short afterBlock) {
        connection.thenAccept(connection -> addInteraction(connection, x, y, z, type, player, beforeBlock, afterBlock));
    }

    protected BaseBlockInfo[] getInteractions(Connection connection, byte x, byte y, byte z){
        final ByteBuffer buffer = ByteBuffer.wrap(getInteractionOrCreate(DSL.using(connection), new byte[]{x, y, z}));
        final BaseBlockInfo[] list = new BaseBlockInfo[buffer.getInt()];

        for(int i = 0;i < list.length;i++){
            final byte type = buffer.get();
            list[i] = BaseBlockInfo.getParserForType(type).decode(buffer, type);
        }

        return list;
    }

    public BaseBlockInfo[] getInteractionsAsync(byte x, byte y, byte z){
        return connection.thenApplyAsync(connection -> getInteractions(connection, x, y, z)).join();
    }

    public BaseBlockInfo[] getInteractions(byte x, byte y, byte z){
        return connection.thenApply(connection -> getInteractions(connection, x, y, z)).join();
    }

    public BaseBlockInfo[] getAllInteractions() {
        return connection.thenApply((connection -> {
            final Result<Record> blocks =  DSL.using(connection).select()
                    .from(table)
                    .fetch();
            final ArrayList<BaseBlockInfo> list = new ArrayList<>();

            for(Record record : blocks) {
                final byte[] pos = record.get(POS, byte[].class);
                list.addAll(Arrays.asList(getInteractions(connection, pos[0], pos[1], pos[2])));
            }

            return list.toArray(new BaseBlockInfo[0]);
        })).join();
    }

    public void unload() {}
}
