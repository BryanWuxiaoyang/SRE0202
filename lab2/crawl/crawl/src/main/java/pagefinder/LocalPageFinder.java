package pagefinder;

import com.etoak.crawl.page.Page;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LocalPageFinder extends PageFinder{
    private final LocalPageCache pageCache;

    public LocalPageFinder(String localDir) throws IOException {
        this.pageCache = new LocalPageCache(localDir, false);
    }

    public LocalPageFinder(LocalPageCache pageCache){
        this.pageCache = pageCache;
    }

    @Override
    public Page findPage(String url) {
        return pageCache.get(url);
    }
}
