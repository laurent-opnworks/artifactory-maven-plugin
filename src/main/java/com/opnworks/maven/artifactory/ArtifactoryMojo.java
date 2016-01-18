package com.opnworks.maven.artifactory;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.net.URL;

import javax.ws.rs.core.Response;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBusFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.opnworks.jaxrs.codegen.artifactory.StorageResource;

/**
 * Goal that removes released snapshots
 * 
 * @goal removeSnapshots
 * 
 * @phase initialize
 * @requiresProject false
 * @requiresOnline true
 */
public class ArtifactoryMojo extends AbstractMojo {

	private static final String SLASH = "/";
	
	private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

	/**
	 * URL of the Artifactory service.
	 * 
	 * @parameter expression="${artifactory.serviceUrl}" default-value="http://artifactory"
	 * 
	 */
	private URL serviceUrl;

	/**
	 * Path to use for snapshot scanning
	 * 
	 * @parameter expression="${artifactory.rootPath}" default-value=""
	 */
	private String rootPath;

	/**
	 * Key of snaphots repository
	 * 
	 * @parameter expression="${artifactory.snapshotsRepoKey}" default-value="snapshots"
	 */
	private String snapshotsRepoKey;

	/**
	 * Key of release repository
	 * 
	 * @parameter expression="${artifactory.releasesRepoKey}" default-value="releases"
	 */
	private String releasesRepoKey;

	/**
	 * Id of the server entry containing Artifactory credentials
	 * 
	 * @parameter expression="${artifactory.serverId}" default-value=""
	 */
	private String serverId;

	/**
	 * When set to true, the goal is executed without actually deleting any artifacts
	 * 
	 * @parameter expression="${artifactory.dryRun}" default-value="false"
	 */
	private boolean dryRun = false;

	/**
	 * @parameter default-value="${settings}"
	 */
	private Settings settings;

	public void execute() throws MojoExecutionException, MojoFailureException {

		try {
			this.removeSnapshots();
		}
		catch (Exception e) {
			throw new MojoFailureException(e.getMessage());
		}
	}

	protected void removeSnapshots() throws Exception {

		SpringBusFactory bf = new SpringBusFactory();
		URL busFile = ArtifactoryMojo.class.getResource("/client/client.xml");

		Bus bus = bf.createBus(busFile.toString());
		SpringBusFactory.setDefaultBus(bus);
		SpringBusFactory.setThreadDefaultBus(bus);

		String urlString = serviceUrl.toString() + "/api/";

		getLog().debug("Actual service url: " + urlString);

		StorageResource proxy = JAXRSClientFactory.create(urlString, StorageResource.class);

		traverseSnapshotTree(proxy, snapshotsRepoKey, rootPath);
		bus.shutdown(true);
	}

	protected void traverseSnapshotTree(StorageResource proxy, String repoKey, String path) throws Exception {
		getLog().debug("repoKey: " + repoKey + ", path: " + path);
		JSONArray tree = getSubFolders(proxy, repoKey, path);
		for (int index = 0; index < tree.length(); index++ ) {
			JSONObject folder = tree.getJSONObject(index);
			String folderUri = folder.getString("uri");
			if (folder.getBoolean("folder")) {
				String folderPath = path + folderUri;

				if (folderPath.endsWith(SNAPSHOT_SUFFIX)) {
					if (wasReleased(proxy, folderPath)) {
						
						Response response = proxy.getStorageInfo(repoKey + SLASH + folderPath, null, null, null, null, null, null, null, null, null,
								null, null);
						String s = response.readEntity(String.class);
						JSONObject jsonObject = new JSONObject(s);

						String urlString = serviceUrl.toString() + SLASH + jsonObject.getString("repo") + jsonObject.getString("path");
						getLog().info("About to delete: " + urlString);
						WebClient client = WebClient.create(urlString);

						// Replace 'user' and 'password' by the actual values
						if (!serverId.isEmpty()) {
							Server server = settings.getServer(serverId);
							if (server != null) {
								String credentials = server.getUsername() + ":" + server.getPassword();
								
								String authorizationHeader = "Basic "
										+ org.apache.cxf.common.util.Base64Utility.encode(credentials
												.getBytes());
								// web clients
								client.header("Authorization", authorizationHeader);
							}
						}

						if (!dryRun) {
							Response resp = client.delete();
							if (resp.getStatus() != 204) {
								getLog().error("Problem deleting " + folderPath + ". Http status: " + resp.getStatus());
							}
							else {
								getLog().info("Deleted: " + folderPath + " = " + resp.getStatus());
							}
						}
						break;
					}
				}
				else {
					// Recursive call to called method
					traverseSnapshotTree(proxy, repoKey, path + folderUri);
				}
			}
		}
	}

	private boolean wasReleased(StorageResource proxy, String folderPath) {
		String releasepath = folderPath.substring(0, folderPath.indexOf(SNAPSHOT_SUFFIX));
		String fullpath = releasesRepoKey + SLASH + releasepath;
		Response storageInfo = proxy.getStorageInfo(fullpath, null, null, null, null, null, null, null, null, null, null, null);

		boolean wasReleased = 200 == storageInfo.getStatus();
		getLog().debug("wasReleased: " + folderPath + " = " + wasReleased);
		return wasReleased;
	}

	private JSONArray getSubFolders(StorageResource proxy, String repoKey, String path) throws MojoExecutionException {

		String fullpath = repoKey + SLASH + path;
		getLog().debug("getSubFolders() for path=" + fullpath);
		Response response = proxy.getStorageInfo(fullpath, null, null, null, null, null, null, null, null, null, null, null);
		
		if (response.getStatus() != 200) {
			throw new MojoExecutionException("Problem retrieving resource at: " + fullpath + 
					". HttpStatus: " + response.getStatus() + ". Reason: " + response.getStatusInfo().getReasonPhrase());
		}
		
		String s = response.readEntity(String.class);
		try {
			JSONObject jsonObject = new JSONObject(s);
			getLog().debug("Children: " + jsonObject.getJSONArray("children"));
			JSONArray children = jsonObject.getJSONArray("children");
			return children;
		} catch (JSONException e) {
			throw new MojoExecutionException("Problem retrieving resource at: " + fullpath +
					".Cause: " + e.getMessage(), e);
		}
	}
}
