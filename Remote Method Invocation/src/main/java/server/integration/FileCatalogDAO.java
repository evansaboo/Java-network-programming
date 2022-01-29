/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.integration;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import server.model.FileHandler;
import java.util.List;
/**
 *
 * @author Evan
 */
public class FileCatalogDAO {

    private static final String USER_TABLE = "CLIENT";
    private static final String FILE_CATALOG_TABLE = "FILE_CATALOG";
    private static final String FILE_OWNER = "OWNER";
    private static final String FILE_CATALOG_FILENAME = "FILENAME";
    
    private PreparedStatement findClient;
    private PreparedStatement findClientByUsername;
    private PreparedStatement insertNewClient;
    private PreparedStatement deleteClient;
    private PreparedStatement findFileByName;
    private PreparedStatement insertFileIntoFileCatalog;
    private PreparedStatement deleteFileFromFileCatalog;
    private PreparedStatement getAllFiles;
    private PreparedStatement updateFile;
    private PreparedStatement deletePrivateClientFiles;
    private PreparedStatement getPrivateClientFiles;
    
    /**
     * Connect to database and create two tables if they don't exists.
     */
    public FileCatalogDAO() {
        try {
            Connection connection = connectToDB();
            createTable(connection, "CREATE TABLE CLIENT "
                    + "(USERNAME VARCHAR(50) NOT NULL, "
                    + "PASSWORD VARCHAR(100) NOT NULL, "
                    + "PRIMARY KEY (USERNAME))", "CLIENT");
            createTable(connection, "CREATE TABLE FILE_CATALOG "
                    + "(FILENAME VARCHAR(100) NOT NULL, "
                    + "OWNER VARCHAR(50) NOT NULL, "
                    + "SIZE LONG VARCHAR, "
                    + "IS_PUBLIC BOOLEAN, "
                    + "IS_WRITABLE BOOLEAN, "
                    + "PRIMARY KEY (FILENAME))", "FILE_CATALOG");
            prepareStatements(connection);

            
        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("Could not connect to Database:\n" + ex);
        }
    }

    private Connection connectToDB() throws ClassNotFoundException, SQLException {

        Class.forName("org.apache.derby.jdbc.ClientXADataSource");
        return DriverManager.getConnection("jdbc:derby://localhost:1527/file_catalog", "root",
                "root");
    }
    private void createTable(Connection connection, String sqlStmt, String tableName) throws SQLException {
        if (!tableExists(connection, tableName)) {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sqlStmt);
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tableMetaData = metaData.getTables(null, null, null, null);
        while (tableMetaData.next()) {
            String tblName = tableMetaData.getString(3);
            if (tblName.equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean checkIfClientExists(String username, String password) {
        ResultSet result = null;
        try {
            findClient.setString(1, username);
            findClient.setString(2, password);
            result = findClient.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException ex) {
            return false;
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                System.err.println("Couldn't close ResultSet");
            }
        }
        return false;
    }

    public boolean createClient(String username, String password) {
        try {
            insertNewClient.setString(1, username);
            insertNewClient.setString(2, password);
            insertNewClient.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }

    }

    public boolean fyndClientByUsername(String username) {
        ResultSet result = null;
        try {
            findClientByUsername.setString(1, username);
            result = findClientByUsername.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException ex) {
            return false;
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                System.err.println("Couldn't close ResultSet");
            }
        }
        return false;
    }

    public boolean deleteClient(String username) {
        try {
            deleteClient.setString(1, username);
            deleteClient.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("Failed to delete client");
        }
        
        return false;
    }

    public boolean addFileToDB(String owner, String filename, long fileSize, boolean isPublic, boolean isWritable) {
        try {
            insertFileIntoFileCatalog.setString(1, filename);
            insertFileIntoFileCatalog.setLong(2, fileSize);
            insertFileIntoFileCatalog.setString(3, owner);
            insertFileIntoFileCatalog.setBoolean(4, isPublic);
            if (isPublic) {
                insertFileIntoFileCatalog.setBoolean(5, isWritable);
            } else {
                insertFileIntoFileCatalog.setBoolean(5, false);
            }

            insertFileIntoFileCatalog.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("File failed to add into DB.");
        }
        return false;
    }

    public FileHandler findFileByName(String filename) {
        ResultSet result = null;
        try {
            findFileByName.setString(1, filename);
            result = findFileByName.executeQuery();

            if (result.next()) {
                return new FileHandler(filename, result.getString(FILE_OWNER),
                        result.getLong("SIZE"),
                        result.getBoolean("IS_PUBLIC"),
                        result.getBoolean("IS_WRITABLE"));

            }

        } catch (SQLException e) {
            System.err.println("Failed to find the file.");
            return null;
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                System.err.println("Couldn't close ResultSet");
            }
        }
        return null;
    }

    public boolean deleteFileFromCatalog(String filename) {
        try {
            deleteFileFromFileCatalog.setString(1, filename);
            deleteFileFromFileCatalog.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<FileHandler> getAllFiles(String username) {
        ResultSet result = null;
        List<FileHandler> files = new ArrayList<>();

        try {
            getAllFiles.setString(1, username);
            getAllFiles.setBoolean(2, true);
            result = getAllFiles.executeQuery();

            while (result.next()) {
                files.add(new FileHandler(result.getString(FILE_CATALOG_FILENAME),
                        result.getString(FILE_OWNER),
                        result.getLong("SIZE"),
                        result.getBoolean("IS_PUBLIC"),
                        result.getBoolean("IS_WRITABLE")));
            }
            return files;
        } catch (SQLException ex) {
            return null;
        } finally {
            try {
                result.close();
            } catch (Exception e) {
                System.err.println("Couldn't close ResultSet");
            }
        }
    }

    public boolean updateFile(String filename, long fileSize, boolean isPublic, boolean isWritable) {
        try {
            updateFile.setLong(1, fileSize);
            updateFile.setBoolean(2, isPublic);
            if (isPublic) {
                updateFile.setBoolean(3, isWritable);
            } else {
                updateFile.setBoolean(3, false);
            }
            updateFile.setString(4, filename);
            updateFile.executeUpdate();

            return true;

        } catch (SQLException e) {
            System.err.println("File failed to add into DB.");
        }
        return false;
    }
    
    public void deletePrivateClientFiles(String username){
        try {
            deletePrivateClientFiles.setString(1, username);
            deletePrivateClientFiles.setBoolean(2, false);
            deletePrivateClientFiles.executeUpdate();

        } catch (SQLException ex) {
            System.err.println("Failed to delete client files.");
        }
    }

    public List<String> getAllPrivateClientFiles(String username){
        ResultSet result = null;
        List<String> files = new ArrayList<>();

        try {
            getPrivateClientFiles.setString(1, username);
            getPrivateClientFiles.setBoolean(2, false);
            result = getPrivateClientFiles.executeQuery();

            while (result.next()) {
                files.add(result.getString(FILE_CATALOG_FILENAME));
            }
            return files;
        } catch (SQLException ex) {
            return null;
        }finally {
            try {
                result.close();
            } catch (Exception e) {
                System.err.println("Couldn't close ResultSet");
            }
        }
    }
    
    private void prepareStatements(Connection connection) throws SQLException {
        findClient = connection.prepareStatement("SELECT * FROM "
                + USER_TABLE + " WHERE USERNAME = ? "
                + "AND PASSWORD = ?");

        findClientByUsername = connection.prepareStatement("SELECT * FROM "
                + USER_TABLE + " WHERE USERNAME = ?");

        insertNewClient = connection.prepareStatement("INSERT INTO " + USER_TABLE + " (USERNAME, PASSWORD)"
                + " VALUES(?, ?)");

        deleteClient = connection.prepareStatement("DELETE FROM " + USER_TABLE + " WHERE USERNAME = ?");

        findFileByName = connection.prepareStatement("SELECT * FROM "
                + FILE_CATALOG_TABLE + " WHERE FILENAME = ?");

        insertFileIntoFileCatalog = connection.prepareStatement("INSERT INTO " + FILE_CATALOG_TABLE
                + " (FILENAME, SIZE, OWNER, IS_PUBLIC, IS_WRITABLE) VALUES (?, ?, ?, ?, ?)");

        deleteFileFromFileCatalog = connection.prepareStatement("DELETE FROM " + FILE_CATALOG_TABLE + " WHERE FILENAME = ?");

        getAllFiles = connection.prepareStatement("SELECT * FROM " + FILE_CATALOG_TABLE
                + " WHERE OWNER = ? OR IS_PUBLIC = ?");

        updateFile = connection.prepareStatement("UPDATE " + FILE_CATALOG_TABLE
                + " SET SIZE = ?, IS_PUBLIC = ?, IS_WRITABLE = ?"
                + " WHERE FILENAME = ?");
        
        getPrivateClientFiles = connection.prepareStatement("SELECT * FROM " + FILE_CATALOG_TABLE
                + " WHERE OWNER = ? AND IS_PUBLIC = ?");
        deletePrivateClientFiles = connection.prepareStatement("DELETE FROM " + FILE_CATALOG_TABLE + 
                " WHERE OWNER = ? AND IS_PUBLIC = ?");

    }

}
