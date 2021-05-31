import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.vmware.vim25.HttpNfcLeaseDeviceUrl;
import com.vmware.vim25.HttpNfcLeaseInfo;
import com.vmware.vim25.HttpNfcLeaseState;
import com.vmware.vim25.OvfCreateImportSpecParams;
import com.vmware.vim25.OvfCreateImportSpecParamsDiskProvisioningType;
import com.vmware.vim25.OvfCreateImportSpecResult;
import com.vmware.vim25.OvfFileItem;
import com.vmware.vim25.mo.ClusterComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.HttpNfcLease;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.OvfManager;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class DeployOVF {

	static {
		disableSslVerification();
	}

	public static void disableSslVerification() {
		try {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {

				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (

		NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

	private ServiceInstance si;
	private static final String DATASTORE = "Datastore";
	private static final String HOSTSYSTEM = "HostSystem";
	private static final String DATACENTER = "Datacenter";

	public DeployOVF(String vcIPaddress, String userName, String password) {
		try {
			si = new ServiceInstance(new URL("https://" + vcIPaddress + "/sdk"), userName, password, true);
		} catch (RemoteException | MalformedURLException e) {
			System.err.println("Unable to connect to vCenter..." + vcIPaddress);
		}
	}

	public VirtualMachine deployOVF(String hostname, String datastoreName, String ovfFilePath) {
		System.out.println("Enter into deployOVF::" + hostname + " , " + datastoreName + " , " + ovfFilePath);
		VirtualMachine vm = null;
		ManagedEntity compResMor = null;
		ResourcePool resourcePool = null;
		Datastore datastore = null;
		HostSystem hs = null;
		Datacenter dc = null;
		try {
			datastore = (Datastore) new InventoryNavigator(si.getRootFolder()).searchManagedEntity(DATASTORE,
					datastoreName);
			hs = (HostSystem) new InventoryNavigator(si.getRootFolder()).searchManagedEntity(HOSTSYSTEM, hostname);
			dc = getDatacenterFromHost(si, hs);
			compResMor = hs.getParent();
			ClusterComputeResource ccr = new ClusterComputeResource(si.getServerConnection(), compResMor.getMOR());
			resourcePool = ccr.getResourcePool();

			OvfManager ovfManager = si.getOvfManager();
			OvfCreateImportSpecParams ovfCreateImpSpecParms = new OvfCreateImportSpecParams();
			ovfCreateImpSpecParms.setDiskProvisioning(OvfCreateImportSpecParamsDiskProvisioningType.thin.toString());
			ovfCreateImpSpecParms.setEntityName("NewOvfVM" + System.currentTimeMillis());
			ovfCreateImpSpecParms.setLocale("");
			ovfCreateImpSpecParms.setDeploymentOption("");
			ovfCreateImpSpecParms.setHostSystem(hs.getMOR());
			ovfCreateImpSpecParms.setPropertyMapping(null);
			StringBuffer buffer = new StringBuffer();
			String ovfDescriptor = null;
			FileReader fr = new FileReader(ovfFilePath);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				buffer.append(line + "\n");
			}
			fr.close();
			ovfDescriptor = buffer.toString();
			OvfCreateImportSpecResult ovfCreateImpSpecResult = ovfManager.createImportSpec(ovfDescriptor, resourcePool,
					datastore, ovfCreateImpSpecParms);
			HttpNfcLease httpNfcLease = resourcePool.importVApp(ovfCreateImpSpecResult.getImportSpec(),
					dc.getVmFolder(), hs);
			Path path = Paths.get(ovfFilePath);
			ovfFilePath = path.getParent().toString().replace("\\", "/") + "/";
			System.out.println("ovfFilePath Parent::" + ovfFilePath);
			Thread thread = uploadVMDK(httpNfcLease, ovfCreateImpSpecResult, hostname, ovfFilePath);
			int percent = 1;
			while (thread.isAlive()) {
				httpNfcLease.httpNfcLeaseProgress(percent);
				if (percent < 99) {
					percent++;
				}
				Thread.sleep(3000);
			}
			httpNfcLease.httpNfcLeaseComplete();
			vm = new VirtualMachine(si.getServerConnection(), httpNfcLease.getInfo().getEntity());
			System.out.println("Successfully VM deployed::" + vm.getName());
		} catch (Exception e) {
			System.err.println("Error::" + e.getMessage());
		}

		return vm;
	}

	public Datacenter getDatacenterFromHost(ServiceInstance si, HostSystem hs) {
		Datacenter dc = null;
		ManagedEntity me = hs.getParent();
		boolean status = false;
		String type = null;
		while (!status) {
			type = me.getMOR().getType();
			if (type.equalsIgnoreCase(DATACENTER)) {
				dc = new Datacenter(si.getServerConnection(), me.getMOR());
				status = true;
			} else {
				me = me.getParent();
			}
		}
		return dc;
	}

	public Thread uploadVMDK(HttpNfcLease httpNfcLease, OvfCreateImportSpecResult importSpecResult, String hostName,
			String ovfFilePath) {
		UploadVMDK uploadVmdk = new UploadVMDK(httpNfcLease, importSpecResult, hostName, ovfFilePath);
		Thread thread = new Thread(uploadVmdk);
		thread.start();
		return thread;
	}

	public static long addTotalBytes(OvfCreateImportSpecResult ovfImportResult) {
		OvfFileItem[] fileItems = ovfImportResult.getFileItem();
		long totalBytes = 0;
		if (fileItems != null) {
			for (OvfFileItem ovfFI : fileItems) {
				System.out.println("DeviceId: " + ovfFI.getDeviceId());
				System.out.println("Path: " + ovfFI.getPath());
				System.out.println("Size: " + ovfFI.getSize());
				totalBytes += ovfFI.getSize();
			}
			System.out.println("Total Bytes::" + totalBytes);
		}
		return totalBytes;
	}

	private static void uploadVMDKFile(String vmdkFile, String url) throws IOException {
		System.out.println("Enter into uploadVMDK::" + vmdkFile + " , " + url);
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String urlHostName, SSLSession session) {
				return true;
			}
		});
		int CHUCK_LEN = 64 * 1024;
		HttpsURLConnection httpConn = (HttpsURLConnection) new URL(url).openConnection();
		httpConn.setDoOutput(true);
		httpConn.setUseCaches(false);
		httpConn.setChunkedStreamingMode(100 * 1024 * 1024);
		httpConn.setRequestMethod("POST");
		httpConn.setRequestProperty("Connection", "Keep-Alive");
		httpConn.setRequestProperty("Expect", "100-continue");
		httpConn.setRequestProperty("Overwrite", "t");
		httpConn.setRequestProperty("Content-Type", "application/x-vnd.vmware-streamVmdk");
		httpConn.setRequestProperty("Content-Length", Long.toString(new File(vmdkFile).length()));
		httpConn.connect();
		BufferedOutputStream bos = new BufferedOutputStream(httpConn.getOutputStream());
		BufferedInputStream diskis = new BufferedInputStream(new FileInputStream(vmdkFile));
		int bytesAvailable = diskis.available();
		int bufferSize = Math.min(bytesAvailable, CHUCK_LEN);
		byte[] buffer = new byte[bufferSize];
		long totalBytesUploaded = 0;
		while (true) {
			int bytesRead = diskis.read(buffer, 0, bufferSize);
			if (bytesRead == -1) {
				System.out.println("Total Bytes Uploaded: " + totalBytesUploaded);
				break;
			}
			totalBytesUploaded += bytesRead;
			bos.write(buffer, 0, bufferSize);
			bos.flush();
			System.out.println("Total Bytes Uploaded: " + totalBytesUploaded);

		}
		diskis.close();
		bos.flush();
		bos.close();
		httpConn.disconnect();
	}

	private class UploadVMDK extends Thread {
		private HttpNfcLease httpNfcLease;
		private OvfCreateImportSpecResult importSpecResult;
		private String hostName;
		private String ovfFilePath;

		UploadVMDK(HttpNfcLease httpNfcLease, OvfCreateImportSpecResult importSpecResult, String hostName,
				String ovfFilePath) {
			this.httpNfcLease = httpNfcLease;
			this.importSpecResult = importSpecResult;
			this.hostName = hostName;
			this.ovfFilePath = ovfFilePath;
		}

		public void run() {
			try {
				HttpNfcLeaseState state;
				while (true) {
					state = httpNfcLease.getState();
					if (state == HttpNfcLeaseState.ready || state == HttpNfcLeaseState.error)
						break;
				}
				if (state == HttpNfcLeaseState.ready) {
					HttpNfcLeaseInfo httpNfcLeaseInfo = httpNfcLease.getInfo();
					HttpNfcLeaseDeviceUrl[] nfcDeviceUrls = httpNfcLeaseInfo.getDeviceUrl();
					for (HttpNfcLeaseDeviceUrl nfsDeviceUrl : nfcDeviceUrls) {
						String deviceKey = nfsDeviceUrl.getImportKey();
						for (OvfFileItem ovfFileItem : importSpecResult.getFileItem()) {
							if (deviceKey.equals(ovfFileItem.getDeviceId())) {
								String vmdkFile = ovfFilePath + ovfFileItem.getPath();
								uploadVMDKFile(vmdkFile, nfsDeviceUrl.getUrl().replace("*", hostName));
								System.out.println("Successfully completed uploading the VMDK::" + vmdkFile);
							}
						}
					}
				}
			} catch (Exception e) {
				System.err.println("Error::" + e.getMessage());
			}
		}
	}

	public static void main(String[] args) {
		String vcIPaddress = "10.10.10.1"; // VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; // VC username
		String passwd = "jcdskjcsdk"; // VC password
		String hostName = "10.10.10.2";
		String datastoreName = "nfs";
		String ovfFilePath = "C:/ovf/centos.ovf";
		DeployOVF ovf = new DeployOVF(vcIPaddress, userName, passwd);
		VirtualMachine vm = ovf.deployOVF(hostName, datastoreName, ovfFilePath);
	}

}
