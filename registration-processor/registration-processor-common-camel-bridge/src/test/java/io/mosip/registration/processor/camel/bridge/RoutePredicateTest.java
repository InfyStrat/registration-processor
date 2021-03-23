
package io.mosip.registration.processor.camel.bridge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.camel.bridge.intercepter.RouteIntercepter;
import io.mosip.registration.processor.camel.bridge.intercepter.RoutePredicate;
import io.mosip.registration.processor.camel.bridge.model.Setting;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*" })

@AutoConfigureMockMvc
public class RoutePredicateTest {

	private RoutePredicate routePredicate;

	private ObjectMapper objectMapper;

	private Exchange exchange;

	private static final Logger LOGGER = RegProcessorLogger.getLogger(RoutePredicateTest.class);

	@Before
	public void init() {
		objectMapper = new ObjectMapper();
		routePredicate = new RoutePredicate();

		ReflectionTestUtils.setField(routePredicate, "hotlistedTagKey", "HOTLISTED");
		Setting[] settings = null;
		try {
			settings = objectMapper.readValue(
					"[{\"hotlistedReason\": \"operator\",\"pauseFor\": 432000,\"defaultResumeAction\": \"<ResumeProcessing/ResumeFromBeginning/StopProcessing>\",\"fromAddress\": \"bio-debup-bus-out\"},{\"hotlistedReason\": \"center\",\"pauseFor\": 432000,\"defaultResumeAction\": \"<ResumeProcessing/ResumeFromBeginning/StopProcessing>\",\"fromAddress\": \"bio-debup-bus-out\"}]",
					Setting[].class);
		} catch (IOException e) {
			LOGGER.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
					"RoutePredicate::exception " + e.getMessage());
		}
		ReflectionTestUtils.setField(routePredicate, "settings", settings);
		ReflectionTestUtils.setField(routePredicate, "objectMapper", objectMapper);
		
		Endpoint endpoint = new DefaultEndpoint() {

			@Override
			public String toString() {

				return "bio-debup-bus-out";
			}

			@Override
			public boolean isSingleton() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Producer createProducer() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Consumer createConsumer(Processor processor) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		};

		exchange = new DefaultExchange(endpoint);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRoutePredicatePositive() throws Exception {
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setRid("10002100741000120201231071308");
		Map<String, String> tags = new HashMap<>();
		tags.put("HOTLISTED", "operator");
		messageDTO.setTags(tags);
		exchange.getMessage().setBody(objectMapper.writeValueAsString(messageDTO));
		assertTrue(routePredicate.matches(exchange));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRoutePredicateHotListedNotFound() throws Exception {
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setRid("10002100741000120201231071308");
		Map<String, String> tags = new HashMap<>();
		messageDTO.setTags(tags);
		exchange.getMessage().setBody(objectMapper.writeValueAsString(messageDTO));
		assertFalse(routePredicate.matches(exchange));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRoutePredicateFromAddressMismatch() throws Exception {
		Endpoint wrongEndpoint = new DefaultEndpoint() {
			@Override
			public String toString() {
				// TODO Auto-generated method stub
				return "wrongendpointtest";
			}

			@Override
			public boolean isSingleton() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public Producer createProducer() throws Exception {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Consumer createConsumer(Processor processor) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		};
		exchange.setFromEndpoint(wrongEndpoint);
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setRid("10002100741000120201231071308");
		Map<String, String> tags = new HashMap<>();
		tags.put("HOTLISTED", "operator");
		messageDTO.setTags(tags);
		exchange.getMessage().setBody(objectMapper.writeValueAsString(messageDTO));
		assertFalse(routePredicate.matches(exchange));

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRoutePredicateHotListedReasonMismatch() throws Exception {
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setRid("10002100741000120201231071308");
		Map<String, String> tags = new HashMap<>();
		tags.put("HOTLISTED", "hostlistedwrongtest");
		messageDTO.setTags(tags);
		exchange.getMessage().setBody(objectMapper.writeValueAsString(messageDTO));
		assertFalse(routePredicate.matches(exchange));

	}

}
