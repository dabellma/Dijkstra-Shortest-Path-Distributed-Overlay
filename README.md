In this assignment I created TCP connections by spawning threads on different nodes so nodes could communicate with each other. Once the connections were created on multiple nodes, multiple simulated network packets were created and routed according to Dijkstra's shortest path algorithm.

Here's a sample run of 14 messaging nodes with 5 connections each:

                     | Num messages sent    | Num messages received | Sum sent messages    | Sum received messages | Num messages relayed
129.82.44.173:44791  | 25000                | 24620                | 190034123735         | -26724120970         | 65015
129.82.44.143:33743  | 25000                | 24395                | 78250061555          | -644633917765        | 0
129.82.44.100:38971  | 25000                | 25500                | -448873203200        | -560096541615        | 11635
129.82.44.197:38573  | 25000                | 25715                | -170252679845        | -249620806980        | 28920
129.82.44.173:37241  | 25000                | 25230                | 52248488580          | 55494116615          | 11720
129.82.44.147:43607  | 25000                | 25445                | 99004674695          | 10121046245          | 15445
129.82.44.72:46309   | 25000                | 25145                | 170078306865         | -140382331020        | 40270
129.82.44.72:34301   | 25000                | 24635                | 121005015985         | -129613613240        | 36930
129.82.44.72:35495   | 25000                | 25040                | -199682486770        | 93884892990          | 1735
129.82.44.170:41955  | 25000                | 24755                | -658454078745        | 563716702305         | 37985
129.82.44.68:44287   | 25000                | 24900                | 435726775775         | -111525873635        | 42095
129.82.44.254:38495  | 25000                | 24770                | -130689899205        | -53651490275         | 15220
129.82.44.240:38803  | 25000                | 25075                | -372984895660        | 553766438930         | 28755
129.82.44.241:46003  | 25000                | 24775                | 196727670810         | 1403372990           | 4065

Sum                  | 350000               | 350000               | -637862125425        | -637862125425



DjikstraVertex - used to hold a key and a weight in the Djikstra algorithm. Created as an object for simplicity.
ShortestPath - the Djikstra algorithm. Notes included in the class.
MessagingNode - represents one of many nodes in the system that registers with the Registry and talks to other MessagingNodes
Node - not used for much, except for consistency so common classes and events can be used to read events in either the MessagingNode or Registry classes
Registry - represents one node which centralizes information; used by MessagingNodes to register with the Registry
TCPChannel - contains information on a socket, receiving thread, and sending thread and is stored on MessagingNodes and the Registry so connections can be saved on these nodes
TCPReceiverThread - runs a separate thread to receive information for a connection to another node
TCPSenderThread - runs a separate thread to send information on a connection to another node
TCPServerThread - runs a separate thread to accept connections
OverlayCreator - creates an overlay without partitions; more details in the class
StatisticsCollectorAndDisplay - used to store thread safe quantities of messages to be displayed by the Registry
DeregisterRequest - used by a MessagingNode to deregister from the Registry
DeregisterResponse - used by the Registry to communicate back to a MessagingNode that has deregistered
Event - an interface used to enforce a common method of serialization among different events
EventFactory - used to receive communications from other nodes and deserialize them into something actionable
LinkInfo - holding class to describe connections, with weights, between MessagingNodes
LinkWeights - used to communicate LinkInfo values among various MessagingNodes
Message - used to transfer packets between MessagingNodes in an overlay
MessagingNodeList - used to transfer information about connected MessagingNodes in the overlay... specifically used as a way for MessagingNodes to know which other MessagingNodes they should connect to
Protocol - common location to describe event types
RegisterRequest - used by a node as a way to request connecting to another node
RegisterResponse - used by a node to respond to a request from another node that asked for a connection
TaskComplete - used by MessagingNodes as an event to communicate to the Registry that they have finished sending messages to each other in the system
TaskInitiate - used by the Registry to tell MessagingNodes to start sending messages to each other
TaskSummaryRequest - used by the Registry to ask each MessagingNode for details about the messages it transmitted
TaskSummaryResponse - used as a response from a MessagingNode to the Registry telling the Registry details about messages it (the MessagingNode) transmitted
