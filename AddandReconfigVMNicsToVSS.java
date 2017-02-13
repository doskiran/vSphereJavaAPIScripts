//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Add network adapter and assign the nic to specified standard switch portgroup
import java.net.URL;

import com.vmware.vim25.Description;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class AddandReconfigVMNicsToVSS {
	public static void main(String[] args) {

		String IPAddress = "10.10.10.1"; //VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; //VC username
		String passwd = "kvmklvmkdf"; // VC password
		String portgroupName = "VM Network"; // Standard switch portgroup name
		String vmName = "centos1"; // VM name
		ServiceInstance si = null;
		
		try {
			si = new ServiceInstance(new URL("https://"
					+ IPAddress + "/sdk"), userName, passwd, true);
			Folder rootFolder = si.getRootFolder();
			VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
					rootFolder).searchManagedEntity("VirtualMachine",
							vmName);
			for (int j = 0; j < 1; j++) {
				reconfigVMNicConnection(vm, portgroupName);
				System.out.println(vm.getName() + " =====> addnic " + (j + 1)
						+ " and reconfig to " + portgroupName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		si.getServerConnection().logout();

	}

	public static void reconfigVMNicConnection(VirtualMachine vm,
			String portgroupName) {
		try {
			VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
			VirtualMachineConfigSpec spec = new VirtualMachineConfigSpec();
			nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
			VirtualEthernetCard nic = new VirtualPCNet32();
			VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
			nicBacking.setDeviceName(portgroupName);
			nic.setBacking(nicBacking);
			Description desc = new Description();
			desc.setLabel(portgroupName);
			desc.setSummary(portgroupName);
			nic.setDeviceInfo(desc);
			nicSpec.setDevice(nic);
			spec.setDeviceChange(new VirtualDeviceConfigSpec[] { nicSpec });
			vm.reconfigVM_Task(spec);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
