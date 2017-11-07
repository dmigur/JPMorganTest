package com.jpmorgan.test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by dmitrigu on 06.11.17.
 * Report class  
 */
public class Report {

    // Sale report generation
    public static SaleReport generateSaleReport(final List<SaleOrder> data) {
        if (data == null) {
            assert (false);
            return null;
        }
        Map<Product, Integer> resultAmount = data.stream().
                collect(
                        Collectors.groupingBy(SaleOrder::getProduct,
                                Collectors.summingInt(SaleOrder::getAmount)));

        Map<Product, Double> resultSum = data.stream().
                collect(
                        Collectors.groupingBy(SaleOrder::getProduct,
                                Collectors.summingDouble(SaleOrder::getValue)));

        Integer totalAmount = data.stream().collect(Collectors.summingInt(SaleOrder::getAmount));
        Double totalSum = data.stream().collect(Collectors.summingDouble(SaleOrder::getValue));
        return new SaleReport(resultAmount, resultSum, totalAmount, totalSum);
    }


    //  Changes report generation 
    public static SaleChangeReport generateChangesReport(List<SaleOrder> data) {  
        if (data == null) {
            assert (false);
            return null;
        }
        Map<Product, Map<OperationType, List<Double>>> res = data.stream().
                collect(
                        Collectors.groupingBy(SaleOrder::getProduct,
                                Collectors.groupingBy(SaleOrder::getOperation,
                                        Collectors.mapping(SaleOrder::getPrice, Collectors.toList()
                                        ))));
        return new SaleChangeReport(res);

    }


}
