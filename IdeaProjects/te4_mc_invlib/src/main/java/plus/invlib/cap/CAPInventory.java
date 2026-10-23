package plus.invlib.cap;


import plus.inv.AInventory;

public class CAPInventory implements ICAPInventory {
	public final AInventory inventory = new AInventory();

	public AInventory getInventory(){
		return this.inventory;
	}
	@Override
	public void copyInventory(ICAPInventory inventory) {
		
		this.inventory.copy(inventory.getInventory());
	}
}