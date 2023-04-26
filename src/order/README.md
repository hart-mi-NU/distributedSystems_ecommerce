# Order Service
 - Order service is used to place orders for the current user once the user has added the required products in the shopping cart.

## How to run order service?
   - Compile the order coordinator `javac order/CoordinatorStarter.java`
   - Compile the order server `javac order/OrderServerStarter.java`
   - Start RMI registry - `rmiregistry`
   - Run the 5 order servers replicas
      - `java order/OrderServerStarter 5001 localhost 1001`
      - `java order/OrderServerStarter 5002 localhost 1001`
      - `java order/OrderServerStarter 5003 localhost 1001`
      - `java order/OrderServerStarter 5004 localhost 1001`
      - `java order/OrderServerStarter 5005 localhost 1001`
   - Run the order coordinator
      - `java order/CoordinatorStarter 1001`
   - The arguments for the server are the server port number, coordinator ip, and coordinator port. The argument for the coordinator is just the coordinator port number.
