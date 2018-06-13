import java.net.URL;

import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
//:: # Author: P Kiran Kumar
//:: # Product/Feature: VC
//:: # Description: Create folder in VMs and Templates view 

public class CreateVMTemplateFolder {

	/**
	 * This method is used to create folder in VMs and Templates view in the VC
	 * inventory
	 * 
	 * @param si
	 * @param datacenter
	 * @return
	 */
	public boolean createVMTemplateFolder(ServiceInstance si,
			String datacenter, String folderName) {
		boolean status = false;
		Datacenter dc = null;
		try {
			dc = (Datacenter) new InventoryNavigator(si.getRootFolder())
					.searchManagedEntity("Datacenter", datacenter);
			if (dc != null) {
				Folder newVMFolder = dc.getVmFolder().createFolder(folderName);
				if (newVMFolder != null) {
					status = true;
					System.out
							.println("Successfully created the folder- "+folderName+" in VMs and Templates view.");
				}
			} else {
				System.out.println("Datacenter- " + datacenter
						+ " not exist in VC inventory.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;
	}

	public static void main(String[] args) {

		String vcIPaddress = "10.10.10.30"; // VC ipaddress/hostname
		String userName = "Administrator@vsphere.local"; // VC username
		String passwd = "vdfverfddsac"; // VC password
		String datacenter = "vcqaDC"; // Datacenter name
		String folderName = "vmtfolder1";// Folder name to create
		try {
			ServiceInstance si = new ServiceInstance(new URL("https://"
					+ vcIPaddress + "/sdk"), userName, passwd, true);
			CreateVMTemplateFolder fol = new CreateVMTemplateFolder();
			fol.createVMTemplateFolder(si, datacenter, folderName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
