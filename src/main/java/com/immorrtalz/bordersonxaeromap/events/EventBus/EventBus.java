package com.immorrtalz.bordersonxaeromap.events.EventBus;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

public class EventBus
{
	private final Map<Class<?>, List<Consumer<?>>> handlers = new HashMap<>();

	/**
	* Registers an event handler for a specific type.
	*
	* @param eventType	The type of the event this handler will receive.
	* @param handler		A callback that will be invoked when the event occurs.
	*/
	public <T> void subscribe(Class<T> eventType, Consumer<T> handler)
	{
		if (handler == null) return;
		handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
	}

	/**
	* Unregisters an event handler for a specific type.
	*
	* @param eventType	The type of the event this handler was registered for.
	* @param handler		A callback to be removed from the list.
	*/
	public <T> void unsubscribe(Class<T> eventType, Consumer<T> handler)
	{
		if (handler == null) return;
		List<Consumer<?>> list = handlers.get(eventType);
		if (list != null) list.removeIf(h -> h.equals(handler));
	}

	/**
	* Invokes all registered event handlers for a specific event type.
	*
	* @param eventArgs The event object to be passed to the callbacks.
	*/
	public <T> void invoke(T eventArgs)
	{
		@SuppressWarnings("unchecked")
		Class<T> eventType = (Class<T>) eventArgs.getClass();
		List<Consumer<?>> list = handlers.get(eventType);

		if (list != null)
		{
			for (Consumer<?> handler : list)
			{
				@SuppressWarnings("unchecked")
				Consumer<T> typedHandler = (Consumer<T>) handler;
				typedHandler.accept(eventArgs);
			}
		}
	}

	/**
	* Invokes all registered event handlers dynamically based on runtime type.
	*
	* @param eventArgs The event object to be passed to the callbacks.
	*/
	public void invokeRuntime(Object eventArgs)
	{
		if (eventArgs == null) return;

		Class<?> eventType = eventArgs.getClass();
		List<Consumer<?>> list = handlers.get(eventType);

		if (list != null)
			{
			for (Consumer<?> handler : list)
				{
				try
				{
					// Use reflection to call the 'accept' method
					Method acceptMethod = handler.getClass().getMethod("accept", Object.class);
					acceptMethod.invoke(handler, eventArgs);
				} catch (Exception e) {
					System.err.println("Error invoking handler: " + e.getMessage());
				}
			}
		}
	}

	/**
	* Removes all registered handlers from the event bus.
	*/
	public void clear() { handlers.clear(); }
}