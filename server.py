from socket import *
import base64
import threading, time
import os

NORMAL = "normal"
EXPERT = "expert"
VERIFY = "VERIFY"
BUFF = 1024 * 5000
HOST = 'localhost'# must be input parameter @TODO
PORT = 4567 # must be input parameter @TODO

class ServerThread(threading.Thread):

	def __init__(self, l, sock):
		threading.Thread.__init__(self)
		self.lock = l
		self.connection = sock
		
	def run(self):
		self.lock.acquire()
		while True:
			data = self.connection.recv(BUFF)
			key = bytes.decode(data)
			print(data)
			if key == NORMAL:
				print("predicting...")
				time.sleep(2)
				imageData = self.connection.recv(BUFF)
				f = open('image_to_predict.jpg', 'wb')
				f.write(imageData)
				f.close()
				os.system("python predict.py image_to_predict.jpg")
				rf = open('result.txt', 'r')
				result = rf.read()
				rf.close()
				self.connection.send(str.encode(result + '\n'))
				time.sleep(2)
			elif key == VERIFY:
				print("veryfying...")
			elif key == EXPERT:
				print('storing new data...')
				dir = self.connection.recv(BUFF)
				dir = bytes.decode(dir)
				t = time.strftime('%Y-%m-%d-%H-%M-%S',time.localtime(time.time()))
				dir = 'C:\\Users\\David\\desktop\\image_classifier\\flowers\\' + dir
				if not os.path.isdir(dir):
					os.mkdir(dir)
				time.sleep(1)
				imageData = self.connection.recv(BUFF)
				newImage = open(dir + '\\' + t + '.jpg', 'wb')
				newImage.write(imageData)
				newImage.close()
				self.connection.send(str.encode('stored\n'))
				time.sleep(2)
			if not data:
				break
		self.lock.release()
		self.connection.close()
			
 
if __name__=='__main__':
	ADDR = (HOST, PORT)
	serversock = socket(AF_INET, SOCK_STREAM)
	serversock.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
	serversock.bind(ADDR)
	serversock.listen(2)
	lock = threading.Lock()
	while True:
		print('Waiting for connection...')
		clientsock, addr = serversock.accept()
		print('...connected from:', addr)
		ServerThread(lock, clientsock).start()
