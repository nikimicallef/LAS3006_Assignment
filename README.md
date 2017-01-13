This project uses maven as a build and execution tool. Follow the steps below to build and execute the project

1. Build the whole project by executing mvn clean install at the project root.
2. Navigate to the server module and run the server via the mvn exec:java command.
3. Once the server has sucesfully started you can deploy a number of clients.
3.1. Navigate to the ClientSubscriber module to create an instance of a ClientSubscriber or to the ClientPublisher module to create an instance of the ClientPublisher.
3.2. Using the command mvn exec:java will execute the client using random paths while using the mvn exec:java -Dexec.args="home/kitchen" will use the home/kitchen path where required.

NOTES:
- The system usees 127.0.0.1 as its address and 1927 as its port.
