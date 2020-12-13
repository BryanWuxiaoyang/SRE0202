package com.etoak.crawl.main;

import com.etoak.crawl.link.Links;
import com.etoak.crawl.page.Page;
import com.etoak.crawl.page.PageParserTool;
import dataset.AttributeSet;
import dataset.DatasetTable;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import pagefinder.LocalPageCache;
import pagefinder.LocalPageFinder;
import pagefinder.PageFinder;
import pagefinder.WebPageFinder;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class MyCrawler {

    /**
     * 使用种子初始化 URL 队列
     *
     * @param seeds 种子 URL
     * @return
     */
    private void initCrawlerWithSeeds(String[] seeds) {
        for (int i = 0; i < seeds.length; i++){
            Links.addUnvisitedUrlQueue(seeds[i]);
        }
    }

    private static final String baseURL = "https://bugs.eclipse.org/bugs/";

    private final PageFinder pageFinder;

    public MyCrawler(){
        this(new WebPageFinder());
    }

    public MyCrawler(PageFinder pageFinder){
        this.pageFinder = pageFinder;
    }

    /**
     * 抓取过程
     *
     * @param seeds
     * @return
     */
    public List<Page> crawling(String[] seeds)throws Exception {

        //初始化 URL 队列
        initCrawlerWithSeeds(seeds);

        List<Page> pageList = new ArrayList<>();
        //循环条件：待抓取的链接不空且抓取的网页不多于 1000
        while (!Links.unVisitedUrlQueueIsEmpty()  && Links.getVisitedUrlNum() <= 1000) {

            //先从待访问的序列中取出第一个；
            String visitUrl = (String) Links.removeHeadOfUnVisitedUrlQueue();
            if (visitUrl == null){
                continue;
            }

            //根据URL得到page;
            Page page = pageFinder.loadPage(visitUrl);
            pageList.add(page);


            //对page进行处理： 访问DOM的某个标签
//            Elements es = PageParserTool.select(page,"td ");
//            for (Element e : es) {
//                System.out.println("enter bug: " + e.text() + "$" + e.attr("href"));
//            }

            //将保存文件
            //FileTool.saveToLocal(page);

            //将已经访问过的链接放入已访问的链接中；
            Links.addVisitedUrlSet(visitUrl);

//            //得到超链接
//            Set<String> links = PageParserTool.getLinks(page,"img");
//            for (String link : links) {
//                Links.addUnvisitedUrlQueue(link);
//                System.out.println("新增爬取路径: " + link);
//            }
        }
        return pageList;
    }


    public static AttributeSet initAttributeSet(){
        AttributeSet attributeSet = new AttributeSet(false);
        attributeSet.add(
                "no",
                "info",
                "status",
                "version",
                "importance",
                "hardware",
                "reported"
                );
        return attributeSet;
    }
    //main 方法入口
    public static void main(String[] args) throws Throwable {
        AttributeSet attributeSet = initAttributeSet();
        DatasetTable table = new DatasetTable(attributeSet, true);
        LocalPageCache pageCache = new LocalPageCache("./pages", true);
        PageFinder finder = new WebPageFinder();
        finder.setPrevFinder(new LocalPageFinder(pageCache));

        String[] mainUrls = new String[]{
                "https://bugs.eclipse.org/bugs/buglist.cgi?chfield=%5BBug%20creation%5D&chfieldfrom=7d",
                "https://bugs.eclipse.org/bugs/buglist.cgi?quicksearch=status%3Dverified"
        };
        List<Element> elements = new ArrayList<>();
        for (String mainUrl : mainUrls) {
            Page mainPage = finder.loadPage(mainUrl);
            Elements es = PageParserTool.select(mainPage, "tr.bz_bugitem td.first-child.bz_id_column a");
            elements.addAll(es);
            pageCache.put(mainUrl, mainPage);
            pageCache.flush();
        }

        final int THREAD_NUM = 128;
        final int pace = Math.max(1, elements.size() / THREAD_NUM);
        final int threadNum = Math.min(elements.size(), THREAD_NUM);
        Thread[] threads = new Thread[threadNum];

        for(int thread = 0; thread < threadNum; thread++){
            final int threadNo = thread;
            threads[thread] = new Thread(()-> {
                int fromIndex = threadNo * pace;
                int toIndex = Math.min((threadNo + 1) * pace, elements.size());
                for (int i = fromIndex; i < toIndex; i++) {
                    Element element = elements.get(i);
                    String url = baseURL + element.attr("href");
                    Page page = null;
                    while((page = finder.loadPage(url)) == null);

                    DatasetTable.Tuple tuple = table.new Tuple();
                    Elements es;
                    es = PageParserTool.select(page, "div.bz_short_desc_container.edit_form a[href~=show]");
                    String text = es.first().text();
                    String no = text.replaceAll("[^0-9]", "").trim();
                    String info = PageParserTool.select(page, "#short_desc_nonedit_display").first().text().trim();
                    String status = PageParserTool.select(page, "#bz_field_status span").first().text().trim();
                    String version = PageParserTool.select(page, "#field_label_version+td").first().text().trim();
                    String importance = PageParserTool.select(page, "tr th.field_label label[accesskey]").first().parent().parent().getElementsByTag("td").first().text();
                    importance = importance.replaceAll("\\(vote\\)", "").trim();
                    String hardware = PageParserTool.select(page, "tr #field_label_rep_platform").first().parent().getElementsByTag("td").first().text().trim();
                    String reported = PageParserTool.select(page, "table tbody tr th.field_label:contains(Reported)+td").first().text().trim();
                    tuple.set("no", no);
                    tuple.set("info", info);
                    tuple.set("status", status);
                    tuple.set("version", version);
                    tuple.set("importance", importance);
                    tuple.set("hardware", hardware);
                    tuple.set("reported", reported);
                    System.out.println("cached:" + tuple);
                    table.add(tuple);
                    pageCache.put(url, page);
                    pageCache.clear();
                }
            });
            threads[thread].start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        pageCache.flush();
        Files.write(new File("./result.txt").toPath(), table.toString().getBytes());

        // Write to dir
//        final String dirPath = "./pages";
//        new File(dirPath).mkdir();
//        File indexFile = new File(dirPath, "index.txt");
//        try(BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexFile))) {
//            int fileNo = 1;
//            for (Page page : pages) {
//                String fileName = "file" + fileNo;
//                File file = new File(dirPath, fileName);
//                try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
//                    out.writeObject(page);
//                    indexWriter.write(page.getUrl() + "\t" + fileName);
//                    indexWriter.newLine();
//                }
//            }
//        }
    }
}
