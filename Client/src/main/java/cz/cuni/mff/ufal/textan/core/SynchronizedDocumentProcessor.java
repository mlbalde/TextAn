package cz.cuni.mff.ufal.textan.core;

import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.EditingTicket;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetAssignmentsByIdRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetAssignmentsByIdResponse;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetAssignmentsFromStringRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetAssignmentsFromStringResponse;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetEditingTicketRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetEditingTicketResponse;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetEntitiesByIdRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetEntitiesByIdResponse;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetEntitiesFromStringRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetEntitiesFromStringResponse;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetProblemsRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.GetProblemsResponse;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.SaveProcessedDocumentByIdRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.SaveProcessedDocumentByIdResponse;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.SaveProcessedDocumentFromStringRequest;
import cz.cuni.mff.ufal.textan.commons.models.documentprocessor.SaveProcessedDocumentFromStringResponse;
import cz.cuni.mff.ufal.textan.commons.ws.DocumentChanged;
import cz.cuni.mff.ufal.textan.commons.ws.IDocumentProcessor;
import cz.cuni.mff.ufal.textan.commons.ws.IdNotFoundException;

/**
 * Simple wrapper around IDocumentProcessor to provide synchronization.
 */
public class SynchronizedDocumentProcessor implements IDocumentProcessor {

    /** Wrapped IDocumentProcessor. */
    protected final IDocumentProcessor innerDP;

    /**
     * Only constructor.
     * @param dp DocumentProcessor to wrap
     */
    public SynchronizedDocumentProcessor(final IDocumentProcessor dp) {
        innerDP = dp;
    }

    @Override
    synchronized public GetAssignmentsByIdResponse getAssignmentsById(
            final GetAssignmentsByIdRequest getAssignmentsByIdRequest,
            final EditingTicket editingTicket) throws IdNotFoundException, DocumentChanged {
        return innerDP.getAssignmentsById(getAssignmentsByIdRequest, editingTicket);
    }

    @Override
    synchronized public SaveProcessedDocumentByIdResponse saveProcessedDocumentById(
            final SaveProcessedDocumentByIdRequest saveProcessedDocumentByIdRequest,
            final EditingTicket editingTicket) throws IdNotFoundException, DocumentChanged {
        return innerDP.saveProcessedDocumentById(saveProcessedDocumentByIdRequest, editingTicket);
    }

    @Override
    synchronized public GetProblemsResponse getProblems(
            final GetProblemsRequest getProblemsRequest,
            final EditingTicket editingTicket) {
        return innerDP.getProblems(getProblemsRequest, editingTicket);
    }

//    @Override
//    synchronized public GetProblemsByIdResponse getProblemsById(
//            final GetProblemsByIdRequest getProblemsByIdRequest,
//            final EditingTicket editingTicket) throws IdNotFoundException {
//        return innerDP.getProblemsById(getProblemsByIdRequest, editingTicket);
//    }

    @Override
    synchronized public GetAssignmentsFromStringResponse getAssignmentsFromString(
            final GetAssignmentsFromStringRequest getAssignmentsFromStringRequest,
            final EditingTicket editingTicket) {
        return innerDP.getAssignmentsFromString(getAssignmentsFromStringRequest, editingTicket);
    }

    @Override
    synchronized public GetEntitiesByIdResponse getEntitiesById(
            final GetEntitiesByIdRequest getEntitiesByIdRequest,
            final EditingTicket editingTicket) throws IdNotFoundException, DocumentChanged {
        return innerDP.getEntitiesById(getEntitiesByIdRequest, editingTicket);
    }

    @Override
    synchronized public GetEntitiesFromStringResponse getEntitiesFromString(
            final GetEntitiesFromStringRequest getEntitiesFromStringRequest,
            final EditingTicket editingTicket) {
        return innerDP.getEntitiesFromString(getEntitiesFromStringRequest, editingTicket);
    }

    @Override
    synchronized public SaveProcessedDocumentFromStringResponse saveProcessedDocumentFromString(
            final SaveProcessedDocumentFromStringRequest saveProcessedDocumentFromStringRequest,
            final EditingTicket editingTicket) throws IdNotFoundException {
        return innerDP.saveProcessedDocumentFromString(saveProcessedDocumentFromStringRequest, editingTicket);
    }

//    @Override
//    synchronized public GetProblemsFromStringResponse getProblemsFromString(
//            final GetProblemsFromStringRequest getProblemsFromStringRequest,
//            final EditingTicket editingTicket) {
//        return innerDP.getProblemsFromString(getProblemsFromStringRequest, editingTicket);
//    }

    @Override
    synchronized public GetEditingTicketResponse getEditingTicket(
            final GetEditingTicketRequest getEditingTicketRequest) {
        return innerDP.getEditingTicket(getEditingTicketRequest);
    }
}
