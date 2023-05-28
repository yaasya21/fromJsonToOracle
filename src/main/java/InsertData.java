import org.json.JSONArray;
import org.json.JSONObject;
import java.io.*;
import java.sql.*;
import java.util.zip.ZipFile;

public class InsertData {
    public static void main(String[] args) throws Exception {
        // Establishing a connection to the Oracle database
        // Write your data here
        String url = "jdbc:oracle:thin:@18.133.58.218:15215/XEPDB1";
        String user = "student_in01_04";
        String password = "student";
        String zipFilePath = "archive.zip";
        Connection connection = null;
        PreparedStatement metadataStatement = null;
        PreparedStatement networksStatement = null;

        try {
            // Load the Oracle JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Establish the database connection
            connection = DriverManager.getConnection(url, user, password);

            // Prepare the SQL statement for metadata insertion
            String metadataSql = "INSERT INTO networks_metadata (instance, startDate, uuid) VALUES (?, TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS.FF3'), ?)";
            metadataStatement = connection.prepareStatement(metadataSql);

            // Prepare the SQL statement for networks insertion
            String networksSql = "INSERT INTO networks (uuid, SSID, capabilities, status, security, debug, \"level\", BSSID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            networksStatement = connection.prepareStatement(networksSql);

            // Open the zip file
            ZipFile zipFile = new ZipFile(zipFilePath);

            // Iterate over the zip entries
            PreparedStatement finalMetadataStatement = metadataStatement;
            PreparedStatement finalNetworksStatement = networksStatement;
            zipFile.stream().forEach(entry -> {
                try {
                    // Read the content of the zip entry
                    InputStream inputStream = zipFile.getInputStream(entry);
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(reader);

                    // Read the JSON content
                    StringBuilder jsonContent = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonContent.append(line);
                    }

                    // Parse the JSON structure
                    JSONObject jsonObject = new JSONObject(jsonContent.toString());

                    // Insert metadata into networks_metadata table
                    String instance = jsonObject.getString("instance");
                    String startDate = jsonObject.getString("startDate");
                    String uuid = jsonObject.getString("uuid");
                    finalMetadataStatement.setString(1, instance);
                    finalMetadataStatement.setString(2, startDate);
                    finalMetadataStatement.setString(3, uuid);
                    finalMetadataStatement.executeUpdate();

                    // Insert network details into networks table
                    JSONArray networksArray = jsonObject.getJSONArray("networks");
                    for (int i = 0; i < networksArray.length(); i++) {
                        JSONObject networkObj = networksArray.getJSONObject(i);
                        String ssid = networkObj.getString("SSID");
                        String capabilities = networkObj.getString("capabilities");
                        String status = networkObj.getString("status");
                        String security = networkObj.getString("security");
                        String debug = networkObj.getString("debug");
                        String level = networkObj.getString("level");
                        String bssid = networkObj.getString("BSSID");
                        finalNetworksStatement.setString(1, uuid);
                        finalNetworksStatement.setString(2, ssid);
                        finalNetworksStatement.setString(3, capabilities);
                        finalNetworksStatement.setString(4, status);
                        finalNetworksStatement.setString(5, security);
                        finalNetworksStatement.setString(6, debug);
                        finalNetworksStatement.setString(7, level);
                        finalNetworksStatement.setString(8, bssid);
                        finalNetworksStatement.executeUpdate();
                    }

                    // Close the resources
                    bufferedReader.close();
                    reader.close();
                    inputStream.close();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            });

            // Close the zip file
            zipFile.close();

            System.out.println("Data insertion completed successfully.");
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            // Close the database connection and prepared statements
            try {
                if (metadataStatement != null) {
                    metadataStatement.close();
                }
                if (networksStatement != null) {
                    networksStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}