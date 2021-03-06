/**********************************************************************************************************************
 Copyright [2014] [Chunqi SHI (chunqi.shi@hotmail.com)]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************************************************/
package edu.brandeis.cs.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.brandeis.cs.json.JsonProxy.JsonArray;
import edu.brandeis.cs.json.JsonProxy.JsonObject;

import java.io.IOException;
import java.util.*;

/**
 * <p> Jackson Json provides best implementation of Json functions</p>
 * <a href="https://github.com/FasterXML">Jackson Json</a>.
 *
 */
public class JacksonJsonProxy implements JsonProxy.NewProxy {


    public static Object valueOf(Object obj) {
        if (obj instanceof  JacksonJsonObject) {
            return ((JacksonJsonObject) obj).original();
        } else if (obj instanceof JacksonJsonArray) {
            return ((JacksonJsonArray) obj).original();
        }
        return obj;
    }


    public static Object value2object(Object obj ) {
        if(obj instanceof List) {
            return new JacksonJsonArray((List)obj);
        } else if(obj instanceof Map) {
            return new JacksonJsonObject((Map)obj);
        }
        return obj;
    }


    public JsonArray newArray() {
        return new JacksonJsonArray();
    }

    
    public JsonObject newObject() {
        return new JacksonJsonObject();
    }


    public static class JacksonJsonObject implements JsonObject {

        Map<String, Object> map = null;

        public JacksonJsonObject() {
            map = new LinkedHashMap<String, Object>();
        }

        public JacksonJsonObject(Map<String, Object> map) {
            this.map = map;
        }

        
        public JsonObject read(String s) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                map = (Map)mapper.readValue(s, LinkedHashMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

        
        public boolean has(String key) {
            return map.containsKey(key);
        }

        
        public Object get(String key) {
            return value2object(map.get(key));
        }

        
        public JsonObject put(String key, Object val) {
            map.put(key, valueOf(val));
            return this;
        }

        
        public JsonObject remove(String key) {
            map.remove(key);
            return this;
        }

        
        public int length() {
            return map.size();
        }

        
        public Collection<String> keys() {
            return map.keySet();
        }

        
        public Object original() {
            return map;
        }

//        
//        public JsonObject clone() {
//            return new JacksonJsonObject(new LinkedHashMap<String, Object>(map));
//        }

        
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this.map);
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


    public static class JacksonJsonArray implements JsonArray{

        List<Object> list = null;

        public JacksonJsonArray (List<Object> list) {
            this.list = list;
        }

        public JacksonJsonArray () {
            list = new ArrayList<Object>();
        }
        
        public JsonArray read(String s) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                list = (List<Object>) mapper.readValue(s, List.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return this;
        }

        
        public int length() {
            return list.size();
        }

        
        public Object get(int i) {
            return value2object(list.get(i));
        }

        
        public JsonArray add(Object s) {
            list.add(valueOf(s));
            return this;
        }

        
        public JsonArray remove(int i) {
            list.remove(i);
            return this;
        }

        
        public JsonArray set(int i, Object obj) {
            list.set(i, obj);
            return this;
        }

        
        public JsonArray convert(String[] arr) {
            for(String s: arr) {
                list.add(s);
            }
            return this;
        }

        
        public JsonArray convert(Collection<String> arr) {
            for(String s: arr) {
                list.add(s);
            }
            return this;
        }

//
//        public JsonArray clone() {
//            return new JacksonJsonArray(new ArrayList<Object>(list));
//        }


        public Object original() {
            return list;
        }

        
        public String toString() {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.writeValueAsString(this.list);
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
