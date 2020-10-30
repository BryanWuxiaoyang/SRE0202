package com.etoak.crawl.main;

import com.etoak.crawl.link.LinkFilter;
import com.etoak.crawl.link.Links;
import com.etoak.crawl.page.Page;
import com.etoak.crawl.page.PageParserTool;
import com.etoak.crawl.page.RequestAndResponseTool;
import com.etoak.crawl.util.FileTool;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
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


    static List<String> reviews = new ArrayList<String>();
    /**
     * 抓取过程
     *
     * @param seeds
     * @return
     */
    public void crawling(String[] seeds) {

        //初始化 URL 队列
        initCrawlerWithSeeds(seeds);

        //定义过滤器，提取以 http://www.baidu.com 开头的链接
        LinkFilter filter = new LinkFilter() {
            public boolean accept(String url) {
                return url.startsWith("http://www.baidu.com");
            }
        };

        //循环条件：待抓取的链接不空且抓取的网页不多于 1000
        while (!Links.unVisitedUrlQueueIsEmpty()  && Links.getVisitedUrlNum() <= 1000) {

            //先从待访问的序列中取出第一个；
            String visitUrl = (String) Links.removeHeadOfUnVisitedUrlQueue();
            if (visitUrl == null){
                continue;
            }

            //根据URL得到page;
            Page page = RequestAndResponseTool.sendRequstAndGetResponse(visitUrl);

            //对page进行处理： 访问DOM的某个标签
            Elements es = PageParserTool.select(page,"a[id]");
            if(!es.isEmpty()){
                System.out.println("下面将打印所有a标签： ");
                System.out.println(es);
                System.out.println(es.text());
                for (Element e : es) {
                    reviews.add(e.text());
                }
            }

            //将保存文件
            FileTool.saveToLocal(page);

            //将已经访问过的链接放入已访问的链接中；
            Links.addVisitedUrlSet(visitUrl);

//            //得到超链接
//            Set<String> links = PageParserTool.getLinks(page,"img");
//            for (String link : links) {
//                Links.addUnvisitedUrlQueue(link);
//                System.out.println("新增爬取路径: " + link);
//            }
        }


    }


    //main 方法入口
    public static void main(String[] args) {
        MyCrawler crawler = new MyCrawler();
        for(int page = 1; page <= 100; page++){
            String url = "https://github.com/microsoft/vscode/issues?page=" + page + "&q=is%3Aissue+is%3Aopen+label%3Afeature-request";
            System.out.println("start page" + page);
            crawler.crawling(new String[]{url});
            System.out.println("finish page" + page);

        }

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("reviews.txt");
            for (String review : reviews) {
                fileWriter.append(review).append("\n");
            }
            reviews.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                assert fileWriter != null;
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
