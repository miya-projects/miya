package com.miya.system.config.orm.envers;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.envers.boot.internal.EnversIntegrator;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.event.spi.*;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.service.ServiceRegistry;

@Slf4j
public class CustomEnversIntegrator extends EnversIntegrator {

    @Override
    public void integrate(Metadata metadata, BootstrapContext bootstrapContext, SessionFactoryImplementor sessionFactory) {
        final ServiceRegistry serviceRegistry = sessionFactory.getServiceRegistry();
        final EnversService enversService = serviceRegistry.getService( EnversService.class );

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Opt-out of registration if EnversService is disabled
        if ( !enversService.isEnabled() ) {
            log.debug( "Skipping Envers listener registrations : EnversService disabled" );
            return;
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Opt-out of registration if asked to not register
//        final boolean autoRegister = serviceRegistry.getService( ConfigurationService.class ).getSetting(
//                AUTO_REGISTER,
//                StandardConverters.BOOLEAN,
//                true
//        );
//        if ( !autoRegister ) {
//            log.debug( "Skipping Envers listener registrations : Listener auto-registration disabled" );
//            return;
//        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Verify that the EnversService is fully initialized and ready to go.
        if ( !enversService.isInitialized() ) {
            throw new HibernateException(
                    "Expecting EnversService to have been initialized prior to call to EnversIntegrator#integrate"
            );
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Opt-out of registration if no audited entities found
        if ( !enversService.getEntitiesConfigurations().hasAuditedEntities() ) {
            log.debug( "Skipping Envers listener registrations : No audited entities found" );
            return;
        }

        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // Do the registrations
        final EventListenerRegistry listenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );
        listenerRegistry.addDuplicationStrategy( EnversListenerDuplicationStrategy.INSTANCE );

        if ( enversService.getEntitiesConfigurations().hasAuditedEntities() ) {
            listenerRegistry.appendListeners(
                    EventType.POST_DELETE,
                    new EnversPostDeleteEventListenerImpl( enversService )
            );
//            listenerRegistry.appendListeners(
//                    EventType.POST_INSERT,
//                    new EnversPostInsertEventListenerImpl( enversService )
//            );
//            listenerRegistry.appendListeners(
//                    EventType.PRE_UPDATE,
//                    new EnversPreUpdateEventListenerImpl( enversService )
//            );
//            listenerRegistry.appendListeners(
//                    EventType.POST_UPDATE,
//                    new EnversPostUpdateEventListenerImpl( enversService )
//            );
//            listenerRegistry.appendListeners(
//                    EventType.POST_COLLECTION_RECREATE,
//                    new EnversPostCollectionRecreateEventListenerImpl( enversService )
//            );
//            listenerRegistry.appendListeners(
//                    EventType.PRE_COLLECTION_REMOVE,
//                    new EnversPreCollectionRemoveEventListenerImpl( enversService )
//            );
//            listenerRegistry.appendListeners(
//                    EventType.PRE_COLLECTION_UPDATE,
//                    new EnversPreCollectionUpdateEventListenerImpl( enversService )
//            );
        }
    }
}
