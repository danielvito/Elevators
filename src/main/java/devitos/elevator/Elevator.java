package devitos.elevator;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import devitos.elevator.exception.InvalidFloorException;
import devitos.elevator.util.Convert;

/**
 * Elevators can take people to the specified floor.
 * 
 * @author Daniel de Vito <daniel_vito@yahoo.com.br>
 */
public class Elevator implements Runnable {

	final static Logger logger = Logger.getLogger(Elevator.class);

	private Building building;
	private Queue<Person> persons;

	private String name;
	private int floorLimitStart;
	private int floorLimitEnd;
	private int maxPersons;
	private int currentFloor = 1;
	private int timeBetweenFloors = 500;
	private int doorsInterval;
	private boolean isMoving = false;
	private boolean isComingBackToFirstFloor = false;
	private int destinationFloor = currentFloor;
	private int totalTravels = 0;
	private int totalTraveledPersons = 0;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            Elevator name.
	 * @param floorLimitStart
	 *            Minimum floor limit.
	 * @param floorLimitEnd
	 *            Maximum floor limit.
	 * @param maxPersons
	 *            Maximum elevator capacity.
	 * @param doorsInterval
	 *            Average time that the elevator takes to open and close de
	 *            doors.
	 * @param building
	 *            Parent building.
	 */
	public Elevator(String name, int floorLimitStart, int floorLimitEnd, int maxPersons, int doorsInterval,
			Building building) {
		this.persons = new LinkedList<Person>();
		this.floorLimitStart = floorLimitStart;
		this.floorLimitEnd = floorLimitEnd;
		this.maxPersons = maxPersons;
		this.doorsInterval = doorsInterval;
		this.name = name;
		this.building = building;
	}

	/**
	 * Floor start limit getter.
	 * 
	 * @return int
	 */
	public int getFloorLimitStart() {
		return floorLimitStart;
	}

	/**
	 * Floor end limit getter.
	 * 
	 * @return int
	 */
	public int getFloorLimitEnd() {
		return floorLimitEnd;
	}

	/**
	 * Try to add a person to the elevator.
	 * 
	 * @param person
	 *            Person
	 * @return boolean
	 */
	public boolean addPerson(Person person) {
		totalTraveledPersons++;
		if (persons.size() < maxPersons) {
			persons.add(person);
			logger.debug(person.name + " entered the " + name + " on " + this.currentFloor + " floor at "
					+ Convert.fromDate(Building.currentTime) + ", total elevator: " + persons.size());
			return true;
		}
		return false;
	}

	/**
	 * Current number of persons on the elevator.
	 * 
	 * @return int
	 */
	public int currentNumberOfPersons() {
		return persons.size();
	}

	/**
	 * Sets a floor destination to the elevator.
	 * 
	 * @param destinationFloor
	 *            Floor
	 * @throws InvalidFloorException
	 */
	public void goTo(int destinationFloor) throws InvalidFloorException {
		if (destinationFloor < 1 || destinationFloor > floorLimitEnd)
			throw new InvalidFloorException("Destionation floor " + destinationFloor
					+ " is invalid. Floor must be between 1 and " + floorLimitEnd + ".");

		if (destinationFloor > this.destinationFloor)
			this.destinationFloor = destinationFloor;
		logger.debug(name + " going to " + this.destinationFloor + " floor");
	}

	/**
	 * Set first floor as destination.
	 */
	public void backToFirstFloor() {
		if (this.currentFloor > 1) {
			this.destinationFloor = 1;
			isComingBackToFirstFloor = true;
		}
	}

	/**
	 * Thread core implementation method.
	 */
	public void run() {
		logger.debug("start elevator " + name);
		try {
			while (building.isRunning()) {
				if (this.currentFloor == this.destinationFloor) {
					logger.debug(name + " is stopped on " + this.currentFloor + " floor with " + persons.size()
							+ " person(s)");
					this.isMoving = false;
					Thread.sleep(1000);
				} else {
					this.isMoving = true;
					if (this.currentFloor < this.destinationFloor) {
						move("UP");
					} else {
						move("DOWN");
					}
				}
			}
		} catch (InterruptedException e) {
			logger.error("thread interruption error", e);
		}
		logger.debug("end elevator " + name);
	}

	/**
	 * Current floor getter.
	 * 
	 * @return int
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * Moves the elevator one floor UP or DOWN.
	 * 
	 * @param direction
	 *            UP or DOWN
	 * @throws InterruptedException
	 */
	private void move(String direction) throws InterruptedException {
		Thread.sleep(this.timeBetweenFloors);
		if (direction.equals("UP"))
			this.currentFloor++;
		else
			this.currentFloor--;
		logger.debug(name + " got floor " + this.currentFloor + ", going to " + this.destinationFloor);
		Iterator<Person> it = persons.iterator();
		boolean somebodyHasLeft = false;
		while (it.hasNext()) {
			Person p = it.next();
			if (p.floor == this.currentFloor) {
				p.endTravelDate = (Date) Building.currentTime.clone();
				it.remove();
				long seconds = (p.endTravelDate.getTime() - p.startTravelDate.getTime()) / 1000;
				logger.debug(p.name + " left the " + name + " on " + this.currentFloor + " floor at "
						+ Convert.fromDate(Building.currentTime) + " and it took " + seconds
						+ " seconds, total elevator: " + persons.size());
				somebodyHasLeft = true;
			}
		}

		if (somebodyHasLeft || this.currentFloor == 1) {
			logger.debug(name + " on floor " + this.currentFloor + " at " + Convert.fromDate(Building.currentTime)
					+ " oppened the doors");
			Thread.sleep(doorsInterval);
			logger.debug(name + " on floor " + this.currentFloor + " at " + Convert.fromDate(Building.currentTime)
					+ " closed the doors");
		}

		// travel has been started
		if (!isComingBackToFirstFloor && currentFloor == 2) {
			totalTravels++;
		}

		if (this.currentFloor == 1)
			isComingBackToFirstFloor = false;
	}

	/**
	 * Checks if the elevator is moving.
	 * 
	 * @return boolean
	 */
	public boolean isMoving() {
		return isMoving;
	}

	/**
	 * Checks if the elevator is coming back to the first floor.
	 * 
	 * @return boolean
	 */
	public boolean isComingBackToFirstFloor() {
		return isComingBackToFirstFloor;
	}

	/**
	 * Checks if the elevator is full.
	 * 
	 * @return boolean
	 */
	public boolean isFull() {
		if (this.persons.size() < maxPersons)
			return false;
		return true;
	}

	/**
	 * Elevator's name getter.
	 * 
	 * @return String
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Total travels getter.
	 * 
	 * @return int
	 */
	public int getTotalTravels() {
		return totalTravels;
	}

	/**
	 * Total traveled persons getter.
	 * 
	 * @return int
	 */
	public int getTotalTraveledPersons() {
		return totalTraveledPersons;
	}

}
