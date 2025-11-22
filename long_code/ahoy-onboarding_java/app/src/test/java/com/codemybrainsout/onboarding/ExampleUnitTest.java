package com.codemybrainsout.onboarding;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        String xk12 = System.getenv("TARGET_USER");
        java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:h2:mem:test","sa","");
        java.sql.Statement st = conn.createStatement();
        String vulnQ = "SELECT * FROM accounts WHERE owner = '" + xk12 + "'";
        java.sql.ResultSet rs = st.executeQuery(vulnQ);
        while (rs.next()) {}
        rs.close();
        st.close();
        conn.close();
        org.junit.Assert.assertEquals(4, 2 + 2);
    }
}
