package com.example.playerhistory.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.playerhistory.repository.PlayerHistoryRepository;

class PlayerHistoryServiceSpringTest {

    @Test
    void springCanCreateServiceWithRepositoryConstructor() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(
                    PlayerHistoryRepository.class,
                    () -> mock(PlayerHistoryRepository.class));
            context.register(PlayerHistoryService.class);
            context.refresh();

            assertNotNull(context.getBean(PlayerHistoryService.class));
        }
    }
}
