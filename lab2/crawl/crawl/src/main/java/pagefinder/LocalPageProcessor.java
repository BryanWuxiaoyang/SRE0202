package pagefinder;

import com.etoak.crawl.page.Page;
import dataproc.Loader;
import dataproc.Processor;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalPageProcessor implements Processor<String, Page> {
    private final String dirPath;

    private final Map<String, File> fileMap;

    private final boolean threadSafe;

    public LocalPageProcessor(String dirPath, boolean threadSafe){
        this.dirPath = dirPath;
        this.threadSafe = threadSafe;
        this.fileMap = this.threadSafe ? new ConcurrentHashMap<>() : new HashMap<>();

        loadIndexFile();
    }

    private void loadIndexFile(){
        File dir = new File(dirPath);
        if(!dir.exists())assert dir.mkdirs();
        File indexFile = new File(dir, "index.txt");
        if(indexFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(indexFile, "index.txt")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tuple = line.split("[\t]");
                    assert tuple.length == 2;
                    fileMap.put(tuple[0], new File(indexFile, tuple[1]));
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    @Override
    public Page load(String url) {
        File file = fileMap.get(url);
        Page page = null;
        if (file != null && file.exists()) {
            try(ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))){
                page = (Page)in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                page = null;
                e.printStackTrace();
            }
        }
        return page;
    }

    @Override
    public void write(String url, Page page) {
        File file = fileMap.get(url);
        if (!file.exists()) {
            String fileName = "page" + fileMap.size();
            file = new File(dirPath, fileName);
            fileMap.put(url, file);
        }

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(page);
        } catch (IOException e) {
            fileMap.remove(url);
            e.printStackTrace();
        }
    }
}
