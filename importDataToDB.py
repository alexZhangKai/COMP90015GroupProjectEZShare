import csv
from pymongo import MongoClient
#open the csv file in the local directory
csvfile = open('/Users/lisha/Google Drive/Study/2017 s2/mobile computing/assignment/assignment·2/crime2017.csv', 'r')
fieldnames = ("Apr - Mar reference period",	"Postcode","Suburb/Town Name",	"CSA Offence Division",	"CSA Offence Subdivision",	"CSA Offence Group","Offence Count","id")
reader = csv.DictReader(csvfile,fieldnames)
#creat a client in mongoDB
mongo_client = MongoClient("mongodb://crimeinfomobile:kaaGy7qBriWfQCLBvp1N3D8nRmL7MB3lKYBfKrwBNjbUVVVEsmL3a6UcAa07IWZOa3n2wv8GO23f2Lb4Y4Rv0w==@crimeinfomobile.documents.azure.com:10255/?ssl=true&replicaSet=globaldb")
db = mongo_client.crimeInfo
db.segment.drop()
col = db.only2017info
# uploading the data into MongoDB
for row in reader:
    col.insert_one(row)
