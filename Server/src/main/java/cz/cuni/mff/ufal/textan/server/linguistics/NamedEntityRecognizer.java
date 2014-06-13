package cz.cuni.mff.ufal.textan.server.linguistics;

import cz.cuni.mff.ufal.nametag.*;
import cz.cuni.mff.ufal.textan.data.repositories.dao.IObjectTypeTableDAO;
import cz.cuni.mff.ufal.textan.data.tables.ObjectTypeTable;
import cz.cuni.mff.ufal.textan.server.models.Entity;
import cz.cuni.mff.ufal.textan.server.models.ObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A named entity recognizer.
 * Internally use NameTag tool.
 *
 * @author Jakub Vlček
 * @see <a href="http://ufal.mff.cuni.cz/nametag">NameTag page</a>
 */
public class NamedEntityRecognizer {

    private static final Logger LOG = LoggerFactory.getLogger(NamedEntityRecognizer.class);

    private final IObjectTypeTableDAO objectTypeTableDAO;
    private final Hashtable<Long, ObjectType> idTempTable;

    private Ner ner;

    public NamedEntityRecognizer(IObjectTypeTableDAO objectTypeTableDAO) {
        this.objectTypeTableDAO = objectTypeTableDAO;
        idTempTable = new Hashtable<>();
    }

    /**
     * Initialize NameTag
     * if there are existing models, than use newest one, else train new
     */
    public void init() {
        LOG.info("Initializing NameTag");
        LOG.info("Looking for models");
        File modelsDir = new File("models");
        if (modelsDir.exists() && modelsDir.isDirectory()) {
            FilenameFilter modelsFilter = (dir, name) -> {
                if (name.lastIndexOf('.') > 0) {
                    // get last index for '.' char
                    int lastIndex = name.lastIndexOf('.');

                    // get extension
                    String str = name.substring(lastIndex);

                    // match path name extension
                    if (str.equals(".ner")) {
                        return true;
                    }
                }
                return false;
            };
            File[] models = modelsDir.listFiles(modelsFilter);
            if (models.length > 0) {
                Arrays.sort(models, (File a, File b) -> Long.signum(b.lastModified() - a.lastModified()));
                LOG.info("Existing model(s) found)");
                int i = 0;
                while ((i < models.length) && (!bindModel(models[i]))) {
                    ++i;
                }
                if (i >= models.length) {
                    LOG.info("Found models are corrupted, learning");
                    learn(true);
                }
            } else {
                LOG.info("No models found");
                learn(true);
            }

        }
    }

    /**
     * changing model of NameTag
     *
     * @param pathToModel path to *.ner file
     * @return true if change was successful, else false
     */
    private boolean bindModel(File pathToModel) {
        if (!pathToModel.exists()) {
            LOG.error("Model " + pathToModel.getAbsolutePath() + " wasn't found");
            return false;
        }
        LOG.info("Changing model");
        Ner tempNer = Ner.load(pathToModel.getAbsolutePath());
        if (tempNer == null) {
            LOG.error("Model " + pathToModel.getAbsolutePath() + " is corrupted");
            return false;
        } else {
            ner = tempNer;
            LOG.info("Model changed to " + pathToModel.getAbsolutePath());
        }
        return true;
    }

    /**
     * Function that creates commands for learning new NameTag model.
     *
     * @return string array with commands
     */
    private List<String> prepareLearningArguments(File workingDirectory) {
        String[] configValues = {"czech", "morphodita:czech-131112-pos_only.tagger", "features-tsd13.txt", "2", "30", "-0.1", "0.1", "0.01", "0.5", "0", ""};
        String[] configNames = {"ner_identifier", "tagger", "featuresFile", "stages", "iterations", "missing_weight", "initial_learning_rage", "final_learning_rage", "gaussian", "hidden_layer", "heldout_data"};
        List<String> result = new LinkedList<>();

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            result.add(workingDirectory.toString() + File.separator + "train_ner.exe");
        } else {
            result.add(workingDirectory.toString() + File.separator + "train_ner");
        }
        try {
            InputStream configFileStream = NamedEntityRecognizer.class.getResource("/NametagLearningConfiguration.properties").openStream();
            Properties p = new Properties();
            p.load(configFileStream);
            configFileStream.close();
            for (int i = 0; i < configNames.length; ++i) {
                try {
                    String value = (String) p.get(configNames[i]);
                    if (value != null) {
                        configValues[i] = value;
                    } else {
                        LOG.warn("Config value " + configNames[i] + " wasn't set, using default value.");
                    }
                } catch (Exception e) {
                    LOG.warn("Config value " + configNames[i] + " wasn't set, using default value.", e);
                } finally {
                    if (!configValues[i].isEmpty()) {
                        result.add(configValues[i]);
                    }
                }
            }
        } catch (Exception e) {
            LOG.warn("Config file for NameTag wasn't found, using default values.", e);
            for (int i = 0; i < configNames.length; ++i) {
                if (!configValues[i].isEmpty()) {
                    result.add(configValues[i]);
                }
            }
        }
        //result[result.length - 1] = command.toString();
        return result;
    }

    /**
     * Learn new model
     *
     * @param waitForModel true when learning is tu be blocking, else false
     */
    private void learn(boolean waitForModel) {
        LOG.info("Started training new NameTag model");
        try {
            File dir = new File(Paths.get("../../Linguistics/training").toAbsolutePath().toRealPath().toString());
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HH-mm-ss-SSS");
            File modelLocation = new File("models" + File.separator + "model" + sdf.format(Calendar.getInstance().getTime()) + ".ner").getAbsoluteFile();
            LOG.debug("New model path: " + modelLocation);

            List<String> learningCommand = prepareLearningArguments(dir);
            LOG.debug("Executing learning command: " + String.join(" ", learningCommand));

            // build process
            ProcessBuilder pb = new ProcessBuilder(learningCommand);
            File trainingDataFile = new File(dir.getAbsolutePath() + File.separator + "cnec2.0-all" + File.separator + "train.txt");
            pb.directory(dir);
            // IO redirection
            pb.redirectInput(trainingDataFile);
            pb.redirectOutput(modelLocation);
            pb.redirectErrorStream(false);
            Process ps = pb.start();

            // read error stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
            String lineErr;
            String linePrev = null;
            while ((lineErr = bufferedReader.readLine()) != null) {
                linePrev = lineErr;
            }

            boolean correctRun = true;
            if (waitForModel) {
                LOG.info("Waiting for training process");
                correctRun = ps.waitFor(5, TimeUnit.MINUTES);
            }

            if ((correctRun) && ((linePrev != null) && (linePrev.endsWith("Recognizer saved.")))) {
                LOG.info("Training done");
                this.bindModel(modelLocation);
            } else {
                LOG.error("Training failed: " + linePrev);
            }

        } catch (IOException e) {
            LOG.error("Training failed", e);
        } catch (InterruptedException e) {
            LOG.error("Training takes too long", e);
        }
    }

    /**
     * Translate entity type from string to Object Type
     *
     * @param entityType entity type decoded by NameTag
     * @return translated entity type
     */
    ObjectType translateEntity(String entityType) {
        ObjectType value = new ObjectType(-1L, "");
        Long id = -1L;
        try {
            id = Long.parseLong(entityType);
        } catch (NumberFormatException nfe) {
            // log outside of method
        }
        if (id == -1L) {
            return null;
        }
        if (idTempTable.containsKey(id)) {
            value = idTempTable.get(id);
            LOG.debug("Using CACHED entity " + value.getName());
        } else {
            try {
                ObjectTypeTable tableObject = objectTypeTableDAO.find(id);
                if (tableObject != null) {
                    value = new ObjectType(tableObject.getId(), tableObject.getName());
                    idTempTable.put(id, value);
                    LOG.debug("Using DATABASE entity " + value.getName());
                } else {
                    LOG.warn("Entity type " + entityType + " recognized, but is not stored in database.");
                }
            } catch (Exception ex) {
                LOG.warn("Exception occurred when trying translate entity.", ex.getMessage());
            }
        }
        return value;
    }

    public List<Entity> tagText(String input) {
        LOG.debug(input);
        if (ner == null) {
            LOG.error("NameTag hasn't model!");
            return new ArrayList<>();
        }
        Forms forms = new Forms();
        TokenRanges tokens = new TokenRanges();
        NamedEntities entities = new NamedEntities();
        ArrayList<NamedEntity> sortedEntities = new ArrayList<>();
        Scanner reader = new Scanner(input);
        List<Entity> entitiesList = new ArrayList<>();
        Stack<NamedEntity> openEntities = new Stack<>();
        Tokenizer tokenizer = ner.newTokenizer();
        boolean notEof = true;
        while (notEof) {
            StringBuilder textBuilder = new StringBuilder();
            String line;

            // Read block
            while (notEof = reader.hasNextLine()) {
                line = reader.nextLine();
                textBuilder.append(line);
                textBuilder.append('\n');
            }
            textBuilder.append('\n');

            // Tokenize and recognize
            String text = textBuilder.toString();
            tokenizer.setText(text);

            while (tokenizer.nextSentence(forms, tokens)) {
                ner.recognize(forms, entities);
                sortEntities(entities, sortedEntities);

                for (int i = 0, e = 0; i < tokens.size(); i++) {

                    for (; e < sortedEntities.size() && sortedEntities.get(e).getStart() == i; e++) {
                        openEntities.push(sortedEntities.get(e));
                    }

                    while (!openEntities.empty() && (openEntities.peek().getStart() + openEntities.peek().getLength() - 1) == i) {
                        NamedEntity endingEntity = openEntities.peek();
                        int entityStart = (int) tokens.get((int) (i - endingEntity.getLength() + 1)).getStart();
                        int entityEnd = (int) (tokens.get(i).getStart() + tokens.get(i).getLength());
                        if (openEntities.size() == 1) {
                            ObjectType recognizedEntity = translateEntity(endingEntity.getType());
                            if (recognizedEntity != null) {
                                LOG.debug("Recognized entity: " + encodeEntities(text.substring(entityStart, entityEnd)));
                                entitiesList.add(new Entity(encodeEntities(text.substring(entityStart, entityEnd)), entityStart, entityEnd - entityStart - 1, recognizedEntity));
                            } else {
                                LOG.debug("Type " + endingEntity.getType() + " of entity " + encodeEntities(text.substring(entityStart, entityEnd)) + " recognized by NameTag, but is not in database.");
                            }

                        }
                        openEntities.pop();
                    }
                }
            }
        }

        return entitiesList;
    }

    private void sortEntities(NamedEntities entities, ArrayList<NamedEntity> sortedEntities) {
        class NamedEntitiesComparator implements Comparator<NamedEntity> {
            public int compare(NamedEntity a, NamedEntity b) {
                if (a.getStart() < b.getStart()) return -1;
                if (a.getStart() > b.getStart()) return 1;
                if (a.getLength() > b.getLength()) return -1;
                if (a.getLength() < b.getLength()) return 1;
                return 0;
            }
        }
        NamedEntitiesComparator comparator = new NamedEntitiesComparator();

        sortedEntities.clear();
        for (int i = 0; i < entities.size(); i++)
            sortedEntities.add(entities.get(i));
        Collections.sort(sortedEntities, comparator);
    }

    private String encodeEntities(String text) {
        return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }
}