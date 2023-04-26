package userInterface;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gateway.EcomInterface;
import inventoryService.api.InventoryService;
import inventoryService.dto.Product;

public class ShoppingCart {

	// quantity (value) for each product id (key)
	Map<Integer, Integer> quantities;

	// subtotal (value) for each product id (key) ... quantity * unit price
	Map<Integer, Double> subtotals;
	String username;
	double total;
	
	// Inventory server used to get item prices
	EcomInterface store;
	
	public ShoppingCart(EcomInterface store, String username) {
		this.quantities = new HashMap<>();
		this.subtotals = new HashMap<>();
		this.total = 0.0;
		this.username = username;
		this.store = store;
	}


	
	// Clear the shopping cart
	public String clearAll() {
		this.quantities.clear();
		this.subtotals.clear();
		this.total = 0.0;
		return "success";
	}
	
	// Update the total
	private void updateTotal() {
		// sum the subtotals hashmap
		double sum = 0.0;
		for (Double value : this.subtotals.values()) {
		    sum += value;
		}
	}
	
	// Add a quantity of productId to the shopping cart
	// Return "success" if successful
	// Return error message is not successful
	public String add(Integer productId, Integer quantity) throws RemoteException {
		Integer stock = this.store.getProductStock(productId);
		if (stock == -1) {
			return "Invalid product id";
		} else if (stock == 0) {
			return "Unable to add to cart - item out of stock";
		} else if ( quantity > stock) {
			return "Unable to add to cart - quantity exceeds available stock";
		}

		Product product = store.getProduct(productId);
		Double unitPrice = product.getPrice();
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
		
		updateTotal();
		return "success";
	}

	
	// update a given quantity of a productId in the shopping cart
	public String update(Integer productId, Integer quantity) throws RemoteException {
		Integer stock = this.store.getProductStock(productId);
		if (stock == -1) {
			return "Invalid product id";
		} else if (stock == 0) {
			return "Unable to add to cart - item out of stock";
		} else if ( quantity > stock) {
			return "Unable to add to cart - quantity exceeds available stock";
		}

		//todo
//		Double unitPrice = inventoryServer.getPrice(productId);
//		Double subtotal = unitPrice * quantity;
//		this.quantities.put(productId, quantity);
//		this.subtotals.put(productId, subtotal);
		updateTotal();
		return "success";
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
	public String remove(Integer productId) throws RemoteException {
		Integer stock = this.store.getProductStock(productId);
		if (stock == -1) {
			return "Invalid product id";
		}
		this.quantities.remove(productId);
		this.subtotals.remove(productId);
		updateTotal();
		return "success";
	}
	
	
	// Return a map of subtotals for each item id
	public Map<Integer, Double> getSubtotals(){
		return this.subtotals;
	}
	
	// Get the grand total for the cart
	public Double getTotal() {
		return this.total;
	}

	public String getUsername() { return this.username; }
	
	// Print the cart contents to the terminal
	public void printCart() {
		// TODO
	}
}
