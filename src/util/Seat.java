package util;

public class Seat {
    private volatile boolean free;
    private volatile boolean taken;
    private volatile boolean checked;
    private volatile boolean canceled;
    private volatile boolean verified;
    private volatile boolean payed;
    private final int number;

    public Seat(int number) {
        this.free = true;
        this.taken = false;
        this.payed = false;
        this.checked = false;
        this.canceled = false;
        this.verified = false;
        this.number = number;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public boolean isPayed() {
        return payed;
    }

    public void setPayed(boolean payed) {
        this.payed = payed;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isNotCanceled() {
        return !canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isNotVerified() {
        return !verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getNumber() {
        return number;
    }

    public void reserve() {
        setFree(false);
        setTaken(true);
    }
}