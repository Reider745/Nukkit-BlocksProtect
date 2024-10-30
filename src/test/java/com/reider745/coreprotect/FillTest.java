package com.reider745.coreprotect;

import com.mefrreex.jooq.JOOQConnector;
import com.reider745.coreprotect.api.LevelDB;
import com.reider745.coreprotect.api.PlayerInteractionType;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

public class FillTest {
    public static void main(String[] args) {
        JOOQConnector.setJOOQMessagesEnabled(false);

        System.out.println("Test for work");

        final LevelDB levelDB = new LevelDB(new File("speed_test.db"));

        levelDB.addInteraction(0, 0, 0, PlayerInteractionType.BREAK, "test");
        levelDB.addInteraction(16, 16, 16, PlayerInteractionType.BREAK, "test");

        System.out.println(Arrays.toString(levelDB.getInteractions(0, 0, 0)));

        System.out.println("Start test speed");

        final int FILL_DB = 1_000;
        final int PERCENT = FILL_DB / 100;
        final Random random = new Random();

        System.out.println("Start fill db, "+FILL_DB);
        long start = System.currentTimeMillis();
        for(int i = 0, percent = 0;i < FILL_DB;i++){
            if(i % PERCENT == 0){
                percent++;
                System.out.println("Percent "+percent+"%");
            }
            levelDB.addInteractionAsync(
                    -512 + random.nextInt(1024),
                    -512 + random.nextInt(1024),
                    -512 + random.nextInt(1024),
                    PlayerInteractionType.BREAK, "test"
            );
        }

        System.out.println("End fill db, time: "+(System.currentTimeMillis() - start));
    }
}