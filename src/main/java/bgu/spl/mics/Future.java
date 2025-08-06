package bgu.spl.mics;

import java.util.concurrent.TimeUnit;

/**
 * A Future object represents a promised result - an object that will
 * eventually be resolved to hold a result of some operation. The class allows
 * Retrieving the result once it is available.
 * 
 * Only private methods may be added to this class.
 * No public constructor is allowed except for the empty constructor.
 */
public class Future<T> {
	private boolean isDone;
	private T result;
	private final Object lock;
	
	/**
	 * This should be the the only public constructor in this class.
	 */
	public Future() {
		isDone = false;
		result = null;
		lock = new Object();
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved.
     * This is a blocking method! It waits for the computation in case it has
     * not been completed.
     * <p>
     * @return return the result of type T if it is available, if not wait until it is available.
     * 	       
     */
	public T get() {
		synchronized(lock) {
			try {
				while (!isDone()) {
				    lock.wait();
				}
				return result;	
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}
	
	/**
     * Resolves the result of this Future object.
     */
	public void resolve (T result) {
		synchronized(lock) {
			if (!isDone()) {
				this.result = result;
				this.isDone = true;
				lock.notifyAll();
			}
		}
	}
	
	/**
     * @return true if this object has been resolved, false otherwise
     */
	public boolean isDone() {
		synchronized(lock) {
			return isDone;
		}
	}
	
	/**
     * retrieves the result the Future object holds if it has been resolved,
     * This method is non-blocking, it has a limited amount of time determined
     * by {@code timeout}
     * <p>
     * @param timout 	the maximal amount of time units to wait for the result.
     * @param unit		the {@link TimeUnit} time units to wait.
     * @return return the result of type T if it is available, if not, 
     * 	       wait for {@code timeout} TimeUnits {@code unit}. If time has
     *         elapsed, return null.
     */
	public T get(long timeout, TimeUnit unit) {
		synchronized(lock) {
			try {
				if (!isDone()) {
					unit.timedWait(lock, timeout);
		        }
			}			
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
			return result;			
		}
	}

}