package com.example.playerhistory.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Table;

class PlayerHistoryTest {

    @Test
    void declaresTrackAndPlayedAtDatabaseUniqueness() {
        Table table = PlayerHistory.class.getAnnotation(Table.class);

        assertEquals("player_history", table.name());
        assertEquals("uk_player_history_track_played_at", table.uniqueConstraints()[0].name());
        assertArrayEquals(
                new String[] {"track_id", "played_at"},
                table.uniqueConstraints()[0].columnNames());
    }
}
