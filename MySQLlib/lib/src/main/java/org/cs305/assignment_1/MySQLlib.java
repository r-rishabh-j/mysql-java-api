package org.cs305.assignment_1;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

public class MySQLlib implements SqlRunner {
    private Connection conn = null;
    private String userName = null;
    private String password = null;
    private String url = null;
    private String xmlCommandPath = "queries.xml";
    public boolean autoCommit = true;

    // reference: https://www.w3resource.com/mysql/mysql-java-connection.php

    /**
     * @param : String userName
     * @param : String password
     * @param : String url
     *          initiates connection to the database
     */
    public void initConnection(String userName, String password, String url) {
        System.out.println("\n\n *********** Connecting to MySQL Database ***********");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, userName, password);
            conn.setAutoCommit(autoCommit);
            System.out.println("\nDatabase Connection Established at url: " + url + " as " + userName);
        } catch (Exception exp) {
            System.err.println("Cannot connect to database server.");
            exp.printStackTrace();
        }
    }

    /**
     * closes connection to database
     */
    public void closeConnection() {
        if (conn != null) {
            try {
                System.out.println("\n***** Terminating Connection *****");
                conn.close();
                conn = null;
                System.out.println("\nDatabase connection terminated...");
            } catch (Exception exp) {
                System.out.println("Error in connection termination!");
            }
        }
    }

    /**
     * change default XML path
     */
    public void setXMLCommandPath(String xmlCommandPath) {
        this.xmlCommandPath = xmlCommandPath;
    }

    /**
     * Print the table column types and the table
     * */
    public void printTableColumnTypes(String table_name) {
        String query = "SELECT * FROM " + table_name + ";";
        Query q = new Query();
        q.query = query;
        try {
            q.run(query, this.conn);
            System.out.println("TABLE " + table_name);
            int count = q.metaData.getColumnCount();
            // print column datatype
            for (int i = 1; i <= count; i++) {
                String clsName = q.metaData.getColumnClassName(i);
                String Name = q.metaData.getColumnName(i);
                String t = q.metaData.getColumnTypeName(i);
                System.out.println("Name: " + Name + ", clsName: " + clsName + " " + " typeName: " + t);
            }
            // print tables
            while (q.rs.next()) {
                for (int i = 1; i <= count; i++) {
                    System.out.print(q.rs.getString(i) + ", ");
                }
                System.out.println();
            }
        } catch (Exception exp) {
            System.out.println(exp);
            q.close();
        } finally {
            q.close();
        }
    }

    /**
     * Executes a select query that returns a single or no record.
     *
     * @param queryId    Unique ID of the query in the queries.xml file.
     * @param queryParam Parameter(s) to be used in the query.
     * @param resultType Type of the object that will be returned after
     *                   populating it with the data returned by the SQL.
     * @return The object populated with the SQL results.
     */

    public <T, R> R selectOne(String queryId, T queryParam, Class<R> resultType) {
        R resultObject;

        // instantiate
        try {
            resultObject = resultType.getConstructor().newInstance();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }

        Query query = new Query();
        // get query from xml file and put full generated string in query
        try {
            query.getQueryFromXML(this.xmlCommandPath, queryId, queryParam, conn);
            if (!query.query.trim().substring(0, 6).equalsIgnoreCase("select")) {
                throw new Exception("Incorrect SQL command");
            }
        } catch (Exception exp) {
            System.out.println(exp);
            exp.printStackTrace();
            query.close();
            return null;
        }
        // runQuery and put results in object
        try {
            // run query
            query.run();
            int count = query.metaData.getColumnCount();
            if (query.rs.next()) {
                for (int i = 1; i <= count; i++) {
                    String clsName = query.metaData.getColumnClassName(i);
                    // set field
                    resultObject.getClass().getField(query.metaData.getColumnName(i)).set(resultObject, query.rs.getObject(i, Class.forName(clsName)));
                }
                if (query.rs.next()) {
                    throw new Exception("Query returns multiple rows! Use selectMany()");
                }
            }
            else {
                for (int i = 1; i <= count; i++) {
                    // set field
                    try {
                        resultObject.getClass().getField(query.metaData.getColumnName(i)).set(resultObject, null);
                    } catch (Exception e) {
                        if ((e instanceof NoSuchFieldException) || (e instanceof SecurityException)) {
                            System.out.println(e);
                            return null;
                        }
                    }
                }
            }
        } catch (Exception exp) {
            System.out.println(exp);
            exp.printStackTrace();
            query.close();
            return null;
        } finally {
            query.close();
        }
        return resultObject;
    }

    /**
     * Same as {@link #selectOne(String, Object, Class)} except that this one
     * returns multiple rows.
     *
     * @param queryId
     * @param queryParam
     * @param resultItemType
     * @return
     */
    public <T, R> List<R> selectMany(String queryId, T queryParam, Class<R> resultItemType) {
        List<R> resultObject = new ArrayList<>();

        Query query = new Query();
        // get query from xml file and put full generated string in query
        try {
            query.getQueryFromXML(this.xmlCommandPath, queryId, queryParam, conn);
            if (!query.query.trim().substring(0, 6).equalsIgnoreCase("select")) {
                throw new Exception("Incorrect SQL command");
            }
        } catch (Exception exp) {
            System.out.println(exp);
            exp.printStackTrace();
            query.close();
            return null;
        }
        // runQuery and put results in object
        try {
            query.run();

            int count = query.metaData.getColumnCount();
            while (query.rs.next()) {
                R row = resultItemType.getConstructor().newInstance();
                for (int i = 1; i <= count; i++) {
                    String clsName = query.metaData.getColumnClassName(i);
                    row.getClass().getField(query.metaData.getColumnName(i)).set(row, query.rs.getObject(i, Class.forName(clsName)));
                }
                resultObject.add(row);
            }
        } catch (Exception exp) {
            System.out.println(exp);
            exp.printStackTrace();
            query.close();
            return null;
        } finally {
            query.close();
        }
        return resultObject;
    }

    /**
     * Execute an update statement and return the number of rows affected.
     *
     * @param queryId
     * @param queryParam
     * @return
     */
    public <T> int update(String queryId, T queryParam) {
        Query query = new Query();
        int rowsAffected = 0;
        // get query from xml file and put full generated string in query
        try {
            query.getQueryFromXML(this.xmlCommandPath, queryId, queryParam, conn);
            if (!query.query.trim().substring(0, 6).equalsIgnoreCase("update")) {
                throw new Exception("Incorrect SQL command");
            }
            rowsAffected = query.runUpdate();
        } catch (Exception exp) {
            System.out.println(exp);
            exp.printStackTrace();
            query.close();
            return -1;
        }
        return rowsAffected;
    }

    /**
     * Execute an insert statement and return the number of rows affected.
     *
     * @param queryId
     * @param queryParam
     * @return
     */
    public <T> int insert(String queryId, T queryParam) {
        Query query = new Query();
        int rowsAffected = 0;
        // get query from xml file and put full generated string in query
        try {
            query.getQueryFromXML(this.xmlCommandPath, queryId, queryParam, conn);
            if (!query.query.trim().substring(0, 6).equalsIgnoreCase("insert")) {
                throw new Exception("Incorrect SQL command");
            }
            rowsAffected = query.runUpdate();
        } catch (Exception exp) {
            System.out.println(exp);
            exp.printStackTrace();
            query.close();
            return -1;
        }
        return rowsAffected;
    }

    /**
     * Execute a delete statement and return the number of rows affected.
     *
     * @param queryId
     * @param queryParam
     * @return
     */
    public <T> int delete(String queryId, T queryParam) {
        Query query = new Query();
        int rowsAffected = -1;
        // get query from xml file and put full generated string in query
        try {
            query.getQueryFromXML(this.xmlCommandPath, queryId, queryParam, conn);
            if (!query.query.trim().substring(0, 6).equalsIgnoreCase("delete")) {
                throw new Exception("Incorrect SQL command");
            }
            rowsAffected = query.runUpdate();
        } catch (Exception exp) {
            System.out.println(exp);
            exp.printStackTrace();
            query.close();

            return -1;
        }
        return rowsAffected;
    }
}
