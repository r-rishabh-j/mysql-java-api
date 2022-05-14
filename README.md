### Java MySQL API to execute CRUD operations.

### What does this program do?
This is a MySQL java API designed to execute CRUD operations. The queries are given in an XML file, from which they
are read and executed.

### How this program works
1. Queries supplied in the XML file have unique ids which, which is supplied to an appropriate API function.
2. The user supplies appropriate parameters which replace the placeholder parameters in the SQL queries in the XML.
3. Java JDBC API along with MySQL Connector/J are utilised to send a request to the database and retrieve results.


### A. Building the library:
1. Library project is present in the directory `/MySQLlib` directory.
2. To build the library to a JAR file, run the command `gradle build` in the project root folder of MySQLlib project.
3. The jar file will be formed in `MySQLlib/lib/build/libs` directory as `lib.jar`.

### B. Importing the library to another Java application.
1. Create a separate Java application using `gradle init` command and select the application to
be a java app(important!).(Select app when prompted by gradle for the type of the project)
2. Select build script DSL to be kotlin.
3. Add the following dependencies to the app level `build.gradle.kts` file:
```kotlin
implementation(rootProject.files("<relative path to library jar file created in step A>"))
implementation("org.jdom:jdom2:2.0.6.1")
implementation("mysql:mysql-connector-java:8.0.26")
```
The relative path to library jar file has to be from the root folder of the application.
4. Select an appropriate package name. It should NOT start with `java.lang`(which is anyway reserved for Java builtins). This would lead to unsupported behaviours.

### C. Placement of `queries.xml` file
Place the `queries.xml` file in the /app folder of the Java application in which the library has been imported.
The path of the file can also be changed by calling appropriate API function mentioned below.

### D. Build and run
These instructions are to build the library and present an example of use of this library in another java application. 
For a successful build, the unit tests need to be passed first. *Make sure that the sakila database is 
in it's original state given in <a href="https://dev.mysql.com/doc/sakila/en">https://dev.mysql.com/doc/sakila/en/, otherwise unit tests might fail(output validation has been performed in the tests. Change in data will lead to change in output)*. It is recommended
to set autoCommit to false while experimenting with the library. The same has been done in unit tests to avoid changes to data in database.
1. Build the jar file of the library by going into the library folder `/MySQLlib` and running `gradle build`.

### E. Unit testing
1. JUnit-Jupiter is used for unit testing of the code.
2. Unit tests are present in the `/MySQLlib/src/main/test` directory.
3. To execute tests, go into the `/MySQLlib` folder and execute `gradle test`

### F. API documentation
Given in API_doc.pdf in the folder.
