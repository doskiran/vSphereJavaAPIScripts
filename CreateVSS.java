import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.HostNetworkPolicy;
import com.vmware.vim25.HostNetworkSecurityPolicy;
import com.vmware.vim25.HostNetworkTrafficShapingPolicy;
import com.vmware.vim25.HostNicFailureCriteria;
import com.vmware.vim25.HostNicOrderPolicy;
import com.vmware.vim25.HostNicTeamingPolicy;
import com.vmware.vim25.HostVirtualSwitchBondBridge;
import com.vmware.vim25.HostVirtualSwitchSpec;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;

//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Creates Virtual Standard Switch in the specified host using vSphere java API. 
public class CreateVSS {

	public boolean addVirtualSwitch(ServiceInstance si, String hostName, String[] pnicIds) {
		System.out.println("Enter into addVirtualSwitch::" + hostName + " ,pnicIds::" + pnicIds);
		boolean status = false;
		try {
			HostVirtualSwitchSpec spec = new HostVirtualSwitchSpec();
			HostVirtualSwitchBondBridge bridge = new HostVirtualSwitchBondBridge();
			HostNetworkPolicy policy = new HostNetworkPolicy();
			HostSystem hs = (HostSystem) new InventoryNavigator(si.getRootFolder()).searchManagedEntity("HostSystem",
					hostName);
			bridge.setNicDevice(pnicIds);
			spec.setBridge(bridge);
			// spec.setMtu(1600);
			spec.setNumPorts(50);
			policy.setNicTeaming(createDefaultNicTeamingPolicy(pnicIds));
			policy.setSecurity(createDefaultSecurityPolicy());
			policy.setShapingPolicy(createDefaultTrafficShapingPolicy());
			spec.setPolicy(policy);
			hs.getHostNetworkSystem().addVirtualSwitch("vSwitch" + System.currentTimeMillis(), spec);
			status = true;
		} catch (Exception e) {
			System.err.println("Failed to add virtual standard switch::" + e.getLocalizedMessage());
		}
		return status;
	}

	public HostNetworkSecurityPolicy createDefaultSecurityPolicy() throws Exception {
		HostNetworkSecurityPolicy hnSecurityPolicy = new HostNetworkSecurityPolicy();
		hnSecurityPolicy.setAllowPromiscuous(Boolean.FALSE);
		hnSecurityPolicy.setForgedTransmits(Boolean.FALSE);
		hnSecurityPolicy.setMacChanges(Boolean.FALSE);
		return hnSecurityPolicy;
	}

	public HostNetworkTrafficShapingPolicy createDefaultTrafficShapingPolicy() throws Exception {
		HostNetworkTrafficShapingPolicy hnTrafficShapingPolicyPolicy = new HostNetworkTrafficShapingPolicy();
		hnTrafficShapingPolicyPolicy.setEnabled(Boolean.FALSE);
		return hnTrafficShapingPolicyPolicy;
	}

	public HostNicTeamingPolicy createDefaultNicTeamingPolicy(String[] pnicIds) throws Exception {
		HostNicTeamingPolicy hnNicTeamingPolicy = new HostNicTeamingPolicy();
		hnNicTeamingPolicy.setPolicy("loadbalance_ip");
		hnNicTeamingPolicy.setNotifySwitches(Boolean.FALSE);
		hnNicTeamingPolicy.setReversePolicy(Boolean.FALSE);
		HostNicFailureCriteria hnFailureCriteria = new HostNicFailureCriteria();
		hnFailureCriteria.setCheckBeacon(Boolean.FALSE);
		hnFailureCriteria.setCheckDuplex(Boolean.FALSE);
		hnFailureCriteria.setCheckErrorPercent(Boolean.FALSE);
		hnFailureCriteria.setCheckSpeed("minimum");
		hnFailureCriteria.setFullDuplex(Boolean.FALSE);
		hnFailureCriteria.setPercentage(0);
		hnFailureCriteria.setSpeed(10);
		hnNicTeamingPolicy.setFailureCriteria(hnFailureCriteria);
		HostNicOrderPolicy nicOrderPolicy = new HostNicOrderPolicy();
		nicOrderPolicy.setActiveNic(pnicIds);
		hnNicTeamingPolicy.setNicOrder(nicOrderPolicy);
		hnNicTeamingPolicy.setRollingOrder(Boolean.FALSE);
		return hnNicTeamingPolicy;
	}

	public static void main(String[] args) throws RemoteException, MalformedURLException {
		String vcIPaddress = "10.10.10.100"; // VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; // VC username
		String passwd = "scc$2j%1f45"; // VC password
		String hostName = "10.10.10.120"; // ESXi ipaddress/hostname
		String[] pnicIds = { "vmnic1" }; // "vmnic1","vmnic2",... // ESXi
											// physical nics
		ServiceInstance si = new ServiceInstance(new URL("https://" + vcIPaddress + "/sdk"), userName, passwd, true);
		System.out.println("Add VirtualSwitch status::" + new CreateVSS().addVirtualSwitch(si, hostName, pnicIds));
	}

}
