To run inventory service, follow the below steps:

- Navigate to the src folder
- On three separate terminal windows, run the following commands
  - `java inventoryService.server.InventoryServiceApplication 127.0.0.1 4001`
  - `java inventoryService.server.InventoryServiceApplication 127.0.0.1 4002`
  - `java inventoryService.server.InventoryServiceApplication 127.0.0.1 4003`\
  This will run 3 replicas of the inventory server.