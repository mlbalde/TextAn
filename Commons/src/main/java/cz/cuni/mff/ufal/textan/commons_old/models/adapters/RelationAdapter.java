package cz.cuni.mff.ufal.textan.commons_old.models.adapters;

import cz.cuni.mff.ufal.textan.commons_old.models.Relation;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Adapts {@link Relation}.
 */
public class RelationAdapter extends XmlAdapter<AdaptedRelation, Relation> {

    @Override
    public Relation unmarshal(AdaptedRelation adaptedRelation) throws Exception {
        final Relation rel = new Relation(adaptedRelation.getId(), adaptedRelation.getType());
        rel.getObjectInRelationIds().addAll(adaptedRelation.getObjectInRelationIds());
        rel.getOrderInRelation().addAll(adaptedRelation.getOrderInRelation());
        return rel;
    }

    @Override
    public AdaptedRelation marshal(Relation relation) throws Exception {
        final AdaptedRelation ar = new AdaptedRelation();
        ar.setId(relation.getId());
        ar.setType(relation.getType());
        ar.setObjectInRelationIds(relation.getObjectInRelationIds());
        ar.setOrderInRelation(relation.getOrderInRelation());
        return ar;
    }
}