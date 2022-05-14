package org.cs305.assignment_1;

import com.mysql.cj.util.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Query {
    public PreparedStatement stmt;
    public ResultSet rs;
    public ResultSetMetaData metaData;
    public String query;

    // select query run
    public void run() throws Exception{
        this.rs = this.stmt.executeQuery();
        this.metaData = this.rs.getMetaData();
    }

    // update, insert, delete
    public int runUpdate() throws Exception{
        int affected_rows = 0;
        affected_rows = this.stmt.executeUpdate();
        return affected_rows;
    }

    // for printing table
    public void run(String query, Connection conn) throws Exception{
        this.query = query;
        this.stmt = conn.prepareStatement(query);
        this.rs = this.stmt.executeQuery();
        this.metaData = this.rs.getMetaData();
    }

    // close query object
    public void close() {
        if (this.rs != null) {
            try {
                this.rs.close();
            } catch (SQLException sqlEx) {
            }
            this.rs = null;
        }

        if (this.stmt != null) {
            try {
                this.stmt.close();
            } catch (SQLException sqlEx) {
            }
            this.stmt = null;
        }
    }

    /*
     * @return: 0 if scalar, 1 if array, 2 if collection
     *
     * */
    private <T> int getType(T queryParam) {
        int type = 0;
        try {
            if (queryParam instanceof Collection<?>) {
                type = 2;
            } else {
                if (queryParam.getClass().isArray()) {
                    type = 1;
                } else {
                    type = 0;
                }
            }
        } catch (Exception e) {
            if (queryParam.getClass().isArray()) {
                type = 1;
            } else {
                type = 0;
            }
        }
        return type;
    }

    // parse xml and store query in stmt
    public <T> String getQueryFromXML(String path, String id, T queryParam, Connection conn) throws Exception {
        File xmlFile = new File(path);
        String paramType = "";
        // get parameter type of object
        if (queryParam != null) {
            paramType = queryParam.getClass().getName();
            if(paramType.charAt(paramType.length()-1)==';'){
                paramType = paramType.substring(0, paramType.length()-1);
            }
            System.out.println("Type of parameter: "+paramType);
        }
        // parse XML file
        String query = null;
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(xmlFile);

        Element rootElement = document.getRootElement();
        List<Element> queryList = rootElement.getChildren();

        // find query ID in list of queries
        boolean queryFound = false;
        for (Element E : queryList) {
            String q_id = ""+E.getAttributeValue("id");
            String q_paramType = ""+E.getAttributeValue("paramType");
            if (id.equals(q_id)) {
                if (!paramType.equals(q_paramType)) {
                    throw new Exception("Invalid paramType");
                }
                query = E.getTextNormalize();
                queryFound = true;
                break;
            }
        }

        if (!queryFound) {
            throw new Exception("Query with id: \"" + id + "\" not found.");
        }

        // process query and supply
        if (queryParam != null) {
            int dataType = getType(queryParam);

                // if it is a scalar or POJO
                if (dataType == 0) {
                    String type = paramType.substring(0, 5);
                    if(type.equals("java.")){
                        int start = 0;
                        int count = 0;
                        // replace ${}
                        while ((start = StringUtils.indexOfIgnoreCase(0, query, "${")) != -1) {
                            int end = StringUtils.indexOfIgnoreCase(start, query, "}");
                            query = query.replace(query.substring(start, end + 1), "?");
                            count++;
                        }
                        // set query params
                        this.stmt = conn.prepareStatement(query);
                        for (int i = 1; i <= count; i++) {
                            this.stmt.setObject(i, queryParam);
                        }
                    }
                    else{
                        List<String> vars=new ArrayList<>();
                        int start = 0;
                        int count = 0;
                        // replace ${}
                        while ((start = StringUtils.indexOfIgnoreCase(0, query, "${")) != -1) {
                            int end = StringUtils.indexOfIgnoreCase(start, query, "}");
                            vars.add(query.substring(start+2, end));
                            query = query.replace(query.substring(start, end + 1), "?");
                            count++;
                        }
                        // set query params
                        this.stmt = conn.prepareStatement(query);
                        for(int i=0;i<count;i++){
                            this.stmt.setObject(i+1, queryParam.getClass().getField(vars.get(i)).get(queryParam));
                        }
                    }
                } else if (dataType == 1) { // if it is an array
                    int start = 0;
                    int array_length = Array.getLength(queryParam);

                    int i = 0;
                    // replace ${}
                    while ((start = StringUtils.indexOfIgnoreCase(0, query, "${")) != -1) {
                        if (i >= array_length) {
                            throw new Exception("Insufficient parameters!");
                        }
                        int end = StringUtils.indexOfIgnoreCase(start, query, "}");
                        query = query.substring(0, start) + "?" + query.substring(end + 1);
                        i++;
                    }

                    // replace params
                    this.stmt = conn.prepareStatement(query);
                    for (int j = 1; j <= i; j++) {
                        this.stmt.setObject(j, Array.get(queryParam, j - 1));
                    }
                } else { // if it is a List collection
                    int start = 0;
                    List<?> temp_queryParam = (List<?>) queryParam;
                    int count = 0;
                    // replace ${}
                    for (Object param : temp_queryParam) {
                        if ((start = StringUtils.indexOfIgnoreCase(0, query, "${")) != -1) {
                            int end = StringUtils.indexOfIgnoreCase(start, query, "}");
                            query = query.substring(0, start) + "?" + query.substring(end + 1);
                            count++;
                        }
                    }
                    // set query params
                    this.stmt = conn.prepareStatement(query);
                    int iter = 0;
                    for (Object ob : temp_queryParam) {
                        if (iter >= count) {
                            break;
                        }
                        this.stmt.setObject(iter + 1, ob);
                        iter++;
                    }

                }
        }
        else{
            this.stmt = conn.prepareStatement(query);
        }
        this.query = query;
        return query;
    }
}