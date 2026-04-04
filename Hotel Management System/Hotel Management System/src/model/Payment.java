package model;

import java.sql.Timestamp;

public class Payment {
    private int id;
    private int reservationId;
    private double amount;
    private Timestamp date;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReservationId() { return reservationId; }
    public void setReservationId(int reservationId) { this.reservationId = reservationId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}