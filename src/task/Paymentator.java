package task;

import java.util.Random;
import util.Seat;
import util.seatMatrix;
import util.seatRegisters;

import static task.logger.startTime;

public class Paymentator implements Runnable {
    /** Proceso de pago:
     *  Este proceso se encarga de verificar el pago de las reservas pendientes
     *  Se tienen 2 hilos que ejecutan este proceso
     *  Cada hilo toma una reserva aleatoria del registro de reservas pendientes y realiza una verificacion de pago
     *  Se establece una probabilidad del 90% de que el pago sea aprobado y 10% de que sea rechazado
     *  Si el pago es aprobado, se elimina del registro de pendientes y se agrega al registro de reservas confirmadas
     *  Si el pago es rechazado, el asiento pasa a estado descartado mientras que la reserva se marca como cancelada, se elimina del registro de pendientes y se agrega al de reservas canceladas
     */
    private final seatMatrix matrix;
    private seatRegisters registers;
    private final int maxOpTime;
    private Random random;
    private final int chanceToFail;

    public Paymentator(seatMatrix matrix, seatRegisters registers, int maxOpTime, int chanceToFail) {
        this.matrix = matrix;
        this.registers = registers;
        this.maxOpTime = maxOpTime;
        random = new Random();
        this.chanceToFail = chanceToFail;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        while (registers.getAmountCanceledPayment()+registers.getAmountConfirmed()<matrix.getSeatsAmount()) {
            try {
                Thread.sleep(random.nextInt(maxOpTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Si aun no termina el proceso, espera hasta que pending deje de estar vacia
            //Es mas rapido que el proceso se quede esperando a que pending deje de estar vacio haciendo un while
            //que poner un if y forzar al proceso a irse a dormir cada vez que el pending esta vacio
            while (registers.PendingIsEmpty()&&(registers.getAmountCanceledPayment()+registers.getAmountConfirmed()<matrix.getSeatsAmount())) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Busca un asiento susceptible de ser cancelado o pagado
            Seat seatToPay = registers.getRandomReservation();

            //Si el asiento es null (que solo puede pasar si entre el segundo while y buscar un asiento, pending se vacio)
            //vuelve a ejecutar el proceso
            if (seatToPay !=null) {
                if (random.nextInt(100)>chanceToFail) {
                    //Confirma el pago
                    registers.confirmPaymentProcess(seatToPay);
                }
                else {
                    //Cancela el pago
                    registers.cancelPaymentProcess(seatToPay);
                }
            }
        }
        System.out.printf("PAYMENTATOR RUN TIME: %.2f [s]\n", (double) (System.currentTimeMillis()-startTime)/1000);
    }
}