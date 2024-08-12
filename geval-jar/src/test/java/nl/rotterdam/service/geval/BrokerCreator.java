/*
 * Copyright 2022, Gemeente Rotterdam, auteursrecht voorbehouden - BCO, the Netherlands
 *
 */
package nl.rotterdam.service.geval;


import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerPlugin;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.IndividualDeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.RedeliveryPolicyMap;
import org.apache.activemq.broker.util.RedeliveryPlugin;

/**
 * Initialize AMQ broker.
 */
@SuppressWarnings("unused")
public class BrokerCreator {

    public static final String QUEUE_FOUTAFHANDELAAR_JNDI_P_NAME = "jms/queue/pAlgemeenFoutafhandelingOpvoerenFoutCdm2Fou";

    public static final String QUEUE_FOUTAFHANDELAAR_NAME = "Algemeen.Foutafhandeling.OpvoerenFout";

    public static final String PRODUCER_CONNECTION_FACTORY_JNDI_NAME = "jms/ProducerConnectionFactory";

    private static BrokerService broker;

    public static Object getResourceByJndiName(String name) {
        try {
            return new InitialContext().lookup(name);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void createForTesting() {
        try {
            if (broker == null) {
                broker = BrokerCreator.startBroker();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static BrokerService startBroker() throws Exception {
        final BrokerService broker = new BrokerService();
        broker.setPersistent(false);
        PolicyMap destinationPolicy = new PolicyMap();
        PolicyEntry dlqPolicy = new PolicyEntry();
        dlqPolicy.setProducerFlowControl(true);
        dlqPolicy.setQueuePrefetch(10);
        IndividualDeadLetterStrategy deadLetterStrategy = new IndividualDeadLetterStrategy();
        deadLetterStrategy.setQueuePrefix("DLQ.");
        deadLetterStrategy.setUseQueueForQueueMessages(true);
        dlqPolicy.setDeadLetterStrategy(deadLetterStrategy);
        dlqPolicy.setQueue(">");
        destinationPolicy.setDefaultEntry(dlqPolicy);
        broker.setDestinationPolicy(destinationPolicy);
        RedeliveryPlugin redeliveryPlugin = new RedeliveryPlugin();
        RedeliveryPolicyMap redeliveryPolicyMap = new RedeliveryPolicyMap();
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setInitialRedeliveryDelay(1000L);
        redeliveryPolicy.setMaximumRedeliveries(1);
        redeliveryPolicy.setMaximumRedeliveryDelay(5000L);
        redeliveryPolicyMap.setDefaultEntry(redeliveryPolicy);
        redeliveryPlugin.setRedeliveryPolicyMap(redeliveryPolicyMap);
        BrokerPlugin[] plugins = new BrokerPlugin[1];
        plugins[0] = redeliveryPlugin;
        broker.setPlugins(plugins);
        broker.setSchedulerSupport(true);
        broker.start();
        return broker;
    }
}
