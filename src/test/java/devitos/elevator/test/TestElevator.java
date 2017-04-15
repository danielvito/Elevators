package devitos.elevator.test;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import devitos.elevator.Building;
import devitos.elevator.Elevator;
import devitos.elevator.Person;
import devitos.elevator.exception.InvalidFloorException;
import devitos.elevator.util.Convert;

public class TestElevator {

	final static Logger logger = Logger.getLogger(TestElevator.class);

	@Rule
	public ExpectedException exception = ExpectedException.none();

	/**
	 * Load file with 1000 samples.
	 * 
	 * @return List of persons
	 */
	private static Queue<Person> loadFile() {
		String csvFile = "./elevadores.csv";
		String line = "";
		String cvsSplitBy = ",";

		Queue<Person> persons = new LinkedList<Person>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(csvFile));

			while ((line = br.readLine()) != null) {
				line = line.replaceAll("\"", "");
				String[] info = line.split(cvsSplitBy);

				// logger.debug("name: " + info[0] + ", date: " + info[1] + ",
				// floor: " + info[2]);
				Person person = new Person();
				person.name = info[0];
				person.arrivalElevatorDate = Convert.fromString(info[1]);
				person.floor = Integer.valueOf(info[2]);
				persons.add(person);

			}

			br.close();

		} catch (IOException e) {
			logger.error("loading file error", e);
		}

		return persons;
	}

	/**
	 * Test load file.
	 */
	@Test
	public void testLoadFile() {
		Queue<Person> persons = loadFile();
		assertEquals(persons.size(), 1000, 0.0);
	}

	/**
	 * Full test with 1000 people and 4 elevators. It pass if all people arrive.
	 */
	@Test
	public void testChallange() {
		Queue<Person> persons = loadFile();

		int maxPersons = 8;
		int doorsInterval = 20000;

		Building building = new Building(persons);
		building.addElevator(new Elevator("Elevator-1", 1, 6, maxPersons, doorsInterval, building));
		building.addElevator(new Elevator("Elevator-2", 7, 12, maxPersons, doorsInterval, building));
		building.addElevator(new Elevator("Elevator-3", 13, 18, maxPersons, doorsInterval, building));
		building.addElevator(new Elevator("Elevator-4", 19, 25, maxPersons, doorsInterval, building));

		int initialListSize = persons.size();
		building.startThreads();

		int timeOut = 7200000; // 2 hours
		int step = 5000;
		int time = 0;
		while (building.isRunning()) {
			try {
				Thread.sleep(step);
			} catch (InterruptedException e) {
				logger.error("thread interruption error", e);
			}
			time += step;
			if (time > timeOut) {
				logger.error("Test aborted after timeout (" + timeOut + ")");
				break;
			}
		}
		building.printDetailedtReport();

		assertEquals(initialListSize, building.getArrivedPersons(), 0.0);
	}

	/**
	 * Small test with only 3 people. It pass if all people arrive.
	 */
	@Test
	public void testElevatorEveryBodyArrived() {
		int maxPersons = 8;
		int doorsInterval = 500;

		Queue<Person> persons = new LinkedList<Person>();

		String[] names = { "Wilhelmine Stracke PhD", "Leola Osinski IV", "Pietro Spencer" };
		String[] arrivalElevatorDates = { "2016-08-31 10:00:00", "2016-08-31 10:00:04", "2016-08-31 10:00:10" };
		Integer[] floors = { 20, 11, 21 };

		for (int i = 0; i < names.length; i++) {
			Person person = new Person();
			person.name = names[i];
			person.arrivalElevatorDate = Convert.fromString(arrivalElevatorDates[i]);
			person.floor = floors[i];
			persons.add(person);
		}

		Building building = new Building(persons);
		building.addElevator(new Elevator("Elevator-1", 1, 25, maxPersons, doorsInterval, building));

		int initialListSize = persons.size();
		building.startThreads();

		int timeOut = 120000;
		int step = 5000;
		int time = 0;
		while (building.isRunning()) {
			try {
				Thread.sleep(step);
			} catch (InterruptedException e) {
				logger.error("thread interruption error", e);
			}
			time += step;
			if (time > timeOut) {
				logger.error("Test aborted after timeout (" + timeOut + ")");
				break;
			}
		}
		building.printDetailedtReport();

		assertEquals(initialListSize, building.getArrivedPersons(), 0.0);

	}

	/**
	 * Test if invalid floor works properly.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvalidFloor() throws Exception {
		exception.expect(InvalidFloorException.class);

		int maxPersons = 8;
		int doorsInterval = 500;

		Queue<Person> persons = new LinkedList<Person>();
		Building building = new Building(persons);
		Elevator elevator = new Elevator("Elevator-1", 1, 25, maxPersons, doorsInterval, building);
		building.addElevator(elevator);

		elevator.goTo(26);
	}

}
