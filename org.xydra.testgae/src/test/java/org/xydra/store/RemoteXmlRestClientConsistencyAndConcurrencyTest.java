package org.xydra.store;

import org.xydra.base.XId;
import org.xydra.core.serialize.xml.XmlParser;
import org.xydra.core.serialize.xml.XmlSerializer;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.delegate.XydraPersistence;
import org.xydra.store.impl.rest.XydraStoreRestClient;


public class RemoteXmlRestClientConsistencyAndConcurrencyTest extends
        AbstractRemoteStoreConcurrencyConsistencyTest {
    
    private static final Logger log = LoggerFactory
            .getLogger(RemoteXmlRestClientConsistencyAndConcurrencyTest.class);
    
    static ServerConfig serverConfig = ServerConfig.XYDRA_LIVE;
    
    @Override
    protected XId getCorrectUser() {
        return serverConfig.testerActor;
    }
    
    @Override
    protected String getCorrectUserPasswordHash() {
        return serverConfig.testerPasswordHash;
    }
    
    @Override
    protected XId getRepositoryId() {
        return serverConfig.mainRepositoryId;
    }
    
    @Override
    protected XydraStore getStore() {
        if(this.store == null) {
            this.store = new XydraStoreRestClient(serverConfig.absoluteURI, new XmlSerializer(),
                    new XmlParser());
        }
        return this.store;
    }
    
    public static void main(String[] args) {
        // server
        if(serverConfig == ServerConfig.TEST_GAE_LOCAL) {
            log.info("make sure you RunXmasJetty in project testgae");
        } else if(serverConfig == ServerConfig.XYDRA_LIVE) {
            log.info("make sure you are online");
        }
        
        // client
        RemoteXmlRestClientConsistencyAndConcurrencyTest t = new RemoteXmlRestClientConsistencyAndConcurrencyTest();
        XydraStore store = t.getStore();
        XydraPersistence persistence = new PersistenceOnStore(t.getCorrectUser(),
                t.getCorrectUserPasswordHash(), store);
        
        for(int i = 0; i < 5; i++) {
            PersistenceRobot robot = new PersistenceRobot(persistence, t.getCorrectUser(), "" + i);
            robot.start();
        }
        log.debug("Robots started");
    }
    
}
