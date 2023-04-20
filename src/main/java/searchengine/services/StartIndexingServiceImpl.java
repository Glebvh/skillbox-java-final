package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {

    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final SitesList sites;

    @Override
    public HashMap<String, Object> start() {
        HashMap<String, Object> response = new HashMap<>();
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            String siteUrl = site.getUrl();
            String siteName = site.getName();
            if (!isIndexing(siteUrl)) {
                siteRepository.deleteByUrl(siteUrl);
                response = siteAdd(siteUrl, siteName);
                if (response.size() == 0) {
                    Thread thread = new Thread(() -> pageAdd(siteUrl));
                    thread.start();
                }
            } else {
                response.put("result", false);
                response.put("error", "Индексация " + siteName + " уже запущена");
            }
        }
        return response;
    }

    public boolean isIndexing(String url) {
        SiteEntity siteEntity = siteRepository.findByUrl(url);
        if (siteEntity != null) {
            return siteEntity.getStatus().toString().equals("INDEXING");
        }
        return false;
    }

    public HashMap<String, Object> siteAdd(String siteUrl, String siteName) {
        HashMap<String, Object> response = new HashMap<>();
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setUrl(siteUrl);
        siteEntity.setName(siteName);
        Connection.Response connectionResponse;
        try {
            connectionResponse = Jsoup.connect(siteUrl).execute();
//            int responseCode = connectionResponse.statusCode();
            siteEntity.setStatus(SiteStatus.INDEXING);
        } catch (IOException e) {
            siteEntity.setStatus(SiteStatus.FAILED);
            siteEntity.setLastError(e.toString());  //TODO сделать описание ошибки c номером
            response.put("result", false);
            response.put("error", siteName + " - " + e);
        }

        siteRepository.save(siteEntity);
        return response;
    }

    public HashMap<String, Object> pageAdd(String siteUrl) {
        HashMap<String, Object> response = new HashMap<>();
        Set<String> finalList = new ForkJoinPool()
                .invoke(new LinksCollectorService(siteUrl, pageRepository, siteRepository, siteUrl));
        LinksCollectorService.linksSet = new HashSet<>();
        SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);
        if(siteEntity.getStatus().toString().equals("FAILED")
                && siteEntity.getLastError().equals("Индексация остановлена пользователем")) {
            response.put("result", true);
        } else if (siteEntity.getStatus().toString().equals("FAILED")) {
            response.put("result", false);
            response.put("error", siteEntity.getName() + " - " + siteEntity.getLastError());
        } else {
            siteEntity.setStatus(SiteStatus.INDEXED);
            response.put("result", true);
        }
        return response;
    }
}
