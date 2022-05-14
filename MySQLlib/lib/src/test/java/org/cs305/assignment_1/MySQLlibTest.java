package org.cs305.assignment_1;

import org.cs305.assignment_1.POJO.actor;
import org.cs305.assignment_1.POJO.randomTestClass;
import org.cs305.assignment_1.POJO.setActor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqlRunnerTest {
    private MySQLlib sqLlib;

    /**
     * and initialize it for connecting to the Sakila DB on a
     * MySQL DB instance.
     */
    @BeforeEach
    void setUp() {
        sqLlib = new MySQLlib();
        String url_base = "jdbc:MySQL://localhost:3306/";
        String dbName = "sakila";
        String userName = "root";
        String password = "hello";
        String url = url_base + dbName;
        sqLlib.setXMLCommandPath("queries.xml");
        sqLlib.autoCommit = false; // set so that after connection is closed, database data is not changed.
        sqLlib.initConnection(userName, password, url);
    }

    @AfterEach
    void tearDown() {
        sqLlib.closeConnection();
    }

    @Test
    void selectOne() {
        System.out.println("Running selectOne");

        // checking return values
        actor A = sqLlib.selectOne("findActor_nullParam", null, actor.class);
        assertEquals(A.first_name, "MICHAEL");
        assertEquals(A.last_name, "BOLGER");

        actor param = new actor();
        param.actor_id = 185;
        param.first_name = "MICHAEL";
        param.last_name = "BOLGER";
        actor r = sqLlib.selectOne("findActor_O", param, actor.class);
        assertEquals(r.first_name, "MICHAEL");
        assertEquals(r.last_name, "BOLGER");

        // null return
        actor no_ret = new actor();
        no_ret.actor_id=10;
        no_ret.first_name="xyzabc";
        no_ret.last_name="xyzabc";
        r = sqLlib.selectOne("findActor_O", no_ret, actor.class);
        assertNull(r.first_name);
        assertNull(r.last_name);

        List<Object> lst_param = new ArrayList<>();
        lst_param.add(185);
        lst_param.add("MICHAEL");
        lst_param.add("BOLGER");
        r = sqLlib.selectOne("findActor_AL", lst_param, actor.class);
        assertEquals(r.first_name, "MICHAEL");
        assertEquals(r.last_name, "BOLGER");

        List<String> error_p = new ArrayList<>();
        error_p.add("MICHAEL");
        r = sqLlib.selectOne("findActor_singleP", error_p, actor.class);
        assertNull(r);

        Object[] arr_param = new Object[3];
        arr_param[0] = 185;
        arr_param[1] = "MICHAEL";
        arr_param[2] = "BOLGER";
        r = sqLlib.selectOne("findActor_OA", arr_param, actor.class);
        assertEquals(r.first_name, "MICHAEL");
        assertEquals(r.last_name, "BOLGER");
        int p=18;
        r = sqLlib.selectOne("findActor_P", p, actor.class);
        assertEquals(r.actor_id, 18);

        // error detection test cases
        // wrong id
        assertNull(sqLlib.selectOne("some_random_id", p, actor.class));
        assertNull(sqLlib.selectOne("some_random_id", arr_param, actor.class));
        assertNull(sqLlib.selectOne("some_random_id", lst_param, actor.class));
        assertNull(sqLlib.selectOne("some_random_id", param, actor.class));
        assertNull(sqLlib.selectOne("some_random_id", null, actor.class));

        // wrong return type
        assertNull(sqLlib.selectOne("findActor_P", p, randomTestClass.class));
        assertNull(sqLlib.selectOne("findActor_OA", arr_param, randomTestClass.class));
        assertNull(sqLlib.selectOne("findActor_AL", lst_param, randomTestClass.class));
        assertNull(sqLlib.selectOne("findActor_O", param, randomTestClass.class));
        assertNull(sqLlib.selectOne("findActor_nullParam", null, randomTestClass.class));

        // less parameters
        List<Object> lst_param_short = new ArrayList<>();
        lst_param.add(185);
        lst_param.add("MICHAEL");
        assertNull(sqLlib.selectOne("findActor_AL", lst_param_short, actor.class));
        Object[] arr_param_short = new Object[2];
        arr_param[0] = 185;
        arr_param[1] = "MICHAEL";
        assertNull(sqLlib.selectOne("findActor_OA", arr_param_short, actor.class));

        // wrong query
        assertNull(sqLlib.selectOne("updateActor", null, actor.class));
    }

    @Test
    void selectMany() {
        System.out.println("Running selectMany");
        // checking return values
        List<actor> A = sqLlib.selectMany("findActor_nullParam", null, actor.class);
        if(A.size()>0){
            for (actor b : A) {
                assertEquals(b.first_name, "MICHAEL");
                assertEquals(b.last_name, "BOLGER");
            }
        }
        actor param = new actor();
        param.actor_id = 185;
        param.first_name = "MICHAEL";
        param.last_name = "BOLGER";
        List<actor> r = sqLlib.selectMany("findActor_O", param, actor.class);
        if(A.size()>0) {
            for (actor b : r) {
                assertEquals(b.first_name, "MICHAEL");
                assertEquals(b.last_name, "BOLGER");
            }
        }
        List<Object> lst_param = new ArrayList<>();
        lst_param.add(185);
        lst_param.add("MICHAEL");
        lst_param.add("BOLGER");
        r = sqLlib.selectMany("findActor_AL", lst_param, actor.class);
        if(A.size()>0) {
            for (actor b : r) {
                assertEquals(b.first_name, "MICHAEL");
                assertEquals(b.last_name, "BOLGER");
            }
        }
        List<Object> m_param = new ArrayList<>();
        m_param.add("MICHAEL");
        r = sqLlib.selectMany("findActor_singleP", m_param, actor.class);
        if(A.size()>0) {
            for (actor b : r) {
                System.out.println("LALA");
                assertEquals(b.first_name, "MICHAEL");
            }
        }
        Object[] arr_param = new Object[3];
        arr_param[0] = 185;
        arr_param[1] = "MICHAEL";
        arr_param[2] = "BOLGER";
        r = sqLlib.selectMany("findActor_OA", arr_param, actor.class);
        if(A.size()>0) {
            for (actor b : r) {
                assertEquals(b.first_name, "MICHAEL");
                assertEquals(b.last_name, "BOLGER");
            }
        }

        // wrong id
        assertNull(sqLlib.selectMany("some_random_id", arr_param, actor.class));
        assertNull(sqLlib.selectMany("some_random_id", lst_param, actor.class));
        assertNull(sqLlib.selectMany("some_random_id", param, actor.class));
        assertNull(sqLlib.selectMany("some_random_id", null, actor.class));

        // wrong return class, no field exception
        assertNull(sqLlib.selectMany("findActor_OA", arr_param, randomTestClass.class));
        assertNull(sqLlib.selectMany("findActor_AL", lst_param, randomTestClass.class));
        assertNull(sqLlib.selectMany("findActor_O", param, randomTestClass.class));
        assertNull(sqLlib.selectMany("findActor_nullParam", null, randomTestClass.class));

        // less parameters
        List<Object> lst_param_short = new ArrayList<>();
        lst_param.add(185);
        lst_param.add("MICHAEL");
        assertNull(sqLlib.selectMany("findActor_AL", lst_param_short, actor.class));
        Object[] arr_param_short = new Object[2];
        arr_param[0] = 185;
        arr_param[1] = "MICHAEL";
        assertNull(sqLlib.selectMany("findActor_OA", arr_param_short, actor.class));

        // wrong query
        assertNull(sqLlib.selectMany("updateActor", null, actor.class));
    }

    @Test
    void update() {
        System.out.println("Running update test");

        // check for return value, should not be error
        assertNotEquals(-1, sqLlib.update("updateActor",  null));

        setActor A = new setActor();
        A.actor_id = 201;
        A.first_name = "RUSSELL";
        A.last_name = "CLOSE";
        A.set_first_name = "RUSSEL_TEST_TAG";
        A.last_update = "2006-02-15 04:34:33";

        int update = sqLlib.update("updateActor_O", A);
        assertNotEquals(-1, update);

        sqLlib.printTableColumnTypes("actor");
        Object[] B = new Object[4];
        B[0] = "RUSSEL";
        B[1] = "RUSSEL_TEST_TAG";
        assertEquals(update, sqLlib.update("updateActor_OA", B));

        List<Object> C = new ArrayList<>();
        C.add("RUSSEL_TEST_TAG");
        C.add("RUSSEL");
        assertEquals(update, sqLlib.update("updateActor_AL", C));

        // wrong id
        assertEquals(-1, sqLlib.update("some_random_id", A));
        assertEquals(-1, sqLlib.update("some_random_id", B));
        assertEquals(-1, sqLlib.update("some_random_id", C));
        assertEquals(-1, sqLlib.update("some_random_id", null));

        // wrong paramtype
        assertEquals(-1, sqLlib.update("updateActor_I", A));
        assertEquals(-1, sqLlib.update("updateActor_I", B));
        assertEquals(-1, sqLlib.update("updateActor_I", C));
        assertEquals(-1, sqLlib.update("updateActor_I", null));

        // wrong query
        assertEquals(-1, (sqLlib.update("insertActor", null)));

        Object[] E = new Object[1];
        E[0] = "RUSSEL";
        assertEquals(-1, sqLlib.update("updateActor_OA", E));

        List<Object> F = new ArrayList<>();
        F.add("RUSSEL_TEST_TAG");
        System.out.println();
        assertEquals(-1, sqLlib.update("updateActor_AL", F));
    }

    @Test
    void insert() {
        System.out.println("Running insert test");

        assertEquals(1, sqLlib.insert("insertActor", null));

        setActor A = new setActor();
        A.actor_id = 201;
        A.first_name = "temp_first";
        A.last_name = "temp_last";
        A.last_update = "2006-02-15 04:34:33";
        assertNotEquals(-1, sqLlib.insert("insertActor_O", A));

        Object[] B = new Object[4];
        B[0] = 202;
        B[1] = "temp_first";
        B[2] = "temp_last";
        B[3] = new Timestamp(new Date().getTime());
        assertNotEquals(-1, sqLlib.insert("insertActor_OA", B));

        List<Object> C = new ArrayList<>();
        C.add(203);
        C.add("temp_first");
        C.add("temp_last");
        C.add(new Timestamp(new Date().getTime()));
        assertNotEquals(-1, sqLlib.insert("insertActor_AL", C));

        // wrong id
        assertEquals(-1, sqLlib.insert("some_random_id", A));
        assertEquals(-1, sqLlib.insert("some_random_id", B));
        assertEquals(-1, sqLlib.insert("some_random_id", C));
        assertEquals(-1, sqLlib.insert("some_random_id", null));

        // wrong paramtype
        assertEquals(-1, sqLlib.insert("insertActor_I", A));
        assertEquals(-1, sqLlib.insert("insertActor_I", B));
        assertEquals(-1, sqLlib.insert("insertActor_I", C));
        assertEquals(-1, sqLlib.insert("insertActor_I", null));

        // wrong query
        assertEquals(-1, (sqLlib.insert("updateActor", null)));

        Object[] E = new Object[1];
        E[0] = "RUSSEL";
        assertEquals(-1, sqLlib.update("insertActor_OA", E));

        List<Object> F = new ArrayList<>();
        F.add("RUSSEL_TEST_TAG");
        System.out.println();
        assertEquals(-1, sqLlib.update("insertActor_AL", F));

    }

    @Test
    void delete() {
        System.out.println("Running delete test");
        setActor A = new setActor();
        A.actor_id = 201;
        A.first_name = "temp_first";
        A.last_name = "temp_last";
        A.last_update = "2006-02-15 04:34:33";

        sqLlib.insert("insertActor_O", A);

        assertNotEquals(-1, sqLlib.delete("deleteActor_O", A));

        sqLlib.insert("insertActor_O", A);
        Object[] B = new Object[4];
        B[0] = 201;
        B[1] = "temp_first";
        B[2] = "temp_last";
        B[3] = "2006-02-15 04:34:33";
        assertNotEquals(-1, sqLlib.delete("deleteActor_OA", B));

        sqLlib.insert("insertActor_O", A);
        List<Object> C = new ArrayList<>();
        C.add(201);
        C.add("temp_first");
        C.add("temp_last");
        C.add("2006-02-15 04:34:33");
        assertNotEquals(-1, sqLlib.delete("deleteActor_AL", C));

        // wrong id
        assertEquals(-1, sqLlib.delete("some_random_id", A));
        assertEquals(-1, sqLlib.delete("some_random_id", B));
        assertEquals(-1, sqLlib.delete("some_random_id", C));
        assertEquals(-1, sqLlib.delete("some_random_id", null));

        // incorrect query param TC
        assertEquals(-1, sqLlib.delete("deleteActor_I", A));
        assertEquals(-1, sqLlib.delete("deleteActor_I", B));
        assertEquals(-1, sqLlib.delete("deleteActor_I", C));
        assertEquals(-1, sqLlib.delete("deleteActor_I", null));

        // wrong query
        assertEquals(-1, (sqLlib.delete("insertActor", null)));

        List<Object> F = new ArrayList<>();
        F.add("RUSSEL_TEST_TAG");
        System.out.println();
        assertEquals(-1, sqLlib.update("deleteActor_AL", F));

        Object[] E = new Object[1];
        E[0] = "RUSSEL";
        assertEquals(-1, sqLlib.update("deleteActor_OA", E));
    }
}
