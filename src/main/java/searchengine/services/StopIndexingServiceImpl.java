package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.SiteEntity;
import searchengine.model.SiteStatus;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {

    private final SiteRepository siteRepository;

    @Override
    public HashMap<String, Object> stop() {
        HashMap<String, Object> response = new HashMap<>();

        List<SiteEntity> siteEntities = siteRepository.findAll();
        for (SiteEntity siteEntity : siteEntities) {
            if (siteEntity != null && siteEntity.getStatus().toString().equals("INDEXING")) {
                siteEntity.setStatus(SiteStatus.FAILED);
                siteEntity.setLastError("Индексация остановлена пользователем");
                siteEntity.setStatusTime(LocalDateTime.now());
                response.put("result " +  siteEntity.getUrl(), true);

            } else {
                response.put("result", false);
                response.put("error", "Индексация не запущена");
            }
        }
        siteRepository.saveAllAndFlush(siteEntities);

        return response;
    }
}
