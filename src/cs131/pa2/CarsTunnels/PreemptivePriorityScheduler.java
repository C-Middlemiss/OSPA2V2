package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PreemptivePriorityScheduler extends Tunnel{
	private volatile ArrayList<Tunnel> basicTunnels; 
	private volatile ArrayList<Vehicle> waitingVehicles; 
	private volatile HashMap<Vehicle, Tunnel> inside;

	public PreemptivePriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name,log);
		basicTunnels = new ArrayList<Tunnel>(); 
		waitingVehicles = new ArrayList<Vehicle>(); 
		inside = new HashMap<Vehicle, Tunnel>(); 
		for (Tunnel t : tunnels){
			this.basicTunnels.add(t);
		}
	}
	
	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		boolean entered = false; 
		boolean hasPriority = false; 
		
		//sync on ArrayList
		synchronized(waitingVehicles){
			
			//add vehicle to waiting queue
			waitingVehicles.add(vehicle);
			while (hasPriority == false){
				hasPriority = true;
				
				//try and sort based on priority
				for (Vehicle v : waitingVehicles){
					if (vehicle.getPriority() < v.getPriority()){
						hasPriority = false; 
					}
				}
				if (vehicle instanceof Ambulance){
					
					//ambulance goes to the front no matter what
					hasPriority = true; 
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
						
						//vehicle goes in the tunnel
						inside.put(vehicle, t);
						for (int i = 0; i<waitingVehicles.size(); i++){
							
							//get the right vehicle out of the queue
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
		
		//sync on ArrayList
		synchronized(waitingVehicles){			
			inside.get(vehicle).exitTunnel(vehicle);
			inside.remove(vehicle);
			
			
			waitingVehicles.notifyAll();
		}
	}
}



