package pagefinder;

import com.etoak.crawl.page.Page;
import com.etoak.crawl.page.PageParserTool;
import com.etoak.crawl.page.RequestAndResponseTool;

import java.net.SocketTimeoutException;

public class WebPageFinder extends PageFinder{
    @Override
    public Page findPage(String url) {
        Page page = null;
        while((page = RequestAndResponseTool.sendRequstAndGetResponse(url)) == null);
        return page;
    }
}
