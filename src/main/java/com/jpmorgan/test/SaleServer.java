package com.jpmorgan.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by dmitrigu on 06.11.17.
 */
public class SaleServer implements Runnable {

    // interval for sale report
    static final int INTERVAL_SALE_REPORT = 10;

    // interval for changes report
    static final int INTERVAL_CHANGE_REPORT = 50;

    // Timeout for polling incoming sale orders
    static final int GET_TIMEOUT = 20;

    // input sale orders queue
    private LinkedBlockingQueue<SaleOrder> inSaleOrders;

    // output sale orders queue
    private List<SaleOrder> outSaleOrders = new ArrayList<SaleOrder>();

    // output sale orders commands list
    private List<SaleOrder> outSaleCommands = new ArrayList<SaleOrder>();

    // stopped job flag
    private AtomicBoolean stopped;

    private int intervalSaleReport = INTERVAL_SALE_REPORT;
    private int intervalChangeReport = INTERVAL_CHANGE_REPORT;

    // 'finished' flag
    private boolean finished = false;

    // changes reports
    private List<SaleChangeReport> saleChangeReports = new ArrayList<SaleChangeReport>();

    // sale reports
    private List<SaleReport> saleReports = new ArrayList<SaleReport>();

    public SaleServer(LinkedBlockingQueue saleOrders, AtomicBoolean stopped) {
        this.inSaleOrders = saleOrders;
        this.stopped = stopped;

    }


    /*
       Sale Server main function
    */
    public void run() {

        System.out.println("**** Starting Sale Server");

        while (!finished) {

            try {
                int size = outSaleOrders.size() + outSaleCommands.size();

                if (size > 0 && (size % intervalSaleReport == 0)) {

                    // generating sales report
                    SaleReport saleReport = Report.generateSaleReport(outSaleOrders);
                    saleReports.add(saleReport);
                    System.out.println(saleReport.format());

                }

                if (size > 0 && (size % intervalChangeReport == 0)) {

                    // stopping and generating chanhges report
                    setStopReceiveNessages(true);
                    System.out.println("**** Stopping accepting messages...");

                    SaleChangeReport report = Report.generateChangesReport(outSaleCommands);
                    System.out.println(report.format());
                    saleChangeReports.add(report);

                    System.out.println("**** Resuming accepting messages...");
                    setStopReceiveNessages(false);

                }

                SaleOrder sale = null;
                sale = inSaleOrders.poll(GET_TIMEOUT, TimeUnit.SECONDS);

                if (sale == null) {
                    continue;
                }

                if (MessageType.ChangeSale.equals(sale.getMessageType())) {
                    // stopping and changing sales
                    setStopReceiveNessages(true);
                    transformSalesByCommand(sale);
                    setStopReceiveNessages(false);

                    //adding sale change commmand
                    outSaleCommands.add(sale);
                } else {
                    //adding sale order
                    outSaleOrders.add(sale);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

 
    /*
        Set 'stoped' flag for receiving messages
    */
    public void setStopReceiveNessages(boolean stopped) {        
        this.stopped.set(stopped);
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    // transforms sale orders
    private void transformSalesByCommand(final SaleOrder sale) {
        if (outSaleOrders.isEmpty()) return;
        if (sale == null) return;

        for (SaleOrder saleOrder : outSaleOrders) {

            if (!sale.getProduct().equals(saleOrder.getProduct())) {
                continue;
            }
            switch (sale.getOperation()) {
                case Add:
                    saleOrder.setPrice(saleOrder.getPrice() + sale.getPrice());
                    break;
                case Subtract:
                    Double price = saleOrder.getPrice() - sale.getPrice();
                    price = price < 0 ? 0 : price;
                    saleOrder.setPrice(price);
                    break;
                case Multiply:
                    saleOrder.setPrice(saleOrder.getPrice() * sale.getPrice());
                    break;

            }
        }
        ;

    }

    public List<SaleOrder> getOutSaleOrders() {
        return outSaleOrders;
    }

    public List<SaleOrder> getOutSaleCommands() {
        return outSaleCommands;
    }

    public List<SaleChangeReport> getSaleChangeReports() {
        return saleChangeReports;
    }

    public void setSaleChangeReports(List<SaleChangeReport> saleChangeReports) {
        this.saleChangeReports = saleChangeReports;
    }

    public List<SaleReport> getSaleReports() {
        return saleReports;
    }

    public void setSaleReports(List<SaleReport> saleReports) {
        this.saleReports = saleReports;
    }

    public int getIntervalSaleReport() {
        return intervalSaleReport;
    }

    public void setIntervalSaleReport(int intervalSaleReport) {
        this.intervalSaleReport = intervalSaleReport;
    }

    public int getIntervalChangeReport() {
        return intervalChangeReport;
    }

    public void setIntervalChangeReport(int intervalChangeReport) {
        this.intervalChangeReport = intervalChangeReport;
    }
}

