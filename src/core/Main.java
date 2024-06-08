package core;

import task.*;
import util.seatMatrix;
import util.seatRegisters;

/**
 * Este programa simula un entorno de reservas de asientos en un sistema de reservas de vuelo implementando conceptos de programacion concurrente,
 * de tal forma que se ejecutan varios procesos de forma simultanea asegurando la exclusion mutua de los recursos compartidos por cada uno
 *
 * Autores:
 * BERNARDI, Mateo
 * LEDESMA, Ignacio
 * MADRID, Santiago
 * ROBLES, Karen Yesica
 *
 * Grupo: CONCURRENT LIFE
 *
 */

public class Main {

    public static void main(String[] args) {
        //PARAMETROS MATRIZ
        final int matrixRows = 6;
        final int matrixColumns = 31;

        //CANTIDAD DE THREADS
        final int amountReservators = 3;
        final int amountPaymentators = 2;
        final int amountValidators = 3;
        final int amountVerificators = 2;
        final int totalThreads = amountReservators + amountPaymentators + amountValidators + amountVerificators;

        //TIEMPO LOGGER
        final int logTimeInterval = 200;

        //Mientras mas grande el maxTime por proceso, mas varia el runtime en cada corrida del programa por random.nextInt()

        //PROCESO DE RESERVA
        final int maxTimeReservationOp = 250; // Siempre mayor o igual a uno por random.nextInt()

        //PROCESO DE PAGO
        final int maxTimePayOp = 200; // Siempre mayor o igual a uno por random.nextInt()
        final int chanceToFail = 10;

        //PROCESO DE VALIDACION/CANCELACION
        final int maxTimeValidationOp = 300; // Siempre mayor o igual a uno por random.nextInt()
        final int chanceToCancel = 10;

        //PROCESO DE VERIFICACION
        final int maxTimeVerificationOp = 100; // Siempre mayor o igual a uno por random.nextInt()

        //CONSTRUCCION DE OBJETOS NECESARIOS
        seatMatrix M = new seatMatrix(matrixRows,matrixColumns);
        seatRegisters Reg = new seatRegisters(M);
        Thread[] threads = new Thread[totalThreads];
        Thread logger = new Thread(new logger(M,Reg,threads,logTimeInterval));
        logger.start();

        //VARIABLES AUXILIARES
        int j = 1;

        for (int i = 0; i < amountReservators; i++) {
            threads[i] = new Thread(new Reservator(M,Reg,maxTimeReservationOp));
            threads[i].setName(threads[i].getName() + ": Reservator " + j);
            j++;
        }
        j = 1;

        for (int i = amountReservators; i < amountReservators + amountPaymentators; i++) {
            threads[i] = new Thread(new Paymentator(M,Reg,maxTimePayOp,chanceToFail));
            threads[i].setName(threads[i].getName() + ": Paymentator " + j);
            j++;
        }
        j = 1;

        for (int i = amountReservators + amountPaymentators; i < amountReservators + amountPaymentators + amountValidators; i++) {
            threads[i] = new Thread(new Validator(M,Reg,maxTimeValidationOp,chanceToCancel));
            threads[i].setName(threads[i].getName() + ": Validator " + j);
            j++;
        }
        j = 1;

        for (int i = amountReservators + amountPaymentators + amountValidators; i < totalThreads; i++) {
            threads[i] = new Thread(new Verificator(M, Reg,maxTimeVerificationOp));
            threads[i].setName(threads[i].getName() + ": Verificator " + j);
            j++;
        }

        for (Thread thread : threads) {
            System.out.println("Initializing " + thread.getName() + " ...");
            thread.start();
        }

    }
}