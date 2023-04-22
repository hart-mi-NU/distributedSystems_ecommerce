package userInterface;

import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {

	// quantity (value) for each product id (key)
	Map<Integer, Integer> quantities;

	// subtotal (value) for each product id (key) ... quantity * unit price
	Map<Integer, Double> subtotals;
	
	// Inventory server used to get item prices
	InventoryServer inventory;
	
	public ShoppingCart(InventoryService inventoryServer) {
		this.quantities = new HashMap<>();
		this.subtotals = new HashMap<>();
		this.inventory = inventoryServer;
	}


	
	// Clear the shopping cart
	public void clearAll() {
		this.quantities = new HashMap<>();
		this.subtotals = new HashMap<>();
	}
	
	// Add a quantity of productId to the shopping cart
	public void add(Integer productId, Integer quantity) {
		Double unitPrice = inventoryServer.getPrice(productId);
		Double subtotal = unitPrice * quantity;
		
		// Update the quantity in the cart
		if (this.quantities.containsKey(productId)) {
			Integer priorQty = this.quantities.get(productId);
			this.quantities.put(productId, priorQty + quantity);
		} else {
			this.quantities.put(productId, quantity);
		}
		
		// Update the subtotal for the product id
		if (this.quantities.containsKey(productId)) {
			Double priorSubtotal = this.subtotals.get(productId);
			this.subtotals.put(productId, priorSubtotal + subtotal);
		} else {
			this.subtotals.put(productId, subtotal);
		}
	}

	
	// update a given quantity of a productId in the shopping cart
	public void update(Integer productId, Integer quantity) {
		Double unitPrice = inventoryServer.getPrice(productId);
		Double subtotal = unitPrice * quantity;
		this.quantities.put(productId, quantity);
		this.subtotals.put(productId, subtotal);
	}
	
	
	// Return a list of quantities in the shopping cart
	public List<List<Integer>> getQuantities() {
		List<List<Integer>> qtys = new ArrayList<List<Integer>>();
		for (Map.Entry<Integer, Integer> entry : this.quantities.entrySet()) {
			List<Integer> list = new ArrayList<>();
            list.add(entry.getKey());
            list.add(entry.getValue());
            qtys.add(list);
        }
		return qtys;
	}
	
	
	// Remove an item from the shopping cart
	public void remove(Integer productId) {
		this.quantities.remove(productId);
		this.subtotals.remove(productId);
	}
	
	
	// Return a map of subtotals for each item id
	public Map<Integer, Double> getSubtotals(){
		return this.subtotals;
	}
}
