package rdf.util

import rdf.JenaUtilities

class Transaction {
	
	def file
	def model
	def ju = new JenaUtilities()
	
	Transaction(model, file){
		this.model = model
		this.file = file
	}
	
	def save() {
		BackupFiles.backup(file)
		ju.saveModelFile(model, file, "TTL")
	}
}
