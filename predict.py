# coding=utf-8
import tensorflow as tf
import sys


image_file = sys.argv[1]



image = tf.gfile.FastGFile(image_file, 'rb').read()


labels = []
for label in tf.gfile.GFile("tmp\\output_labels.txt"):
	labels.append(label.rstrip())


with tf.gfile.FastGFile("tmp\\output_graph.pb", 'rb') as f:
	graph_def = tf.GraphDef()
	graph_def.ParseFromString(f.read())
	tf.import_graph_def(graph_def, name='')

with tf.Session() as sess:
	softmax_tensor = sess.graph.get_tensor_by_name('final_result:0')
	predict = sess.run(softmax_tensor, {'DecodeJpeg/contents:0': image})

	top = predict[0].argsort()[-len(predict[0]):][::-1]
	for index in top:
		human_string = labels[index]
		score = predict[0][index]
		rf = open("result.txt", 'w')
		rf.write(labels[top[0]] + ' ' + str(round(100*predict[0][top[0]], 2)) + '%')
		rf.close()
		print(human_string, score)
