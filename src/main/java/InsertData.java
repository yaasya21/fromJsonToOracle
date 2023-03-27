import java.sql.*;

public class InsertData {
    public static void main(String[] args) throws Exception {
        Connection con = null;
        PreparedStatement insert = null;
        PreparedStatement createTable = null;
        PreparedStatement truncateStatement = null;
        PreparedStatement dropTable = null;

        // Establishing a connection to the Oracle database
        // Write your data here
        String url = "jdbc:oracle:thin:@18.133.58.218:15215/XEPDB1";
        String user = "student_in01_04";
        String password = "student";

        //Number of experiments
        int tryN = 35;

        // Setting values for the parameters in the SQL query
        try {
            // Establish connection to Oracle database
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(url, user, password);
            createTable = con.prepareStatement("CREATE TABLE TEST(" +
                                                    "VALUE1 VARCHAR(10)," +
                                                    "VALUE2 VARCHAR(10)," +
                                                    "VALUE3 VARCHAR(10)," +
                                                    "VALUE4 VARCHAR(10)," +
                                                    "VALUE5 VARCHAR(10)" +
                                                    ")");
            createTable.executeUpdate();
            // Creating a PreparedStatement object to execute SQL queries
            insert = con.prepareStatement("INSERT INTO TEST (TEST.VALUE1, TEST.VALUE2, TEST.VALUE3, TEST.VALUE4, TEST.VALUE5) VALUES (?, ?, ?, ?, ?)");
            truncateStatement = con.prepareStatement("TRUNCATE TABLE TEST");
            dropTable = con.prepareStatement("DROP TABLE TEST");

            long average = 0, startTime, endTime;
            // We will do 35 experiments
            for(int n = 0; n < tryN; n++) {
                // Set values for each record and adding that insert into batch
                for (int i = 0; i < 1; i++) {
                    insert.setString(1, "oracle");
                    insert.setString(2, "oracle");
                    insert.setString(3, "oracle");
                    insert.setString(4, "oracle");
                    insert.setString(5, "oracle");
                    insert.addBatch();
                }

                // Running of timer
                startTime = System.currentTimeMillis();
                // Execute batch insert
                int[] inserted = insert.executeBatch();
                endTime = System.currentTimeMillis();
                average += (endTime - startTime);

                // Truncate the table before new transaction
                truncateStatement.executeUpdate();
            }
            System.out.println("Average time taken: " + (average/tryN) + " miliseconds after " + tryN + " tries");
            dropTable.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close statements and connection
            try {
                if (createTable != null) createTable.close();
                if (insert != null) insert.close();
                if (truncateStatement != null) truncateStatement.close();
                if (dropTable != null) dropTable.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}