package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.PageEntity;
import searchengine.model.PageRepository;
import searchengine.model.SiteEntity;
import searchengine.model.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.RecursiveTask;

@RequiredArgsConstructor
public class LinksCollectorService extends RecursiveTask<Set<String>> {
    public static Set<String> linksSet = new TreeSet<>();
    private final String url;
    private final PageRepository pageRepository;
    //    private final SiteEntity siteEntity;
    private final SiteRepository siteRepository;

    private final String siteUrl;


    @Override
    protected Set<String> compute() {
        List<LinksCollectorService> tasks = new ArrayList<>();

        setLinkToStaticSet(url);

        Set<String> tempSet = getLinksSet(url);
        for (String link : tempSet) {
            LinksCollectorService task = new LinksCollectorService(link, pageRepository, siteRepository, siteUrl);
            task.fork();
            tasks.add(task);
        }
        tasks.forEach(LinksCollectorService::join);

        return linksSet;
    }

    public Set<String> getLinksSet(String urlNext) {
        Set<String> tempSet = new TreeSet<>();
        Document document;
        try {
            Thread.sleep(110);

            Connection.Response response = Jsoup.connect(urlNext).execute();
            int responseCode = response.statusCode();

            PageEntity pageEntity = new PageEntity();

//          =======
            SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
//          =======

            document = Jsoup.connect(urlNext).get();
            pageEntity.setSiteId(siteEntity);
            pageEntity.setPath(urlNext);
            pageEntity.setCode(responseCode);
            pageEntity.setContent(String.valueOf(document));
            Elements elements = document.select("a[href]");
            for (Element element : elements) {
                String link = element.absUrl("href");
                if (checkURL(link) && setLinkToStaticSet(link)) {
                    tempSet.add(link);
                }
            }
            pageRepository.save(pageEntity);

        } catch (IOException | InterruptedException e) {
            return tempSet;
        }
        return tempSet;
    }

    public synchronized boolean setLinkToStaticSet(String link) {
        return linksSet.add(link);
    }

    public boolean checkURL(String link) {
        return link.startsWith(siteUrl) && link.endsWith("/")
                || link.startsWith(siteUrl) && link.endsWith(".html");
    }

}