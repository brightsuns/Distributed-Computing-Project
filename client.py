import base64, socket 
import time
import socket
if __name__ == "__main__":
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	f = open('ham1.jpg', 'rb')
	data = f.read()
	f.close()
	sock.connect(('localhost', 4567))
	sock.send(b'hello')
	sock.close()
