package cs131.pa2.CarsTunnels;

import java.awt.List;
import java.util.ArrayList;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;

public class BasicTunnel extends Tunnel{
	public volatile ArrayList<Vehicle> occupantsVehicles; 
	private static int maxVehicles; 
	
	public BasicTunnel(String name) {
		super(name);
		maxVehicles = 3; 
		occupantsVehicles = new ArrayList<Vehicle>();
	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) {
		//deal with Sled
		if (vehicle instanceof Sled || vehicle instanceof Ambulance){	
			if (occupantsVehicles.size() > 0){
				return false;
			}
			occupantsVehicles.add(vehicle);
			return true; 
		} else {
			//deal with sleds
			for (int i = 0; i < occupantsVehicles.size(); i++){
				if (occupantsVehicles.get(i) instanceof Sled || occupantsVehicles.get(i) instanceof Ambulance){
					return false; 
				}
			}
			//deal with direction
			if (occupantsVehicles.size() > 0 && occupantsVehicles.get(0).getDirection() != vehicle.getDirection()){
				return false;
			}
			//deal with capacity
			if (occupantsVehicles.size() >= this.maxVehicles){
				return false;
			} 
			//System.out.println("enter" + vehicle.getName());
			occupantsVehicles.add(vehicle);
			return true; 
		}
	}
		

	@Override
	public synchronized void exitTunnelInner(Vehicle vehicle) {
		for (int i = 0; i < occupantsVehicles.size(); i++){
			if (occupantsVehicles.get(i).equals(vehicle)){
				occupantsVehicles.remove(i);
			}
		}
	}
}
