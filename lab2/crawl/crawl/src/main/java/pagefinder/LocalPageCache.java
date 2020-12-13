package pagefinder;

import com.etoak.crawl.page.Page;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPageCache {
    private final String localDir;

    private final Map<String, File> fileMap;

    private final Map<String, Page> pageMap;

    private final boolean threadSafe;

    public LocalPageCache(String localDir, boolean threadSafe) {
        this.localDir = localDir;
        this.fileMap = threadSafe ? new ConcurrentHashMap<>() : new HashMap<>();
        this.pageMap = threadSafe ? new ConcurrentHashMap<>() : new HashMap<>();
        this.threadSafe = threadSafe;

        assert new File(localDir).mkdirs();
        loadIndexFile();
    }

    private void loadIndexFile(){
        File dirFile = new File(localDir, "index.txt");
        if(dirFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(localDir, "index.txt")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tuple = line.split("[\t]");
                    assert tuple.length == 2;
                    fileMap.put(tuple[0], new File(localDir, tuple[1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    private Page loadFile(String url, File pageFile){
        Page page = null;
        try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(pageFile))){
            page = (Page)in.readObject();
            pageMap.put(url, page);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return page;
    }

    public void put(String url, Page page){
        pageMap.put(url, page);
    }

    public Page get(String url){
        Page page = pageMap.get(url);
        if(page == null){
            File pageFile = fileMap.get(url);
            if(pageFile != null) {
                if(threadSafe){
                    synchronized (this){
                        if((page = pageMap.get(url)) == null){
                            page = loadFile(url, pageFile);
                        }
                    }
                }
                else page = loadFile(url, pageFile);
            }
        }
        return page;
    }

    private void doSave(){
        File indexFile = new File(localDir, "index.txt");
        try (BufferedWriter indexWriter = new BufferedWriter(new FileWriter(indexFile, true))) {
            pageMap.forEach((url, page) -> {
                if (!fileMap.containsKey(url)) {
                    String fileName = "page" + fileMap.size();
                    File pageFile = new File(localDir, fileName);
                    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(pageFile))) {
                        out.writeObject(page);
                        indexWriter.write(url + "\t" + fileName);
                        indexWriter.newLine();
                        fileMap.put(url, pageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flush(){
        if(threadSafe) synchronized (this) { doSave(); }
        else doSave();
    }

    public void clear(){
        flush();
        pageMap.clear();
    }
}
