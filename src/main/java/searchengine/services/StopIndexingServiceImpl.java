package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {
    @Override
    public HashMap<String, Object> stop() {
        return null;
    }
}
