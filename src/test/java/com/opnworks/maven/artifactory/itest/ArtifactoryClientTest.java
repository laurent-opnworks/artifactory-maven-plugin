package com.opnworks.maven.artifactory.itest;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilisation du test. Les valeurs par défaut sont les suivantes. Modifier ces valeurs au besoin.
 * 
 * <pre>
 * {@code
 * 	-DwsHost=localhost
 *  -DwsPort=8080
 *  -DwsSslPort=8443
 *  -DwsFile=/fwd-cxf-securite/doubleitService?wsdl
 * }
 * </pre>
 */
public class ArtifactoryClientTest { // extends AbstractBusClientServerTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(ArtifactoryClientTest.class);

	static final String WS_DEFAULT_HOST = "repo.jfrog.org"; 

	static final String WS_DEFAULT_PORT = "80"; //"8081";
	// static final String WS_DEFAULT_SSL_PORT = "8443";
	static final String WS_DEFAULT_FILE = "/artifactory/api/";

	// private static final String NAMESPACE = "http://www.example.org/contract/DoubleIt";
	// private static final QName SERVICE_QNAME = new QName(NAMESPACE, "DoubleItService");

	private String releaseRepoKey = "/libs-releases-local/";
	private String snapshotRepoKey = "/libs-snapshots-local/";

	@BeforeClass
	public static void init() throws Exception {

	}

	protected URL buildWsdlURL(String wsProtocol, String aHost, String aPort, String aFile) throws MalformedURLException {
		String wsHost = System.getProperty("wsHost", aHost);
		int wsPort = Integer.parseInt(System.getProperty("wsPort", aPort));
		String wsFile = System.getProperty("wsFile", aFile);
		URL wsdl = new URL(wsProtocol, wsHost, wsPort, wsFile);
		LOG.info("WSDL location: " + wsdl);

		return wsdl;
	}

	@org.junit.Test
	public void testArtifactoryClient() throws Exception {

		SpringBusFactory bf = new SpringBusFactory();
		URL busFile = ArtifactoryClientTest.class.getResource("/client/client.xml");

		Bus bus = bf.createBus(busFile.toString());
		SpringBusFactory.setDefaultBus(bus);
		SpringBusFactory.setThreadDefaultBus(bus);

		URL url = buildWsdlURL("http", WS_DEFAULT_HOST, WS_DEFAULT_PORT, WS_DEFAULT_FILE);
		//
		// BookStore proxy will get the configuration from Spring

		String urlString = url.toString();

		LOG.info("urlString: " + urlString);

		StorageResource proxy = JAXRSClientFactory.create(urlString, StorageResource.class);

		String path = releaseRepoKey + "org/apache/maven";
		LOG.info("path: " + path);
		Response response = proxy.getStorageInfo(path, null, null, null, null, null, null, null, null, null, null, null);

		LOG.info("proxy.getStorageInfo(path) - Code: " + response.getStatusInfo().getStatusCode());
		LOG.info("Reason: " + response.getStatusInfo().getReasonPhrase());

		LOG.info("Entity: " + response.getEntity());

		String s = response.readEntity(String.class);

		LOG.info("Entity: " + s);
		JSONObject jsonObject = new JSONObject(s);

		LOG.info("Children: " + jsonObject.getJSONArray("children"));

		JSONArray children = jsonObject.getJSONArray("children");
		for (int i = 0; i < children.length(); i++ ) {
			LOG.info("child: " + children.getJSONObject(i));
			JSONObject folder = children.getJSONObject(i);
			LOG.info("folder: " + folder.get("uri"));
		}

		bus.shutdown(true);
	}

	@org.junit.Test
	public void testTreeTraversal() throws Exception {

		SpringBusFactory bf = new SpringBusFactory();
		URL busFile = ArtifactoryClientTest.class.getResource("/client/client.xml");

		Bus bus = bf.createBus(busFile.toString());
		SpringBusFactory.setDefaultBus(bus);
		SpringBusFactory.setThreadDefaultBus(bus);

		URL url = buildWsdlURL("http", WS_DEFAULT_HOST, WS_DEFAULT_PORT, WS_DEFAULT_FILE);
		String urlString = url.toString();

		LOG.info("urlString: " + urlString);

		StorageResource proxy = JAXRSClientFactory.create(urlString, StorageResource.class);

		String path = "org/apache/maven"; 
		traverseTree(proxy, snapshotRepoKey, path);
		bus.shutdown(true);
	}

	@org.junit.Test
	public void testGetFolder() throws Exception {

		SpringBusFactory bf = new SpringBusFactory();
		URL busFile = ArtifactoryClientTest.class.getResource("/client/client.xml");

		Bus bus = bf.createBus(busFile.toString());
		SpringBusFactory.setDefaultBus(bus);
		SpringBusFactory.setThreadDefaultBus(bus);

		URL url = buildWsdlURL("http", WS_DEFAULT_HOST, WS_DEFAULT_PORT, WS_DEFAULT_FILE);
		String urlString = url.toString();

		LOG.info("urlString: " + urlString);

		StorageResource proxy = JAXRSClientFactory.create(urlString, StorageResource.class);

		String path = snapshotRepoKey + "org/apache/maven";
		LOG.info("path: " + path);
		Response response = proxy.getStorageInfo(path, null, null, null, null, null, null, null, null, null, null, null);

		LOG.info("Entity: " + response.getEntity());

		String s = response.readEntity(String.class);
		JSONObject jsonObject = new JSONObject(s);
		LOG.info("Entity: " + jsonObject);
		bus.shutdown(true);
	}

	public void traverseTree(StorageResource proxy, String repoKey, String path) throws Exception {
		LOG.debug("path: " + path);
		JSONArray tree = getSubFolders(proxy, repoKey, path);
		for (int index = 0; index < tree.length(); index++ ) {
			JSONObject folder = tree.getJSONObject(index);
			String folderUri = folder.getString("uri");
			if (folder.getBoolean("folder")) {
				String folderPath = path + folderUri;

				if (folderPath.endsWith("-SNAPSHOT")) {
					if (wasReleased(proxy, folderPath)) {

						Response response = proxy.getStorageInfo(repoKey + folderPath, null, null, null, null, null, null, null, null, null, null,
								null);
						String s = response.readEntity(String.class);
						JSONObject jsonObject = new JSONObject(s);
						URL url = buildWsdlURL("http", WS_DEFAULT_HOST, WS_DEFAULT_PORT, "/artifactory/");

						String urlString = url.toString() + jsonObject.getString("repo") + jsonObject.getString("path");
						
						LOG.info("About to delete: " + urlString);
						WebClient client = WebClient.create(urlString);

						// Replace 'user' and 'password' by the actual values
						String authorizationHeader = "Basic "
								+ org.apache.cxf.common.util.Base64Utility.encode("laurent.gauthier:qaz123qaz".getBytes());
						// web clients
						client.header("Authorization", authorizationHeader);

//						Response resp = client.delete();
//						if (resp.getStatus() != 204) {
//							LOG.error("Problem deleting " + folderPath + ". Http status: " + resp.getStatus());
//						}
//						else {
//							LOG.info("Deleted: " + folderPath + " = " + resp.getStatus());
//						}

						break;
					}
				}
				else {
					traverseTree(proxy, repoKey, path + folderUri);
				}
			}
		}
	}

	private boolean wasReleased(StorageResource proxy, String folderPath) {
		String releasepath = folderPath.substring(0, folderPath.indexOf("-SNAPSHOT"));
		String fullpath = releaseRepoKey + releasepath;
		Response storageInfo = proxy.getStorageInfo(fullpath, null, null, null, null, null, null, null, null, null, null, null);

		boolean wasReleased = 200 == storageInfo.getStatus();
		LOG.info("wasReleased: " + folderPath + " = " + wasReleased);
		return wasReleased;
	}

	private JSONArray getSubFolders(StorageResource proxy, String repoKey, String path) throws JSONException {

		String fullpath = repoKey + path;
		Response response = proxy.getStorageInfo(fullpath, null, null, null, null, null, null, null, null, null, null, null);
		String s = response.readEntity(String.class);
		JSONObject jsonObject = new JSONObject(s);
		LOG.debug("Children: " + jsonObject.getJSONArray("children"));
		JSONArray children = jsonObject.getJSONArray("children");
		return children;
	}

}
