package travelsampleloadgen.loadgenerator;

class DocumentType {
	String type;
	int offSet;
	volatile long firstDocument;
	volatile long lastDocument;
	volatile long lastIteration;
	volatile long numCreated;

	DocumentType(String type, int offset) {
		this.type = type;
		this.offSet = offset;
		this.firstDocument = 10000 * offset;
		this.lastDocument = 10000 * offset;
		this.lastIteration = 10000 * offset;
		this.numCreated = 0;
	}

	/**
	 * @param firstDocument the firstDocument to set
	 */
	public synchronized void setFirstDocument(long firstDocument) {
		this.firstDocument = firstDocument;
	}

	/**
	 * @param lastDocument the lastDocument to set
	 */
	public synchronized void setLastDocument(long lastDocument) {
		if(lastDocument > this.lastDocument) {
			this.lastDocument = lastDocument;
		}
	}

	/**
	 * @param lastIteration the lastIteration to set
	 */
	public synchronized void setLastIteration(long lastIteration) {
		this.lastIteration = lastIteration;
	}
	
	public synchronized void increaseNumCreated() {
		this.numCreated++;
	}
	
}