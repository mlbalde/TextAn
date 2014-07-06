package cz.cuni.mff.ufal.textan.server.linguistics;

import cz.cuni.mff.ufal.nametag.*;
import cz.cuni.mff.ufal.textan.data.repositories.dao.IDocumentTableDAO;
import cz.cuni.mff.ufal.textan.data.repositories.dao.IObjectTypeTableDAO;
import cz.cuni.mff.ufal.textan.data.tables.AliasOccurrenceTable;
import cz.cuni.mff.ufal.textan.data.tables.DocumentTable;
import cz.cuni.mff.ufal.textan.data.tables.ObjectTable;
import cz.cuni.mff.ufal.textan.data.tables.ObjectTypeTable;
import cz.cuni.mff.ufal.textan.data.views.INameTagView;
import cz.cuni.mff.ufal.textan.data.views.NameTagRecord;
import cz.cuni.mff.ufal.textan.server.models.Entity;
import cz.cuni.mff.ufal.textan.server.models.ObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A named entity recognizer.
 * Internally use NameTag tool.
 *
 * @author Jakub Vlček
 * @see <a href="http://ufal.mff.cuni.cz/nametag">NameTag page</a>
 */
public class NamedEntityRecognizer {

    private static final Logger LOG = LoggerFactory.getLogger(NamedEntityRecognizer.class);

    private static final String MODELS_DIR = "models";
    private static final String MODEL_FILE_EXTENSION = ".ner";
    private static final String MODEL_FILE_PREFIX = "model";
    private static final String TRAINING_DIR = "training";
    private static final String TRAINING_DATA_EXTENSION = ".txt";
    private static final String TRAINING_DATA_PREFIX = "temporaryTrainingData";

    private static final String SPACE_REPLACE_REGEX = "\\s";
    private static final String BEFORE_PUNCT_NEWLINE_REGEX = "([^\\n])(\\p{Punct})";
    private static final String BEFORE_PUNCT_REPLACE_REGEX = "$1\n$2";
    private static final String AFTER_PUNCT_NEWLINE_REGEX = "\\n(\\p{Punct})(.+)";
    private static final String AFTER_PUNCT_REPLACE_REGEX = "\n$1\n$2";
    private static final String ADD_TAG_REGEX = "(.+)\\n";
    private static final String CONTINUING_ENTITY_REGEX = "I-";
    private static final String CONTINUING_ENTITY_REPLACE_REGEX = "B-";

    private static final Pattern spaceReplacePattern = Pattern.compile(SPACE_REPLACE_REGEX);
    private static final Pattern beforePunctPattern = Pattern.compile(BEFORE_PUNCT_NEWLINE_REGEX);
    private static final Pattern afterPunctPattern = Pattern.compile(AFTER_PUNCT_NEWLINE_REGEX);
    private static final Pattern addTagPattern = Pattern.compile(ADD_TAG_REGEX);
    private static final Pattern continuingEntityPattern = Pattern.compile(CONTINUING_ENTITY_REGEX);



    private final IObjectTypeTableDAO objectTypeTableDAO;
    private final INameTagView nameTagView;
    private final IDocumentTableDAO documentTableDAO;
    private final Hashtable<Long, ObjectType> idTempTable;

    private Ner ner;

    /**
     * Create new NamedEntityRecognizer
     * @param objectTypeTableDAO data access object to object tables
     * @param nameTagView data access object to nameTagView
     * @param documentTableDAO data access object to document table
     */
    public NamedEntityRecognizer(IObjectTypeTableDAO objectTypeTableDAO, INameTagView nameTagView, IDocumentTableDAO documentTableDAO) {
        this.objectTypeTableDAO = objectTypeTableDAO;
        this.nameTagView = nameTagView;
        this.documentTableDAO = documentTableDAO;
        idTempTable = new Hashtable<Long, ObjectType>();
    }

    /**
     * Get available models and sort them by 'last modified date' descending
     * @param modelsDir look up directory
     * @return sorted models
     */
    private File[] getSortedModels(File modelsDir) {
        if (modelsDir.exists() && modelsDir.isDirectory()) {
            FilenameFilter modelsFilter = (dir, name) -> (name.length() > MODEL_FILE_EXTENSION.length()) && (name.endsWith(MODEL_FILE_EXTENSION));
            File[] models = modelsDir.listFiles(modelsFilter);
            Arrays.sort(models, (File a, File b) -> Long.signum(b.lastModified() - a.lastModified()));
            return models;
        } else {
            LOG.warn("Directory {} not exists", modelsDir);
            return null;
        }
    }

    /**
     * Initialize NameTag
     * if there are existing models, than use newest one, else train new
     */
    public void init() {
        LOG.info("Initializing NameTag");

        LOG.info("Looking for models");
        File[] models = getSortedModels(new File(MODELS_DIR));
        if (models != null || models.length > 0) {
            LOG.info("Existing model(s) found)");
            int i = 0;
            while ((i < models.length) && (!bindModel(models[i]))) {
                ++i;
            }
            if (i >= models.length) {
                LOG.info("Found models are corrupted, learning new model");
                learn(true);
            }
        } else {
            LOG.info("No models found");
            learn(true);
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
            LOG.error("Model {} wasn't found", pathToModel.getAbsolutePath());
            return false;
        }

        LOG.info("Changing model");
        Ner tempNer = Ner.load(pathToModel.getAbsolutePath());
        if (tempNer == null) {
            LOG.error("Model {} is corrupted", pathToModel.getAbsolutePath());
            return false;
        } else {
            ner = tempNer;
            LOG.info("Model changed to {}", pathToModel.getAbsolutePath());
        }

        return true;
    }

    /**
     * Prepares training data generated from database to provided file
     * @param fileWithTrainingData file with output data (data will be appended)
     */
    private void prepareLearningData(File fileWithTrainingData) {
        LOG.info("Creating data from database started");
        try {
            PrintWriter output = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileWithTrainingData, true), "UTF-8"));

            List<NameTagRecord> documents = nameTagView.findAll();
            Collections.sort(documents, (
                    NameTagRecord a, NameTagRecord b) ->
                    a.getDocumentID() != b.getDocumentID()
                            ? Long.signum(a.getDocumentID() - b.getDocumentID())
                            : Long.signum(a.getAliasOccurrencePosition() - b.getAliasOccurrencePosition()));

            long lastID = -1L;
            int lastEntityEnd = 0;
            String documentText = "";
            StringBuilder taggedDocument = new StringBuilder();
            for (NameTagRecord record : documents) {
                if (lastID != record.getDocumentID()) {
                    output.print(taggedDocument.toString().replaceAll("[\\n]+","\n"));
                    taggedDocument.delete(0, taggedDocument.length());
                    documentText = documentTableDAO.find(record.getDocumentID()).getText();
                    lastEntityEnd = 0;
                    lastID = record.getDocumentID();
                }
                if (lastEntityEnd == record.getAliasOccurrencePosition() + 1) {
                    taggedDocument.append(formatAliasForTraining(record.getAlias(), record.getObjectTypeID(), true));
                } else {
                    taggedDocument.append(formatAliasForTraining(documentText.substring(lastEntityEnd, record.getAliasOccurrencePosition()), null, false));
                    taggedDocument.append(formatAliasForTraining(record.getAlias(), record.getObjectTypeID(), false));
                }
                lastEntityEnd = record.getAliasOccurrencePosition() + record.getAlias().length();
            }
            taggedDocument.append(formatAliasForTraining(documentText.substring(lastEntityEnd), null, false));
            output.print(taggedDocument.toString());
            taggedDocument.delete(0, taggedDocument.length());
            output.close();
            LOG.info("Creating data from database finished");
        } catch (FileNotFoundException e) {
            LOG.error("File for training data not found {}", fileWithTrainingData.getPath(), e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Format string to nametag training data format
     * @param alias Alias to be formatted
     * @param id ID of alias (null if this is not an alias)
     * @param continuingEntity true if previous entity wasn't separated
     * @return Formatted text
     */
    private String formatAliasForTraining(String alias, Long id, boolean continuingEntity) {
        String tagRegex = id != null ? "$1\tI-" + id +"\n" : "$1\t_\n";

        alias = alias.trim();

        Matcher m = spaceReplacePattern.matcher(alias);
        alias = m.replaceAll("\n");

        m = beforePunctPattern.matcher(alias);
        alias = m.replaceAll(BEFORE_PUNCT_REPLACE_REGEX);

        m = afterPunctPattern.matcher(alias);
        alias = m.replaceAll(AFTER_PUNCT_REPLACE_REGEX);

        alias += '\n';

        m = addTagPattern.matcher(alias);
        alias = m.replaceAll(tagRegex);

        if (continuingEntity) {
            m = continuingEntityPattern.matcher(alias);
            alias = m.replaceAll(CONTINUING_ENTITY_REPLACE_REGEX);
        }
        return alias;
    }

    /**
     * Copy file from source location to destination
     * @param source source
     * @param destination target
     * @throws IOException
     */
    private void copyFile(File source, File destination) {
        try ( FileChannel inputChannel = new FileInputStream(source).getChannel();
              FileChannel outputChannel = new FileOutputStream(destination).getChannel(); ) {
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (IOException e) {
            LOG.error("Can't copy file from {} to {}", source.getPath(), destination.getPath(), e);
        }
    }


    /**
     * Learn new model
     *
     * @param waitForModel true when learning is tu be blocking, else false
     */
    public void learn(boolean waitForModel) {
        LOG.info("Started training new NameTag model");
        try {
            File dir = new File(TRAINING_DIR).getCanonicalFile();
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd_HH-mm-ss-SSS");
            Date date = Calendar.getInstance().getTime();
            File modelLocation = new File(MODELS_DIR, MODEL_FILE_PREFIX + sdf.format(date) + MODEL_FILE_EXTENSION).getAbsoluteFile();
            LOG.debug("New model path: {}", modelLocation);

            LearningParameters learningParameters = new LearningParameters(dir);
            File trainingDataFile = new File(TRAINING_DIR, TRAINING_DATA_PREFIX + sdf.format(date) + TRAINING_DATA_EXTENSION).getAbsoluteFile();
            if ((new File(TRAINING_DIR).isDirectory()) || (new File(TRAINING_DIR).mkdir())) {
                if (learningParameters.useDefaultTrainingData()) {
                    LOG.info("Copiing default training data from {}", learningParameters.getTrainingData().getPath());
                    try {
                        Files.copy(learningParameters.getTrainingData().toPath(), trainingDataFile.toPath());
                        copyFile(learningParameters.getTrainingData(), trainingDataFile);
                    } catch (IOException e) {
                        LOG.error("Can't copy default training data from {} to {}: {}", learningParameters.getTrainingData().getPath(), trainingDataFile.getPath(), e);
                    }
                }
                prepareLearningData(trainingDataFile);
            } else {
                LOG.error("Can't create training data folder");
                return;
            }

            LOG.debug("Executing learning command: {}", String.join(" ", learningParameters.getCommand()));
            LOG.debug("Training data file: {}", trainingDataFile.getPath());

            // build process
            ProcessBuilder pb = new ProcessBuilder(learningParameters.getCommand());
            pb.directory(dir);

            // IO redirection
            pb.redirectInput(trainingDataFile);
            pb.redirectOutput(modelLocation);
            pb.redirectErrorStream(false);
            if (waitForModel) {
                LOG.info("Training started, waiting to be done (max {} milliseconds)", learningParameters.getWaitingTime());
            } else {
                LOG.info("Training started, continuing in work");
            }
            Process ps = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorMsg.append(line + '\n');
            }

            boolean notTimeout= true;
            if (waitForModel) {
                notTimeout = ps.waitFor(learningParameters.getWaitingTime(), TimeUnit.MILLISECONDS);
            }

            if ((notTimeout) && (ps.exitValue() == 0)) {
                LOG.info("Training done");
                this.bindModel(modelLocation);
            } else {
                LOG.error("Training failed: exit code: {}, error message: {}", ps.exitValue(), errorMsg);
            }
            File[] models = getSortedModels(new File(MODELS_DIR));
            for (int i = learningParameters.getMaximumStoredModels(); i < models.length; ++i) {
                LOG.info("Maximum models count exceeded, deleting model {}", models[i].toString());
                models[i].delete();
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
        long id = -1L;
        try {
            id = Long.parseLong(entityType);
        } catch (NumberFormatException nfe) {
            return null;
        }
        if (idTempTable.containsKey(id)) {
            value = idTempTable.get(id);
            LOG.debug("Using CACHED entity {}", value.getName());
        } else {
            ObjectTypeTable tableObject = objectTypeTableDAO.find(id);
            if (tableObject != null) {
                value = new ObjectType(tableObject.getId(), tableObject.getName());
                idTempTable.put(id, value);
                LOG.debug("Using DATABASE entity {}", value.getName());
            } else {
                LOG.warn("Entity type {} recognized, but is not stored in database.", entityType);
            }
        }
        return value;
    }

    /**
     * Tag provided text
     * @param input input text por tagging
     * @return return List of entities occurrence
     */
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
                                LOG.debug("Recognized entity: {}", encodeEntities(text.substring(entityStart, entityEnd)));
                                entitiesList.add(new Entity(encodeEntities(text.substring(entityStart, entityEnd)), entityStart, entityEnd - entityStart - 1, recognizedEntity));
                            } else {
                                LOG.debug("Type {} of entity {} recognized by NameTag, but is not in database.", endingEntity.getType(), encodeEntities(text.substring(entityStart, entityEnd)));
                            }

                        }
                        openEntities.pop();
                    }
                }
            }
        }

        return entitiesList;
    }

    /**
     * Sort entities (based on start position and length)
     * @param entities input entities
     * @param sortedEntities sorted entities
     */
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

    /**
     * Encode entities with special characters
     * @param text input text
     * @return encoded text
     */
    private String encodeEntities(String text) {
        return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }
}