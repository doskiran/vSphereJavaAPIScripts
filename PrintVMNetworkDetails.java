import java.net.URL;

import com.vmware.vim25.Description;
import com.vmware.vim25.GuestNicInfo;
import com.vmware.vim25.VirtualDevice;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Print the Virtual machine network details(Nic name,IPv4,Mac,Nic Key,Portgroup) using vSphere java API. 
public class PrintVMNetworkDetils {

	public void printVMNetworkDetails(ServiceInstance si, String vmName) throws Exception {
		String format = "%-20s %-18s %-20s %-8s %-100s\n";
		try {
			VirtualMachine vm = (VirtualMachine) new InventoryNavigator(si.getRootFolder())
					.searchManagedEntity("VirtualMachine", vmName);
			GuestNicInfo[] nicArr = vm.getGuest().getNet();
			System.out.printf(format, "Network Adatpter", "IP4Address", "Mac", "Key", "Portgroup");
			System.out.printf(format, "----------------", "----------", "---", "---", "---------");
			for (GuestNicInfo nic : nicArr) {
				String ipaddress = nic.getIpConfig().getIpAddress()[0].getIpAddress();
				if (isValidIPv4AddrFormat(ipaddress)) {
					System.out.printf(format, getNetworkadapterName(vm, nic.getDeviceConfigId()),
							nic.getIpConfig().getIpAddress()[0].getIpAddress(), nic.getMacAddress(),
							nic.getDeviceConfigId(), nic.getNetwork());
				}
			}
		} catch (Exception e) {
			System.err.println("Error in printVMNetworkDetails()::" + e.getLocalizedMessage());
		}
	}

	public String getNetworkadapterName(VirtualMachine vm, int key) {
		String adapterName = null;
		try {
			VirtualDevice[] vdeviceArray = (VirtualDevice[]) vm.getPropertyByPath("config.hardware.device");
			for (int k = 0; k < vdeviceArray.length; k++) {
				Description vDetails = vdeviceArray[k].getDeviceInfo();
				if (vDetails.getLabel().contains("Network adapter")) {
					VirtualEthernetCard nicec = (VirtualEthernetCard) vdeviceArray[k];
					if (nicec.getKey() == key) {
						adapterName = vDetails.getLabel();
						break;
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error in getNetworkadapterName()::" + e.getLocalizedMessage());
		}
		return adapterName;
	}

	public boolean isValidIPv4AddrFormat(String ipv4Address) {
		boolean partOne = true;
		String[] parts = ipv4Address.split("\\.");
		if (parts.length != 4) {
			return false;
		}
		for (String s : parts) {
			int i = Integer.parseInt(s);
			if (partOne) {
				if ((i <= 0) || (i > 255)) {
					return false;
				}
				partOne = false;
			} else {
				if ((i < 0) || (i > 255)) {
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) throws Exception {
		String vcIPaddress = "10.10.10.1"; // VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; // VC username
		String passwd = "sfdefcsdlk87sdef7oi"; // VC password
		String vmName = "testvm"; // VM Name
		ServiceInstance si = new ServiceInstance(new URL("https://" + vcIPaddress + "/sdk"), userName, passwd, true);
		new PrintVMNetworkDetils().printVMNetworkDetails(si, vmName);
	}
}
