# SocketsServer

A multithreaded NAT traversal project that allow hosts behind NAT to initiate P2P connections to each other by using a pool of cloud proxies.
This was made as part of an independent study in 2013.

## How it works
The server creates an overlay network (similar to Hamachi) to accomplish communication with the clients.
The server responds to client connection requests by giving out the IP address of worker servers to clients based on which worker is handling that client's workload.
The worker servers then proxy the connections between the clients, allowing them to communicate across NAT. One or both or neither of the hosts can be behind NAT.

The server runs on the cloud, the clients run on the hosts. Only the server and workers must have public IP addresses.
The client can run on anything that can run Java - the demonstration of this was between a PC and an android app.

As this was an academic project, this is more of a proof-of-concept. 
The worker servers are abstracted into threads of the connection facilitation server, but the implementation is such that the work to make them into servers is trivial.
