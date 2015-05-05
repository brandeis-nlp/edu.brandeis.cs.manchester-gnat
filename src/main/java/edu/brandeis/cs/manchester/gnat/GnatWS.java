package edu.brandeis.cs.manchester.gnat;

import edu.brandeis.cs.json.JsonProxy;
import edu.brandeis.cs.json2json.Json2Json;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.lappsgrid.api.WebService;
import org.lappsgrid.discriminator.Discriminators;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;


public class GnatWS implements WebService, IGnat {

    /**
     * http://gnat.sourceforge.net/
     * http://textmining.ls.manchester.ac.uk/biocontext/
     * http://textmining.ls.manchester.ac.uk:8081/?text=human%20p53%20protein&task=gnorm
     * @param s
     * @return
     * @throws Exception
     */
    public static String httpget(String s) throws Exception{
        URI uri = new URIBuilder()
                .setScheme("http")
                .setHost("textmining.ls.manchester.ac.uk")
                .setPort(8081)
                .setPath("/")
                .setParameter("text", s)
                .setParameter("returntype", "xml")
                .build();
        HttpGet httpget = new HttpGet(uri);
        System.out.println(httpget.getURI());
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response1 = httpclient.execute(httpget);
        try {
            System.out.println(response1.getStatusLine());
            HttpEntity entity = response1.getEntity();
            StringWriter writer = new StringWriter();
            IOUtils.copy(new InputStreamReader(entity.getContent()), writer);
            return writer.toString();
        } finally {
            response1.close();
        }
    }

    public static String getResource(String name) throws IOException{
        java.io.InputStream in =  GnatWS.class.getClassLoader().getResourceAsStream(name);
        return IOUtils.toString(in);
    }

    public String execute(String s) {
        try {
            JsonProxy.JsonObject json = null;
            JsonProxy.JsonObject payload = null;
            String txt = s;
            if (s.trim().startsWith("{")) {
                json = JsonProxy.newObject().read(s);
                String discriminator = json.get("discriminator").toString().trim();
                if (discriminator.equals(Discriminators.Uri.TEXT)) {
                    txt = json.get("payload").toString();
                    json.put("discriminator", Discriminators.Uri.JSON_LD);
                    payload = JsonProxy.newObject().put("text", JsonProxy.newObject().put("@value", txt));
                    json.put("payload", payload);
                } else if (discriminator.equals(Discriminators.Uri.JSON_LD)) {
                    payload = (JsonProxy.JsonObject) json.get("payload");
                    JsonProxy.JsonObject textobj = (JsonProxy.JsonObject) payload.get("text");
                    txt = (String) textobj.get("@value");
                }
            } else {
                json = JsonProxy.newObject();
                json.put("discriminator", Discriminators.Uri.JSON_LD);
                payload = JsonProxy.newObject().put("text", JsonProxy.newObject().put("@value", txt));
                json.put("payload", payload);
            }
            payload.put("@context", "http://vocab.lappsgrid.org/context-1.0.0.jsonld");
            String xml = httpget(s);

            System.out.println("\nXML : \n-----------------------\n"+ xml);
            String annotations =  Json2Json.xml2jsondsl(xml,
                    getResource("edu.brandeis.cs.manchester.gnat.GnatWS.dsl"));
            System.out.println("\nAnnotations : \n-----------------------\n"+ annotations);
            JsonProxy.JsonObject annsobj = JsonProxy.newObject().read(annotations);
            JsonProxy.JsonArray viewsobj = null;
            if (payload.has("views")) {
                viewsobj = (JsonProxy.JsonArray) payload.get("views");
            } else {
                viewsobj = JsonProxy.newArray();
                payload.put("views", viewsobj);
            }
            JsonProxy.JsonObject view = JsonProxy.newObject();
            JsonProxy.JsonObject contains = JsonProxy.newObject();
            contains.put(Discriminators.Uri.NE,JsonProxy.newObject().put("producer", this.getClass().getName() + ": 0.2.0")
            .put("type", "ner:gnat"));
            view.put("metadata", JsonProxy.newObject().put("contains", contains));
            view.put("annotations", annsobj.get("annotations"));
            viewsobj.add(view);
            return json.toString();
        }catch (Throwable th) {
            JsonProxy.JsonObject json = JsonProxy.newObject();
            json.put("discriminator", Discriminators.Uri.ERROR);
            JsonProxy.JsonObject error = JsonProxy.newObject();
            error.put("class", this.getClass().getName());
            error.put("message", th.getMessage());
            StringWriter sw = new StringWriter();
            th.printStackTrace( new PrintWriter(sw));
            System.err.println(sw.toString());
            error.put("stacktrace", sw.toString());
            json.put("payload", error);
            return json.toString();
        }
    }





    public String getMetadata() {
            // get caller name using reflection
            String name = this.getClass().getName();
            //
            String resName = "/metadata/"+ name +".json";
            System.out.println("load resources:" + resName);
            try {
                String meta = IOUtils.toString(this.getClass().getResourceAsStream(resName));
                JsonProxy.JsonObject json = JsonProxy.newObject();
                json.put("discriminator", Discriminators.Uri.META);
                json.put("payload", JsonProxy.newObject().read(meta));
                System.out.println("---------------------META:-------------------\n" + json.toString());
                return json.toString();
            }catch (Throwable th) {
                JsonProxy.JsonObject json = JsonProxy.newObject();
                json.put("discriminator", Discriminators.Uri.ERROR);
                JsonProxy.JsonObject error = JsonProxy.newObject();
                error.put("class", name);
                error.put("error", "NOT EXIST: " + resName);
                error.put("message", th.getMessage());
                StringWriter sw = new StringWriter();
                th.printStackTrace( new PrintWriter(sw));
                System.err.println(sw.toString());
                error.put("stacktrace", sw.toString());
                json.put("payload", error);
                return json.toString();
            }
    }

    public String getAnnotationXml(String s) throws Exception{
        return  httpget(s);
    }
}



