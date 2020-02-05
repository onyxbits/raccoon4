package de.onyxbits.raccoon.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

public class DroidConnectionSocketFactory implements
		LayeredConnectionSocketFactory {

	@Override
	public Socket createSocket(HttpContext context) throws IOException {
		return new Socket();
	}

	@Override
	public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host,
			InetSocketAddress remoteAddress, InetSocketAddress localAddress,
			HttpContext context) throws IOException {
		Socket sock = socket != null ? socket : createSocket(context);
		sock.connect(remoteAddress, connectTimeout);
		return createLayeredSocket(sock, host.getHostName(),
				remoteAddress.getPort(), context);
	}

	@Override
	public Socket createLayeredSocket(Socket socket, String target, int port,
			HttpContext context) throws IOException, UnknownHostException {
		return new DroidSocket(socket);
	}

}
