package task;

import java.util.Random;
import util.Seat;
import util.seatMatrix;
import util.seatRegisters;

import static task.logger.startTime;

public class Reservator implements Runnable {
    /** Proceso de reserva:
     *  Este proceso se encarga de recibir las solicitudes de reserva de los usuarios
     *  Se tienen 3 hilos que ejecutan este proceso
     *  Cada hilo intenta reservar un asiento aleatorio en la matriz, verificando que este disponible
     *  Si el asiento no esta libre, el hilo debe buscar otro asiento que si lo este
     *  Una vez reservado el asiento, el mismo se marca como ocupado y se registra la reserva pendiente en el registro de reservas pendientes
     */
    private final seatMatrix matrix;
    private seatRegisters registers;
    private final int maxOpTime;
    private Random random;
    private int randomRow;
    private int randomCol;
    private Seat seatToReserve;

    public Reservator(seatMatrix matrix, seatRegisters registers, int maxOpTime) {
        this.matrix = matrix;
        this.registers = registers;
        this.maxOpTime = maxOpTime;
        random = new Random();
        randomRow = random.nextInt(matrix.getRows());
        randomCol = random.nextInt(matrix.getCols());
        seatToReserve = matrix.getSeat(randomRow, randomCol);
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        while (registers.getAmountReserved()<matrix.getSeatsAmount()) {
            try {
                Thread.sleep(random.nextInt(maxOpTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Si aun no termino el proceso, busca hasta encontrar un asiento libre

            //Se hace un while y no un if porque el proceso tardaria mas en encontrar un asiento teniendo en cuenta
            //el sleep que se le hace cada vez que termina (ejemplo: si hay 100 asientos en la matriz y 99 estan reservados,
            //cuando lo que buscamos es encontrar un asiento libre, el proceso podria a llegar a tardar mucho en encontrar un asiento
            //valido debido a que tendria que hacer un sleep cada vez que falla en encontrarlo)
            while (seatToReserve.isTaken()&&registers.getAmountReserved()<matrix.getSeatsAmount()) {
                randomizeSeat();
            }

            //Reserva el asiento
            registers.reservationProcess(seatToReserve);
            //Busca otro asiento libre para la siguiente iteracion
            randomizeSeat();
        }
        System.out.printf("RESERVATOR RUN TIME: %.2f [s]\n", (double) (System.currentTimeMillis()-startTime)/1000);
    }

    public void randomizeSeat() {
        randomRow = random.nextInt(matrix.getRows());
        randomCol = random.nextInt(matrix.getCols());
        seatToReserve = matrix.getSeat(randomRow,randomCol);
    }
}