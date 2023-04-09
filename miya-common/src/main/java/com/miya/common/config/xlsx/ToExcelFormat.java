package com.miya.common.config.xlsx;

/**
 * 数据被导出到excel中时，对数据进行格式化，这样在只有小的改动情况下不用为了导出Excel而专门新建一个VO，可以直接复用原来的VO
 */
public interface ToExcelFormat {

    /**
     * 定义导出到Excel时，应当如何格式化数据
     */
    String toStringForExcel();

}
