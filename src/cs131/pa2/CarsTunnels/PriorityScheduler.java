package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PriorityScheduler extends Tunnel{
	private volatile ArrayList<Tunnel> basicTunnels; 
	private volatile ArrayList<Vehicle> waitingVehicles; 
	private volatile HashMap<Vehicle, Tunnel> inside;
	final Lock lock = new ReentrantLock ();
	final Condition notPriority = lock.newCondition();
	final Condition notFull = lock.newCondition();
	
	
	public PriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name,log);
		basicTunnels.addAll(tunnels);
	}
	
	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		boolean entered = false; 
		boolean hasPriority = false; 
		lock.lock(); 
		while (hasPriority == false){
			hasPriority = true;
			for (Vehicle v : waitingVehicles){
				if (vehicle.getPriority() < v.getPriority()){
					hasPriority = false; 
				}
			}
			if (hasPriority == false){
				try {
					notPriority.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		while (entered == false){
			try {
				for (Tunnel t: basicTunnels){
					entered = t.tryToEnter(vehicle);
					if (entered == false){
						continue; 
					} else {
						inside.put(vehicle, t);
						notPriority.signal();
						lock.unlock();
						return true; 
					}
				}
				try {
					notFull.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} finally {
				lock.unlock();
			}
		}
		return false; 
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		lock.lock();
		inside.get(vehicle);
		try {
			inside.get(vehicle).exitTunnel(vehicle);
			inside.remove(vehicle);
			notFull.signal();
		} finally {
			lock.unlock();
		}
	}
}
