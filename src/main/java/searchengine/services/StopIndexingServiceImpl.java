package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.model.SiteRepository;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StopIndexingServiceImpl implements StopIndexingService {

    private final SiteRepository siteRepository;
    private final SitesList sites;

    @Override
    public HashMap<String, Object> stop() {
        HashMap<String, Object> response = new HashMap<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            String siteUrl = site.getUrl();
            SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);
            if (siteEntity != null && siteEntity.getStatus().toString().equals("INDEXING")) {
                response.put("result", true);
                siteEntity.setStatus(SiteStatus.FAILED);
                siteEntity.setLastError("Индексация остановлена пользователем");
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
            } else {
                response.put("result", false);
                response.put("error", "Индексация не запущена");
            }
        }
        return response;
    }
}
