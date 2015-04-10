package org.ojbc.federatedquery.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;

import javax.inject.Inject;

import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ojbc.util.osgi.OjbcContext;
import org.ojbc.util.osgi.test.AbstractPaxExamIntegrationTest;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.Filter;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;

/**
 * Integration test for federated query bundles using Pax Exam.
 * 
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class FederatedQueryIntegrationTest extends AbstractPaxExamIntegrationTest {

	private static final Log log = LogFactory.getLog(FederatedQueryIntegrationTest.class);

	private static final String KARAF_VERSION = "2.2.11";

	//Person Search
	@Inject
	@Filter(value = "(org.springframework.osgi.bean.name=org.ojbc.bundles.intermediaries.person-search-request-service-intermediary-context)", timeout = 60000)
	private OjbcContext personSearchIntermediaryOjbcContext;

	@Inject
	@Filter(value = "(org.springframework.context.service.name=org.ojbc.bundles.intermediaries.person-search-request-service-intermediary)", timeout = 60000)
	private ApplicationContext personSearchIntermediaryBundleContext;

	//Vehicle Search
	@Inject
	@Filter(value = "(org.springframework.osgi.bean.name=org.ojbc.bundles.intermediaries.vehicle-search-request-service-intermediary-context)", timeout = 60000)
	private OjbcContext vehicleSearchIntermediaryOjbcContext;

	@Inject
	@Filter(value = "(org.springframework.context.service.name=org.ojbc.bundles.intermediaries.vehicle-search-request-service-intermediary)", timeout = 60000)
	private ApplicationContext vehicleSearchIntermediaryBundleContext;
	
	//Firearms Search
	@Inject
	@Filter(value = "(org.springframework.osgi.bean.name=org.ojbc.bundles.intermediaries.firearms-search-request-service-intermediary-context)", timeout = 60000)
	private OjbcContext firearmsSearchIntermediaryOjbcContext;

	@Inject
	@Filter(value = "(org.springframework.context.service.name=org.ojbc.bundles.intermediaries.firearm-search-request-service-intermediary)", timeout = 60000)
	private ApplicationContext firearmsSearchIntermediaryBundleContext;	
	
	//Person Query - Warrants
	@Inject
	@Filter(value = "(org.springframework.osgi.bean.name=org.ojbc.bundles.intermediaries.person-query-service-warrants-intermediary-context)", timeout = 60000)
	private OjbcContext warrantsQueryIntermediaryOjbcContext;

	@Inject
	@Filter(value = "(org.springframework.context.service.name=org.ojbc.bundles.intermediaries.person-query-service-warrants-intermediary)", timeout = 60000)
	private ApplicationContext warrantsQueryIntermediaryBundleContext;
	
	//Person Query - Criminal History
	@Inject
	@Filter(value = "(org.springframework.osgi.bean.name=org.ojbc.bundles.intermediaries.person-query-service-criminal-history-intermediary-context)", timeout = 60000)
	private OjbcContext criminalHistoryQueryIntermediaryOjbcContext;

	@Inject
	@Filter(value = "(org.springframework.context.service.name=org.ojbc.bundles.intermediaries.person-query-service-criminal-history-intermediary)", timeout = 60000)
	private ApplicationContext criminalHistoryQueryIntermediaryBundleContext;	
	
	//Firearms Query
	@Inject
	@Filter(value = "(org.springframework.osgi.bean.name=org.ojbc.bundles.intermediaries.firearm-registration-query-request-service-intermediary-context)", timeout = 60000)
	private OjbcContext firearmsQueryIntermediaryOjbcContext;

	@Inject
	@Filter(value = "(org.springframework.context.service.name=org.ojbc.bundles.intermediaries.firearm-registration-query-request-service-intermediary)", timeout = 60000)
	private ApplicationContext firearmsQueryIntermediaryBundleContext;	
	
	@Configuration
	public Option[] config() {

		MavenArtifactUrlReference karafUrl = maven().groupId("org.apache.karaf").artifactId("apache-karaf").version(KARAF_VERSION).type("zip");

		MavenUrlReference karafCamelFeature = maven().groupId("org.apache.camel.karaf").artifactId("apache-camel").version("2.10.7").classifier("features").type("xml");

		return new Option[] {
				
				//Need extra memory due to the number of bundles being installed and their service specs
				new VMOption("-Xmx1G"),
				
				karafDistributionConfiguration().frameworkUrl(karafUrl).karafVersion(KARAF_VERSION).name("Apache Karaf").unpackDirectory(new File("target/exam")).useDeployFolder(false),

				//keepRuntimeFolder(), 	
				logLevel(LogLevel.INFO),

				KarafDistributionOption.replaceConfigurationFile("etc/org.ops4j.pax.url.mvn.cfg", new File("src/main/config/org.ops4j.pax.url.mvn.cfg")),
				KarafDistributionOption.replaceConfigurationFile("etc/ojbc.context.services.cfg", replaceConfigurationFile()),
				
				KarafDistributionOption.features(karafCamelFeature, "camel"),
				KarafDistributionOption.features(karafCamelFeature, "camel-cxf"),
				KarafDistributionOption.features(karafCamelFeature, "camel-saxon"),

				mavenBundle().groupId("commons-codec").artifactId("commons-codec").version("1.6").start(),
				mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.commons-io").version("1.3.2_5").start(),
				mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.xpp3").version("1.1.4c_5").start(),
				mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.dom4j").version("1.6_1").start(),
				mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").version("4.2.5").start(),
				mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").version("4.2.5").start(),
				mavenBundle().groupId("org.springframework").artifactId("spring-jdbc").version("3.0.7.RELEASE").start(),

				// _Common intermediary stuff

				mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.commons-collections").version("3.2.1_3").start(),
				mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.commons-lang").version("2.4_6").start(),

				// Camel Intermediary dependencies

				mavenBundle().groupId("org.osgi").artifactId("org.osgi.enterprise").version("4.2.0").start(),
				mavenBundle().groupId("com.h2database").artifactId("h2").version("1.3.174").start(),
				mavenBundle().groupId("commons-pool").artifactId("commons-pool").version("1.6").start(),
				mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.commons-dbcp").version("1.2.2_7").start(),

				// OJB bundles here
				mavenBundle().groupId("org.ojbc.bundles.utilities").artifactId("h2-mock-database").start(),
				
				mavenBundle().groupId("org.ojbc.bundles.shared").artifactId("ojb-osgi-test-common").start(),
				mavenBundle().groupId("org.ojbc.bundles.shared").artifactId("ojb-common").start(),
				mavenBundle().groupId("org.ojbc.bundles.shared").artifactId("ojb-camel-common").start(),
				mavenBundle().groupId("org.ojbc.bundles.shared").artifactId("ojb-fedquery-common").start(),
				mavenBundle().groupId("org.ojbc.bundles.shared").artifactId("ojb-resources-common").start(),
				mavenBundle().groupId("org.ojbc.bundles.shared").artifactId("ojb-osgi-common").start(),
								
				//Entity Resolution Bundles
				mavenBundle().groupId("gov.nij.bundles.shared").artifactId("Entity_Resolution_Resources").start(),
				
				// Search intermediaries
				mavenBundle().groupId("org.ojbc.bundles.intermediaries").artifactId("person-search-request-service-intermediary").start(),
				mavenBundle().groupId("org.ojbc.bundles.intermediaries").artifactId("vehicle-search-request-service-intermediary").start(),
				mavenBundle().groupId("org.ojbc.bundles.intermediaries").artifactId("firearm-search-request-service-intermediary").start(),
				
				// Query intermediaries
				mavenBundle().groupId("org.ojbc.bundles.intermediaries").artifactId("person-query-service-warrants-intermediary").start(),
				mavenBundle().groupId("org.ojbc.bundles.intermediaries").artifactId("person-query-service-criminal-history-intermediary").start(),
				mavenBundle().groupId("org.ojbc.bundles.intermediaries").artifactId("firearm-registration-query-request-service-intermediary").start()
		};
	}

	private File replaceConfigurationFile() {
		File contextConfigurationFile =new File("src/main/config/ojbc.context.services.cfg"); 
		return contextConfigurationFile;
	}

	@Before
	public void setup() throws Exception {
		bundleContext.registerService(OsgiBundleApplicationContextListener.class.getName(), new ContextListener(), null);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetup() throws Exception {
		assertNotNull(personSearchIntermediaryOjbcContext);
		assertNotNull(personSearchIntermediaryBundleContext);
		assertNotNull(vehicleSearchIntermediaryOjbcContext);
		assertNotNull(vehicleSearchIntermediaryBundleContext);
		assertNotNull(warrantsQueryIntermediaryOjbcContext);
		assertNotNull(warrantsQueryIntermediaryBundleContext);
		assertNotNull(criminalHistoryQueryIntermediaryOjbcContext);
		assertNotNull(criminalHistoryQueryIntermediaryBundleContext);
		assertNotNull(firearmsQueryIntermediaryOjbcContext);
		assertNotNull(firearmsQueryIntermediaryBundleContext);
		assertNotNull(firearmsSearchIntermediaryOjbcContext);
		assertNotNull(firearmsSearchIntermediaryBundleContext);
		
		System.err.println(executeCommand("osgi:list -t 1", 20000L, false));
	}
	
	@Test
	public void testQueryBundleStartup() throws Exception {
		
		//Person Search
		CxfEndpoint personSearchRequestEndpoint = personSearchIntermediaryBundleContext.getBean("searchRequestFederatedServiceEndpoint", CxfEndpoint.class);
		String personSearchRequestEndpointAddress = personSearchRequestEndpoint.getAddress();

		assertNotNull(personSearchRequestEndpoint);
		log.info("Person Search Federated Endpoint: " + personSearchRequestEndpointAddress);

		//Vehicle Search
		CxfEndpoint vehicleSearchRequestEndpoint = vehicleSearchIntermediaryBundleContext.getBean("searchRequestFederatedServiceEndpoint", CxfEndpoint.class);
		String vehicleSearchRequestEndpointAddress = vehicleSearchRequestEndpoint.getAddress();

		assertNotNull(vehicleSearchRequestEndpoint);
		log.info("Vehicle Search Federated Endpoint: " + vehicleSearchRequestEndpointAddress);

		//Firearms Search
		CxfEndpoint firearmsSearchRequestEndpoint = firearmsSearchIntermediaryBundleContext.getBean("searchRequestFederatedServiceEndpoint", CxfEndpoint.class);
		String firearmsSearchRequestEndpointAddress = firearmsSearchRequestEndpoint.getAddress();

		assertNotNull(firearmsSearchRequestEndpoint);
		log.info("Firearms Search Federated Endpoint: " + firearmsSearchRequestEndpointAddress);
		
		//Warrants Query
		CxfEndpoint personQueryWarrantsEndpoint = warrantsQueryIntermediaryBundleContext.getBean("searchRequestFederatedServiceEndpoint", CxfEndpoint.class);
		String personQueryWarrantsEndpointAddress = personQueryWarrantsEndpoint.getAddress();

		assertNotNull(personQueryWarrantsEndpoint);
		
		log.info("Person Query Warrants Federated Endpoint: " + personQueryWarrantsEndpointAddress);

		//Criminal History Query
        CxfEndpoint personQueryCriminalHistoryEndpoint = criminalHistoryQueryIntermediaryBundleContext.getBean("searchRequestFederatedServiceEndpoint", CxfEndpoint.class);
        String personQueryCriminalHistoryEndpointAddress = personQueryCriminalHistoryEndpoint.getAddress();
        assertEquals(personQueryCriminalHistoryEndpointAddress, "https://localhost:18601/OJB/PersonQueryService/CriminalHistory");

        assertNotNull(personQueryCriminalHistoryEndpoint);
        
		log.info("Person Query Criminal History Federated Endpoint: " + personQueryCriminalHistoryEndpointAddress);

		//Firearms Query
		CxfEndpoint firearmRegistrationQueryRequestFederatedServiceEndpoint = firearmsQueryIntermediaryBundleContext.getBean("firearmRegistrationQueryRequestFederatedServiceEndpoint", CxfEndpoint.class);
		assertNotNull(firearmRegistrationQueryRequestFederatedServiceEndpoint);
		
		String firearmRegistrationQueryRequestFederatedServiceEndpointAddress = firearmRegistrationQueryRequestFederatedServiceEndpoint.getAddress();
		assertEquals("https://localhost:18604/OJB/FirearmRegistrationQueryRequestService", firearmRegistrationQueryRequestFederatedServiceEndpointAddress);
		
		log.info("Firearms Query Federated Endpoint: " + firearmRegistrationQueryRequestFederatedServiceEndpointAddress);
		
	}



	private static final class ContextListener implements OsgiBundleApplicationContextListener {

		@Override
		public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent e) {
			// you can put stuff in here if you want to be notified of osgi-spring context events
		}
	}

}