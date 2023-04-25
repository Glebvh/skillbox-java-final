package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteRepository;
import searchengine.model.SiteStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
                siteEntity.setStatus(SiteStatus.FAILED);
                siteEntity.setLastError("Индексация остановлена пользователем");
                siteEntity.setStatusTime(LocalDateTime.now());
                siteRepository.save(siteEntity);
                response.put("result " + siteUrl, true);

            } else {
                response.put("result " + siteUrl, false);
                response.put("error", "Индексация " + siteUrl + " не запущена");
            }
        }
        return response;
    }
}
