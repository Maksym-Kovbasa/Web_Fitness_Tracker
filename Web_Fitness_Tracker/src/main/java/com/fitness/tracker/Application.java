package com.fitness.tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.fitness.tracker.Service.GoalService;

@SpringBootApplication
public class Application {

    @Autowired
    private GoalService goalService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        goalService.fixInvalidProgressValues();
		goalService.updateAllGoalsUsername();
    }
}
