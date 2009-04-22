package org.springframework.flex.remoting;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.core.AbstractServiceConfigProcessor;
import org.springframework.util.CollectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.services.RemotingService;
import flex.messaging.services.Service;
import flex.messaging.services.remoting.adapters.JavaAdapter;

/**
 * {@link MessageBrokerConfigProcessor} implementation that installs a default
 * RemotingService if one has not already been configured through the BlazeDS
 * XML configuration.
 * 
 * <p>
 * Using this processor makes the traditional <code>remoting-config.xml</code>
 * file in BlazeDS XML configuration unnecessary when exclusively using Spring
 * to configure Flex remoting destinations.
 * 
 * <p>
 * This processor is installed automatically when using the
 * <code>message-broker</code> tag in the xml namespace configuration. Its
 * settings can be customized using the <code>remoting-service</code> child tag.
 * See the XSD docs for more detail.
 * 
 * @author Jeremy Grelle
 */
public class RemotingServiceConfigProcessor extends
		AbstractServiceConfigProcessor {

	private static final Log log = LogFactory
			.getLog(RemotingServiceConfigProcessor.class);

	@Override
	protected String getServiceClassName() {
		return RemotingService.class.getName();
	}

	@Override
	protected String getServiceId() {
		return "remoting-service";
	}

	@Override
	protected String getServiceAdapterId() {
		return "java-object";
	}

	@Override
	protected String getServiceAdapterClassName() {
		return JavaAdapter.class.getName();
	}

	/**
	 * Try to find a sensible default AMF channel for the default
	 * RemotingService
	 * 
	 * If a application-level default is set on the MessageBroker, that will be
	 * used. Otherwise will use the first AMFEndpoint from services-config.xml
	 * that it finds.
	 * 
	 * @param broker
	 * @param service
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void findDefaultChannel(MessageBroker broker, Service service) {
		if (!CollectionUtils.isEmpty(broker.getDefaultChannels())) {
			return;
		}

		Iterator channels = broker.getChannelIds().iterator();
		while (channels.hasNext()) {
			Endpoint endpoint = broker.getEndpoint((String) channels.next());
			if (endpoint instanceof AMFEndpoint) {
				service.addDefaultChannel(endpoint.getId());
				return;
			}
		}
		log
				.warn("No appropriate default channels were detected for the RemotingService.  "
						+ "The channels must be explicitly set on any exported service.");
	}

}