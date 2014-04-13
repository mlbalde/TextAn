package cz.cuni.mff.ufal.textan.server.services;

import cz.cuni.mff.ufal.textan.data.repositories.dao.IDocumentTableDAO;
import cz.cuni.mff.ufal.textan.data.tables.DocumentTable;
import cz.cuni.mff.ufal.textan.server.models.EditingTicket;
import cz.cuni.mff.ufal.textan.server.models.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * A service which provides a named entity recognition.
 * @author Petr Fanta
 */
@Service
public class NamedEntityRecognizerService {

    private final IDocumentTableDAO documentTableDAO;
    //TODO add field with "nametag"

    /**
     * Instantiates a new Named entity recognizer service.
     *
     * @param documentTableDAO the document table dAO
     */
    @Autowired
    public NamedEntityRecognizerService(IDocumentTableDAO documentTableDAO) {
        this.documentTableDAO = documentTableDAO;
    }

    /**
     *  Gets entities from a plain text.
     *
     * @param text the text
     * @param editingTicket the editing ticket
     * @return the list of entities found in the text
     */
    public List<Entity> getEntities(String text, EditingTicket editingTicket) {
        //TODO: call nametag

        return new ArrayList<>();
    }

    /**
     * Gets entities from a document with the given identifier .
     *
     * @param documentId the identifier of a document
     * @param editingTicket the editing ticket
     * @return the list of entities found in the document
     * @throws IdNotFoundException the exception thrown if a document with the given id wasn't found
     */
    public List<Entity> getEntities(long documentId, EditingTicket editingTicket) throws IdNotFoundException{

        DocumentTable documentTable = documentTableDAO.find(documentId);
        if (documentTable == null) {
            throw new IdNotFoundException("documentId", documentId);
        }

        //TODO: call nametag

        return new ArrayList<>();
    }
}