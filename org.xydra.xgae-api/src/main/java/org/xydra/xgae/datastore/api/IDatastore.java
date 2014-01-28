package org.xydra.xgae.datastore.api;

public interface IDatastore {
    
    IDatastoreSync sync();
    
    IDatastoreAsync async();
    
    SEntity createEntity(SKey key);
    
    SEntity createEntity(String kind, String name);
    
    SText createText(String value);
    
    SKey createKey(String kind, String name);
    
    /**
     * @return true if writing is enabled. It might still fail, but at least it
     *         is enables, i.e. datastore is not in some kind of read-only or
     *         completely unavailable mode.
     */
    boolean canWriteDataStore();
    
}
