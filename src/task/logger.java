package task;

import util.seatMatrix;
import util.seatRegisters;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class logger implements Runnable {
    static long startTime;
    private seatRegisters registers;
    private Thread[] threads;
    private final int opTime;
    private final seatMatrix matrix;

    public logger(seatMatrix matrix, seatRegisters registers, Thread[] threads, int opTime) {
        logger.startTime = System.currentTimeMillis();
        this.registers = registers;
        this.threads = threads;
        this.opTime = opTime;
        this.matrix = matrix;
    }

    @Override
    public void run() {
        /**
         * This thread writes the status of the other threads in a .log file
         * until they finish their tasks
         */
        try (FileWriter file = new FileWriter(".\\logs\\run.log"); PrintWriter pw = new PrintWriter(file)) {
            boolean finish = false;
            while (!finish) {
                try {
                    Thread.sleep(opTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                writeThreadInfo(pw, threads, registers);
                finish = true;
                for (Thread thread : threads) {
                    finish = finish && (thread.getState() == Thread.State.TERMINATED);
                }
            }
            // Print info in .log
            pw.printf("TOTAL RUN TIME: %.2f [s]", (double) (System.currentTimeMillis() - startTime) / 1000);
            double aux1 = (double) registers.verifiedSize() * 100;
            pw.printf("\nTOTAL FLIGHT OCCUPATION: %.2f%%", (aux1/matrix.getSeatsAmount()));
            // Show info in terminal
            System.out.printf("\n\nTOTAL RUN TIME: %.2f [s]\n\n", (double) (System.currentTimeMillis() - startTime) / 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method writes the state of a thread in a file
     * It is used inside run()
     *
     * @param pw         PrintWriter to write the data
     * @param threads    Threads whose information will be written
     * @param register   Registros de asientos
     */
    private static void writeThreadInfo(PrintWriter pw, Thread[] threads, seatRegisters register) {
        pw.printf("---------------------------------------------\n");
        pw.printf("\nTime since start: %d [ms]\n\n", System.currentTimeMillis() - startTime);
        pw.printf("---------------------------------------------\n");
        pw.printf("\nREGISTER STATUS: \n\n");
        pw.printf("\t- Reserved seats waiting for being processed (seats in array): \t%d\n", register.pendingSize());
        pw.printf("\t- Reserved seats processed: \t%d\n", register.getAmountReserved());
        pw.printf("\t- Confirmed seats waiting for being processed: \t%d\n", register.confirmedSize());
        pw.printf("\t- Confirmed seats processed: \t%d\n", register.getAmountConfirmed());
        pw.printf("\t- Canceled PAYMENTS processed: \t%d\n", register.getAmountCanceledPayment());
        pw.printf("\t- Checked SEATS processed: \t%d\n", register.getAmountChecked());
        pw.printf("\t- Canceled SEATS processed: \t%d\n", register.getAmountCanceled());
        pw.printf("\t- TOTAL CANCELLATIONS: \t%d\n", register.canceledSize());
        pw.printf("\t- VERIFIED PASSENGERS: \t%d\n", register.verifiedSize());
        pw.printf("\t- ORDERS PROCESSED: \t%d\n", register.verifiedSize()+register.canceledSize());
        pw.println();
        pw.printf("---------------------------------------------\n");
        pw.printf("\nTHREADS STATUS: \n\n");
        for (Thread thread : threads) {
            pw.printf("\t- %s  State: \t%s\n", thread.getName(), thread.getState());
        }
        pw.println();
        pw.printf("---------------------------------------------\n");
        pw.println();

    }
}
