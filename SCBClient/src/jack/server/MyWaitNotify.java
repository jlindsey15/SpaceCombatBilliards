package jack.server;

public class MyWaitNotify {
	// Used for inter-thread communication.

	static Object myMonitorObject = new Object();

	public void doWait() {
		// Makes calling thread wait until notification
		synchronized (myMonitorObject) {
			try {
				myMonitorObject.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void doWait(int i) {
		// overloaded method - same as above except with a timeout
		synchronized (myMonitorObject) {
			try {
				myMonitorObject.wait(i);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void doNotify() {
		// notifies waiting threads that they can start again
		synchronized (myMonitorObject) {
			myMonitorObject.notifyAll();
		}
	}
}