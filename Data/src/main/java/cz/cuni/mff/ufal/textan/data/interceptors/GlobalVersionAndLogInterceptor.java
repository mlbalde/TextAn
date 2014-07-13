package cz.cuni.mff.ufal.textan.data.interceptors;

import cz.cuni.mff.ufal.textan.data.tables.GlobalVersionTable;
import cz.cuni.mff.ufal.textan.data.tables.JoinedObjectsTable;
import cz.cuni.mff.ufal.textan.data.tables.ObjectTable;
import java.io.Serializable;
import java.util.Iterator;
import org.hibernate.EmptyInterceptor;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

/**
 *
 * @author Vaclav Pernicka
 */
public class GlobalVersionAndLogInterceptor extends LogInterceptor {
    private static final long serialVersionUID = 20156489756124L;

    
    public GlobalVersionAndLogInterceptor(String username) {
        super(username);
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof ObjectTable) {
            
            Long version = getAndIncreaseGlobalVersion();
            // TODO SET NEW VERSION
            for (int i = 0; i < propertyNames.length; i++) {
                if ("globalVersion".equals(propertyNames[i])) state[i] = version;
            }
            
            super.onSave(entity, id, state, propertyNames, types);             
            return true;
        }
        if (entity instanceof JoinedObjectsTable) {
            updateGlobalVersionOfObject(id);
            return super.onSave(entity, id, state, propertyNames, types);
        }
        return super.onSave(entity, id, state, propertyNames, types); 

    }

    
    
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (entity instanceof ObjectTable) {
            
            Long version = getAndIncreaseGlobalVersion();
            // TODO SET NEW VERSION
            for (int i = 0; i < propertyNames.length; i++) {
                if ("globalVersion".equals(propertyNames[i])) currentState[i] = version;
            }
            
            super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
            return true;
        }
        if (entity instanceof JoinedObjectsTable) {
            updateGlobalVersionOfObject(id);
            return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types); //To change body of generated methods, choose Tools | Templates.
        }
        return super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types); //To change body of generated methods, choose Tools | Templates.
    }
    private void updateGlobalVersionOfObject(Serializable joinedObjectID) {
        // TODO
    }
    private long getAndIncreaseGlobalVersion() {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        // get version record
        GlobalVersionTable gvt =
        (GlobalVersionTable)session.createCriteria(GlobalVersionTable.class)
                .setLockMode(LockMode.PESSIMISTIC_WRITE)
                .setMaxResults(1)
                .list().get(0);
        
        //get version
        long result = gvt.getVersion();
        
        // update version
        gvt.setVersion(result + 1);
        //session.update(gvt);
        session.saveOrUpdate(gvt);

        tx.commit();
        session.close();
        
        return result;
    }
}