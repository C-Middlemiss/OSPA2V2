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
		synchronized(waitingVehicles){
			waitingVehicles.add(vehicle);
			while (hasPriority == false){
				hasPriority = true;
				for (Vehicle v : waitingVehicles){
					if (vehicle.getPriority() < v.getPriority()){
						hasPriority = false; 
					}
				}
				if (vehicle instanceof Ambulance){
					hasPriority = true; 
				}
				
				if (hasPriority == false){
					try {
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
						inside.put(vehicle, t);
						for (int i = 0; i<waitingVehicles.size(); i++){
							if (waitingVehicles.get(i).equals(vehicle)){
								waitingVehicles.remove(i);
							}
						}
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
		synchronized(waitingVehicles){			
			inside.get(vehicle).exitTunnel(vehicle);
			inside.remove(vehicle);
			
			
			waitingVehicles.notifyAll();
		}
	}
}



