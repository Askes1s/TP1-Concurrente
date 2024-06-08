package util;

import java.util.ArrayList;
import java.util.Random;

public class seatRegisters {
    private final ArrayList<Seat> pending;
    private final ArrayList<Seat> confirmed;
    private final ArrayList<Seat> canceled;
    private final ArrayList<Seat> verified;
    //Poner final en los arrays quiere decir que no puedo reapuntar la variable que le asigne a otra instancia
    private final seatMatrix matrix;
    private final Object reservedLock;
    private final Object confirmedLock;
    private final Object canceledPaymentLock;
    private final Object canceledLock;
    private final Object verifiedLock;
    private final Object checkLock;
    private volatile int amountReserved;
    private volatile int amountConfirmed;
    private volatile int amountCanceledPayment;
    private volatile int amountChecked;
    private volatile int amountCanceled;
    private volatile int amountVerified;
    private Random randomReservation;
    private Random randomConfirmed;
    private Random randomChecked;

    public seatRegisters(seatMatrix matrix) {
        this.pending = new ArrayList<>();
        this.confirmed = new ArrayList<>();
        this.canceled = new ArrayList<>();
        this.verified = new ArrayList<>();
        this.matrix = matrix;
        this.reservedLock = new Object();
        this.confirmedLock = new Object();
        this.canceledPaymentLock = new Object();
        this.checkLock = new Object();
        this.canceledLock = new Object();
        this.verifiedLock = new Object();
        this.randomReservation = new Random();
        this.randomConfirmed = new Random();
        this.randomChecked = new Random();
        this.amountReserved = 0;
        this.amountConfirmed = 0;
        this.amountCanceledPayment = 0;
        this.amountCanceled = 0;
        this.amountVerified = 0;
    }

    public void reservationProcess(Seat seat) {
        //Puede darse el caso de que 2 hilos hayan encontrado al mismo asiento libre,
        //si uno lo checkea y otro llega mas tarde, si no se pone el if isFree(), puede que se realice
        //este proceso nuevamente con el mismo asiento e incrementaria amountReserved erroneamente

        //Por como estan planteadas las secciones criticas, todo asiento que se AGREGUE a pending va a tener taken=true y free=false

        synchronized (seat) {
            if (seat.isFree()) {
                synchronized (pending) {
                    seat.reserve();
                    pending.add(seat);
                    incAmountReserved();
                }
                /*System.out.printf("\n%s RESERVATION Completed! Seat number: %d\n", Thread.currentThread().getName(), seat.getNumber());
                System.out.println("Seats RESERVED IN TOTAL: " + amountReserved);
                System.out.println("Seats RESERVED: " + pending.size());
                System.out.println("-----Inside array:-----");
                for (Seat s : pending) {
                    System.out.print(s.getNumber() + " ");
                }
                System.out.println();
                System.out.println("-----------------------");*/
            }
        }
    }

    public void confirmPaymentProcess(Seat seat) {
        //Por como estan planteadas las secciones criticas, primero se saca de pending y luego se confirma
        //asi que todo:NUNCA SE VA A DAR EL CASO DE QUE SE ENCUENTRE UN ASIENTO CONFIRMADO EN PENDING
        //y ademas todo asiento que se AGREGUE a confirmed va a tener payed=true

        //De todas formas, puede darse el caso de que 2 hilos hayan encontrado la misma reserva,
        //si uno la confirma y otro llega mas tarde, si no se pone el if !isPayed(), puede que se realice
        //este proceso nuevamente con el mismo asiento e incrementaria amountConfirmed erroneamente

        synchronized (seat) {
            if (seat.isNotCanceled()&&!seat.isPayed()){
                synchronized (pending) {
                    pending.remove(seat);
                }
                synchronized (confirmed) {
                    seat.setPayed(true);
                    confirmed.add(seat);
                    incAmountConfirmed();
                }
                /*System.out.printf("\n%s PAYMENT ACCEPTED! Seat number: %d\n", Thread.currentThread().getName(), seat.getNumber());
                System.out.println("Seats PAYED IN TOTAL: " + amountConfirmed);
                System.out.println("Seats PAYED: " + confirmed.size());
                System.out.println("-----Inside array:-----");
                for (Seat s : confirmed) {
                    System.out.print(s.getNumber() + " ");
                }
                System.out.println();
                System.out.println("-----------------------");*/
            }
        }
    }

    public void cancelPaymentProcess(Seat seat) {
        //Por como estan planteadas las secciones criticas, primero se saca de pending y luego se cancela
        //asi que todo:NUNCA SE VA A DAR EL CASO DE QUE SE ENCUENTRE UN ASIENTO CANCELADO EN PENDING
        //y ademas todo asiento que se AGREGUE a canceled va a tener canceled=true

        //De todas formas, puede darse el caso de que 2 hilos hayan encontrado la misma reserva,
        //si uno la cancela y otro llega mas tarde, si no se pone el if isNotCanceled(), puede que se realice
        //este proceso nuevamente con el mismo asiento e incrementaria amountCanceled erroneamente

        synchronized (seat) {
            if (seat.isNotCanceled()&&!seat.isPayed()){
                synchronized (pending) {
                    pending.remove(seat);
                }
                synchronized (canceled) {
                    seat.setCanceled(true);
                    canceled.add(seat);
                    incAmountCanceledPayment();
                }
                /*System.out.printf("\n%s PAYMENT REJECTED! Seat number: %d\n", Thread.currentThread().getName(), seat.getNumber());
                System.out.println("Seats CANCELED IN TOTAL: " + amountCanceledPayment);
                System.out.println("-----Inside array:-----");
                for (Seat s : canceled) {
                System.out.print(s.getNumber() + " ");
                }
                System.out.println();
                System.out.println("-----------------------");*/
            }
        }
    }

    public void check(Seat seat) {
        //todo: NO TODOS LOS ASIENTOS EN CONFIRMED TIENEN CHECK=TRUE

        //Puede darse el caso de que 2 hilos hayan encontrado al mismo asiento confirmado,
        //si uno lo checkea y otro llega mas tarde, si no se pone el if isChecked(), puede que se realice
        //este proceso nuevamente con el mismo asiento e incrementaria amountChecked erroneamente

        synchronized (seat) {
            if (seat.isNotCanceled()&&!seat.isChecked()){
                seat.setChecked(true);
                incAmountChecked();
                /*System.out.printf("\n%s Successfully CHECKED! Seat number: %d\n", Thread.currentThread().getName(), seat.getNumber());*/
            }
        }
    }

    public void cancelPayedProcess(Seat seat) {
        //Por como estan planteadas las secciones criticas, primero se saca de confirmed y luego se cancela
        //asi que todo:NUNCA SE VA A DAR EL CASO DE QUE SE ENCUENTRE UN ASIENTO CANCELADO EN CONFIRMED
        //y ademas todo asiento que se AGREGUE a canceled va a tener canceled=true
        //todo: PERO NO TODOS LOS ASIENTOS EN CONFIRMED TIENEN CHECK=TRUE

        //De todas formas, puede darse el caso de que 2 hilos hayan encontrado al mismo asiento confirmado,
        //si uno lo cancela y otro llega mas tarde, si no se pone el if isNotCanceled(), puede que se realice
        //este proceso nuevamente con el mismo asiento e incrementaria amountCanceled erroneamente

        synchronized (seat) {
            if (seat.isNotCanceled()&&!seat.isChecked()){
                synchronized (confirmed) {
                    confirmed.remove(seat);
                }
                synchronized (canceled){
                    seat.setCanceled(true);
                    canceled.add(seat);
                    incAmountCanceled();
                }
                /*System.out.printf("\n%s Successfully CANCELED! Seat number: %d\n", Thread.currentThread().getName(), seat.getNumber());
                System.out.println("Seats CANCELED IN TOTAL: " + canceled.size());
                System.out.println("-----Inside array:-----");
                for (Seat s : canceled) {
                    System.out.print(s.getNumber() + " ");
                }
                System.out.println();
                System.out.println("-----------------------");*/
                }
        }
    }

    public void verificationProcess(Seat seat) {
        //Por como estan planteadas las secciones criticas, primero se saca de confirmed y luego se verifica
        //asi que todo:NUNCA SE VA A DAR EL CASO DE QUE SE ENCUENTRE UN ASIENTO VERIFICADO EN CONFIRMED
        //y ademas todo asiento que ENTRA a verified tiene check=true

        //De todas formas, puede darse el caso de que 2 hilos hayan encontrado al mismo asiento chequeado,
        //si uno lo verifica y otro llega mas tarde, si no se pone el if isNotVerified(), puede que se realice
        //este proceso nuevamente con el mismo asiento e incrementaria amountVerified erroneamente

        synchronized (seat) {
            if (seat.isChecked()&&seat.isNotVerified()) {
                synchronized (confirmed) {
                    confirmed.remove(seat);
                }
                synchronized (verified) {
                    seat.setVerified(true);
                    verified.add(seat);
                    incAmountVerified();
                }
                /*System.out.printf("\n%s Successfully VERIFIED! Seat number: %d\n", Thread.currentThread().getName(), seat.getNumber());
                System.out.println("Seats VERIFIED IN TOTAL: " + amountVerified);
                System.out.println("Seats VERIFIED: " + verified.size());
                System.out.println("-----Inside array:-----");
                for (Seat s : verified) {
                    System.out.print(s.getNumber() + " ");
                }
                System.out.println();
                System.out.println("-----------------------");*/
            }
        }
    }

    public Seat getRandomReservation() {
        synchronized (pending) {
            //Si pending esta vacia al buscar un asiento, devuelve null
            if (!pending.isEmpty()) {
                //Aca no hace falta poner un while como en los dos metodos siguientes porque no se recorre al array
                //hasta encontrar asientos que cumplan X condicion para llevarlos a un array o no,
                //directamente se van sacando a medida que entran
                return pending.get(randomReservation.nextInt(pending.size()));
            }
            return null;
        }
    }

    public Seat getRandomConfirmed() {
        //Ningun otro hilo puede acceder a confirmed hasta que se encuentre un asiento
        //De tal forma que Confirmed NO se puede vaciar MIENTRAS busco un asiento
        synchronized (confirmed) {
            //Si confirmed esta vacia al buscar un asiento, devuelve null
            if (!confirmed.isEmpty()) {
                Seat aux = confirmed.get(randomConfirmed.nextInt(confirmed.size()));
                //Si aun no termino el proceso, busca hasta encontrar un asiento en confirmed que NO ESTE chequeado
                //(Por si se chequea al ultimo asiento mientras esta dentro del while)

                //Se hace un while y no un if porque el proceso tardaria mas en encontrar un asiento teniendo en cuenta
                //el sleep que se le hace cada vez que termina (ejemplo: si hay 100 asientos en confirmed y 99 estan chequeados,
                //cuando lo que buscamos en este metodo es un asiento NO chequeado, el proceso podria a llegar a tardar mucho en encontrar un asiento
                //valido debido a que tendria que hacer un sleep cada vez que falla en encontrarlo)

                //Se agrega una condicion mas para priorizar al proceso de chequeo por sobre el de validacion porque sino
                //puede pasar que el proceso de validacion queda esperando a que en confirmed se chequeen asientos, pero
                //estos asientos nunca llegan a chequearse porque ambos metodos usan a confirmed como lock (se produce deadlock)

                //en estos casos es util haber declarado a los contadores como volatile

                while (aux.isChecked()&&(amountChecked+amountCanceled+amountCanceledPayment)<matrix.getSeatsAmount()&&(amountChecked<amountVerified)) {
                    aux = confirmed.get(randomConfirmed.nextInt(confirmed.size()));
                }
                return aux;
            }
            return null;
        }
    }

    public Seat getRandomChecked() {
        //LA UNICA DIFERENCIA ENTRE ESTE METODO Y EL ANTERIOR ES EN EL SEGUNDO WHILE

        //Ningun otro hilo puede acceder a confirmed hasta que se encuentre un asiento
        //De tal forma que Confirmed NO se puede vaciar MIENTRAS busco un asiento

        synchronized (confirmed) {
            //Si confirmed esta vacia al buscar un asiento, devuelve null
            if (!confirmed.isEmpty()) {
                Seat aux = confirmed.get(randomChecked.nextInt(confirmed.size()));
                //Si aun no termino el proceso, busca hasta encontrar un asiento en confirmed que ESTE chequeado
                //Por como estan planteadas las secciones criticas en verificationProcess(), primero se saca de confirmed y luego se verifica
                //asi que NUNCA SE VA A DAR EL CASO DE QUE SE ENCUENTRE UN ASIENTO VERIFICADO EN CONFIRMED

                //Se hace un while y no un if porque el proceso tardaria mas en encontrar un asiento teniendo en cuenta
                //el sleep que se le hace cada vez que termina (ejemplo: si hay 100 asientos en confirmed y 99 estan sin chequear,
                //cuando lo que buscamos en este metodo es un asiento chequeado, el proceso podria a llegar a tardar mucho en encontrar un asiento
                //valido debido a que tendria que hacer un sleep cada vez que falla en encontrarlo)

                //Se agrega una condicion mas para priorizar al proceso de chequeo por sobre el de validacion porque sino
                //puede pasar que el proceso de validacion queda esperando a que en confirmed se chequeen asientos, pero
                //estos asientos nunca llegan a chequearse porque ambos metodos usan a confirmed como lock (se produce deadlock)

                //en estos casos es util haber declarado a los contadores como volatile

                while (!aux.isChecked()&&(amountVerified+amountCanceled+amountCanceledPayment)<matrix.getSeatsAmount()&&(amountChecked<amountVerified)) {
                    aux = confirmed.get(randomChecked.nextInt(confirmed.size()));
                }
                return aux;
            }
            return null;
        }
    }
    
    public int getAmountReserved() {
        synchronized (reservedLock){
            return amountReserved;
        }
    }
    
    public void incAmountReserved() {
        synchronized (reservedLock){
            amountReserved++;
        }
    }

    public int getAmountConfirmed() {
        synchronized (confirmedLock){
            return amountConfirmed;
        }
    }

    public void incAmountConfirmed() {
        synchronized (confirmedLock){
            amountConfirmed++;
        }
    }

    public int getAmountChecked() {
        synchronized (checkLock){
            return amountChecked;
        }
    }

    public void incAmountChecked() {
        synchronized (checkLock){
            amountChecked++;
        }
    }

    public int getAmountCanceledPayment() {
        synchronized (canceledPaymentLock){
            return amountCanceledPayment;
        }
    }

    public void incAmountCanceledPayment() {
        synchronized (canceledPaymentLock){
            amountCanceledPayment++;
        }
    }

    public int getAmountCanceled() {
        synchronized (canceledLock){
            return amountCanceled;
        }
    }

    public void incAmountCanceled() {
        synchronized (canceledLock){
            amountCanceled++;
        }
    }

    public int getAmountVerified() {
        synchronized (verifiedLock){
            return amountVerified;
        }
    }

    public void incAmountVerified() {
        synchronized (verifiedLock){
            amountVerified++;
        }
    }

    public boolean PendingIsEmpty() {return pending.isEmpty();}

    public boolean ConfirmedIsEmpty() {return confirmed.isEmpty();}

    //Estos metodos solamente se usan para el logger (que es un solo hilo) asi que no hace falta que sean synchronized

    public int pendingSize() { return pending.size(); }

    public int confirmedSize() { return confirmed.size(); }

    public int canceledSize() { return canceled.size(); }

    public int verifiedSize(){ return verified.size(); }
    
}
