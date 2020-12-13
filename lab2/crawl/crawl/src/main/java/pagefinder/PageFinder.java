package pagefinder;

import com.etoak.crawl.page.Page;

public abstract class PageFinder {
    private PageFinder prevFinder;

    protected abstract Page findPage(String url);

    public PageFinder setPrevFinder(PageFinder finder){
        PageFinder temp = prevFinder;
        this.prevFinder = finder;
        return temp;
    }

    public PageFinder getPrevFinder(){
        return prevFinder;
    }
    public Page loadPage(String url){
        Page page = prevFinder == null ? null : prevFinder.loadPage(url);
        return page == null ? findPage(url) : page;
    }
}
