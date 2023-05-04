package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

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
                siteAdd(siteUrl, siteName);
                Thread thread = new Thread(() -> {
                    pageAdd(siteUrl);
                });
                thread.start();
                response.put("result", true);
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

    public void siteAdd(String siteUrl, String siteName) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setUrl(siteUrl);
        siteEntity.setName(siteName);
        Connection.Response connectionResponse;
        try {
            connectionResponse = Jsoup.connect(siteUrl).execute();
            int statusCode = connectionResponse.statusCode();
            if (statusCode == 200) {
                siteEntity.setStatus(SiteStatus.INDEXING);
                siteRepository.saveAndFlush(siteEntity);
            } else {
                exceptionChoice(statusCode, siteRepository, siteEntity);
            }
        } catch (IOException e) {
            siteEntity.setStatus(SiteStatus.FAILED);
            siteEntity.setLastError(e.toString());
            siteRepository.saveAndFlush(siteEntity);
            throw new IndexingException("Ошибка"); //Какой номер?
        }
    }

    public void exceptionChoice(int statusCode, SiteRepository siteRepository, SiteEntity siteEntity) {
        siteEntity.setStatus(SiteStatus.FAILED);
        String msg = null;
        if (statusCode == 400) {
            msg = "Ошибка запроса";
        } else if (statusCode == 401) {
            msg = "Ошибка авторизации";
        } else if (statusCode == 403) {
            msg = "Ошибка доступа";
        } else if (statusCode == 404) {
            msg = "Страница не найдена";
        }
        siteEntity.setLastError(msg);
        siteRepository.saveAndFlush(siteEntity);
        throw new IndexingException(msg);
    }

    public void pageAdd(String siteUrl) {
        Set<String> finalList = new ForkJoinPool()
                .invoke(new LinksCollectorService(siteUrl, pageRepository, siteRepository, siteUrl));

        SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);

        if (!siteEntity.getStatus().toString().equals("FAILED")) {
            siteEntity.setStatus(SiteStatus.INDEXED);
            siteRepository.save(siteEntity);
        }

        LinksCollectorService.linksSet = new HashSet<>();
    }
}
