package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Iterator;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {
	private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> queues;
	private final ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<MicroService>> subscribers;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventFutures;
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();


	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	private MessageBusImpl() {
	    queues = new ConcurrentHashMap<>();
		subscribers = new ConcurrentHashMap<>();
		eventFutures = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

	

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		subscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).offer(m);
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		subscribers.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).offer(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<?> future = eventFutures.get(e);
		if (future != null) {
			((Future<T>)future).resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		boolean locked = false;
		try {
			rwl.readLock().lockInterruptibly();
			locked = true;
			LinkedBlockingQueue<MicroService> subs = subscribers.get(b.getClass());
			if (!(subs == null || subs.isEmpty())) {
				Iterator<MicroService> it = subs.iterator();
				while (it.hasNext()) {
					queues.get(it.next()).offer(b);
				}
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		finally {
			if (locked) {
			    rwl.readLock().unlock();
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		Future<T> future = null;
		boolean locked = false;
		try {
			rwl.readLock().lockInterruptibly();
			locked = true;
			LinkedBlockingQueue<MicroService> subs = subscribers.get(e.getClass());
			if (!(subs == null || subs.isEmpty())) {
				MicroService service = subs.poll();
			    LinkedBlockingQueue<Message> queue = queues.get(service);
			    queue.offer(e);
			    subs.offer(service);
			    future = new Future<>();
			    eventFutures.put(e, future);
			}
		}
		catch (InterruptedException error) {
			Thread.currentThread().interrupt();
		}
		finally {
			if (locked) {
				rwl.readLock().unlock();
			}
		}
		return future;
	}

	@Override
	public void register(MicroService m) {
		queues.putIfAbsent(m, new LinkedBlockingQueue<Message>());
	}

	@Override
	public void unregister(MicroService m) {
		try {
			rwl.writeLock().lockInterruptibly();
			queues.remove(m);
			subscribers.forEach((type, subs) -> subs.remove(m));
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		finally {
			if (rwl.isWriteLockedByCurrentThread()) {
				rwl.writeLock().unlock();
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = queues.get(m);
		return queue.take();
	}

	public ConcurrentHashMap<Class<? extends Message>, LinkedBlockingQueue<MicroService>> getSubscribers() {
		return subscribers;
	}

	public ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> getQueues() {
		return queues;
	}
	
	public ConcurrentHashMap<Event<?>, Future<?>> getEventFutures() {
		return eventFutures;
	}
	
}