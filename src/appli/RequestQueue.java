package appli;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RequestQueue {
	private LinkedList queue = new LinkedList();
	private int maxQueueLength;
	private int minThreads;
	private int maxThreads;
	private int currentThreads = 0;
	private String requestHandlerClassName;
	private List threadPool = new ArrayList();
	private boolean running = true;

	public RequestQueue(String requestHandlerClassName, int maxQueueLength, int minThreads, int maxThreads) {
		// Initialize our parameters
		this.requestHandlerClassName = requestHandlerClassName;
		this.maxQueueLength = maxQueueLength;
		this.minThreads = minThreads;
		this.maxThreads = maxThreads;
		this.currentThreads = this.minThreads;

		// Create the minimum number of threads
		for (int i = 0; i < this.minThreads; i++) {
			RequestThread thread = new RequestThread(this, i, requestHandlerClassName);
			thread.start();
			this.threadPool.add(thread);
		}
	}

	public String getRequestHandlerClassName() {
		return this.requestHandlerClassName;
	}

	public synchronized void add(Object o) throws Exception {
		// Validate that we have room of the object before we add it to the
		// queue
		if (queue.size() > this.maxQueueLength) {
			throw new Exception("The Request Queue is full. Max size = " + this.maxQueueLength);
		}

		// Add the new object to the end of the queue
		queue.addLast(o);

		// See if we have an available thread to process the request
		boolean availableThread = false;
		for (Iterator i = this.threadPool.iterator(); i.hasNext();) {
			RequestThread rt = (RequestThread) i.next();
			if (!rt.isProcessing()) {
				System.out.println("Found an available thread");
				availableThread = true;
				break;
			}
			System.out.println("Thread is busy");
		}

		// See if we have an available thread
		if (!availableThread) {
			if (this.currentThreads < this.maxThreads) {
				System.out.println("Creating a new thread to satisfy the incoming request");
				RequestThread thread = new RequestThread(this, currentThreads++, this.requestHandlerClassName);
				thread.start();
				this.threadPool.add(thread);
			} else {
				System.out.println("Whoops, can’t grow the thread pool, guess you have to wait");
			}
		}

		// Wake someone up
		notifyAll();
	}

	public synchronized Object getNextObject() {
		// Setup waiting on the Request Queue
		while (queue.isEmpty()) {
			try {
				if (!running) {
					// Exit criteria for stopping threads
					return null;
				}
				wait();
			} catch (InterruptedException ie) {
			}
		}
		// Return the item at the head of the queue
		return queue.removeFirst();
	}

	/**
	 * Shuts down the request queue and kills all of the request threads
	 */
	public synchronized void shutdown() {
		System.out.println("Shutting down request threads...");

		// Mark the queue as not running so that we will free up our request
		// threads
		this.running = false;

		// Tell each thread to kill itself
		for (Iterator i = this.threadPool.iterator(); i.hasNext();) {
			RequestThread rt = (RequestThread) i.next();
			rt.killThread();
		}

		// Wake up all threads and let them die
		notifyAll();
	}
}
