package cz.cuni.mff.ufal.textan.gui.graph;

import cz.cuni.mff.ufal.textan.core.ObjectType;
import cz.cuni.mff.ufal.textan.core.Relation;
import java.util.Arrays;

/**
 * Object represeting relation between multiple objects without hypergraphs.
 */
public class RelationObject extends cz.cuni.mff.ufal.textan.core.Object {

    /** Underlying relation. */
    protected final Relation relation;

    /**
     * Only constructor.
     * @param relation blueprint
     */
    public RelationObject(final Relation relation) {
        super(-1, new ObjectType(relation.getType().getId(), relation.getType().getName()), Arrays.asList(relation.getType().getName()));
        this.relation = relation;
    }

    /**
     * Returns underlying relation.
     * @return inderlying relation
     */
    public Relation getRelation() {
        return relation;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return relation.toString();
    }
}
