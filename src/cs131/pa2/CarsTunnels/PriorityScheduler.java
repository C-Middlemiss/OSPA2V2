package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
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
	
	public PriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name,log);
		basicTunnels = new ArrayList<Tunnel>(); 
		
		//arraylist of vehicles waiting
		waitingVehicles = new ArrayList<Vehicle>(); 
		
		//Hashmap of cars inside
		inside = new HashMap<Vehicle, Tunnel>(); 
		for (Tunnel t : tunnels){
			this.basicTunnels.add(t);
		}
	}
	
	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		boolean entered = false; 
		boolean hasPriority = false; 
		
		//sync on the ArrayList
		synchronized(waitingVehicles){
			
			//add vehicle to the waiting queue
			waitingVehicles.add(vehicle);
			while (hasPriority == false){
				hasPriority = true;
				for (Vehicle v : waitingVehicles){
					
					//checks if vehicle has priority over the other
					if (vehicle.getPriority() < v.getPriority()){
						hasPriority = false; 
					}
				}
				if (hasPriority == false){
					try {
						
						//wait until notified
						waitingVehicles.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			while (entered == false){
				for (Tunnel t: basicTunnels){
					entered = t.tryToEnter(vehicle);
					if (entered == false){
						continue; 
					} else {
						
						//put vehicle inside tunnel
						inside.put(vehicle, t);
						for (int i = 0; i<waitingVehicles.size(); i++){
							if (waitingVehicles.get(i).equals(vehicle)){
								waitingVehicles.remove(i);
							}
						}
						
						//wake up all threads
						waitingVehicles.notifyAll();
						return true; 
					}
				}
				try {
					waitingVehicles.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return false; 
		}
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		
		//sync on the ArrayList
		synchronized(waitingVehicles){			
			inside.get(vehicle).exitTunnel(vehicle);
			inside.remove(vehicle);
			
			//wake up all threads
			waitingVehicles.notifyAll();
		}
	}
}