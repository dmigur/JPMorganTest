package com.jpmorgan.test;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dmitrigu on 06.11.17.
 * Sale client  
 */
public class SaleClient implements Runnable {

    private Queue saleOrders;
    private AtomicBoolean stop;
    private int index;

    public SaleClient(int i, Queue<SaleOrder> salequeue, AtomicBoolean stop) {
        this.saleOrders = salequeue;
        this.index = i;
        this.stop = stop;
    }

    /*
        SaleCient main procedure
    */
    @Override
    public void run() {

        System.out.println("**** Starting Client");

        // processing sales one by one
        for (SaleOrder saleOrder : SaleOrders.getSaleOrders()) {

            if (stop.get()) {
                System.out.println("**** Client " + index + " waiting...");
                while (stop.get()) {
                    try {
                        Thread.sleep(20);
                    } catch (Exception e) {
                    }
                }
                continue;
            } else {
                processCommand(saleOrder);
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }

        System.out.println(" Client " + index + " finished  ");
    }

    /*  
        Processing messages 
    */
    private void processCommand(SaleOrder saleOrder) {

        try {
            System.out.println("**** processing message " + saleOrder);
            process(saleOrder);

        } catch (Exception e) {
            System.out.println("error saving sale");
        }
    }

   
    /*  
        Processing orders, adds orders to queue
    */
    private void process(SaleOrder saleorder) {         
        if (saleOrders == null) return;
        saleOrders.add(saleorder);
    }
}
