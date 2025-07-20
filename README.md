# Custom Command Scheduler

This project implements a simple Java-based command scheduler that reads commands from a text file and executes them at specified times or intervals. It supports both one-time executions at a fixed date/time and recurring executions with a fixed delay.

# How It Works
The Scheduler class performs the following steps:

1.It reads each line from ./src/com/src/amex/test/tmp/commands.txt.

2.Based on the command format, it determines if it's a one-time or recurring task.

3.It schedules the tasks using Java's ScheduledExecutorService.

4.When a task is executed, it uses an ExecutorService backed by virtual threads to write the specified data to ./src/com/src/amex/test/tmp/sample-output.txt.
