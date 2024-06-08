package task;

import java.util.Random;
import util.Seat;
import util.seatMatrix;
import util.seatRegisters;

import static task.logger.startTime;

public class Verificator implements Runnable {
    /** Proceso de verificacion:
     *  Este proceso selecciona de manera aleatoria una reserva del registro de reservas confirmadas
     *  Para cada reserva marcada como "checked", se debe eliminar del registro de reservas confirmadas y se debe insertar en el registro de reservas verificadas
     *  Este proceso es ejecutado por dos hilos
     */
    private final seatMatrix matrix;
    private final seatRegisters registers;
    private final int maxOpTime;
    private Random random;

    public Verificator(seatMatrix matrix, seatRegisters registers, int maxOpTime) {
        this.matrix = matrix;
        this.registers = registers;
        this.maxOpTime = maxOpTime;
        random = new Random();
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        while (registers.getAmountVerified()+registers.getAmountCanceled()+registers.getAmountCanceledPayment()<matrix.getSeatsAmount()) {
            try {
                Thread.sleep(random.nextInt(maxOpTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Si aun no termino el proceso, espera hasta que confirmed deje de estar vacia
            //Es mas rapido que el proceso se quede esperando a que confirmed deje de estar vacio haciendo un while
            //que poner un if y forzar al proceso a irse a dormir cada vez que el confirmed esta vacio
            while (registers.ConfirmedIsEmpty()&&(registers.getAmountVerified()+registers.getAmountCanceled()+registers.getAmountCanceledPayment()<matrix.getSeatsAmount())) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Busca una asiento susceptible de ser verificado
            Seat seatToVerify = registers.getRandomChecked();
            //Si el asiento es null (que solo puede pasar si entre el segundo while y buscar un asiento, confirmed se vacio)
            //vuelve a ejecutar el proceso
            if (seatToVerify !=null) {
                //Verifica al asiento
                registers.verificationProcess(seatToVerify);
            }
        }
        System.out.printf("VERIFICATOR RUN TIME: %.2f [s]\n", (double) (System.currentTimeMillis()-startTime)/1000);
    }

}