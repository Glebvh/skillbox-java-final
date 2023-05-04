package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.Response;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StartIndexingService;
import searchengine.services.StatisticsService;
import searchengine.services.StopIndexingService;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    private final StartIndexingService startIndexingService;

    private final StopIndexingService stopIndexingService;


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<HashMap<String,Object>> startIndexing() {
        return ResponseEntity.ok(startIndexingService.start());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<HashMap<String,Object>> stopIndexing() {
         return ResponseEntity.ok(stopIndexingService.stop());
    }
}
