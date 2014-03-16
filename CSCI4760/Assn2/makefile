#makefile
#Elliott Tanner
#Assn2


CP = ./classes
TEST_CP = ./test_classes
ALL_CL = ./classes/*/*.class
ALL_TEST_CL =./test_classes/*/*.class
ALL_SRC = ./src/*/*.java	#all source code
ALL_TEST_SRC = ./test_src/transport/*.java
JARS = .:junit-4.10.jar:

compile:
	javac -d $(CP) -cp $(JARS)  $(ALL_SRC)
	javac -d $(TEST_CP) -cp $(JARS)$(CP): $(ALL_TEST_SRC)

run: compile
	java -cp $(CP) transport.ReliableReceiver&
	java -cp $(CP) transport.ReliableSender

send: compile
	java -cp $(CP) transport.ReliableSender

rcv: compile
	java -cp $(CP) transport.ReliableReceiver

test: compile
	java -cp $(JARS)$(TEST_CP):$(CP) org.junit.runner.JUnitCore transport.ReliableTransportMessageTest

clean:
	-rm $(ALL_CL)
	-rm $(ALL_TEST_CL)
	-rm ./*~
	-rm ./*error*.txt
	-rm ./test_src/transport/*.java~
	-rm ./src/transport/*.java~
