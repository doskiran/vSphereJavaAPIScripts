//:: # Author: P Kiran Kumar
//:: # Product/Feature: ESXi/NFS
//:: # Description: Creating/Mounting NFS datastore in ESXi(no connection to vCenter server) using vSphere API. 
//::# How to run this sample: http://vthinkbeyondvm.com/getting-started-with-yavi-java-opensource-java-sdk-for-vmware-vsphere-step-by-step-guide-for-beginners/
package com.vmware.yavijava;

import java.net.URL;

import com.vmware.vim25.HostNasVolumeSpec;
import com.vmware.vim25.mo.HostDatastoreSystem;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;

public class AddNFSDatastoreinESXi {

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out
					.println("Usage: AddNFSDatastoreinESXi hostname username password");
			System.exit(-1);
		}
		String hostname = args[0]; // ESXi IP address
		String username = args[1]; // ESXi username
		String password = args[2]; // ESXi password
		ServiceInstance si = new ServiceInstance(new URL("https://" + hostname
				+ "/sdk"), username, password, true);
		HostSystem host = null;
		ManagedEntity[] hosts;
		try {
			hosts = new InventoryNavigator(si.getRootFolder())
					.searchManagedEntities("HostSystem");
			for (ManagedEntity me : hosts) {
				host = (HostSystem) me;
			}
			if (host != null) {
				HostDatastoreSystem dssystem = host.getHostDatastoreSystem();
				HostNasVolumeSpec NasSpec = new HostNasVolumeSpec();
				NasSpec.setAccessMode("readWrite");
				NasSpec.setLocalPath("NFSShare2");//Name of your choice for NFS share/datastore
				NasSpec.setRemoteHost("192.100.211.6");//NFS server ip
				NasSpec.setRemotePath("/store1");//Mount point on NFS server
				dssystem.createNasDatastore(NasSpec);
				System.out.println("AddNFSDatastoreinESXi API is called");
			}
		} catch (Exception e) {
			System.out.println("Error::" + e.getMessage());
		}
		si.getServerConnection().logout();

	}
}
