#### ATM Transaction Scenario

System Involved 
- ATM Machines
- Bank Servers
- Customer 

#### Data Flow
1. ATM Machines received withdrawal requests from Customers.
2. ATM Machines send transaction requests to Bank Servers for processing.
3. Bank Servers process the requests and send back transaction status to ATM Machines under following conditions 
   1. Successful Transaction (When ATM have sufficient cash and Customer have sufficient balance)
   2. ATM Machines dispense cash to Customers upon successful transaction status.
   3. Failed Transaction (When ATM do not have sufficient cash or Customer do not have sufficient balance)
   4. ATM Machines notify Customers of failed transaction status.
   5. Bank Servers log all transaction details for record-keeping and auditing purposes.
