//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Add network adapter and assign the nic to specified distributed switch portgroup
import java.net.URL;

import com.vmware.vim25.Description;
import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardDistributedVirtualPortBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.mo.DistributedVirtualPortgroup;
import com.vmware.vim25.mo.DistributedVirtualSwitch;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class AddandReconfigVMNicsToDVS {

	public static void main(String[] args) {

		String IPAddress = "10.10.10.1"; //VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; //VC username
		String passwd = "kvmklvmkdf"; // VC password
		String dvSwitchName = "dvSwitch"; // dvSwitch name
		String dvPortgroupName = "dvPortGroup"; // dvSwitch portgroup name
		String portGroupKey = null;
		String dvsUUID = null;
		String vmName = "centos1"; // VM name
		ServiceInstance si = null;
		
		try {
			si = new ServiceInstance(new URL("https://"
					+ IPAddress + "/sdk"), userName, passwd, true);
			Folder rootFolder = si.getRootFolder();
			VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
					rootFolder).searchManagedEntity("VirtualMachine",
							vmName);
			DistributedVirtualSwitch dvs = (DistributedVirtualSwitch) new InventoryNavigator(
					si.getRootFolder()).searchManagedEntity(
					"DistributedVirtualSwitch", dvSwitchName);
			DistributedVirtualPortgroup[] dvPortgroup = dvs.getPortgroup();
			for (int j = 0; j < dvPortgroup.length; j++) {
				String portGroupName = dvPortgroup[j].getName();
				if (portGroupName.equalsIgnoreCase(dvPortgroupName)) {
					portGroupKey = dvPortgroup[j].getKey();
				}
			}
			dvsUUID = dvs.getUuid();
			for (int j = 0; j < 1; j++) {
				reconfigVMNicConnection(vm, dvsUUID, portGroupKey);
				System.out.println(vm.getName() + " =====> addnic " + (j + 1)
						+ " and reconfig to " + dvPortgroupName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		si.getServerConnection().logout();
	}

	public static void reconfigVMNicConnection(VirtualMachine vm,
			String dvsUUID, String portGroupKey) {
		try {
			VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
			VirtualMachineConfigSpec spec = new VirtualMachineConfigSpec();
			nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
			VirtualEthernetCard nic = new VirtualPCNet32();
			VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
			DistributedVirtualSwitchPortConnection portConn = new DistributedVirtualSwitchPortConnection();
			portConn.setPortgroupKey(portGroupKey);
			portConn.setSwitchUuid(dvsUUID);
			nicBacking.setPort(portConn);
			nic.setBacking(nicBacking);
			nic.setAddressType("Generated");
			Description desc = new Description();
			desc.setLabel(portGroupKey);
			desc.setSummary(portGroupKey);
			nic.setDeviceInfo(desc);
			nicSpec.setDevice(nic);
			spec.setDeviceChange(new VirtualDeviceConfigSpec[] { nicSpec });
			vm.reconfigVM_Task(spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
