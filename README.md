# Robot Mapping Simulation (Java)

This project simulates a vacuum robot mapping system using a multithreaded microservice architecture in Java. Each sensor runs in its own thread and communicates via a central message bus to produce a final map of the environment.

## 🧠 Features

- **Microservice architecture** using object-oriented design.
- **Multithreading** with one service thread per sensor.
- **Central message bus** (singleton) for inter-service communication.
- **Thread-safe design** using:
  - Synchronization
  - Read-write locks
  - Atomic operations
- **Error handling** for malformed or inconsistent input.

## ▶️ How to Run

To run the simulation with Maven:

From the project root:
```bash
mvn exec:java \
  -Dexec.mainClass="bgu.spl.mics.application.GurionRockRunner" \
  -Dexec.args="\"example input/configuration_file.json\" \"example input/camera_data.json\" \"example input/lidar_data.json\" \"example input/pose_data.json\""
```

You can also use:
example_input_2/ or example_input_with_error/ (contains malformed)

The resulting output will be generated inside the input directory (e.g., example input/output.json).

🧪 Running Tests: mvn test