package cz.cuni.mff.ufal.textan.server.setup;

import com.beust.jcommander.JCommander;
import cz.cuni.mff.ufal.textan.server.setup.options.CleanDB;
import cz.cuni.mff.ufal.textan.server.setup.options.Command;
import cz.cuni.mff.ufal.textan.server.setup.options.CreateDB;
import cz.cuni.mff.ufal.textan.server.setup.options.ListTypes;
import cz.cuni.mff.ufal.textan.server.setup.options.LoadObjectTypes;
import cz.cuni.mff.ufal.textan.server.setup.options.LoadRelationTypes;
import cz.cuni.mff.ufal.textan.server.setup.options.Options;
import cz.cuni.mff.ufal.textan.server.setup.options.PrepareTrainingData;
import cz.cuni.mff.ufal.textan.server.setup.options.RenameObjectTypes;
import cz.cuni.mff.ufal.textan.server.setup.options.RenameRelationTypes;
import cz.cuni.mff.ufal.textan.server.setup.utils.ScriptRunner;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.*;

/**
 * Simple class for batch report processing.
 * For executing different commands uses double dispatch.
 */
public class Setuper {

    private static final int EXIT_STATUS_HELP = 1;
    private static final int EXIT_STATUS_MISSING_PARAM = 2;
    private static final int EXIT_STATUS_MISSING_CLASS = 3;
    private static final int EXIT_STATUS_SQL_PROBLEM = 3;
    private static final int EXIT_STATUS_IO_PROBLEM = 4;
    private static final int EXIT_STATUS_UNEXPECTED_PROBLEM = 100;

    private static final String CREATE_SCRIPT_FILENAME = "create.sql";
    private static final String CLEAN_SCRIPT_FILENAME = "clean.sql";

    /**
     * Main method.
     * @param args command line arguments
     */
    public static void main(final String[] args) {

        try {
            //parse command line options
            final Options options = new Options();
            final JCommander jCommander = options.createJCommander();
            jCommander.parse(args);
            if (options.help || jCommander.getParsedCommand() == null) {
                jCommander.usage();
                System.exit(EXIT_STATUS_HELP);
            }
            options.processSettings();
            if (options.driver == null) {
                System.out.println("Database driver not specified!");
                jCommander.usage();
                System.exit(EXIT_STATUS_MISSING_PARAM);
            }
            if (options.url == null) {
                System.out.println("Database url not specified!");
                jCommander.usage();
                System.exit(EXIT_STATUS_MISSING_PARAM);
            }
            if (options.user == null) {
                System.out.println("Database user not specified!");
                jCommander.usage();
                System.exit(EXIT_STATUS_MISSING_PARAM);
            }
            if (options.password == null) {
                System.out.println("Database password not specified!");
                jCommander.usage();
                System.exit(EXIT_STATUS_MISSING_PARAM);
            }
            //
            final String commandName = jCommander.getParsedCommand();
            final Command command = options.getCommand(commandName);
            new Setuper(options).execute(command);

        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(EXIT_STATUS_MISSING_CLASS);
        } catch (SQLException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(EXIT_STATUS_SQL_PROBLEM);
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(EXIT_STATUS_IO_PROBLEM);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(EXIT_STATUS_UNEXPECTED_PROBLEM);
        }

    }

    /** Application options. */
    protected final Options options;

    /** Database connection */
    protected Connection connection;

    /**
     * Only constructor.
     * @param options application options
     */
    public Setuper(final Options options) {
        this.options = options;
    }

    /**
     * Executes command using visitor pattern.
     * @param command command to execute
     */
    public void execute(final Command command) throws ClassNotFoundException, SQLException, IOException {

        //Open db connection
        try {
            Class.forName(options.driver);
        } catch (ClassNotFoundException e) {
            System.err.println("The JDBC driver '{}' was not found.");
            throw e;
        }

        connection = null;
        try {
            connection = DriverManager.getConnection(options.url, options.user, options.password);
        } catch (SQLException e) {
            System.err.println("Connection to database failed.");
            throw e;
        }

        try {
            command.accept(this);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Cleans the database.
     * @param command command options
     */
    public void cleanDB(final CleanDB command) throws IOException, SQLException {
        //TODO: fix script loading
        try (Reader cleanScriptReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(CLEAN_SCRIPT_FILENAME))) {
            connection.setAutoCommit(false);
            ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);
//            scriptRunner.setLogWriter(null);
//            scriptRunner.setErrorLogWriter(null);
            scriptRunner.runScript(cleanScriptReader);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Creates the database.
     * @param command command options
     */
    public void createDB(final CreateDB command) throws IOException, SQLException {
        //TODO: fix script loading
        try (Reader createScriptReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(CREATE_SCRIPT_FILENAME))) {
            connection.setAutoCommit(false);
            ScriptRunner scriptRunner = new ScriptRunner(connection, false, true);
//            scriptRunner.setLogWriter(null);
//            scriptRunner.setErrorLogWriter(null);
            scriptRunner.runScript(createScriptReader);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Loads object types to the database.
     * @param command command options
     */
    public void createObjectTypes(final LoadObjectTypes command) throws SQLException {
        try {
            connection.setAutoCommit(false);

            String insertObjectTypeQuery = "INSERT INTO ObjectType (name) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(insertObjectTypeQuery);
            for (String typeName : command.types) {
                statement.setString(1, typeName);
                statement.executeUpdate();
            }

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Loads relation types to the database.
     * @param command command options
     */
    public void createRelationTypes(final LoadRelationTypes command) throws SQLException {
        try {
            connection.setAutoCommit(false);

            String insertRelationTypeQuery = "INSERT INTO RelationType (name) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(insertRelationTypeQuery)) {
                for (String typeName : command.types) {
                    statement.setString(1, typeName);
                    statement.executeUpdate();
                }
            }

            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Listing object and relation types in the database.
     * @param command command options
     */
    public void listTypes(final ListTypes command) throws SQLException {
        //TODO test if connection  != null

        try (Statement statement = connection.createStatement()) {
            String objectTypesQuery = "SELECT id_object_type, name FROM ObjectType ORDER BY id_object_type";
            ResultSet objectTypes = statement.executeQuery(objectTypesQuery);

            System.out.println("Object types");
            System.out.println("ID\tName"); //TODO better formatting
            while (objectTypes.next()) {
                long id = objectTypes.getLong("id_object_type");
                String name = objectTypes.getString("name");
                System.out.printf("%s\t%s\n", id, name);
            }

            System.out.println();

            String relationTypesQuery = "SELECT id_relation_type, name FROM RelationType ORDER BY id_relation_type";
            ResultSet relationTypes = statement.executeQuery(relationTypesQuery);

            System.out.println("Relation types");
            System.out.println("ID\tName"); //TODO better formatting
            while (relationTypes.next()) {
                long id = relationTypes.getLong("id_relation_type");
                String name = relationTypes.getString("name");
                System.out.printf("%s\t%s\n", id, name);
            }
        }
    }

    /**
     * Renames object types to the database.
     * @param command command options
     */
    public void renameObjectTypes(final RenameObjectTypes command) {
        //TODO implement
    }

    /**
     * Renames relation types to the database.
     * @param command command options
     */
    public void renameRelationTypes(final RenameRelationTypes command) {
        //TODO implement
    }

    /**
     * Prepares training data.
     * @param command command options
     */
    public void prepareTrainingData(final PrepareTrainingData command) {
        //TODO implement
    }
}
