package org.influxdb.jmx;

import org.influxdb.impl.BatchProcessor;

import javax.annotation.Nonnull;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

final class MBeanServer {

    private static final Logger LOGGER = Logger.getLogger(BatchProcessor.class.getName());
    private final javax.management.MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    private static MBeanServer instance;

    private MBeanServer() {
    }


    public void registerMBean(@Nonnull final Object mBeanInstance,
                              @Nonnull final Class mBeanInterface,
                              @Nonnull final String mBeanName) {
        try {

            StandardMBean standardMBean = new StandardMBean(mBeanInstance, mBeanInterface);
            ObjectInstance objectInstance = server.registerMBean(standardMBean, new ObjectName(mBeanName));
            LOGGER.log(Level.FINE, "register mbean: " + objectInstance.getClassName()
                    + " - " + objectInstance.getObjectName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to register MBean " + mBeanName, e);
        }
    }

    public void unregisterMBean(@Nonnull final String mBeanName) {
        try {
            ObjectName objectName = new ObjectName(mBeanName);
            if (server.isRegistered(objectName)) {
                LOGGER.log(Level.FINE, "unregister mbean: " + objectName);
                server.unregisterMBean(objectName);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unable to unregister MBean " + mBeanName, e);
        }
    }

    @Nonnull
    public static synchronized MBeanServer getMBeanServer() {
        if (instance == null) {
            instance = new MBeanServer();
        }
        return instance;
    }
}
