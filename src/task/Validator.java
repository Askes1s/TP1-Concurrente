package task;

import java.util.Random;
import util.Seat;
import util.seatMatrix;
import util.seatRegisters;

import static task.logger.startTime;

public class Validator implements Runnable {
    /** Proceso de Cancelacion/Validacion:
     *  Se tienen 3 hilos que ejecutan este proceso
     *  Cada hilo selecciona una reserva aleatoria del registro de reservas confirmadas y la cancela con una probabilidad del 10%
     *  Si la reserva es cancelada, se elimina del registro de reservas confirmadas y se agrega al registro de reservas canceladas, mientras que el asiento pasa a estado descartado
     *  Si la reserva no es cancelada, la misma se marca como checked
     */
    private final seatMatrix matrix;
    private final seatRegisters registers;
    private final int maxOpTime;
    private Random random;
    private final int chanceToFail;

    public Validator(seatMatrix matrix, seatRegisters registers, int maxOpTime, int chanceToFail) {
        this.matrix = matrix;
        this.registers = registers;
        this.maxOpTime = maxOpTime;
        random = new Random();
        this.chanceToFail = chanceToFail;
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        while (registers.getAmountChecked()+registers.getAmountCanceled()+registers.getAmountCanceledPayment()<matrix.getSeatsAmount()) {
            try {
                Thread.sleep(random.nextInt(maxOpTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Si aun no termino el proceso, espera hasta que confirmed deje de estar vacia
            //Es mas rapido que el proceso se quede esperando a que confirmed deje de estar vacio haciendo un while
            //que poner un if y forzar al proceso a irse a dormir cada vez que el confirmed esta vacio
            while (registers.ConfirmedIsEmpty()&&(registers.getAmountChecked()+registers.getAmountCanceled()+registers.getAmountCanceledPayment()<matrix.getSeatsAmount())) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Busca un asiento susceptible de ser cancelado o chequeado
            Seat seatToCheck = registers.getRandomConfirmed();

            //Si el asiento es null (que solo puede pasar si entre el segundo while y buscar un asiento, confirmed se vacio),
            //vuelve a ejecutar el proceso
            if (seatToCheck !=null) {
                //Si el asiento no fue cancelado ni se chequeó, lo cancela o lo chequea dependiendo de chance
                if (seatToCheck.isNotCanceled()&&!seatToCheck.isChecked()) {
                    if (random.nextInt(100)>chanceToFail) {
                        //Chequea el asiento
                        registers.check(seatToCheck);
                    }
                    else {
                        //Cancela el asiento
                        registers.cancelPayedProcess(seatToCheck);
                    }
                }
            }
        }
        System.out.printf("VALIDATOR RUN TIME: %.2f [s]\n", (double) (System.currentTimeMillis()-startTime)/1000);
    }
}