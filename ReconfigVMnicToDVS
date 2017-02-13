//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Assign particular dvSwitch portgroup to the specified network adapter.
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.vmware.vim25.Description;
import com.vmware.vim25.DistributedVirtualSwitchPortConnection;
import com.vmware.vim25.VirtualDevice;
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

public class ReconfigVMnicToDVS {

	public static void main(String[] args) {

		String IPAddress = "10.10.10.1"; //VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; //VC username
		String passwd = "kvmklvmkdf"; // VC password
		String dvSwitchName = "dvSwitch"; // dvSwitch name
		String dvPortgroupName = "dvPortGroup"; // dvSwitch portgroup name
		String networkAdapter = "Network adapter 1"; // Network adapter name to assign dvportgroup
		String vmName = "centos1"; // VM Name
		String portGroupKey = null;
		String dvsUUID = null;
		ServiceInstance si = null;
		
		try {
			si = new ServiceInstance(new URL("https://" + IPAddress + "/sdk"),
					userName, passwd, true);
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
			reconfigVMNicConnection(vm, dvsUUID, portGroupKey, networkAdapter);
			System.out.println(vm.getName() + " =====> addnic and reconfig to "
					+ dvPortgroupName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		si.getServerConnection().logout();
	}

	public static void reconfigVMNicConnection(VirtualMachine vm,
			String dvsUUID, String portGroupKey, String networkAdapter) {
		try {

			VirtualMachineConfigSpec spec = new VirtualMachineConfigSpec();
			VirtualDevice[] vdeviceArray = (VirtualDevice[]) vm
					.getPropertyByPath("config.hardware.device");
			VirtualEthernetCardDistributedVirtualPortBackingInfo nicBacking = new VirtualEthernetCardDistributedVirtualPortBackingInfo();
			List<VirtualDeviceConfigSpec> nicSpecAL = new ArrayList<VirtualDeviceConfigSpec>();
			for (int k = 0; k < vdeviceArray.length; k++) {
				Description vDetails = vdeviceArray[k].getDeviceInfo();
				if (vDetails.getLabel().equalsIgnoreCase(networkAdapter)) {
					VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
					nicSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
					VirtualEthernetCard nic = new VirtualPCNet32();
					nic = (VirtualEthernetCard) vdeviceArray[k];
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
					nicSpecAL.add(nicSpec);
					break;
				}
			}
			VirtualDeviceConfigSpec[] nicSpecArray = nicSpecAL
					.toArray(new VirtualDeviceConfigSpec[nicSpecAL.size()]);
			spec.setDeviceChange(nicSpecArray);
			vm.reconfigVM_Task(spec);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
