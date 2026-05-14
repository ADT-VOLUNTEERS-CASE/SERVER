package org.adt.volunteerscase.config;

import lombok.RequiredArgsConstructor;
import org.adt.volunteerscase.service.RatingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
public class RatingInitializer {

    private final RatingService ratingService;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildRatingsOnStartup() {
        ratingService.rebuildAllRatings();
    }
}
