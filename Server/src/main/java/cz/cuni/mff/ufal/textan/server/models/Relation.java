package cz.cuni.mff.ufal.textan.server.models;

import cz.cuni.mff.ufal.textan.commons.utils.Pair;
import cz.cuni.mff.ufal.textan.data.tables.RelationTable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A service layer representation of the Relation
 * @author Petr Fanta
 */
public class Relation {

    private final long id;
    private final RelationType type;
    private final List<Pair<Long, Integer>> objectsInRelation;
    private final boolean isNew;

    /**
     * Instantiates a new Relation.
     *
     * @param id the id
     * @param type the type
     * @param objectsInRelation the objects in relation
     * @param isNew the is new
     */
    public Relation(long id, RelationType type, List<Pair<Long, Integer>> objectsInRelation, boolean isNew) {
        this.id = id;
        this.type = type;
        this.objectsInRelation = objectsInRelation;
        this.isNew = isNew;
    }

    /**
     * Creates a {@link cz.cuni.mff.ufal.textan.server.models.Relation} from a {@link cz.cuni.mff.ufal.textan.data.tables.RelationTable}.
     *
     * @param relationTable the relation table
     * @return the relation
     */
    public static Relation fromRelationTable(RelationTable relationTable) {

        List<Pair<Long, Integer>> objectsInRelation = relationTable.getObjectsInRelation().stream()
                .map(inRelation -> new Pair<Long, Integer>(inRelation.getObject().getId(), inRelation.getOrder()))
                .collect(Collectors.toList());


        return new Relation(
                relationTable.getId(),
                RelationType.fromRelationTypeTable(relationTable.getRelationType()),
                objectsInRelation,
                false
        );
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public RelationType getType() {
        return type;
    }

    /**
     * Gets identifiers of objects in relation and they order in relation.
     *
     * @return the objects in relation
     */
    public List<Pair<Long, Integer>> getObjectsInRelation() {
        return objectsInRelation;
    }

    /**
     * Indicates if relation is in database.
     *
     * @return the boolean
     */
    public boolean isNew() {
        return isNew;
    }

    /**
     * Converts the instance to a {@link cz.cuni.mff.ufal.textan.commons.models.Relation}.
     *
     * @return the {@link cz.cuni.mff.ufal.textan.commons.models.Relation}
     */
    public cz.cuni.mff.ufal.textan.commons.models.Relation toCommonsRelation() {

        cz.cuni.mff.ufal.textan.commons.models.Relation commonsRelation = new cz.cuni.mff.ufal.textan.commons.models.Relation();
        commonsRelation.setId(id);
        commonsRelation.setRelationType(type.toCommonsRelationType());

        cz.cuni.mff.ufal.textan.commons.models.Relation.ObjectInRelationIds objectInRelationIds = new cz.cuni.mff.ufal.textan.commons.models.Relation.ObjectInRelationIds();
        objectInRelationIds.getInRelations().addAll(
                objectsInRelation.stream().map(inRelation -> {
                    cz.cuni.mff.ufal.textan.commons.models.Relation.ObjectInRelationIds.InRelation commonsInRelation = new cz.cuni.mff.ufal.textan.commons.models.Relation.ObjectInRelationIds.InRelation();
                    commonsInRelation.setObjectId(inRelation.getFirst());
                    commonsInRelation.setOrder(inRelation.getSecond());
                    return commonsInRelation;
                })
                .collect(Collectors.toList())
        );
        commonsRelation.setObjectInRelationIds(objectInRelationIds);
        commonsRelation.setIsNew(isNew);

        return commonsRelation;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Relation relation = (Relation) o;

        if (id != relation.id) return false;
        if (type != null ? !type.equals(relation.type) : relation.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        //FIXME:
        return "Relation{" +
                "id=" + id +
                ", type=" + type +
                ", objectsInRelation=" + objectsInRelation +
                ", isNew=" + isNew +
                '}';
    }
}