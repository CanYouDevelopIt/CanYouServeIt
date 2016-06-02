package appli;

class ServerDescription {
	public String host;
	public int port;
	public int clientsConectedCount = 0;
	public boolean isAlive = true;

	public ServerDescription(String host, int port) {
		this.host = host;
		this.port = port;
	}

	@Override
	public String toString() {
		return "ServerDescription [" + host + ":" + port + "]";
	}
}