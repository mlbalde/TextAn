package cz.cuni.mff.ufal.textan.server;

import cz.cuni.mff.ufal.textan.commons_old.models.Entity;
import cz.cuni.mff.ufal.textan.commons_old.models.Object;
import cz.cuni.mff.ufal.textan.commons_old.models.ObjectType;
import cz.cuni.mff.ufal.textan.commons_old.models.Rating;
import cz.cuni.mff.ufal.textan.commons_old.models.Relation;
import cz.cuni.mff.ufal.textan.commons_old.models.Ticket;
import cz.cuni.mff.ufal.textan.commons_old.ws.IDocumentProcessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.jws.WebService;

/**
 * For now only mocking document processing.
 */
@WebService(endpointInterface = "cz.cuni.mff.ufal.textan.commons_old.ws.IDocumentProcessor", serviceName = "DocumentProcessor")
public class DocumentProcessor implements IDocumentProcessor {

    @Override
    public Ticket getTicket(String username) {
        return new Ticket(username, new Date());
    }

    @Override
    public int addDocument(String text) {
        return 0;
    }

    @Override
    public void updateDocument(String text, int documentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Entity[] getEntities(String text) {
        return new Entity[] { new Entity(text, 0, text.length(), 0) };
    }

    @Override
    public Entity[] getEntitiesById(int documentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Rating[] getObjects(String text, Entity[] entities) {
        final Rating[] array = new Rating[entities.length];
        for (int i = 0; i < array.length; ++i) {
            array[i] = new Rating();
            array[i].candidate = new Object[] { new Object(0, new ObjectType(0, "Person"), new ArrayList<>(Arrays.asList("Pepa"))) };
            array[i].rating = new double[] { 1D };
        }
        return array;
    }

    @Override
    public Rating[] getObjectsById(int documentId, Entity[] entities) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveProcessedDocument(int documentId, Object[] objects, Relation[] relations, Ticket ticket, boolean force) {
        System.out.println("DOCUMENT SAVED!");
    }

    @Override
    public void getProblems(int documentId, Object[] objects, Relation[] relations, Ticket ticket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}