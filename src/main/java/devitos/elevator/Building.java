package devitos.elevator;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

import devitos.elevator.exception.InvalidFloorException;
import devitos.elevator.util.Convert;

/**
 * Building orchestrates the elevators and persons.
 * 
 * @author Daniel de Vito <daniel_vito@yahoo.com.br>
 */
public class Building implements Runnable {

	final static Logger logger = Logger.getLogger(Building.class);

	private Queue<Person> persons;
	private Queue<Person> arrivedPersons;
	private Queue<Elevator> elevators;
	public static Date startTime = Convert.fromString("2016-08-31 10:00:00");
	public static Date currentTime = Convert.fromString("2016-08-31 10:00:00");
	private boolean isRunning = false;

	/**
	 * Constructor.
	 * 
	 * @param persons
	 *            List with all the people who want to travel.
	 */
	public Building(Queue<Person> persons) {
		this.persons = persons;
		this.arrivedPersons = new LinkedList<Person>();
		this.elevators = new LinkedList<Elevator>();
	}

	/**
	 * Starts elevators and building threads.
	 */
	public void startThreads() {
		isRunning = true;
		for (Elevator elevator : elevators) {
			(new Thread(elevator)).start();
		}
		(new Thread(this)).start();
	}

	/**
	 * Add an elevator to the building.
	 * 
	 * @param elevator
	 *            Elevator to be added to the building.
	 */
	public void addElevator(Elevator elevator) {
		this.elevators.add(elevator);
	}

	/**
	 * Checks if building thread is active.
	 * 
	 * @return boolean
	 */
	public boolean isRunning() {
		return this.isRunning;
	}

	/**
	 * Thread core implementation method.
	 */
	public void run() {
		logger.debug("start building");
		try {
			while (persons.size() > 0 || isSomebodyTraveling()) {

				logger.debug("queue size: " + persons.size() + ", current time: " + Convert.fromDate(currentTime));

				Iterator<Person> it = persons.iterator();
				while (it.hasNext()) {
					Person p = it.next();

					if (p.arrivalElevatorDate.compareTo(currentTime) <= 0) {
						Elevator elevator = getAvailableElevator(p.floor);
						if (elevator != null && elevator.addPerson(p)) {
							p.startTravelDate = (Date) currentTime.clone();
							elevator.goTo(p.floor);
							it.remove();
							arrivedPersons.add(p);
						} else {
							// logger.debug(p.name + " is waiting for an avaible
							// elevator since " +
							// Util.fromDate(p.arrivalElevatorDate));
							callStoppedElevators();
						}
					}

				}
				Thread.sleep(1000);
				currentTime.setTime(currentTime.getTime() + 1000);
			}

		} catch (InterruptedException e) {
			logger.error("thread interruption error", e);
		} catch (InvalidFloorException e) {
			logger.error("invalid floor error", e);
		}
		isRunning = false;
		logger.debug("end building");
	}

	/**
	 * Checks if there is anybody traveling on elevators.
	 * 
	 * @return boolean
	 */
	private boolean isSomebodyTraveling() {
		for (Elevator elevator : elevators) {
			if (elevator.isMoving())
				return true;
		}
		return false;
	}

	/**
	 * Call elevators back to the first floor.
	 */
	private void callStoppedElevators() {
		for (Elevator elevator : elevators) {
			if (elevator.getCurrentFloor() > 1 && !elevator.isMoving()) {
				elevator.backToFirstFloor();
			}
		}
	}

	/**
	 * Finds an available elevator on first floor.
	 * 
	 * @param floor
	 *            Target floor if one available elevator is found.
	 * @return elevator
	 */
	private Elevator getAvailableElevator(int floor) {
		for (Elevator elevator : elevators) {
			if (elevator.getCurrentFloor() == 1 && !elevator.isFull() && floor >= elevator.getFloorLimitStart()
					&& floor <= elevator.getFloorLimitEnd()) {
				return elevator;
			}
		}
		return null;
	}

	/**
	 * Returns the number of persons that arrived.
	 * 
	 * @return
	 */
	public int getArrivedPersons() {
		return arrivedPersons.size();
	}

	/**
	 * Prints the final report with metrics about waiting and travel times.
	 */
	public void printDetailedtReport() {
		Iterator<Person> it = arrivedPersons.iterator();
		logger.info("name\tarrival\tstartTravel\tendTrave\tqueueTime\ttravelTime\ttotalTime");

		float queueTimeSum = 0;
		float travelTimeSum = 0;
		float totalTimeSum = 0;

		while (it.hasNext()) {
			Person person = it.next();

			float queueTime = (person.startTravelDate.getTime() - person.arrivalElevatorDate.getTime()) / 1000;
			float travelTime = (person.endTravelDate.getTime() - person.startTravelDate.getTime()) / 1000;
			float totalTime = queueTime + travelTime;

			String line = person.name.substring(0, 10) + "\t" + Convert.fromDate(person.arrivalElevatorDate) + "\t"
					+ Convert.fromDate(person.startTravelDate) + "\t" + Convert.fromDate(person.endTravelDate) + "\t"
					+ queueTime + "\t" + travelTime + "\t" + totalTime;

			queueTimeSum += queueTime;
			travelTimeSum += travelTime;
			totalTimeSum += totalTime;

			logger.info(line);
		}

		int totalTravelers = arrivedPersons.size();
		float queueTimeAvg = 0;
		float travelTimeAvg = 0;
		float totalTimeAvg = 0;

		if (totalTravelers > 0) {
			queueTimeAvg = queueTimeSum / totalTravelers;
			travelTimeAvg = travelTimeSum / totalTravelers;
			totalTimeAvg = totalTimeSum / totalTravelers;
		}

		logger.info("Total travelers\t" + totalTravelers);
		logger.info("Simulation start\t" + Convert.fromDate(startTime));
		logger.info("Simulation end\t" + Convert.fromDate(currentTime));
		logger.info("Simulation duration (seconds)\t" + (currentTime.getTime() - startTime.getTime()) / 1000);
		logger.info("Queue time avarage (seconds)\t" + queueTimeAvg);
		logger.info("Travel time avarage (seconds)\t" + travelTimeAvg);
		logger.info("Total time avarage (seconds)\t" + totalTimeAvg);

		for (Elevator elevator : elevators) {
			logger.info(elevator.getName() + " total travels\t" + elevator.getTotalTravels());
			logger.info(elevator.getName() + " total travelers\t" + elevator.getTotalTraveledPersons());
		}
	}

}
