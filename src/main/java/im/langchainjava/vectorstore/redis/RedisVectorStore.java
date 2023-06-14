package im.langchainjava.vectorstore.redis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import im.langchainjava.embedding.EmbeddingService;
import im.langchainjava.utils.ByteArrays;
import im.langchainjava.utils.JsonUtils;
import im.langchainjava.vectorstore.Document;
import im.langchainjava.vectorstore.VectorStore;
import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.Schema;
import redis.clients.jedis.search.SearchResult;

@Slf4j
public class RedisVectorStore implements VectorStore {

    private static int DIM = 1536;
    
    private static int EMBEDDING_SIZE = 8192;

    private static String MD5 = "MD5";

    JedisPooled client;
    int dim;
    String index;

    EmbeddingService embeddingService;

    public RedisVectorStore(String host, int port, String index, EmbeddingService embedding){
        this.client = new JedisPooled(host, port);
        this.dim = DIM;
        this.index = index; 
        this.embeddingService = embedding;
    }

    private boolean checkIndex(String index){
        try{
            Map<String,Object> info = client.ftInfo(index);
            if(info == null){
                return false;
            }
        }catch(Exception e){
            return false;
        }
        return true;
    }

    private void createIndex(String index){
        if(checkIndex(index)){
            return;
        }

        Map<String,Object> attr = new HashMap<>();
        attr.put("TYPE","FLOAT32");
        attr.put("DIM",dim);
        attr.put("DISTANCE_METRIC","L2");
        Schema sc = new Schema()
            .addTextField("content", 1.0)
            .addTextField("metadata", 1.0)
            .addVectorField("content_vector", Schema.VectorField.VectorAlgo.FLAT, attr);

        IndexDefinition def = new IndexDefinition().setPrefixes(new String[]{index});

        client.ftCreate(index, IndexOptions.defaultOptions().setDefinition(def), sc);
    }

    private Query prepareQuery(String user, String text, int k){
        float[] embedding = embeddingService.embededQuery(user, text);
        byte[] vector;
        try {
            vector = ByteArrays.floatArrayToByteArray(embedding);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new Query("*=>[KNN " + k + " @content_vector $vector AS vector_score]")
                .dialect(2)
                .returnFields("content", "metadata", "vector_score")
                .setSortBy("vector_score", false)
                .setWithScores()
                .addParam("vector", vector);
    }
    
    @Override
    public List<Document> similaritySearch(String user, String text, int k) {
        Query q = prepareQuery(user, text, k);
        if(q == null){
            return new ArrayList<>();
        }
        SearchResult result = this.client.ftSearch(index, q);
        List<redis.clients.jedis.search.Document> docs = result.getDocuments();
        if(docs == null || docs.isEmpty()){
            return new ArrayList<>();
        }
        List<Document> retDocs = new ArrayList<>();
        for(redis.clients.jedis.search.Document d : docs){
            Document rd = new Document();
            rd.setContent(d.getString("content"));
            rd.setMeta(JsonUtils.toMapOf(d.getString("metadata")));
            retDocs.add(rd);
            // rd.setVector(ByteArrays.ByteArrayToFloatArray((byte[])d.get("content_vector")));  
        }

        return retDocs;
    }

    @Override
    public void addDocuments(String user, List<String> docs, List<Map<String,String>> meta) {
        createIndex(index);
        List<Document> docList = new ArrayList<>();
        int size = docs.size();
        for(int i = 0; i< size; i++){
            log.info("running.." + i);
            List<String> docStrList = new ArrayList<>();
            docStrList.add(docs.get(i));
            List<float[]> vectorList = embeddingService.embededDocuments(user, docStrList);
            String content = docs.get(i);
            Map<String,String> metadata = meta.get(i);
            float[] vector = vectorList.get(0);
            Document doc = new Document();
            doc.setContent(content);
            doc.setMeta(metadata);
            doc.setVector(vector);
            docList.add(doc);
        }

        for(Document doc : docList){
            Map<byte[], byte[]> map = new HashMap<>();
            float[] floats = doc.getVector();
            try {
                byte[] contentBytes = doc.getContent().getBytes("utf8");
                MessageDigest m;
                try {
                    m = MessageDigest.getInstance(MD5);
                    m.update(contentBytes);
                    byte md5[] = m.digest();
                    String key = index + ":" + Base64.getEncoder().encodeToString(md5);
                    map.put("content".getBytes("utf8"), contentBytes);
                    map.put("metadata".getBytes("utf8"), JsonUtils.fromMapToBytes(doc.getMeta()));
                    try {
                        byte[] bs = ByteArrays.floatArrayToByteArray(floats);
                        map.put("content_vector".getBytes("utf8"), bs);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    this.client.hset(key.getBytes("utf8"), map);
                } catch (NoSuchAlgorithmException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } 
        }
    }

    @Override
    public void loadFromFile(File f) {
        this.createIndex(this.index);
        try(FileInputStream in = new FileInputStream(f)){
            EpubReader bookReader = new EpubReader();
            Book b = bookReader.readEpub(in);

            Spine spine = b.getSpine();
            List<SpineReference> spineList = spine.getSpineReferences();
            int count = spineList.size();
            List<String> contents = new ArrayList<>();
            List<Map<String,String>> metaData = new ArrayList<>();
            for (int i = 0; count > i; i++) {
                Resource res = spine.getResource(i);
                try (InputStream is = res.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));){

                    KXmlParser parser = new KXmlParser();
                    parser.setInput(reader);
                    int eventType = parser.getEventType();
                    
                    eventType = parser.next();
                    StringBuilder sb  = new StringBuilder();
                    while( eventType != XmlPullParser.END_DOCUMENT) {
                        if(eventType == XmlPullParser.TEXT){
                            String element = parser.getText();
                            if(sb.length() + element.length() > EMBEDDING_SIZE){
                                contents.add(sb.toString());
                                Map<String,String> meta = new HashMap<String,String>();
                                meta.put("sources",index + ":" + res.getHref());
                                meta.put("href", res.getHref());
                                meta.put("line_num", String.valueOf(parser.getLineNumber()));
                                metaData.add(meta);
                                sb = new StringBuilder();
                            }
                            if(element != null && !element.isEmpty()){
                                sb.append(element).append(" ");
                            }
                        }
                        eventType = parser.next();
                    }
                    if(sb.length() > 0){
                        contents.add(sb.toString());
                        Map<String,String> meta = new HashMap<String,String>();
                        meta.put("sources",index + ":" + res.getHref());
                        meta.put("href", res.getHref());
                        meta.put("line_num", String.valueOf(parser.getLineNumber()));
                        metaData.add(meta);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
            addDocuments("system", contents, metaData);

        }catch(Exception e){
            e.printStackTrace();
        }
    } 
    
}
