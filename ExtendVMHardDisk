//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Extend existing harddisk with specified capacity in a VM using vSphere API. Note:: It is recommenced to assign harddiskName should start from "Hard disk 2",because "Hard disk 1" contain the OS data

import java.net.URL;

import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceBackingInfo;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class ExtendHardDisk {
	public static void main(String[] args) {
		String IPAddress = "10.10.10.1"; //VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; //VC username
		String passwd = "kvmklvmkdf"; // VC password
		long diskCapacityinKB = 2000000;//Required harddisk capacity in KB
		String harddiskName = "Hard disk 2"; // Name of the Hard disk to extend, Eg- "Hard disk 2" or "Hard disk 3"...
		String vmName = "centos1";// VM name
		ServiceInstance si = null;
		
		try {
			si = new ServiceInstance(new URL("https://" + IPAddress + "/sdk"),
					userName, passwd, true);
			Folder rootFolder = si.getRootFolder();
			VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
					rootFolder).searchManagedEntity("VirtualMachine",
							vmName);
			try {
				int ckey = 0;
				int unitNumber = 0;
				int key = 0;
				String waitStr = null;
				VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
				VirtualMachineConfigInfo vmConfigInfo = (VirtualMachineConfigInfo) vm
						.getConfig();
				VirtualDisk disk = new VirtualDisk();
				VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
				VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
				VirtualDevice[] vDevice = vmConfigInfo.getHardware()
						.getDevice();
				for (int k = 0; k < vDevice.length; k++) {
					if (vDevice[k].getDeviceInfo().getLabel()
							.equalsIgnoreCase(harddiskName)) {
						ckey = vDevice[k].getControllerKey();
						unitNumber = vDevice[k].getUnitNumber();
						key = vDevice[k].getKey();
						VirtualDeviceBackingInfo info = vDevice[k].getBacking();
						diskfileBacking = (VirtualDiskFlatVer2BackingInfo) info;
						break;
					}
				}
				disk.setControllerKey(ckey);
				disk.setUnitNumber(unitNumber);
				disk.setBacking(diskfileBacking);
				disk.setCapacityInKB(diskCapacityinKB);
				disk.setKey(key);
				diskSpec.setOperation(VirtualDeviceConfigSpecOperation.edit);
				diskSpec.setDevice(disk);
				vmConfigSpec
						.setDeviceChange(new VirtualDeviceConfigSpec[] { diskSpec });
				Task task = vm.reconfigVM_Task(vmConfigSpec);
				waitStr = task.waitForTask();
				if (waitStr != null && waitStr.equalsIgnoreCase("success")) {
					System.out.println(harddiskName
							+ " extended successfully..");
				} else {
					System.out.println("Error::Reconfig Fail...");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		si.getServerConnection().logout();
	}
}
