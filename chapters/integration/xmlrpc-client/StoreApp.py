#!/usr/bin/python

##
# A direct port of the StoreApp Java XML-RPC Client example
# shipped with the eXist O'Riley book
#
# Author: Adam Retter
##

import array
import os
import sys
import xmlrpclib

def printUseage():
	sys.stderr.write("""useage: StoreApp.py <server> <port> <file> <internet-media-type> <collection> <username> [password]

	server: The hostname or ip address of the server e.g. localhost.
	port: The tcp port of the server e.g. 8080.
	file: The path to a file you wish to store in the database.
	internet-media-type: The Internet Media Type of the file you wish to store in the database.
	collection: The path to a collection in the database where the file should be stored e.g. /db/mycollection.
	username: The eXist account to use to connect to the database e.g. admin.
	password: The password of the eXist account to use to connect to the database, if ommitted then an blank password is used.
    """)

# get the command line arguments
if len(sys.argv) < 6:
	printUseage()
	sys.exit(1)
server = sys.argv[1]
port = sys.argv[2]
file = sys.argv[3]
if not(os.path.isfile(file)):
	sys.stderr.write("File '%s' does not exist!%s" % (file, os.linesep))
	sys.exit(2)
mediaType = sys.argv[4]
collection = sys.argv[5]
username = sys.argv[6]
if len(sys.argv) == 8:
	password = sys.argv[7]
else:
	password = ""

# get an XML-RPC connection to the eXist server
uri = "http://%s:%s@%s:%s/exist/xmlrpc" % (username, password, server, port)
rpc = xmlrpclib.ServerProxy(uri)

print "Starting upload of %s to http://%s:%s/exist/xmlrpc%s...%s" % (os.path.basename(file), server, port, collection, os.linesep)
        
#upload the resource
with open(file, "rb") as f:
	remoteIdentifier = None
	while True:
		chunk = f.read(1024)
		if chunk:
			buf = array.array("B", chunk)
			if not(remoteIdentifier):
				#upload first chunk of file to server
				remoteIdentifier = rpc.upload(xmlrpclib.Binary(buf), len(buf))
			else:
				#append further chunks of file to remote file on server
				remoteIdentifier = rpc.upload(remoteIdentifier, xmlrpclib.Binary(buf), len(buf))
		else:
			break;

#have the server parse and store the remote file into the database
result = rpc.parseLocal(remoteIdentifier, collection + "/" + os.path.basename(file), True, mediaType)
if result == True:
	print "Finished upload OK.%s" % os.linesep
else:
    sys.stderr.write("Could not store file.%s" % os.linesep)
    sys.exit(3)
