package pagefinder;

import com.etoak.crawl.page.Page;
import com.etoak.crawl.page.RequestAndResponseTool;
import dataproc.Loader;

public class WebPageLoader implements Loader<String, Page> {
    @Override
    public Page load(String key) {
        return RequestAndResponseTool.sendRequstAndGetResponse(key);
    }
}
