package cs131.pa2.CarsTunnels;

import java.util.ArrayList;
import java.util.Collection;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PriorityScheduler extends Tunnel{
	private volatile ArrayList<Tunnel> basicTunnels; 
	private volatile ArrayList<Vehicle> waitingVehicles; 
	
	
	public PriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name,log);
		basicTunnels.addAll(tunnels);
	}
	
	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		int i = 0; 
		waitingVehicles.add(vehicle); //will be changed to add and sort method 
		while (waitingVehicles.size() != 0){
			synchronized(basicTunnels.get(i)){
				if (basicTunnels.get(i).tryToEnter(waitingVehicles.get(0)) == false){
					try {
						basicTunnels.get(i).wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					i++;
					//System.out.print(i);
					
				} else {
					vehicle.setCurrTunnel(basicTunnels.get(i));
				}
			}
			
		}
		return false; 
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		boolean done = false; 
		boolean gotTunnel = false; 
		int i = 0;
		while (gotTunnel == false){
			if (basicTunnels.get(i).equals(vehicle.getCurrTunnel())){
				gotTunnel = true; 
				
			} else {
				//System.out.print("here" + i);
				i ++; 
			}
		}
		
		while (done == false){
			synchronized(basicTunnels.get(i)){
				basicTunnels.get(i).exitTunnel(vehicle);
				done = true; 
				this.notifyAll();
			}
		}
	}
	
}
