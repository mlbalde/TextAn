package cz.cuni.mff.ufal.textan.server.ws;


import cz.cuni.mff.ufal.textan.commons.models.*;

import javax.jws.WebParam;
import java.util.List;

/**
 * For now only mocking document processing.
 */
public class DocumentProcessor implements cz.cuni.mff.ufal.textan.commons.ws.DocumentProcessor {

    @Override
    public List<GetObjectsFromStringResponse.Assignment> getObjectsFromString(@WebParam(name = "text", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") String text, @WebParam(name = "entities", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") GetObjectsFromString.Entities entities, @WebParam(name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) EditingTicket editingTicket) {
        return null;
    }

    @Override
    public boolean saveProcessedDocumentById(@WebParam(name = "documentId", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") int documentId, @WebParam(name = "objects", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") SaveProcessedDocumentById.Objects objects, @WebParam(name = "relations", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") SaveProcessedDocumentById.Relations relations, @WebParam(name = "force", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") boolean force, @WebParam(name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) EditingTicket editingTicket) {
        return false;
    }

    @Override
    public void getProblems(@WebParam(name = "documentId", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") int documentId, @WebParam(name = "objects", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") GetProblems.Objects objects, @WebParam(name = "relations", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") GetProblems.Relations relations, @WebParam(name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) EditingTicket editingTicket) {

    }

    @Override
    public List<GetObjectsByIdResponse.Assignment> getObjectsById(@WebParam(name = "id", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") int id, @WebParam(name = "entities", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") GetObjectsById.Entities entities, @WebParam(name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) EditingTicket editingTicket) {
        return null;
    }

    @Override
    public GetEntitiesFromStringResponse getEntitiesFromString(@WebParam(partName = "getEntitiesFromString", name = "getEntitiesFromString", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") GetEntitiesFromString getEntitiesFromString, @WebParam(partName = "editingTicket", name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) EditingTicket editingTicket) {
        return null;
    }

    @Override
    public GetEntitiesByIdResponse getEntitiesById(@WebParam(partName = "getEntitiesById", name = "getEntitiesById", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") GetEntitiesById getEntitiesById, @WebParam(partName = "editingTicket", name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) EditingTicket editingTicket) {
        return null;
    }

    @Override
    public boolean saveProcessedDocumentFromString(@WebParam(name = "text", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") String text, @WebParam(name = "objects", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") SaveProcessedDocumentFromString.Objects objects, @WebParam(name = "relations", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") SaveProcessedDocumentFromString.Relations relations, @WebParam(name = "force", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") boolean force, @WebParam(name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) EditingTicket editingTicket) {
        return false;
    }

    @Override
    public GetEditingTicketResponse getEditingTicket(@WebParam(partName = "getEditingTicket", name = "editingTicket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz") EditingTicket getEditingTicket, @WebParam(partName = "ticket", name = "ticket", targetNamespace = "http://models.commons.textan.ufal.mff.cuni.cz", header = true) Ticket ticket) {
        return null;
    }
}