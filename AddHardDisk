//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Add new harddisk with specified provision type and capacity in a VM using vSphere API. 
import java.net.URL;

import com.vmware.vim25.DatastoreSummary;
import com.vmware.vim25.Description;
import com.vmware.vim25.TaskInfoState;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class AddHardDisk {

	public static void main(String[] args) {

		String IPAddress = "10.10.10.1"; //VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; //VC username
		String passwd = "kvmklvmkdf"; // VC password
		String dataStoreToAddDisk = ""; // (Optional) datastore to add harddisk, else add harddisk in one of the supported VM datastore
		String vmProvisionedType = "Thin Provision"; // "Thin Provision" or "" or "Thick Provision Eager Zeroed" or "Thick Provision Lazy Zeroed"
		long diskCapacityinKB = 1000000; //Required harddisk capacity in KB
		String vmName = "centos1"; // VM name
		ServiceInstance si = null;
		try {
			si = new ServiceInstance(new URL("https://"
					+ IPAddress + "/sdk"), userName, passwd, true);
			Folder rootFolder = si.getRootFolder();
			VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
					rootFolder).searchManagedEntity("VirtualMachine",
							vmName);
			try {
				int ckey = 0;
				int unitNumber = 0;
				VirtualMachineConfigSpec vmConfigSpec = new VirtualMachineConfigSpec();
				VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
				VirtualMachineConfigInfo vmConfigInfo = (VirtualMachineConfigInfo) vm
						.getConfig();
				VirtualDisk disk = new VirtualDisk();
				VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
				VirtualDevice[] vDevice = vmConfigInfo.getHardware()
						.getDevice();
				for (int i = 0; i < vDevice.length; i++) {
					if (vDevice[i].getDeviceInfo().getLabel()
							.equalsIgnoreCase("SCSI Controller 0")) {
						ckey = vDevice[i].getKey();
					}
				}
				if (dataStoreToAddDisk.isEmpty()) {
					dataStoreToAddDisk = getDataStoreName(vm, diskCapacityinKB);
				}
				int count = getNumOfVMDisks(vm) + 1;
				unitNumber = (count != 7) ? count : count + 1;
				String vmdlLoc = "[" + dataStoreToAddDisk + "] " + vm.getName()
						+ "/" + vm.getName() + "_" + count + ".vmdk";
				System.out.println("vmdlLoc::" + vmdlLoc);
				diskfileBacking.setFileName(vmdlLoc);
				diskfileBacking.setDiskMode("persistent");
				if (vmProvisionedType != null
						&& vmProvisionedType.equalsIgnoreCase("Thin Provision")) {
					Datastore[] ds = vm.getDatastores();
					for (int j = 0; j < ds.length; j++) {
						if (ds[j].getName().equalsIgnoreCase(dataStoreToAddDisk)) {
							if (ds[j].getCapability()
									.isPerFileThinProvisioningSupported()) {
								diskfileBacking.setThinProvisioned(true);
							} else {
								System.out
										.println("Error::Thin Provision not supported...");
								return;
							}

						}
					}
				} else if (vmProvisionedType != null
						&& vmProvisionedType
								.equalsIgnoreCase("Thick Provision Eager Zeroed")) {
					diskfileBacking.setEagerlyScrub(true);
				} else if (vmProvisionedType != null
						&& vmProvisionedType
								.equalsIgnoreCase("Thick Provision Lazy Zeroed")) {
					diskfileBacking.setEagerlyScrub(false);
					diskfileBacking.setThinProvisioned(false);
				}
				disk.setControllerKey(ckey);
				disk.setUnitNumber(unitNumber);
				disk.setBacking(diskfileBacking);
				disk.setCapacityInKB(diskCapacityinKB);
				disk.setKey(-1);
				diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
				diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);
				diskSpec.setDevice(disk);
				if (diskSpec != null) {
					vmConfigSpec
							.setDeviceChange(new VirtualDeviceConfigSpec[] { diskSpec });
				}
				Task task = vm.reconfigVM_Task(vmConfigSpec);
				if ((task.getTaskInfo().getState())
						.equals(TaskInfoState.success)) {
					System.out
							.println("Successfully creted virtual harddisk...");
				} else if ((task.getTaskInfo().getState())
						.equals(TaskInfoState.error)) {
					System.out.println("Error::Reconfig Fail...");
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		si.getServerConnection().logout();
	}

	static int getNumOfVMDisks(VirtualMachine vm) {
		int count = 0;
		VirtualDevice[] vdeviceArray = vm.getConfig().getHardware().getDevice();
		for (int k = 0; k < vdeviceArray.length; k++) {
			Description vDetails = vdeviceArray[k].getDeviceInfo();
			String str = vDetails.getLabel();
			if (str.startsWith("Hard disk")) {
				count++;
			}
		}
		return count;
	}

	static String getDataStoreName(VirtualMachine vm, long diskCapacityinKB)
			throws Exception {
		String dsName = null;
		Datastore[] datastores = vm.getDatastores();
		for (int i = 0; i < datastores.length; i++) {
			if (datastores[i].getSummary().isAccessible()) {
				DatastoreSummary ds = datastores[i].getSummary();
				if (ds.getFreeSpace() > diskCapacityinKB) {
					dsName = ds.getName();
					break;
				}
			}
		}
		return dsName;
	}

}
