
/**
 * BoundBufferExample class simulates a bounded buffer.
 * The buffer is a fixed size and can be specific via command line arguments.
 *
 * @author Ian Duffy
 * @author Richard Kavanagh
 * @author Darren Brogan
 */
public class BoundBufferExample {
  public static void main(String[] args) throws InterruptedException {

    // Set a default size for the bounded buffer
    int size=50;

    // Change the size based on command line arguments if any
    if(args.length == 1) {
      try {
        size = Integer.parseInt(args[0]);
      } catch(NumberFormatException e) {
        System.out.println("Invalid Argument given");
        System.exit(1);
      }
    }

    // Create the bounded buffer
    BoundedBuffer buffer = new BoundedBuffer(size);

    // Create the threads
    Producer producer = new Producer(buffer);
    Consumer consumer = new Consumer(buffer);
    Watcher  watcher  = new Watcher(buffer);

    // Start the threads
    producer.start();
    consumer.start();
    watcher.start();

    // End the threads after 60 seconds
    Thread.sleep(60 * 1000);
    producer.interrupt();
    consumer.interrupt();
    watcher.interrupt();
    producer.join();
    consumer.join();
    watcher.join();

    System.out.print("Average wait time is: ");
    buffer.getAverageWait();
  }
}

/**
 * BoundedBuffer represents a buffer of a fixed size.
 */
class BoundedBuffer {
  private int[] buffer;
  private int nextIn, nextOut, size, occupied, ins, outs;
  private long[] times;
  private long totalTimes;

  BoundedBuffer(int size) {
    // Initialize the buffer
    this.size = size;
    buffer = new int[size];
    times = new long[size];

    // Initialize the required variables
    nextIn          = 0;
    nextOut         = 0;
    occupied        = 0;
    ins             = 0;
    outs            = 0;
  }

  /**
   * Outputs the current status of the buffer.
   */
  void getStatus() {
    System.out.println("Delta = " + (ins - outs - occupied)
                       + " Occupied = " + occupied);
  }

  /**
   * Outputs the average wait
   */
  void getAverageWait() {
    if(outs>0) {
      System.out.println((double)totalTimes/(double)outs);
    }
  }

  /**
   * Gets value stored at nextOut and returns it.
   *
   * @return a integer from the buffer at index nextOut
   */
  synchronized int get() {
    // Wait until the buffer isn't empty
    while(occupied == 0) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }

    totalTimes += System.currentTimeMillis() - times[nextOut];

    int contents = buffer[nextOut];
    nextOut=(nextOut+1)%size;
    occupied--;
    outs++;

    // Notify other threads of completion
    notifyAll();

    return contents;
  }

  /**
   * Inserted the given parameter into the buffer
   *
   * @param a int containing the value to the placed into the buffer
   */
  synchronized void put(int value) {
    // Wait until the buffer isn't full
    while(occupied == size) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }

    buffer[nextIn] = value;
    times[nextIn] = System.currentTimeMillis();
    nextIn=(nextIn+1)%size;
    occupied++;
    ins++;

    // Notify other threads of completion
    notifyAll();
  }
}

/**
 * The Producer generates a random integer between 0 and 100.
 * It inserts it to the buffer and then sleeps for a random duration between
 * 0 and 100ms
 */
class Producer extends Thread {
  private BoundedBuffer buffer;

  Producer(BoundedBuffer buffer) {
    this.buffer = buffer;
  }

  public synchronized void run() {
    try {
      while(true) {
        // Generate a random number between 0 and 100
        int randomNumber = (int)(Math.random() * 100);

        // Place the random number onto the buffer
        buffer.put(randomNumber);

        // Sleep for for the duration of the random number
        sleep(randomNumber);
      }
    } catch (InterruptedException e) {
      System.out.println("Goodbye from Producer");
      return;
    }
  }
}

/**
 * The consumer gets integers from the buffer and then sleep for a random
 * duration between 0 and 100ms
 */
class Consumer extends Thread {
  private BoundedBuffer buffer;

  Consumer(BoundedBuffer buffer) {
    this.buffer = buffer;
  }

  public synchronized void run() {
    try {
      while(true) {
        // Get an integer from the buffer
        buffer.get();

        // Sleep for a random amount of time
        sleep((int)(Math.random() * 100));
      }
    } catch(InterruptedException e) {
      System.out.println("Goodbye from Consumer");
      return;
    }
  }
}

/**
 * The watcher wakes up every 1 second and prints out the status
 * of the buffer.
 */
class Watcher extends Thread {
  private BoundedBuffer buffer;

  Watcher(BoundedBuffer buffer) {
    this.buffer = buffer;
  }

  public synchronized void run() {
    try {
      while(true) {
        // Sleep for 1 second
        sleep(1 * 1000);

        // Print out the status of the buffer
        buffer.getStatus();
      }
    } catch(InterruptedException e) {
      System.out.println("Goodbye from Watcher");
      return;
    }
  }
}
